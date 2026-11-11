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
 *  net.minecraft.class_5348
 */
package de.orange.client.gui;

import de.orange.client.OrangeClient;
import de.orange.client.gui.PackDetailScreen;
import de.orange.client.modrinth.ModrinthService;
import de.orange.client.ui.Anim;
import de.orange.client.ui.Glass;
import de.orange.client.ui.GlassScreen;
import de.orange.client.ui.Rect;
import de.orange.client.ui.ResourcePackCard;
import de.orange.client.ui.SearchField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_437;
import net.minecraft.class_5348;

public final class ResourcePackScreen
extends GlassScreen {
    private static final String[] SORT = new String[]{"relevance", "downloads", "newest"};
    private static final String[] SORT_NAMES = new String[]{"Relevanz", "Beliebt", "Neueste"};
    private static final String[] VERSIONS = new String[]{"1.21.11", "1.21.10", "1.21.9", "all"};
    private static final String[] CATEGORIES = new String[]{"", "combat", "vanilla-like", "realistic", "tweaks"};
    private static final String[] CATEGORY_NAMES = new String[]{"Alle Kategorien", "PvP", "Vanilla", "Realistisch", "Tweaks"};
    private final class_437 parent;
    private final ModrinthService service;
    private final Map<String, ResourcePackCard> cards = new HashMap<String, ResourcePackCard>();
    private final Map<String, Boolean> busy = new HashMap<String, Boolean>();
    private List<ModrinthService.ProjectHit> hits = List.of();
    private SearchField search;
    private String query;
    private String message = "";
    private boolean error;
    private boolean loading;
    private boolean first = true;
    private int sort;
    private int version;
    private int category;
    private int offset;
    private int total;
    private int request;
    private int scroll;
    private int left;
    private int top;
    private int panelW;
    private int panelH;
    private int listTop;
    private int listBottom;
    private int columns;
    private int cardW;
    private int cardH;
    private boolean compact;
    private long deadline;
    private long lastFrame;
    private long opened = System.nanoTime();
    private float dt;
    private static final int CARD_H = 190;
    private static final int GAP = 10;

    public ResourcePackScreen(class_437 parent) {
        this(parent, "");
    }

    public ResourcePackScreen(class_437 parent, String initialQuery) {
        super((class_2561)class_2561.method_43470((String)"Orange Client \u00b7 Resource Packs"));
        this.parent = parent;
        this.query = initialQuery == null ? "" : initialQuery;
        this.service = OrangeClient.instance().packs();
    }

    protected void method_25426() {
        this.panelW = Math.min(930, this.field_22789 - 20);
        this.panelH = this.field_22790 - 20;
        this.left = (this.field_22789 - this.panelW) / 2;
        this.top = 10;
        this.listTop = this.top + 99;
        this.listBottom = this.top + this.panelH - 52;
        this.compact = this.listBottom - this.listTop < 190;
        this.cardH = this.compact ? Math.clamp((long)(this.listBottom - this.listTop), 68, 96) : 190;
        this.columns = this.compact ? Math.max(1, Math.min(2, (this.panelW - 24) / 330)) : Math.max(1, Math.min(4, (this.panelW - 24) / 184));
        this.cardW = (this.panelW - 24 - (this.columns - 1) * 10) / this.columns;
        this.search = new SearchField(this.field_22793, this.left + 14, this.top + 40, this.panelW - 107, 23, (class_2561)class_2561.method_43470((String)"Resource Packs suchen"));
        this.search.method_1880(160);
        this.search.method_47404((class_2561)class_2561.method_43470((String)"Packs auf Modrinth suchen \u2026"));
        this.search.method_1852(this.query);
        this.search.method_1863(text -> {
            this.query = text;
            this.deadline = System.nanoTime() + 350000000L;
        });
        this.method_37063(this.search);
        this.scroll = Math.clamp((long)this.scroll, 0, this.maxScroll());
        if (this.first) {
            this.first = false;
            this.fetch(0);
        }
    }

    private boolean reduced() {
        return OrangeClient.instance().config().reducedMotion;
    }

    @Override
    protected void method_57734(class_332 c) {
        if (OrangeClient.instance().config().blur) {
            super.method_57734(c);
        }
    }

    public void method_25393() {
        if (this.deadline > 0L && System.nanoTime() >= this.deadline) {
            this.deadline = 0L;
            this.fetch(0);
        }
    }

    private void fetch(int start) {
        this.deadline = 0L;
        int token = ++this.request;
        this.offset = start;
        this.scroll = 0;
        this.loading = true;
        this.message = "";
        this.error = false;
        ModrinthService.SearchQuery filter = new ModrinthService.SearchQuery(this.query, SORT[this.sort], start, VERSIONS[this.version], CATEGORIES[this.category]);
        this.service.search(filter).whenComplete((page, failure) -> class_310.method_1551().execute(() -> {
            if (token != this.request) {
                return;
            }
            this.loading = false;
            if (failure != null) {
                this.hits = List.of();
                this.total = 0;
                this.error = true;
                this.message = ResourcePackScreen.failureMessage(failure);
            } else {
                this.hits = page.hits();
                this.total = page.total();
                this.offset = page.offset();
                this.cards.keySet().retainAll(this.hits.stream().map(ModrinthService.ProjectHit::id).toList());
            }
        }));
    }

    private void install(ModrinthService.ProjectHit hit) {
        if (Boolean.TRUE.equals(this.busy.get(hit.id()))) {
            return;
        }
        if (this.service.installed(hit.id()) != null) {
            this.field_22787.method_1507((class_437)new PackDetailScreen(this, hit));
            return;
        }
        if (!hit.versions().contains("1.21.11")) {
            this.message = "Dieses Pack bietet keine Version f\u00fcr Minecraft 1.21.11. Details \u00f6ffnen.";
            this.error = true;
            return;
        }
        this.busy.put(hit.id(), true);
        this.message = "Download wird gepr\u00fcft und installiert \u2026";
        this.error = false;
        this.service.install(hit).whenComplete((result, failure) -> class_310.method_1551().execute(() -> {
            this.busy.remove(hit.id());
            boolean bl = this.error = failure != null;
            if (failure != null) {
                this.message = ResourcePackScreen.failureMessage(failure);
            } else {
                this.message = "Installiert. Anschlie\u00dfend im Minecraft-Pack-Men\u00fc aktivieren.";
                this.field_22787.method_1520().method_14445();
            }
        }));
    }

    private Rect searchButton() {
        return new Rect(this.left + this.panelW - 84, this.top + 40, 70, 23);
    }

    private Rect back() {
        return new Rect(this.left + this.panelW - 84, this.top + 10, 70, 23);
    }

    private Rect sortButton() {
        return new Rect(this.left + 14, this.top + 71, Math.min(105, (this.panelW - 38) / 3), 19);
    }

    private Rect versionButton() {
        Rect s = this.sortButton();
        return new Rect(s.x() + s.w() + 5, s.y(), Math.min(105, (this.panelW - 38) / 3), 19);
    }

    private Rect categoryButton() {
        Rect s = this.versionButton();
        return new Rect(s.x() + s.w() + 5, s.y(), Math.min(142, this.panelW - 14 - (s.x() + s.w() + 5 - this.left)), 19);
    }

    private Rect cardBounds(int index) {
        return new Rect(this.left + 12 + index % this.columns * (this.cardW + 10), this.listTop + index / this.columns * (this.cardH + 10) - this.scroll, this.cardW, this.cardH);
    }

    public void method_25394(class_332 c, int mx, int my, float delta) {
        long now = System.nanoTime();
        this.dt = this.lastFrame == 0L ? 0.0f : Math.min(0.05f, (float)(now - this.lastFrame) / 1.0E9f);
        this.lastFrame = now;
        float fade = this.reduced() ? 1.0f : Anim.easeOutCubic(Math.clamp((float)(now - this.opened) / 3.5E8f, 0.0f, 1.0f));
        Glass.begin(c, this.field_22789, this.field_22790, fade);
        Glass.panel(c, this.left, this.top, this.panelW, this.panelH, 0.0f, 0.0f, 0.6f);
        Glass.icon(c, "pack", this.left + 24, this.top + 22, 17, Glass.ACCENT);
        Glass.title(c, "Resource Packs", this.left + 41, this.top + 12, 1.2f, -986895);
        Glass.button(c, this.back(), "Zur\u00fcck", this.back().contains(mx, my));
        Glass.button(c, this.searchButton(), this.loading ? "L\u00e4dt \u2026" : "Suchen", this.searchButton().contains(mx, my));
        Rect s = this.sortButton();
        Rect v = this.versionButton();
        Rect k = this.categoryButton();
        Glass.button(c, s, SORT_NAMES[this.sort], s.contains(mx, my));
        Glass.button(c, v, VERSIONS[this.version].equals("all") ? "Alle Versionen" : VERSIONS[this.version], v.contains(mx, my));
        Glass.button(c, k, CATEGORY_NAMES[this.category], k.contains(mx, my));
        this.scroll = Math.clamp((long)this.scroll, 0, this.maxScroll());
        c.method_44379(this.left + 8, this.listTop, this.left + this.panelW - 8, this.listBottom);
        if (this.loading) {
            Glass.icon(c, "search", this.left + this.panelW / 2, this.listTop + (this.compact ? 18 : 35), this.compact ? 18 : 24, Glass.ACCENT);
            this.center(c, "Modrinth wird durchsucht \u2026", this.listTop + (this.compact ? 39 : 60), -6184277);
        } else if (this.hits.isEmpty()) {
            Glass.icon(c, "pack", this.left + this.panelW / 2, this.listTop + (this.compact ? 18 : 35), this.compact ? 18 : 27, -6184277);
            this.center(c, this.error ? "Verbindung fehlgeschlagen" : "Keine passenden Packs gefunden", this.listTop + (this.compact ? 39 : 65), -986895);
            if (!this.compact || this.listBottom - this.listTop > 85) {
                this.center(c, this.error ? "Die Fehlermeldung steht unten. Erneut suchen ist jederzeit m\u00f6glich." : "Suche oder Filter anpassen und erneut entdecken.", this.listTop + (this.compact ? 57 : 84), -6184277);
            }
        } else {
            for (int i = 0; i < this.hits.size(); ++i) {
                Rect b = this.cardBounds(i);
                if (b.y() + b.h() < this.listTop || b.y() > this.listBottom) continue;
                ModrinthService.ProjectHit hit = this.hits.get(i);
                boolean installed = this.service.installed(hit.id()) != null;
                this.cards.computeIfAbsent(hit.id(), id -> new ResourcePackCard()).draw(c, b, hit, installed ? "Verwalten" : "Installieren", installed, Boolean.TRUE.equals(this.busy.get(hit.id())), mx, my, this.dt, this.reduced());
            }
        }
        c.method_44380();
        if (!this.loading && this.maxScroll() > 0) {
            int track = Math.max(1, this.listBottom - this.listTop);
            int thumb = Math.max(15, track * track / (track + this.maxScroll()));
            int sy = this.listTop + this.scroll * (track - thumb) / this.maxScroll();
            c.method_25294(this.left + this.panelW - 5, sy, this.left + this.panelW - 3, sy + thumb, -1996488705);
        }
        int footer = this.top + this.panelH - 43;
        Glass.text(c, Glass.trim(this.message.isEmpty() ? "Packs nach dem Installieren in Minecraft ausw\u00e4hlen und aktivieren." : this.message, this.panelW - 28), this.left + 14, footer, this.error ? -20320 : -6184277);
        Rect prev = new Rect(this.left + 14, footer + 16, 64, 21);
        Rect next = new Rect(this.left + this.panelW - 78, footer + 16, 64, 21);
        Glass.button(c, prev, "Vorherige", prev.contains(mx, my));
        Glass.button(c, next, "Weitere", next.contains(mx, my));
        this.center(c, (String)(this.total == 0 ? "0 Ergebnisse" : this.offset + 1 + "\u2013" + (this.offset + this.hits.size()) + " / " + this.total), footer + 21, -6184277);
        super.method_25394(c, mx, my, delta);
        if (!this.message.isBlank() && my >= footer && my < footer + 15 && mx >= this.left && mx <= this.left + this.panelW) {
            c.method_51447(this.field_22793, this.field_22793.method_1728((class_5348)class_2561.method_43470((String)this.message), Math.max(100, Math.min(360, this.field_22789 - 30))), mx, my);
        }
    }

    private void center(class_332 c, String value, int y, int color) {
        String text = Glass.trim(value, this.panelW - 36);
        Glass.text(c, text, this.left + (this.panelW - Glass.textWidth(text)) / 2, y, color);
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        if (click.method_74245() != 0) {
            return super.method_25402(click, doubled);
        }
        double x = click.comp_4798();
        double y = click.comp_4799();
        if (this.back().contains(x, y)) {
            this.method_25419();
            return true;
        }
        if (this.searchButton().contains(x, y)) {
            this.fetch(0);
            return true;
        }
        if (this.sortButton().contains(x, y)) {
            this.sort = (this.sort + 1) % SORT.length;
            this.fetch(0);
            return true;
        }
        if (this.versionButton().contains(x, y)) {
            this.version = (this.version + 1) % VERSIONS.length;
            this.fetch(0);
            return true;
        }
        if (this.categoryButton().contains(x, y)) {
            this.category = (this.category + 1) % CATEGORIES.length;
            this.fetch(0);
            return true;
        }
        int footer = this.top + this.panelH - 43;
        if (new Rect(this.left + 14, footer + 16, 64, 21).contains(x, y)) {
            if (!this.loading && this.offset > 0) {
                this.fetch(Math.max(0, this.offset - 12));
            }
            return true;
        }
        if (new Rect(this.left + this.panelW - 78, footer + 16, 64, 21).contains(x, y)) {
            if (!this.loading && this.offset + this.hits.size() < this.total) {
                this.fetch(this.offset + 12);
            }
            return true;
        }
        if (!this.loading && y >= (double)this.listTop && y < (double)this.listBottom) {
            for (int i = 0; i < this.hits.size(); ++i) {
                Rect b = this.cardBounds(i);
                if (!b.contains(x, y)) continue;
                ModrinthService.ProjectHit hit = this.hits.get(i);
                if (ResourcePackCard.actionBounds(b).contains(x, y)) {
                    this.install(hit);
                } else {
                    this.field_22787.method_1507((class_437)new PackDetailScreen(this, hit));
                }
                return true;
            }
        }
        return super.method_25402(click, doubled);
    }

    public boolean method_25401(double mx, double my, double horizontal, double vertical) {
        if (mx >= (double)this.left && mx < (double)(this.left + this.panelW) && my >= (double)this.listTop && my < (double)this.listBottom) {
            this.scroll = Math.clamp((long)(this.scroll - (int)(vertical * 35.0)), 0, this.maxScroll());
            return true;
        }
        return super.method_25401(mx, my, horizontal, vertical);
    }

    @Override
    public boolean method_25404(class_11908 input) {
        if (input.comp_4795() == 257 || input.comp_4795() == 335) {
            this.fetch(0);
            return true;
        }
        return super.method_25404(input);
    }

    private int maxScroll() {
        return Math.max(0, (this.hits.size() + Math.max(1, this.columns) - 1) / Math.max(1, this.columns) * (this.cardH + 10) - 10 - (this.listBottom - this.listTop));
    }

    static String failureMessage(Throwable failure) {
        while (failure instanceof CompletionException && failure.getCause() != null) {
            failure = failure.getCause();
        }
        return failure.getMessage() == null ? "Verbindung fehlgeschlagen. Bitte erneut versuchen." : failure.getMessage();
    }

    @Override
    public void method_25419() {
        OrangeClient.returnTo(this.parent);
    }

    @Override
    public boolean method_25421() {
        return false;
    }
}

