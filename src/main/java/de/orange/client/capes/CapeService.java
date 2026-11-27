package de.orange.client.capes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_12079;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_320;
import net.minecraft.class_634;
import net.minecraft.class_640;

/**
 * Orange-owned cape and client-presence API client.
 *
 * The Minecraft access token is read from the active game session only when
 * needed. It is never written to disk or copied into the player's config.
 */
public final class CapeService {
    private static final String API = "https://orange-api-7kkb.onrender.com";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(35);
    private static final long HEARTBEAT_INTERVAL_NANOS = Duration.ofSeconds(30).toNanos();

    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .version(HttpClient.Version.HTTP_1_1)
        .build();

    private static final ExecutorService NETWORK = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "Orange-Cape-Network");
        thread.setDaemon(true);
        return thread;
    });

    private static volatile String orangeAccessToken;
    private static volatile UUID tokenForMinecraftUuid;
    private static volatile Set<UUID> orangePlayers = Set.of();
    private static volatile long nextHeartbeatAt;
    private static final ConcurrentHashMap<UUID, class_2960> CAPE_TEXTURES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Long> CAPE_REFRESH_AT = new ConcurrentHashMap<>();
    private static final Set<UUID> CAPE_LOADING = ConcurrentHashMap.newKeySet();
    private static final long CAPE_REFRESH_NANOS = Duration.ofSeconds(60).toNanos();

    private CapeService() {
    }

    public record CapeStatus(boolean published, String message) {
    }

    public record CommunityCape(UUID minecraftUuid, String username, String textureUrl) {
    }

    public static boolean isOrangeClient(UUID uuid) {
        return uuid != null && orangePlayers.contains(uuid);
    }

    /**
     * Called on the client tick. Network work is always performed away from
     * Minecraft's render/tick thread.
     */
    public static void tickPresence(class_310 client) {
        if (client == null || client.field_1724 == null || client.method_1562() == null) {
            orangePlayers = Set.of();
            nextHeartbeatAt = 0L;
            return;
        }

        class_320 session = client.method_1548();
        UUID ownUuid = session.method_44717();
        String minecraftToken = session.method_1674();
        if (ownUuid == null || minecraftToken == null || minecraftToken.isBlank() || "0".equals(minecraftToken)) {
            orangePlayers = Set.of();
            return;
        }

        long now = System.nanoTime();
        if (now < nextHeartbeatAt) {
            return;
        }
        nextHeartbeatAt = now + HEARTBEAT_INTERVAL_NANOS;

        class_634 networkHandler = client.method_1562();
        Collection<?> listedPlayers = networkHandler.method_31363();
        List<UUID> uuids = new ArrayList<>();
        for (Object value : listedPlayers) {
            if (value instanceof class_640 entry && uuids.size() < 80) {
                UUID uuid = entry.method_2966().id();
                if (uuid != null && !uuids.contains(uuid)) {
                    uuids.add(uuid);
                }
            }
        }
        if (!uuids.contains(ownUuid) && uuids.size() < 80) {
            uuids.add(ownUuid);
        }

        NETWORK.execute(() -> {
            try {
                String token = ensureOrangeToken(minecraftToken, ownUuid);
                sendAuthorized(token, "/v1/client/heartbeat", "POST", null, null);
                if (uuids.isEmpty()) {
                    orangePlayers = Set.of();
                    return;
                }

                String joined = String.join(",", uuids.stream().map(UUID::toString).toList());
                String encoded = URLEncoder.encode(joined, StandardCharsets.UTF_8);
                HttpResponse<String> response = send(
                    HttpRequest.newBuilder(URI.create(API + "/v1/client/presence?uuids=" + encoded))
                        .timeout(REQUEST_TIMEOUT)
                        .GET()
                        .build()
                );
                requireSuccess(response);
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray online = json.getAsJsonArray("online_uuids");
                Set<UUID> next = new HashSet<>();
                if (online != null) {
                    online.forEach(value -> {
                        try {
                            next.add(UUID.fromString(value.getAsString()));
                        } catch (IllegalArgumentException ignored) {
                            // Ignore malformed entries rather than breaking the tab list.
                        }
                    });
                }
                orangePlayers = Set.copyOf(next);
                // Fetch published capes for players currently in this server's player list.
                for (UUID playerUuid : uuids) {
                    ensureCapeTexture(playerUuid);
                }
            } catch (Exception error) {
                // A failed heartbeat simply lets the 90-second server-side presence expire.
                orangePlayers = Set.of();
            }
        });
    }

    /**
     * Returns the registered dynamic cape texture, or null until it has loaded.
     */
    public static class_2960 capeTexture(UUID playerUuid) {
        return playerUuid == null ? null : CAPE_TEXTURES.get(playerUuid);
    }

    private static void refreshCapeTexture(UUID playerUuid) {
        if (playerUuid == null) {
            return;
        }
        CAPE_REFRESH_AT.remove(playerUuid);
        ensureCapeTexture(playerUuid);
    }

    private static void ensureCapeTexture(UUID playerUuid) {
        long now = System.nanoTime();
        Long lastCheck = CAPE_REFRESH_AT.get(playerUuid);
        if (lastCheck != null && now - lastCheck < CAPE_REFRESH_NANOS) {
            return;
        }
        if (!CAPE_LOADING.add(playerUuid)) {
            return;
        }

        NETWORK.execute(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(
                    URI.create(API + "/v1/capes/player/" + playerUuid)
                )
                    .timeout(REQUEST_TIMEOUT)
                    .header("Accept", "image/png")
                    .GET()
                    .build();
                HttpResponse<byte[]> response = HTTP.send(request, HttpResponse.BodyHandlers.ofByteArray());
                CAPE_REFRESH_AT.put(playerUuid, System.nanoTime());

                if (response.statusCode() == 404) {
                    CAPE_TEXTURES.remove(playerUuid);
                    return;
                }
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    return;
                }

                byte[] png = response.body();
                if (png == null || png.length == 0 || png.length > 256 * 1024) {
                    return;
                }

                class_310 client = class_310.method_1551();
                if (client == null) {
                    return;
                }
                class_2960 textureId = class_2960.method_60655(
                    "orange",
                    "capes/" + playerUuid.toString().replace("-", "")
                );

                client.execute(() -> {
                    try {
                        class_1011 image = class_1011.method_49277(png);
                        if (image.method_4307() != 64 || image.method_4323() != 32) {
                            image.close();
                            return;
                        }
                        class_1043 texture = new class_1043(() -> "Orange cape " + playerUuid, image);
                        client.method_1531().method_4616(textureId, texture);
                        CAPE_TEXTURES.put(playerUuid, textureId);
                    } catch (Exception error) {
                        tracingUnavailable(playerUuid, error);
                    }
                });
            } catch (Exception error) {
                tracingUnavailable(playerUuid, error);
            } finally {
                CAPE_LOADING.remove(playerUuid);
            }
        });
    }

    private static void tracingUnavailable(UUID playerUuid, Exception error) {
        // Keep texture failures isolated to this player; retry after the refresh interval.
        CAPE_REFRESH_AT.put(playerUuid, System.nanoTime());
    }

    public static CompletableFuture<CapeStatus> loadMyCape() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String token = ensureCurrentSession();
                HttpResponse<String> response = sendAuthorized(token, "/v1/capes/me", "GET", null, null);
                requireSuccess(response);
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                boolean published = json.has("published") && json.get("published").getAsBoolean();
                return new CapeStatus(published, published ? "Du hast bereits ein Cape veröffentlicht." : "Noch kein Cape veröffentlicht.");
            } catch (Exception error) {
                throw new CompletionException(friendly(error));
            }
        }, NETWORK);
    }

    /** Downloads the current account's published PNG so it can be edited in place. */
    public static CompletableFuture<byte[]> loadMyCapePng() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String token = ensureCurrentSession();
                HttpResponse<String> response = sendAuthorized(token, "/v1/capes/me", "GET", null, null);
                requireSuccess(response);
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                if (!json.has("published") || !json.get("published").getAsBoolean()
                    || !json.has("texture_url") || json.get("texture_url").isJsonNull()) {
                    return null;
                }
                return downloadCapeTexture(json.get("texture_url").getAsString());
            } catch (Exception error) {
                throw new CompletionException(friendly(error));
            }
        }, NETWORK);
    }

    /** Returns the newest public Orange capes, limited by the server to a safe page size. */
    public static CompletableFuture<List<CommunityCape>> loadCommunityCapes() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(API + "/v1/capes"))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
                HttpResponse<String> response = send(request);
                requireSuccess(response);
                JsonArray array = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonArray("capes");
                List<CommunityCape> result = new ArrayList<>();
                if (array != null) {
                    for (int i = 0; i < array.size(); i++) {
                        try {
                            JsonObject item = array.get(i).getAsJsonObject();
                            UUID uuid = UUID.fromString(item.get("minecraft_uuid").getAsString());
                            String username = item.get("username").getAsString();
                            String textureUrl = item.get("texture_url").getAsString();
                            result.add(new CommunityCape(uuid, username, textureUrl));
                        } catch (RuntimeException ignored) {
                            // Ignore malformed list entries without losing the remaining capes.
                        }
                    }
                }
                return List.copyOf(result);
            } catch (Exception error) {
                throw new CompletionException(friendly(error));
            }
        }, NETWORK);
    }

    /** Downloads a selected community cape as a PNG template. */
    public static CompletableFuture<byte[]> loadCommunityCape(CommunityCape cape) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (cape == null) {
                    throw new IllegalArgumentException("Kein Community-Cape ausgewählt.");
                }
                return downloadCapeTexture(cape.textureUrl());
            } catch (Exception error) {
                throw new CompletionException(friendly(error));
            }
        }, NETWORK);
    }

    public static CompletableFuture<String> publishCape(byte[] png) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (png == null || png.length == 0 || png.length > 256 * 1024) {
                    throw new IllegalArgumentException("Das PNG muss kleiner als 256 KiB sein.");
                }
                String token = ensureCurrentSession();
                HttpResponse<String> response = sendAuthorized(token, "/v1/capes/me", "PUT", "image/png", png);
                requireSuccess(response);
                refreshCapeTexture(tokenForMinecraftUuid);
                return "Cape veröffentlicht! Dein neues Cape ersetzt dein bisheriges Cape.";
            } catch (Exception error) {
                throw new CompletionException(friendly(error));
            }
        }, NETWORK);
    }

    public static CompletableFuture<String> deleteMyCape() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String token = ensureCurrentSession();
                HttpResponse<String> response = sendAuthorized(token, "/v1/capes/me", "DELETE", null, null);
                requireSuccess(response);
                UUID ownUuid = tokenForMinecraftUuid;
                if (ownUuid != null) {
                    CAPE_TEXTURES.remove(ownUuid);
                    CAPE_REFRESH_AT.remove(ownUuid);
                }
                return "Dein Cape wurde entfernt.";
            } catch (Exception error) {
                throw new CompletionException(friendly(error));
            }
        }, NETWORK);
    }

    private static String ensureCurrentSession() throws Exception {
        class_310 client = class_310.method_1551();
        if (client == null) {
            throw new IllegalStateException("Minecraft ist noch nicht bereit.");
        }
        class_320 session = client.method_1548();
        UUID uuid = session.method_44717();
        String minecraftToken = session.method_1674();
        if (uuid == null || minecraftToken == null || minecraftToken.isBlank() || "0".equals(minecraftToken)) {
            throw new IllegalStateException("Bitte melde dich mit einem Microsoft-Minecraft-Konto an.");
        }
        return ensureOrangeToken(minecraftToken, uuid);
    }

    private static synchronized String ensureOrangeToken(String minecraftToken, UUID minecraftUuid) throws Exception {
        if (orangeAccessToken != null && minecraftUuid.equals(tokenForMinecraftUuid)) {
            return orangeAccessToken;
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(API + "/v1/auth/minecraft"))
            .timeout(REQUEST_TIMEOUT)
            .header("Authorization", "Bearer " + minecraftToken)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

        HttpResponse<String> response = send(request);
        requireSuccess(response);
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        String token = json.get("access_token").getAsString();
        if (token.isBlank()) {
            throw new IOException("Der Orange-Server hat kein gültiges Sitzungstoken geliefert.");
        }

        // Keep only the Orange bearer token in memory. The Minecraft token is not retained.
        tokenForMinecraftUuid = minecraftUuid;
        orangeAccessToken = token;
        return token;
    }

    private static HttpResponse<String> sendAuthorized(
        String token,
        String path,
        String method,
        String contentType,
        byte[] body
    ) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(API + path))
            .timeout(REQUEST_TIMEOUT)
            .header("Authorization", "Bearer " + token);

        if (contentType != null) {
            builder.header("Content-Type", contentType);
        }

        HttpRequest.BodyPublisher publisher = body == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofByteArray(body);

        builder.method(method, publisher);
        return send(builder.build());
    }

    private static HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static byte[] downloadCapeTexture(String texturePath) throws IOException, InterruptedException {
        if (texturePath == null || texturePath.isBlank()) {
            throw new IOException("Für dieses Cape ist keine Textur-Adresse vorhanden.");
        }
        URI textureUri = texturePath.startsWith("https://") || texturePath.startsWith("http://")
            ? URI.create(texturePath)
            : URI.create(API + (texturePath.startsWith("/") ? texturePath : "/" + texturePath));
        HttpRequest request = HttpRequest.newBuilder(textureUri)
            .timeout(REQUEST_TIMEOUT)
            .header("Accept", "image/png")
            .GET()
            .build();
        HttpResponse<byte[]> response = HTTP.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Cape-Server antwortet mit HTTP " + response.statusCode() + ".");
        }
        byte[] png = response.body();
        if (png == null || png.length == 0 || png.length > 256 * 1024) {
            throw new IOException("Die heruntergeladene Cape-PNG ist ungültig oder zu groß.");
        }
        return png;
    }

    private static void requireSuccess(HttpResponse<String> response) throws IOException {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return;
        }
        String message = "Orange-Server antwortet mit HTTP " + response.statusCode() + ".";
        try {
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            if (json.has("error")) {
                message = json.get("error").getAsString();
            }
        } catch (RuntimeException ignored) {
            // Keep the HTTP status message for a non-JSON response.
        }
        throw new IOException(message);
    }

    private static RuntimeException friendly(Exception error) {
        Throwable cause = error;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String message = cause.getMessage();
        if (message == null || message.isBlank()) {
            message = "Orange-Server ist gerade nicht erreichbar.";
        }
        return new RuntimeException(message, cause);
    }
}
