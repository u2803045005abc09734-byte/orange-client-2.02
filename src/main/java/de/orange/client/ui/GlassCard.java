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

public class GlassCard
extends GlassPanel {
    private Rect visual;

    public GlassCard(Rect bounds) {
        super(bounds);
        this.visual = bounds;
    }

    @Override
    public Rect bounds() {
        return this.visual;
    }

    @Override
    public boolean contains(double x, double y) {
        return this.visual.contains(x, y);
    }

    @Override
    public void setBounds(Rect bounds) {
        super.setBounds(bounds);
        this.visual = bounds;
    }

    @Override
    public void draw(class_332 c, double mx, double my, float dt, boolean selected, boolean reduced, float alpha) {
        float h = this.hover.to(this.contains(mx, my) ? 1.0f : 0.0f, dt, reduced);
        float s = this.selection.to(selected ? 1.0f : 0.0f, dt, reduced);
        int expandX = Math.round((float)this.bounds.w() * (0.008f * h + 0.012f * s));
        int expandY = Math.round((float)this.bounds.h() * (0.008f * h + 0.012f * s));
        this.visual = new Rect(this.bounds.x() - expandX, this.bounds.y() - expandY, this.bounds.w() + expandX * 2, this.bounds.h() + expandY * 2);
        Glass.panel(c, this.visual, h, s, alpha);
    }
}

