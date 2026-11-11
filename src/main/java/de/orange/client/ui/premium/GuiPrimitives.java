/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package de.orange.client.ui.premium;

import de.orange.client.OrangeClient;
import de.orange.client.ui.Glass;
import de.orange.client.ui.Rect;
import de.orange.client.ui.premium.GuiTheme;
import net.minecraft.class_332;

public final class GuiPrimitives {
    private static final float[][] EDGES = new float[33][];

    private GuiPrimitives() {
    }

    public static void window(class_332 c, Rect r, float alpha) {
        GuiTheme t = GuiTheme.current();
        GuiPrimitives.rounded(c, r.x() - 4, r.y() + 3, r.w() + 8, r.h() + 7, 14, GuiTheme.alpha(-16777216, alpha * 0.1f));
        GuiPrimitives.rounded(c, r.x() - 1, r.y() + 2, r.w() + 2, r.h() + 3, 13, GuiTheme.alpha(-16777216, alpha * 0.22f));
        OrangeClient orange = OrangeClient.instance();
        float opacity = orange == null ? 0.65f : orange.config().uiOpacity;
        GuiPrimitives.rounded(c, r.x(), r.y(), r.w(), r.h(), 12, GuiTheme.alpha(t.background(), alpha * (0.76f + opacity * 0.21f)));
        GuiPrimitives.border(c, r, GuiTheme.opacity(t.border(), alpha * 0.85f), 12);
    }

    public static void panel(class_332 c, Rect r, int color, int radius) {
        GuiPrimitives.rounded(c, r.x(), r.y(), r.w(), r.h(), radius, color);
    }

    public static void borderedPanel(class_332 c, Rect r, int fill, int border, int radius) {
        GuiPrimitives.panel(c, r, fill, radius);
        GuiPrimitives.border(c, r, border, radius);
    }

    public static void border(class_332 c, Rect bounds, int color, int radius) {
        int x = bounds.x();
        int y = bounds.y();
        int w = bounds.w();
        int h = bounds.h();
        if (w < 1 || h < 1 || color >>> 24 == 0) {
            return;
        }
        int r = Math.clamp((long)radius, 0, Math.min(32, Math.min(w, h) / 2));
        if (r < 1) {
            c.method_25294(x, y, x + w, y + 1, color);
            c.method_25294(x, y + h - 1, x + w, y + h, color);
            c.method_25294(x, y + 1, x + 1, y + h - 1, color);
            c.method_25294(x + w - 1, y + 1, x + w, y + h - 1, color);
            return;
        }
        float[] outer = GuiPrimitives.edges(r);
        float[] inner = GuiPrimitives.edges(Math.max(0, r - 1));
        c.method_25294(x, y + r, x + 1, y + h - r, color);
        c.method_25294(x + w - 1, y + r, x + w, y + h - r, color);
        for (int row = 0; row < r; ++row) {
            float inside = row == 0 ? -1.0f : 1.0f + inner[row - 1];
            GuiPrimitives.borderRow(c, x, y + row, w, outer[row], inside, color);
            GuiPrimitives.borderRow(c, x, y + h - row - 1, w, outer[row], inside, color);
        }
    }

    private static void borderRow(class_332 c, int x, int y, int w, float outer, float inner, int color) {
        if (inner < 0.0f) {
            int start = (int)outer;
            c.method_25294(x + start + 1, y, x + w - start - 1, y + 1, color);
            int edge = GuiTheme.opacity(color, 1.0f - (outer - (float)start));
            c.method_25294(x + start, y, x + start + 1, y + 1, edge);
            c.method_25294(x + w - start - 1, y, x + w - start, y + 1, edge);
            return;
        }
        int end = (int)inner;
        int start = (int)outer;
        float coverage = end == start ? inner - outer : 1.0f - (outer - (float)start);
        int edge = GuiTheme.opacity(color, coverage);
        c.method_25294(x + start, y, x + start + 1, y + 1, edge);
        c.method_25294(x + w - start - 1, y, x + w - start, y + 1, edge);
        if (end > start + 1) {
            c.method_25294(x + start + 1, y, x + end, y + 1, color);
            c.method_25294(x + w - end, y, x + w - start - 1, y + 1, color);
        }
        if (end > start && inner > (float)end) {
            int innerEdge = GuiTheme.opacity(color, inner - (float)end);
            c.method_25294(x + end, y, x + end + 1, y + 1, innerEdge);
            c.method_25294(x + w - end - 1, y, x + w - end, y + 1, innerEdge);
        }
    }

    private static float[] edges(int radius) {
        float[] result = EDGES[radius];
        if (result == null) {
            result = new float[radius];
            for (int row = 0; row < radius; ++row) {
                double dy = (double)(radius - row) - 0.5;
                result[row] = (float)((double)radius - Math.sqrt((double)(radius * radius) - dy * dy));
            }
            GuiPrimitives.EDGES[radius] = result;
        }
        return result;
    }

    public static void rounded(class_332 c, Rect r, int radius, int color) {
        GuiPrimitives.panel(c, r, color, radius);
    }

    public static void rounded(class_332 c, int x, int y, int w, int h, int radius, int color) {
        if (w <= 0 || h <= 0 || color >>> 24 == 0) {
            return;
        }
        int r = Math.clamp((long)radius, 0, Math.min(32, Math.min(w, h) / 2));
        if (r == 0) {
            c.method_25294(x, y, x + w, y + h, color);
            return;
        }
        float[] edges = GuiPrimitives.edges(r);
        c.method_25294(x, y + r, x + w, y + h - r, color);
        for (int row = 0; row < r; ++row) {
            int inset = (int)edges[row];
            int edge = GuiTheme.opacity(color, 1.0f - (edges[row] - (float)inset));
            GuiPrimitives.roundedRow(c, x, y + row, w, inset, color, edge);
            GuiPrimitives.roundedRow(c, x, y + h - row - 1, w, inset, color, edge);
        }
    }

    private static void roundedRow(class_332 c, int x, int y, int w, int inset, int color, int edge) {
        c.method_25294(x + inset + 1, y, x + w - inset - 1, y + 1, color);
        c.method_25294(x + inset, y, x + inset + 1, y + 1, edge);
        c.method_25294(x + w - inset - 1, y, x + w - inset, y + 1, edge);
    }

    public static void line(class_332 c, int x1, int y1, int x2, int y2, int color) {
        Glass.line(c, x1, y1, x2, y2, 1, color);
    }

    public static void line(class_332 c, int x1, int y1, int x2, int y2, int thickness, int color) {
        Glass.line(c, x1, y1, x2, y2, thickness, color);
    }

    public static void icon(class_332 c, String id, int centerX, int centerY, int size, int color) {
        Glass.icon(c, id, centerX, centerY, size, color);
    }
}

