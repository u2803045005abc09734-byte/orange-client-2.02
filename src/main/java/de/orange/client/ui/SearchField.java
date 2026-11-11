/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_342
 */
package de.orange.client.ui;

import de.orange.client.mixin.TextFieldSelectionAccessor;
import de.orange.client.ui.Glass;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;

public final class SearchField
extends class_342 {
    private String placeholder = "Suchen \u2026";
    private int start;

    public SearchField(class_327 renderer, int x, int y, int w, int h, class_2561 label) {
        super(renderer, x, y, w, h, label);
        this.method_1858(false);
        this.method_71503(false);
        this.method_1868(-986895);
        this.method_1880(160);
    }

    public void method_47404(class_2561 text) {
        super.method_47404(text);
        this.placeholder = text.getString();
    }

    public void method_48579(class_332 c, int mx, int my, float tickDelta) {
        int next;
        Glass.panel(c, this.method_46426(), this.method_46427(), this.method_25368(), this.method_25364(), this.method_25370() ? 0.8f : (this.method_49606() ? 0.35f : 0.0f), 0.0f, 1.0f);
        int textX = this.method_46426() + 11;
        int textY = this.method_46427() + (this.method_25364() - 10) / 2;
        String text = this.method_1882();
        int cursor = Math.clamp((long)this.method_1881(), 0, text.length());
        int available = Math.max(0, this.method_25368() - 22);
        this.start = Math.clamp((long)this.start, 0, cursor);
        while (this.start < cursor && Glass.textWidth(text.substring(this.start, cursor)) > available - 3) {
            this.start = text.offsetByCodePoints(this.start, 1);
        }
        while (this.start > 0 && Glass.textWidth(text.substring(text.offsetByCodePoints(this.start, -1), cursor)) <= available - 3) {
            this.start = text.offsetByCodePoints(this.start, -1);
        }
        int end = this.start;
        while (end < text.length() && Glass.textWidth(text.substring(this.start, next = text.offsetByCodePoints(end, 1))) <= available) {
            end = next;
        }
        c.method_44379(textX, this.method_46427() + 1, this.method_55442() - 9, this.method_55443() - 1);
        if (text.isEmpty() && !this.method_25370()) {
            Glass.text(c, Glass.trim(this.placeholder, available), textX, textY, -9868426);
        } else {
            String selection = this.method_1866();
            if (!selection.isEmpty()) {
                int anchor = ((TextFieldSelectionAccessor)((Object)this)).orange$selectionEnd();
                int a = Math.clamp((long)Math.min(cursor, anchor), this.start, end);
                int b = Math.clamp((long)Math.max(cursor, anchor), this.start, end);
                int x1 = textX + Glass.textWidth(text.substring(this.start, a));
                int x2 = textX + Glass.textWidth(text.substring(this.start, b));
                c.method_25294(x1, textY - 1, x2, textY + 11, Glass.alpha(Glass.ACCENT, 0.28f));
            }
            Glass.text(c, text.substring(this.start, end), textX, textY, -986895);
            if (this.method_25370() && (System.nanoTime() / 500000000L & 1L) == 0L) {
                int cx = textX + Glass.textWidth(text.substring(this.start, cursor));
                c.method_25294(cx, textY - 1, cx + 1, textY + 11, Glass.ACCENT);
            }
        }
        c.method_44380();
    }

    public void method_25348(class_11909 click, boolean doubled) {
        this.method_25365(true);
        String value = this.method_1882();
        int closest = this.cursorAt(click.comp_4798());
        if (doubled) {
            int a = closest;
            int b = closest;
            while (a > 0 && !Character.isWhitespace(value.codePointBefore(a))) {
                a = value.offsetByCodePoints(a, -1);
            }
            while (b < value.length() && !Character.isWhitespace(value.codePointAt(b))) {
                b = value.offsetByCodePoints(b, 1);
            }
            this.method_1883(a, false);
            this.method_1883(b, true);
        } else {
            this.method_1883(closest, false);
        }
    }

    protected void method_25349(class_11909 click, double dx, double dy) {
        this.method_1883(this.cursorAt(click.comp_4798()), true);
    }

    private int cursorAt(double mouseX) {
        String value = this.method_1882();
        int closest = Math.min(this.start, value.length());
        double target = mouseX - (double)this.method_46426() - 11.0;
        while (closest < value.length()) {
            double nextWidth;
            int next = value.offsetByCodePoints(closest, 1);
            double previousWidth = Glass.textWidth(value.substring(this.start, closest));
            if (target < (previousWidth + (nextWidth = (double)Glass.textWidth(value.substring(this.start, next)))) * 0.5) break;
            closest = next;
        }
        return closest;
    }
}

