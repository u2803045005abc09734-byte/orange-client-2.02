/*
 * Decompiled with CFR.
 */
package de.orange.client.ui.premium.components;

import de.orange.client.ui.Anim;
import de.orange.client.ui.Rect;
import de.orange.client.ui.premium.GuiFrame;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ExpandableSection {
    private final String id;
    private final Supplier<Boolean> getter;
    private final Consumer<Boolean> setter;
    private final Anim height = new Anim();
    private boolean initialized;
    private Rect visible = new Rect(0, 0, 0, 0);

    public ExpandableSection(String id, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        this.id = Objects.requireNonNull(id);
        this.getter = Objects.requireNonNull(getter);
        this.setter = Objects.requireNonNull(setter);
    }

    public String id() {
        return this.id;
    }

    public boolean expanded() {
        return Boolean.TRUE.equals(this.getter.get());
    }

    public void toggle() {
        this.setter.accept(!this.expanded());
    }

    public float height(float targetFull, GuiFrame frame) {
        float target;
        float f = target = this.expanded() && Float.isFinite(targetFull) ? Math.max(0.0f, targetFull) : 0.0f;
        if (!this.initialized) {
            this.height.snap(target);
            this.initialized = true;
        }
        return this.height.to(target, frame.dt(), frame.reduced());
    }

    public float currentHeight() {
        return this.height.value();
    }

    public Rect visibleBounds(Rect fullBounds) {
        this.visible = new Rect(fullBounds.x(), fullBounds.y(), fullBounds.w(), Math.min(fullBounds.h(), Math.max(0, Math.round(this.height.value()))));
        return this.visible;
    }

    public boolean contains(double mx, double my) {
        return this.expanded() && this.visible.contains(mx, my);
    }
}

