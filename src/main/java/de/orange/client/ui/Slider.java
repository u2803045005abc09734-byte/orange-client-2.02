/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package de.orange.client.ui;

import de.orange.client.ui.Glass;
import de.orange.client.ui.Rect;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.class_332;

public class Slider {
    protected Rect bounds;
    private final float min;
    private final float max;
    private float value;
    private boolean dragging;
    private Consumer<Float> listener = v -> {};

    public Slider(Rect bounds, float min, float max, float value) {
        if (!(max > min)) {
            throw new IllegalArgumentException("Slider maximum must exceed minimum");
        }
        this.bounds = bounds;
        this.min = min;
        this.max = max;
        this.setValue(value);
    }

    public Slider onChange(Consumer<Float> listener) {
        this.listener = listener;
        return this;
    }

    public void setBounds(Rect bounds) {
        this.bounds = bounds;
    }

    public Rect bounds() {
        return this.bounds;
    }

    public float value() {
        return this.value;
    }

    public boolean dragging() {
        return this.dragging;
    }

    public void setValue(float value) {
        this.value = Float.isFinite(value) ? Math.clamp(value, this.min, this.max) : this.min;
    }

    public void draw(class_332 c, String label, double mx, double my) {
        Glass.text(c, label, this.bounds.x(), this.bounds.y(), -6184277);
        String valueLabel = String.format(Locale.ROOT, "%.0f%%", Float.valueOf(this.value * 100.0f));
        Glass.text(c, valueLabel, this.bounds.right() - Glass.textWidth(valueLabel), this.bounds.y(), -986895);
        int cy = this.bounds.bottom() - 7;
        Glass.rounded(c, this.bounds.x(), cy - 2, this.bounds.w(), 4, 2, -13487300);
        float progress = (this.value - this.min) / (this.max - this.min);
        int progressWidth = Math.round((float)this.bounds.w() * progress);
        Glass.rounded(c, this.bounds.x(), cy - 2, progressWidth, 4, 2, Glass.alpha(Glass.ACCENT, 0.7f));
        Glass.rounded(c, this.bounds.x() + progressWidth - 4, cy - 4, 8, 8, 4, this.dragging || this.bounds.contains(mx, my) ? Glass.ACCENT : -986895);
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || !this.bounds.contains(mx, my)) {
            return false;
        }
        this.dragging = true;
        this.update(mx);
        return true;
    }

    public boolean mouseDragged(double mx, double my) {
        if (!this.dragging) {
            return false;
        }
        this.update(mx);
        return true;
    }

    public boolean mouseReleased() {
        boolean was = this.dragging;
        this.dragging = false;
        return was;
    }

    private void update(double mx) {
        float next = this.min + (this.max - this.min) * (float)Math.clamp((mx - (double)this.bounds.x()) / (double)Math.max(1, this.bounds.w()), 0.0, 1.0);
        if (next != this.value) {
            this.value = next;
            this.listener.accept(Float.valueOf(this.value));
        }
    }
}

