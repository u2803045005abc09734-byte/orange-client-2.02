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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.class_310;
import net.minecraft.class_320;
import net.minecraft.class_634;

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

    private CapeService() {
    }

    public record CapeStatus(boolean published, String message) {
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
            if (value instanceof UUID uuid && uuids.size() < 80) {
                uuids.add(uuid);
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
            } catch (Exception error) {
                // A failed heartbeat simply lets the 90-second server-side presence expire.
                orangePlayers = Set.of();
            }
        });
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

    public static CompletableFuture<String> publishCape(byte[] png) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (png == null || png.length == 0 || png.length > 256 * 1024) {
                    throw new IllegalArgumentException("Das PNG muss kleiner als 256 KiB sein.");
                }
                String token = ensureCurrentSession();
                HttpResponse<String> response = sendAuthorized(token, "/v1/capes/me", "PUT", "image/png", png);
                requireSuccess(response);
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
