/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  org.joml.Matrix3x2fStack
 */
package de.orange.client.gui.premium;

import de.orange.client.OrangeClient;
import de.orange.client.gui.EmptyScreen;
import de.orange.client.gui.ProfilesScreen;
import de.orange.client.gui.premium.GuiPage;
import de.orange.client.gui.premium.ModulePage;
import de.orange.client.gui.premium.QuickSearchPage;
import de.orange.client.gui.premium.VisualsPage;
import de.orange.client.input.GuiAccess;
import de.orange.client.module.Category;
import de.orange.client.module.ClientModule;
import de.orange.client.ui.Anim;
import de.orange.client.ui.GlassScreen;
import de.orange.client.ui.Rect;
import de.orange.client.ui.premium.GuiFrame;
import de.orange.client.ui.premium.GuiLayout;
import de.orange.client.ui.premium.GuiPrimitives;
import de.orange.client.ui.premium.GuiText;
import de.orange.client.ui.premium.GuiTheme;
import de.orange.client.ui.premium.components.ColorPickerComponent;
import de.orange.client.ui.premium.components.PopupManager;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.class_10799;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.joml.Matrix3x2fStack;

public final class ClickGuiScreen
extends GlassScreen {
    public static final Category[] TABS = new Category[]{Category.PVP, Category.VISUALS, Category.HUD, Category.PLAYER, Category.MISC};
    private static final String[] HEADER_ICONS = new String[]{"search", "theme", "social", "wardrobe", "layers"};
    private static final String[] HEADER_HINTS = new String[]{"Search", "Theme colour", "Profiles", "Cosmetics", "Client settings"};
    private final Map<Category, GuiPage> pages = new EnumMap<Category, GuiPage>(Category.class);
    private final PopupManager popups = new PopupManager();
    private final Anim underlineX = new Anim();
    private final Anim underlineWidth = new Anim();
    private final Anim[] headerHover = new Anim[]{new Anim(), new Anim(), new Anim(), new Anim(), new Anim()};
    private final ColorPickerComponent themePicker = new ColorPickerComponent(() -> OrangeClient.instance().config().guiAccent, value -> {
        OrangeClient.instance().config().guiAccent = value;
    });
    private GuiLayout layout;
    private Category selected;
    private QuickSearchPage search;
    private boolean searching;
    private boolean closing;
    private boolean initialized;
    private long opened;
    private long lastFrame;
    private long closingAt;
    private long changedAt;
    private long hoveredAt;
    private int hoveredIcon = -1;
    private float dt;
    private float visibility = 1.0f;
    private float scale = 1.0f;
    private float closeFrom = 1.0f;

    public ClickGuiScreen() {
        super((class_2561)class_2561.method_43470((String)"Orange Client"));
    }

    protected void method_25426() {
        this.layout = GuiLayout.compute(this.field_22789, this.field_22790);
        if (!this.initialized) {
            this.pages.put(Category.VISUALS, new VisualsPage());
            for (Category category : TABS) {
                if (category == Category.VISUALS) continue;
                this.pages.put(category, new ModulePage(category, this));
            }
            this.search = new QuickSearchPage(this, this::focusModule, this::select);
            this.initialized = true;
        }
        this.selected = Category.valueOf(OrangeClient.instance().config().clickGuiCategory);
        this.lastFrame = this.changedAt = System.nanoTime();
        if (!this.closing) {
            this.opened = this.lastFrame;
        }
        Rect tab = this.layout.tab(this.selectedIndex());
        this.underlineX.snap(tab.x() + 7);
        this.underlineWidth.snap(tab.w() - 14);
        if (this.searching) {
            this.search.activate();
        }
    }

    private int selectedIndex() {
        for (int i = 0; i < TABS.length; ++i) {
            if (TABS[i] != this.selected) continue;
            return i;
        }
        return 1;
    }

    private GuiPage page() {
        return this.searching ? this.search : this.pages.get((Object)this.selected);
    }

    public void select(Category value) {
        if (value == Category.OPTIMIZER) {
            this.select(Category.MISC);
            ((ModulePage)this.pages.get((Object)Category.MISC)).focusOptimizer();
            return;
        }
        if (this.page() != null) {
            this.page().leave();
        }
        this.popups.close();
        this.searching = false;
        this.selected = value;
        this.changedAt = System.nanoTime();
        OrangeClient.instance().config().clickGuiCategory = value.name();
        OrangeClient.instance().save();
    }

    public void focusModule(ClientModule module) {
        Category destination = Arrays.stream(TABS).filter(module.categories()::contains).findFirst().orElse(Category.HUD);
        this.select(destination);
        this.pages.get((Object)destination).focusModule(module.id());
    }

    public void focusModule(String id) {
        this.focusModule(OrangeClient.instance().modules().find(id));
    }

    public void focusSettings() {
        this.select(Category.MISC);
        GuiPage guiPage = this.pages.get((Object)Category.MISC);
        if (guiPage instanceof ModulePage) {
            ModulePage modules = (ModulePage)guiPage;
            modules.focusSettings();
        }
    }

    public void method_25393() {
        if (this.closing && (OrangeClient.instance().config().reducedMotion || System.nanoTime() - this.closingAt >= 180000000L)) {
            GuiAccess.closeToGame(this.field_22787);
        }
    }

    public void method_25394(class_332 c, int mouseX, int mouseY, float tickDelta) {
        if (!GuiAccess.worldReady(this.field_22787)) {
            return;
        }
        long now = System.nanoTime();
        this.dt = Math.clamp((float)(now - this.lastFrame) / 1.0E9f, 0.0f, 0.1f);
        this.lastFrame = now;
        boolean reduced = OrangeClient.instance().config().reducedMotion;
        this.visibility = reduced ? (float)(!this.closing ? 1 : 0) : (this.closing ? this.closeFrom * (1.0f - Anim.easeOutCubic((float)(now - this.closingAt) / 1.8E8f)) : Anim.easeOutCubic((float)(now - this.opened) / 2.0E8f));
        this.scale = 0.975f + 0.025f * this.visibility;
        double mx = this.pointerX(mouseX);
        double my = this.pointerY(mouseY);
        GuiFrame frame = new GuiFrame(mx, my, this.dt, this.visibility, reduced);
        c.method_25294(0, 0, this.field_22789, this.field_22790, GuiTheme.alpha(-16645110, 0.43f * this.visibility));
        Matrix3x2fStack matrices = c.method_51448();
        matrices.pushMatrix();
        matrices.translate((float)this.field_22789 / 2.0f, (float)this.field_22790 / 2.0f);
        matrices.scale(this.scale, this.scale);
        matrices.translate((float)(-this.field_22789) / 2.0f, (float)(-this.field_22790) / 2.0f);
        GuiPrimitives.window(c, this.layout.window(), this.visibility);
        this.drawHeader(c, frame, now);
        this.drawNavigation(c, frame);
        float pageAlpha = reduced ? 1.0f : Anim.easeOutCubic((float)(now - this.changedAt) / 1.4E8f);
        this.page().render(c, this.layout.content(), new GuiFrame(mx, my, this.dt, this.visibility * pageAlpha, reduced), this.popups);
        this.popups.render(c, frame, this.layout.window().inset(8));
        if (this.hoveredIcon >= 0 && now - this.hoveredAt > 350000000L && !this.popups.isOpen()) {
            this.drawTooltip(c, HEADER_HINTS[this.hoveredIcon], frame);
        }
        matrices.popMatrix();
        super.method_25394(c, mouseX, mouseY, tickDelta);
    }

    private void drawHeader(class_332 c, GuiFrame frame, long now) {
        GuiTheme t = GuiTheme.current();
        Rect h = this.layout.header();
        c.method_25293(class_10799.field_56883, class_2960.method_60655((String)"orange", (String)"icon.png"), h.x(), h.y() + 7, 0.0f, 0.0f, 13, 13, 128, 128, 128, 128, GuiTheme.alpha(-1, frame.alpha()));
        GuiText.boldSized(c, "Orange Client", h.x() + 19, h.y() + 7, 12.0f, GuiTheme.opacity(t.text(), frame.alpha()));
        int hovered = -1;
        for (int i = 0; i < HEADER_ICONS.length; ++i) {
            Rect r = this.layout.headerIcon(i);
            boolean over = r.contains(frame.mouseX(), frame.mouseY());
            float hover = this.headerHover[i].to(over ? 1.0f : 0.0f, frame.dt(), frame.reduced());
            if (hover > 0.001f) {
                GuiPrimitives.panel(c, r, GuiTheme.opacity(t.hover(), frame.alpha() * hover), 5);
            }
            this.drawHeaderIcon(c, i, r, GuiTheme.opacity(GuiTheme.mix(t.muted(), t.text(), hover), frame.alpha()));
            if (!over) continue;
            hovered = i;
        }
        if (hovered != this.hoveredIcon) {
            this.hoveredIcon = hovered;
            this.hoveredAt = now;
        }
        this.themePicker.layout(this.layout.headerIcon(1), this.popups);
    }

    private void drawHeaderIcon(class_332 c, int index, Rect r, int color) {
        int x = r.centerX();
        int y = r.centerY();
        if (index == 1) {
            GuiPrimitives.rounded(c, x - 2, y - 2, 5, 5, 3, color);
            for (int i = 0; i < 8; ++i) {
                double a = (double)i * Math.PI / 4.0;
                GuiPrimitives.line(c, x + (int)Math.round(Math.cos(a) * 5.0), y + (int)Math.round(Math.sin(a) * 5.0), x + (int)Math.round(Math.cos(a) * 6.0), y + (int)Math.round(Math.sin(a) * 6.0), color);
            }
        } else if (index == 4) {
            GuiPrimitives.line(c, x - 5, y - 2, x, y - 5, color);
            GuiPrimitives.line(c, x, y - 5, x + 5, y - 2, color);
            GuiPrimitives.line(c, x - 5, y - 2, x, y + 1, color);
            GuiPrimitives.line(c, x, y + 1, x + 5, y - 2, color);
            GuiPrimitives.line(c, x - 5, y + 2, x, y + 5, color);
            GuiPrimitives.line(c, x, y + 5, x + 5, y + 2, color);
        } else {
            GuiPrimitives.icon(c, HEADER_ICONS[index], x, y, 10, color);
        }
    }

    private void drawNavigation(class_332 c, GuiFrame frame) {
        GuiTheme t = GuiTheme.current();
        for (int i = 0; i < TABS.length; ++i) {
            Rect tab = this.layout.tab(i);
            GuiText.bold(c, TABS[i].displayName(), tab.x() + 7, tab.y() + 7, GuiTheme.opacity(TABS[i] == this.selected && !this.searching ? t.text() : t.muted(), frame.alpha()));
        }
        Rect active = this.layout.tab(this.selectedIndex());
        float x = this.underlineX.to(active.x() + 7, this.dt, frame.reduced());
        float w = this.underlineWidth.to(active.w() - 14, this.dt, frame.reduced());
        if (!this.searching) {
            GuiPrimitives.panel(c, new Rect(Math.round(x), this.layout.navigation().bottom() - 2, Math.round(w), 1), GuiTheme.opacity(t.accent(), frame.alpha()), 0);
        }
    }

    private void drawTooltip(class_332 c, String text, GuiFrame frame) {
        int w = GuiText.widthSized(text, 8.5f) + 14;
        Rect r = PopupManager.clamp(new Rect((int)frame.mouseX() - w / 2, (int)frame.mouseY() + 13, w, 20), this.layout.window().inset(4));
        GuiTheme t = GuiTheme.current();
        GuiPrimitives.borderedPanel(c, r, GuiTheme.opacity(t.secondary(), frame.alpha()), GuiTheme.opacity(t.border(), frame.alpha()), 5);
        GuiText.small(c, text, r.x() + 7, r.y() + 6, GuiTheme.opacity(t.text(), frame.alpha()));
    }

    private double pointerX(double x) {
        return (x - (double)this.field_22789 / 2.0) / (double)this.scale + (double)this.field_22789 / 2.0;
    }

    private double pointerY(double y) {
        return (y - (double)this.field_22790 / 2.0) / (double)this.scale + (double)this.field_22790 / 2.0;
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        double y;
        if (this.closing || !GuiAccess.worldReady(this.field_22787)) {
            return true;
        }
        double x = this.pointerX(click.comp_4798());
        if (this.popups.mouseClicked(x, y = this.pointerY(click.comp_4799()), click.method_74245())) {
            return true;
        }
        if (click.method_74245() == 0) {
            int i;
            for (i = 0; i < TABS.length; ++i) {
                if (!this.layout.tab(i).contains(x, y)) continue;
                this.select(TABS[i]);
                return true;
            }
            for (i = 0; i < 5; ++i) {
                if (!this.layout.headerIcon(i).contains(x, y)) continue;
                switch (i) {
                    case 0: {
                        this.page().leave();
                        this.searching = !this.searching;
                        this.changedAt = System.nanoTime();
                        if (!this.searching) break;
                        this.search.activate();
                        break;
                    }
                    case 1: {
                        this.themePicker.open(this.popups);
                        break;
                    }
                    case 2: {
                        this.field_22787.method_1507((class_437)new ProfilesScreen(this));
                        break;
                    }
                    case 3: {
                        this.field_22787.method_1507((class_437)new EmptyScreen(this, "wardrobe"));
                        break;
                    }
                    case 4: {
                        this.focusSettings();
                    }
                }
                return true;
            }
        }
        this.page().mouseClicked(x, y, click.method_74245());
        return true;
    }

    public boolean method_25403(class_11909 click, double dx, double dy) {
        double y;
        if (this.closing) {
            return true;
        }
        double x = this.pointerX(click.comp_4798());
        if (!this.popups.mouseDragged(x, y = this.pointerY(click.comp_4799()), click.method_74245(), dx / (double)this.scale, dy / (double)this.scale)) {
            this.page().mouseDragged(x, y, click.method_74245(), dx / (double)this.scale, dy / (double)this.scale);
        }
        return true;
    }

    public boolean method_25406(class_11909 click) {
        double y;
        double x = this.pointerX(click.comp_4798());
        if (!this.popups.mouseReleased(x, y = this.pointerY(click.comp_4799()), click.method_74245())) {
            this.page().mouseReleased(x, y, click.method_74245());
        }
        OrangeClient.instance().save();
        return true;
    }

    public boolean method_25401(double x, double y, double horizontal, double vertical) {
        if (!this.popups.mouseScrolled(x = this.pointerX(x), y = this.pointerY(y), horizontal, vertical)) {
            this.page().mouseScrolled(x, y, horizontal, vertical);
        }
        return true;
    }

    @Override
    public boolean method_25404(class_11908 input) {
        if (input.comp_4795() == 256 || OrangeClient.instance().menuKey().method_1417(input)) {
            return super.method_25404(input);
        }
        if (this.closing) {
            return true;
        }
        if (input.comp_4795() == 70 && (input.comp_4797() & 2) != 0 && !this.popups.isOpen()) {
            this.page().leave();
            this.searching = true;
            this.search.activate();
            return true;
        }
        return this.popups.keyPressed(input) || this.page().keyPressed(input) || super.method_25404(input);
    }

    public boolean method_25400(class_11905 input) {
        return this.closing || this.popups.charTyped(input) || this.page().charTyped(input);
    }

    public boolean onEscape() {
        this.popups.close();
        return false;
    }

    public void requestClose() {
        if (this.closing) {
            return;
        }
        this.popups.close();
        if (this.page() != null) {
            this.page().leave();
        }
        OrangeClient.instance().save();
        if (OrangeClient.instance().config().reducedMotion) {
            GuiAccess.closeToGame(this.field_22787);
            return;
        }
        this.closing = true;
        this.closingAt = System.nanoTime();
        this.closeFrom = this.visibility;
    }

    public void method_25432() {
        this.popups.close();
        if (this.page() != null) {
            this.page().leave();
        }
        OrangeClient.instance().save();
    }

    @Override
    public void method_25419() {
        this.requestClose();
    }
}

