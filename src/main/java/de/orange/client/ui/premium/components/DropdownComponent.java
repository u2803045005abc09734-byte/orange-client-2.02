/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_332
 */
package de.orange.client.ui.premium.components;

import de.orange.client.ui.Anim;
import de.orange.client.ui.Rect;
import de.orange.client.ui.premium.GuiFrame;
import de.orange.client.ui.premium.GuiPrimitives;
import de.orange.client.ui.premium.GuiText;
import de.orange.client.ui.premium.GuiTheme;
import de.orange.client.ui.premium.components.PopupManager;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.class_11908;
import net.minecraft.class_332;

public final class DropdownComponent<T> {
    private final List<T> options;
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private final Function<T, String> label;
    private final Anim arrow = new Anim();
    private final Anim hover = new Anim();
    private Rect bounds = new Rect(0, 0, 0, 0);
    private PopupManager manager;

    public DropdownComponent(List<T> options, Supplier<T> getter, Consumer<T> setter, Function<T, String> label) {
        this.options = List.copyOf(options);
        this.getter = Objects.requireNonNull(getter);
        this.setter = Objects.requireNonNull(setter);
        this.label = Objects.requireNonNull(label);
    }

    public DropdownComponent(List<T> options, Supplier<T> getter, Consumer<T> setter) {
        this(options, getter, setter, String::valueOf);
    }

    public T value() {
        return this.getter.get();
    }

    public List<T> options() {
        return this.options;
    }

    public void layout(Rect bounds, PopupManager manager) {
        this.bounds = Objects.requireNonNull(bounds);
        this.manager = Objects.requireNonNull(manager);
    }

    public Rect bounds() {
        return this.bounds;
    }

    public void draw(class_332 c, Rect bounds, GuiFrame frame, PopupManager manager) {
        this.layout(bounds, manager);
        GuiTheme theme = GuiTheme.current();
        float h = this.hover.to(bounds.contains(frame.mouseX(), frame.mouseY()) ? 1.0f : 0.0f, frame.dt(), frame.reduced());
        float p = this.arrow.to(manager.isOpen(this) ? 1.0f : 0.0f, frame.dt(), frame.reduced());
        GuiPrimitives.borderedPanel(c, bounds, GuiTheme.opacity(GuiTheme.mix(theme.secondary(), theme.hover(), h * 0.6f), frame.alpha()), GuiTheme.opacity(theme.border(), frame.alpha()), 5);
        T value = this.value();
        String text = value == null ? "Select" : Objects.toString(this.label.apply(value), "");
        GuiText.text(c, GuiText.trim(text, Math.max(0, bounds.w() - 26)), bounds.x() + 8, bounds.centerY() - 5, GuiTheme.opacity(this.options.isEmpty() ? theme.disabled() : theme.text(), frame.alpha()));
        int cx = bounds.right() - 10;
        int cy = bounds.centerY();
        double angle = (double)p * Math.PI;
        int x1 = cx + (int)Math.round(-2.0 * Math.cos(angle) + Math.sin(angle));
        int y1 = cy + (int)Math.round(-2.0 * Math.sin(angle) - Math.cos(angle));
        int x2 = cx - (int)Math.round(Math.sin(angle));
        int y2 = cy + (int)Math.round(Math.cos(angle));
        int x3 = cx + (int)Math.round(2.0 * Math.cos(angle) + Math.sin(angle));
        int y3 = cy + (int)Math.round(2.0 * Math.sin(angle) - Math.cos(angle));
        int color = GuiTheme.opacity(theme.muted(), frame.alpha());
        GuiPrimitives.line(c, x1, y1, x2, y2, color);
        GuiPrimitives.line(c, x2, y2, x3, y3, color);
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || this.manager == null || this.options.isEmpty() || !this.bounds.contains(mx, my)) {
            return false;
        }
        if (this.manager.isOpen(this)) {
            this.manager.close();
        } else {
            this.manager.open(this, new OptionsPopup(this.manager));
        }
        return true;
    }

    public boolean mouseClicked(double mx, double my, int button, PopupManager manager) {
        this.manager = Objects.requireNonNull(manager);
        return this.mouseClicked(mx, my, button);
    }

    private final class OptionsPopup
    implements PopupManager.Popup {
        private static final int ROW = 20;
        private static final int PADDING = 4;
        private final PopupManager manager;
        private Rect full = new Rect(0, 0, 0, 0);
        private Rect visible = new Rect(0, 0, 0, 0);
        private int scroll;
        private int focus;
        private float age;
        private float closeAge;
        private float heightFraction;
        private float closedFrom;
        private boolean upward;
        private boolean closing;

        private OptionsPopup(PopupManager manager) {
            this.focus = Math.max(0, DropdownComponent.this.options.indexOf(DropdownComponent.this.value()));
            this.manager = manager;
        }

        @Override
        public void layout(Rect limit) {
            int width = Math.min(Math.max(DropdownComponent.this.bounds.w(), 96), Math.max(0, limit.w()));
            int height = Math.min(8 + 20 * DropdownComponent.this.options.size(), Math.min(180, Math.max(0, limit.h())));
            int below = limit.bottom() - DropdownComponent.this.bounds.bottom() - 3;
            int above = DropdownComponent.this.bounds.y() - limit.y() - 3;
            this.upward = height > below && above > below;
            int y = this.upward ? DropdownComponent.this.bounds.y() - height - 3 : DropdownComponent.this.bounds.bottom() + 3;
            this.full = PopupManager.clamp(new Rect(DropdownComponent.this.bounds.x(), y, width, height), limit);
            this.scroll = Math.clamp((long)this.scroll, 0, this.maxScroll());
            this.updateVisible();
        }

        private void updateVisible() {
            int height = Math.clamp((long)Math.round((float)this.full.h() * this.heightFraction), 0, this.full.h());
            this.visible = new Rect(this.full.x(), this.upward ? this.full.bottom() - height : this.full.y(), this.full.w(), height);
        }

        @Override
        public Rect bounds() {
            return this.visible;
        }

        @Override
        public void render(class_332 c, GuiFrame frame) {
            if (this.closing) {
                this.closeAge += frame.dt();
                this.heightFraction = frame.reduced() ? 0.0f : this.closedFrom * (1.0f - Anim.easeOutCubic(this.closeAge / 0.14f));
            } else {
                this.age += frame.dt();
                this.heightFraction = frame.reduced() ? 1.0f : Anim.easeOutCubic(this.age / 0.16f);
            }
            this.updateVisible();
            if (this.visible.h() < 1 || this.visible.w() < 1) {
                return;
            }
            c.method_44379(this.visible.x(), this.visible.y(), this.visible.right(), this.visible.bottom());
            GuiTheme theme = GuiTheme.current();
            GuiPrimitives.borderedPanel(c, this.full, GuiTheme.opacity(theme.secondary(), frame.alpha()), GuiTheme.opacity(theme.border(), frame.alpha()), 6);
            for (int i = 0; i < DropdownComponent.this.options.size(); ++i) {
                boolean hovered;
                Rect row = this.row(i);
                if (row.bottom() <= this.visible.y() || row.y() >= this.visible.bottom()) continue;
                boolean selected = Objects.equals(DropdownComponent.this.options.get(i), DropdownComponent.this.value());
                boolean bl = hovered = this.visible.contains(frame.mouseX(), frame.mouseY()) && row.contains(frame.mouseX(), frame.mouseY());
                if (selected || hovered || i == this.focus) {
                    GuiPrimitives.panel(c, row, GuiTheme.opacity(theme.hover(), frame.alpha() * (selected ? 1.0f : 0.7f)), 3);
                }
                GuiText.text(c, GuiText.trim(Objects.toString(DropdownComponent.this.label.apply(DropdownComponent.this.options.get(i)), ""), row.w() - 12), row.x() + 6, row.centerY() - 5, GuiTheme.opacity(selected ? theme.secondaryAccent() : theme.text(), frame.alpha()));
            }
            if (this.maxScroll() > 0) {
                int track = Math.max(1, this.full.h() - 8);
                int thumb = Math.max(10, track * track / (20 * DropdownComponent.this.options.size()));
                int y = this.full.y() + 4 + this.scroll * (track - thumb) / this.maxScroll();
                c.method_25294(this.full.right() - 3, y, this.full.right() - 2, y + thumb, GuiTheme.opacity(theme.muted(), frame.alpha() * 0.6f));
            }
            c.method_44380();
        }

        private Rect row(int index) {
            return new Rect(this.full.x() + 4, this.full.y() + 4 + index * 20 - this.scroll, Math.max(0, this.full.w() - 8), 20);
        }

        private int maxScroll() {
            return Math.max(0, DropdownComponent.this.options.size() * 20 - (this.full.h() - 8));
        }

        private void choose(int index) {
            if (index >= 0 && index < DropdownComponent.this.options.size()) {
                DropdownComponent.this.setter.accept(DropdownComponent.this.options.get(index));
                this.manager.close();
            }
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (button != 0 || !this.visible.contains(mx, my) || this.closing) {
                return false;
            }
            for (int i = 0; i < DropdownComponent.this.options.size(); ++i) {
                if (!this.row(i).contains(mx, my)) continue;
                this.choose(i);
                return true;
            }
            return true;
        }

        @Override
        public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
            if (this.visible.contains(mx, my)) {
                this.scroll = Math.clamp((long)(this.scroll - (int)(vertical * 20.0)), 0, this.maxScroll());
            }
            return true;
        }

        @Override
        public boolean keyPressed(class_11908 input) {
            if (input.comp_4795() == 264 || input.comp_4795() == 265) {
                this.focus = Math.floorMod(this.focus + (input.comp_4795() == 264 ? 1 : -1), DropdownComponent.this.options.size());
                int top = this.focus * 20;
                int bottom = top + 20;
                int viewport = Math.max(0, this.full.h() - 8);
                if (top < this.scroll) {
                    this.scroll = top;
                } else if (bottom > this.scroll + viewport) {
                    this.scroll = bottom - viewport;
                }
                this.scroll = Math.clamp((long)this.scroll, 0, this.maxScroll());
                return true;
            }
            if (input.comp_4795() == 257 || input.comp_4795() == 335 || input.comp_4795() == 32) {
                this.choose(this.focus);
                return true;
            }
            if (input.comp_4795() == 256) {
                this.manager.close();
                return true;
            }
            return false;
        }

        @Override
        public void close() {
            this.closing = true;
            this.closedFrom = this.heightFraction;
            this.closeAge = 0.0f;
        }
    }
}

