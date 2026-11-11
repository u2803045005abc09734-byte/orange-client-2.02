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

public final class ProfileCard
extends GlassPanel {
    public ProfileCard() {
        super(new Rect(0, 0, 1, 1));
    }

    public void draw(class_332 context, Rect layout, String name, boolean active, boolean selected, double mouseX, double mouseY, float deltaSeconds, boolean reducedMotion, int accent) {
        this.setBounds(layout);
        super.draw(context, mouseX, mouseY, deltaSeconds, selected, reducedMotion, 1.0f);
        Glass.text(context, Glass.trim(name, layout.w() - 18), layout.x() + 8, layout.y() + 8, selected ? accent : -986895);
        Glass.text(context, active ? "Aktiv" : "Gespeichert", layout.x() + 8, layout.y() + 23, -6184277);
    }
}

