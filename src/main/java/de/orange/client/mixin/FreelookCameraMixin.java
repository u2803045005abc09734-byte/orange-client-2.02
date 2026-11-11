/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1937
 *  net.minecraft.class_310
 *  net.minecraft.class_4184
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package de.orange.client.mixin;

import de.orange.client.module.FreelookController;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={class_4184.class})
public abstract class FreelookCameraMixin {
    @Shadow
    protected abstract void method_19325(float var1, float var2);

    @Redirect(method={"method_19321"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_4184;method_19325(FF)V"))
    private void orange$cameraRotation(class_4184 camera, float yaw, float pitch, class_1937 world, class_1297 entity, boolean thirdPerson, boolean inverseView, float tickProgress) {
        if (FreelookController.active() && entity == class_310.method_1551().field_1724) {
            this.method_19325(FreelookController.yaw(), FreelookController.pitch());
        } else {
            this.method_19325(yaw, pitch);
        }
    }
}

