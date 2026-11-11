/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  org.slf4j.LoggerFactory
 */
package de.orange.client.modrinth;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.slf4j.LoggerFactory;

public final class ModrinthService
implements AutoCloseable {
    public static final String GAME_VERSION = "1.21.11";
    public static final long MAX_DOWNLOAD_BYTES = 0x6400000L;
    private static final int PAGE_SIZE = 12;
    private static final long MAX_JSON_BYTES = 0x400000L;
    private static final Gson GSON = new Gson();
    private static final Set<String> SORTS = Set.of("relevance", "downloads", "follows", "newest", "updated");
    private static final Set<String> CDN_HOSTS = Set.of("cdn.modrinth.com");
    private static final String USER_AGENT = "OrangeClient/1.1.0 (Minecraft 1.21.11; resource-pack browser)";
    private final Path resourcepacks;
    private final Path manifestPath;
    private final Path imageDirectory;
    private final Map<String, InstalledPack> installed = new ConcurrentHashMap<String, InstalledPack>();
    private final Object mutationLock = new Object();
    private final Map<String, CachedJson> apiCache = new LinkedHashMap<String, CachedJson>(16, 0.75f, true);
    private final Map<String, byte[]> imageCache = new LinkedHashMap<String, byte[]>(16, 0.75f, true);
    private final Map<String, CompletableFuture<byte[]>> imageRequests = new HashMap<String, CompletableFuture<byte[]>>();
    private final Map<String, CompletableFuture<Void>> removals = new HashMap<String, CompletableFuture<Void>>();
    private final ExecutorService imageExecutor = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "Orange-Pack-Images");
        t.setDaemon(true);
        return t;
    });
    private boolean manifestBlocked;
    private long apiBackoffUntil;
    private static final int MAX_IMAGE_BYTES = 0x400000;
    private static final int MAX_MEMORY_IMAGE_BYTES = 0xC00000;
    private final ExecutorService executor = Executors.newFixedThreadPool(3, runnable -> {
        Thread thread = new Thread(runnable, "Orange-Modrinth");
        thread.setDaemon(true);
        return thread;
    });
    private final ScheduledExecutorService deadlines = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Orange-Download-Timeout");
        thread.setDaemon(true);
        return thread;
    });
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15L)).followRedirects(HttpClient.Redirect.NEVER).build();
    private final Map<String, CompletableFuture<InstallResult>> downloads = new HashMap<String, CompletableFuture<InstallResult>>();
    private volatile boolean closed;

    public ModrinthService(Path resourcepacks) {
        this(resourcepacks, resourcepacks.toAbsolutePath().getParent().resolve("config/orange-client/cache"));
    }

    public ModrinthService(Path resourcepacks, Path cacheDirectory) {
        this.resourcepacks = resourcepacks.toAbsolutePath().normalize();
        this.imageDirectory = cacheDirectory.toAbsolutePath().normalize().resolve("images");
        this.manifestPath = cacheDirectory.toAbsolutePath().normalize().getParent().resolve("installed-packs.json");
        this.loadManifest();
    }

    public CompletableFuture<SearchPage> search(String query, String sort, int offset) {
        return this.search(new SearchQuery(query, sort, offset, GAME_VERSION, ""));
    }

    public CompletableFuture<SearchPage> search(SearchQuery requested) {
        SearchQuery query = requested == null ? new SearchQuery("", "relevance", 0, GAME_VERSION, "") : requested;
        return this.task(() -> {
            JsonObject result = this.api(ModrinthService.searchEndpoint(query)).getAsJsonObject();
            ArrayList<ProjectHit> hits = new ArrayList<ProjectHit>();
            for (JsonElement value : result.getAsJsonArray("hits")) {
                JsonObject hit = value.getAsJsonObject();
                hits.add(new ProjectHit(ModrinthService.string(hit, "project_id"), ModrinthService.string(hit, "slug"), ModrinthService.string(hit, "title"), ModrinthService.string(hit, "description"), hit.has("downloads") ? hit.get("downloads").getAsLong() : 0L, ModrinthService.string(hit, "author"), ModrinthService.string(hit, "icon_url"), ModrinthService.strings(hit, "versions"), ModrinthService.strings(hit, "categories")));
            }
            return new SearchPage(List.copyOf(hits), result.get("total_hits").getAsInt(), Math.max(0, query.offset()));
        }, "Resource Packs konnten nicht geladen werden");
    }

    static String searchEndpoint(SearchQuery query) {
        String version;
        String text;
        if (query == null) {
            query = new SearchQuery("", "relevance", 0, GAME_VERSION, "");
        }
        String sort = query.sort() != null && SORTS.contains(query.sort()) ? query.sort() : "relevance";
        String string = text = query.query() == null ? "" : query.query().strip();
        if (text.length() > 160) {
            text = text.substring(0, 160);
        }
        String string2 = version = query.gameVersion() == null || query.gameVersion().isBlank() ? GAME_VERSION : query.gameVersion();
        if (!version.matches("[A-Za-z0-9._+\\-]{1,40}")) {
            version = GAME_VERSION;
        }
        JsonArray facets = new JsonArray();
        JsonArray type = new JsonArray();
        type.add("project_type:resourcepack");
        facets.add((JsonElement)type);
        if (!version.equals("all")) {
            JsonArray versions = new JsonArray();
            versions.add("versions:" + version);
            facets.add((JsonElement)versions);
        }
        if (query.category() != null && query.category().matches("[a-z0-9-]{1,60}")) {
            JsonArray categories = new JsonArray();
            categories.add("categories:" + query.category());
            facets.add((JsonElement)categories);
        }
        return "/search?query=" + ModrinthService.encode(text) + "&facets=" + ModrinthService.encode(facets.toString()) + "&index=" + sort + "&limit=12&offset=" + Math.max(0, query.offset());
    }

    public CompletableFuture<ProjectDetails> details(ProjectHit hit) {
        return this.task(() -> {
            JsonArray members;
            ModrinthService.requireId(hit == null ? null : hit.id());
            JsonObject project = this.api("/project/" + hit.id()).getAsJsonObject();
            if (!"resourcepack".equals(ModrinthService.string(project, "project_type"))) {
                throw new IOException("Dieses Projekt ist kein Resource Pack.");
            }
            String author = hit.author();
            if (author.isBlank() && !ModrinthService.string(project, "team").isBlank() && !(members = this.api("/team/" + ModrinthService.string(project, "team") + "/members").getAsJsonArray()).isEmpty()) {
                author = ModrinthService.string(members.get(0).getAsJsonObject().getAsJsonObject("user"), "username");
            }
            ProjectHit full = new ProjectHit(hit.id(), ModrinthService.string(project, "slug"), ModrinthService.string(project, "title"), ModrinthService.string(project, "description"), project.has("downloads") ? project.get("downloads").getAsLong() : hit.downloads(), author, ModrinthService.string(project, "icon_url"), ModrinthService.strings(project, "game_versions"), ModrinthService.strings(project, "categories"));
            ArrayList<String> gallery = new ArrayList<String>();
            if (project.has("gallery")) {
                for (JsonElement value : project.getAsJsonArray("gallery")) {
                    String url = ModrinthService.string(value.getAsJsonObject(), "url");
                    if (url.isBlank()) continue;
                    gallery.add(url);
                }
            }
            ArrayList<PackVersion> versions = new ArrayList<PackVersion>();
            for (JsonElement value : this.api("/project/" + hit.id() + "/version").getAsJsonArray()) {
                JsonObject version = value.getAsJsonObject();
                versions.add(new PackVersion(ModrinthService.string(version, "id"), ModrinthService.string(version, "name"), ModrinthService.strings(version, "game_versions"), ModrinthService.string(version, "date_published")));
            }
            return new ProjectDetails(full, ModrinthService.string(project, "body"), List.copyOf(gallery), List.copyOf(versions));
        }, "Pack-Details konnten nicht geladen werden");
    }

    public CompletableFuture<ProjectDetails> details(String id) {
        return this.details(new ProjectHit(id, "", "", "", 0L, ""));
    }

    public CompletableFuture<InstallResult> install(ProjectHit project) {
        return this.install(project, "");
    }

    public CompletableFuture<InstallResult> update(ProjectHit project) {
        return this.install(project);
    }

    public synchronized CompletableFuture<InstallResult> install(ProjectHit project, String versionId) {
        if (this.closed) {
            return CompletableFuture.failedFuture(new IOException("Modrinth-Verbindung ist geschlossen."));
        }
        try {
            ModrinthService.requireId(project == null ? null : project.id());
        }
        catch (IOException ex) {
            return CompletableFuture.failedFuture(ex);
        }
        if (this.removals.containsKey(project.id())) {
            return CompletableFuture.failedFuture(new IOException("Dieses Pack wird gerade deinstalliert."));
        }
        CompletableFuture<InstallResult> existing = this.downloads.get(project.id());
        if (existing != null) {
            return existing;
        }
        CompletableFuture<InstallResult> future = this.task(() -> this.download(project, versionId == null ? "" : versionId), "Installation fehlgeschlagen");
        this.downloads.put(project.id(), future);
        future.whenComplete((result, error) -> {
            ModrinthService modrinthService = this;
            synchronized (modrinthService) {
                this.downloads.remove(project.id(), future);
            }
        });
        return future;
    }

    public InstalledPack installed(String projectId) {
        if (projectId == null) {
            return null;
        }
        InstalledPack pack = this.installed.get(projectId);
        return pack != null && Files.isRegularFile(this.resourcepacks.resolve(pack.filename()), new LinkOption[0]) ? pack : null;
    }

    public List<InstalledPack> installedPacks() {
        return this.installed.values().stream().filter(p -> Files.isRegularFile(this.resourcepacks.resolve(p.filename()), new LinkOption[0])).toList();
    }

    public synchronized CompletableFuture<Void> uninstall(String projectId) {
        if (this.closed) {
            return CompletableFuture.failedFuture(new IOException("Modrinth-Verbindung ist geschlossen."));
        }
        if (this.downloads.containsKey(projectId)) {
            return CompletableFuture.failedFuture(new IOException("Dieses Pack wird gerade installiert."));
        }
        if (this.removals.containsKey(projectId)) {
            return this.removals.get(projectId);
        }
        CompletableFuture<Void> future = this.task(() -> {
            Object object = this.mutationLock;
            synchronized (object) {
                this.requireManifestWritable();
                InstalledPack pack = this.installed.get(projectId);
                if (pack == null) {
                    throw new IOException("Dieses Pack wird nicht von Orange Client verwaltet.");
                }
                Path file = this.managedPath(pack);
                if (Files.exists(file, new LinkOption[0]) && !ModrinthService.ownedFile(file, pack.sha512())) {
                    throw new IOException("Das Pack wurde au\u00dferhalb des Clients ver\u00e4ndert. Es wird nicht gel\u00f6scht.");
                }
                Path staged = null;
                if (Files.exists(file, new LinkOption[0])) {
                    staged = Files.createTempFile(this.resourcepacks, ".orange-removing-", ".tmp", new FileAttribute[0]);
                    try {
                        Files.move(file, staged, StandardCopyOption.REPLACE_EXISTING);
                    }
                    catch (IOException ex) {
                        Files.deleteIfExists(staged);
                        throw ex;
                    }
                }
                this.installed.remove(projectId);
                try {
                    this.saveManifest();
                }
                catch (IOException ex) {
                    this.installed.put(projectId, pack);
                    if (staged != null) {
                        Files.move(staged, file, new CopyOption[0]);
                    }
                    throw ex;
                }
                if (staged != null) {
                    try {
                        Files.deleteIfExists(staged);
                    }
                    catch (IOException ex) {
                        LoggerFactory.getLogger((String)"Orange").warn("Deinstallierte Pack-Datei konnte nicht vollst\u00e4ndig bereinigt werden: {}", (Object)staged.getFileName());
                    }
                }
            }
            return null;
        }, "Deinstallation fehlgeschlagen");
        this.removals.put(projectId, future);
        future.whenComplete((result, error) -> {
            ModrinthService modrinthService = this;
            synchronized (modrinthService) {
                this.removals.remove(projectId, future);
            }
        });
        return future;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private InstallResult download(ProjectHit project, String requestedVersion) throws Exception {
        InstalledPack previous;
        String expectedHash;
        Object file;
        JsonArray versions;
        ModrinthService modrinthService = this;
        synchronized (modrinthService) {
            this.requireManifestWritable();
        }
        JsonObject chosenVersion = null;
        JsonObject chosen = null;
        if (!requestedVersion.isBlank()) {
            ModrinthService.requireId(requestedVersion);
            versions = new JsonArray();
            versions.add(this.api("/version/" + requestedVersion));
        } else {
            versions = this.api("/project/" + project.id() + "/version?game_versions=" + ModrinthService.encode("[\"1.21.11\"]")).getAsJsonArray();
        }
        for (JsonElement element : versions) {
            JsonObject version = element.getAsJsonObject();
            if (!ModrinthService.strings(version, "game_versions").contains(GAME_VERSION) || !project.id().equals(ModrinthService.string(version, "project_id"))) continue;
            for (JsonElement fileElement : version.getAsJsonArray("files")) {
                JsonObject candidate = fileElement.getAsJsonObject();
                if (!ModrinthService.string(candidate, "filename").toLowerCase(Locale.ROOT).endsWith(".zip")) continue;
                if (chosen == null) {
                    chosen = candidate;
                    chosenVersion = version;
                }
                if (!candidate.has("primary") || !candidate.get("primary").getAsBoolean()) continue;
                chosen = candidate;
                chosenVersion = version;
                break;
            }
            if (chosen == null) continue;
            break;
        }
        if (chosen == null) {
            throw new IOException("Kein ZIP-Resource-Pack f\u00fcr Minecraft 1.21.11 verf\u00fcgbar.");
        }
        ModrinthService.safeFilename(ModrinthService.string(chosen, "filename"));
        String versionId = ModrinthService.string(chosenVersion, "id");
        ModrinthService.requireId(versionId);
        JsonObject hashes = chosen.getAsJsonObject("hashes");
        String string = expectedHash = hashes == null ? "" : ModrinthService.string(hashes, "sha512").toLowerCase(Locale.ROOT);
        if (!expectedHash.matches("[a-f0-9]{128}")) {
            throw new IOException("Modrinth liefert keine g\u00fcltige SHA-512-Pr\u00fcfsumme.");
        }
        if (chosen.has("size") && chosen.get("size").getAsLong() > 0x6400000L) {
            throw new IOException("Dieses Pack ist gr\u00f6\u00dfer als 100 MiB.");
        }
        URI url = URI.create(ModrinthService.string(chosen, "url"));
        ModrinthService.requireDownloadUri(url);
        file = this;
        synchronized (file) {
            previous = this.installed.get(project.id());
        }
        if (previous != null && previous.versionId().equals(versionId) && previous.sha512().equals(expectedHash) && ModrinthService.ownedFile(this.managedPath(previous), expectedHash)) {
            return new InstallResult(this.managedPath(previous), previous.filename(), true);
        }
        Files.createDirectories(this.resourcepacks, new FileAttribute[0]);
        Path temporary = Files.createTempFile(this.resourcepacks, ".orange-", ".part", new FileAttribute[0]);
        Path published = null;
        try {
            this.downloadFile(url, temporary, expectedHash);
            ModrinthService.validateZip(temporary);
            Object object = this.mutationLock;
            synchronized (object) {
                Path old;
                this.requireManifestWritable();
                String stem = "orange-" + project.id() + "-" + expectedHash.substring(0, 12);
                for (int suffix = 0; suffix < 1000; ++suffix) {
                    Path destination = this.resourcepacks.resolve(stem + (String)(suffix == 0 ? "" : "-" + suffix) + ".zip");
                    try {
                        ModrinthService.publishNew(temporary, destination);
                        published = destination;
                        break;
                    }
                    catch (FileAlreadyExistsException fileAlreadyExistsException) {
                        continue;
                    }
                }
                if (published == null) {
                    throw new IOException("Es konnte kein freier Dateiname f\u00fcr das Pack gefunden werden.");
                }
                InstalledPack replacement = new InstalledPack(project.id(), versionId, published.getFileName().toString(), expectedHash);
                this.installed.put(project.id(), replacement);
                try {
                    this.saveManifest();
                }
                catch (IOException ex) {
                    if (previous == null) {
                        this.installed.remove(project.id());
                    } else {
                        this.installed.put(project.id(), previous);
                    }
                    Files.deleteIfExists(published);
                    throw ex;
                }
                if (previous != null && !previous.filename().equals(replacement.filename()) && ModrinthService.ownedFile(old = this.managedPath(previous), previous.sha512())) {
                    try {
                        Files.deleteIfExists(old);
                    }
                    catch (IOException ex) {
                        LoggerFactory.getLogger((String)"Orange").warn("Vorherige Pack-Datei konnte nicht entfernt werden: {}", (Object)old.getFileName());
                    }
                }
                InstallResult installResult = new InstallResult(published, replacement.filename(), false);
                return installResult;
            }
        }
        finally {
            Files.deleteIfExists(temporary);
        }
    }

    static void publishNew(Path temporary, Path destination) throws IOException {
        try {
            Files.createLink(destination, temporary);
        }
        catch (FileAlreadyExistsException ex) {
            throw ex;
        }
        catch (UnsupportedOperationException ex) {
            Files.move(temporary, destination, new CopyOption[0]);
        }
        catch (FileSystemException ex) {
            if (Files.exists(destination, LinkOption.NOFOLLOW_LINKS)) {
                throw new FileAlreadyExistsException(destination.toString());
            }
            Files.move(temporary, destination, new CopyOption[0]);
        }
    }

    static boolean ownedFile(Path file, String expectedHash) throws IOException {
        return Files.isRegularFile(file, new LinkOption[0]) && !Files.isSymbolicLink(file) && Files.size(file) <= 0x6400000L && ModrinthService.hash(file).equals(expectedHash);
    }

    private Path managedPath(InstalledPack pack) throws IOException {
        String filename = ModrinthService.safeFilename(pack.filename());
        Path path = this.resourcepacks.resolve(filename).normalize();
        if (!path.getParent().equals(this.resourcepacks)) {
            throw new IOException("Ung\u00fcltiger verwalteter Dateipfad.");
        }
        return path;
    }

    private void requireManifestWritable() throws IOException {
        if (this.manifestBlocked) {
            throw new IOException("Die Pack-Verwaltung ist besch\u00e4digt. installed-packs.json sichern und reparieren, bevor Packs ver\u00e4ndert werden.");
        }
        if (this.closed) {
            throw new IOException("Modrinth-Verbindung ist geschlossen.");
        }
    }

    private synchronized void loadManifest() {
        if (!Files.isRegularFile(this.manifestPath, new LinkOption[0])) {
            return;
        }
        try {
            if (Files.size(this.manifestPath) > 0x400000L) {
                throw new IOException("Verwaltungsdatei ist zu gro\u00df.");
            }
            JsonObject root = (JsonObject)GSON.fromJson(Files.readString(this.manifestPath), JsonObject.class);
            if (root == null || !root.has("packs")) {
                throw new IOException("Verwaltungsdatei hat kein packs-Feld.");
            }
            if (!root.has("schema") || root.get("schema").getAsInt() != 1) {
                throw new IOException("Nicht unterst\u00fctztes Pack-Verwaltungsformat.");
            }
            for (JsonElement value : root.getAsJsonArray("packs")) {
                InstalledPack pack = (InstalledPack)GSON.fromJson(value, InstalledPack.class);
                ModrinthService.requireId(pack.projectId());
                ModrinthService.requireId(pack.versionId());
                ModrinthService.safeFilename(pack.filename());
                if (pack.sha512() == null || !pack.sha512().matches("[a-f0-9]{128}")) {
                    throw new IOException("Ung\u00fcltige gespeicherte Pr\u00fcfsumme.");
                }
                if (this.installed.values().stream().anyMatch(p -> p.filename().equals(pack.filename()))) {
                    throw new IOException("Doppelte Dateizuordnung.");
                }
                if (this.installed.containsKey(pack.projectId())) {
                    throw new IOException("Doppelte Projektzuordnung.");
                }
                this.installed.put(pack.projectId(), pack);
            }
        }
        catch (Exception ex) {
            this.installed.clear();
            this.manifestBlocked = true;
            LoggerFactory.getLogger((String)"Orange").warn("Pack-Verwaltung konnte nicht geladen werden: {}", (Object)ex.getMessage());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void saveManifest() throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("schema", (Number)1);
        root.add("packs", GSON.toJsonTree(this.installed.values()));
        Files.createDirectories(this.manifestPath.getParent(), new FileAttribute[0]);
        Path tmp = Files.createTempFile(this.manifestPath.getParent(), "orange-packs-", ".tmp", new FileAttribute[0]);
        try {
            Files.writeString(tmp, (CharSequence)GSON.toJson((JsonElement)root), new OpenOption[0]);
            try {
                Files.move(tmp, this.manifestPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, this.manifestPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        finally {
            Files.deleteIfExists(tmp);
        }
    }

    public synchronized CompletableFuture<byte[]> image(String url) {
        if (this.closed) {
            return CompletableFuture.failedFuture(new IOException("Bildspeicher ist geschlossen."));
        }
        try {
            ModrinthService.requireDownloadUri(URI.create(url));
        }
        catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
        byte[] cached = this.imageCache.get(url);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        CompletableFuture<byte[]> active = this.imageRequests.get(url);
        if (active != null) {
            return active;
        }
        if (this.imageRequests.size() >= 64) {
            return CompletableFuture.failedFuture(new IOException("Zu viele Vorschaubilder werden gleichzeitig geladen."));
        }
        CompletableFuture<byte[]> future = CompletableFuture.supplyAsync(() -> {
            try {
                byte[] bytes;
                Files.createDirectories(this.imageDirectory, new FileAttribute[0]);
                String key = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(url.getBytes(StandardCharsets.UTF_8)));
                Path file = this.imageDirectory.resolve(key + ".img");
                if (Files.isRegularFile(file, new LinkOption[0]) && Files.size(file) <= 0x400000L && Files.size(file) > 0L) {
                    bytes = Files.readAllBytes(file);
                } else {
                    HttpResponse<InputStream> response = this.send(URI.create(url));
                    try (InputStream input = this.timedBody(response.body());){
                        ModrinthService.requireSuccess(response.statusCode());
                        if (response.headers().firstValueAsLong("Content-Length").orElse(0L) > 0x400000L) {
                            throw new IOException("Vorschaubild ist zu gro\u00df.");
                        }
                        bytes = input.readNBytes(0x400001);
                        if (bytes.length == 0 || bytes.length > 0x400000) {
                            throw new IOException("Vorschaubild ist leer oder zu gro\u00df.");
                        }
                    }
                    Path tmp = Files.createTempFile(this.imageDirectory, "image-", ".tmp", new FileAttribute[0]);
                    try {
                        Files.write(tmp, bytes, new OpenOption[0]);
                        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
                    }
                    finally {
                        Files.deleteIfExists(tmp);
                    }
                    this.trimImageDisk();
                }
                ModrinthService modrinthService = this;
                synchronized (modrinthService) {
                    this.imageCache.put(url, bytes);
                    Iterator<Map.Entry<String, byte[]>> it = this.imageCache.entrySet().iterator();
                    for (long size = this.imageCache.values().stream().mapToLong(b -> ((byte[])b).length).sum(); it.hasNext() && (this.imageCache.size() > 32 || size > 0xC00000L); size -= (long)it.next().getValue().length) {
                        it.remove();
                    }
                }
                return bytes;
            }
            catch (Exception ex) {
                throw ModrinthService.failure("Vorschaubild nicht verf\u00fcgbar", ex);
            }
        }, this.imageExecutor);
        this.imageRequests.put(url, future);
        future.whenComplete((result, error) -> {
            ModrinthService modrinthService = this;
            synchronized (modrinthService) {
                this.imageRequests.remove(url, future);
            }
        });
        return future;
    }

    private void trimImageDisk() {
        try (Stream<Path> files = Files.list(this.imageDirectory)) {
            List<Path> list = files
                .filter(path -> path.getFileName().toString().endsWith(".img"))
                .sorted(Comparator.comparingLong(path -> path.toFile().lastModified()))
                .toList();
            long bytes = 0L;
            for (Path path : list) {
                bytes += Files.size(path);
            }
            for (int i = 0; i < list.size() && (bytes > 0x4000000L || list.size() - i > 256); i++) {
                Path oldest = list.get(i);
                long size = Files.size(oldest);
                Files.deleteIfExists(oldest);
                bytes -= size;
            }
        } catch (IOException ex) {
            LoggerFactory.getLogger("Orange").debug("Vorschaubild-Cache konnte nicht bereinigt werden.", ex);
        }
    }

    private <T> CompletableFuture<T> task(IoTask<T> work, String prefix) {
        if (this.closed) {
            return CompletableFuture.failedFuture(new IOException("Modrinth-Verbindung ist geschlossen."));
        }
        try {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return work.run();
                }
                catch (Exception ex) {
                    throw ModrinthService.failure(prefix, ex);
                }
            }, this.executor);
        }
        catch (RejectedExecutionException ex) {
            return CompletableFuture.failedFuture(new IOException("Modrinth-Verbindung ist geschlossen.", ex));
        }
    }

    private static void requireId(String id) throws IOException {
        if (id == null || !id.matches("[A-Za-z0-9_-]{1,80}")) {
            throw new IOException("Ung\u00fcltige Modrinth-ID.");
        }
    }

    private static List<String> strings(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonArray()) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<String>();
        for (JsonElement value : object.getAsJsonArray(key)) {
            if (!value.isJsonPrimitive()) continue;
            result.add(value.getAsString());
        }
        return List.copyOf(result);
    }

    private void downloadFile(URI url, Path destination, String expectedHash) throws Exception {
        HttpResponse<InputStream> response = this.send(url);
        try (InputStream input = this.timedBody(response.body());){
            ModrinthService.requireSuccess(response.statusCode());
            if (response.headers().firstValueAsLong("Content-Length").orElse(0L) > 0x6400000L) {
                throw new IOException("Dieses Pack ist gr\u00f6\u00dfer als 100 MB.");
            }
            MessageDigest digest = ModrinthService.sha512();
            try (OutputStream output = Files.newOutputStream(destination, new OpenOption[0]);){
                int read;
                byte[] buffer = new byte[16384];
                long total = 0L;
                while ((read = input.read(buffer)) != -1) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new IOException("Download abgebrochen.");
                    }
                    if ((total += (long)read) > 0x6400000L) {
                        throw new IOException("Das Pack \u00fcberschreitet das Download-Limit von 100 MB.");
                    }
                    digest.update(buffer, 0, read);
                    output.write(buffer, 0, read);
                }
            }
            if (!HexFormat.of().formatHex(digest.digest()).equals(expectedHash)) {
                throw new IOException("Die SHA-512-Pr\u00fcfsumme stimmt nicht. Die Datei wurde verworfen.");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private JsonElement api(String endpoint) throws IOException, InterruptedException {
        ModrinthService modrinthService = this;
        synchronized (modrinthService) {
            CachedJson cached = this.apiCache.get(endpoint);
            if (cached != null && cached.expires() > System.currentTimeMillis()) {
                return cached.value();
            }
            if (this.apiBackoffUntil > System.currentTimeMillis()) {
                throw new IOException("Modrinth-Anfragelimit erreicht. Bitte eine Minute warten.");
            }
        }
        HttpResponse<InputStream> response = this.send(URI.create("https://api.modrinth.com/v2" + endpoint));
        try (InputStream input = this.timedBody(response.body());){
            if (response.statusCode() == 429) {
                ModrinthService modrinthService2 = this;
                synchronized (modrinthService2) {
                    this.apiBackoffUntil = System.currentTimeMillis() + 60000L;
                }
            }
            ModrinthService.requireSuccess(response.statusCode());
            byte[] bytes = input.readNBytes(0x400001);
            if ((long)bytes.length > 0x400000L) {
                throw new IOException("Modrinth-Antwort ist zu gro\u00df.");
            }
            JsonElement result = (JsonElement)GSON.fromJson(new String(bytes, StandardCharsets.UTF_8), JsonElement.class);
            if (result == null) {
                throw new IOException("Modrinth liefert eine leere Antwort.");
            }
            synchronized (this) {
                String oldest;
                this.apiCache.put(endpoint, new CachedJson(result, System.currentTimeMillis() + 45000L, bytes.length));
                for (long cacheBytes = this.apiCache.values().stream().mapToLong(CachedJson::bytes).sum(); this.apiCache.size() > 24 || cacheBytes > 0x1000000L; cacheBytes -= this.apiCache.remove(oldest).bytes()) {
                    oldest = this.apiCache.keySet().iterator().next();
                }
            }
            return result;
        }
    }

    private InputStream timedBody(InputStream body) {
        final ScheduledFuture<?> timeout = this.deadlines.schedule(() -> {
            try {
                body.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }, 60L, TimeUnit.SECONDS);
        return new FilterInputStream(body){

            @Override
            public void close() throws IOException {
                timeout.cancel(false);
                super.close();
            }
        };
    }

    private HttpResponse<InputStream> send(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(60L)).header("User-Agent", USER_AGENT).header("Accept", "application/json, application/zip;q=0.9, */*;q=0.8").GET().build();
        return this.http.send(request, HttpResponse.BodyHandlers.ofInputStream());
    }

    private static void requireSuccess(int status) throws IOException {
        if (status == 429) {
            throw new IOException("Modrinth ist gerade ausgelastet. Bitte sp\u00e4ter erneut versuchen.");
        }
        if (status >= 300 && status < 400) {
            throw new IOException("Eine unerwartete Weiterleitung wurde abgelehnt.");
        }
        if (status < 200 || status >= 300) {
            throw new IOException("Modrinth meldet HTTP " + status + ".");
        }
    }

    static void requireDownloadUri(URI uri) throws IOException {
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || !CDN_HOSTS.contains(uri.getHost().toLowerCase(Locale.ROOT)) || uri.getUserInfo() != null || uri.getPort() != -1 && uri.getPort() != 443) {
            throw new IOException("Download-Adresse ist keine erlaubte HTTPS-Adresse von Modrinth.");
        }
    }

    static String safeFilename(String filename) throws IOException {
        if (filename.isBlank() || filename.length() > 200 || filename.startsWith(".") || filename.contains("/") || filename.contains("\\") || filename.contains(":") || filename.chars().anyMatch(c -> c < 32 || c == 127) || !filename.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            throw new IOException("Das Pack hat einen ung\u00fcltigen Dateinamen.");
        }
        return filename;
    }

    static void validateZip(Path path) throws IOException {
        try (ZipFile zip = new ZipFile(path.toFile());){
            ZipEntry metadata = zip.getEntry("pack.mcmeta");
            if (metadata == null || metadata.isDirectory() || metadata.getSize() > 0x100000L) {
                throw new IOException("Die ZIP-Datei enth\u00e4lt kein g\u00fcltiges pack.mcmeta.");
            }
            try (InputStream input = zip.getInputStream(metadata);){
                byte[] bytes = input.readNBytes(0x100001);
                if (bytes.length > 0x100000) {
                    throw new IOException("pack.mcmeta ist zu gro\u00df.");
                }
                JsonObject json = (JsonObject)GSON.fromJson(new String(bytes, StandardCharsets.UTF_8), JsonObject.class);
                if (json == null || !json.has("pack") || !json.get("pack").isJsonObject()) {
                    throw new IOException("pack.mcmeta enth\u00e4lt keine Pack-Beschreibung.");
                }
            }
            Enumeration<? extends ZipEntry> entries = zip.entries();
            HashSet<String> names = new HashSet<String>();
            long uncompressed = 0L;
            int count = 0;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName().replace('\\', '/');
                if (name.startsWith("/") || name.contains(":") || name.chars().anyMatch(c -> c < 32) || List.of(name.split("/")).contains("..") || !names.add(name)) {
                    throw new IOException("Das ZIP enth\u00e4lt unsichere oder doppelte Dateipfade.");
                }
                if (++count <= 100000 && (uncompressed += Math.max(0L, entry.getSize())) <= 0x40000000L) continue;
                throw new IOException("Das entpackte Resource Pack ist zu gro\u00df.");
            }
        }
        catch (RuntimeException exception) {
            throw new IOException("Die Datei ist kein g\u00fcltiges Minecraft Resource Pack.", exception);
        }
    }

    static String hash(Path path) throws IOException {
        MessageDigest digest = ModrinthService.sha512();
        try (InputStream input = Files.newInputStream(path, new OpenOption[0]);){
            int read;
            byte[] buffer = new byte[16384];
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static MessageDigest sha512() {
        try {
            return MessageDigest.getInstance("SHA-512");
        }
        catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-512 ist nicht verf\u00fcgbar.", exception);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String string(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? "" : value.getAsString();
    }

    private static RuntimeException failure(String prefix, Exception exception) {
        String detail;
        if (exception instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        if ((detail = exception.getMessage()) == null || detail.isBlank()) {
            detail = exception.getClass().getSimpleName();
        }
        return new IllegalStateException(prefix + ": " + detail, exception);
    }

    @Override
    public synchronized void close() {
        this.closed = true;
        List.copyOf(this.downloads.values()).forEach(future -> future.cancel(true));
        this.downloads.clear();
        List.copyOf(this.removals.values()).forEach(future -> future.cancel(true));
        this.removals.clear();
        this.executor.shutdownNow();
        List.copyOf(this.imageRequests.values()).forEach(f -> f.cancel(true));
        this.imageRequests.clear();
        this.imageCache.clear();
        this.apiCache.clear();
        this.imageExecutor.shutdownNow();
        this.deadlines.shutdownNow();
        this.http.shutdownNow();
    }

    public record SearchQuery(String query, String sort, int offset, String gameVersion, String category) {
    }

    @FunctionalInterface
    private static interface IoTask<T> {
        public T run() throws Exception;
    }

    public record ProjectHit(String id, String slug, String title, String description, long downloads, String author, String iconUrl, List<String> versions, List<String> categories) {
        public ProjectHit(String id, String slug, String title, String description, long downloads, String author) {
            this(id, slug, title, description, downloads, author, "", List.of(), List.of());
        }
    }

    public record InstalledPack(String projectId, String versionId, String filename, String sha512) {
    }

    public record InstallResult(Path path, String name, boolean alreadyInstalled) {
    }

    private record CachedJson(JsonElement value, long expires, int bytes) {
    }

    public record PackVersion(String id, String name, List<String> gameVersions, String date) {
    }

    public record ProjectDetails(ProjectHit project, String body, List<String> gallery, List<PackVersion> versions) {
    }

    public record SearchPage(List<ProjectHit> hits, int total, int offset) {
    }
}

