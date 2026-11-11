/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_332
 */
package de.orange.client.gui.premium;

import de.orange.client.ui.Rect;
import de.orange.client.ui.premium.GuiFrame;
import de.orange.client.ui.premium.components.PopupManager;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_332;

public interface GuiPage {
    public void render(class_332 var1, Rect var2, GuiFrame var3, PopupManager var4);

    default public boolean mouseClicked(double x, double y, int button) {
        return false;
    }

    default public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        return false;
    }

    default public boolean mouseReleased(double x, double y, int button) {
        return false;
    }

    default public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        return false;
    }

    default public boolean keyPressed(class_11908 input) {
        return false;
    }

    default public boolean charTyped(class_11905 input) {
        return false;
    }

    default public void focusModule(String id) {
    }

    default public void leave() {
    }
}

