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
package de.orange.client.ui.premium;

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

public final class GuiText {
    public static final float BODY_SIZE = 10.0f;
    public static final float SMALL_SIZE = 8.5f;
    public static final float SOURCE_SIZE = 11.0f;
    private static final class_2583 REGULAR = class_2583.field_24360.method_27704((class_11719)new class_11719.class_11721(class_2960.method_60655((String)"orange", (String)"ui")));
    private static final class_2583 BOLD = class_2583.field_24360.method_27704((class_11719)new class_11719.class_11721(class_2960.method_60655((String)"orange", (String)"ui_bold")));
    private static final Map<String, class_5250> REGULAR_TEXTS = GuiText.cache();
    private static final Map<String, class_5250> BOLD_TEXTS = GuiText.cache();

    private static Map<String, class_5250> cache() {
        return new LinkedHashMap<String, class_5250>(128, 0.75f, true){

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, class_5250> entry) {
                return this.size() > 512;
            }
        };
    }

    private GuiText() {
    }

    private static class_5250 value(String text, boolean bold) {
        String key;
        Map<String, class_5250> cache = bold ? BOLD_TEXTS : REGULAR_TEXTS;
        class_5250 value = cache.get(key = text == null ? "" : text);
        if (value == null) {
            value = class_2561.method_43470((String)key).method_10862(bold ? BOLD : REGULAR);
            cache.put(key, value);
        }
        return value;
    }

    public static void text(class_332 c, String text, int x, int y, int color) {
        GuiText.draw(c, text, x, y, 10.0f, color, false);
    }

    public static void bold(class_332 c, String text, int x, int y, int color) {
        GuiText.draw(c, text, x, y, 10.0f, color, true);
    }

    public static void small(class_332 c, String text, int x, int y, int color) {
        GuiText.draw(c, text, x, y, 8.5f, color, false);
    }

    public static void drawSized(class_332 c, String text, int x, int y, float size, int color) {
        GuiText.draw(c, text, x, y, size, color, false);
    }

    public static void boldSized(class_332 c, String text, int x, int y, float size, int color) {
        GuiText.draw(c, text, x, y, size, color, true);
    }

    private static void draw(class_332 c, String text, int x, int y, float size, int color, boolean bold) {
        if (!Float.isFinite(size) || size <= 0.0f || color >>> 24 == 0) {
            return;
        }
        Matrix3x2fStack matrices = c.method_51448();
        matrices.pushMatrix();
        matrices.translate((float)x, (float)y);
        matrices.scale(size / 11.0f, size / 11.0f);
        c.method_51439(class_310.method_1551().field_1772, (class_2561)GuiText.value(text, bold), 0, 0, color, false);
        matrices.popMatrix();
    }

    public static int width(String text) {
        return GuiText.widthSized(text, 10.0f);
    }

    public static int widthSized(String text, float size) {
        return GuiText.widthSized(text, size, false);
    }

    public static int widthSized(String text, float size, boolean bold) {
        return (int)Math.ceil((float)class_310.method_1551().field_1772.method_27525((class_5348)GuiText.value(text, bold)) * size / 11.0f);
    }

    public static void centered(class_332 c, String text, int centerX, int y, int color) {
        GuiText.text(c, text, centerX - GuiText.width(text) / 2, y, color);
    }

    public static String trim(String text, int width) {
        return GuiText.trim(text, width, 10.0f);
    }

    public static String trim(String text, int width, float size) {
        if (text == null || width <= 0) {
            return "";
        }
        if (GuiText.widthSized(text, size) <= width) {
            return text;
        }
        if (GuiText.widthSized("\u2026", size) > width) {
            return "";
        }
        int low = 0;
        int high = text.codePointCount(0, text.length());
        while (low < high) {
            int middle = low + high + 1 >>> 1;
            if (GuiText.widthSized(text.substring(0, text.offsetByCodePoints(0, middle)) + "\u2026", size) <= width) {
                low = middle;
                continue;
            }
            high = middle - 1;
        }
        return text.substring(0, text.offsetByCodePoints(0, low)) + "\u2026";
    }
}

