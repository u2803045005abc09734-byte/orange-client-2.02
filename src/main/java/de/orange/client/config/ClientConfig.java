/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 */
package de.orange.client.config;

import com.google.gson.Gson;
import de.orange.client.config.RenderSettings;
import de.orange.client.config.VisualSettings;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class ClientConfig {
    private static final Pattern MODULE_ID = Pattern.compile("[a-z0-9_-]{1,64}");
    private static final Pattern EXPANSION_ID = Pattern.compile("[a-z0-9_.-]{1,96}");
    private static final Set<String> CLICK_GUI_CATEGORIES = Set.of("PVP", "VISUALS", "HUD", "PLAYER", "MISC");
    private static final Set<String> RENDER_PAGES = Set.of("PLAYERS", "CRYSTALS", "END_CRYSTALS", "HAND", "ITEMS", "COLOURS");
    public float hudScale = 1.0f;
    public float uiOpacity = 0.65f;
    public boolean reducedMotion = false;
    public boolean blur = true;
    public int accent = -26020;
    public int guiAccent = -30172;
    public String clickGuiCategory = "VISUALS";
    public Map<String, Boolean> clickGuiExpanded = ClientConfig.defaultExpanded();
    public String rendersPage = "PLAYERS";
    public RenderSettings renders = new RenderSettings();
    public VisualSettings visuals = new VisualSettings();
    public Map<String, ModuleState> modules = new LinkedHashMap<String, ModuleState>();

    public ClientConfig() {
        this.defaults("armor", true, 12, 140);
        this.defaults("fps", true, 12, 12);
        this.defaults("coordinates", true, 12, 40);
        this.defaults("keystrokes", false, 200, 12);
        this.defaults("sprint", false, 12, 252);
        this.defaults("freelook", false, 12, 12);
        this.defaults("crosshair", false, 12, 12);
        this.defaults("clock", false, 200, 120);
        this.defaults("renders", false, 12, 12);
        this.defaults("nametags", false, 12, 12);
        this.defaults("hitboxes", false, 12, 12);
        this.defaults("fullbright", false, 12, 12);
        this.defaults("ambience", false, 12, 12);
    }

    public static ClientConfig defaults() {
        return new ClientConfig();
    }

    private static Map<String, Boolean> defaultExpanded() {
        LinkedHashMap<String, Boolean> values = new LinkedHashMap<String, Boolean>();
        values.put("renders", true);
        values.put("nametags", false);
        values.put("hitboxes", false);
        values.put("fullbright", false);
        values.put("ambience", false);
        return values;
    }

    public boolean expanded(String id, boolean fallback) {
        Boolean value;
        ClientConfig.requireExpansionId(id);
        if (this.clickGuiExpanded == null) {
            this.clickGuiExpanded = ClientConfig.defaultExpanded();
        }
        return (value = this.clickGuiExpanded.get(id)) == null ? fallback : value;
    }

    public void setExpanded(String id, boolean value) {
        ClientConfig.requireExpansionId(id);
        if (this.clickGuiExpanded == null) {
            this.clickGuiExpanded = ClientConfig.defaultExpanded();
        }
        this.clickGuiExpanded.put(id, value);
    }

    private static void requireExpansionId(String id) {
        if (id == null || !EXPANSION_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Ung\u00fcltige Abschnittskennung");
        }
    }

    private void defaults(String id, boolean enabled, int x, int y) {
        this.modules.put(id, new ModuleState(enabled, x, y));
    }

    public ModuleState module(String id) {
        if (id == null || !MODULE_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Ung\u00fcltige Modulkennung");
        }
        if (this.modules == null) {
            this.modules = new LinkedHashMap<String, ModuleState>();
        }
        return this.modules.computeIfAbsent(id, ignored -> new ModuleState(false, 12, 12));
    }

    public ClientConfig copy() {
        ClientConfig result = (ClientConfig)new Gson().fromJson(new Gson().toJson((Object)this), ClientConfig.class);
        result.normalize();
        return result;
    }

    public void normalize() {
        this.hudScale = ClientConfig.finiteClamp(this.hudScale, 0.5f, 2.0f, 1.0f);
        this.uiOpacity = ClientConfig.finiteClamp(this.uiOpacity, 0.25f, 0.95f, 0.65f);
        this.accent |= 0xFF000000;
        this.guiAccent |= 0xFF000000;
        if (!CLICK_GUI_CATEGORIES.contains(this.clickGuiCategory == null ? "" : this.clickGuiCategory)) {
            this.clickGuiCategory = "VISUALS";
        }
        if (!RENDER_PAGES.contains(this.rendersPage == null ? "" : this.rendersPage)) {
            this.rendersPage = "PLAYERS";
        }
        if (this.clickGuiExpanded == null) {
            this.clickGuiExpanded = ClientConfig.defaultExpanded();
        }
        this.clickGuiExpanded.entrySet().removeIf(e -> e.getKey() == null || !EXPANSION_ID.matcher((CharSequence)e.getKey()).matches() || e.getValue() == null);
        ClientConfig.defaultExpanded().forEach(this.clickGuiExpanded::putIfAbsent);
        if (this.renders == null) {
            this.renders = new RenderSettings();
        }
        if (this.visuals == null) {
            this.visuals = new VisualSettings();
        }
        this.renders.normalize();
        this.visuals.normalize();
        ClientConfig defaults = new ClientConfig();
        if (this.modules == null) {
            this.modules = new LinkedHashMap<String, ModuleState>();
        }
        this.modules.entrySet().removeIf(e -> e.getKey() == null || !MODULE_ID.matcher((CharSequence)e.getKey()).matches());
        defaults.modules.forEach((id, state) -> this.modules.putIfAbsent((String)id, (ModuleState)state));
        this.modules.replaceAll((id, state) -> {
            if (state == null) {
                state = defaults.modules.getOrDefault(id, new ModuleState(false, 12, 12));
            }
            state.scale = ClientConfig.finiteClamp(state.scale, 0.5f, 3.0f, 1.0f);
            state.opacity = ClientConfig.finiteClamp(state.opacity, 0.1f, 1.0f, 0.8f);
            state.x = Math.clamp((long)state.x, 0, 32768);
            state.y = Math.clamp((long)state.y, 0, 32768);
            state.color |= 0xFF000000;
            return state;
        });
    }

    private static float finiteClamp(float value, float min, float max, float fallback) {
        return Float.isFinite(value) ? Math.clamp(value, min, max) : fallback;
    }

    public static final class ModuleState {
        public boolean enabled;
        public int x;
        public int y;
        public float scale = 1.0f;
        public float opacity = 0.8f;
        public int color = -723984;

        public ModuleState() {
        }

        public ModuleState(boolean enabled, int x, int y) {
            this.enabled = enabled;
            this.x = x;
            this.y = y;
        }
    }
}

