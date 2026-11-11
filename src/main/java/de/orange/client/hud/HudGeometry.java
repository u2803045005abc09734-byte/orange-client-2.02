/*
 * Decompiled with CFR.
 */
package de.orange.client.hud;

public final class HudGeometry {
    private HudGeometry() {
    }

    public static Bounds place(int x, int y, int rawWidth, int rawHeight, float requestedScale, int screenWidth, int screenHeight) {
        int sw = Math.max(1, screenWidth);
        int sh = Math.max(1, screenHeight);
        int rw = Math.max(1, rawWidth);
        int rh = Math.max(1, rawHeight);
        float scale = Float.isFinite(requestedScale) ? Math.clamp(requestedScale, 0.1f, 8.0f) : 1.0f;
        scale = Math.min(scale, Math.min((float)sw / (float)rw, (float)sh / (float)rh));
        int w = Math.min(sw, Math.max(1, (int)Math.ceil((double)rw * (double)scale - 0.001)));
        int h = Math.min(sh, Math.max(1, (int)Math.ceil((double)rh * (double)scale - 0.001)));
        return new Bounds(Math.clamp((long)x, 0, sw - w), Math.clamp((long)y, 0, sh - h), w, h, scale);
    }

    public record Bounds(int x, int y, int width, int height, float scale) {
        public boolean contains(double mx, double my) {
            return mx >= (double)this.x && my >= (double)this.y && mx < (double)(this.x + this.width) && my < (double)(this.y + this.height);
        }
    }
}

