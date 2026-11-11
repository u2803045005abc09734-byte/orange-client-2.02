/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_332
 */
package de.orange.client.ui.premium.components;

import de.orange.client.ui.Rect;
import de.orange.client.ui.premium.GuiFrame;
import de.orange.client.ui.premium.GuiPrimitives;
import de.orange.client.ui.premium.GuiText;
import de.orange.client.ui.premium.GuiTheme;
import de.orange.client.ui.premium.components.PopupManager;
import de.orange.client.ui.premium.components.SearchComponent;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_332;

public final class ColorPickerComponent {
    private final IntSupplier value;
    private final IntConsumer changed;
    private final boolean allowAlpha;
    private Rect preview = new Rect(0, 0, 0, 0);
    private PopupManager manager;

    public ColorPickerComponent(IntSupplier value, IntConsumer changed) {
        this(value, changed, false);
    }

    public ColorPickerComponent(IntSupplier value, IntConsumer changed, boolean allowAlpha) {
        this.value = Objects.requireNonNull(value);
        this.changed = Objects.requireNonNull(changed);
        this.allowAlpha = allowAlpha;
    }

    public void layout(Rect preview, PopupManager manager) {
        this.preview = preview;
        this.manager = manager;
    }

    public void drawPreview(class_332 context, Rect preview, GuiFrame frame, PopupManager manager) {
        this.layout(preview, manager);
        GuiTheme theme = GuiTheme.current();
        boolean hovered = preview.contains(frame.mouseX(), frame.mouseY()) || manager.isOpen(this);
        GuiPrimitives.borderedPanel(context, preview, GuiTheme.opacity(hovered ? theme.secondaryAccent() : theme.border(), frame.alpha()), GuiTheme.opacity(hovered ? theme.secondaryAccent() : theme.border(), frame.alpha()), Math.min(5, preview.h() / 3));
        Rect inside = preview.inset(2);
        if (this.allowAlpha) {
            ColorPickerComponent.checker(context, inside, frame.alpha());
        }
        GuiPrimitives.panel(context, inside, GuiTheme.opacity(this.color(), frame.alpha()), Math.min(4, inside.h() / 3));
    }

    public boolean mouseClicked(double x, double y, int button) {
        if (button != 0 || !this.preview.contains(x, y) || this.manager == null) {
            return false;
        }
        if (this.manager.isOpen(this)) {
            this.manager.close();
        } else {
            this.open(this.manager);
        }
        return true;
    }

    public void open(PopupManager manager) {
        this.manager = manager;
        manager.open(this, new PickerPopup(manager));
    }

    private int color() {
        return this.allowAlpha ? this.value.getAsInt() : this.value.getAsInt() | 0xFF000000;
    }

    private static int channel(int color, int index) {
        return color >>> (index == 3 ? 24 : 16 - index * 8) & 0xFF;
    }

    private static float unit(float value) {
        return Float.isFinite(value) ? Math.clamp(value, 0.0f, 1.0f) : 0.0f;
    }

    public static int fromHsv(float hue, float saturation, float value) {
        float g;
        float r;
        float h = ColorPickerComponent.unit(hue) * 6.0f;
        float s = ColorPickerComponent.unit(saturation);
        float v = ColorPickerComponent.unit(value);
        int sector = (int)h;
        float f = h - (float)sector;
        float p = v * (1.0f - s);
        float q = v * (1.0f - f * s);
        float t = v * (1.0f - (1.0f - f) * s);
        return 0xFF000000 | Math.round(r * 255.0f) << 16 | Math.round(g * 255.0f) << 8 | Math.round((switch (sector % 6) {
            case 0 -> {
                r = v;
                g = t;
                yield p;
            }
            case 1 -> {
                r = q;
                g = v;
                yield p;
            }
            case 2 -> {
                r = p;
                g = v;
                yield t;
            }
            case 3 -> {
                r = p;
                g = q;
                yield v;
            }
            case 4 -> {
                r = t;
                g = p;
                yield v;
            }
            default -> {
                r = v;
                g = p;
                yield q;
            }
        }) * 255.0f);
    }

    public static float[] toHsv(int color) {
        float min;
        float r = (float)(color >>> 16 & 0xFF) / 255.0f;
        float g = (float)(color >>> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        float max = Math.max(r, Math.max(g, b));
        float d = max - (min = Math.min(r, Math.min(g, b)));
        float h = d == 0.0f ? 0.0f : (max == r ? (g - b) / d % 6.0f : (max == g ? (b - r) / d + 2.0f : (r - g) / d + 4.0f));
        if ((h /= 6.0f) < 0.0f) {
            h += 1.0f;
        }
        return new float[]{h, max == 0.0f ? 0.0f : d / max, max};
    }

    public static OptionalInt parseHex(String text, boolean allowAlpha) {
        if (text == null) {
            return OptionalInt.empty();
        }
        String hex = text.startsWith("#") ? text.substring(1) : text;
        if (!hex.matches(allowAlpha ? "(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})" : "[0-9a-fA-F]{6}")) {
            return OptionalInt.empty();
        }
        long parsed = Long.parseLong(hex, 16);
        return OptionalInt.of(hex.length() == 8 ? (int)(parsed >>> 8 | (parsed & 0xFFL) << 24) : 0xFF000000 | (int)parsed);
    }

    public static String toHex(int color, boolean alpha) {
        return alpha ? String.format(Locale.ROOT, "#%08X", (color & 0xFFFFFF) << 8 | color >>> 24) : String.format(Locale.ROOT, "#%06X", color & 0xFFFFFF);
    }

    private static void checker(class_332 context, Rect rect, float alpha) {
        for (int y = rect.y(); y < rect.bottom(); y += 5) {
            for (int x = rect.x(); x < rect.right(); x += 5) {
                int color = ((x - rect.x()) / 5 + (y - rect.y()) / 5 & 1) == 0 ? -10066318 : -13026746;
                context.method_25294(x, y, Math.min(x + 5, rect.right()), Math.min(y + 5, rect.bottom()), GuiTheme.opacity(color, alpha));
            }
        }
    }

    private final class PickerPopup
    implements PopupManager.Popup {
        private final PopupManager manager;
        private final SearchComponent hex;
        private Rect bounds;
        private Rect sv;
        private Rect hueBar;
        private Rect sample;
        private Rect close;
        private Rect hexBounds;
        private final Rect[] channels = new Rect[4];
        private float hue;
        private float saturation;
        private float brightness;
        private int current;
        private int drag;

        private PickerPopup(PopupManager manager) {
            this.manager = manager;
            this.bounds = new Rect(ColorPickerComponent.this.preview.right() - 264, ColorPickerComponent.this.preview.bottom() + 5, 264, 196);
            this.read(ColorPickerComponent.this.color());
            this.hex = new SearchComponent(ColorPickerComponent.this.allowAlpha ? "RRGGBBAA" : "RRGGBB", this::hexChanged, false, ColorPickerComponent.this.allowAlpha ? 9 : 7, false);
            this.hex.setTextPredicate(text -> text.matches(ColorPickerComponent.this.allowAlpha ? "#?[0-9a-fA-F]{0,8}" : "#?[0-9a-fA-F]{0,6}"));
            this.syncHex();
            this.layout(this.bounds);
        }

        @Override
        public Rect bounds() {
            return this.bounds;
        }

        @Override
        public void layout(Rect limit) {
            this.bounds = PopupManager.clamp(new Rect(ColorPickerComponent.this.preview.right() - 264, ColorPickerComponent.this.preview.bottom() + 5, 264, 196), limit);
            int x = this.bounds.x();
            int y = this.bounds.y();
            int planeHeight = ColorPickerComponent.this.allowAlpha ? 48 : 62;
            int planeWidth = Math.max(20, this.bounds.w() - 84);
            this.sv = new Rect(x + 10, y + 25, planeWidth, planeHeight);
            this.hueBar = new Rect(this.sv.right() + 8, this.sv.y(), 12, planeHeight);
            this.sample = new Rect(this.hueBar.right() + 10, this.sv.y(), Math.max(10, this.bounds.right() - this.hueBar.right() - 20), 33);
            this.close = new Rect(this.bounds.right() - 26, y + 3, 22, 19);
            int channelStart = this.sv.bottom() + 10;
            for (int i = 0; i < (ColorPickerComponent.this.allowAlpha ? 4 : 3); ++i) {
                this.channels[i] = new Rect(x + 25, channelStart + i * 19 + 5, Math.max(20, this.bounds.w() - 76), 4);
            }
            this.hexBounds = new Rect(x + 42, this.bounds.bottom() - 33, Math.max(20, this.bounds.w() - 52), 23);
            this.hex.layout(this.hexBounds);
        }

        @Override
        public void render(class_332 context, GuiFrame frame) {
            int actual = ColorPickerComponent.this.color();
            if (actual != this.current) {
                this.read(actual);
                this.syncHex();
            }
            GuiTheme theme = GuiTheme.current();
            GuiPrimitives.rounded(context, this.bounds.x() - 3, this.bounds.y() + 3, this.bounds.w() + 6, this.bounds.h() + 4, 9, GuiTheme.alpha(-16777216, frame.alpha() * 0.27f));
            GuiPrimitives.borderedPanel(context, this.bounds, GuiTheme.opacity(theme.panel(), frame.alpha()), GuiTheme.opacity(GuiTheme.mix(theme.border(), theme.accent(), 0.35f), frame.alpha()), 8);
            GuiText.bold(context, "Custom colour", this.bounds.x() + 10, this.bounds.y() + 8, GuiTheme.opacity(theme.text(), frame.alpha()));
            int closeColor = GuiTheme.opacity(this.close.contains(frame.mouseX(), frame.mouseY()) ? theme.text() : theme.muted(), frame.alpha());
            GuiPrimitives.line(context, this.close.centerX() - 3, this.close.centerY() - 3, this.close.centerX() + 3, this.close.centerY() + 3, closeColor);
            GuiPrimitives.line(context, this.close.centerX() + 3, this.close.centerY() - 3, this.close.centerX() - 3, this.close.centerY() + 3, closeColor);
            this.drawPlane(context, frame.alpha());
            for (int row = 0; row < this.hueBar.h(); ++row) {
                int c = ColorPickerComponent.fromHsv((float)row / (float)Math.max(1, this.hueBar.h() - 1), 1.0f, 1.0f);
                context.method_25294(this.hueBar.x(), this.hueBar.y() + row, this.hueBar.right(), this.hueBar.y() + row + 1, GuiTheme.opacity(c, frame.alpha()));
            }
            int hy = this.hueBar.y() + Math.round(this.hue * (float)Math.max(1, this.hueBar.h() - 1));
            context.method_25294(this.hueBar.x() - 1, hy - 1, this.hueBar.right() + 1, hy + 2, GuiTheme.opacity(theme.text(), frame.alpha()));
            context.method_25294(this.hueBar.x(), hy, this.hueBar.right(), hy + 1, GuiTheme.opacity(ColorPickerComponent.fromHsv(this.hue, 1.0f, 1.0f), frame.alpha()));
            ColorPickerComponent.checker(context, this.sample, frame.alpha());
            GuiPrimitives.panel(context, this.sample, GuiTheme.opacity(this.current, frame.alpha()), 3);
            GuiText.small(context, "Preview", this.sample.x(), this.sample.bottom() + 5, GuiTheme.opacity(theme.muted(), frame.alpha()));
            int count = ColorPickerComponent.this.allowAlpha ? 4 : 3;
            String[] labels = new String[]{"R", "G", "B", "A"};
            for (int i = 0; i < count; ++i) {
                Rect track = this.channels[i];
                int channel = ColorPickerComponent.channel(this.current, i);
                GuiText.small(context, labels[i], this.bounds.x() + 10, track.y() - 3, GuiTheme.opacity(theme.muted(), frame.alpha()));
                GuiPrimitives.panel(context, track, GuiTheme.opacity(theme.border(), frame.alpha()), 2);
                int width = Math.round((float)((track.w() - 1) * channel) / 255.0f);
                GuiPrimitives.panel(context, new Rect(track.x(), track.y(), width + 1, track.h()), GuiTheme.opacity(theme.accent(), frame.alpha()), 2);
                GuiPrimitives.rounded(context, track.x() + width - 3, track.y() - 2, 7, 8, 4, GuiTheme.opacity(theme.text(), frame.alpha()));
                GuiText.small(context, Integer.toString(channel), track.right() + 9, track.y() - 3, GuiTheme.opacity(theme.text(), frame.alpha()));
            }
            GuiText.small(context, "HEX", this.bounds.x() + 10, this.hexBounds.y() + 7, GuiTheme.opacity(theme.muted(), frame.alpha()));
            this.hex.draw(context, this.hexBounds, frame);
        }

        private void drawPlane(class_332 context, float alpha) {
            int columns = Math.min(48, this.sv.w());
            for (int i = 0; i < columns; ++i) {
                int x1 = this.sv.x() + i * this.sv.w() / columns;
                int x2 = this.sv.x() + (i + 1) * this.sv.w() / columns;
                context.method_25296(x1, this.sv.y(), x2, this.sv.bottom(), GuiTheme.opacity(ColorPickerComponent.fromHsv(this.hue, (float)i / (float)Math.max(1, columns - 1), 1.0f), alpha), GuiTheme.alpha(-16777216, alpha));
            }
            int sx = this.sv.x() + Math.round(this.saturation * (float)(this.sv.w() - 1));
            int sy = this.sv.y() + Math.round((1.0f - this.brightness) * (float)(this.sv.h() - 1));
            GuiPrimitives.rounded(context, sx - 3, sy - 3, 7, 7, 3, GuiTheme.alpha(-16777216, alpha));
            GuiPrimitives.rounded(context, sx - 2, sy - 2, 5, 5, 2, GuiTheme.alpha(-1, alpha));
            context.method_25294(sx - 1, sy - 1, sx + 2, sy + 2, GuiTheme.opacity(this.current | 0xFF000000, alpha));
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            if (button != 0) {
                return true;
            }
            if (this.close.contains(x, y)) {
                this.manager.close();
                return true;
            }
            if (this.hexBounds.contains(x, y)) {
                this.drag = 0;
                return this.hex.mouseClicked(x, y, button);
            }
            this.hex.setFocused(false);
            if (this.sv.contains(x, y)) {
                this.drag = 1;
            } else if (this.hueBar.contains(x, y)) {
                this.drag = 2;
            } else {
                for (int i = 0; i < (ColorPickerComponent.this.allowAlpha ? 4 : 3); ++i) {
                    Rect track = this.channels[i];
                    if (!new Rect(track.x() - 3, track.y() - 6, track.w() + 6, 16).contains(x, y)) continue;
                    this.drag = i + 3;
                    break;
                }
            }
            if (this.drag != 0) {
                this.adjust(x, y);
            }
            return true;
        }

        @Override
        public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
            if (button != 0) {
                return true;
            }
            if (this.drag != 0) {
                this.adjust(x, y);
            } else {
                this.hex.mouseDragged(x, y, button, dx, dy);
            }
            return true;
        }

        @Override
        public boolean mouseReleased(double x, double y, int button) {
            this.drag = 0;
            this.hex.mouseReleased(x, y, button);
            return true;
        }

        @Override
        public boolean keyPressed(class_11908 input) {
            return this.hex.keyPressed(input);
        }

        @Override
        public boolean charTyped(class_11905 input) {
            return this.hex.charTyped(input);
        }

        @Override
        public void close() {
            this.drag = 0;
            this.hex.setFocused(false);
        }

        private void adjust(double x, double y) {
            if (this.drag == 1) {
                this.saturation = ColorPickerComponent.unit((float)((x - (double)this.sv.x()) / (double)Math.max(1, this.sv.w() - 1)));
                this.brightness = 1.0f - ColorPickerComponent.unit((float)((y - (double)this.sv.y()) / (double)Math.max(1, this.sv.h() - 1)));
                this.apply(this.current & 0xFF000000 | ColorPickerComponent.fromHsv(this.hue, this.saturation, this.brightness) & 0xFFFFFF, false);
            } else if (this.drag == 2) {
                this.hue = ColorPickerComponent.unit((float)((y - (double)this.hueBar.y()) / (double)Math.max(1, this.hueBar.h() - 1)));
                this.apply(this.current & 0xFF000000 | ColorPickerComponent.fromHsv(this.hue, this.saturation, this.brightness) & 0xFFFFFF, false);
            } else if (this.drag >= 3) {
                int index = this.drag - 3;
                int amount = Math.round(ColorPickerComponent.unit((float)((x - (double)this.channels[index].x()) / (double)Math.max(1, this.channels[index].w() - 1))) * 255.0f);
                int shift = index == 3 ? 24 : 16 - index * 8;
                this.apply(this.current & ~(255 << shift) | amount << shift, true);
            }
            this.syncHex();
        }

        private void hexChanged(String text) {
            ColorPickerComponent.parseHex(text, ColorPickerComponent.this.allowAlpha).ifPresent(c -> this.apply(c, true));
        }

        private void syncHex() {
            this.hex.setTextSilently(ColorPickerComponent.toHex(this.current, ColorPickerComponent.this.allowAlpha));
        }

        private void apply(int color, boolean updateHsv) {
            int n = this.current = ColorPickerComponent.this.allowAlpha ? color : color | 0xFF000000;
            if (updateHsv) {
                this.read(this.current);
            }
            ColorPickerComponent.this.changed.accept(this.current);
        }

        private void read(int color) {
            this.current = color;
            float[] hsv = ColorPickerComponent.toHsv(color);
            if (hsv[1] > 0.0f && hsv[2] > 0.0f) {
                this.hue = hsv[0];
            }
            this.saturation = hsv[1];
            this.brightness = hsv[2];
        }
    }
}

