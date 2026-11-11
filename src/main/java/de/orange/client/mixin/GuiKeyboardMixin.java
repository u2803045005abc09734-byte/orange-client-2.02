/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_309
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package de.orange.client.mixin;

import de.orange.client.input.GuiAccess;
import net.minecraft.class_11908;
import net.minecraft.class_309;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_309.class})
public abstract class GuiKeyboardMixin {
    @Inject(method={"method_1466"}, at={@At(value="HEAD")}, cancellable=true)
    private void orange$menuInput(long window, int action, class_11908 input, CallbackInfo callback) {
        class_310 client = class_310.method_1551();
        if (client != null && window == client.method_22683().method_4490() && GuiAccess.processKey(client, action, input)) {
            callback.cancel();
        }
    }
}

