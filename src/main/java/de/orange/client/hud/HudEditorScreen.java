/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package de.orange.client.hud;

import de.orange.client.OrangeClient;
import de.orange.client.config.ClientConfig;
import de.orange.client.hud.HudGeometry;
import de.orange.client.hud.HudRenderer;
import de.orange.client.ui.Glass;
import de.orange.client.ui.GlassScreen;
import de.orange.client.ui.Rect;
import de.orange.client.ui.Slider;
import java.io.IOException;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

public final class HudEditorScreen
extends GlassScreen {
    private final class_437 parent;
    private String selected;
    private String dragging;
    private double grabX;
    private double grabY;
    private boolean showInspector = true;
    private boolean dirty;
    private Slider scaleSlider;
    private Slider opacitySlider;
    private Slider globalSlider;
    private String message = "";
    private boolean error;
    private long messageUntil;

    public HudEditorScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"Orange Client \u00b7 HUD-Editor"));
        this.parent = parent;
    }

    public HudEditorScreen(class_437 parent, String selectedModule) {
        this(parent);
        this.selected = selectedModule != null && HudRenderer.HUD_IDS.contains(selectedModule) ? selectedModule : null;
    }

    private OrangeClient orange() {
        return OrangeClient.instance();
    }

    private ClientConfig config() {
        return this.orange().config();
    }

    private HudRenderer hud() {
        return this.orange().hud();
    }

    private Rect inspector() {
        int w = Math.min(188, Math.max(148, this.field_22789 / 3));
        return new Rect(this.field_22789 - w - 12, 40, w, 164);
    }

    private Rect done() {
        return new Rect(this.field_22789 - 91, this.field_22790 - 30, 79, 22);
    }

    private Rect resetAll() {
        return new Rect(12, this.field_22790 - 30, 107, 22);
    }

    private Rect details() {
        return new Rect(127, this.field_22790 - 30, 85, 22);
    }

    protected void method_25426() {
        if (this.selected == null || !this.config().module((String)this.selected).enabled) {
            this.selected = HudRenderer.HUD_IDS.stream().filter(id -> this.config().module((String)id).enabled).findFirst().orElse(null);
        }
        this.rebuildSliders();
    }

    private void rebuildSliders() {
        Rect p = this.inspector();
        if (this.selected != null) {
            ClientConfig.ModuleState state = this.config().module(this.selected);
            this.scaleSlider = new Slider(new Rect(p.x() + 12, p.y() + 40, p.w() - 24, 25), 0.5f, 3.0f, state.scale).onChange(v -> {
                this.config().module((String)this.selected).scale = v.floatValue();
                this.dirty = true;
            });
            this.opacitySlider = new Slider(new Rect(p.x() + 12, p.y() + 70, p.w() - 24, 25), 0.1f, 1.0f, state.opacity).onChange(v -> {
                this.config().module((String)this.selected).opacity = v.floatValue();
                this.dirty = true;
            });
        }
        this.globalSlider = new Slider(new Rect(p.x() + 12, p.y() + 100, p.w() - 24, 25), 0.5f, 2.0f, this.config().hudScale).onChange(v -> {
            this.config().hudScale = v.floatValue();
            this.dirty = true;
        });
    }

    public void method_25394(class_332 c, int mx, int my, float delta) {
        Glass.theme(this.config().accent, this.config().uiOpacity);
        c.method_25294(0, 0, this.field_22789, this.field_22790, 1611008012);
        for (int x = 0; x < this.field_22789; x += 20) {
            c.method_25294(x, 0, x + 1, this.field_22790, 372126526);
        }
        for (int y = 0; y < this.field_22790; y += 20) {
            c.method_25294(0, y, this.field_22789, y + 1, 372126526);
        }
        this.hud().renderModules(c, this.field_22789, this.field_22790, true);
        for (String id2 : HudRenderer.HUD_IDS) {
            if (!this.config().module((String)id2).enabled) continue;
            HudGeometry.Bounds b = this.hud().bounds(id2, this.field_22789, this.field_22790);
            if (!id2.equals(this.selected) && !b.contains(mx, my)) continue;
            int color = id2.equals(this.selected) ? this.config().accent : Glass.alpha(-986895, 0.6f);
            c.method_25294(b.x(), b.y(), b.x() + b.width(), b.y() + 1, color);
            c.method_25294(b.x(), b.y() + b.height() - 1, b.x() + b.width(), b.y() + b.height(), color);
            c.method_25294(b.x(), b.y(), b.x() + 1, b.y() + b.height(), color);
            c.method_25294(b.x() + b.width() - 1, b.y(), b.x() + b.width(), b.y() + b.height(), color);
            if (!id2.equals(this.dragging)) continue;
            if (Math.abs(b.x() + b.width() / 2 - this.field_22789 / 2) <= 2) {
                c.method_25294(this.field_22789 / 2, 0, this.field_22789 / 2 + 1, this.field_22790, Glass.alpha(color, 0.35f));
            }
            if (Math.abs(b.y() + b.height() / 2 - this.field_22790 / 2) > 2) continue;
            c.method_25294(0, this.field_22790 / 2, this.field_22789, this.field_22790 / 2 + 1, Glass.alpha(color, 0.35f));
        }
        if (this.showInspector) {
            this.renderInspector(c, mx, my);
        }
        Glass.button(c, this.done(), "Fertig", this.done().contains(mx, my));
        Glass.button(c, this.resetAll(), "Alles zur\u00fccksetzen", this.resetAll().contains(mx, my));
        Glass.button(c, this.details(), this.showInspector ? "Details aus" : "Details ein", this.details().contains(mx, my));
        if (this.field_22789 > 550) {
            Glass.text(c, Glass.trim("HUD-Editor \u00b7 Ziehen \u00b7 Tab: Details", this.field_22789 - 325), 226, this.field_22790 - 23, -6184277);
        }
        if (!this.message.isEmpty() && System.currentTimeMillis() < this.messageUntil) {
            Glass.text(c, Glass.trim(this.message, Math.max(50, this.field_22789 - 30)), 16, this.field_22790 - 45, this.error ? -25712 : -986895);
        }
        if (HudRenderer.HUD_IDS.stream().noneMatch(id -> this.config().module((String)id).enabled)) {
            Glass.centered(c, "Aktiviere zuerst ein HUD-Modul in der Modul\u00fcbersicht.", this.field_22789 / 2, this.field_22790 / 2, -6184277);
        }
    }

    private void renderInspector(class_332 c, int mx, int my) {
        Rect p = this.inspector();
        Glass.panel(c, p, 0.0f, 0.0f, 1.0f);
        Glass.text(c, "AUSGEW\u00c4HLT", p.x() + 12, p.y() + 10, -6184277);
        if (this.selected == null) {
            Glass.text(c, "Kein HUD aktiv", p.x() + 12, p.y() + 31, -986895);
            return;
        }
        String name = this.orange().modules().find(this.selected).name();
        Glass.text(c, Glass.trim(name, p.w() - 42), p.x() + 12, p.y() + 25, -986895);
        Rect reset = new Rect(p.right() - 32, p.y() + 8, 22, 22);
        Glass.button(c, reset, "R", reset.contains(mx, my));
        this.scaleSlider.draw(c, "Gr\u00f6\u00dfe", mx, my);
        this.opacitySlider.draw(c, "Deckkraft", mx, my);
        this.globalSlider.draw(c, "Global", mx, my);
        Glass.text(c, "Textfarbe", p.x() + 12, p.y() + 143, -6184277);
        int[] colors = this.colors();
        for (int i = 0; i < colors.length; ++i) {
            int x = p.right() - 75 + i * 17;
            Glass.rounded(c, x, p.y() + 139, 13, 13, 4, colors[i]);
            if (this.config().module((String)this.selected).color != colors[i]) continue;
            c.method_25294(x + 3, p.y() + 154, x + 10, p.y() + 155, -986895);
        }
    }

    private int[] colors() {
        return new int[]{-723984, this.config().accent, -4668975, -7285067};
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        if (click.method_74245() != 0) {
            return super.method_25402(click, doubled);
        }
        double mx = click.comp_4798();
        double my = click.comp_4799();
        if (this.done().contains(mx, my)) {
            this.method_25419();
            return true;
        }
        if (this.resetAll().contains(mx, my)) {
            ClientConfig defaults = ClientConfig.defaults();
            for (String id : HudRenderer.HUD_IDS) {
                this.resetState(id, defaults);
            }
            this.config().hudScale = 1.0f;
            this.dirty = true;
            this.rebuildSliders();
            this.save();
            this.feedback("HUD-Layout zur\u00fcckgesetzt", false);
            return true;
        }
        if (this.details().contains(mx, my)) {
            this.showInspector = !this.showInspector;
            return true;
        }
        Rect p = this.inspector();
        if (this.showInspector && p.contains(mx, my)) {
            if (this.selected != null) {
                if (new Rect(p.right() - 32, p.y() + 8, 22, 22).contains(mx, my)) {
                    this.resetState(this.selected, ClientConfig.defaults());
                    this.dirty = true;
                    this.rebuildSliders();
                    this.save();
                    return true;
                }
                if (this.scaleSlider.mouseClicked(mx, my, 0) || this.opacitySlider.mouseClicked(mx, my, 0) || this.globalSlider.mouseClicked(mx, my, 0)) {
                    return true;
                }
                int[] colors = this.colors();
                for (int i = 0; i < colors.length; ++i) {
                    if (!new Rect(p.right() - 75 + i * 17, p.y() + 139, 13, 16).contains(mx, my)) continue;
                    this.config().module((String)this.selected).color = colors[i];
                    this.dirty = true;
                    this.save();
                    return true;
                }
            }
            return true;
        }
        for (int i = HudRenderer.HUD_IDS.size() - 1; i >= 0; --i) {
            String id = HudRenderer.HUD_IDS.get(i);
            HudGeometry.Bounds b = this.hud().bounds(id, this.field_22789, this.field_22790);
            if (!this.config().module((String)id).enabled || !b.contains(mx, my)) continue;
            this.selected = id;
            this.dragging = id;
            this.grabX = mx - (double)b.x();
            this.grabY = my - (double)b.y();
            this.rebuildSliders();
            return true;
        }
        return super.method_25402(click, doubled);
    }

    public boolean method_25403(class_11909 click, double dx, double dy) {
        if (this.showInspector && this.selected != null && (this.scaleSlider.mouseDragged(click.comp_4798(), click.comp_4799()) || this.opacitySlider.mouseDragged(click.comp_4798(), click.comp_4799()) || this.globalSlider.mouseDragged(click.comp_4798(), click.comp_4799()))) {
            return true;
        }
        if (this.dragging == null) {
            return super.method_25403(click, dx, dy);
        }
        HudGeometry.Bounds b = this.hud().bounds(this.dragging, this.field_22789, this.field_22790);
        ClientConfig.ModuleState s = this.config().module(this.dragging);
        int x = Math.clamp((long)((int)Math.round(click.comp_4798() - this.grabX)), 0, Math.max(0, this.field_22789 - b.width()));
        int y = Math.clamp((long)((int)Math.round(click.comp_4799() - this.grabY)), 0, Math.max(0, this.field_22790 - b.height()));
        if (Math.abs(x + b.width() / 2 - this.field_22789 / 2) < 5) {
            x = (this.field_22789 - b.width()) / 2;
        }
        if (Math.abs(y + b.height() / 2 - this.field_22790 / 2) < 5) {
            y = (this.field_22790 - b.height()) / 2;
        }
        s.x = x;
        s.y = y;
        this.dirty = true;
        return true;
    }

    public boolean method_25406(class_11909 click) {
        boolean used = this.dragging != null;
        this.dragging = null;
        if (this.scaleSlider != null) {
            used |= this.scaleSlider.mouseReleased();
        }
        if (this.opacitySlider != null) {
            used |= this.opacitySlider.mouseReleased();
        }
        if (this.globalSlider != null) {
            used |= this.globalSlider.mouseReleased();
        }
        if (this.dirty) {
            this.save();
        }
        return used || super.method_25406(click);
    }

    @Override
    public boolean method_25404(class_11908 input) {
        if (input.comp_4795() == 258) {
            this.showInspector = !this.showInspector;
            return true;
        }
        return super.method_25404(input);
    }

    private void resetState(String id, ClientConfig defaults) {
        ClientConfig.ModuleState s = this.config().module(id);
        ClientConfig.ModuleState d = defaults.module(id);
        s.x = d.x;
        s.y = d.y;
        s.scale = d.scale;
        s.opacity = d.opacity;
        s.color = d.color;
    }

    private void save() {
        if (!this.dirty) {
            return;
        }
        try {
            this.orange().profiles().save();
            this.dirty = false;
        }
        catch (IOException ex) {
            this.feedback("Speichern fehlgeschlagen: " + ex.getMessage(), true);
        }
    }

    private void feedback(String text, boolean isError) {
        this.message = text;
        this.error = isError;
        this.messageUntil = System.currentTimeMillis() + 6000L;
    }

    public void method_25432() {
        this.save();
    }

    @Override
    public void method_25419() {
        this.save();
        this.field_22787.method_1507(this.parent);
    }

    @Override
    public boolean method_25421() {
        return false;
    }

    @Override
    public void method_25420(class_332 c, int mx, int my, float delta) {
        if (this.field_22787.field_1687 == null) {
            super.method_25420(c, mx, my, delta);
        }
    }
}

