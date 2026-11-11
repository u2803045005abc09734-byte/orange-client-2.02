/*
 * Decompiled with CFR.
 */
package de.orange.client.ui.premium;

import de.orange.client.OrangeClient;

public record GuiTheme(int background, int secondary, int panel, int hover, int accent, int secondaryAccent, int text, int muted, int disabled, int border) {
    public static final int MAIN_BACKGROUND = -16052972;
    public static final int SECONDARY_BACKGROUND = -15658207;
    public static final int PANEL_BACKGROUND = -15263702;
    public static final int HOVER_BACKGROUND = -14671559;
    public static final int PRIMARY_ACCENT = -30172;
    public static final int SECONDARY_ACCENT = -23737;
    public static final int MAIN_TEXT = -789510;
    public static final int SECONDARY_TEXT = -5920585;
    public static final int DISABLED_TEXT = -9407354;
    public static final int BORDER = -14079172;
    public static final int BACKGROUND = -16052972;
    public static final int PANEL = -15263702;
    public static final int HOVER = -14671559;
    public static final int ACCENT = -30172;
    public static final int TEXT = -789510;
    public static final int MUTED = -5920585;
    public static final GuiTheme DEFAULT = new GuiTheme(-16052972, -15658207, -15263702, -14671559, -30172, -23737, -789510, -5920585, -9407354, -14079172);
    private static int cachedAccent = -30172;
    private static GuiTheme cached = DEFAULT;

    public static GuiTheme current() {
        int value;
        OrangeClient orange = OrangeClient.instance();
        int n = value = orange == null ? -30172 : orange.config().guiAccent | 0xFF000000;
        if (value != cachedAccent) {
            cachedAccent = value;
            cached = new GuiTheme(-16052972, -15658207, -15263702, -14671559, value, GuiTheme.mix(value, -1, 0.25f), -789510, -5920585, -9407354, -14079172);
        }
        return cached;
    }

    public static int alpha(int color, float opacity) {
        return color & 0xFFFFFF | Math.clamp((long)Math.round(255.0f * opacity), 0, 255) << 24;
    }

    public static int opacity(int color, float multiplier) {
        return color & 0xFFFFFF | Math.clamp((long)Math.round((float)(color >>> 24) * multiplier), 0, 255) << 24;
    }

    public static int mix(int from, int to, float amount) {
        float t = Math.clamp(amount, 0.0f, 1.0f);
        int a = Math.round((float)(from >>> 24) + (float)((to >>> 24) - (from >>> 24)) * t);
        int r = Math.round((float)(from >>> 16 & 0xFF) + (float)((to >>> 16 & 0xFF) - (from >>> 16 & 0xFF)) * t);
        int g = Math.round((float)(from >>> 8 & 0xFF) + (float)((to >>> 8 & 0xFF) - (from >>> 8 & 0xFF)) * t);
        int b = Math.round((float)(from & 0xFF) + (float)((to & 0xFF) - (from & 0xFF)) * t);
        return a << 24 | r << 16 | g << 8 | b;
    }
}

