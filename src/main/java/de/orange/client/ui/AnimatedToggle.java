/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package de.orange.client.ui;

import de.orange.client.ui.Anim;
import de.orange.client.ui.Rect;
import de.orange.client.ui.premium.GuiTheme;
import de.orange.client.ui.premium.components.ToggleArt;
import net.minecraft.class_332;

public final class AnimatedToggle {
    private final Anim motion = new Anim();
    private boolean initialized;

    public void draw(class_332 class_3322, int n, int n2, boolean bl, float f, boolean bl2) {
        if (!this.initialized) {
            this.motion.snap(bl ? 1.0f : 0.0f);
            this.initialized = true;
        }
        float f2 = this.motion.to(bl ? 1.0f : 0.0f, f, bl2);
        GuiTheme guiTheme = GuiTheme.current();
        int n3 = GuiTheme.mix(guiTheme.border(), guiTheme.accent(), f2);
        int n4 = GuiTheme.mix(-723720, guiTheme.text(), 0.035f);
        ToggleArt.draw(class_3322, new Rect(n, n2, 34, 19), n3, n4, f2, 1.0f);
    }

    public float progress() {
        return this.motion.value();
    }
}

