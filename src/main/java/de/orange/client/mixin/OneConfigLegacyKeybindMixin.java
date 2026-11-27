package de.orange.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "org.polyfrost.oneconfig.internal.OneConfigConfig")
public final class OneConfigLegacyKeybindMixin {
    @Inject(method = "refreshKeybind", at = @At("TAIL"))
    private static void orangeClient$disableLegacyOpenKey(CallbackInfo ci) {
        try {
            Class<?> config = Class.forName("org.polyfrost.oneconfig.internal.OneConfigConfig");
            java.lang.reflect.Field registered = config.getDeclaredField("registeredKeybind");
            registered.setAccessible(true);
            Object keybind = registered.get(null);
            if (keybind == null) return;

            Class<?> manager = Class.forName("org.polyfrost.oneconfig.api.ui.v1.keybind.KeybindManager");
            for (java.lang.reflect.Method method : manager.getDeclaredMethods()) {
                if (!method.getName().equals("unregister") || method.getParameterCount() != 1) continue;
                method.setAccessible(true);
                method.invoke(null, keybind);
                registered.set(null, null);
                return;
            }
        } catch (Throwable ignored) {
            // OneConfig is optional; never let keybind cleanup crash OrangeClient.
        }
    }
}
