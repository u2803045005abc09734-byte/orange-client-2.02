/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11719
 *  net.minecraft.class_11719$class_11721
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_5250
 *  net.minecraft.class_5348
 *  org.joml.Matrix3x2fStack
 */
package de.orange.client.ui;

import de.orange.client.ui.Rect;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.class_11719;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_5250;
import net.minecraft.class_5348;
import org.joml.Matrix3x2fStack;

public final class Glass {
    public static final int TEXT = -986895;
    public static final int MUTED = -6184277;
    public static int ACCENT = -26020;
    private static float surfaceOpacity = 0.65f;
    public static final int FAINT = -9868426;
    public static final int DANGER = -25712;
    public static final class_2583 FONT_STYLE = class_2583.field_24360.method_27704((class_11719)new class_11719.class_11721(class_2960.method_60655((String)"orange", (String)"ui")));
    private static final int[][] RADII = new int[33][];
    private static final Map<String, class_5250> TEXTS = new LinkedHashMap<String, class_5250>(128, 0.75f, true){

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, class_5250> entry) {
            return this.size() > 512;
        }
    };

    private Glass() {
    }

    public static void theme(int accent, float opacity) {
        ACCENT = accent | 0xFF000000;
        surfaceOpacity = Math.clamp(opacity, 0.25f, 0.95f);
    }

    public static class_5250 styled(String text) {
        return TEXTS.computeIfAbsent(text == null ? "" : text, v -> class_2561.method_43470((String)v).method_10862(FONT_STYLE));
    }

    public static void text(class_332 c, String value, int x, int y, int color) {
        c.method_51439(class_310.method_1551().field_1772, (class_2561)Glass.styled(value), x, y, color, false);
    }

    public static int textWidth(String value) {
        return class_310.method_1551().field_1772.method_27525((class_5348)Glass.styled(value));
    }

    public static void centered(class_332 c, String value, int centerX, int y, int color) {
        Glass.text(c, value, centerX - Glass.textWidth(value) / 2, y, color);
    }

    public static void title(class_332 c, String value, int x, int y, float scale, int color) {
        Matrix3x2fStack m = c.method_51448();
        m.pushMatrix();
        m.translate((float)x, (float)y);
        m.scale(scale, scale);
        Glass.text(c, value, 0, 0, color);
        m.popMatrix();
    }

    public static String trim(String value, int width) {
        if (value == null || width <= 0) {
            return "";
        }
        if (Glass.textWidth(value) <= width) {
            return value;
        }
        if (Glass.textWidth("\u2026") > width) {
            return "";
        }
        int low = 0;
        int high = value.codePointCount(0, value.length());
        while (low < high) {
            int middle = low + high + 1 >>> 1;
            int end = value.offsetByCodePoints(0, middle);
            if (Glass.textWidth(value.substring(0, end) + "\u2026") <= width) {
                low = middle;
                continue;
            }
            high = middle - 1;
        }
        return value.substring(0, value.offsetByCodePoints(0, low)) + "\u2026";
    }

    public static void begin(class_332 c, int width, int height, float fade) {
        float a = Math.clamp(fade, 0.0f, 1.0f);
        c.method_25294(0, 0, width, height, Glass.alpha(-16382198, 0.53f * a));
        c.method_25296(0, 0, width, Math.max(1, height / 2), Glass.alpha(-15263715, 0.27f * a), 1513501);
        c.method_25296(0, height / 2, width, height, 131847, Glass.alpha(-16645369, 0.45f * a));
        int glowW = Math.min(320, width / 2);
        for (int i = 0; i < 5; ++i) {
            int inset = i * glowW / 12;
            Glass.rounded(c, width / 2 - glowW / 2 + inset, 0, glowW - inset * 2, Math.max(1, height / 5 - i * 8), 30, Glass.alpha(-15197, 0.006f * a));
        }
    }

    public static int alpha(int rgb, float opacity) {
        return rgb & 0xFFFFFF | Math.clamp((long)Math.round(255.0f * opacity), 0, 255) << 24;
    }

    public static int opacity(int argb, float factor) {
        return argb & 0xFFFFFF | Math.clamp((long)Math.round((float)(argb >>> 24) * factor), 0, 255) << 24;
    }

    public static int mix(int first, int second, float progress) {
        float p = Math.clamp(progress, 0.0f, 1.0f);
        int a = Math.round((float)(first >>> 24) + (float)((second >>> 24) - (first >>> 24)) * p);
        int r = Math.round((float)(first >>> 16 & 0xFF) + (float)((second >>> 16 & 0xFF) - (first >>> 16 & 0xFF)) * p);
        int g = Math.round((float)(first >>> 8 & 0xFF) + (float)((second >>> 8 & 0xFF) - (first >>> 8 & 0xFF)) * p);
        int b = Math.round((float)(first & 0xFF) + (float)((second & 0xFF) - (first & 0xFF)) * p);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static void panel(class_332 c, int x, int y, int w, int h, float hover, float selected, float alpha) {
        if (w < 2 || h < 2 || alpha <= 0.0f) {
            return;
        }
        float hv = Math.clamp(hover, 0.0f, 1.0f);
        float sel = Math.clamp(selected, 0.0f, 1.0f);
        int radius = Math.min(13, Math.min(w / 3, h / 3));
        Glass.rounded(c, x - 3, y + 4, w + 6, h + 3, radius + 2, Glass.alpha(-16777216, (0.08f + 0.025f * hv) * alpha));
        Glass.rounded(c, x - 1, y + 2, w + 2, h + 2, radius + 1, Glass.alpha(-16777216, 0.12f * alpha));
        Glass.rounded(c, x, y, w, h, radius, Glass.alpha(-2104854, (0.115f + 0.11f * hv + 0.1f * sel) * alpha));
        Glass.rounded(c, x + 1, y + 1, w - 2, h - 2, Math.max(1, radius - 1), Glass.alpha(-15658473, (surfaceOpacity + 0.07f - 0.2f * hv - 0.08f * sel) * alpha));
        c.method_25294(x + radius, y + 1, x + w - radius, y + 2, Glass.alpha(-1, (0.12f + hv * 0.09f) * alpha));
        for (int row = 0; row < Math.min(12, h / 3); ++row) {
            int inset = radius / 2 + Math.max(0, radius / 2 - row);
            float light = 0.026f * (1.0f - (float)row / 12.0f) + 0.017f * sel;
            c.method_25294(x + inset + 1, y + 2 + row, x + w - inset - 1, y + 3 + row, Glass.alpha(-1512193, light * alpha));
        }
        if (sel > 0.01f) {
            Glass.rounded(c, x + radius, y + h - 2, Math.max(1, w - radius * 2), 1, 0, Glass.alpha(ACCENT, 0.28f * sel * alpha));
        }
        if (hv > 0.01f && w > 35 && h > 25) {
            int sweep = x + radius + Math.round((float)(w - radius * 2) * hv);
            for (int i = -4; i <= 4; ++i) {
                int sx = Math.clamp((long)(sweep + i), x + radius, x + w - radius - 1);
                c.method_25294(sx, y + 4, sx + 1, y + h - 5, Glass.alpha(-788993, 0.009f * hv * (1.0f - (float)Math.abs(i) / 5.0f) * alpha));
            }
        }
    }

    public static void panel(class_332 c, Rect r, float hover, float selected, float alpha) {
        Glass.panel(c, r.x(), r.y(), r.w(), r.h(), hover, selected, alpha);
    }

    public static void button(class_332 c, Rect r, String label, boolean hovered) {
        Glass.panel(c, r, hovered ? 1.0f : 0.0f, 0.0f, 1.0f);
        Glass.centered(c, Glass.trim(label, Math.max(0, r.w() - 14)), r.centerX(), r.y() + (r.h() - 10) / 2, hovered ? -986895 : -6184277);
    }

    public static void rounded(class_332 c, int x, int y, int w, int h, int radius, int color) {
        int row;
        if (w <= 0 || h <= 0 || color >>> 24 == 0) {
            return;
        }
        int r = Math.clamp((long)radius, 0, Math.min(32, Math.min(w, h) / 2));
        if (r == 0) {
            c.method_25294(x, y, x + w, y + h, color);
            return;
        }
        int[] offsets = RADII[r];
        if (offsets == null) {
            offsets = new int[r];
            for (row = 0; row < r; ++row) {
                double dy = (double)(r - row) - 0.5;
                offsets[row] = (int)Math.ceil((double)r - Math.sqrt((double)(r * r) - dy * dy));
            }
            Glass.RADII[r] = offsets;
        }
        c.method_25294(x, y + r, x + w, y + h - r, color);
        for (row = 0; row < r; ++row) {
            int inset = offsets[row];
            c.method_25294(x + inset, y + row, x + w - inset, y + row + 1, color);
            c.method_25294(x + inset, y + h - row - 1, x + w - inset, y + h - row, color);
        }
    }

    public static void solid(class_332 c, int x, int y, int w, int h, int radius, int color) {
        Glass.rounded(c, x, y, w, h, radius, color);
    }

    public static void line(class_332 c, int x1, int y1, int x2, int y2, int thickness, int color) {
        if (x1 == x2) {
            c.method_25294(x1 - thickness / 2, Math.min(y1, y2), x1 + (thickness + 1) / 2, Math.max(y1, y2) + 1, color);
            return;
        }
        if (y1 == y2) {
            c.method_25294(Math.min(x1, x2), y1 - thickness / 2, Math.max(x1, x2) + 1, y1 + (thickness + 1) / 2, color);
            return;
        }
        int dx = Math.abs(x2 - x1);
        int sx = x1 < x2 ? 1 : -1;
        int dy = -Math.abs(y2 - y1);
        int sy = y1 < y2 ? 1 : -1;
        int err = dx + dy;
        while (true) {
            c.method_25294(x1 - thickness / 2, y1 - thickness / 2, x1 + (thickness + 1) / 2, y1 + (thickness + 1) / 2, color);
            if (x1 == x2 && y1 == y2) break;
            int e2 = err * 2;
            if (e2 >= dy) {
                err += dy;
                x1 += sx;
            }
            if (e2 > dx) continue;
            err += dx;
            y1 += sy;
        }
    }

    private static void ring(class_332 c, int cx, int cy, int r, int color) {
        int lastX = cx + r;
        int lastY = cy;
        for (int i = 1; i <= 24; ++i) {
            double a = Math.PI * 2 * (double)i / 24.0;
            int nx = cx + (int)Math.round(Math.cos(a) * (double)r);
            int ny = cy + (int)Math.round(Math.sin(a) * (double)r);
            Glass.line(c, lastX, lastY, nx, ny, 1, color);
            lastX = nx;
            lastY = ny;
        }
    }

    public static void icon(class_332 c, String id, int cx, int cy, int size, int color) {
        int s = Math.max(4, size / 2);
        int t = size >= 26 ? 2 : 1;
        int x = cx - s;
        int y = cy - s;
        switch (id) {
            case "pvp": {
                Glass.line(c, cx - s + 2, cy + s - 2, cx + s - 2, cy - s + 2, t + 1, color);
                Glass.line(c, cx - s + 3, cy + s - 7, cx - s + 7, cy + s - 3, t, color);
                Glass.line(c, cx + s - 3, cy + s - 2, cx - s + 2, cy - s + 3, t, color);
                Glass.line(c, cx + s - 7, cy + s - 3, cx + s - 3, cy + s - 7, t, color);
                break;
            }
            case "armor": {
                Glass.line(c, cx - s + 2, y + 2, cx + s - 2, y + 2, t, color);
                Glass.line(c, cx - s + 2, y + 2, cx - s + 3, cy + 3, t, color);
                Glass.line(c, cx + s - 2, y + 2, cx + s - 3, cy + 3, t, color);
                Glass.line(c, cx - s + 3, cy + 3, cx, cy + s, t, color);
                Glass.line(c, cx + s - 3, cy + 3, cx, cy + s, t, color);
                Glass.line(c, cx, cy - 3, cx, cy + s - 4, t, color);
                break;
            }
            case "keys": {
                Glass.line(c, x, y + 3, cx + s, y + 3, t, color);
                Glass.line(c, x, y + 3, x, cy + s - 2, t, color);
                Glass.line(c, cx + s, y + 3, cx + s, cy + s - 2, t, color);
                Glass.line(c, x, cy + s - 2, cx + s, cy + s - 2, t, color);
                int key = Math.max(1, size / 7);
                for (int row = 0; row < 2; ++row) {
                    for (int col = 0; col < 3; ++col) {
                        c.method_25294(cx - s + 3 + col * (size - 5) / 3, cy - s + 6 + row * (key + 2), cx - s + 3 + col * (size - 5) / 3 + key, cy - s + 6 + row * (key + 2) + key, color);
                    }
                }
                break;
            }
            case "sprint": {
                Glass.ring(c, cx + 2, cy - s + 2, Math.max(1, s / 4), color);
                Glass.line(c, cx, cy - 2, cx - 3, cy + 3, t, color);
                Glass.line(c, cx, cy - 2, cx + s - 1, cy, t, color);
                Glass.line(c, cx, cy - 2, cx - s + 1, cy, t, color);
                Glass.line(c, cx - 3, cy + 3, cx - s + 1, cy + s, t, color);
                Glass.line(c, cx - 3, cy + 3, cx + 3, cy + 4, t, color);
                Glass.line(c, cx + 3, cy + 4, cx + 2, cy + s, t, color);
                break;
            }
            case "hud": 
            case "fps": 
            case "coordinates": {
                int q = Math.max(3, s - 2);
                int gap = 3;
                Glass.rounded(c, x, y, q, q, 2, color);
                Glass.rounded(c, cx + gap, y, q, q, 2, color);
                Glass.rounded(c, x, cy + gap, q, q, 2, color);
                Glass.rounded(c, cx + gap, cy + gap, q, q, 2, color);
                break;
            }
            case "visuals": 
            case "freelook": {
                Glass.line(c, x, cy, cx, y + 3, t, color);
                Glass.line(c, cx, y + 3, cx + s, cy, t, color);
                Glass.line(c, x, cy, cx, cy + s - 3, t, color);
                Glass.line(c, cx, cy + s - 3, cx + s, cy, t, color);
                Glass.ring(c, cx, cy, Math.max(2, s / 3), color);
                break;
            }
            case "player": 
            case "social": {
                Glass.ring(c, cx, cy - s / 2, Math.max(2, s / 3), color);
                Glass.line(c, cx - s + 2, cy + s - 1, cx - s + 3, cy + 2, t, color);
                Glass.line(c, cx - s + 3, cy + 2, cx + s - 3, cy + 2, t, color);
                Glass.line(c, cx + s - 3, cy + 2, cx + s - 2, cy + s - 1, t, color);
                Glass.line(c, cx - s + 2, cy + s - 1, cx + s - 2, cy + s - 1, t, color);
                break;
            }
            case "misc": 
            case "settings": {
                Glass.ring(c, cx, cy, Math.max(3, s - 3), color);
                Glass.ring(c, cx, cy, Math.max(1, s / 4), color);
                for (int i = 0; i < 8; ++i) {
                    double a = Math.PI * (double)i / 4.0;
                    Glass.line(c, cx + (int)(Math.cos(a) * (double)(s - 2)), cy + (int)(Math.sin(a) * (double)(s - 2)), cx + (int)(Math.cos(a) * (double)s), cy + (int)(Math.sin(a) * (double)s), t + 1, color);
                }
                break;
            }
            case "optimizer": {
                Glass.line(c, cx + 2, y, x + 2, cy + 2, t, color);
                Glass.line(c, x + 2, cy + 2, cx + 2, cy + 2, t, color);
                Glass.line(c, cx + 2, cy + 2, cx - 2, cy + s, t, color);
                Glass.line(c, cx - 2, cy + s, cx + s - 1, cy - 2, t, color);
                Glass.line(c, cx + s - 1, cy - 2, cx + 1, cy - 2, t, color);
                break;
            }
            case "wardrobe": {
                Glass.line(c, x, y + 4, cx - s / 3, y, t, color);
                Glass.line(c, cx - s / 3, y, cx, y + 3, t, color);
                Glass.line(c, cx, y + 3, cx + s / 3, y, t, color);
                Glass.line(c, cx + s / 3, y, cx + s, y + 4, t, color);
                Glass.line(c, x, y + 4, x + 2, cy, t, color);
                Glass.line(c, x + 2, cy, cx - s / 2, cy - 1, t, color);
                Glass.line(c, cx - s / 2, cy - 1, cx - s / 2, cy + s, t, color);
                Glass.line(c, cx - s / 2, cy + s, cx + s / 2, cy + s, t, color);
                Glass.line(c, cx + s / 2, cy + s, cx + s / 2, cy - 1, t, color);
                Glass.line(c, cx + s / 2, cy - 1, cx + s - 2, cy, t, color);
                Glass.line(c, cx + s - 2, cy, cx + s, y + 4, t, color);
                break;
            }
            case "search": {
                Glass.ring(c, cx - 2, cy - 2, Math.max(3, s - 4), color);
                Glass.line(c, cx + s / 3, cy + s / 3, cx + s, cy + s, t + 1, color);
                break;
            }
            case "profiles": {
                Glass.rounded(c, x + 3, y, size - 5, size - 3, 3, Glass.alpha(color, 0.22f));
                Glass.line(c, x, y + 4, x, cy + s, t, color);
                Glass.line(c, x, cy + s, cx + s - 4, cy + s, t, color);
                Glass.line(c, cx + s - 4, cy + s, cx + s - 4, y + 4, t, color);
                Glass.line(c, cx + s - 4, y + 4, x, y + 4, t, color);
                break;
            }
            case "pack": 
            case "resourcepack": {
                Glass.line(c, x, y + 4, cx, y, t, color);
                Glass.line(c, cx, y, cx + s, y + 4, t, color);
                Glass.line(c, x, y + 4, cx, cy, t, color);
                Glass.line(c, cx, cy, cx + s, y + 4, t, color);
                Glass.line(c, x, y + 4, x, cy + s - 3, t, color);
                Glass.line(c, x, cy + s - 3, cx, cy + s, t, color);
                Glass.line(c, cx, cy + s, cx + s, cy + s - 3, t, color);
                Glass.line(c, cx + s, cy + s - 3, cx + s, y + 4, t, color);
                Glass.line(c, cx, cy, cx, cy + s, t, color);
                break;
            }
            case "back": {
                Glass.line(c, cx + s / 2, y + 2, cx - s / 2, cy, t, color);
                Glass.line(c, cx - s / 2, cy, cx + s / 2, cy + s - 2, t, color);
                break;
            }
            case "close": {
                Glass.line(c, x + 3, y + 3, cx + s - 3, cy + s - 3, t, color);
                Glass.line(c, x + 3, cy + s - 3, cx + s - 3, y + 3, t, color);
                break;
            }
            case "clock": {
                Glass.ring(c, cx, cy, Math.max(3, s - 1), color);
                Glass.line(c, cx, cy, cx, y + 4, t, color);
                Glass.line(c, cx, cy, cx + s / 2, cy + 2, t, color);
                break;
            }
            case "crosshair": {
                Glass.line(c, x, cy, cx - 3, cy, t, color);
                Glass.line(c, cx + 3, cy, cx + s, cy, t, color);
                Glass.line(c, cx, y, cx, cy - 3, t, color);
                Glass.line(c, cx, cy + 3, cx, cy + s, t, color);
                break;
            }
            default: {
                Glass.ring(c, cx, cy, Math.max(3, s - 2), color);
                c.method_25294(cx - 1, cy - 1, cx + 2, cy + 2, color);
            }
        }
    }
}

