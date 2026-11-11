/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1304
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_746
 *  net.minecraft.class_9779
 *  org.joml.Matrix3x2fStack
 */
package de.orange.client.hud;

import de.orange.client.config.ClientConfig;
import de.orange.client.hud.HudEditorScreen;
import de.orange.client.hud.HudGeometry;
import de.orange.client.ui.Glass;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_746;
import net.minecraft.class_9779;
import org.joml.Matrix3x2fStack;

public final class HudRenderer {
    public static final List<String> HUD_IDS = List.of("armor", "fps", "coordinates", "keystrokes", "sprint", "clock");
    private static final class_1304[] SLOTS = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};
    private static final String[] SLOT_NAMES = new String[]{"Helm", "Brust", "Beine", "Schuhe"};
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final Supplier<ClientConfig> config;
    private static Supplier<ClientConfig> activeConfig;
    private class_1799[] samples;
    private final float[] keyAnimation = new float[7];
    private final boolean[] keyDown = new boolean[7];
    private int lastX = Integer.MIN_VALUE;
    private int lastY;
    private int lastZ;
    private String coordinateText = "X 128   Y 64   Z -256";
    private long lastRender;
    private long clockSecond = -1L;
    private String clockText = "";

    public HudRenderer(Supplier<ClientConfig> config) {
        this.config = config;
        activeConfig = config;
    }

    public HudGeometry.Bounds bounds(String id, int screenWidth, int screenHeight) {
        ClientConfig cfg = this.config.get();
        ClientConfig.ModuleState state = cfg.module(id);
        int w = switch (id) {
            case "armor" -> 150;
            case "coordinates" -> Math.max(176, Glass.textWidth(this.coordinates()) + 28);
            case "keystrokes" -> 94;
            case "sprint" -> 120;
            case "clock" -> 102;
            default -> 92;
        };
        int h = switch (id) {
            case "armor" -> 112;
            case "keystrokes" -> 106;
            default -> 30;
        };
        return HudGeometry.place(state.x, state.y, w, h, state.scale * cfg.hudScale, screenWidth, screenHeight);
    }

    public void render(class_332 c, class_9779 tickCounter) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || mc.field_1690.field_1842 || mc.field_1755 instanceof HudEditorScreen) {
            return;
        }
        this.renderModules(c, mc.method_22683().method_4486(), mc.method_22683().method_4502(), false);
        if (HudRenderer.shouldRenderCrosshair()) {
            this.drawCrosshair(c, mc.method_22683().method_4486(), mc.method_22683().method_4502());
        }
    }

    public void renderModules(class_332 c, int screenWidth, int screenHeight, boolean preview) {
        ClientConfig cfg = this.config.get();
        Glass.theme(cfg.accent, cfg.uiOpacity);
        long now = System.nanoTime();
        float dt = this.lastRender == 0L ? 0.016f : Math.min(0.1f, (float)(now - this.lastRender) / 1.0E9f);
        this.lastRender = now;
        class_310 mc = class_310.method_1551();
        this.keyDown[0] = mc.field_1690.field_1894.method_1434();
        this.keyDown[1] = mc.field_1690.field_1913.method_1434();
        this.keyDown[2] = mc.field_1690.field_1881.method_1434();
        this.keyDown[3] = mc.field_1690.field_1849.method_1434();
        this.keyDown[4] = mc.field_1690.field_1903.method_1434();
        this.keyDown[5] = mc.field_1690.field_1832.method_1434();
        this.keyDown[6] = mc.field_1690.field_1867.method_1434();
        float response = cfg.reducedMotion ? 1.0f : (float)(1.0 - Math.exp(-20.0f * dt));
        for (int i = 0; i < this.keyDown.length; ++i) {
            int n = i;
            this.keyAnimation[n] = this.keyAnimation[n] + ((float)(this.keyDown[i] ? 1 : 0) - this.keyAnimation[i]) * response;
        }
        for (String id : HUD_IDS) {
            if (!cfg.module((String)id).enabled) continue;
            this.draw(c, id, this.bounds(id, screenWidth, screenHeight), preview);
        }
    }

    public static boolean shouldRenderCrosshair() {
        class_310 mc = class_310.method_1551();
        return activeConfig != null && HudRenderer.activeConfig.get().module((String)"crosshair").enabled && mc.field_1724 != null && mc.field_1690.method_31044().method_31034() && !mc.field_1690.field_1842 && !mc.field_1705.method_53531().method_53536();
    }

    private void drawCrosshair(class_332 c, int width, int height) {
        ClientConfig cfg = this.config.get();
        ClientConfig.ModuleState state = cfg.module("crosshair");
        int x = width / 2;
        int y = height / 2;
        int color = cfg.accent;
        int reach = Math.max(3, Math.round(5.0f * state.scale));
        c.method_25294(x - reach - 1, y - 1, x - 1, y + 2, -1342177280);
        c.method_25294(x + 2, y - 1, x + reach + 2, y + 2, -1342177280);
        c.method_25294(x - 1, y - reach - 1, x + 2, y - 1, -1342177280);
        c.method_25294(x - 1, y + 2, x + 2, y + reach + 2, -1342177280);
        c.method_25294(x - reach, y, x - 1, y + 1, color);
        c.method_25294(x + 2, y, x + reach + 1, y + 1, color);
        c.method_25294(x, y - reach, x + 1, y - 1, color);
        c.method_25294(x, y + 2, x + 1, y + reach + 1, color);
    }

    private void draw(class_332 c, String id, HudGeometry.Bounds b, boolean preview) {
        ClientConfig cfg = this.config.get();
        ClientConfig.ModuleState state = cfg.module(id);
        class_310 mc = class_310.method_1551();
        Matrix3x2fStack matrices = c.method_51448();
        matrices.pushMatrix();
        matrices.translate((float)b.x(), (float)b.y());
        matrices.scale(b.scale(), b.scale());
        int rawW = Math.round((float)b.width() / b.scale());
        int rawH = Math.round((float)b.height() / b.scale());
        Glass.panel(c, 0, 0, rawW, rawH, 0.0f, 0.0f, state.opacity);
        int text = state.color;
        switch (id) {
            case "fps": {
                Glass.icon(c, "fps", 15, 15, 12, cfg.accent);
                Glass.text(c, mc.method_47599() + " FPS", 28, 10, text);
                break;
            }
            case "coordinates": {
                Glass.icon(c, "coordinates", 13, 15, 12, cfg.accent);
                Glass.text(c, Glass.trim(this.coordinates(), rawW - 30), 26, 10, text);
                break;
            }
            case "clock": {
                long second = System.currentTimeMillis() / 1000L;
                if (second != this.clockSecond) {
                    this.clockSecond = second;
                    this.clockText = LocalTime.now().format(TIME);
                }
                Glass.icon(c, "clock", 14, 15, 12, cfg.accent);
                Glass.text(c, this.clockText, 27, 10, text);
                break;
            }
            case "sprint": {
                String label = mc.field_1724 != null && mc.field_1724.method_5624() ? "Sprinten" : "Gehen";
                Glass.icon(c, "player", 14, 15, 12, cfg.accent);
                Glass.text(c, label, 28, 10, text);
                break;
            }
            case "armor": {
                Glass.text(c, "AUSR\u00dcSTUNG", 12, 9, cfg.accent);
                if (this.samples == null && preview && mc.field_1724 == null) {
                    this.samples = new class_1799[]{new class_1799((class_1935)class_1802.field_8805), new class_1799((class_1935)class_1802.field_8058), new class_1799((class_1935)class_1802.field_8348), new class_1799((class_1935)class_1802.field_8285)};
                }
                for (int i = 0; i < SLOTS.length; ++i) {
                    class_1799 item;
                    int y = 25 + i * 20;
                    class_1799 class_17992 = mc.field_1724 != null ? mc.field_1724.method_6118(SLOTS[i]) : (item = preview ? this.samples[i] : class_1799.field_8037);
                    if (item.method_7960()) {
                        Glass.text(c, SLOT_NAMES[i], 13, y + 3, -6184277);
                        Glass.text(c, "\u2014", rawW - 19, y + 3, -6184277);
                        continue;
                    }
                    c.method_51427(item, 10, y - 1);
                    int remaining = item.method_7936() - item.method_7919();
                    Glass.text(c, (String)(item.method_7963() ? remaining + " / " + item.method_7936() : "Angelegt"), 34, y, text);
                    if (!item.method_7963()) continue;
                    int barWidth = rawW - 48;
                    c.method_25294(35, y + 13, 35 + barWidth, y + 15, 1429156923);
                    int colored = Math.clamp((long)((int)((float)(barWidth * remaining) / (float)item.method_7936())), 0, barWidth);
                    c.method_25294(35, y + 13, 35 + colored, y + 15, remaining < item.method_7936() / 4 ? -33424 : cfg.accent);
                }
                break;
            }
            case "keystrokes": {
                this.key(c, "W", 35, 6, 24, this.keyAnimation[0], cfg.accent, text);
                this.key(c, "A", 9, 31, 24, this.keyAnimation[1], cfg.accent, text);
                this.key(c, "S", 35, 31, 24, this.keyAnimation[2], cfg.accent, text);
                this.key(c, "D", 61, 31, 24, this.keyAnimation[3], cfg.accent, text);
                this.key(c, "SPACE", 9, 56, 76, this.keyAnimation[4], cfg.accent, text);
                this.key(c, "SHIFT", 9, 80, 36, this.keyAnimation[5], cfg.accent, text);
                this.key(c, "CTRL", 49, 80, 36, this.keyAnimation[6], cfg.accent, text);
            }
        }
        matrices.popMatrix();
    }

    private String coordinates() {
        class_746 player = class_310.method_1551().field_1724;
        if (player == null) {
            return "X 128   Y 64   Z -256";
        }
        int x = player.method_31477();
        int y = player.method_31478();
        int z = player.method_31479();
        if (x != this.lastX || y != this.lastY || z != this.lastZ) {
            this.coordinateText = "X " + x + "   Y " + y + "   Z " + z;
            this.lastX = x;
            this.lastY = y;
            this.lastZ = z;
        }
        return this.coordinateText;
    }

    private void key(class_332 c, String label, int x, int y, int w, float pressed, int accent, int text) {
        Glass.panel(c, x, y, w, 21, pressed * 0.6f, pressed * 0.6f, 0.3f + pressed * 0.15f);
        if (pressed > 0.01f) {
            c.method_25294(x + 5, y + 19, x + w - 5, y + 20, (int)(pressed * 255.0f) << 24 | accent & 0xFFFFFF);
        }
        Glass.text(c, label, x + (w - Glass.textWidth(label)) / 2, y + 6, pressed > 0.5f ? accent : text);
    }
}

