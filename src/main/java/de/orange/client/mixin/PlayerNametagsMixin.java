/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1007
 *  net.minecraft.class_11659
 *  net.minecraft.class_12075
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_4587
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Constant
 *  org.spongepowered.asm.mixin.injection.ModifyConstant
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package de.orange.client.mixin;

import de.orange.client.render.VisualModules;
import net.minecraft.class_1007;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_4587;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={class_1007.class})
abstract class PlayerNametagsMixin {
    PlayerNametagsMixin() {
    }

    @Redirect(method={"method_4213(Lnet/minecraft/class_10055;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_11659;method_73482(Lnet/minecraft/class_4587;Lnet/minecraft/class_243;ILnet/minecraft/class_2561;ZIDLnet/minecraft/class_12075;)V"))
    private void orange$label(class_11659 queue, class_4587 matrices, class_243 position, int y, class_2561 text, boolean seeThrough, int light, double distance, class_12075 camera) {
        VisualModules.submitPlayerLabel(queue, matrices, position, y, text, seeThrough, light, distance, camera);
    }

    @ModifyConstant(method={"method_4213(Lnet/minecraft/class_10055;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V"}, constant={@Constant(floatValue=0.025f)})
    private float orange$scoreboardSpacing(float vanillaScale) {
        return vanillaScale * VisualModules.nametagScale();
    }
}

