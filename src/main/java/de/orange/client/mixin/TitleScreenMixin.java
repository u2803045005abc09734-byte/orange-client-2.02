/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_437
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package de.orange.client.mixin;

import de.orange.client.input.GuiAccess;
import de.orange.client.ui.PackImageCache;
import net.minecraft.class_310;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_310.class})
public abstract class TitleScreenMixin {
    @Inject(method={"method_1507"}, at={@At(value="HEAD")}, cancellable=true)
    private void orange$guardScreen(class_437 screen, CallbackInfo callback) {
        if (GuiAccess.isOwned(screen) && !GuiAccess.allowOwnedScreen((class_310)(Object)this)) {
            callback.cancel();
        }
    }

    @Inject(method={"method_18096(Lnet/minecraft/class_437;ZZ)V"}, at={@At(value="HEAD")})
    private void orange$disconnect(class_437 screen, boolean transferring, boolean clearResources, CallbackInfo callback) {
        GuiAccess.beginDisconnect((class_310)(Object)this);
        PackImageCache.closeShared();
    }

    @Inject(method={"method_24288"}, at={@At(value="TAIL")})
    private void orange$windowTitle(CallbackInfo callback) {
        ((class_310)(Object)this).method_22683().method_24286("Orange Client");
    }
}

