/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package de.orange.client.ui;

import de.orange.client.ui.Glass;
import de.orange.client.ui.GlassCard;
import de.orange.client.ui.Rect;
import net.minecraft.class_332;

public final class CategoryCard
extends GlassCard {
    private final String icon;
    private final String label;

    public CategoryCard(String icon, String label) {
        super(new Rect(0, 0, 1, 1));
        this.icon = icon;
        this.label = label;
    }

    public void draw(class_332 c, Rect layout, double mx, double my, float dt, boolean selected, boolean reduced, float alpha) {
        this.setBounds(layout);
        super.draw(c, mx, my, dt, selected, reduced, alpha);
        Rect r = this.bounds();
        boolean compact = r.h() < 52;
        int iconSize = compact ? Math.clamp((long)(r.h() / 3), 12, 16) : Math.clamp((long)(r.h() / 3), 16, 34);
        int iconY = compact ? r.y() + Math.max(10, r.h() / 3 - 1) : r.y() + r.h() * 2 / 5;
        int labelY = compact ? r.bottom() - 12 : r.bottom() - Math.max(19, r.h() / 5);
        int color = Glass.opacity(Glass.mix(-2960422, -986895, this.hover.value()), alpha);
        Glass.icon(c, this.icon, r.centerX(), iconY, iconSize, color);
        Glass.centered(c, Glass.trim(this.label, Math.max(1, r.w() - 10)), r.centerX(), labelY, color);
        if (this.selection.value() > 0.01f) {
            int indicatorColor = Glass.alpha(Glass.ACCENT, alpha * this.selection.value() * 0.8f);
            if (compact) {
                Glass.rounded(c, r.x() + 4, r.y() + 6, 1, r.h() - 12, 0, indicatorColor);
            } else {
                Glass.rounded(c, r.centerX() - 7, r.bottom() - 8, 14, 2, 1, indicatorColor);
            }
        }
    }
}

