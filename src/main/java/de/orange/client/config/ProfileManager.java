/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  org.slf4j.LoggerFactory
 */
package de.orange.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.orange.client.config.ClientConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.LoggerFactory;

public final class ProfileManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final long MAX_BYTES = 0x100000L;
    private final Path root;
    private final Path directory;
    private final Path exports;
    private final Map<String, String> names = new LinkedHashMap<String, String>();
    private final List<String> warnings = new ArrayList<String>();
    private String active = "Default";
    private ClientConfig config = new ClientConfig();

    public ProfileManager(Path configDirectory) {
        this.root = configDirectory.toAbsolutePath().normalize().resolve("orange-client");
        this.directory = this.root.resolve("profiles");
        this.exports = this.root.resolve("exports");
        try {
            Files.createDirectories(this.directory, new FileAttribute[0]);
            try (Stream<Path> files = Files.list(this.directory);){
                for (Path path : files.filter(p -> p.getFileName().toString().endsWith(".json")).toList()) {
                    try {
                        Profile profile = ProfileManager.read(path);
                        if (!path.getFileName().equals(this.file(profile.name).getFileName())) {
                            throw new IOException("Profil-Dateiname passt nicht zum Namen.");
                        }
                        if (this.names.putIfAbsent(ProfileManager.key(profile.name), profile.name) == null) continue;
                        throw new IOException("Doppelter Profilname.");
                    }
                    catch (IOException exception) {
                        this.quarantine(path, exception.getMessage());
                    }
                }
            }
            if (this.names.isEmpty()) {
                this.writeProfile("Default", this.config, false);
                this.names.put(ProfileManager.key("Default"), "Default");
            }
            this.active = this.list().getFirst();
            Path state = this.root.resolve("state.json");
            if (Files.isRegularFile(state, new LinkOption[0])) {
                try {
                    JsonObject json = ProfileManager.readJson(state);
                    String selected = json.get("active").getAsString();
                    if (this.names.containsKey(ProfileManager.key(selected))) {
                        this.active = this.names.get(ProfileManager.key(selected));
                    }
                }
                catch (IOException | RuntimeException exception) {
                    this.warn("Aktives Profil konnte nicht gelesen werden; erstes Profil geladen.");
                }
            }
            this.config = ProfileManager.read((Path)this.file((String)this.active)).config;
            this.writeState();
        }
        catch (IOException | RuntimeException exception) {
            this.warn("Profilordner ist nicht verf\u00fcgbar: " + exception.getMessage());
            this.names.putIfAbsent(ProfileManager.key(this.active), this.active);
        }
    }

    public ClientConfig config() {
        return this.config;
    }

    public String active() {
        return this.active;
    }

    public Path exportsDirectory() {
        return this.exports;
    }

    public List<String> warnings() {
        return List.copyOf(this.warnings);
    }

    public List<String> list() {
        return this.names.values().stream()
            .sorted(Comparator.<String, Boolean>comparing(name -> !name.equals("Default"))
                .thenComparing(Comparator.naturalOrder()))
            .toList();
    }

    public synchronized void save() throws IOException {
        this.config.normalize();
        this.writeProfile(this.active, this.config, true);
        this.writeState();
    }

    public synchronized void create(String name) throws IOException {
        name = ProfileManager.validateName(name);
        this.requireNew(name);
        this.writeProfile(name, this.config.copy(), false);
        this.names.put(ProfileManager.key(name), name);
    }

    public synchronized void load(String name) throws IOException {
        ClientConfig next;
        String actual = this.requireExisting(name);
        if (actual.equals(this.active)) {
            this.save();
            return;
        }
        try {
            next = ProfileManager.read((Path)this.file((String)actual)).config;
        }
        catch (IOException exception) {
            throw new IOException("Profil besch\u00e4digt oder nicht lesbar. Aktuelle Einstellungen bleiben erhalten: " + actual, exception);
        }
        this.save();
        String previous = this.active;
        this.active = actual;
        try {
            this.writeState();
        }
        catch (IOException exception) {
            this.active = previous;
            throw exception;
        }
        this.config = next;
    }

    public synchronized void rename(String oldName, String newName) throws IOException {
        String actual = this.requireExisting(oldName);
        if (actual.equals(newName = ProfileManager.validateName(newName))) {
            return;
        }
        if (ProfileManager.key(actual).equals(ProfileManager.key(newName))) {
            ClientConfig data = actual.equals(this.active) ? this.config.copy() : ProfileManager.read((Path)this.file((String)actual)).config;
            this.writeProfile(newName, data, true);
            this.names.put(ProfileManager.key(newName), newName);
            if (this.active.equals(actual)) {
                this.active = newName;
                this.writeState();
            }
            return;
        }
        this.requireNew(newName);
        ClientConfig data = actual.equals(this.active) ? this.config.copy() : ProfileManager.read((Path)this.file((String)actual)).config;
        this.writeProfile(newName, data, false);
        try {
            Files.delete(this.file(actual));
        }
        catch (IOException exception) {
            Files.deleteIfExists(this.file(newName));
            throw exception;
        }
        this.names.remove(ProfileManager.key(actual));
        this.names.put(ProfileManager.key(newName), newName);
        if (this.active.equals(actual)) {
            this.active = newName;
            this.writeState();
        }
    }

    public synchronized void duplicate(String source, String target) throws IOException {
        source = this.requireExisting(source);
        target = ProfileManager.validateName(target);
        this.requireNew(target);
        ClientConfig data = source.equals(this.active) ? this.config.copy() : ProfileManager.read((Path)this.file((String)source)).config;
        this.writeProfile(target, data, false);
        this.names.put(ProfileManager.key(target), target);
    }

    public synchronized void delete(String name) throws IOException {
        String actual = this.requireExisting(name);
        if (this.names.size() == 1) {
            throw new IOException("Das letzte Profil kann nicht gel\u00f6scht werden.");
        }
        if (actual.equals(this.active)) {
            throw new IOException("Wechsle vor dem L\u00f6schen zu einem anderen Profil.");
        }
        Files.delete(this.file(actual));
        this.names.remove(ProfileManager.key(actual));
    }

    public synchronized Path exportProfile(String name) throws IOException {
        String actual = this.requireExisting(name);
        ClientConfig data = actual.equals(this.active) ? this.config.copy() : ProfileManager.read((Path)this.file((String)actual)).config;
        Files.createDirectories(this.exports, new FileAttribute[0]);
        String friendly = actual.replace(' ', '-');
        Path destination = this.exports.resolve(friendly + ".orange-profile.json");
        int i = 2;
        while (Files.exists(destination, new LinkOption[0])) {
            destination = this.exports.resolve(friendly + "-" + i + ".orange-profile.json");
            ++i;
        }
        ProfileManager.atomicWrite(destination, ProfileManager.encode(actual, data), false);
        return destination;
    }

    public synchronized String importProfile(Path source) throws IOException {
        if (!Files.isRegularFile(source, new LinkOption[0]) || Files.isSymbolicLink(source)) {
            throw new IOException("Bitte eine normale JSON-Profildatei ausw\u00e4hlen.");
        }
        Profile incoming = ProfileManager.read(source);
        this.requireNew(incoming.name);
        this.writeProfile(incoming.name, incoming.config, false);
        this.names.put(ProfileManager.key(incoming.name), incoming.name);
        return incoming.name;
    }

    private void requireNew(String name) throws IOException {
        if (this.names.containsKey(ProfileManager.key(name)) || Files.exists(this.file(name), new LinkOption[0])) {
            throw new IOException("Ein Profil mit diesem Namen existiert bereits: " + name);
        }
    }

    private String requireExisting(String name) throws IOException {
        String actual = this.names.get(ProfileManager.key(ProfileManager.validateName(name)));
        if (actual == null) {
            throw new IOException("Profil nicht gefunden: " + name);
        }
        return actual;
    }

    public static String validateName(String raw) throws IOException {
        if (raw == null) {
            throw new IOException("Bitte einen Profilnamen eingeben.");
        }
        String value = Normalizer.normalize(raw.strip(), Normalizer.Form.NFKC);
        if (value.isEmpty() || value.length() > 48 || !value.matches("[\\p{L}\\p{N} _-]+")) {
            throw new IOException("Profilname: 1\u201348 Buchstaben/Ziffern, Leerzeichen, - oder _.");
        }
        String upper = value.toUpperCase(Locale.ROOT);
        if (upper.matches("CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]")) {
            throw new IOException("Dieser Profilname ist reserviert.");
        }
        return value;
    }

    private static String key(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    }

    private Path file(String name) {
        try {
            String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(ProfileManager.key(name).getBytes(StandardCharsets.UTF_8)));
            return this.directory.resolve(digest + ".json");
        }
        catch (NoSuchAlgorithmException exception) {
            throw new AssertionError((Object)exception);
        }
    }

    private static JsonObject readJson(Path path) throws IOException {
        JsonObject jsonObject;
        block10: {
            if (!Files.isRegularFile(path, new LinkOption[0]) || Files.isSymbolicLink(path) || Files.size(path) > 0x100000L) {
                throw new IOException("Ung\u00fcltige oder zu gro\u00dfe Profildatei.");
            }
            BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
            try {
                JsonElement element = JsonParser.parseReader((Reader)reader);
                if (!element.isJsonObject()) {
                    throw new IOException("JSON-Objekt erwartet.");
                }
                jsonObject = element.getAsJsonObject();
                if (reader == null) break block10;
            }
            catch (Throwable throwable) {
                try {
                    if (reader != null) {
                        try {
                            reader.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (RuntimeException exception) {
                    throw new IOException("Ung\u00fcltiges Profil-JSON.", exception);
                }
            }
            reader.close();
        }
        return jsonObject;
    }

    private static Profile read(Path path) throws IOException {
        JsonObject json = ProfileManager.readJson(path);
        try {
            if (!(json.has("schema") && json.get("schema").getAsInt() == 1 && json.has("config") && json.get("config").isJsonObject())) {
                throw new IOException("Nicht unterst\u00fctztes Orange-Profilformat.");
            }
            String name = ProfileManager.validateName(json.get("name").getAsString());
            ClientConfig data = (ClientConfig)GSON.fromJson(json.get("config"), ClientConfig.class);
            if (data == null) {
                throw new IOException("Profil enth\u00e4lt keine Einstellungen.");
            }
            data.normalize();
            return new Profile(name, data);
        }
        catch (RuntimeException exception) {
            throw new IOException("Profil enth\u00e4lt ung\u00fcltige Werte.", exception);
        }
    }

    private static String encode(String name, ClientConfig data) {
        JsonObject root = new JsonObject();
        root.addProperty("schema", (Number)1);
        root.addProperty("name", name);
        root.add("config", GSON.toJsonTree((Object)data));
        return GSON.toJson((JsonElement)root);
    }

    private void writeProfile(String name, ClientConfig data, boolean replace) throws IOException {
        ProfileManager.atomicWrite(this.file(name), ProfileManager.encode(name, data), replace);
    }

    private void writeState() throws IOException {
        JsonObject state = new JsonObject();
        state.addProperty("active", this.active);
        ProfileManager.atomicWrite(this.root.resolve("state.json"), GSON.toJson((JsonElement)state), true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void atomicWrite(Path destination, String json, boolean replace) throws IOException {
        Files.createDirectories(destination.getParent(), new FileAttribute[0]);
        if (!replace && Files.exists(destination, new LinkOption[0])) {
            throw new FileAlreadyExistsException(destination.toString());
        }
        Path temporary = Files.createTempFile(destination.getParent(), ".orange-", ".tmp", new FileAttribute[0]);
        try {
            Files.writeString(temporary, (CharSequence)json, StandardCharsets.UTF_8, new OpenOption[0]);
            if (!replace) {
                try {
                    Files.createLink(destination, temporary);
                }
                catch (UnsupportedOperationException exception) {
                    Files.move(temporary, destination, new CopyOption[0]);
                }
            } else {
                try {
                    Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                }
                catch (AtomicMoveNotSupportedException exception) {
                    Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        finally {
            Files.deleteIfExists(temporary);
        }
    }

    private void quarantine(Path source, String reason) {
        String message = "Besch\u00e4digtes Profil gesichert: " + String.valueOf(source.getFileName()) + ". " + reason;
        try {
            Files.move(source, source.resolveSibling(String.valueOf(source.getFileName()) + ".broken-" + String.valueOf(UUID.randomUUID())), new CopyOption[0]);
        }
        catch (IOException exception) {
            message = message + " Sicherung konnte nicht angelegt werden; Original bleibt erhalten.";
        }
        this.warn(message);
    }

    private void warn(String message) {
        this.warnings.add(message);
        LoggerFactory.getLogger((String)"Orange Client").warn(message);
    }

    private record Profile(String name, ClientConfig config) {
    }
}

