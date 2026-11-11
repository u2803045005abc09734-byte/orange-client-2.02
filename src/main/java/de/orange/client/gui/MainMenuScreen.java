/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_429
 *  net.minecraft.class_437
 *  net.minecraft.class_500
 *  net.minecraft.class_526
 */
package de.orange.client.gui;

import de.orange.client.OrangeClient;
import de.orange.client.config.ClientConfig;
import de.orange.client.gui.CategoryScreen;
import de.orange.client.gui.EmptyScreen;
import de.orange.client.gui.MenuLayout;
import de.orange.client.gui.ProfilesScreen;
import de.orange.client.gui.ResourcePackScreen;
import de.orange.client.gui.SearchScreen;
import de.orange.client.gui.SettingsScreen;
import de.orange.client.module.Category;
import de.orange.client.ui.Anim;
import de.orange.client.ui.CategoryCard;
import de.orange.client.ui.Glass;
import de.orange.client.ui.GlassScreen;
import de.orange.client.ui.Rect;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_429;
import net.minecraft.class_437;
import net.minecraft.class_500;
import net.minecraft.class_526;

public final class MainMenuScreen
extends GlassScreen {
    private static final String[] DOCK = new String[]{"Wardrobe", "Search", "Social", "Profiles"};
    private static final String[] DOCK_ICONS = new String[]{"wardrobe", "search", "social", "profiles"};
    private final class_437 parent;
    private final boolean titleMode;
    private final CategoryCard[] cards = new CategoryCard[6];
    private final Anim[] dockHover = new Anim[]{new Anim(0.0f), new Anim(0.0f), new Anim(0.0f), new Anim(0.0f)};
    private MenuLayout layout;
    private long opened;
    private long previousFrame;
    private long pendingAt;
    private int selected = -1;

    public MainMenuScreen(class_437 parent) {
        this(parent, false);
    }

    public MainMenuScreen(class_437 parent, boolean titleMode) {
        super((class_2561)class_2561.method_43470((String)"Orange Client"));
        this.parent = parent;
        this.titleMode = titleMode;
        Category[] categories = Category.values();
        for (int i = 0; i < this.cards.length; ++i) {
            this.cards[i] = new CategoryCard(categories[i].icon(), categories[i].displayName());
        }
    }

    protected void method_25426() {
        this.layout = MenuLayout.compute(this.field_22789, this.field_22790, this.titleMode);
        this.opened = this.previousFrame = System.nanoTime();
        this.pendingAt = 0L;
        this.selected = -1;
    }

    @Override
    protected void method_57734(class_332 c) {
        if (OrangeClient.instance().config().blur) {
            super.method_57734(c);
        }
    }

    public void method_25394(class_332 c, int mx, int my, float delta) {
        ClientConfig config = OrangeClient.instance().config();
        Glass.theme(config.accent, config.uiOpacity);
        long now = System.nanoTime();
        float dt = Math.clamp((float)(now - this.previousFrame) / 1.0E9f, 0.0f, 0.1f);
        this.previousFrame = now;
        float age = (float)(now - this.opened) / 1.0E9f;
        float reveal = config.reducedMotion ? 1.0f : Anim.easeOutCubic(Math.clamp(age / 0.4f, 0.0f, 1.0f));
        Glass.begin(c, this.field_22789, this.field_22790, reveal);
        this.drawHeader(c, mx, my);
        int logoY = this.layout.logoY() + Math.round((1.0f - reveal) * 6.0f);
        MainMenuScreen.logo(c, this.field_22789 / 2, logoY, this.layout.logoRadius(), reveal);
        String brand = "ORANGE CLIENT";
        float scale = this.layout.compact() ? 1.35f : 2.0f;
        Glass.title(c, brand, this.field_22789 / 2 - Math.round((float)Glass.textWidth(brand) * scale / 2.0f), this.layout.titleY(), scale, Glass.opacity(-986895, reveal));
        if (!this.layout.compact()) {
            Glass.centered(c, "DEIN SPIEL. DEIN SETUP.", this.field_22789 / 2, this.layout.titleY() + 27, Glass.opacity(-6184277, reveal * 0.8f));
        }
        for (int i = 0; i < this.cards.length; ++i) {
            float cardFade = config.reducedMotion ? 1.0f : Anim.easeOutCubic(Math.clamp((age - (float)i * 0.018f) / 0.38f, 0.0f, 1.0f));
            Rect base = this.layout.cards().get(i);
            Rect animated = new Rect(base.x(), base.y() + Math.round((1.0f - cardFade) * 10.0f), base.w(), base.h());
            this.cards[i].draw(c, animated, mx, my, dt, this.selected == i, config.reducedMotion, cardFade);
        }
        float dockFade = config.reducedMotion ? 1.0f : Anim.easeOutCubic(Math.clamp((age - 0.12f) / 0.4f, 0.0f, 1.0f));
        Rect dock = this.layout.dock();
        Glass.panel(c, dock, 0.0f, 0.0f, dockFade);
        for (int i = 0; i < 4; ++i) {
            Rect r = this.layout.dockItems().get(i);
            float hv = this.dockHover[i].to(r.contains(mx, my) ? 1.0f : 0.0f, dt, config.reducedMotion);
            if (hv > 0.01f) {
                Glass.panel(c, r, hv, 0.0f, dockFade * 0.8f);
            }
            int color = Glass.opacity(Glass.mix(-6184277, -986895, hv), dockFade);
            if (this.layout.compact()) {
                Glass.icon(c, DOCK_ICONS[i], r.centerX(), r.y() + 7, 12, color);
                Glass.centered(c, DOCK[i], r.centerX(), r.y() + 16, color);
                continue;
            }
            Glass.icon(c, DOCK_ICONS[i], r.x() + 17, r.centerY(), 17, color);
            Glass.text(c, DOCK[i], r.x() + 31, r.centerY() - 5, color);
        }
        if (!this.layout.compact()) {
            Glass.text(c, "1.21.11 / FABRIC", 16, this.field_22790 - 15, -9868426);
            String profile = OrangeClient.instance().profiles().active();
            Glass.text(c, "Profil \u00b7 " + Glass.trim(profile, 130), this.field_22789 - Glass.textWidth("Profil \u00b7 " + Glass.trim(profile, 130)) - 16, this.field_22790 - 15, -9868426);
        }
    }

    private void drawHeader(class_332 c, int mx, int my) {
        if (this.titleMode) {
            Rect solo = this.solo();
            Rect multi = this.multi();
            Rect options = this.options();
            Glass.button(c, solo, "Solo", solo.contains(mx, my));
            Glass.button(c, multi, "Server", multi.contains(mx, my));
            Glass.button(c, options, "Optionen", options.contains(mx, my));
        } else {
            Glass.text(c, "ORANGE / CLIENT", 16, 17, -9868426);
        }
        this.headerIcon(c, this.packButton(), "pack", mx, my);
        this.headerIcon(c, this.settingsButton(), "settings", mx, my);
        this.headerIcon(c, this.closeButton(), "close", mx, my);
        Rect hovered = null;
        String tooltip = "";
        if (this.packButton().contains(mx, my)) {
            hovered = this.packButton();
            tooltip = "Resource Packs";
        }
        if (this.settingsButton().contains(mx, my)) {
            hovered = this.settingsButton();
            tooltip = "Darstellung & Einstellungen";
        }
        if (this.closeButton().contains(mx, my)) {
            hovered = this.closeButton();
            String string = tooltip = this.titleMode ? "Spiel beenden" : "Men\u00fc schlie\u00dfen";
        }
        if (hovered != null) {
            int w = Glass.textWidth(tooltip) + 14;
            int x = Math.max(5, Math.min(this.field_22789 - w - 5, hovered.right() - w));
            Glass.panel(c, x, 38, w, 21, 0.0f, 0.0f, 1.0f);
            Glass.text(c, tooltip, x + 7, 44, -6184277);
        }
    }

    private void headerIcon(class_332 c, Rect r, String id, int mx, int my) {
        Glass.panel(c, r, r.contains(mx, my) ? 1.0f : 0.0f, 0.0f, 0.8f);
        Glass.icon(c, id, r.centerX(), r.centerY(), 14, r.contains(mx, my) ? -986895 : -6184277);
    }

    private Rect solo() {
        return new Rect(12, 10, 51, 22);
    }

    private Rect multi() {
        return new Rect(68, 10, 56, 22);
    }

    private Rect options() {
        return new Rect(129, 10, 76, 22);
    }

    private Rect packButton() {
        return new Rect(this.field_22789 - 100, 10, 26, 22);
    }

    private Rect settingsButton() {
        return new Rect(this.field_22789 - 68, 10, 26, 22);
    }

    private Rect closeButton() {
        return new Rect(this.field_22789 - 36, 10, 26, 22);
    }

    public static void logo(class_332 c, int cx, int cy, int radius, float alpha) {
        int i;
        int lastX = cx + radius;
        int lastY = cy;
        for (i = 1; i <= 64; ++i) {
            double angle = Math.PI * 2 * (double)i / 64.0;
            int nx = cx + (int)Math.round(Math.cos(angle) * (double)radius);
            int ny = cy + (int)Math.round(Math.sin(angle) * (double)radius);
            Glass.line(c, lastX, lastY, nx, ny, radius > 18 ? 2 : 1, Glass.alpha(Glass.ACCENT, alpha));
            lastX = nx;
            lastY = ny;
        }
        for (i = 0; i < 8; ++i) {
            double a = Math.PI * 2 * (double)i / 8.0;
            Glass.line(c, cx + (int)(Math.cos(a) * 4.0), cy + (int)(Math.sin(a) * 4.0), cx + (int)(Math.cos(a) * (double)(radius - 5)), cy + (int)(Math.sin(a) * (double)(radius - 5)), radius > 18 ? 2 : 1, Glass.alpha(Glass.ACCENT, alpha * 0.88f));
        }
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        int i;
        if (click.method_74245() != 0 || this.pendingAt != 0L) {
            return super.method_25402(click, doubled);
        }
        double x = click.comp_4798();
        double y = click.comp_4799();
        if (this.packButton().contains(x, y)) {
            this.field_22787.method_1507((class_437)new ResourcePackScreen(this));
            return true;
        }
        if (this.settingsButton().contains(x, y)) {
            this.field_22787.method_1507((class_437)new SettingsScreen(this));
            return true;
        }
        if (this.closeButton().contains(x, y)) {
            if (this.titleMode) {
                this.field_22787.method_1592();
            } else {
                this.method_25419();
            }
            return true;
        }
        if (this.titleMode) {
            if (this.solo().contains(x, y)) {
                this.field_22787.method_1507((class_437)new class_526((class_437)this));
                return true;
            }
            if (this.multi().contains(x, y)) {
                this.field_22787.method_1507((class_437)new class_500((class_437)this));
                return true;
            }
            if (this.options().contains(x, y)) {
                this.field_22787.method_1507((class_437)new class_429((class_437)this, this.field_22787.field_1690));
                return true;
            }
        }
        for (i = 0; i < this.cards.length; ++i) {
            if (!this.cards[i].contains(x, y)) continue;
            this.selected = i;
            this.pendingAt = System.nanoTime() + (OrangeClient.instance().config().reducedMotion ? 0L : 170000000L);
            return true;
        }
        for (i = 0; i < 4; ++i) {
            if (!this.layout.dockItems().get(i).contains(x, y)) continue;
            this.field_22787.method_1507((class_437)(switch (i) {
                case 0 -> new EmptyScreen(this, "wardrobe");
                case 1 -> new SearchScreen(this);
                case 2 -> new EmptyScreen(this, "social");
                default -> new ProfilesScreen(this);
            }));
            return true;
        }
        return super.method_25402(click, doubled);
    }

    public void method_25393() {
        if (this.pendingAt != 0L && System.nanoTime() >= this.pendingAt) {
            this.pendingAt = 0L;
            this.field_22787.method_1507((class_437)new CategoryScreen((class_437)this, Category.values()[this.selected]));
        }
    }

    @Override
    public boolean method_25404(class_11908 key) {
        if (key.comp_4795() == 256 || OrangeClient.instance().menuKey() != null && OrangeClient.instance().menuKey().method_1417(key)) {
            if (!this.titleMode) {
                this.method_25419();
            }
            return true;
        }
        return super.method_25404(key);
    }

    @Override
    public void method_25419() {
        if (!this.titleMode) {
            OrangeClient.instance().save();
            this.field_22787.method_1507(this.parent);
        }
    }

    @Override
    public boolean method_25421() {
        return false;
    }
}

