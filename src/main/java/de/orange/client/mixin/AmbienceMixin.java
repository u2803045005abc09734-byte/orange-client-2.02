/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_12076
 *  net.minecraft.class_2874$class_12326
 *  net.minecraft.class_4184
 *  net.minecraft.class_638
 *  net.minecraft.class_9975
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package de.orange.client.mixin;

import de.orange.client.render.VisualModules;
import net.minecraft.class_12076;
import net.minecraft.class_2874;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_9975;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_9975.class})
abstract class AmbienceMixin {
    AmbienceMixin() {
    }

    @Inject(method={"method_74926"}, at={@At(value="TAIL")})
    private void orange$skyColor(class_638 world, float tickProgress, class_4184 camera, class_12076 state, CallbackInfo ci) {
        if (state.field_64464 == class_2874.class_12326.field_64386) {
            state.field_63097 = VisualModules.skyColor(state.field_63097);
        }
    }
}

