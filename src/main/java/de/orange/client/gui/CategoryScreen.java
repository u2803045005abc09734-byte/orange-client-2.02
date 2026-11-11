/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_364
 *  net.minecraft.class_437
 */
package de.orange.client.gui;

import de.orange.client.OrangeClient;
import de.orange.client.gui.OrangeSubScreen;
import de.orange.client.gui.SettingsScreen;
import de.orange.client.hud.HudEditorScreen;
import de.orange.client.module.Category;
import de.orange.client.module.ClientModule;
import de.orange.client.ui.Anim;
import de.orange.client.ui.Glass;
import de.orange.client.ui.ModuleEntry;
import de.orange.client.ui.Rect;
import de.orange.client.ui.SearchField;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_437;

public final class CategoryScreen
extends OrangeSubScreen {
    private static final Category[] CATEGORIES = Category.values();
    private final Category category;
    private final String focus;
    private SearchField search;
    private String query = "";
    private int scroll;
    private String cachedQuery;
    private List<ClientModule> cachedEntries = List.of();
    private final Map<String, ModuleEntry> rows = new HashMap<String, ModuleEntry>();
    private final Map<Category, Anim> tabHovers = new EnumMap<Category, Anim>(Category.class);

    public CategoryScreen(class_437 parent, Category category) {
        this(parent, category, "");
    }

    public CategoryScreen(class_437 parent, Category category, String focus) {
        super(parent, category.displayName());
        this.category = category;
        this.focus = focus;
    }

    @Override
    protected void method_25426() {
        super.method_25426();
        this.search = new SearchField(this.field_22793, this.left + 14, this.top + 82, this.panelWidth - 28, 25, (class_2561)class_2561.method_43470((String)"Module suchen"));
        this.search.method_1880(80);
        this.search.method_1852(this.query);
        this.search.method_47404((class_2561)class_2561.method_43470((String)"Module suchen \u2026"));
        this.search.method_1863(value -> {
            this.query = value;
            this.scroll = 0;
        });
        this.method_37063(this.search);
    }

    private List<ClientModule> entries() {
        String q = this.query.strip().toLowerCase(Locale.ROOT);
        if (!q.equals(this.cachedQuery)) {
            this.cachedQuery = q;
            this.cachedEntries = OrangeClient.instance().modules().in(this.category).stream().filter(m -> q.isEmpty() || (m.name() + " " + m.description()).toLowerCase(Locale.ROOT).contains(q)).toList();
        }
        return this.cachedEntries;
    }

    private int rowHeight() {
        return this.panelWidth < 390 ? 61 : 66;
    }

    private float reveal(int index) {
        return OrangeClient.instance().config().reducedMotion ? 1.0f : Anim.easeOutCubic(Math.clamp((this.age() - 0.025f * (float)index) / 0.32f, 0.0f, 1.0f));
    }

    private Rect entryRect(int index) {
        return new Rect(this.left + 12, this.listTop() + index * this.rowHeight() - this.scroll + (int)((1.0f - this.reveal(index)) * 8.0f), this.panelWidth - 24, this.rowHeight() - 7);
    }

    private int listTop() {
        return this.top + 114;
    }

    private int listBottom() {
        return this.top + this.panelHeight - 46;
    }

    private Rect tabBounds(int index) {
        int available = this.panelWidth - 24;
        int gap = 4;
        int x = this.left + 12 + index * (available + gap) / CATEGORIES.length;
        int next = this.left + 12 + (index + 1) * (available + gap) / CATEGORIES.length;
        return new Rect(x, this.top + 54, next - x - gap, 22);
    }

    private void drawTabs(class_332 c, int mx, int my) {
        Category[] categories = CATEGORIES;
        boolean iconsOnly = this.panelWidth < 480;
        boolean reduced = OrangeClient.instance().config().reducedMotion;
        for (int i = 0; i < categories.length; ++i) {
            int color;
            Rect rect;
            Category value = categories[i];
            float hover = this.tabHovers.computeIfAbsent(value, ignored -> new Anim(0.0f)).to((rect = this.tabBounds(i)).contains(mx, my) ? 1.0f : 0.0f, this.dt, reduced);
            Glass.panel(c, rect, hover, value == this.category ? 1.0f : 0.0f, 0.8f);
            int n = color = value == this.category ? OrangeClient.instance().config().guiAccent : -6184277;
            if (iconsOnly) {
                Glass.icon(c, value.icon(), rect.centerX(), rect.centerY(), 13, color);
                continue;
            }
            if (rect.w() >= 88) {
                Glass.icon(c, value.icon(), rect.x() + 13, rect.centerY(), 12, color);
                Glass.text(c, Glass.trim(value.displayName(), rect.w() - 32), rect.x() + 25, rect.y() + 6, color);
                continue;
            }
            this.centered(c, Glass.trim(value.displayName(), rect.w() - 10), rect.centerX(), rect.y() + 6, color);
        }
    }

    private void drawEmptyState(class_332 c) {
        int center = this.left + this.panelWidth / 2;
        int height = this.listBottom() - this.listTop();
        int iconSize = height < 90 ? 17 : 27;
        int blockHeight = height < 90 ? 54 : 76;
        int y = this.listTop() + Math.max(0, (height - blockHeight) / 2);
        Glass.icon(c, this.category.icon(), center, y + iconSize / 2, iconSize, -6184277);
        String title = this.category == Category.OPTIMIZER ? "Performance-Preset verf\u00fcgbar" : "Keine passenden Module";
        String description = this.category == Category.OPTIMIZER ? "Nutze Client tools \u2192 Optimizer f\u00fcr den Lightweight-Modus." : "Probiere einen anderen Suchbegriff.";
        int titleY = y + iconSize + 7;
        this.centered(c, Glass.trim(title, this.panelWidth - 42), center, titleY, -986895);
        this.centered(c, Glass.trim(description, this.panelWidth - 42), center, titleY + 16, -6184277);
    }

    public void method_25394(class_332 c, int mx, int my, float delta) {
        this.begin(c, mx, my, delta, this.category.icon(), this.category.displayName(), "Dein Spiel. Dein Setup.");
        this.drawTabs(c, mx, my);
        List<ClientModule> entries = this.entries();
        int rowHeight = this.rowHeight();
        this.scroll = Math.clamp((long)this.scroll, 0, Math.max(0, entries.size() * rowHeight - (this.listBottom() - this.listTop())));
        c.method_44379(this.left + 8, this.listTop(), this.left + this.panelWidth - 8, this.listBottom());
        if (entries.isEmpty()) {
            this.drawEmptyState(c);
        }
        for (int i = 0; i < entries.size(); ++i) {
            ClientModule module = entries.get(i);
            int y = this.listTop() + i * rowHeight - this.scroll;
            if (y + rowHeight < this.listTop() || y > this.listBottom()) continue;
            float reveal = this.reveal(i);
            Rect rect = this.entryRect(i);
            this.rows.computeIfAbsent(module.id(), ignored -> new ModuleEntry()).draw(c, rect, module, this.focus.equals(module.id()), mx, my, this.dt, OrangeClient.instance().config().reducedMotion, reveal, OrangeClient.instance().config().guiAccent);
        }
        c.method_44380();
        if (entries.size() * rowHeight > this.listBottom() - this.listTop()) {
            int track = this.listBottom() - this.listTop();
            int thumb = Math.max(18, track * track / (entries.size() * rowHeight));
            int sy = this.listTop() + this.scroll * (track - thumb) / Math.max(1, entries.size() * rowHeight - track);
            c.method_25294(this.left + this.panelWidth - 6, sy, this.left + this.panelWidth - 4, sy + thumb, -1996488705);
        }
        int footer = this.top + this.panelHeight - 34;
        Glass.text(c, entries.size() + " Module", this.left + 17, footer + 8, -6184277);
        Rect editor = new Rect(this.left + this.panelWidth - 136, footer, 120, 25);
        if (this.category == Category.HUD || this.category == Category.PVP) {
            Glass.button(c, editor, "HUD bearbeiten", editor.contains(mx, my));
        } else if (this.category != Category.OPTIMIZER) {
            Glass.button(c, editor, "Einstellungen", editor.contains(mx, my));
        }
        super.method_25394(c, mx, my, delta);
        this.drawMessage(c, mx, my);
        if (this.panelWidth < 480) {
            for (int i = 0; i < CATEGORIES.length; ++i) {
                if (!this.tabBounds(i).contains(mx, my)) continue;
                c.method_51438(this.field_22793, (class_2561)Glass.styled(CATEGORIES[i].displayName()), mx, my);
                break;
            }
        }
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        if (this.closeClick(click)) {
            return true;
        }
        if (click.method_74245() != 0) {
            return super.method_25402(click, doubled);
        }
        for (int i = 0; i < CATEGORIES.length; ++i) {
            if (!this.tabBounds(i).contains(click.comp_4798(), click.comp_4799())) continue;
            if (CATEGORIES[i] != this.category) {
                this.field_22787.method_1507((class_437)new CategoryScreen(this.parent, CATEGORIES[i]));
            }
            return true;
        }
        Rect editor = new Rect(this.left + this.panelWidth - 136, this.top + this.panelHeight - 34, 120, 25);
        if (this.category != Category.OPTIMIZER && editor.contains(click.comp_4798(), click.comp_4799())) {
            this.field_22787.method_1507((class_437)(this.category == Category.HUD || this.category == Category.PVP ? new HudEditorScreen(this) : new SettingsScreen(this)));
            return true;
        }
        if (click.comp_4799() >= (double)this.listTop() && click.comp_4799() < (double)this.listBottom()) {
            List<ClientModule> entries = this.entries();
            for (int index = 0; index < entries.size(); ++index) {
                if (!this.entryRect(index).contains(click.comp_4798(), click.comp_4799())) continue;
                ClientModule module = entries.get(index);
                if (ModuleEntry.settingsBounds(this.entryRect(index)).contains(click.comp_4798(), click.comp_4799())) {
                    this.field_22787.method_1507((class_437)new SettingsScreen(this, module.id()));
                } else {
                    module.setEnabled(!module.enabled());
                    this.save();
                }
                return true;
            }
        }
        return super.method_25402(click, doubled);
    }

    public boolean method_25401(double mx, double my, double horizontal, double vertical) {
        if (mx >= (double)this.left && mx < (double)(this.left + this.panelWidth) && my >= (double)this.listTop() && my < (double)this.listBottom()) {
            this.scroll = Math.clamp((long)(this.scroll - (int)(vertical * 28.0)), 0, Math.max(0, this.entries().size() * this.rowHeight() - (this.listBottom() - this.listTop())));
            return true;
        }
        return super.method_25401(mx, my, horizontal, vertical);
    }
}

