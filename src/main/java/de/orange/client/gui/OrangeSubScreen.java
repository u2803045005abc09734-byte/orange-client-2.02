/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package de.orange.client.gui;

import de.orange.client.OrangeClient;
import de.orange.client.ui.Anim;
import de.orange.client.ui.Glass;
import de.orange.client.ui.GlassScreen;
import de.orange.client.ui.Rect;
import java.io.IOException;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

abstract class OrangeSubScreen
extends GlassScreen {
    protected final class_437 parent;
    protected int left;
    protected int top;
    protected int panelWidth;
    protected int panelHeight;
    protected float dt;
    private final long opened;
    private long last;
    protected String message;

    protected OrangeSubScreen(class_437 parent, String title) {
        super((class_2561)class_2561.method_43470((String)title));
        this.last = this.opened = System.nanoTime();
        this.message = "";
        this.parent = parent;
    }

    protected void method_25426() {
        this.panelWidth = Math.min(720, this.field_22789 - 20);
        this.panelHeight = Math.min(480, this.field_22790 - 20);
        this.left = (this.field_22789 - this.panelWidth) / 2;
        this.top = (this.field_22790 - this.panelHeight) / 2;
    }

    protected float age() {
        return (float)(System.nanoTime() - this.opened) / 1.0E9f;
    }

    protected void begin(class_332 c, int mx, int my, float delta, String icon, String heading, String subtitle) {
        long now = System.nanoTime();
        this.dt = Math.clamp((float)(now - this.last) / 1.0E9f, 0.0f, 0.1f);
        this.last = now;
        float fade = OrangeClient.instance().config().reducedMotion ? 1.0f : Anim.easeOutCubic(Math.clamp(this.age() / 0.3f, 0.0f, 1.0f));
        Glass.begin(c, this.field_22789, this.field_22790, fade);
        Glass.panel(c, this.left, this.top, this.panelWidth, this.panelHeight, 0.0f, 0.0f, fade * 0.93f);
        Glass.icon(c, icon, this.left + 29, this.top + 27, 20, OrangeClient.instance().config().guiAccent);
        Glass.text(c, heading, this.left + 49, this.top + 14, -986895);
        Glass.text(c, Glass.trim(subtitle, this.panelWidth - 102), this.left + 49, this.top + 32, -6184277);
        Rect close = new Rect(this.left + this.panelWidth - 38, this.top + 12, 25, 25);
        Glass.button(c, close, "\u00d7", close.contains(mx, my));
    }

    protected void centered(class_332 c, String text, int cx, int y, int color) {
        Glass.text(c, text, cx - Glass.textWidth(text) / 2, y, color);
    }

    protected boolean closeClick(class_11909 click) {
        if (click.method_74245() == 0 && new Rect(this.left + this.panelWidth - 38, this.top + 12, 25, 25).contains(click.comp_4798(), click.comp_4799())) {
            this.method_25419();
            return true;
        }
        return false;
    }

    protected void save() {
        try {
            OrangeClient.instance().profiles().save();
            this.message = "";
        }
        catch (IOException e) {
            this.message = "Speichern fehlgeschlagen: " + e.getMessage();
        }
    }

    protected void scrollBar(class_332 c, int contentHeight, int viewportTop, int viewportBottom, int scroll) {
        int track = viewportBottom - viewportTop;
        if (contentHeight <= track || track <= 0) {
            return;
        }
        int thumb = Math.max(16, track * track / contentHeight);
        int sy = viewportTop + scroll * (track - thumb) / Math.max(1, contentHeight - track);
        Glass.rounded(c, this.left + this.panelWidth - 7, sy, 2, thumb, 1, 0x77FFFFFF);
    }

    protected void drawMessage(class_332 c, int mx, int my) {
        if (this.message.isEmpty()) {
            return;
        }
        Glass.text(c, Glass.trim(this.message, this.panelWidth - 34), this.left + 17, this.top + this.panelHeight - 48, -17253);
        if (new Rect(this.left + 12, this.top + this.panelHeight - 51, this.panelWidth - 24, 18).contains(mx, my)) {
            c.method_51438(this.field_22793, (class_2561)class_2561.method_43470((String)this.message), mx, my);
        }
    }

    @Override
    protected void method_57734(class_332 c) {
        if (OrangeClient.instance().config().blur) {
            super.method_57734(c);
        }
    }

    @Override
    public void method_25419() {
        this.field_22787.method_1507(this.parent);
    }

    @Override
    public boolean method_25421() {
        return false;
    }
}

