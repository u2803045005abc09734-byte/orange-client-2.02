/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11909
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package de.orange.client.gui;

import de.orange.client.gui.OrangeSubScreen;
import de.orange.client.ui.Glass;
import de.orange.client.ui.Rect;
import net.minecraft.class_11909;
import net.minecraft.class_332;
import net.minecraft.class_437;

public final class EmptyScreen
extends OrangeSubScreen {
    private final boolean wardrobe;

    public EmptyScreen(class_437 parent, String kind) {
        super(parent, "wardrobe".equalsIgnoreCase(kind) ? "Wardrobe" : "Social");
        this.wardrobe = "wardrobe".equalsIgnoreCase(kind);
    }

    public void method_25394(class_332 c, int mx, int my, float delta) {
        this.begin(c, mx, my, delta, this.wardrobe ? "wardrobe" : "social", this.wardrobe ? "Wardrobe" : "Social", this.wardrobe ? "Cosmetics & Outfits" : "Freunde & Verbindungen");
        int cx = this.left + this.panelWidth / 2;
        int cy = this.top + this.panelHeight / 2 - 32;
        Glass.panel(c, cx - 34, cy - 42, 68, 68, 0.0f, 0.0f, 0.75f);
        Glass.icon(c, this.wardrobe ? "wardrobe" : "social", cx, cy - 8, 31, -6184277);
        this.centered(c, this.wardrobe ? "Noch keine Cosmetics" : "Hier entsteht Verbindung", cx, cy + 43, -986895);
        String line1 = this.wardrobe ? "Es ist kein Cosmetics-Anbieter verbunden." : "Ein Freunde-Backend ist noch nicht eingerichtet.";
        String line2 = this.wardrobe ? "Orange ver\u00e4ndert deine vorhandenen Capes nicht." : "Es werden keine Online-Freunde simuliert.";
        this.centered(c, Glass.trim(line1, this.panelWidth - 42), cx, cy + 63, -6184277);
        this.centered(c, Glass.trim(line2, this.panelWidth - 42), cx, cy + 79, -6184277);
        Rect back = new Rect(cx - 51, this.top + this.panelHeight - 43, 102, 27);
        Glass.button(c, back, "Zur\u00fcck", back.contains(mx, my));
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        if (this.closeClick(click)) {
            return true;
        }
        if (click.method_74245() == 0 && new Rect(this.left + this.panelWidth / 2 - 51, this.top + this.panelHeight - 43, 102, 27).contains(click.comp_4798(), click.comp_4799())) {
            this.method_25419();
            return true;
        }
        return super.method_25402(click, doubled);
    }
}

