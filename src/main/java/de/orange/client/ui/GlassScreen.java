/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_2561
 *  net.minecraft.class_304
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package de.orange.client.ui;

import de.orange.client.OrangeClient;
import de.orange.client.config.ClientConfig;
import de.orange.client.input.GuiAccess;
import de.orange.client.ui.Glass;
import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_332;
import net.minecraft.class_437;

public abstract class GlassScreen
extends class_437 {
    protected GlassScreen(class_2561 title) {
        super(title);
    }

    public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
        ClientConfig settings = OrangeClient.instance().config();
        Glass.theme(settings.guiAccent, settings.uiOpacity);
        super.method_25420(context, mouseX, mouseY, delta);
    }

    protected void method_57734(class_332 context) {
        if (OrangeClient.instance().config().blur) {
            super.method_57734(context);
        }
    }

    protected void method_57735(class_332 context) {
    }

    public boolean method_25404(class_11908 input) {
        class_304 key = OrangeClient.instance().menuKey();
        if (key != null && key.method_1417(input) || input.comp_4795() == 256) {
            GuiAccess.processKey(this.field_22787, 1, input);
            return true;
        }
        return super.method_25404(input);
    }

    public void method_25419() {
        GuiAccess.closeToGame(this.field_22787);
    }

    public boolean method_25421() {
        return false;
    }
}

