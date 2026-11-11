/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11909
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package de.orange.client.gui;

import de.orange.client.OrangeClient;
import de.orange.client.config.ClientConfig;
import de.orange.client.gui.OrangeSubScreen;
import de.orange.client.hud.HudEditorScreen;
import de.orange.client.module.ClientModule;
import de.orange.client.ui.AnimatedToggle;
import de.orange.client.ui.Glass;
import de.orange.client.ui.Rect;
import java.util.Locale;
import net.minecraft.class_11909;
import net.minecraft.class_332;
import net.minecraft.class_437;

public final class SettingsScreen
extends OrangeSubScreen {
    private static final int[] COLORS = new int[]{-26020, -723984, -4864513, -5644601, -1462069};
    private final String moduleId;
    private ClientModule module;
    private final AnimatedToggle motionToggle = new AnimatedToggle();
    private final AnimatedToggle blurToggle = new AnimatedToggle();
    private int scroll;
    private int dragging = -1;

    public SettingsScreen(class_437 parent) {
        this(parent, null);
    }

    public SettingsScreen(class_437 parent, String moduleId) {
        super(parent, "Einstellungen");
        this.moduleId = moduleId;
    }

    @Override
    protected void method_25426() {
        super.method_25426();
        if (this.moduleId != null) {
            this.module = OrangeClient.instance().modules().find(this.moduleId);
        }
    }

    private int bodyTop() {
        return this.top + 61;
    }

    private int bodyBottom() {
        return this.top + this.panelHeight - 41;
    }

    private boolean hud() {
        return this.module != null && this.module.hud();
    }

    private int contentHeight() {
        return this.module == null ? 292 : (this.hud() ? 255 : (this.moduleId.equals("freelook") ? 116 : 173));
    }

    private int y(int row) {
        return this.bodyTop() + row - this.scroll;
    }

    private Rect line(int row) {
        return new Rect(this.left + 14, this.y(row), this.panelWidth - 28, 47);
    }

    public void method_25394(class_332 c, int mx, int my, float delta) {
        ClientConfig cfg = OrangeClient.instance().config();
        this.begin(c, mx, my, delta, "settings", this.module == null ? "Einstellungen" : this.module.name(), this.module == null ? "Darstellung & Bedienung \u00b7 aktuelles Profil" : this.module.description());
        this.scroll = Math.clamp((long)this.scroll, 0, Math.max(0, this.contentHeight() - (this.bodyBottom() - this.bodyTop())));
        c.method_44379(this.left + 9, this.bodyTop(), this.left + this.panelWidth - 9, this.bodyBottom());
        if (this.module == null) {
            this.toggle(c, this.line(0), "Animationen reduzieren", "Direkte \u00dcberg\u00e4nge und weniger Bewegung", cfg.reducedMotion, this.motionToggle, mx, my);
            this.toggle(c, this.line(54), "Hintergrund-Blur", "Ein wiederverwendeter Blur-Pass pro Men\u00fc", cfg.blur, this.blurToggle, mx, my);
            this.slider(c, 108, "Glas-Deckkraft", cfg.uiOpacity, 0.25f, 0.95f, String.format(Locale.ROOT, "%.0f %%", Float.valueOf(cfg.uiOpacity * 100.0f)), mx, my);
            this.slider(c, 171, "Globale HUD-Gr\u00f6\u00dfe", cfg.hudScale, 0.5f, 2.0f, String.format(Locale.ROOT, "%.0f %%", Float.valueOf(cfg.hudScale * 100.0f)), mx, my);
            this.colorRow(c, 238, "Akzentfarbe", cfg.accent, mx, my);
        } else if (this.hud()) {
            ClientConfig.ModuleState state = cfg.module(this.moduleId);
            this.slider(c, 0, "Elementgr\u00f6\u00dfe", state.scale, 0.5f, 3.0f, String.format(Locale.ROOT, "%.0f %%", Float.valueOf(state.scale * 100.0f)), mx, my);
            this.slider(c, 63, "HUD-Deckkraft", state.opacity, 0.1f, 1.0f, String.format(Locale.ROOT, "%.0f %%", Float.valueOf(state.opacity * 100.0f)), mx, my);
            this.colorRow(c, 132, "Textfarbe", state.color, mx, my);
            Rect edit = new Rect(this.left + 26, this.y(201), this.panelWidth - 52, 34);
            Glass.button(c, edit, "Position im HUD-Editor bearbeiten", edit.contains(mx, my));
        } else if (this.moduleId.equals("freelook")) {
            Glass.panel(c, this.line(0).x(), this.y(0), this.panelWidth - 28, 101, 0.0f, 0.0f, 0.75f);
            Glass.icon(c, "freelook", this.left + 38, this.y(31), 23, cfg.guiAccent);
            Glass.text(c, "Linke Alt-Taste halten", this.left + 63, this.y(17), -986895);
            Glass.text(c, Glass.trim("Die Kamera dreht sich unabh\u00e4ngig von deinem Blick.", this.panelWidth - 94), this.left + 63, this.y(37), -6184277);
            Glass.text(c, Glass.trim("Taste unter Minecraft \u2192 Steuerung \u00e4ndern.", this.panelWidth - 54), this.left + 28, this.y(70), -6184277);
        } else {
            Glass.panel(c, this.line(0).x(), this.y(0), this.panelWidth - 28, 49, 0.0f, 0.0f, 0.75f);
            Glass.text(c, "Fadenkreuz-Farbe", this.left + 27, this.y(16), -986895);
            Glass.text(c, "Verwendet die globale Akzentfarbe.", this.left + 27, this.y(32), -6184277);
            this.colorRow(c, 65, "Akzentfarbe", cfg.accent, mx, my);
        }
        c.method_44380();
        this.scrollBar(c, this.contentHeight(), this.bodyTop(), this.bodyBottom(), this.scroll);
        int footer = this.top + this.panelHeight - 31;
        Glass.text(c, Glass.trim("Profil: " + OrangeClient.instance().profiles().active(), this.panelWidth - 151), this.left + 18, footer + 6, -6184277);
        Rect done = new Rect(this.left + this.panelWidth - 115, footer - 3, 98, 26);
        Glass.button(c, done, "Speichern", done.contains(mx, my));
        this.drawMessage(c, mx, my);
    }

    private void toggle(class_332 c, Rect r, String title, String desc, boolean enabled, AnimatedToggle toggle, int mx, int my) {
        Glass.panel(c, r.x(), r.y(), r.w(), r.h(), r.contains(mx, my) ? 0.4f : 0.0f, 0.0f, 0.76f);
        Glass.text(c, Glass.trim(title, r.w() - 71), r.x() + 12, r.y() + 10, -986895);
        Glass.text(c, Glass.trim(desc, r.w() - 71), r.x() + 12, r.y() + 27, -6184277);
        toggle.draw(c, r.right() - 45, r.y() + 15, enabled, this.dt, OrangeClient.instance().config().reducedMotion);
    }

    private Rect sliderTrack(int row) {
        return new Rect(this.left + 29, this.y(row) + 35, this.panelWidth - 58, 15);
    }

    private void slider(class_332 c, int row, String label, float value, float min, float max, String formatted, int mx, int my) {
        Rect r = new Rect(this.left + 14, this.y(row), this.panelWidth - 28, 56);
        Glass.panel(c, r.x(), r.y(), r.w(), r.h(), r.contains(mx, my) ? 0.25f : 0.0f, 0.0f, 0.76f);
        Glass.text(c, label, this.left + 27, this.y(row) + 9, -986895);
        Glass.text(c, formatted, this.left + this.panelWidth - 29 - Glass.textWidth(formatted), this.y(row) + 9, -6184277);
        Rect track = this.sliderTrack(row);
        float p = Math.clamp((value - min) / (max - min), 0.0f, 1.0f);
        Glass.rounded(c, track.x(), track.y() + 6, track.w(), 3, 1, 0x44FFFFFF);
        Glass.rounded(c, track.x(), track.y() + 6, Math.max(1, Math.round((float)track.w() * p)), 3, 1, OrangeClient.instance().config().guiAccent);
        int thumb = track.x() + Math.round((float)(track.w() - 6) * p);
        Glass.rounded(c, thumb, track.y() + 2, 7, 11, 3, -986895);
    }

    private void colorRow(class_332 c, int row, String title, int selected, int mx, int my) {
        Glass.text(c, title, this.left + 27, this.y(row) + 2, -986895);
        for (int i = 0; i < COLORS.length; ++i) {
            Rect r = new Rect(this.left + 27 + i * 37, this.y(row) + 20, 28, 24);
            Glass.panel(c, r.x(), r.y(), r.w(), r.h(), r.contains(mx, my) ? 1.0f : 0.0f, COLORS[i] == selected ? 1.0f : 0.0f, 0.9f);
            Glass.rounded(c, r.x() + 7, r.y() + 5, 14, 14, 7, COLORS[i]);
        }
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        if (this.closeClick(click)) {
            return true;
        }
        if (click.method_74245() != 0) {
            return super.method_25402(click, doubled);
        }
        ClientConfig cfg = OrangeClient.instance().config();
        if (new Rect(this.left + this.panelWidth - 115, this.top + this.panelHeight - 34, 98, 26).contains(click.comp_4798(), click.comp_4799())) {
            this.save();
            if (this.message.isEmpty()) {
                this.method_25419();
            }
            return true;
        }
        if (click.comp_4799() < (double)this.bodyTop() || click.comp_4799() >= (double)this.bodyBottom()) {
            return super.method_25402(click, doubled);
        }
        if (this.module == null) {
            if (this.line(0).contains(click.comp_4798(), click.comp_4799())) {
                cfg.reducedMotion = !cfg.reducedMotion;
                this.save();
                return true;
            }
            if (this.line(54).contains(click.comp_4798(), click.comp_4799())) {
                cfg.blur = !cfg.blur;
                this.save();
                return true;
            }
            if (this.sliderTrack(108).contains(click.comp_4798(), click.comp_4799())) {
                this.dragging = 0;
                this.adjust(click.comp_4798());
                return true;
            }
            if (this.sliderTrack(171).contains(click.comp_4798(), click.comp_4799())) {
                this.dragging = 1;
                this.adjust(click.comp_4798());
                return true;
            }
            if (this.selectColor(click, 238, false)) {
                return true;
            }
        } else if (this.hud()) {
            if (this.sliderTrack(0).contains(click.comp_4798(), click.comp_4799())) {
                this.dragging = 2;
                this.adjust(click.comp_4798());
                return true;
            }
            if (this.sliderTrack(63).contains(click.comp_4798(), click.comp_4799())) {
                this.dragging = 3;
                this.adjust(click.comp_4798());
                return true;
            }
            if (this.selectColor(click, 132, true)) {
                return true;
            }
            if (new Rect(this.left + 26, this.y(201), this.panelWidth - 52, 34).contains(click.comp_4798(), click.comp_4799())) {
                this.field_22787.method_1507((class_437)new HudEditorScreen(this));
                return true;
            }
        } else if (this.moduleId.equals("crosshair") && this.selectColor(click, 65, false)) {
            return true;
        }
        return super.method_25402(click, doubled);
    }

    private boolean selectColor(class_11909 click, int row, boolean moduleColor) {
        for (int i = 0; i < COLORS.length; ++i) {
            if (!new Rect(this.left + 27 + i * 37, this.y(row) + 20, 28, 24).contains(click.comp_4798(), click.comp_4799())) continue;
            if (moduleColor) {
                OrangeClient.instance().config().module((String)this.moduleId).color = COLORS[i];
            } else {
                OrangeClient.instance().config().accent = COLORS[i];
            }
            this.save();
            return true;
        }
        return false;
    }

    private void adjust(double mouseX) {
        int row = this.dragging == 0 ? 108 : (this.dragging == 1 ? 171 : (this.dragging == 2 ? 0 : 63));
        Rect r = this.sliderTrack(row);
        float p = Math.clamp((float)(mouseX - (double)r.x()) / (float)r.w(), 0.0f, 1.0f);
        ClientConfig cfg = OrangeClient.instance().config();
        switch (this.dragging) {
            case 0: {
                cfg.uiOpacity = 0.25f + p * 0.7f;
                break;
            }
            case 1: {
                cfg.hudScale = 0.5f + p * 1.5f;
                break;
            }
            case 2: {
                cfg.module((String)this.moduleId).scale = 0.5f + p * 2.5f;
                break;
            }
            case 3: {
                cfg.module((String)this.moduleId).opacity = 0.1f + p * 0.9f;
            }
        }
    }

    public boolean method_25403(class_11909 click, double dx, double dy) {
        if (this.dragging >= 0) {
            this.adjust(click.comp_4798());
            return true;
        }
        return super.method_25403(click, dx, dy);
    }

    public boolean method_25406(class_11909 click) {
        if (this.dragging >= 0) {
            this.dragging = -1;
            this.save();
            return true;
        }
        return super.method_25406(click);
    }

    public boolean method_25401(double mx, double my, double horizontal, double vertical) {
        if (mx >= (double)this.left && mx < (double)(this.left + this.panelWidth) && my >= (double)this.bodyTop() && my < (double)this.bodyBottom()) {
            this.scroll = Math.clamp((long)(this.scroll - (int)(vertical * 24.0)), 0, Math.max(0, this.contentHeight() - (this.bodyBottom() - this.bodyTop())));
            return true;
        }
        return super.method_25401(mx, my, horizontal, vertical);
    }

    @Override
    public void method_25419() {
        this.save();
        super.method_25419();
    }

    public void method_25432() {
        this.save();
    }
}

