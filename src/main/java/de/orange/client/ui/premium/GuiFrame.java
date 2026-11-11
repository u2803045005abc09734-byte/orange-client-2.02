/*
 * Decompiled with CFR.
 */
package de.orange.client.ui.premium;

public record GuiFrame(double mouseX, double mouseY, float dt, float alpha, boolean reduced) {
    public GuiFrame {
        dt = Math.clamp(Float.isFinite(dt) ? dt : 0.0f, 0.0f, 0.25f);
        alpha = Math.clamp(Float.isFinite(alpha) ? alpha : 1.0f, 0.0f, 1.0f);
    }
}

