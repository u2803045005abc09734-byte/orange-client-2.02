/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
 *  net.minecraft.class_11658
 *  net.minecraft.class_12249
 *  net.minecraft.class_1297
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_259
 *  net.minecraft.class_265
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_4588
 *  net.minecraft.class_9974
 */
package de.orange.client.render;

import de.orange.client.config.ClientConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.class_11658;
import net.minecraft.class_12249;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_9974;

public final class HitboxRenderer {
    private static final RenderStateDataKey<Frame> FRAME = RenderStateDataKey.create(() -> "orange:hitboxes");
    private static final double RANGE_SQUARED = 4096.0;
    private static boolean initialized;

    private HitboxRenderer() {
    }

    public static void initialize(Supplier<ClientConfig> config) {
        Objects.requireNonNull(config, "config");
        if (initialized) {
            return;
        }
        initialized = true;
        WorldRenderEvents.END_EXTRACTION.register(context -> HitboxRenderer.extract(context, (ClientConfig)config.get()));
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(HitboxRenderer::draw);
    }

    private static void extract(WorldExtractionContext context, ClientConfig config) {
        if (context.worldState() == null) {
            return;
        }
        class_11658 state = context.worldState();
        state.setData(FRAME, null);
        if (config == null || !config.module((String)"hitboxes").enabled || context.world() == null || context.camera() == null || context.frustum() == null) {
            return;
        }
        class_4184 camera = context.camera();
        class_243 cameraPos = camera.method_71156();
        float tickProgress = context.tickCounter().method_60637(false);
        ArrayList<class_265> shapes = new ArrayList<class_265>();
        for (class_1297 entity : context.world().method_18112()) {
            class_243 position;
            if (entity.method_31481() || entity.method_7325() || entity.method_5767() || entity == camera.method_19331() && !camera.method_19333() || (position = entity.method_30950(tickProgress)).method_1025(cameraPos) > 4096.0 || !entity.method_5727(cameraPos.field_1352, cameraPos.field_1351, cameraPos.field_1350)) continue;
            class_238 box = entity.method_5829().method_997(position.method_1020(entity.method_73189()));
            if (!context.frustum().method_23093(box)) continue;
            shapes.add(class_259.method_1078((class_238)box));
        }
        if (!shapes.isEmpty()) {
            state.setData(FRAME, (Object)new Frame(List.copyOf(shapes), config.visuals.hitboxColor, Math.clamp(config.visuals.hitboxWidth, 0.5f, 4.0f)));
        }
    }

    private static void draw(WorldRenderContext context) {
        class_11658 state = context.worldState();
        if (state == null || context.matrices() == null || context.consumers() == null || state.field_63082 == null || state.field_63082.field_63078 == null) {
            return;
        }
        Frame frame = (Frame)state.getData(FRAME);
        if (frame == null) {
            return;
        }
        class_243 camera = context.worldState().field_63082.field_63078;
        class_4588 consumer = context.consumers().method_73477(class_12249.method_76015());
        for (class_265 shape : frame.shapes()) {
            class_9974.method_62296((class_4587)context.matrices(), (class_4588)consumer, (class_265)shape, (double)(-camera.field_1352), (double)(-camera.field_1351), (double)(-camera.field_1350), (int)frame.color(), (float)frame.width());
        }
    }

    private record Frame(List<class_265> shapes, int color, float width) {
    }
}

