/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_458
 */
package de.orange.client.gui.premium;

import de.orange.client.OrangeClient;
import de.orange.client.config.ClientConfig;
import de.orange.client.gui.EmptyScreen;
import de.orange.client.gui.ProfilesScreen;
import de.orange.client.gui.ResourcePackScreen;
import de.orange.client.gui.SearchScreen;
import de.orange.client.gui.premium.GuiPage;
import de.orange.client.gui.premium.OptimizerInfoPopup;
import de.orange.client.hud.HudEditorScreen;
import de.orange.client.module.Category;
import de.orange.client.module.ClientModule;
import de.orange.client.ui.Anim;
import de.orange.client.ui.Rect;
import de.orange.client.ui.premium.GuiFrame;
import de.orange.client.ui.premium.GuiPrimitives;
import de.orange.client.ui.premium.GuiText;
import de.orange.client.ui.premium.GuiTheme;
import de.orange.client.ui.premium.components.ColorPickerComponent;
import de.orange.client.ui.premium.components.ExpandableSection;
import de.orange.client.ui.premium.components.PopupManager;
import de.orange.client.ui.premium.components.SliderComponent;
import de.orange.client.ui.premium.components.ToggleComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_458;

public final class ModulePage
implements GuiPage {
    private static final int ROW_HEIGHT = 35;
    private static final int SETTING_HEIGHT = 27;
    private final Category category;
    private final class_437 parent;
    private final List<ModuleRow> modules;
    private final List<Hit> hits = new ArrayList<Hit>();
    private final List<SliderComponent> sliders = new ArrayList<SliderComponent>();
    private final ExpandableSection general;
    private final Anim generalArrow = new Anim(1.0f);
    private final ToggleComponent reducedMotion;
    private final ToggleComponent blur;
    private final SliderComponent globalScale;
    private final SliderComponent opacity;
    private final ColorPickerComponent guiColour;
    private final ColorPickerComponent hudColour;
    private Rect viewport = new Rect(0, 0, 0, 0);
    private int scroll;
    private int contentHeight;
    private String focusPending;
    private String highlighted;
    private long highlightUntil;
    private long changedAt;
    private boolean dirty;
    private boolean optimizerPending;

    public ModulePage(Category category, class_437 parent) {
        if (category == Category.VISUALS || category == Category.OPTIMIZER) {
            throw new IllegalArgumentException("This page is for PvP, HUD, Player and Misc");
        }
        this.category = category;
        this.parent = parent;
        this.modules = ModulePage.orange().modules().in(category).stream().map(x$0 -> new ModuleRow((ClientModule)x$0)).toList();
        this.general = new ExpandableSection("general", () -> ModulePage.config().expanded("general", true), value -> {
            ModulePage.config().setExpanded("general", (boolean)value);
            this.save();
        });
        this.reducedMotion = this.toggle(() -> ModulePage.config().reducedMotion, value -> {
            ModulePage.config().reducedMotion = value;
        });
        this.blur = this.toggle(() -> ModulePage.config().blur, value -> {
            ModulePage.config().blur = value;
        });
        this.globalScale = this.slider(0.5, 2.0, () -> ModulePage.config().hudScale, value -> {
            ModulePage.config().hudScale = (float)value;
        });
        this.opacity = this.slider(0.25, 0.95, () -> ModulePage.config().uiOpacity, value -> {
            ModulePage.config().uiOpacity = (float)value;
        });
        this.guiColour = new ColorPickerComponent(() -> ModulePage.config().guiAccent, value -> {
            ModulePage.config().guiAccent = value;
            this.changed();
        });
        this.hudColour = new ColorPickerComponent(() -> ModulePage.config().accent, value -> {
            ModulePage.config().accent = value;
            this.changed();
        });
    }

    private static OrangeClient orange() {
        return OrangeClient.instance();
    }

    private static ClientConfig config() {
        return ModulePage.orange().config();
    }

    private void changed() {
        this.dirty = true;
        this.changedAt = System.nanoTime();
    }

    private void save() {
        ModulePage.orange().save();
        this.dirty = false;
    }

    private ToggleComponent toggle(BooleanSupplier value, Consumer<Boolean> change) {
        return new ToggleComponent(value, next -> {
            change.accept((Boolean)next);
            this.changed();
        });
    }

    private SliderComponent slider(double min, double max, DoubleSupplier value, DoubleConsumer change) {
        SliderComponent control = new SliderComponent(min, max, value, next -> {
            change.accept(next);
            this.changed();
        });
        this.sliders.add(control);
        return control;
    }

    @Override
    public void render(class_332 c, Rect viewport, GuiFrame frame, PopupManager popups) {
        this.viewport = viewport;
        if (this.optimizerPending) {
            this.optimizerPending = false;
            popups.open(this, new OptimizerInfoPopup(viewport, popups));
        }
        if (this.dirty && System.nanoTime() - this.changedAt > 450000000L && this.sliders.stream().noneMatch(SliderComponent::dragging)) {
            this.save();
        }
        this.hits.clear();
        this.scroll = Math.clamp((long)this.scroll, 0, this.maxScroll());
        int x = viewport.x() + 2;
        int w = Math.max(0, viewport.w() - 6);
        int y = viewport.y() + 3 - this.scroll;
        c.method_44379(viewport.x(), viewport.y(), viewport.right(), viewport.bottom());
        if (this.category == Category.HUD || this.category == Category.PVP) {
            Rect tools = new Rect(x, y, w, 31);
            GuiText.small(c, this.category == Category.HUD ? "Position, size & visibility" : "Your essentials for combat", x + 10, y + 10, ModulePage.color(-5920585, frame));
            Rect edit = new Rect(tools.right() - 102, y + 1, 94, 24);
            this.button(c, edit, "HUD editor", () -> this.open(new HudEditorScreen(this.parent)), frame);
            y += 36;
            if (this.category == Category.HUD) {
                this.settingSlider(c, new Rect(x + 10, y, w - 20, 27), "Global HUD scale", this.globalScale, frame);
                y += 33;
            }
        } else if (this.category == Category.PLAYER) {
            GuiText.small(c, "Camera & player information", x + 10, y + 6, ModulePage.color(-5920585, frame));
            y += 25;
        }
        Integer focusY = null;
        for (ModuleRow module : this.modules) {
            if (module.module.id().equals(this.focusPending)) {
                focusY = y + this.scroll - viewport.y();
            }
            y = module.render(c, new Rect(x, y, w, 35), frame, popups) + 4;
        }
        if (this.category == Category.MISC) {
            y += 5;
            y = this.renderGeneral(c, new Rect(x, y, w, 29), frame, popups) + 12;
            y = this.renderTools(c, x, y, w, frame, popups);
        }
        this.contentHeight = Math.max(0, y + this.scroll - viewport.y() + 5);
        c.method_44380();
        this.drawScrollBar(c, frame);
        if (focusY != null) {
            this.scroll = Math.clamp((long)(focusY - 5), 0, this.maxScroll());
            this.focusPending = null;
        }
    }

    private int renderGeneral(class_332 c, Rect header, GuiFrame frame, PopupManager popups) {
        this.sectionHeader(c, header, "Client settings", this.general, this.generalArrow, frame);
        int fullHeight = 170;
        int shown = Math.round(this.general.height(fullHeight, frame));
        Rect full = new Rect(header.x() + 5, header.bottom() + 3, header.w() - 10, fullHeight);
        Rect visible = this.general.visibleBounds(full);
        if (shown > 0) {
            c.method_44379(visible.x(), visible.y(), visible.right(), visible.bottom());
            int y = full.y() + 2;
            this.settingToggle(c, new Rect(full.x() + 6, y, full.w() - 12, 27), "Reduce animations", this.reducedMotion, visible, frame);
            this.settingToggle(c, new Rect(full.x() + 6, y += 27, full.w() - 12, 27), "Background blur", this.blur, visible, frame);
            this.settingSlider(c, new Rect(full.x() + 6, y += 27, full.w() - 12, 27), "Glass opacity", this.opacity, visible, frame);
            this.settingSlider(c, new Rect(full.x() + 6, y += 27, full.w() - 12, 27), "Global HUD scale", this.globalScale, visible, frame);
            this.settingColour(c, new Rect(full.x() + 6, y += 27, full.w() - 12, 27), "Interface accent", this.guiColour, visible, frame, popups);
            this.settingColour(c, new Rect(full.x() + 6, y += 27, full.w() - 12, 27), "HUD & crosshair accent", this.hudColour, visible, frame, popups);
            c.method_44380();
        }
        return header.bottom() + 3 + shown;
    }

    private int renderTools(class_332 c, int x, int y, int w, GuiFrame frame, PopupManager popups) {
        GuiText.bold(c, "Client tools", x + 10, y, ModulePage.color(-789510, frame));
        y += 20;
        boolean columns = w >= 360;
        int gap = 7;
        int bw = columns ? (w - 20 - gap) / 2 : w - 20;
        String[] titles = new String[]{"Resource packs", "Profiles", "Global search", "Social", "Optimizer", "Minecraft controls"};
        Runnable[] actions = new Runnable[]{() -> this.open(new ResourcePackScreen(this.parent)), () -> this.open(new ProfilesScreen(this.parent)), () -> this.open(new SearchScreen(this.parent)), () -> this.open(new EmptyScreen(this.parent, "social")), () -> popups.open(this, new OptimizerInfoPopup(this.viewport, popups)), this::openControls};
        for (int i = 0; i < titles.length; ++i) {
            int row = columns ? i / 2 : i;
            int column = columns ? i % 2 : 0;
            this.button(c, new Rect(x + 10 + column * (bw + gap), y + row * 30, bw, 24), titles[i], actions[i], frame);
        }
        return y + (columns ? 3 : titles.length) * 30;
    }

    private void open(class_437 screen) {
        this.leave();
        class_310.method_1551().method_1507(screen);
    }

    private void openControls() {
        class_310 mc = class_310.method_1551();
        this.open((class_437)new class_458(this.parent, mc.field_1690));
    }

    private void sectionHeader(class_332 c, Rect r, String title, ExpandableSection section, Anim motion, GuiFrame frame) {
        GuiPrimitives.panel(c, r, ModulePage.color(-15263702, frame), 7);
        GuiText.bold(c, title, r.x() + 10, r.centerY() - 5, ModulePage.color(-789510, frame));
        Rect arrow = new Rect(r.right() - 25, r.y() + 3, 22, r.h() - 6);
        ModulePage.arrow(c, arrow, motion.to(section.expanded() ? 1.0f : 0.0f, frame.dt(), frame.reduced()), frame);
        this.hit(r, (mx, my, b) -> {
            if (b != 0) {
                return false;
            }
            section.toggle();
            return true;
        });
    }

    private void settingToggle(class_332 c, Rect r, String label, ToggleComponent toggle, Rect gate, GuiFrame frame) {
        GuiText.text(c, GuiText.trim(label, r.w() - 40), r.x(), r.centerY() - 5, ModulePage.color(-789510, frame));
        Rect control = new Rect(r.right() - 36, r.centerY() - 9, 34, 18);
        toggle.draw(c, control, frame);
        this.hit(ModulePage.intersection(control, gate), (mx, my, b) -> toggle.mouseClicked(mx, my, b));
        ModulePage.divider(c, r, frame);
    }

    private void settingSlider(class_332 c, Rect r, String label, SliderComponent slider, GuiFrame frame) {
        this.settingSlider(c, r, label, slider, this.viewport, frame);
    }

    private void settingSlider(class_332 c, Rect r, String label, SliderComponent slider, Rect gate, GuiFrame frame) {
        int reserved = Math.max(86, Math.min(142, r.w() / 2));
        GuiText.text(c, GuiText.trim(label, r.w() - reserved - 5), r.x(), r.centerY() - 5, ModulePage.color(-789510, frame));
        Rect track = new Rect(r.right() - reserved, r.centerY() - 6, reserved - 35, 12);
        slider.draw(c, track, frame);
        String value = slider.formatted();
        GuiText.small(c, value, r.right() - GuiText.widthSized(value, 8.5f), r.centerY() - 4, ModulePage.color(-5920585, frame));
        this.hit(ModulePage.intersection(track, gate), (mx, my, b) -> slider.mouseClicked(mx, my, b));
        ModulePage.divider(c, r, frame);
    }

    private void settingColour(class_332 c, Rect r, String label, ColorPickerComponent picker, Rect gate, GuiFrame frame, PopupManager popups) {
        GuiText.text(c, GuiText.trim(label, r.w() - 35), r.x(), r.centerY() - 5, ModulePage.color(-789510, frame));
        Rect control = new Rect(r.right() - 18, r.centerY() - 8, 16, 16);
        picker.drawPreview(c, control, frame, popups);
        this.hit(ModulePage.intersection(control, gate), picker::mouseClicked);
        ModulePage.divider(c, r, frame);
    }

    private void button(class_332 c, Rect r, String label, Runnable action, GuiFrame frame) {
        this.button(c, r, label, action, this.viewport, frame);
    }

    private void button(class_332 c, Rect r, String label, Runnable action, Rect gate, GuiFrame frame) {
        boolean hover = r.contains(frame.mouseX(), frame.mouseY()) && ModulePage.intersection(this.viewport, gate).contains(frame.mouseX(), frame.mouseY());
        GuiPrimitives.borderedPanel(c, r, ModulePage.color(hover ? -14671559 : -15658207, frame), ModulePage.color(-14079172, frame), 5);
        String title = GuiText.trim(label, r.w() - 12, 8.5f);
        GuiText.small(c, title, r.centerX() - GuiText.widthSized(title, 8.5f) / 2, r.centerY() - 4, ModulePage.color(hover ? -789510 : -5920585, frame));
        this.hit(ModulePage.intersection(r, gate), (mx, my, b) -> {
            if (b != 0) {
                return false;
            }
            action.run();
            return true;
        });
    }

    private static void arrow(class_332 c, Rect r, float expanded, GuiFrame frame) {
        int x = r.centerX();
        int y = r.centerY();
        int color = ModulePage.color(-5920585, frame);
        double angle = (double)expanded * Math.PI / 2.0;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        int x1 = x + (int)Math.round(-cos + 2.0 * sin);
        int y1 = y + (int)Math.round(-sin - 2.0 * cos);
        int x2 = x + (int)Math.round(cos);
        int y2 = y + (int)Math.round(sin);
        int x3 = x + (int)Math.round(-cos - 2.0 * sin);
        int y3 = y + (int)Math.round(-sin + 2.0 * cos);
        GuiPrimitives.line(c, x1, y1, x2, y2, color);
        GuiPrimitives.line(c, x2, y2, x3, y3, color);
    }

    private static void divider(class_332 c, Rect r, GuiFrame frame) {
        c.method_25294(r.x(), r.bottom() - 1, r.right(), r.bottom(), GuiTheme.opacity(-14079172, frame.alpha() * 0.4f));
    }

    private static int color(int color, GuiFrame frame) {
        return GuiTheme.opacity(color, frame.alpha());
    }

    private static Rect intersection(Rect a, Rect b) {
        int x = Math.max(a.x(), b.x());
        int y = Math.max(a.y(), b.y());
        return new Rect(x, y, Math.max(0, Math.min(a.right(), b.right()) - x), Math.max(0, Math.min(a.bottom(), b.bottom()) - y));
    }

    private void hit(Rect bounds, ClickAction action) {
        Rect visible = ModulePage.intersection(bounds, this.viewport);
        if (visible.w() > 0 && visible.h() > 0) {
            this.hits.add(new Hit(visible, action));
        }
    }

    private void drawScrollBar(class_332 c, GuiFrame frame) {
        if (this.contentHeight <= this.viewport.h() || this.viewport.h() <= 0) {
            return;
        }
        int thumb = Math.max(12, this.viewport.h() * this.viewport.h() / this.contentHeight);
        int y = this.viewport.y() + this.scroll * (this.viewport.h() - thumb) / Math.max(1, this.maxScroll());
        GuiPrimitives.rounded(c, this.viewport.right() - 2, y, 2, thumb, 1, GuiTheme.opacity(-5920585, frame.alpha() * 0.45f));
    }

    private int maxScroll() {
        return Math.max(0, this.contentHeight - this.viewport.h());
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (!this.viewport.contains(x, y)) {
            return false;
        }
        for (Hit hit : this.hits) {
            if (!hit.bounds.contains(x, y) || !hit.action.click(x, y, button)) continue;
            if (this.dirty && this.sliders.stream().noneMatch(SliderComponent::dragging)) {
                this.save();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        for (SliderComponent slider : this.sliders) {
            if (!slider.mouseDragged(x, y, button)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        boolean handled = false;
        for (SliderComponent slider : this.sliders) {
            handled |= slider.mouseReleased(x, y, button);
        }
        if (this.dirty) {
            this.save();
        }
        return handled;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (!this.viewport.contains(x, y)) {
            return false;
        }
        this.scroll = Math.clamp((long)(this.scroll - (int)(vertical * 28.0)), 0, this.maxScroll());
        return true;
    }

    @Override
    public void focusModule(String id) {
        if (this.modules.stream().noneMatch(row -> row.module.id().equals(id))) {
            return;
        }
        ModulePage.config().setExpanded("module_" + id, true);
        this.focusPending = this.highlighted = id;
        this.highlightUntil = System.nanoTime() + 2000000000L;
        this.save();
    }

    public void focusSettings() {
        if (this.category != Category.MISC) {
            return;
        }
        ModulePage.config().setExpanded("general", true);
        this.scroll = 0;
        this.save();
    }

    public void focusOptimizer() {
        if (this.category == Category.MISC) {
            this.optimizerPending = true;
        }
    }

    @Override
    public void leave() {
        for (SliderComponent slider : this.sliders) {
            slider.mouseReleased();
        }
        if (this.dirty) {
            this.save();
        }
    }

    private final class ModuleRow {
        private final ClientModule module;
        private final ToggleComponent enabled;
        private final ExpandableSection expanded;
        private final SliderComponent scale;
        private final SliderComponent opacity;
        private final ColorPickerComponent textColour;
        private final Anim arrowMotion = new Anim();
        private final Anim hoverMotion = new Anim();
        private final boolean hasSettings;

        private ModuleRow(ClientModule module) {
            this.module = module;
            this.hasSettings = module.hud() || module.id().equals("freelook");
            this.enabled = new ToggleComponent(module::enabled, value -> {
                module.setEnabled((boolean)value);
                ModulePage.this.save();
            });
            String key = "module_" + module.id();
            this.expanded = new ExpandableSection(key, () -> ModulePage.config().expanded(key, false), value -> {
                ModulePage.config().setExpanded(key, (boolean)value);
                ModulePage.this.save();
            });
            this.scale = ModulePage.this.slider(0.5, 3.0, () -> ModulePage.config().module((String)module.id()).scale, value -> {
                ModulePage.config().module((String)module.id()).scale = (float)value;
            });
            this.opacity = ModulePage.this.slider(0.1, 1.0, () -> ModulePage.config().module((String)module.id()).opacity, value -> {
                ModulePage.config().module((String)module.id()).opacity = (float)value;
            });
            this.textColour = new ColorPickerComponent(() -> ModulePage.config().module((String)module.id()).color, value -> {
                ModulePage.config().module((String)module.id()).color = value;
                ModulePage.this.changed();
            });
        }

        private int render(class_332 c, Rect header, GuiFrame frame, PopupManager popups) {
            boolean focus = this.module.id().equals(ModulePage.this.highlighted) && System.nanoTime() < ModulePage.this.highlightUntil;
            boolean hover = header.contains(frame.mouseX(), frame.mouseY()) && ModulePage.this.viewport.contains(frame.mouseX(), frame.mouseY());
            float hovered = this.hoverMotion.to(hover || focus ? 1.0f : 0.0f, frame.dt(), frame.reduced());
            if (hovered > 0.001f) {
                GuiPrimitives.panel(c, header, GuiTheme.opacity(focus ? GuiTheme.current().hover() : GuiTheme.current().secondary(), frame.alpha() * hovered), 7);
            }
            GuiText.bold(c, this.module.name(), header.x() + 10, header.centerY() - 5, ModulePage.color(-789510, frame));
            Rect toggle = new Rect(header.right() - 56, header.centerY() - 8, 30, 16);
            this.enabled.draw(c, toggle, frame);
            ModulePage.this.hit(toggle, (mx, my, b) -> this.enabled.mouseClicked(mx, my, b));
            if (!this.hasSettings) {
                return header.bottom();
            }
            Rect arrow = new Rect(header.right() - 22, header.y() + 4, 19, header.h() - 8);
            ModulePage.arrow(c, arrow, this.arrowMotion.to(this.expanded.expanded() ? 1.0f : 0.0f, frame.dt(), frame.reduced()), frame);
            ModulePage.this.hit(arrow, (mx, my, b) -> {
                if (b != 0) {
                    return false;
                }
                this.expanded.toggle();
                return true;
            });
            int fullHeight = this.module.hud() ? 122 : 66;
            int shown = Math.round(this.expanded.height(fullHeight, frame));
            Rect full = new Rect(header.x() + 5, header.bottom(), header.w() - 10, fullHeight);
            Rect visible = this.expanded.visibleBounds(full);
            if (shown > 0) {
                c.method_44379(visible.x(), visible.y(), visible.right(), visible.bottom());
                int y = full.y() + 2;
                if (this.module.hud()) {
                    ModulePage.this.settingSlider(c, new Rect(full.x() + 6, y, full.w() - 12, 27), "Element scale", this.scale, visible, frame);
                    ModulePage.this.settingSlider(c, new Rect(full.x() + 6, y += 27, full.w() - 12, 27), "Opacity", this.opacity, visible, frame);
                    ModulePage.this.settingColour(c, new Rect(full.x() + 6, y += 27, full.w() - 12, 27), "Text colour", this.textColour, visible, frame, popups);
                    ClientConfig.ModuleState state = ModulePage.config().module(this.module.id());
                    GuiText.small(c, "X: " + state.x + "   Y: " + state.y, full.x() + 6, (y += 27) + 8, ModulePage.color(-5920585, frame));
                    Rect edit = new Rect(full.right() - 110, y + 1, 104, 23);
                    ModulePage.this.button(c, edit, "Edit position", () -> ModulePage.this.open(new HudEditorScreen(ModulePage.this.parent, this.module.id())), visible, frame);
                } else {
                    GuiText.small(c, GuiText.trim(this.module.description(), full.w() - 12, 8.5f), full.x() + 6, y + 5, ModulePage.color(-5920585, frame));
                    GuiText.small(c, "Set your hold key in Minecraft controls.", full.x() + 6, y + 21, ModulePage.color(-5920585, frame));
                    ModulePage.this.button(c, new Rect(full.x() + 6, y + 36, Math.min(135, full.w() - 12), 23), "Configure keybind", ModulePage.this::openControls, visible, frame);
                }
                c.method_44380();
            }
            return header.bottom() + shown;
        }
    }

    @FunctionalInterface
    private static interface ClickAction {
        public boolean click(double var1, double var3, int var5);
    }

    private record Hit(Rect bounds, ClickAction action) {
    }
}

