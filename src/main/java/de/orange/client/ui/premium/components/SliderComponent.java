/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package de.orange.client.ui.premium.components;

import de.orange.client.ui.Anim;
import de.orange.client.ui.Rect;
import de.orange.client.ui.premium.GuiFrame;
import de.orange.client.ui.premium.GuiPrimitives;
import de.orange.client.ui.premium.GuiTheme;
import java.util.Objects;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
import net.minecraft.class_332;

public final class SliderComponent {
    private final double min;
    private final double max;
    private final DoubleSupplier getter;
    private final DoubleConsumer setter;
    private DoubleFunction<String> formatter = value -> Math.round(value * 100.0) + "%";
    private Rect bounds = new Rect(0, 0, 0, 0);
    private final Anim hover = new Anim();
    private boolean dragging;

    public SliderComponent(double min, double max, DoubleSupplier getter, DoubleConsumer setter) {
        if (!Double.isFinite(min) || !Double.isFinite(max) || !Double.isFinite(max - min) || max <= min) {
            throw new IllegalArgumentException("Invalid slider range");
        }
        this.min = min;
        this.max = max;
        this.getter = Objects.requireNonNull(getter);
        this.setter = Objects.requireNonNull(setter);
    }

    public SliderComponent format(DoubleFunction<String> formatter) {
        this.formatter = Objects.requireNonNull(formatter);
        return this;
    }

    public String formatted() {
        return this.formatter.apply(this.value());
    }

    public double value() {
        double value = this.getter.getAsDouble();
        return Double.isFinite(value) ? Math.clamp(value, this.min, this.max) : this.min;
    }

    public double min() {
        return this.min;
    }

    public double max() {
        return this.max;
    }

    public boolean dragging() {
        return this.dragging;
    }

    public void setBounds(Rect bounds) {
        this.bounds = Objects.requireNonNull(bounds);
    }

    public Rect bounds() {
        return this.bounds;
    }

    private boolean visible() {
        return this.bounds.w() >= 2 && this.bounds.h() >= 2;
    }

    private int diameter() {
        return Math.max(0, Math.min(8, Math.min(this.bounds.w(), this.bounds.h())));
    }

    private double left() {
        return (double)this.bounds.x() + (double)this.diameter() / 2.0;
    }

    private double right() {
        return (double)this.bounds.right() - (double)this.diameter() / 2.0;
    }

    public Rect handleBounds() {
        int d = this.diameter();
        double p = (this.value() - this.min) / (this.max - this.min);
        return new Rect((int)Math.round(this.left() + (this.right() - this.left()) * p - (double)d / 2.0), this.bounds.centerY() - d / 2, d, d);
    }

    public Rect trackBounds() {
        int height = Math.max(0, Math.min(4, this.bounds.h()));
        return new Rect((int)Math.ceil(this.left()), this.bounds.centerY() - height / 2, Math.max(0, (int)Math.floor(this.right() - this.left())), height);
    }

    private boolean visibleContains(double mx, double my) {
        return this.handleBounds().contains(mx, my) || this.trackBounds().contains(mx, my);
    }

    public void draw(class_332 c, Rect bounds, GuiFrame frame) {
        this.setBounds(bounds);
        if (!this.visible()) {
            return;
        }
        GuiTheme theme = GuiTheme.current();
        Rect handle = this.handleBounds();
        Rect track = this.trackBounds();
        float h = this.hover.to(this.dragging || this.visibleContains(frame.mouseX(), frame.mouseY()) ? 1.0f : 0.0f, frame.dt(), frame.reduced());
        GuiPrimitives.panel(c, track, GuiTheme.opacity(theme.border(), frame.alpha()), 2);
        int progress = Math.max(0, Math.min(track.w(), handle.centerX() - track.x()));
        GuiPrimitives.rounded(c, track.x(), track.y(), progress, track.h(), 2, GuiTheme.opacity(theme.secondaryAccent(), frame.alpha()));
        GuiPrimitives.panel(c, handle, GuiTheme.opacity(GuiTheme.mix(theme.text(), theme.secondaryAccent(), h * 0.18f), frame.alpha()), handle.h() / 2);
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || !this.visible() || !this.visibleContains(mx, my)) {
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

    public boolean mouseDragged(double mx, double my, int button) {
        return button == 0 && this.mouseDragged(mx, my);
    }

    public boolean mouseReleased() {
        boolean was = this.dragging;
        this.dragging = false;
        return was;
    }

    public boolean mouseReleased(double mx, double my, int button) {
        return button == 0 && this.mouseReleased();
    }

    private void update(double mx) {
        if (!Double.isFinite(mx)) {
            return;
        }
        double fraction = Math.clamp((mx - this.left()) / Math.max(1.0, this.right() - this.left()), 0.0, 1.0);
        double next = this.min + (this.max - this.min) * fraction;
        if (Double.compare(next, this.value()) != 0) {
            this.setter.accept(next);
        }
    }
}

