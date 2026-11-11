/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_9779
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package de.orange.client.mixin;

import de.orange.client.hud.HudRenderer;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_329.class})
public abstract class CrosshairMixin {
    @Inject(method={"method_1736"}, at={@At(value="HEAD")}, cancellable=true)
    private void orange$crosshair(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        if (HudRenderer.shouldRenderCrosshair()) {
            ci.cancel();
        }
    }
}

