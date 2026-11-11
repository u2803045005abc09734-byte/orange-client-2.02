/*
 * Decompiled with CFR.
 */
package de.orange.client.module;

import de.orange.client.config.ClientConfig;
import de.orange.client.module.Category;
import de.orange.client.module.ClientModule;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class ModuleRegistry {
    private final List<ClientModule> all;
    private final Map<Category, List<ClientModule>> categories = new EnumMap<Category, List<ClientModule>>(Category.class);

    public ModuleRegistry(Supplier<ClientConfig> config) {
        this.all = List.of(new ClientModule("armor", "Armor HUD", "Ausr\u00fcstung und genaue Haltbarkeit im Blick.", "armor", true, config, Category.PVP, Category.HUD), new ClientModule("fps", "FPS", "Die aktuelle Bildrate deines Spiels.", "fps", true, config, Category.PVP, Category.HUD), new ClientModule("keystrokes", "Keystrokes", "WASD, Space, Shift und Ctrl in Echtzeit.", "keys", true, config, Category.PVP, Category.HUD), new ClientModule("freelook", "Freelook", "Linke Alt-Taste halten, um die Kamera frei zu drehen.", "freelook", false, config, Category.PVP, Category.PLAYER), new ClientModule("coordinates", "Koordinaten", "Deine genaue Blockposition in der Welt.", "coordinates", true, config, Category.HUD, Category.PLAYER), new ClientModule("sprint", "Sprint-Status", "Zeigt, ob du gerade sprintest oder gehst.", "sprint", true, config, Category.HUD, Category.PLAYER), new ClientModule("renders", "Renders", "Farbige Konturen f\u00fcr Spieler, Endkristalle und Items.", "visuals", false, config, Category.VISUALS), new ClientModule("nametags", "Nametags", "Spieler-Namensschilder mit eigener Gr\u00f6\u00dfe, Farbe und Hintergrund.", "player", false, config, Category.VISUALS), new ClientModule("hitboxes", "Hitboxes", "Sichtbare Entity-Begrenzungen mit einstellbarer Linienbreite.", "crosshair", false, config, Category.VISUALS), new ClientModule("fullbright", "Fullbright", "Erh\u00f6ht die Umgebungsbeleuchtung \u00fcber die Client-Lightmap.", "visuals", false, config, Category.VISUALS), new ClientModule("ambience", "Ambience", "Mischt eine eigene Farbe in den Himmel der Oberwelt.", "visuals", false, config, Category.VISUALS), new ClientModule("crosshair", "Crosshair", "Ein dezentes Fadenkreuz in deiner Akzentfarbe.", "crosshair", false, config, Category.VISUALS), new ClientModule("clock", "Uhr", "Die lokale Systemzeit als kleines HUD.", "clock", true, config, Category.HUD, Category.MISC));
        for (Category category : Category.values()) {
            this.categories.put(category, this.all.stream().filter(m -> m.categories().contains((Object)category)).toList());
        }
    }

    public List<ClientModule> all() {
        return this.all;
    }

    public List<ClientModule> in(Category category) {
        return this.categories.getOrDefault((Object)category, List.of());
    }

    public ClientModule find(String id) {
        return this.all.stream().filter(m -> m.id().equals(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("Unbekanntes Modul: " + id));
    }
}

