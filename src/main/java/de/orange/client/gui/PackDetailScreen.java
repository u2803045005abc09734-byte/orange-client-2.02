/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_407
 *  net.minecraft.class_437
 *  net.minecraft.class_5348
 */
package de.orange.client.gui;

import de.orange.client.OrangeClient;
import de.orange.client.gui.ResourcePackScreen;
import de.orange.client.modrinth.ModrinthService;
import de.orange.client.ui.Glass;
import de.orange.client.ui.GlassScreen;
import de.orange.client.ui.PackImageCache;
import de.orange.client.ui.Rect;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_407;
import net.minecraft.class_437;
import net.minecraft.class_5348;

public final class PackDetailScreen
extends GlassScreen {
    private final class_437 parent;
    private final ModrinthService.ProjectHit hit;
    private final ModrinthService service;
    private ModrinthService.ProjectDetails details;
    private List<String> images = List.of();
    private List<String> bodyLines = List.of();
    private List<String> descriptionLines = List.of();
    private int left;
    private int top;
    private int panelW;
    private int panelH;
    private int viewportTop;
    private int viewportBottom;
    private int heroH;
    private int scroll;
    private int contentHeight;
    private int selectedVersion;
    private int imageIndex;
    private int versionScroll;
    private boolean loading = true;
    private boolean busy;
    private boolean error;
    private boolean versionOpen;
    private boolean confirmDelete;
    private String message = "";
    private int generation;

    public PackDetailScreen(class_437 parent, ModrinthService.ProjectHit hit) {
        super((class_2561)class_2561.method_43470((String)("Orange Client \u00b7 " + hit.title())));
        this.parent = parent;
        this.hit = hit;
        this.service = OrangeClient.instance().packs();
    }

    protected void method_25426() {
        this.panelW = Math.min(820, this.field_22789 - 20);
        this.panelH = this.field_22790 - 20;
        this.left = (this.field_22789 - this.panelW) / 2;
        this.top = 10;
        this.viewportTop = this.top + 44;
        this.viewportBottom = this.top + this.panelH - 68;
        this.heroH = Math.max(90, Math.min(190, this.panelW / 3));
        this.reflow();
        if (this.details == null && this.generation == 0) {
            this.load();
        }
    }

    private void load() {
        int token = ++this.generation;
        this.loading = true;
        this.error = false;
        this.message = "";
        this.service.details(this.hit).whenComplete((project, failure) -> class_310.method_1551().execute(() -> {
            if (token != this.generation) {
                return;
            }
            this.loading = false;
            if (failure != null) {
                this.error = true;
                this.message = ResourcePackScreen.failureMessage(failure);
            } else {
                this.details = project;
                ArrayList<String> choices = new ArrayList<String>();
                if (this.hit.iconUrl() != null && !this.hit.iconUrl().isBlank()) {
                    choices.add(this.hit.iconUrl());
                }
                for (String url : project.gallery()) {
                    if (choices.contains(url)) continue;
                    choices.add(url);
                }
                this.images = List.copyOf(choices);
                this.selectedVersion = 0;
                for (int i = 0; i < project.versions().size(); ++i) {
                    if (!this.compatible(project.versions().get(i))) continue;
                    this.selectedVersion = i;
                    break;
                }
                this.reflow();
            }
        }));
    }

    private void reflow() {
        int textWidth = Math.max(80, this.panelW - 40);
        this.descriptionLines = PackDetailScreen.wrap(this.hit.description(), textWidth);
        String body = this.details == null ? "" : this.details.body();
        body = body.replaceAll("!\\[[^\\]]*]\\([^)]*\\)", "").replaceAll("\\[([^\\]]+)]\\([^)]*\\)", "$1").replaceAll("(?m)^#{1,6}\\s+", "").replace("**", "").replace("__", "").replace("```", "");
        this.bodyLines = PackDetailScreen.wrap(body, textWidth);
        this.contentHeight = this.heroH + 190 + this.descriptionLines.size() * 13 + this.bodyLines.size() * 13;
        this.scroll = Math.clamp((long)this.scroll, 0, this.maxScroll());
    }

    @Override
    protected void method_57734(class_332 c) {
        if (OrangeClient.instance().config().blur) {
            super.method_57734(c);
        }
    }

    private ModrinthService.PackVersion chosen() {
        return this.details == null || this.details.versions().isEmpty() ? null : this.details.versions().get(Math.clamp((long)this.selectedVersion, 0, this.details.versions().size() - 1));
    }

    private boolean compatible(ModrinthService.PackVersion version) {
        return version.gameVersions().contains("1.21.11");
    }

    private Rect back() {
        return new Rect(this.left + this.panelW - 78, this.top + 10, 64, 23);
    }

    private Rect versionButton() {
        return new Rect(this.left + 14, this.viewportTop + this.heroH + 61 - this.scroll, this.panelW - 28, 26);
    }

    private Rect action(int index) {
        int w = (this.panelW - 38) / 3;
        return new Rect(this.left + 14 + index * (w + 5), this.top + this.panelH - 37, w, 24);
    }

    private void operation(CompletableFuture<?> action, String success) {
        this.busy = true;
        this.error = false;
        this.message = "Vorgang l\u00e4uft \u2026";
        action.whenComplete((result, failure) -> class_310.method_1551().execute(() -> {
            this.busy = false;
            this.error = failure != null;
            String string = this.message = failure == null ? success : ResourcePackScreen.failureMessage(failure);
            if (failure == null) {
                this.field_22787.method_1520().method_14445();
            }
        }));
    }

    private void install() {
        if (this.busy) {
            return;
        }
        ModrinthService.PackVersion version = this.chosen();
        if (version == null || !this.compatible(version)) {
            this.error = true;
            this.message = "Bitte eine Version f\u00fcr Minecraft 1.21.11 ausw\u00e4hlen.";
            return;
        }
        ModrinthService.InstalledPack previous = this.service.installed(this.hit.id());
        if (previous != null && this.field_22787.method_1520().method_29210().contains("file/" + previous.filename())) {
            this.error = true;
            this.message = "Pack vor dem Versionswechsel im Minecraft-Pack-Men\u00fc deaktivieren.";
            return;
        }
        this.operation(this.service.install(this.hit, version.id()), "Installiert. Pack im Minecraft-Pack-Men\u00fc aktivieren.");
    }

    private void update() {
        if (this.busy) {
            return;
        }
        ModrinthService.InstalledPack pack = this.service.installed(this.hit.id());
        if (pack != null && this.field_22787.method_1520().method_29210().contains("file/" + pack.filename())) {
            this.error = true;
            this.message = "Pack vor dem Aktualisieren im Minecraft-Pack-Men\u00fc deaktivieren.";
            return;
        }
        this.operation(this.service.update(this.hit), "Aktuelle Version installiert. Im Minecraft-Pack-Men\u00fc aktivieren.");
    }

    private void askDelete() {
        if (this.busy) {
            return;
        }
        ModrinthService.InstalledPack pack = this.service.installed(this.hit.id());
        if (pack == null) {
            this.message = "Dieses Pack wird nicht von Orange verwaltet.";
            this.error = true;
            return;
        }
        if (this.field_22787.method_1520().method_29210().contains("file/" + pack.filename())) {
            this.message = "Pack vor dem Entfernen im Minecraft-Pack-Men\u00fc deaktivieren.";
            this.error = true;
            return;
        }
        this.confirmDelete = true;
    }

    public void method_25394(class_332 c, int mx, int my, float delta) {
        Glass.begin(c, this.field_22789, this.field_22790, 1.0f);
        Glass.panel(c, this.left, this.top, this.panelW, this.panelH, 0.0f, 0.0f, 0.68f);
        Glass.icon(c, "pack", this.left + 23, this.top + 22, 17, Glass.ACCENT);
        Glass.title(c, Glass.trim(this.hit.title(), (int)((float)(this.panelW - 130) / 1.15f)), this.left + 40, this.top + 12, 1.15f, -986895);
        Glass.button(c, this.back(), "Zur\u00fcck", this.back().contains(mx, my));
        c.method_44379(this.left + 8, this.viewportTop, this.left + this.panelW - 8, this.viewportBottom);
        if (this.loading) {
            Glass.text(c, "Projektdetails werden geladen \u2026", this.left + 22, this.viewportTop + 25, -6184277);
        } else if (this.details == null) {
            Glass.text(c, "Details konnten nicht geladen werden.", this.left + 22, this.viewportTop + 25, -986895);
            Glass.button(c, new Rect(this.left + 22, this.viewportTop + 48, 120, 24), "Erneut versuchen", false);
        } else {
            int y = this.viewportTop - this.scroll;
            String image = this.images.isEmpty() ? this.hit.iconUrl() : this.images.get(Math.clamp((long)this.imageIndex, 0, this.images.size() - 1));
            PackImageCache.get().drawContain(c, image, new Rect(this.left + 14, y, this.panelW - 28, this.heroH));
            if (this.images.size() > 1) {
                Rect prev = new Rect(this.left + 22, y + this.heroH - 30, 28, 23);
                Rect next = new Rect(this.left + this.panelW - 50, y + this.heroH - 30, 28, 23);
                Glass.button(c, prev, "\u2039", prev.contains(mx, my));
                Glass.button(c, next, "\u203a", next.contains(mx, my));
                Glass.text(c, this.imageIndex + 1 + " / " + this.images.size(), this.left + 60, y + this.heroH - 23, -1);
            }
            Glass.text(c, Glass.trim("Von " + this.hit.author() + "  \u00b7  " + this.hit.downloads() + " Downloads", this.panelW - 28), this.left + 14, y + this.heroH + 14, -6184277);
            ModrinthService.InstalledPack installed = this.service.installed(this.hit.id());
            Glass.text(c, (String)(installed == null ? "Noch nicht installiert" : "Installiert \u00b7 " + installed.versionId()), this.left + 14, y + this.heroH + 32, installed == null ? -6184277 : Glass.ACCENT);
            ModrinthService.PackVersion choice = this.chosen();
            String selected = choice == null ? "Keine Version verf\u00fcgbar" : "Version: " + choice.name() + "  \u203a";
            Rect vb = this.versionButton();
            Glass.button(c, vb, Glass.trim(selected, vb.w() - 18), vb.contains(mx, my));
            if (choice != null) {
                Glass.text(c, Glass.trim("Minecraft " + String.join((CharSequence)", ", choice.gameVersions()), this.panelW - 30), this.left + 14, y + this.heroH + 94, this.compatible(choice) ? -6184277 : -19813);
            }
            int ty = y + this.heroH + 115;
            Glass.text(c, "\u00dcBERSICHT", this.left + 14, ty, Glass.ACCENT);
            ty += 18;
            for (String line : this.descriptionLines) {
                if (ty >= this.viewportTop - 13 && ty < this.viewportBottom) {
                    Glass.text(c, line, this.left + 14, ty, -986895);
                }
                ty += 13;
            }
            Glass.text(c, "BESCHREIBUNG", this.left + 14, ty += 15, Glass.ACCENT);
            ty += 18;
            for (String line : this.bodyLines) {
                if (ty >= this.viewportTop - 13 && ty < this.viewportBottom) {
                    Glass.text(c, line, this.left + 14, ty, -6184277);
                }
                ty += 13;
            }
            if (this.bodyLines.isEmpty()) {
                Glass.text(c, "Keine ausf\u00fchrliche Beschreibung vorhanden.", this.left + 14, ty, -6184277);
            }
        }
        c.method_44380();
        if (!this.loading && this.maxScroll() > 0) {
            int track = Math.max(1, this.viewportBottom - this.viewportTop);
            int thumb = Math.max(14, track * track / (track + this.maxScroll()));
            int sy = this.viewportTop + this.scroll * (track - thumb) / this.maxScroll();
            c.method_25294(this.left + this.panelW - 5, sy, this.left + this.panelW - 3, sy + thumb, -1996488705);
        }
        int statusY = this.top + this.panelH - 58;
        String status = this.message.isBlank() ? "Version pr\u00fcfen. Downloads werden per SHA-512 verifiziert." : this.message;
        Glass.text(c, Glass.trim(status, this.panelW - 28), this.left + 14, statusY, this.error ? -20324 : -6184277);
        ModrinthService.InstalledPack installedPack = this.service.installed(this.hit.id());
        boolean installed = installedPack != null;
        boolean different = installed && this.chosen() != null && !this.chosen().id().equals(installedPack.versionId());
        Glass.button(c, this.action(0), this.busy ? "Bitte warten \u2026" : (different ? "Version installieren" : (installed ? "Aktualisieren" : "Installieren")), this.action(0).contains(mx, my));
        Glass.button(c, this.action(1), installed ? "Entfernen" : "Versionen", this.action(1).contains(mx, my));
        Glass.button(c, this.action(2), "Modrinth \u2197", this.action(2).contains(mx, my));
        if (this.versionOpen) {
            this.drawVersions(c, mx, my);
        } else if (this.confirmDelete) {
            this.drawDelete(c, mx, my);
        } else if (!this.message.isBlank() && my >= statusY && my < statusY + 14 && mx >= this.left && mx < this.left + this.panelW) {
            c.method_51447(this.field_22793, this.field_22793.method_1728((class_5348)class_2561.method_43470((String)this.message), Math.min(360, this.field_22789 - 30)), mx, my);
        }
    }

    private Rect popup() {
        int w = Math.min(480, this.field_22789 - 30);
        int h = Math.min(270, this.field_22790 - 30);
        return new Rect((this.field_22789 - w) / 2, (this.field_22790 - h) / 2, w, h);
    }

    private void drawVersions(class_332 c, int mx, int my) {
        Rect p = this.popup();
        c.method_25294(0, 0, this.field_22789, this.field_22790, -1342177280);
        Glass.panel(c, p.x(), p.y(), p.w(), p.h(), 0.0f, 1.0f, 0.95f);
        Glass.text(c, "Version ausw\u00e4hlen", p.x() + 15, p.y() + 14, -986895);
        Glass.text(c, "Installation nur f\u00fcr Minecraft 1.21.11", p.x() + 15, p.y() + 30, -6184277);
        Rect close = new Rect(p.x() + p.w() - 39, p.y() + 9, 27, 23);
        Glass.button(c, close, "\u00d7", close.contains(mx, my));
        c.method_44379(p.x() + 8, p.y() + 49, p.x() + p.w() - 8, p.y() + p.h() - 8);
        if (this.details != null) {
            for (int i = 0; i < this.details.versions().size(); ++i) {
                int y = p.y() + 49 + i * 38 - this.versionScroll;
                if (y + 37 < p.y() + 49 || y >= p.y() + p.h() - 8) continue;
                Rect row = new Rect(p.x() + 9, y, p.w() - 18, 35);
                ModrinthService.PackVersion v = this.details.versions().get(i);
                Glass.panel(c, row.x(), row.y(), row.w(), row.h(), row.contains(mx, my) ? 1.0f : 0.0f, i == this.selectedVersion ? 0.65f : 0.0f, 0.75f);
                Glass.text(c, Glass.trim(v.name(), row.w() - 22), row.x() + 10, row.y() + 6, this.compatible(v) ? -986895 : -6184277);
                Glass.text(c, Glass.trim(String.join((CharSequence)", ", v.gameVersions()) + " \u00b7 " + v.date(), row.w() - 22), row.x() + 10, row.y() + 20, this.compatible(v) ? Glass.ACCENT : -6184277);
            }
        }
        c.method_44380();
    }

    private Rect deletePopup() {
        return new Rect((this.field_22789 - Math.min(390, this.field_22789 - 30)) / 2, (this.field_22790 - 142) / 2, Math.min(390, this.field_22789 - 30), 142);
    }

    private void drawDelete(class_332 c, int mx, int my) {
        Rect p = this.deletePopup();
        c.method_25294(0, 0, this.field_22789, this.field_22790, -1342177280);
        Glass.panel(c, p.x(), p.y(), p.w(), p.h(), 0.0f, 1.0f, 0.95f);
        Glass.text(c, "Resource Pack entfernen?", p.x() + 17, p.y() + 18, -986895);
        Glass.text(c, Glass.trim(this.hit.title(), p.w() - 34), p.x() + 17, p.y() + 43, -6184277);
        Glass.text(c, "Nur die von Orange installierte Datei wird gel\u00f6scht.", p.x() + 17, p.y() + 61, -6184277);
        Rect cancel = new Rect(p.x() + 17, p.y() + 96, (p.w() - 44) / 2, 26);
        Rect remove = new Rect(cancel.x() + cancel.w() + 10, cancel.y(), cancel.w(), 26);
        Glass.button(c, cancel, "Abbrechen", cancel.contains(mx, my));
        Glass.button(c, remove, "Entfernen", remove.contains(mx, my));
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        if (click.method_74245() != 0) {
            return super.method_25402(click, doubled);
        }
        double x = click.comp_4798();
        double y = click.comp_4799();
        if (this.versionOpen) {
            int row;
            Rect p = this.popup();
            if (new Rect(p.x() + p.w() - 39, p.y() + 9, 27, 23).contains(x, y)) {
                this.versionOpen = false;
                return true;
            }
            if (x >= (double)(p.x() + 9) && x < (double)(p.x() + p.w() - 9) && y >= (double)(p.y() + 49) && y < (double)(p.y() + p.h() - 8) && this.details != null && (row = (int)((y - (double)(p.y() + 49) + (double)this.versionScroll) / 38.0)) >= 0 && row < this.details.versions().size()) {
                this.selectedVersion = row;
                this.versionOpen = false;
            }
            return true;
        }
        if (this.confirmDelete) {
            Rect p = this.deletePopup();
            Rect cancel = new Rect(p.x() + 17, p.y() + 96, (p.w() - 44) / 2, 26);
            Rect remove = new Rect(cancel.x() + cancel.w() + 10, cancel.y(), cancel.w(), 26);
            if (cancel.contains(x, y)) {
                this.confirmDelete = false;
            }
            if (remove.contains(x, y)) {
                this.confirmDelete = false;
                this.operation(this.service.uninstall(this.hit.id()), "Pack entfernt.");
            }
            return true;
        }
        if (this.back().contains(x, y)) {
            this.method_25419();
            return true;
        }
        if (this.action(0).contains(x, y)) {
            ModrinthService.InstalledPack current = this.service.installed(this.hit.id());
            if (current != null && (this.chosen() == null || this.chosen().id().equals(current.versionId()))) {
                this.update();
            } else {
                this.install();
            }
            return true;
        }
        if (this.action(1).contains(x, y)) {
            if (this.service.installed(this.hit.id()) != null) {
                this.askDelete();
            } else if (this.details != null) {
                this.versionOpen = true;
                this.versionScroll = 0;
            }
            return true;
        }
        if (this.action(2).contains(x, y)) {
            class_407.method_49623((class_437)this, (String)("https://modrinth.com/resourcepack/" + this.hit.id()));
            return true;
        }
        if (!this.loading && this.details == null && new Rect(this.left + 22, this.viewportTop + 48, 120, 24).contains(x, y)) {
            this.load();
            return true;
        }
        if (y >= (double)this.viewportTop && y < (double)this.viewportBottom && this.details != null) {
            if (this.versionButton().contains(x, y)) {
                this.versionOpen = true;
                this.versionScroll = 0;
                return true;
            }
            int iy = this.viewportTop - this.scroll;
            if (this.images.size() > 1 && new Rect(this.left + 22, iy + this.heroH - 30, 28, 23).contains(x, y)) {
                this.imageIndex = Math.floorMod(this.imageIndex - 1, this.images.size());
                return true;
            }
            if (this.images.size() > 1 && new Rect(this.left + this.panelW - 50, iy + this.heroH - 30, 28, 23).contains(x, y)) {
                this.imageIndex = (this.imageIndex + 1) % this.images.size();
                return true;
            }
        }
        return super.method_25402(click, doubled);
    }

    public boolean method_25401(double mx, double my, double horizontal, double vertical) {
        if (this.versionOpen && this.details != null) {
            Rect p = this.popup();
            this.versionScroll = Math.clamp((long)(this.versionScroll - (int)(vertical * 32.0)), 0, Math.max(0, this.details.versions().size() * 38 - (p.h() - 57)));
            return true;
        }
        if (!this.confirmDelete && mx >= (double)this.left && mx < (double)(this.left + this.panelW) && my >= (double)this.viewportTop && my < (double)this.viewportBottom) {
            this.scroll = Math.clamp((long)(this.scroll - (int)(vertical * 35.0)), 0, this.maxScroll());
            return true;
        }
        return super.method_25401(mx, my, horizontal, vertical);
    }

    @Override
    public boolean method_25404(class_11908 input) {
        if (input.comp_4795() == 256 && (this.versionOpen || this.confirmDelete)) {
            this.versionOpen = false;
            this.confirmDelete = false;
            return true;
        }
        return super.method_25404(input);
    }

    private int maxScroll() {
        return Math.max(0, this.contentHeight - (this.viewportBottom - this.viewportTop));
    }

    private static List<String> wrap(String input, int width) {
        ArrayList<String> result = new ArrayList<String>();
        if (input == null || input.isBlank()) {
            return result;
        }
        String bounded = input.length() > 100000 ? input.substring(0, 100000) : input;
        for (String para : bounded.split("\\R", -1)) {
            if (para.isBlank()) {
                result.add("");
                continue;
            }
            String line = "";
            for (String word : para.split("\\s+")) {
                String candidate;
                String string = candidate = line.isEmpty() ? word : line + " " + word;
                if (Glass.textWidth(candidate) > width && !line.isEmpty()) {
                    result.add(line);
                    line = word;
                } else {
                    line = candidate;
                }
                while (Glass.textWidth(line) > width && line.length() > 1) {
                    int lo = 1;
                    int hi = line.length();
                    while (lo < hi) {
                        int mid = (lo + hi + 1) / 2;
                        if (Glass.textWidth(line.substring(0, mid)) <= width) {
                            lo = mid;
                            continue;
                        }
                        hi = mid - 1;
                    }
                    result.add(line.substring(0, lo));
                    line = line.substring(lo);
                }
            }
            result.add(line);
        }
        return result;
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

