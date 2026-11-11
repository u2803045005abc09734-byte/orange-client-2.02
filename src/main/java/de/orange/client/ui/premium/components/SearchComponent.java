/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_11910
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_342
 */
package de.orange.client.ui.premium.components;

import de.orange.client.mixin.TextFieldSelectionAccessor;
import de.orange.client.ui.Rect;
import de.orange.client.ui.premium.GuiFrame;
import de.orange.client.ui.premium.GuiPrimitives;
import de.orange.client.ui.premium.GuiText;
import de.orange.client.ui.premium.GuiTheme;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_11910;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_342;

public final class SearchComponent {
    private final PremiumField field;
    private final boolean searchIcon;
    private final boolean clearButton;
    private Rect bounds;
    private Rect clear;
    private boolean silent;
    private boolean dragging;

    public SearchComponent(String placeholder, Consumer<String> changed) {
        this(placeholder, changed, true, 160, true);
    }

    public SearchComponent(String placeholder, Consumer<String> changed, boolean searchIcon, int maxLength, boolean clearButton) {
        this.clear = this.bounds = new Rect(0, 0, 0, 0);
        this.searchIcon = searchIcon;
        this.clearButton = clearButton;
        this.field = new PremiumField(Objects.requireNonNullElse(placeholder, ""));
        this.field.method_1880(Math.max(1, maxLength));
        this.field.method_1863(value -> {
            if (!this.silent && changed != null) {
                changed.accept((String)value);
            }
        });
    }

    public void draw(class_332 context, Rect bounds, GuiFrame frame) {
        this.layout(bounds);
        GuiTheme theme = GuiTheme.current();
        boolean hovered = bounds.contains(frame.mouseX(), frame.mouseY());
        GuiPrimitives.borderedPanel(context, bounds, GuiTheme.opacity(hovered || this.field.method_25370() ? theme.hover() : theme.secondary(), frame.alpha()), GuiTheme.opacity(this.field.method_25370() ? GuiTheme.mix(theme.border(), theme.accent(), 0.65f) : theme.border(), frame.alpha()), 5);
        if (this.searchIcon) {
            GuiPrimitives.icon(context, "search", bounds.x() + 11, bounds.centerY(), 9, GuiTheme.opacity(theme.muted(), frame.alpha()));
        }
        this.field.alpha = frame.alpha();
        this.field.method_48579(context, (int)frame.mouseX(), (int)frame.mouseY(), frame.dt());
        if (this.clearButton && !this.field.method_1882().isEmpty()) {
            int color = GuiTheme.opacity(this.clear.contains(frame.mouseX(), frame.mouseY()) ? theme.text() : theme.muted(), frame.alpha());
            GuiPrimitives.line(context, this.clear.centerX() - 2, this.clear.centerY() - 2, this.clear.centerX() + 2, this.clear.centerY() + 2, color);
            GuiPrimitives.line(context, this.clear.centerX() + 2, this.clear.centerY() - 2, this.clear.centerX() - 2, this.clear.centerY() + 2, color);
        }
    }

    public void layout(Rect bounds) {
        this.bounds = bounds;
        this.clear = new Rect(bounds.right() - 22, bounds.y(), 22, bounds.h());
        int left = this.searchIcon ? 23 : 8;
        int right = this.clearButton ? 23 : 8;
        this.field.method_46421(bounds.x() + left);
        this.field.method_46419(bounds.y());
        this.field.method_25358(Math.max(1, bounds.w() - left - right));
        this.field.method_53533(Math.max(1, bounds.h()));
    }

    public boolean mouseClicked(double x, double y, int button) {
        return this.mouseClicked(new class_11909(x, y, new class_11910(button, 0)), false);
    }

    public boolean mouseClicked(class_11909 click, boolean doubled) {
        this.dragging = false;
        if (!this.bounds.contains(click.comp_4798(), click.comp_4799())) {
            this.field.method_25365(false);
            return false;
        }
        if (click.method_74245() != 0) {
            return true;
        }
        this.field.method_25365(true);
        if (this.clearButton && !this.field.method_1882().isEmpty() && this.clear.contains(click.comp_4798(), click.comp_4799())) {
            this.field.method_1852("");
            return true;
        }
        this.field.method_25348(click, doubled);
        this.dragging = true;
        return true;
    }

    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (!this.dragging || !this.field.method_25370() || button != 0) {
            return false;
        }
        this.field.dragTo(new class_11909(x, y, new class_11910(button, 0)), dx, dy);
        return true;
    }

    public boolean mouseReleased(double x, double y, int button) {
        boolean consumed;
        boolean bl = consumed = this.dragging && button == 0;
        if (button == 0) {
            this.dragging = false;
        }
        return consumed;
    }

    public boolean keyPressed(class_11908 input) {
        return this.field.method_25370() && this.field.method_25404(input);
    }

    public boolean charTyped(class_11905 input) {
        return this.field.method_25370() && this.field.method_25400(input);
    }

    public String text() {
        return this.field.method_1882();
    }

    public void setText(String text) {
        this.field.method_1852(Objects.requireNonNullElse(text, ""));
    }

    public void setTextSilently(String text) {
        this.silent = true;
        try {
            this.setText(text);
        }
        finally {
            this.silent = false;
        }
    }

    public void setTextPredicate(Predicate<String> predicate) {
        this.field.method_1890(predicate);
    }

    public void setFocused(boolean focused) {
        this.field.method_25365(focused);
        if (!focused) {
            this.dragging = false;
        }
    }

    public boolean isFocused() {
        return this.field.method_25370();
    }

    public void clear() {
        this.field.method_1852("");
    }

    public Rect bounds() {
        return this.bounds;
    }

    public class_342 field() {
        return this.field;
    }

    private static final class PremiumField
    extends class_342 {
        private final String placeholder;
        private int start;
        private float alpha = 1.0f;

        private PremiumField(String placeholder) {
            super(class_310.method_1551().field_1772, 0, 0, 10, 22, (class_2561)class_2561.method_43470((String)placeholder));
            this.placeholder = placeholder;
            this.method_1858(false);
            this.method_71503(false);
        }

        public void method_48579(class_332 context, int mouseX, int mouseY, float tickDelta) {
            int next;
            String text = this.method_1882();
            int cursor = Math.clamp((long)this.method_1881(), 0, text.length());
            int available = Math.max(0, this.method_25368() - 2);
            this.start = Math.clamp((long)this.start, 0, cursor);
            while (this.start < cursor && GuiText.width(text.substring(this.start, cursor)) > available - 2) {
                this.start = text.offsetByCodePoints(this.start, 1);
            }
            while (this.start > 0 && GuiText.width(text.substring(text.offsetByCodePoints(this.start, -1), cursor)) <= available - 2) {
                this.start = text.offsetByCodePoints(this.start, -1);
            }
            int end = this.start;
            while (end < text.length() && GuiText.width(text.substring(this.start, next = text.offsetByCodePoints(end, 1))) <= available) {
                end = next;
            }
            int textY = this.method_46427() + (this.method_25364() - 10) / 2;
            GuiTheme theme = GuiTheme.current();
            context.method_44379(this.method_46426(), this.method_46427() + 1, this.method_55442(), this.method_55443() - 1);
            if (text.isEmpty() && !this.method_25370()) {
                GuiText.text(context, GuiText.trim(this.placeholder, available), this.method_46426(), textY, GuiTheme.opacity(theme.disabled(), this.alpha));
            } else {
                if (!this.method_1866().isEmpty()) {
                    int anchor = ((TextFieldSelectionAccessor)((Object)this)).orange$selectionEnd();
                    int a = Math.clamp((long)Math.min(cursor, anchor), this.start, end);
                    int b = Math.clamp((long)Math.max(cursor, anchor), this.start, end);
                    int x1 = this.method_46426() + GuiText.width(text.substring(this.start, a));
                    int x2 = this.method_46426() + GuiText.width(text.substring(this.start, b));
                    context.method_25294(x1, textY - 1, x2, textY + 11, GuiTheme.alpha(theme.accent(), this.alpha * 0.32f));
                }
                GuiText.text(context, text.substring(this.start, end), this.method_46426(), textY, GuiTheme.opacity(theme.text(), this.alpha));
                if (this.method_25370() && (System.nanoTime() / 500000000L & 1L) == 0L) {
                    int x = this.method_46426() + GuiText.width(text.substring(this.start, cursor));
                    context.method_25294(x, textY - 1, x + 1, textY + 11, GuiTheme.opacity(theme.secondaryAccent(), this.alpha));
                }
            }
            context.method_44380();
        }

        public void method_25348(class_11909 click, boolean doubled) {
            this.method_25365(true);
            int closest = this.cursorAt(click.comp_4798());
            String value = this.method_1882();
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

        private void dragTo(class_11909 click, double dx, double dy) {
            this.method_25349(click, dx, dy);
        }

        private int cursorAt(double mouseX) {
            String value = this.method_1882();
            int closest = Math.min(this.start, value.length());
            double target = mouseX - (double)this.method_46426();
            while (closest < value.length()) {
                int nextWidth;
                int next = value.offsetByCodePoints(closest, 1);
                int previousWidth = GuiText.width(value.substring(this.start, closest));
                if (target < (double)(previousWidth + (nextWidth = GuiText.width(value.substring(this.start, next)))) * 0.5) break;
                closest = next;
            }
            return closest;
        }
    }
}

