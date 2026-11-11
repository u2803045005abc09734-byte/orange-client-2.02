/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_364
 *  net.minecraft.class_437
 */
package de.orange.client.gui;

import de.orange.client.OrangeClient;
import de.orange.client.gui.CategoryScreen;
import de.orange.client.gui.OrangeSubScreen;
import de.orange.client.gui.PackDetailScreen;
import de.orange.client.gui.ProfilesScreen;
import de.orange.client.gui.ResourcePackScreen;
import de.orange.client.gui.SettingsScreen;
import de.orange.client.gui.premium.ClickGuiScreen;
import de.orange.client.modrinth.ModrinthService;
import de.orange.client.module.Category;
import de.orange.client.module.ClientModule;
import de.orange.client.ui.Glass;
import de.orange.client.ui.Rect;
import de.orange.client.ui.SearchField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionException;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_437;

public final class SearchScreen
extends OrangeSubScreen {
    private SearchField field;
    private String query = "";
    private String remoteError = "";
    private List<Result> local = List.of();
    private List<ModrinthService.ProjectHit> packs = List.of();
    private List<Result> combined = List.of();
    private int request;
    private int scroll;
    private long searchAt;
    private boolean pending;
    private boolean loading;

    public SearchScreen(class_437 parent) {
        super(parent, "Suche");
    }

    @Override
    protected void method_25426() {
        super.method_25426();
        this.field = new SearchField(this.field_22793, this.left + 14, this.top + 55, this.panelWidth - 28, 29, (class_2561)class_2561.method_43470((String)"Orange Client durchsuchen"));
        this.field.method_47404((class_2561)class_2561.method_43470((String)"Module, Kategorien, Einstellungen, Resourcepacks \u2026"));
        this.field.method_1880(100);
        this.field.method_1852(this.query);
        this.field.method_1863(this::changed);
        this.method_37063((class_364)this.field);
        this.method_48265((class_364)this.field);
        this.rebuild();
        if (!this.query.isBlank()) {
            this.pending = true;
            this.searchAt = System.nanoTime() + 300000000L;
        }
    }

    private void changed(String value) {
        this.query = value;
        this.scroll = 0;
        this.remoteError = "";
        this.packs = List.of();
        ++this.request;
        this.loading = false;
        this.rebuild();
        this.pending = !this.query.isBlank();
        this.searchAt = System.nanoTime() + 350000000L;
    }

    private void rebuild() {
        String q = this.query.strip().toLowerCase(Locale.ROOT);
        ArrayList<Result> results = new ArrayList<Result>();
        if (!q.isEmpty()) {
            for (ClientModule m : OrangeClient.instance().modules().all()) {
                if (!(m.name() + " " + m.description() + " " + m.id()).toLowerCase(Locale.ROOT).contains(q)) continue;
                results.add(new Result("module", m.icon(), m.name(), m.description(), m));
            }
        }
        for (Category category : Category.values()) {
            if (!q.isEmpty() && !(category.displayName() + " category kategorie").toLowerCase(Locale.ROOT).contains(q)) continue;
            results.add(new Result("category", category.icon(), category.displayName(), "Kategorie \u00f6ffnen", (Object)category));
        }
        if (!q.isEmpty() && "einstellungen settings animationen blur transparenz deckkraft glas hudgr\u00f6\u00dfe hud skalierung farbe akzent".contains(q)) {
            results.add(new Result("settings", "settings", "Darstellung & Bedienung", "Animationen, Blur, Farben und globale HUD-Gr\u00f6\u00dfe", null));
        }
        if (!q.isEmpty() && "profile profiles profil import export konfiguration".contains(q)) {
            results.add(new Result("profiles", "profiles", "Profile", "Profile erstellen, wechseln, importieren und exportieren", null));
        }
        this.local = List.copyOf(results);
        this.combine();
    }

    public void method_25393() {
        if (!this.pending || System.nanoTime() < this.searchAt) {
            return;
        }
        this.pending = false;
        this.loading = true;
        int token = ++this.request;
        OrangeClient.instance().packs().search(this.query, "relevance", 0).whenComplete((page, error) -> class_310.method_1551().execute(() -> {
            if (token != this.request) {
                return;
            }
            this.loading = false;
            if (error != null) {
                this.packs = List.of();
                Throwable cause = error;
                while (cause instanceof CompletionException && cause.getCause() != null) {
                    cause = cause.getCause();
                }
                this.remoteError = cause.getMessage() == null ? "Modrinth ist gerade nicht erreichbar." : cause.getMessage();
            } else {
                this.packs = page.hits().stream().limit(5L).toList();
            }
            this.combine();
        }));
    }

    private int bodyTop() {
        return this.top + 92;
    }

    private int bodyBottom() {
        return this.top + this.panelHeight - 43;
    }

    private int rowHeight() {
        return this.panelWidth < 390 ? 45 : 48;
    }

    private void combine() {
        ArrayList<Result> results = new ArrayList<Result>(this.local);
        for (ModrinthService.ProjectHit hit : this.packs) {
            results.add(new Result("pack", "pack", hit.title(), hit.author() + " \u00b7 " + hit.description(), hit));
        }
        this.combined = List.copyOf(results);
    }

    private List<Result> results() {
        return this.combined;
    }

    public void method_25394(class_332 c, int mx, int my, float delta) {
        this.begin(c, mx, my, delta, "search", "Alles an einem Ort", "Module \u00b7 Kategorien \u00b7 Einstellungen \u00b7 Modrinth");
        List<Result> results = this.results();
        int rowHeight = this.rowHeight();
        this.scroll = Math.clamp((long)this.scroll, 0, Math.max(0, results.size() * rowHeight + 26 - (this.bodyBottom() - this.bodyTop())));
        c.method_44379(this.left + 9, this.bodyTop(), this.left + this.panelWidth - 9, this.bodyBottom());
        for (int i = 0; i < results.size(); ++i) {
            Result result = results.get(i);
            int y = this.bodyTop() + i * rowHeight - this.scroll;
            if (y + rowHeight < this.bodyTop() || y >= this.bodyBottom()) continue;
            Rect r = new Rect(this.left + 13, y, this.panelWidth - 26, rowHeight - 5);
            Glass.panel(c, r.x(), r.y(), r.w(), r.h(), r.contains(mx, my) ? 0.6f : 0.0f, 0.0f, 0.71f);
            Glass.icon(c, result.icon, this.left + 36, y + r.h() / 2, 17, OrangeClient.instance().config().guiAccent);
            int reserve = result.kind.equals("module") ? 74 : 22;
            Glass.text(c, Glass.trim(result.title, this.panelWidth - 91 - reserve), this.left + 57, y + 7, -986895);
            Glass.text(c, Glass.trim(result.description, this.panelWidth - 91 - reserve), this.left + 57, y + 24, -6184277);
            Object object = result.value;
            if (!(object instanceof ClientModule)) continue;
            ClientModule module = (ClientModule)object;
            String state = module.enabled() ? "AN" : "AUS";
            Rect toggle = new Rect(this.left + this.panelWidth - 63, y + 8, 38, 26);
            Glass.button(c, toggle, state, toggle.contains(mx, my));
        }
        int footY = this.bodyTop() + results.size() * rowHeight - this.scroll + 7;
        if (this.loading || this.pending) {
            Glass.text(c, "Resourcepacks werden gesucht \u2026", this.left + 26, footY, -6184277);
        } else if (!this.remoteError.isEmpty()) {
            Glass.text(c, Glass.trim(this.remoteError, this.panelWidth - 53), this.left + 26, footY, -17253);
        } else if (results.isEmpty()) {
            Glass.text(c, "Keine passenden Ergebnisse.", this.left + 26, this.bodyTop() + 21, -6184277);
        }
        c.method_44380();
        if (!this.remoteError.isEmpty() && footY >= this.bodyTop() && footY < this.bodyBottom() && new Rect(this.left + 20, footY, this.panelWidth - 40, 16).contains(mx, my)) {
            c.method_51438(this.field_22793, (class_2561)class_2561.method_43470((String)this.remoteError), mx, my);
        }
        this.scrollBar(c, results.size() * rowHeight + 26, this.bodyTop(), this.bodyBottom(), this.scroll);
        Rect browse = new Rect(this.left + this.panelWidth - 167, this.top + this.panelHeight - 34, 151, 25);
        Glass.button(c, browse, "Resourcepacks \u00f6ffnen", browse.contains(mx, my));
        Glass.text(c, results.size() + " Ergebnisse", this.left + 18, this.top + this.panelHeight - 26, -6184277);
        super.method_25394(c, mx, my, delta);
        this.drawMessage(c, mx, my);
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        if (this.closeClick(click)) {
            return true;
        }
        if (click.method_74245() != 0) {
            return super.method_25402(click, doubled);
        }
        if (new Rect(this.left + this.panelWidth - 167, this.top + this.panelHeight - 34, 151, 25).contains(click.comp_4798(), click.comp_4799())) {
            this.field_22787.method_1507((class_437)new ResourcePackScreen(this, this.query));
            return true;
        }
        if (click.comp_4798() >= (double)(this.left + 13) && click.comp_4798() < (double)(this.left + this.panelWidth - 13) && click.comp_4799() >= (double)this.bodyTop() && click.comp_4799() < (double)this.bodyBottom()) {
            List<Result> results = this.results();
            int index = (int)((click.comp_4799() - (double)this.bodyTop() + (double)this.scroll) / (double)this.rowHeight());
            if (index >= 0 && index < results.size() && click.comp_4799() < (double)(this.bodyTop() + index * this.rowHeight() - this.scroll + this.rowHeight() - 5)) {
                Result result = results.get(index);
                switch (result.kind) {
                    case "module": {
                        ClientModule module = (ClientModule)result.value;
                        int y = this.bodyTop() + index * this.rowHeight() - this.scroll;
                        if (new Rect(this.left + this.panelWidth - 63, y + 8, 38, 26).contains(click.comp_4798(), click.comp_4799())) {
                            module.setEnabled(!module.enabled());
                            this.save();
                            break;
                        }
                        Category cat = Arrays.stream(Category.values()).filter(module.categories()::contains).findFirst().orElse(Category.HUD);
                        class_437 class_4372 = this.parent;
                        if (class_4372 instanceof ClickGuiScreen) {
                            ClickGuiScreen gui = (ClickGuiScreen)class_4372;
                            gui.focusModule(module);
                            this.field_22787.method_1507((class_437)gui);
                            break;
                        }
                        this.field_22787.method_1507((class_437)new CategoryScreen(this, cat, module.id()));
                        break;
                    }
                    case "category": {
                        class_437 class_4373 = this.parent;
                        if (class_4373 instanceof ClickGuiScreen) {
                            ClickGuiScreen gui = (ClickGuiScreen)class_4373;
                            gui.select((Category)((Object)result.value));
                            this.field_22787.method_1507((class_437)gui);
                            break;
                        }
                        this.field_22787.method_1507((class_437)new CategoryScreen((class_437)this, (Category)((Object)result.value)));
                        break;
                    }
                    case "settings": {
                        class_437 class_4374 = this.parent;
                        if (class_4374 instanceof ClickGuiScreen) {
                            ClickGuiScreen gui = (ClickGuiScreen)class_4374;
                            gui.focusSettings();
                            this.field_22787.method_1507((class_437)gui);
                            break;
                        }
                        this.field_22787.method_1507((class_437)new SettingsScreen(this));
                        break;
                    }
                    case "profiles": {
                        this.field_22787.method_1507((class_437)new ProfilesScreen(this));
                        break;
                    }
                    case "pack": {
                        this.field_22787.method_1507((class_437)new PackDetailScreen(this, (ModrinthService.ProjectHit)result.value));
                    }
                }
                return true;
            }
        }
        return super.method_25402(click, doubled);
    }

    @Override
    public boolean method_25404(class_11908 input) {
        if (input.comp_4795() == 257 || input.comp_4795() == 335) {
            this.field_22787.method_1507((class_437)new ResourcePackScreen(this, this.query));
            return true;
        }
        return super.method_25404(input);
    }

    public boolean method_25401(double mx, double my, double horizontal, double vertical) {
        if (mx >= (double)this.left && mx < (double)(this.left + this.panelWidth) && my >= (double)this.bodyTop() && my < (double)this.bodyBottom()) {
            this.scroll = Math.clamp((long)(this.scroll - (int)(vertical * 25.0)), 0, Math.max(0, this.results().size() * this.rowHeight() + 26 - (this.bodyBottom() - this.bodyTop())));
            return true;
        }
        return super.method_25401(mx, my, horizontal, vertical);
    }

    public void method_25432() {
        ++this.request;
        this.pending = false;
    }

    private record Result(String kind, String icon, String title, String description, Object value) {
    }
}

