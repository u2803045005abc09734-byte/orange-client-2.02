/*
 * Decompiled with CFR.
 */
package de.orange.client.config;

public final class RenderSettings {
    public Target players = new Target();
    public Target crystals = new Target();
    public Target items = new Target();
    public int primary = -8034827;
    public int secondary = -5927681;
    public float rainbowSpeed = 0.25f;
    public float gradientSpeed = 0.2f;
    public float gradientSpread = 12.0f;

    public void normalize() {
        if (this.players == null) {
            this.players = new Target();
        }
        if (this.crystals == null) {
            this.crystals = new Target();
        }
        if (this.items == null) {
            this.items = new Target();
        }
        this.players.normalize();
        this.crystals.normalize();
        this.items.normalize();
        this.primary |= 0xFF000000;
        this.secondary |= 0xFF000000;
        this.rainbowSpeed = RenderSettings.finiteClamp(this.rainbowSpeed, 0.05f, 2.0f, 0.25f);
        this.gradientSpeed = RenderSettings.finiteClamp(this.gradientSpeed, 0.0f, 2.0f, 0.2f);
        this.gradientSpread = RenderSettings.finiteClamp(this.gradientSpread, 0.0f, 64.0f, 12.0f);
    }

    public void applyPrimaryToTargets(int color) {
        this.primary = color | 0xFF000000;
        if (this.players == null) {
            this.players = new Target();
        }
        if (this.crystals == null) {
            this.crystals = new Target();
        }
        if (this.items == null) {
            this.items = new Target();
        }
        this.crystals.primary = this.items.primary = this.primary;
        this.players.primary = this.items.primary;
    }

    public void applySecondaryToTargets(int color) {
        this.secondary = color | 0xFF000000;
        if (this.players == null) {
            this.players = new Target();
        }
        if (this.crystals == null) {
            this.crystals = new Target();
        }
        if (this.items == null) {
            this.items = new Target();
        }
        this.crystals.secondary = this.items.secondary = this.secondary;
        this.players.secondary = this.items.secondary;
    }

    private static float finiteClamp(float value, float min, float max, float fallback) {
        return Float.isFinite(value) ? Math.clamp(value, min, max) : fallback;
    }

    public static final class Target {
        public boolean glow = true;
        public float strength = 0.68f;
        public ColourMode mode = ColourMode.GRADIENT;
        public int primary = -8034827;
        public int secondary = -5927681;
        public int custom = -8034827;

        public void normalize() {
            this.strength = RenderSettings.finiteClamp(this.strength, 0.0f, 1.0f, 0.68f);
            if (this.mode == null) {
                this.mode = ColourMode.GRADIENT;
            }
            this.primary |= 0xFF000000;
            this.secondary |= 0xFF000000;
            this.custom |= 0xFF000000;
        }
    }

    public static enum ColourMode {
        SOLID,
        GRADIENT,
        RAINBOW,
        CUSTOM;

    }
}

