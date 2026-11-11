/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package de.orange.client.ui;

import de.orange.client.ui.Glass;
import de.orange.client.ui.GlassPanel;
import de.orange.client.ui.Rect;
import net.minecraft.class_332;

public final class GlassButton
extends GlassPanel {
    private String label;
    private final Runnable action;
    private boolean enabled = true;

    public GlassButton(Rect bounds, String label, Runnable action) {
        super(bounds);
        this.label = label;
        this.action = action;
    }

    public void label(String label) {
        this.label = label;
    }

    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void draw(class_332 c, double mx, double my, float dt, boolean reduced, float alpha) {
        float h = this.hover.to(this.enabled && this.contains(mx, my) ? 1.0f : 0.0f, dt, reduced);
        Glass.panel(c, this.bounds, h, 0.0f, alpha * (this.enabled ? 1.0f : 0.55f));
        Glass.centered(c, Glass.trim(this.label, Math.max(1, this.bounds.w() - 12)), this.bounds.centerX(), this.bounds.y() + (this.bounds.h() - 10) / 2, Glass.opacity(h > 0.2f ? -986895 : -6184277, alpha * (this.enabled ? 1.0f : 0.55f)));
    }

    public boolean click(double x, double y, int button) {
        if (button != 0 || !this.enabled || !this.contains(x, y)) {
            return false;
        }
        this.action.run();
        return true;
    }
}

