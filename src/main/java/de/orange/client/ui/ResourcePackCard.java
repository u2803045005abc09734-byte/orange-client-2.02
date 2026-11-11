/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package de.orange.client.ui;

import de.orange.client.modrinth.ModrinthService;
import de.orange.client.ui.Anim;
import de.orange.client.ui.Glass;
import de.orange.client.ui.PackImageCache;
import de.orange.client.ui.Rect;
import net.minecraft.class_332;

public final class ResourcePackCard {
    private final Anim hover = new Anim(0.0f);

    public void draw(class_332 c, Rect box, ModrinthService.ProjectHit project, String action, boolean installed, boolean busy, int mx, int my, float dt, boolean reduced) {
        float h = this.hover.to(box.contains(mx, my) ? 1.0f : 0.0f, dt, reduced);
        Glass.panel(c, box.x(), box.y(), box.w(), box.h(), h, installed ? 0.12f : 0.0f, 0.8f);
        if (ResourcePackCard.compact(box)) {
            this.drawCompact(c, box, project, action, installed, busy, mx, my);
            return;
        }
        int imageHeight = Math.max(38, Math.min(90, box.h() - 103));
        Rect image = new Rect(box.x() + 8, box.y() + 8, box.w() - 16, imageHeight);
        PackImageCache.get().draw(c, project.iconUrl(), image);
        if (installed) {
            Glass.panel(c, box.x() + 12, box.y() + 12, 66, 17, 0.0f, 0.25f, 0.85f);
            Glass.text(c, "INSTALLIERT", box.x() + 18, box.y() + 16, Glass.ACCENT);
        }
        int textY = image.y() + image.h() + 9;
        Glass.text(c, Glass.trim(project.title(), box.w() - 20), box.x() + 10, textY, -986895);
        Glass.text(c, Glass.trim(project.author(), box.w() - 20), box.x() + 10, textY + 14, -6184277);
        Glass.text(c, Glass.trim(project.description(), box.w() - 20), box.x() + 10, textY + 28, -5527131);
        String info = project.versions().contains("1.21.11") ? "1.21.11" : "Andere Version";
        Glass.text(c, info, box.x() + 10, box.y() + box.h() - 42, -6184277);
        Rect button = ResourcePackCard.actionBounds(box);
        Glass.button(c, button, busy ? "Wird geladen \u2026" : action, button.contains(mx, my));
    }

    private void drawCompact(class_332 c, Rect box, ModrinthService.ProjectHit project, String action, boolean installed, boolean busy, int mx, int my) {
        int imageSize = Math.min(64, box.h() - 16);
        PackImageCache.get().draw(c, project.iconUrl(), new Rect(box.x() + 8, box.y() + 8, imageSize, imageSize));
        int tx = box.x() + imageSize + 18;
        int fullWidth = Math.max(1, box.right() - tx - 10);
        Rect button = ResourcePackCard.actionBounds(box);
        int detailWidth = Math.max(1, button.x() - tx - 8);
        Glass.text(c, Glass.trim(project.title(), fullWidth), tx, box.y() + 8, -986895);
        Glass.text(c, Glass.trim(project.author(), fullWidth), tx, box.y() + 22, -6184277);
        Glass.text(c, Glass.trim(project.description(), detailWidth), tx, box.y() + 36, -5527131);
        String version = project.versions().contains("1.21.11") ? "1.21.11" : "Andere Version";
        Object info = installed ? "Installiert \u00b7 " + version : version;
        Glass.text(c, Glass.trim((String)info, detailWidth), tx, box.bottom() - 16, installed ? Glass.ACCENT : -6184277);
        Glass.button(c, button, busy ? "L\u00e4dt \u2026" : action, button.contains(mx, my));
    }

    private static boolean compact(Rect box) {
        return box.h() < 150;
    }

    public static Rect actionBounds(Rect box) {
        if (ResourcePackCard.compact(box)) {
            return new Rect(box.right() - 88, box.bottom() - 29, 80, 21);
        }
        return new Rect(box.x() + 8, box.y() + box.h() - 28, box.w() - 16, 21);
    }
}

