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

public final class ScrollContainer {
    private Rect viewport;
    private int contentHeight;
    private float target;
    private float offset;
    private final Anim anim = new Anim();

    public ScrollContainer(Rect viewport) {
        this.viewport = viewport;
    }

    public void layout(Rect viewport, int contentHeight) {
        this.viewport = viewport;
        this.contentHeight = Math.max(0, contentHeight);
        this.target = Math.clamp(this.target, 0.0f, (float)this.max());
        this.offset = Math.clamp(this.offset, 0.0f, (float)this.max());
    }

    public int max() {
        return Math.max(0, this.contentHeight - this.viewport.h());
    }

    public int offset() {
        return Math.round(this.offset);
    }

    public Rect viewport() {
        return this.viewport;
    }

    public void update(float deltaSeconds, boolean reduced) {
        this.offset = Math.clamp(this.anim.to(this.target, deltaSeconds, reduced), 0.0f, (float)this.max());
    }

    public boolean scroll(double mx, double my, double delta) {
        if (!this.viewport.contains(mx, my)) {
            return false;
        }
        this.target = Math.clamp(this.target - (float)delta * 30.0f, 0.0f, (float)this.max());
        return true;
    }

    public void reset() {
        this.offset = 0.0f;
        this.target = 0.0f;
        this.anim.snap(0.0f);
    }

    public void begin(class_332 c) {
        c.method_44379(this.viewport.x(), this.viewport.y(), this.viewport.right(), this.viewport.bottom());
    }

    public void end(class_332 c) {
        c.method_44380();
        if (this.max() == 0 || this.viewport.h() < 15) {
            return;
        }
        int height = Math.max(16, this.viewport.h() * this.viewport.h() / Math.max(1, this.contentHeight));
        int y = this.viewport.y() + Math.round(this.offset / (float)this.max() * (float)(this.viewport.h() - height));
        Glass.rounded(c, this.viewport.right() - 3, y, 2, height, 1, Glass.alpha(-986895, 0.3f));
    }
}

