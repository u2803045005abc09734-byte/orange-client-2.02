/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10017
 *  net.minecraft.class_1297
 *  net.minecraft.class_897
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package de.orange.client.mixin;

import de.orange.client.render.VisualModules;
import net.minecraft.class_10017;
import net.minecraft.class_1297;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_897.class})
abstract class EntityOutlineMixin {
    EntityOutlineMixin() {
    }

    @Inject(method={"method_62425"}, at={@At(value="RETURN")})
    private void orange$outline(class_1297 entity, float tickProgress, CallbackInfoReturnable<class_10017> cir) {
        VisualModules.applyOutline(entity, (class_10017)cir.getReturnValue());
    }
}

