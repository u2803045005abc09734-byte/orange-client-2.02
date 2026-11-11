/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.textures.FilterMode
 *  net.minecraft.class_11243
 *  net.minecraft.class_11719$class_11721
 *  net.minecraft.class_11767
 *  net.minecraft.class_12236
 *  net.minecraft.class_2960
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package de.orange.client.mixin;

import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.class_11243;
import net.minecraft.class_11719;
import net.minecraft.class_11767;
import net.minecraft.class_12236;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={class_11243.class})
public abstract class ClientFontSamplingMixin {
    @Shadow
    @Final
    private class_11767 comp_4637;

    @ModifyArg(method={"comp_4056"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_12136;method_75294(Lcom/mojang/blaze3d/textures/FilterMode;)Lnet/minecraft/class_12137;"), index=0)
    private FilterMode orange$smoothClientFonts(FilterMode original) {
        class_11767 source = this.comp_4637;
        if (source instanceof class_12236 glyph) {
            class_11719 glyphSource = glyph.comp_3317().method_27708();
            if (glyphSource instanceof class_11719.class_11721 font) {
                class_2960 id = font.comp_4590();
                if (id.method_12836().equals("orange")
                    && (id.method_12832().equals("ui") || id.method_12832().equals("ui_bold"))) {
                    return FilterMode.LINEAR;
                }
            }
        }
        return original;
    }
}

