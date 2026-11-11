/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package de.orange.client.gui.premium;

import de.orange.client.OrangeClient;
import de.orange.client.gui.ResourcePackScreen;
import de.orange.client.gui.premium.ClickGuiScreen;
import de.orange.client.gui.premium.GuiPage;
import de.orange.client.module.Category;
import de.orange.client.module.ClientModule;
import de.orange.client.ui.Rect;
import de.orange.client.ui.premium.GuiFrame;
import de.orange.client.ui.premium.GuiPrimitives;
import de.orange.client.ui.premium.GuiText;
import de.orange.client.ui.premium.GuiTheme;
import de.orange.client.ui.premium.components.PopupManager;
import de.orange.client.ui.premium.components.SearchComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

final class QuickSearchPage
implements GuiPage {
    private final SearchComponent search;
    private final Consumer<ClientModule> openModule;
    private final Consumer<Category> openCategory;
    private final class_437 parent;
    private List<Result> results = List.of();
    private Rect viewport;
    private Rect body = this.viewport = new Rect(0, 0, 0, 0);
    private int scroll;
    private int focus;

    QuickSearchPage(class_437 parent, Consumer<ClientModule> openModule, Consumer<Category> openCategory) {
        this.parent = parent;
        this.openModule = openModule;
        this.openCategory = openCategory;
        this.search = new SearchComponent("Search modules, categories, settings\u2026", value -> this.rebuild());
        this.rebuild();
    }

    void activate() {
        this.rebuild();
        this.search.setFocused(true);
    }

    private void rebuild() {
        String query = this.search == null ? "" : this.search.text().strip().toLowerCase(Locale.ROOT);
        ArrayList<Result> found = new ArrayList<Result>();
        for (ClientModule module : OrangeClient.instance().modules().all()) {
            if (!(module.name() + " " + module.id() + " " + module.description()).toLowerCase(Locale.ROOT).contains(query)) continue;
            found.add(new Result(module.name(), module.description(), module.icon(), () -> this.openModule.accept(module)));
        }
        for (Category category : ClickGuiScreen.TABS) {
            if (query.isEmpty() || !category.displayName().toLowerCase(Locale.ROOT).contains(query)) continue;
            found.add(new Result(category.displayName(), "Open category", category.icon(), () -> this.openCategory.accept(category)));
        }
        if (query.isEmpty() || "settings theme blur animation colour color scale hud size".contains(query)) {
            found.add(new Result("Client settings", "Theme, blur, animations and HUD scale", "settings", () -> {
                this.openCategory.accept(Category.MISC);
                class_437 patt0$temp = this.parent;
                if (patt0$temp instanceof ClickGuiScreen) {
                    ClickGuiScreen gui = (ClickGuiScreen)patt0$temp;
                    gui.focusSettings();
                }
            }));
        }
        this.results = List.copyOf(found);
        this.scroll = 0;
        this.focus = 0;
    }

    @Override
    public void render(class_332 c, Rect viewport, GuiFrame frame, PopupManager popups) {
        this.viewport = viewport;
        GuiTheme t = GuiTheme.current();
        this.search.draw(c, new Rect(viewport.x() + 7, viewport.y() + 2, viewport.w() - 14, 25), frame);
        Rect resource = this.resourceButton();
        if (resource.contains(frame.mouseX(), frame.mouseY())) {
            GuiPrimitives.panel(c, resource, GuiTheme.opacity(t.hover(), frame.alpha()), 5);
        }
        GuiPrimitives.icon(c, "pack", resource.x() + 10, resource.centerY(), 10, GuiTheme.opacity(t.secondaryAccent(), frame.alpha()));
        GuiText.small(c, "Search resource packs on Modrinth  \u2192", resource.x() + 23, resource.y() + 6, GuiTheme.opacity(t.muted(), frame.alpha()));
        this.body = new Rect(viewport.x() + 7, viewport.y() + 55, viewport.w() - 14, Math.max(1, viewport.h() - 57));
        this.scroll = Math.clamp((long)this.scroll, 0, Math.max(0, this.results.size() * 35 - this.body.h()));
        c.method_44379(this.body.x(), this.body.y(), this.body.right(), this.body.bottom());
        for (int i = 0; i < this.results.size(); ++i) {
            Rect row = this.row(i);
            if (row.bottom() <= this.body.y() || row.y() >= this.body.bottom()) continue;
            Result result = this.results.get(i);
            if (row.contains(frame.mouseX(), frame.mouseY()) || i == this.focus && this.search.isFocused()) {
                GuiPrimitives.panel(c, row, GuiTheme.opacity(t.hover(), frame.alpha() * 0.7f), 5);
            }
            GuiPrimitives.icon(c, result.icon(), row.x() + 11, row.centerY(), 11, GuiTheme.opacity(t.muted(), frame.alpha()));
            GuiText.text(c, GuiText.trim(result.name(), row.w() - 40), row.x() + 27, row.y() + 5, GuiTheme.opacity(t.text(), frame.alpha()));
            GuiText.small(c, GuiText.trim(result.description(), row.w() - 40, 8.5f), row.x() + 27, row.y() + 19, GuiTheme.opacity(t.muted(), frame.alpha()));
        }
        if (this.results.isEmpty()) {
            GuiText.bold(c, "No matching modules", this.body.x() + 10, this.body.y() + 16, GuiTheme.opacity(t.text(), frame.alpha()));
            GuiText.small(c, "Try another name or search Modrinth above.", this.body.x() + 10, this.body.y() + 33, GuiTheme.opacity(t.muted(), frame.alpha()));
        }
        c.method_44380();
    }

    private Rect row(int index) {
        return new Rect(this.body.x(), this.body.y() + index * 35 - this.scroll, this.body.w(), 32);
    }

    private Rect resourceButton() {
        return new Rect(this.viewport.x() + 7, this.viewport.y() + 30, this.viewport.w() - 14, 21);
    }

    private void browse() {
        class_310.method_1551().method_1507((class_437)new ResourcePackScreen(this.parent, this.search.text()));
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (this.search.mouseClicked(x, y, button)) {
            return true;
        }
        if (button != 0) {
            return false;
        }
        if (this.resourceButton().contains(x, y)) {
            this.browse();
            return true;
        }
        if (this.body.contains(x, y)) {
            for (int i = 0; i < this.results.size(); ++i) {
                if (!this.row(i).contains(x, y)) continue;
                this.results.get(i).action().run();
                return true;
            }
        }
        return this.viewport.contains(x, y);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        return this.search.mouseDragged(x, y, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        return this.search.mouseReleased(x, y, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (!this.body.contains(x, y)) {
            return false;
        }
        this.scroll = Math.clamp((long)(this.scroll - (int)(vertical * 27.0)), 0, Math.max(0, this.results.size() * 35 - this.body.h()));
        return true;
    }

    @Override
    public boolean keyPressed(class_11908 input) {
        if (input.comp_4795() == 264 || input.comp_4795() == 265) {
            if (!this.results.isEmpty()) {
                this.focus = Math.floorMod(this.focus + (input.comp_4795() == 264 ? 1 : -1), this.results.size());
                this.scroll = Math.clamp((long)(this.focus * 35 - this.body.h() / 2), 0, Math.max(0, this.results.size() * 35 - this.body.h()));
            }
            return true;
        }
        if (input.comp_4795() == 257 || input.comp_4795() == 335) {
            if (!this.results.isEmpty()) {
                this.results.get(Math.clamp((long)this.focus, 0, this.results.size() - 1)).action().run();
            }
            return true;
        }
        return this.search.keyPressed(input);
    }

    @Override
    public boolean charTyped(class_11905 input) {
        return this.search.charTyped(input);
    }

    @Override
    public void leave() {
        this.search.setFocused(false);
    }

    private record Result(String name, String description, String icon, Runnable action) {
    }
}

