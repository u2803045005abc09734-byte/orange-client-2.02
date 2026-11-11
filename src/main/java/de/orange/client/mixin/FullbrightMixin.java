/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2874
 *  net.minecraft.class_765
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package de.orange.client.mixin;

import de.orange.client.render.VisualModules;
import net.minecraft.class_2874;
import net.minecraft.class_765;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={class_765.class})
abstract class FullbrightMixin {
    FullbrightMixin() {
    }

    @Redirect(method={"method_3313"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_2874;comp_656()F"))
    private float orange$ambientLight(class_2874 dimension) {
        return VisualModules.ambientLight(dimension.comp_656());
    }
}

