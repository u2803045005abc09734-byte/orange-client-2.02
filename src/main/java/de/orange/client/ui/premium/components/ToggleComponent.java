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
import de.orange.client.ui.premium.GuiTheme;
import de.orange.client.ui.premium.components.ToggleArt;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.class_332;

public final class ToggleComponent {
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;
    private final Anim motion = new Anim();
    private final Anim hover = new Anim();
    private Rect bounds = new Rect(0, 0, 0, 0);
    private boolean initialized;

    public ToggleComponent(BooleanSupplier booleanSupplier, Consumer<Boolean> consumer) {
        this.getter = Objects.requireNonNull(booleanSupplier);
        this.setter = Objects.requireNonNull(consumer);
    }

    public boolean value() {
        return this.getter.getAsBoolean();
    }

    public void setBounds(Rect rect) {
        this.bounds = Objects.requireNonNull(rect);
    }

    public Rect bounds() {
        return this.bounds;
    }

    public void draw(class_332 class_3322, Rect rect, GuiFrame guiFrame) {
        this.setBounds(rect);
        if (rect.w() < 8 || rect.h() < 8) {
            return;
        }
        boolean bl = this.value();
        if (!this.initialized) {
            this.motion.snap(bl ? 1.0f : 0.0f);
            this.initialized = true;
        }
        float f = this.motion.to(bl ? 1.0f : 0.0f, guiFrame.dt(), guiFrame.reduced());
        float f2 = this.hover.to(rect.contains(guiFrame.mouseX(), guiFrame.mouseY()) ? 1.0f : 0.0f, guiFrame.dt(), guiFrame.reduced());
        GuiTheme guiTheme = GuiTheme.current();
        int n = GuiTheme.mix(guiTheme.border(), guiTheme.accent(), f);
        n = GuiTheme.mix(n, guiTheme.text(), f2 * 0.055f);
        int n2 = GuiTheme.mix(-723720, guiTheme.text(), 0.035f);
        ToggleArt.draw(class_3322, rect, n, n2, f, guiFrame.alpha());
    }

    public boolean mouseClicked(double d, double d2, int n) {
        if (n != 0 || this.bounds.w() < 8 || this.bounds.h() < 8 || !this.bounds.contains(d, d2)) {
            return false;
        }
        this.setter.accept(!this.value());
        return true;
    }
}

