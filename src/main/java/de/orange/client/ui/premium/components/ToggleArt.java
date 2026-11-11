/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 */
package de.orange.client.ui.premium.components;

import de.orange.client.ui.Rect;
import de.orange.client.ui.premium.GuiTheme;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;

public final class ToggleArt {
    private static final int TRACK_TEX_W = 128;
    private static final int TRACK_TEX_H = 64;
    private static final int CIRCLE_TEX = 64;
    private static final class_2960 TRACK = class_2960.method_60655((String)"orange", (String)"textures/gui/toggle_track.png");
    private static final class_2960 THUMB = class_2960.method_60655((String)"orange", (String)"textures/gui/toggle_thumb.png");
    private static final class_2960 SHADOW = class_2960.method_60655((String)"orange", (String)"textures/gui/toggle_thumb_shadow.png");

    private ToggleArt() {
    }

    public static void draw(class_332 class_3322, Rect rect, int n, int n2, float f, float f2) {
        int n3 = rect.w();
        int n4 = rect.h();
        if (n3 < 8 || n4 < 8) {
            return;
        }
        int n6 = Math.min(18, n4);
        int n5 = rect.y() + Math.max(0, (n4 - n6) / 2);
        int n7 = Math.min(n3, Math.max(n6 + 8, n3));
        int n8 = rect.x() + (n3 - n7) / 2;
        class_3322.method_25293(class_10799.field_56883, TRACK, n8, n5, 0.0f, 0.0f, n7, n6, 128, 64, 128, 64, GuiTheme.opacity(n, f2));
        int n9 = Math.min(n6 - 4, 14);
        n9 = Math.max(8, n9);
        int n10 = Math.max(1, (n6 - n9) / 2);
        int n11 = Math.max(0, n7 - n10 * 2 - n9);
        int n12 = n8 + n10 + Math.round((float)n11 * Math.max(0.0f, Math.min(1.0f, f)));
        int n13 = n5 + (n6 - n9) / 2;
        int n14 = n9 + 2;
        int n15 = n12 - 1;
        int n16 = n13;
        class_3322.method_25293(class_10799.field_56883, SHADOW, n15, n16, 0.0f, 0.0f, n14, n14, 64, 64, 64, 64, GuiTheme.opacity(0x70000000, f2));
        class_3322.method_25293(class_10799.field_56883, THUMB, n12, n13, 0.0f, 0.0f, n9, n9, 64, 64, 64, 64, GuiTheme.opacity(n2, f2));
    }
}

