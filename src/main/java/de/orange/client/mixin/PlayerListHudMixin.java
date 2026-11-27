package de.orange.client.mixin;

import de.orange.client.capes.CapeService;
import net.minecraft.class_11719;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_355;
import net.minecraft.class_5250;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds the exact white Orange Client mark only to players whose client has
 * recently checked in with the Orange presence service.
 */
@Mixin(class_355.class)
public abstract class PlayerListHudMixin {
    @Inject(method = "method_1918", at = @At("RETURN"), cancellable = true)
    private void orange$clientBadge(class_640 entry, CallbackInfoReturnable<class_2561> cir) {
        if (entry == null || !CapeService.isOrangeClient(entry.method_2966().getId())) {
            return;
        }

        class_11719.class_11721 badgeFont =
            new class_11719.class_11721(class_2960.method_60655((String)"orange", (String)"orange_badge"));
        class_5250 badge = class_2561.method_43470((String)"\uE000")
            .method_27694(style -> style.method_27704(badgeFont));
        badge.method_10852(class_2561.method_43470((String)" "));
        badge.method_10852(cir.getReturnValue());
        cir.setReturnValue(badge);
    }
}
