/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package de.orange.client.gui.premium;

import de.orange.client.OrangeClient;
import de.orange.client.config.ClientConfig;
import de.orange.client.config.RenderSettings;
import de.orange.client.gui.premium.GuiPage;
import de.orange.client.ui.Anim;
import de.orange.client.ui.Rect;
import de.orange.client.ui.premium.GuiFrame;
import de.orange.client.ui.premium.GuiPrimitives;
import de.orange.client.ui.premium.GuiText;
import de.orange.client.ui.premium.GuiTheme;
import de.orange.client.ui.premium.components.ColorPickerComponent;
import de.orange.client.ui.premium.components.DropdownComponent;
import de.orange.client.ui.premium.components.ExpandableSection;
import de.orange.client.ui.premium.components.PopupManager;
import de.orange.client.ui.premium.components.SliderComponent;
import de.orange.client.ui.premium.components.ToggleComponent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.class_332;

public final class VisualsPage
implements GuiPage {
    private static final int HEADER = 28;
    private static final int SETTING = 28;
    private static final int GAP = 3;
    private static final String[] RENDER_IDS = new String[]{"PLAYERS", "CRYSTALS", "HAND", "ITEMS", "COLOURS"};
    private static final String[] RENDER_LABELS = new String[]{"Players", "End crystals", "Hand", "Items", "Colours"};
    private static final Rect EMPTY = new Rect(0, 0, 0, 0);
    private final ExpandableSection renders = this.expansion("renders", true);
    private final ToggleComponent rendersToggle = new ToggleComponent(() -> VisualsPage.config().module((String)"renders").enabled, enabled -> {
        VisualsPage.config().module((String)"renders").enabled = enabled;
        this.changed();
    });
    private final Anim rendersArrow = new Anim();
    private final Anim subUnderlineX = new Anim();
    private final Anim subUnderlineWidth = new Anim();
    private final TargetControls players = new TargetControls(this, () -> VisualsPage.config().renders.players);
    private final TargetControls crystals = new TargetControls(this, () -> VisualsPage.config().renders.crystals);
    private final TargetControls items = new TargetControls(this, () -> VisualsPage.config().renders.items);
    private final List<SettingRow> colours = List.of(new ColourRow("Primary colour", () -> VisualsPage.config().renders.primary, value -> VisualsPage.config().renders.applyPrimaryToTargets(value)), new ColourRow("Secondary colour", () -> VisualsPage.config().renders.secondary, value -> VisualsPage.config().renders.applySecondaryToTargets(value)), new ColourRow("Accent colour", () -> VisualsPage.config().guiAccent, value -> {
        VisualsPage.config().guiAccent = value;
    }), new SliderRow("Gradient motion", 0.0, 2.0, () -> VisualsPage.config().renders.gradientSpeed, value -> {
        VisualsPage.config().renders.gradientSpeed = (float)value;
    }, VisualsPage::speed), new SliderRow("Gradient spread", 0.0, 64.0, () -> VisualsPage.config().renders.gradientSpread, value -> {
        VisualsPage.config().renders.gradientSpread = (float)value;
    }, value -> String.format(Locale.ROOT, "%.0f m", value)), new SliderRow("Rainbow speed", 0.05, 2.0, () -> VisualsPage.config().renders.rainbowSpeed, value -> {
        VisualsPage.config().renders.rainbowSpeed = (float)value;
    }, VisualsPage::speed));
    private final List<ModuleRow> modules = List.of(new ModuleRow("nametags", "Nametags", List.of(new SliderRow("Text size", 0.5, 3.0, () -> VisualsPage.config().visuals.nametagScale, value -> {
        VisualsPage.config().visuals.nametagScale = (float)value;
    }, VisualsPage::percentage), new ColourRow("Text colour", () -> VisualsPage.config().visuals.nametagText, value -> {
        VisualsPage.config().visuals.nametagText = value;
    }), new ToggleRow("Background", () -> VisualsPage.config().visuals.nametagBackground, value -> {
        VisualsPage.config().visuals.nametagBackground = value;
    }), new ColourRow("Background colour", () -> VisualsPage.config().visuals.nametagBackgroundColor, value -> {
        VisualsPage.config().visuals.nametagBackgroundColor = value;
    }, true))), new ModuleRow("hitboxes", "Hitboxes", List.of(new ColourRow("Line colour", () -> VisualsPage.config().visuals.hitboxColor, value -> {
        VisualsPage.config().visuals.hitboxColor = value;
    }), new SliderRow("Line thickness", 0.5, 4.0, () -> VisualsPage.config().visuals.hitboxWidth, value -> {
        VisualsPage.config().visuals.hitboxWidth = (float)value;
    }, value -> String.format(Locale.ROOT, "%.1f px", value)))), new ModuleRow("fullbright", "Fullbright", List.of(new SliderRow("Brightness", 0.0, 1.0, () -> VisualsPage.config().visuals.fullbrightStrength, value -> {
        VisualsPage.config().visuals.fullbrightStrength = (float)value;
    }, VisualsPage::percentage))), new ModuleRow("ambience", "Ambience", List.of(new ColourRow("Sky colour", () -> VisualsPage.config().visuals.ambienceColor, value -> {
        VisualsPage.config().visuals.ambienceColor = value;
    }), new SliderRow("Tint strength", 0.0, 1.0, () -> VisualsPage.config().visuals.ambienceStrength, value -> {
        VisualsPage.config().visuals.ambienceStrength = (float)value;
    }, VisualsPage::percentage))), new ModuleRow("crosshair", "Crosshair", List.of(new ColourRow("Crosshair colour", () -> VisualsPage.config().accent, value -> {
        VisualsPage.config().accent = value;
    }))));
    private final List<SettingRow> visibleSettings = new ArrayList<SettingRow>();
    private final List<SliderRow> allSliders = new ArrayList<SliderRow>();
    private final Map<String, Integer> moduleOffsets = new LinkedHashMap<String, Integer>();
    private final Rect[] renderTabs = new Rect[RENDER_IDS.length];
    private Rect viewport = EMPTY;
    private Rect rendersHeader = EMPTY;
    private Rect rendersBody = EMPTY;
    private Rect rendersArrowBounds = EMPTY;
    private int scroll;
    private int contentHeight;
    private boolean dirty;
    private boolean underlineInitialized;
    private long lastSave = System.nanoTime();
    private long highlightUntil;
    private String pendingFocus;
    private String highlighted;

    public VisualsPage() {
        for (int i = 0; i < this.renderTabs.length; ++i) {
            this.renderTabs[i] = EMPTY;
        }
        this.collectSliders(this.players.all());
        this.collectSliders(this.crystals.all());
        this.collectSliders(this.items.all());
        this.collectSliders(this.colours);
        this.modules.forEach(module -> this.collectSliders(module.settings));
    }

    private static ClientConfig config() {
        return OrangeClient.instance().config();
    }

    private static String percentage(double value) {
        return Math.round(value * 100.0) + "%";
    }

    private static String speed(double value) {
        return String.format(Locale.ROOT, "%.2f\u00d7", value);
    }

    private void collectSliders(List<SettingRow> rows) {
        for (SettingRow row : rows) {
            if (!(row instanceof SliderRow)) continue;
            SliderRow slider = (SliderRow)row;
            this.allSliders.add(slider);
        }
    }

    private ExpandableSection expansion(String id, boolean fallback) {
        return new ExpandableSection(id, () -> VisualsPage.config().expanded(id, fallback), value -> {
            VisualsPage.config().setExpanded(id, (boolean)value);
            this.changed();
        });
    }

    private void changed() {
        this.dirty = true;
    }

    private void save() {
        if (this.dirty) {
            OrangeClient.instance().save();
            this.dirty = false;
            this.lastSave = System.nanoTime();
        }
    }

    @Override
    public void render(class_332 context, Rect viewport, GuiFrame frame, PopupManager popups) {
        this.viewport = viewport;
        this.scroll = Math.clamp((long)this.scroll, 0, Math.max(0, this.contentHeight - viewport.h()));
        this.visibleSettings.clear();
        this.moduleOffsets.clear();
        GuiTheme theme = GuiTheme.current();
        int x = viewport.x() + 1;
        int y = viewport.y() + 1 - this.scroll;
        int width = Math.max(0, viewport.w() - 4);
        context.method_44379(viewport.x(), viewport.y(), viewport.right(), viewport.bottom());
        this.moduleOffsets.put("renders", 0);
        this.rendersHeader = new Rect(x, y, width, 28);
        this.renderHeader(context, frame, this.rendersHeader, "Renders", this.rendersToggle, this.rendersArrow, this.renders.expanded(), true, "renders");
        this.rendersArrowBounds = this.arrowBounds(this.rendersHeader);
        boolean columns = width >= 360;
        List<SettingRow> rows = this.renderRows();
        int fullHeight = this.renderBodyHeight(rows, columns);
        int animatedHeight = Math.round(this.renders.height(fullHeight, frame));
        Rect fullBody = new Rect(x + 2, y += 31, Math.max(0, width - 4), fullHeight);
        this.rendersBody = this.renders.visibleBounds(fullBody);
        if (animatedHeight > 0) {
            context.method_44379(this.rendersBody.x(), this.rendersBody.y(), this.rendersBody.right(), this.rendersBody.bottom());
            GuiPrimitives.borderedPanel(context, fullBody, VisualsPage.colour(theme.secondary(), frame.alpha() * 0.72f), VisualsPage.colour(GuiTheme.mix(theme.border(), theme.accent(), 0.24f), frame.alpha()), 8);
            this.drawRenderTabs(context, fullBody, frame);
            if (this.renderPage().equals("HAND")) {
                GuiText.text(context, "Hand rendering is not available.", fullBody.x() + 12, fullBody.y() + 39, VisualsPage.colour(theme.muted(), frame.alpha()));
                GuiText.small(context, "No compatible hand module is installed.", fullBody.x() + 12, fullBody.y() + 55, VisualsPage.colour(theme.disabled(), frame.alpha()));
            } else {
                this.drawRenderRows(context, fullBody, rows, columns, frame, popups);
            }
            context.method_44380();
        }
        y += animatedHeight + 7;
        for (ModuleRow module : this.modules) {
            this.moduleOffsets.put(module.id, y + this.scroll - viewport.y());
            y = module.render(context, new Rect(x + 2, y, Math.max(0, width - 4), 28), frame, popups);
        }
        this.contentHeight = Math.max(0, y + this.scroll - viewport.y() + 3);
        context.method_44380();
        this.drawScrollbar(context, frame);
        if (this.pendingFocus != null) {
            Integer offset = this.moduleOffsets.get(this.pendingFocus);
            if (offset != null) {
                this.scroll = Math.clamp((long)offset.intValue(), 0, Math.max(0, this.contentHeight - viewport.h()));
            }
            this.pendingFocus = null;
        }
        if (this.dirty && System.nanoTime() - this.lastSave > 500000000L && this.allSliders.stream().noneMatch(row -> row.slider.dragging())) {
            this.save();
        }
    }

    private static int colour(int value, float alpha) {
        return GuiTheme.opacity(value, alpha);
    }

    private String renderPage() {
        String value = VisualsPage.config().rendersPage;
        return value.equals("END_CRYSTALS") ? "CRYSTALS" : value;
    }

    private TargetControls activeTarget() {
        return switch (this.renderPage()) {
            case "CRYSTALS" -> this.crystals;
            case "ITEMS" -> this.items;
            default -> this.players;
        };
    }

    private List<SettingRow> renderRows() {
        return switch (this.renderPage()) {
            case "HAND" -> List.of();
            case "COLOURS" -> this.colours;
            default -> this.activeTarget().visible();
        };
    }

    private int renderBodyHeight(List<SettingRow> rows, boolean columns) {
        if (this.renderPage().equals("HAND")) {
            return 82;
        }
        if (!columns) {
            return 31 + rows.size() * 28 + 5;
        }
        if (this.renderPage().equals("COLOURS")) {
            return 120;
        }
        int rightRows = rows.size() - 2;
        return 31 + Math.max(2, rightRows) * 28 + 5;
    }

    private void drawRenderTabs(class_332 context, Rect body, GuiFrame frame) {
        GuiTheme theme = GuiTheme.current();
        int x = body.x() + 12;
        int top = body.y() + 7;
        float font = body.w() < 340 ? 8.5f : 9.5f;
        int gap = body.w() < 340 ? 12 : 15;
        int activeIndex = 0;
        for (int i = 0; i < RENDER_IDS.length; ++i) {
            int labelWidth = GuiText.widthSized(RENDER_LABELS[i], font, true);
            this.renderTabs[i] = new Rect(x - 2, body.y() + 3, labelWidth + 4, 23);
            boolean selected = this.renderPage().equals(RENDER_IDS[i]);
            if (selected) {
                activeIndex = i;
            }
            GuiText.boldSized(context, RENDER_LABELS[i], x, top + 3, font, VisualsPage.colour(selected ? theme.text() : theme.muted(), frame.alpha()));
            x += labelWidth + gap;
        }
        Rect active = this.renderTabs[activeIndex];
        if (!this.underlineInitialized) {
            this.subUnderlineX.snap(active.x() + 2);
            this.subUnderlineWidth.snap(active.w() - 4);
            this.underlineInitialized = true;
        }
        int underlineX = Math.round(this.subUnderlineX.to(active.x() + 2, frame.dt(), frame.reduced()));
        int underlineWidth = Math.round(this.subUnderlineWidth.to(active.w() - 4, frame.dt(), frame.reduced()));
        context.method_25294(body.x() + 12, body.y() + 26, body.right() - 12, body.y() + 27, VisualsPage.colour(theme.border(), frame.alpha() * 0.55f));
        GuiPrimitives.rounded(context, underlineX, body.y() + 26, underlineWidth, 1, 0, VisualsPage.colour(theme.secondaryAccent(), frame.alpha()));
    }

    private void drawRenderRows(class_332 context, Rect body, List<SettingRow> rows, boolean columns, GuiFrame frame, PopupManager popups) {
        int x = body.x() + 12;
        int y = body.y() + 29;
        int width = body.w() - 24;
        Rect clip = VisualsPage.intersection(this.viewport, this.rendersBody);
        for (int i = 0; i < rows.size(); ++i) {
            int column = 0;
            int row = i;
            if (columns) {
                if (this.renderPage().equals("COLOURS")) {
                    column = i % 2;
                    row = i / 2;
                } else if (i >= 2) {
                    column = 1;
                    row = i - 2;
                }
            }
            int columnWidth = columns ? (width - 18) / 2 : width;
            int rx = x + column * (columnWidth + 18);
            SettingRow setting = rows.get(i);
            setting.render(context, new Rect(rx, y + row * 28, columnWidth, 28), clip, frame, popups);
            if (!this.renders.expanded()) continue;
            this.visibleSettings.add(setting);
        }
    }

    private Rect arrowBounds(Rect header) {
        return new Rect(header.right() - 25, header.y() + 3, 22, header.h() - 6);
    }

    private Rect toggleBounds(Rect header) {
        return new Rect(header.right() - 57, header.centerY() - 8, 30, 16);
    }

    private void renderHeader(class_332 context, GuiFrame frame, Rect header, String title, ToggleComponent toggle, Anim arrow, boolean expanded, boolean primary, String id) {
        boolean hovered;
        GuiTheme theme = GuiTheme.current();
        boolean bl = hovered = this.viewport.contains(frame.mouseX(), frame.mouseY()) && header.contains(frame.mouseX(), frame.mouseY());
        if (primary || hovered || this.highlighted != null && this.highlighted.equals(id) && System.nanoTime() < this.highlightUntil) {
            int fill = primary ? theme.panel() : theme.hover();
            GuiPrimitives.panel(context, header, VisualsPage.colour(fill, frame.alpha() * (primary ? 1.0f : 0.45f)), 7);
        }
        GuiText.bold(context, title, header.x() + 10, header.centerY() - 5, VisualsPage.colour(theme.text(), frame.alpha()));
        toggle.draw(context, this.toggleBounds(header), frame);
        Rect a = this.arrowBounds(header);
        float progress = arrow.to(expanded ? 1.0f : 0.0f, frame.dt(), frame.reduced());
        double angle = (double)progress * Math.PI / 2.0;
        int centerX = a.centerX();
        int centerY = a.centerY();
        int arrowColour = VisualsPage.colour(a.contains(frame.mouseX(), frame.mouseY()) ? theme.text() : theme.muted(), frame.alpha());
        int ax = centerX + (int)Math.round(-Math.cos(angle) + 2.5 * Math.sin(angle));
        int ay = centerY + (int)Math.round(-Math.sin(angle) - 2.5 * Math.cos(angle));
        int bx = centerX + (int)Math.round(1.5 * Math.cos(angle));
        int by = centerY + (int)Math.round(1.5 * Math.sin(angle));
        int cx = centerX + (int)Math.round(-Math.cos(angle) - 2.5 * Math.sin(angle));
        int cy = centerY + (int)Math.round(-Math.sin(angle) + 2.5 * Math.cos(angle));
        GuiPrimitives.line(context, ax, ay, bx, by, arrowColour);
        GuiPrimitives.line(context, bx, by, cx, cy, arrowColour);
    }

    private void drawScrollbar(class_332 context, GuiFrame frame) {
        int maximum = this.contentHeight - this.viewport.h();
        if (maximum <= 0) {
            return;
        }
        int thumb = Math.max(16, this.viewport.h() * this.viewport.h() / this.contentHeight);
        int y = this.viewport.y() + this.scroll * (this.viewport.h() - thumb) / maximum;
        GuiPrimitives.rounded(context, this.viewport.right() - 2, y, 2, thumb, 1, VisualsPage.colour(GuiTheme.current().border(), frame.alpha()));
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button != 0 || !this.viewport.contains(x, y)) {
            return false;
        }
        if (this.rendersHeader.contains(x, y)) {
            if (this.rendersToggle.mouseClicked(x, y, button)) {
                this.save();
                return true;
            }
            if (this.rendersArrowBounds.contains(x, y) || x < (double)this.rendersToggle.bounds().x()) {
                this.renders.toggle();
                this.save();
                return true;
            }
        }
        if (this.renders.expanded() && this.rendersBody.contains(x, y)) {
            for (int i = 0; i < this.renderTabs.length; ++i) {
                if (!this.renderTabs[i].contains(x, y)) continue;
                VisualsPage.config().rendersPage = RENDER_IDS[i];
                this.changed();
                this.save();
                return true;
            }
        }
        for (ModuleRow module : this.modules) {
            if (!module.clickHeader(x, y, button)) continue;
            this.save();
            return true;
        }
        for (SettingRow setting : this.visibleSettings) {
            if (!setting.click(x, y, button)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (button != 0) {
            return false;
        }
        for (SliderRow row : this.allSliders) {
            if (!row.slider.mouseDragged(x, y, button)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        boolean handled = false;
        if (button == 0) {
            for (SliderRow row : this.allSliders) {
                handled |= row.slider.mouseReleased(x, y, button);
            }
        }
        this.save();
        return handled;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (!this.viewport.contains(x, y)) {
            return false;
        }
        this.scroll = Math.clamp((long)(this.scroll - (int)Math.round(vertical * 22.0)), 0, Math.max(0, this.contentHeight - this.viewport.h()));
        return true;
    }

    @Override
    public void focusModule(String id) {
        String section;
        String string = section = id == null ? "renders" : id;
        if (!section.equals("renders") && this.modules.stream().noneMatch(module -> module.id.equals(section))) {
            return;
        }
        VisualsPage.config().setExpanded(section, true);
        this.pendingFocus = this.highlighted = section;
        this.highlightUntil = System.nanoTime() + 2000000000L;
        this.changed();
    }

    @Override
    public void leave() {
        for (SliderRow row : this.allSliders) {
            row.slider.mouseReleased();
        }
        this.save();
    }

    private static Rect intersection(Rect a, Rect b) {
        int x = Math.max(a.x(), b.x());
        int y = Math.max(a.y(), b.y());
        return new Rect(x, y, Math.max(0, Math.min(a.right(), b.right()) - x), Math.max(0, Math.min(a.bottom(), b.bottom()) - y));
    }

    private final class TargetControls {
        private final Supplier<RenderSettings.Target> target;
        private final SettingRow glow;
        private final SettingRow strength;
        private final SettingRow mode;
        private final SettingRow primary;
        private final SettingRow secondary;
        private final SettingRow custom;
        private final SettingRow rainbow;

        private TargetControls(VisualsPage visualsPage, Supplier<RenderSettings.Target> target) {
            this.target = target;
            this.glow = visualsPage.new ToggleRow("Glow", () -> ((RenderSettings.Target)target.get()).glow, enabled -> {
                ((RenderSettings.Target)target.get()).glow = enabled;
            });
            this.strength = visualsPage.new SliderRow("Glow strength", 0.0, 1.0, () -> ((RenderSettings.Target)target.get()).strength, value -> {
                ((RenderSettings.Target)target.get()).strength = (float)value;
            }, VisualsPage::percentage);
            this.mode = visualsPage.new ModeRow(target);
            this.primary = visualsPage.new ColourRow("Primary colour", () -> ((RenderSettings.Target)target.get()).primary, value -> {
                ((RenderSettings.Target)target.get()).primary = value;
            });
            this.secondary = visualsPage.new ColourRow("Secondary colour", () -> ((RenderSettings.Target)target.get()).secondary, value -> {
                ((RenderSettings.Target)target.get()).secondary = value;
            });
            this.custom = visualsPage.new ColourRow("Custom colour", () -> ((RenderSettings.Target)target.get()).custom, value -> {
                ((RenderSettings.Target)target.get()).custom = value;
            });
            this.rainbow = visualsPage.new SliderRow("Rainbow speed", 0.05, 2.0, () -> VisualsPage.config().renders.rainbowSpeed, value -> {
                VisualsPage.config().renders.rainbowSpeed = (float)value;
            }, VisualsPage::speed);
        }

        private List<SettingRow> visible() {
            return switch (this.target.get().mode) {
                default -> throw new MatchException(null, null);
                case RenderSettings.ColourMode.SOLID -> List.of(this.glow, this.strength, this.mode, this.primary);
                case RenderSettings.ColourMode.GRADIENT -> List.of(this.glow, this.strength, this.mode, this.primary, this.secondary);
                case RenderSettings.ColourMode.CUSTOM -> List.of(this.glow, this.strength, this.mode, this.custom);
                case RenderSettings.ColourMode.RAINBOW -> List.of(this.glow, this.strength, this.mode, this.rainbow);
            };
        }

        private List<SettingRow> all() {
            return List.of(this.glow, this.strength, this.mode, this.primary, this.secondary, this.custom, this.rainbow);
        }
    }

    private final class ColourRow
    extends SettingRow {
        final ColorPickerComponent picker;

        ColourRow(String label, IntSupplier getter, IntConsumer setter) {
            this(label, getter, setter, false);
        }

        ColourRow(String label, IntSupplier getter, IntConsumer setter, boolean alpha) {
            super(VisualsPage.this, label);
            this.picker = new ColorPickerComponent(getter, value -> {
                setter.accept(value);
                VisualsPage.this.changed();
            }, alpha);
        }

        @Override
        void draw(class_332 context, GuiFrame frame, PopupManager popups) {
            this.label(context, frame, this.bounds.w() - 26);
            this.picker.drawPreview(context, new Rect(this.bounds.right() - 16, this.bounds.centerY() - 8, 16, 16), frame, popups);
        }

        @Override
        boolean clickControl(double x, double y, int button) {
            return this.picker.mouseClicked(x, y, button);
        }
    }

    private final class SliderRow
    extends SettingRow {
        final SliderComponent slider;

        SliderRow(String label, double min, double max, DoubleSupplier getter, DoubleConsumer setter, DoubleFunction<String> format) {
            super(VisualsPage.this, label);
            this.slider = new SliderComponent(min, max, getter, value -> {
                setter.accept(value);
                VisualsPage.this.changed();
            }).format(format);
        }

        @Override
        void draw(class_332 context, GuiFrame frame, PopupManager popups) {
            int labelSpace = Math.min(113, Math.round((float)this.bounds.w() * 0.46f));
            int valueSpace = Math.max(28, GuiText.widthSized(this.slider.formatted(), 9.0f));
            this.label(context, frame, labelSpace - 4);
            int trackX = this.bounds.x() + labelSpace + 1;
            int trackWidth = Math.max(16, this.bounds.right() - valueSpace - 8 - trackX);
            this.slider.draw(context, new Rect(trackX, this.bounds.centerY() - 6, trackWidth, 12), frame);
            GuiText.drawSized(context, this.slider.formatted(), this.bounds.right() - valueSpace, this.bounds.centerY() - 5, 9.0f, VisualsPage.colour(GuiTheme.current().muted(), frame.alpha()));
        }

        @Override
        boolean clickControl(double x, double y, int button) {
            return this.slider.mouseClicked(x, y, button);
        }
    }

    private final class ModuleRow {
        private final String id;
        private final String title;
        private final List<SettingRow> settings;
        private final ExpandableSection expansion;
        private final ToggleComponent toggle;
        private final Anim arrow = new Anim();
        private Rect header = EMPTY;
        private Rect body = EMPTY;

        private ModuleRow(String id, String title, List<SettingRow> settings) {
            this.id = id;
            this.title = title;
            this.settings = settings;
            this.expansion = VisualsPage.this.expansion(id, false);
            this.toggle = new ToggleComponent(() -> VisualsPage.config().module((String)id).enabled, enabled -> {
                VisualsPage.config().module((String)id).enabled = enabled;
                VisualsPage.this.changed();
            });
        }

        private int render(class_332 context, Rect header, GuiFrame frame, PopupManager popups) {
            this.header = header;
            VisualsPage.this.renderHeader(context, frame, header, this.title, this.toggle, this.arrow, this.expansion.expanded(), false, this.id);
            int height = Math.round(this.expansion.height(this.settings.size() * 28 + 8, frame));
            Rect full = new Rect(header.x() + 4, header.bottom(), header.w() - 8, this.settings.size() * 28 + 8);
            this.body = this.expansion.visibleBounds(full);
            if (height > 0) {
                context.method_44379(this.body.x(), this.body.y(), this.body.right(), this.body.bottom());
                GuiPrimitives.panel(context, full, VisualsPage.colour(GuiTheme.current().secondary(), frame.alpha() * 0.68f), 6);
                Rect clip = VisualsPage.intersection(VisualsPage.this.viewport, this.body);
                for (int i = 0; i < this.settings.size(); ++i) {
                    SettingRow row = this.settings.get(i);
                    row.render(context, new Rect(full.x() + 10, full.y() + 4 + i * 28, full.w() - 20, 28), clip, frame, popups);
                    if (!this.expansion.expanded()) continue;
                    VisualsPage.this.visibleSettings.add(row);
                }
                context.method_44380();
            }
            return header.bottom() + height + 3;
        }

        private boolean clickHeader(double x, double y, int button) {
            if (!this.header.contains(x, y)) {
                return false;
            }
            if (this.toggle.mouseClicked(x, y, button)) {
                return true;
            }
            if (VisualsPage.this.arrowBounds(this.header).contains(x, y) || x < (double)this.toggle.bounds().x()) {
                this.expansion.toggle();
                return true;
            }
            return false;
        }
    }

    private final class ToggleRow
    extends SettingRow {
        final ToggleComponent toggle;

        ToggleRow(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
            super(VisualsPage.this, label);
            this.toggle = new ToggleComponent(getter, enabled -> {
                setter.accept((Boolean)enabled);
                VisualsPage.this.changed();
            });
        }

        @Override
        void draw(class_332 context, GuiFrame frame, PopupManager popups) {
            this.label(context, frame, this.bounds.w() - 38);
            this.toggle.draw(context, new Rect(this.bounds.right() - 30, this.bounds.centerY() - 8, 30, 16), frame);
        }

        @Override
        boolean clickControl(double x, double y, int button) {
            boolean handled = this.toggle.mouseClicked(x, y, button);
            if (handled) {
                VisualsPage.this.save();
            }
            return handled;
        }
    }

    private abstract class SettingRow {
        final String label;
        Rect bounds = EMPTY;
        Rect clip = EMPTY;

        SettingRow(VisualsPage visualsPage, String label) {
            this.label = label;
        }

        final void render(class_332 context, Rect bounds, Rect clip, GuiFrame frame, PopupManager popups) {
            this.bounds = bounds;
            this.clip = VisualsPage.intersection(bounds, clip);
            if (this.clip.h() <= 0 || this.clip.w() <= 0) {
                return;
            }
            this.draw(context, frame, popups);
            context.method_25294(bounds.x(), bounds.bottom() - 1, bounds.right(), bounds.bottom(), VisualsPage.colour(GuiTheme.current().border(), frame.alpha() * 0.42f));
        }

        abstract void draw(class_332 var1, GuiFrame var2, PopupManager var3);

        abstract boolean clickControl(double var1, double var3, int var5);

        final boolean click(double x, double y, int button) {
            return this.clip.contains(x, y) && this.clickControl(x, y, button);
        }

        final void label(class_332 context, GuiFrame frame, int space) {
            GuiText.text(context, GuiText.trim(this.label, space), this.bounds.x(), this.bounds.centerY() - 5, VisualsPage.colour(GuiTheme.current().text(), frame.alpha()));
        }
    }

    private final class ModeRow
    extends SettingRow {
        final DropdownComponent<RenderSettings.ColourMode> dropdown;

        ModeRow(Supplier<RenderSettings.Target> target) {
            super(VisualsPage.this, "Colour mode");
            this.dropdown = new DropdownComponent<RenderSettings.ColourMode>(List.of(RenderSettings.ColourMode.values()), () -> ((RenderSettings.Target)target.get()).mode, value -> {
                ((RenderSettings.Target)target.get()).mode = value;
                VisualsPage.this.changed();
                VisualsPage.this.save();
            }, value -> switch (value) {
                default -> throw new MatchException(null, null);
                case RenderSettings.ColourMode.SOLID -> "Solid";
                case RenderSettings.ColourMode.GRADIENT -> "Gradient";
                case RenderSettings.ColourMode.RAINBOW -> "Rainbow";
                case RenderSettings.ColourMode.CUSTOM -> "Custom";
            });
        }

        @Override
        void draw(class_332 context, GuiFrame frame, PopupManager popups) {
            int controlWidth = Math.min(105, Math.max(82, Math.round((float)this.bounds.w() * 0.57f)));
            this.label(context, frame, this.bounds.w() - controlWidth - 6);
            this.dropdown.draw(context, new Rect(this.bounds.right() - controlWidth, this.bounds.centerY() - 10, controlWidth, 20), frame, popups);
        }

        @Override
        boolean clickControl(double x, double y, int button) {
            return this.dropdown.mouseClicked(x, y, button);
        }
    }
}

