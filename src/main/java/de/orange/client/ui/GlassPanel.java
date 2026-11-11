/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package de.orange.client.ui;

import de.orange.client.ui.Anim;
import de.orange.client.ui.Glass;
import de.orange.client.ui.Rect;
import net.minecraft.class_332;

public class GlassPanel {
    protected Rect bounds;
    protected final Anim hover = new Anim();
    protected final Anim selection = new Anim();

    public GlassPanel(Rect bounds) {
        this.bounds = bounds;
    }

    public Rect bounds() {
        return this.bounds;
    }

    public void setBounds(Rect bounds) {
        this.bounds = bounds;
    }

    public boolean contains(double x, double y) {
        return this.bounds.contains(x, y);
    }

    public void draw(class_332 c, double mx, double my, float dt, boolean selected, boolean reduced, float alpha) {
        float h = this.hover.to(this.contains(mx, my) ? 1.0f : 0.0f, dt, reduced);
        float s = this.selection.to(selected ? 1.0f : 0.0f, dt, reduced);
        Glass.panel(c, this.bounds, h, s, alpha);
    }

    public float hoverProgress() {
        return this.hover.value();
    }

    public float selectedProgress() {
        return this.selection.value();
    }
}

