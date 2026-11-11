/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_156
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_364
 *  net.minecraft.class_437
 *  net.minecraft.class_5348
 *  org.slf4j.LoggerFactory
 */
package de.orange.client.gui;

import de.orange.client.OrangeClient;
import de.orange.client.config.ProfileManager;
import de.orange.client.ui.Glass;
import de.orange.client.ui.GlassScreen;
import de.orange.client.ui.ProfileCard;
import de.orange.client.ui.Rect;
import de.orange.client.ui.SearchField;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_437;
import net.minecraft.class_5348;
import org.slf4j.LoggerFactory;

public final class ProfilesScreen
extends GlassScreen {
    private final class_437 parent;
    private final ProfileManager profiles;
    private final List<Action> actions = new ArrayList<Action>();
    private SearchField name;
    private SearchField importPath;
    private String selected;
    private String status = "Profile speichern Module, HUD und Darstellung.";
    private String deletePending;
    private boolean error;
    private int left;
    private int top;
    private int totalWidth;
    private int listWidth;
    private int bodyTop;
    private int bodyBottom;
    private int contentScroll;
    private int listScroll;
    private long openedAt;
    private long loadAt;
    private long lastFrame;
    private final Map<String, ProfileCard> profileCards = new HashMap<String, ProfileCard>();

    public ProfilesScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"Orange Client \u00b7 Profiles"));
        this.parent = parent;
        this.profiles = OrangeClient.instance().profiles();
        this.selected = this.profiles.active();
    }

    protected void method_25426() {
        if (this.openedAt == 0L) {
            this.openedAt = System.nanoTime();
        }
        this.totalWidth = Math.min(780, this.field_22789 - 24);
        this.left = (this.field_22789 - this.totalWidth) / 2;
        this.top = 18;
        this.listWidth = Math.clamp((long)(this.totalWidth / 3), 102, 215);
        this.bodyTop = 77;
        this.bodyBottom = this.field_22790 - 48;
        String n = this.name == null ? this.selected : this.name.method_1882();
        String p = this.importPath == null ? "" : this.importPath.method_1882();
        int rx = this.left + this.listWidth + 14;
        int rw = this.totalWidth - this.listWidth - 14;
        this.name = new SearchField(this.field_22793, rx + 8, this.bodyTop + 21, rw - 16, 24, (class_2561)class_2561.method_43470((String)"Profilname"));
        this.name.method_1858(false);
        this.name.method_1880(48);
        this.name.method_1852(n);
        this.name.method_1868(-986895);
        this.name.method_47404((class_2561)class_2561.method_43470((String)"Neuer Profilname"));
        this.importPath = new SearchField(this.field_22793, rx + 8, this.bodyTop + 188, rw - 16, 25, (class_2561)class_2561.method_43470((String)"Importpfad"));
        this.importPath.method_1858(false);
        this.importPath.method_1880(2048);
        this.importPath.method_1852(p);
        this.importPath.method_1868(-986895);
        this.importPath.method_47404((class_2561)class_2561.method_43470((String)"Pfad zur .orange-profile.json"));
        this.method_25429((class_364)this.name);
        this.method_25429((class_364)this.importPath);
        this.contentScroll = Math.clamp((long)this.contentScroll, 0, this.maxContentScroll());
        this.listScroll = Math.clamp((long)this.listScroll, 0, this.maxListScroll());
        if (!this.profiles.warnings().isEmpty() && this.status.startsWith("Profile speichern")) {
            this.status = this.profiles.warnings().getFirst();
            this.error = true;
        }
    }

    public void method_25394(class_332 c, int mx, int my, float delta) {
        long now = System.nanoTime();
        float seconds = this.lastFrame == 0L ? 0.016f : Math.clamp((float)(now - this.lastFrame) / 1.0E9f, 0.0f, 0.1f);
        this.lastFrame = now;
        boolean reduced = OrangeClient.instance().config().reducedMotion;
        float loadFlash = this.loadAt == 0L ? 0.0f : 1.0f - Math.clamp((float)(now - this.loadAt) / 4.5E8f, 0.0f, 1.0f);
        float fade = OrangeClient.instance().config().reducedMotion ? 1.0f : Math.clamp((float)(System.nanoTime() - this.openedAt) / 2.6E8f, 0.0f, 1.0f);
        Glass.begin(c, this.field_22789, this.field_22790, fade);
        Glass.title(c, "Profiles", this.left + 4, this.top + 1, 1.7f, -986895);
        Glass.text(c, "Dein Setup. F\u00fcr jede Spielweise.", this.left + 4, this.top + 31, -6184277);
        Rect back = new Rect(this.left + this.totalWidth - 63, this.top + 4, 60, 25);
        Glass.button(c, back, "Zur\u00fcck", back.contains(mx, my));
        int bodyHeight = this.bodyBottom - this.bodyTop;
        Glass.panel(c, this.left, this.bodyTop, this.listWidth, bodyHeight, 0.0f, 0.0f, 1.0f);
        int rx = this.left + this.listWidth + 14;
        int rw = this.totalWidth - this.listWidth - 14;
        Glass.panel(c, rx, this.bodyTop, rw, bodyHeight, 0.0f, loadFlash, 1.0f);
        Glass.text(c, this.profiles.list().size() + " PROFILE", this.left + 12, this.bodyTop + 12, -6184277);
        c.method_44379(this.left + 5, this.bodyTop + 30, this.left + this.listWidth - 5, this.bodyBottom - 5);
        List<String> names = this.profiles.list();
        this.profileCards.keySet().retainAll(names);
        for (int i = 0; i < names.size(); ++i) {
            String value = names.get(i);
            int y = this.bodyTop + 32 + i * 41 - this.listScroll;
            if (y + 38 < this.bodyTop + 30 || y >= this.bodyBottom) continue;
            this.profileCards.computeIfAbsent(value, ignored -> new ProfileCard()).draw(c, new Rect(this.left + 6, y, this.listWidth - 12, 37), value, value.equals(this.profiles.active()), value.equals(this.selected), mx, my >= this.bodyTop + 30 && my < this.bodyBottom - 5 ? (double)my : -1.0, seconds, reduced, OrangeClient.instance().config().guiAccent);
        }
        c.method_44380();
        this.actions.clear();
        c.method_44379(rx + 3, this.bodyTop + 3, rx + rw - 3, this.bodyBottom - 3);
        int y = this.bodyTop - this.contentScroll;
        Glass.text(c, "PROFILNAME", rx + 12, y + 10, -6184277);
        this.name.method_46419(y + 21);
        this.name.method_25394(c, mx, my, delta);
        int gap = 5;
        int bw = (rw - 24 - gap * 2) / 3;
        this.action(c, mx, my, rx + 12, y + 54, bw, "Erstellen", "create");
        this.action(c, mx, my, rx + 12 + bw + gap, y + 54, bw, "Duplizieren", "duplicate");
        this.action(c, mx, my, rx + 12 + (bw + gap) * 2, y + 54, bw, "Umbenennen", "rename");
        Glass.text(c, "AUSGEW\u00c4HLT", rx + 12, y + 91, -6184277);
        Glass.text(c, Glass.trim(this.selected, rw - 24), rx + 12, y + 108, -986895);
        this.action(c, mx, my, rx + 12, y + 128, bw, "Laden", "load");
        this.action(c, mx, my, rx + 12 + bw + gap, y + 128, bw, "Export", "export");
        this.action(c, mx, my, rx + 12 + (bw + gap) * 2, y + 128, bw, this.selected.equals(this.deletePending) ? "Best\u00e4tigen" : "L\u00f6schen", "delete");
        Glass.text(c, "JSON IMPORTIEREN", rx + 12, y + 173, -6184277);
        this.importPath.method_46419(y + 188);
        this.importPath.method_25394(c, mx, my, delta);
        this.action(c, mx, my, rx + 12, y + 220, Math.max(65, rw / 2 - 15), "Importieren", "import");
        this.action(c, mx, my, rx + rw / 2 + 3, y + 220, Math.max(65, rw / 2 - 15), "Ordner \u00f6ffnen", "folder");
        Glass.text(c, Glass.trim("Oder eine Profildatei hierher ziehen.", rw - 24), rx + 12, y + 251, -6184277);
        c.method_44380();
        if (this.maxContentScroll() > 0) {
            int thumb = Math.max(18, bodyHeight * bodyHeight / 272);
            int sy = this.bodyTop + this.contentScroll * (bodyHeight - thumb) / Math.max(1, this.maxContentScroll());
            c.method_25294(rx + rw - 3, sy, rx + rw - 2, sy + thumb, 0x70FFFFFF);
        }
        Glass.text(c, Glass.trim(this.status, this.totalWidth - 8), this.left + 4, this.field_22790 - 31, this.error ? -24165 : -6184277);
        if (my >= this.field_22790 - 39 && my <= this.field_22790 - 15 && this.status.length() > 20) {
            c.method_51447(this.field_22793, this.field_22793.method_1728((class_5348)class_2561.method_43470((String)this.status), Math.max(160, Math.min(360, this.field_22789 - 24))), mx, my);
        } else if (my > this.bodyTop + 3 && my < this.bodyBottom - 3) {
            for (Action action : this.actions) {
                if (!action.rect.contains(mx, my) || Glass.textWidth(action.label) <= action.rect.w() - 14) continue;
                c.method_51438(this.field_22793, (class_2561)Glass.styled(action.label), mx, my);
                break;
            }
        }
    }

    private void action(class_332 c, int mx, int my, int x, int y, int w, String label, String id) {
        Rect rect = new Rect(x, y, w, 23);
        Glass.button(c, rect, Glass.trim(label, w - 8), rect.contains(mx, my));
        this.actions.add(new Action(id, label, rect));
    }

    @Override
    protected void method_57734(class_332 context) {
        if (OrangeClient.instance().config().blur) {
            super.method_57734(context);
        }
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        double my;
        if (click.method_74245() != 0) {
            return super.method_25402(click, doubled);
        }
        double mx = click.comp_4798();
        if (new Rect(this.left + this.totalWidth - 63, this.top + 4, 60, 25).contains(mx, my = click.comp_4799())) {
            this.method_25419();
            return true;
        }
        if (new Rect(this.left + 5, this.bodyTop + 30, this.listWidth - 10, this.bodyBottom - this.bodyTop - 35).contains(mx, my)) {
            int index = (int)((my - (double)this.bodyTop - 32.0 + (double)this.listScroll) / 41.0);
            List<String> entries = this.profiles.list();
            if (index >= 0 && index < entries.size() && new Rect(this.left + 6, this.bodyTop + 32 + index * 41 - this.listScroll, this.listWidth - 12, 37).contains(mx, my)) {
                this.selected = entries.get(index);
                this.name.method_1852(this.selected);
                this.deletePending = null;
                return true;
            }
        }
        if (my > (double)(this.bodyTop + 3) && my < (double)(this.bodyBottom - 3)) {
            for (Action action : this.actions) {
                if (!action.rect.contains(mx, my)) continue;
                this.perform(action.id);
                return true;
            }
            if (this.name.method_25405(mx, my)) {
                this.method_25395((class_364)this.name);
                return this.name.method_25402(click, doubled);
            }
            if (this.importPath.method_25405(mx, my)) {
                this.method_25395((class_364)this.importPath);
                return this.importPath.method_25402(click, doubled);
            }
        }
        this.method_25395(null);
        return true;
    }

    private void perform(String id) {
        this.error = false;
        try {
            switch (id) {
                case "create": {
                    this.profiles.create(this.name.method_1882());
                    this.selected = ProfileManager.validateName(this.name.method_1882());
                    this.status = "Profil erstellt: " + this.selected;
                    break;
                }
                case "duplicate": {
                    this.profiles.duplicate(this.selected, this.name.method_1882());
                    this.selected = ProfileManager.validateName(this.name.method_1882());
                    this.status = "Profil dupliziert: " + this.selected;
                    break;
                }
                case "rename": {
                    this.profiles.rename(this.selected, this.name.method_1882());
                    this.selected = ProfileManager.validateName(this.name.method_1882());
                    this.status = "Profil umbenannt: " + this.selected;
                    break;
                }
                case "load": {
                    this.profiles.load(this.selected);
                    this.status = "Profil geladen: " + this.selected;
                    this.loadAt = this.openedAt = System.nanoTime();
                    break;
                }
                case "export": {
                    Path file = this.profiles.exportProfile(this.selected);
                    this.status = "Export gespeichert: " + String.valueOf(file);
                    break;
                }
                case "delete": {
                    if (!this.selected.equals(this.deletePending)) {
                        this.deletePending = this.selected;
                        this.status = "Zum L\u00f6schen \u201eBest\u00e4tigen\u201c anklicken. Das aktive Profil ist gesch\u00fctzt.";
                        return;
                    }
                    this.profiles.delete(this.selected);
                    this.selected = this.profiles.active();
                    this.name.method_1852(this.selected);
                    this.status = "Profil gel\u00f6scht.";
                    break;
                }
                case "import": {
                    this.selected = this.profiles.importProfile(Path.of(this.importPath.method_1882().strip(), new String[0]));
                    this.name.method_1852(this.selected);
                    this.status = "Profil importiert: " + this.selected;
                    break;
                }
                case "folder": {
                    Files.createDirectories(this.profiles.exportsDirectory(), new FileAttribute[0]);
                    class_156.method_668().method_60932(this.profiles.exportsDirectory());
                    this.status = "Exportordner ge\u00f6ffnet.";
                }
            }
            if (!id.equals("delete")) {
                this.deletePending = null;
            }
            this.listScroll = Math.clamp((long)this.listScroll, 0, this.maxListScroll());
        }
        catch (IOException | RuntimeException exception) {
            this.error = true;
            this.status = exception.getMessage() == null ? "Profilaktion fehlgeschlagen." : exception.getMessage();
        }
    }

    public void method_29638(List<Path> paths) {
        if (paths.size() != 1) {
            this.status = "Bitte genau eine Orange-Profildatei hierher ziehen.";
            this.error = true;
            return;
        }
        this.importPath.method_1852(paths.getFirst().toString());
        this.perform("import");
    }

    public boolean method_25401(double mx, double my, double horizontal, double vertical) {
        if (my >= (double)this.bodyTop && my <= (double)this.bodyBottom) {
            if (mx >= (double)this.left && mx <= (double)(this.left + this.listWidth)) {
                this.listScroll = Math.clamp((long)(this.listScroll - (int)(vertical * 27.0)), 0, this.maxListScroll());
            } else if (mx > (double)(this.left + this.listWidth)) {
                this.contentScroll = Math.clamp((long)(this.contentScroll - (int)(vertical * 27.0)), 0, this.maxContentScroll());
            }
            return true;
        }
        return super.method_25401(mx, my, horizontal, vertical);
    }

    private int maxContentScroll() {
        return Math.max(0, 272 - (this.bodyBottom - this.bodyTop));
    }

    private int maxListScroll() {
        return Math.max(0, this.profiles.list().size() * 41 - (this.bodyBottom - this.bodyTop - 37));
    }

    @Override
    public boolean method_25404(class_11908 input) {
        if (this.name.method_25370() && this.name.method_25404(input)) {
            return true;
        }
        if (this.importPath.method_25370() && this.importPath.method_25404(input)) {
            return true;
        }
        return super.method_25404(input);
    }

    @Override
    public void method_25419() {
        try {
            this.profiles.save();
        }
        catch (IOException exception) {
            LoggerFactory.getLogger((String)"Orange Client").warn("Profil konnte beim Schlie\u00dfen nicht gespeichert werden", (Throwable)exception);
        }
        this.field_22787.method_1507(this.parent);
    }

    @Override
    public boolean method_25421() {
        return false;
    }

    private record Action(String id, String label, Rect rect) {
    }
}

