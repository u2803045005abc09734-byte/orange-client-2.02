/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.minecraft.class_304
 *  net.minecraft.class_304$class_11900
 *  net.minecraft.class_310
 *  net.minecraft.class_5498
 */
package de.orange.client.module;

import de.orange.client.config.ClientConfig;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_5498;

public final class FreelookController {
    private static Supplier<ClientConfig> config;
    private static class_304 hold;
    private static boolean active;
    private static class_5498 previousPerspective;
    private static float yaw;
    private static float pitch;

    private FreelookController() {
    }

    public static void initialize(Supplier<ClientConfig> settings) {
        config = settings;
        if (hold == null) {
            hold = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.orange.freelook", 342, class_304.class_11900.field_62555));
        }
    }

    public static void tick(class_310 mc) {
        boolean allowed;
        boolean bl = allowed = config != null && hold != null && FreelookController.config.get().module((String)"freelook").enabled && hold.method_1434() && mc.field_1724 != null && mc.field_1687 != null && mc.field_1755 == null && mc.method_1569() && !mc.field_1724.method_6113();
        if (!allowed) {
            FreelookController.stop();
            return;
        }
        if (!active) {
            yaw = mc.field_1724.method_36454();
            pitch = mc.field_1724.method_36455();
            previousPerspective = mc.field_1690.method_31044();
            mc.field_1690.method_31043(class_5498.field_26665);
            active = true;
        }
        if (mc.field_1690.method_31044() != class_5498.field_26665) {
            mc.field_1690.method_31043(class_5498.field_26665);
        }
    }

    public static boolean active() {
        return active;
    }

    public static float yaw() {
        return yaw;
    }

    public static float pitch() {
        return pitch;
    }

    public static void turn(double dx, double dy) {
        yaw = (float)((double)yaw + dx * 0.15);
        pitch = Math.clamp((float)((double)pitch + dy * 0.15), -90.0f, 90.0f);
    }

    public static void stop() {
        if (!active) {
            return;
        }
        active = false;
        class_310 mc = class_310.method_1551();
        if (previousPerspective != null) {
            mc.field_1690.method_31043(previousPerspective);
        }
        previousPerspective = null;
    }
}

