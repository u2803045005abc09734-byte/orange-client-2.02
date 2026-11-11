/*
 * Decompiled with CFR.
 */
package de.orange.client.config;

public final class VisualSettings {
    public float nametagScale = 1.0f;
    public int nametagText = -789510;
    public int nametagBackgroundColor = -2146364639;
    public boolean nametagBackground = true;
    public int hitboxColor = -8034827;
    public float hitboxWidth = 1.5f;
    public float fullbrightStrength = 1.0f;
    public int ambienceColor = -9141048;
    public float ambienceStrength = 0.35f;

    public void normalize() {
        this.nametagScale = VisualSettings.finiteClamp(this.nametagScale, 0.5f, 3.0f, 1.0f);
        this.hitboxWidth = VisualSettings.finiteClamp(this.hitboxWidth, 0.5f, 4.0f, 1.5f);
        this.fullbrightStrength = VisualSettings.finiteClamp(this.fullbrightStrength, 0.0f, 1.0f, 1.0f);
        this.ambienceStrength = VisualSettings.finiteClamp(this.ambienceStrength, 0.0f, 1.0f, 0.35f);
        this.nametagText |= 0xFF000000;
        this.hitboxColor |= 0xFF000000;
        this.ambienceColor |= 0xFF000000;
    }

    private static float finiteClamp(float value, float min, float max, float fallback) {
        return Float.isFinite(value) ? Math.clamp(value, min, max) : fallback;
    }
}

