package de.orange.client.mixin;

import de.orange.client.capes.CapeService;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_11890;
import net.minecraft.class_12079;
import net.minecraft.class_2960;
import net.minecraft.class_8685;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Supplies a published Orange cape texture to vanilla's existing cape renderer.
 */
@Mixin(class_1007.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(method = "method_62604", at = @At("TAIL"))
    private void orange$usePublishedCape(
        class_11890 player,
        class_10055 state,
        float tickProgress,
        CallbackInfo ci
    ) {
        if (player == null || state == null || state.field_53520 == null) {
            return;
        }

        class_2960 capeTexture = CapeService.capeTexture(player.method_5667());
        if (capeTexture == null) {
            return;
        }

        class_8685 current = state.field_53520;
        class_12079.class_12081 capeAsset = new class_12079.class_10726(capeTexture, capeTexture);
        state.field_53520 = new class_8685(
            current.comp_1626(),
            capeAsset,
            current.comp_1628(),
            current.comp_1629(),
            current.comp_1630()
        );
        state.field_53532 = true;
    }
}
