/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10017
 *  net.minecraft.class_11659
 *  net.minecraft.class_12075
 *  net.minecraft.class_1297
 *  net.minecraft.class_1511
 *  net.minecraft.class_1542
 *  net.minecraft.class_1657
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_327$class_6415
 *  net.minecraft.class_4587
 *  net.minecraft.class_5348
 *  net.minecraft.class_5481
 *  net.minecraft.class_765
 *  org.joml.Quaternionfc
 */
package de.orange.client.render;

import de.orange.client.config.ClientConfig;
import de.orange.client.config.RenderSettings;
import de.orange.client.config.VisualSettings;
import de.orange.client.render.HitboxRenderer;
import de.orange.client.render.VisualColors;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.class_10017;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_4587;
import net.minecraft.class_5348;
import net.minecraft.class_5481;
import net.minecraft.class_765;
import org.joml.Quaternionfc;

public final class VisualModules {
    private static Supplier<ClientConfig> config;
    private static final long ANIMATION_START;

    private VisualModules() {
    }

    public static void initialize(Supplier<ClientConfig> currentConfig) {
        config = Objects.requireNonNull(currentConfig, "currentConfig");
        HitboxRenderer.initialize(currentConfig);
    }

    private static ClientConfig current() {
        return config == null ? null : config.get();
    }

    public static void applyOutline(class_1297 entity, class_10017 state) {
        RenderSettings.Target target;
        ClientConfig cfg = VisualModules.current();
        if (cfg == null || !cfg.module((String)"renders").enabled || entity.method_31481()) {
            return;
        }
        if (entity instanceof class_1657) {
            target = cfg.renders.players;
        } else if (entity instanceof class_1511) {
            target = cfg.renders.crystals;
        } else if (entity instanceof class_1542) {
            target = cfg.renders.items;
        } else {
            return;
        }
        if (!target.glow || target.strength <= 0.0f) {
            return;
        }
        double seconds = (double)(System.nanoTime() - ANIMATION_START) / 1.0E9;
        state.field_61821 = VisualColors.outline(target, cfg.renders, seconds, state.field_53325, state.field_53327);
    }

    public static float ambientLight(float vanillaAmbient) {
        ClientConfig cfg = VisualModules.current();
        if (cfg == null || !cfg.module((String)"fullbright").enabled) {
            return vanillaAmbient;
        }
        return vanillaAmbient + (1.0f - vanillaAmbient) * cfg.visuals.fullbrightStrength;
    }

    public static int skyColor(int vanillaColor) {
        ClientConfig cfg = VisualModules.current();
        if (cfg == null || !cfg.module((String)"ambience").enabled) {
            return vanillaColor;
        }
        return VisualColors.mix(vanillaColor, cfg.visuals.ambienceColor, cfg.visuals.ambienceStrength);
    }

    public static float nametagScale() {
        ClientConfig cfg = VisualModules.current();
        return cfg != null && cfg.module((String)"nametags").enabled ? cfg.visuals.nametagScale : 1.0f;
    }

    public static void submitPlayerLabel(class_11659 queue, class_4587 matrices, class_243 position, int yOffset, class_2561 text, boolean seeThrough, int light, double distance, class_12075 camera) {
        ClientConfig cfg = VisualModules.current();
        if (cfg == null || !cfg.module((String)"nametags").enabled) {
            queue.method_73482(matrices, position, yOffset, text, seeThrough, light, distance, camera);
            return;
        }
        if (position == null) {
            return;
        }
        VisualSettings settings = cfg.visuals;
        float scale = 0.025f * settings.nametagScale;
        int color = settings.nametagText;
        int background = settings.nametagBackground ? settings.nametagBackgroundColor : 0;
        class_5481 original = text.method_30937();
        class_5481 recolored = visitor -> original.accept((index, style, codePoint) -> visitor.accept(index, style.method_36139(color & 0xFFFFFF), codePoint));
        matrices.method_22903();
        matrices.method_22904(position.field_1352, position.field_1351 + 0.5, position.field_1350);
        matrices.method_22907((Quaternionfc)camera.field_63081);
        matrices.method_22905(scale, -scale, scale);
        float x = (float)(-class_310.method_1551().field_1772.method_27525((class_5348)text)) / 2.0f;
        int subdued = color & 0xFFFFFF | Integer.MIN_VALUE;
        if (seeThrough) {
            queue.method_73478(matrices, x, (float)yOffset, recolored, false, class_327.class_6415.field_33994, light, subdued, background, 0);
            queue.method_73478(matrices, x, (float)yOffset, recolored, false, class_327.class_6415.field_33993, class_765.method_62228((int)light, (int)2), color, 0, 0);
        } else {
            queue.method_73478(matrices, x, (float)yOffset, recolored, false, class_327.class_6415.field_33993, light, subdued, background, 0);
        }
        matrices.method_22909();
    }

    static {
        ANIMATION_START = System.nanoTime();
    }
}

