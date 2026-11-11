/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package de.orange.client.ui;

import de.orange.client.module.ClientModule;
import de.orange.client.ui.Anim;
import de.orange.client.ui.AnimatedToggle;
import de.orange.client.ui.Glass;
import de.orange.client.ui.Rect;
import net.minecraft.class_332;

public final class ModuleEntry {
    private final Anim hover = new Anim(0.0f);
    private final AnimatedToggle toggle = new AnimatedToggle();

    public void draw(class_332 c, Rect bounds, ClientModule module, boolean focused, int mouseX, int mouseY, float deltaSeconds, boolean reducedMotion, float alpha, int accent) {
        float amount = this.hover.to(bounds.contains(mouseX, mouseY) ? 1.0f : 0.0f, deltaSeconds, reducedMotion);
        Glass.panel(c, bounds, amount, focused ? 0.22f : 0.0f, 0.72f * alpha);
        Glass.icon(c, module.icon(), bounds.x() + 23, bounds.centerY(), 19, Glass.opacity(module.enabled() ? accent : -6184277, alpha));
        int textX = bounds.x() + 44;
        int textWidth = Math.max(1, bounds.w() - 142);
        Glass.text(c, Glass.trim(module.name(), textWidth), textX, bounds.y() + 12, Glass.opacity(-986895, alpha));
        Glass.text(c, Glass.trim(module.description(), textWidth), textX, bounds.y() + 30, Glass.opacity(-6184277, alpha));
        Rect settings = ModuleEntry.settingsBounds(bounds);
        Glass.panel(c, settings, settings.contains(mouseX, mouseY) ? 0.9f : 0.0f, 0.0f, 0.48f * alpha);
        Glass.icon(c, "settings", settings.centerX(), settings.centerY(), 12, Glass.opacity(-6184277, alpha));
        Rect switchBounds = ModuleEntry.toggleBounds(bounds);
        this.toggle.draw(c, switchBounds.x(), switchBounds.y(), module.enabled(), deltaSeconds, reducedMotion);
    }

    public static Rect settingsBounds(Rect bounds) {
        return new Rect(bounds.right() - 84, bounds.centerY() - 12, 24, 24);
    }

    public static Rect toggleBounds(Rect bounds) {
        return new Rect(bounds.right() - 48, bounds.centerY() - 8, 31, 17);
    }
}

