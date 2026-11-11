/*
 * Decompiled with CFR.
 */
package de.orange.client.ui.premium;

import de.orange.client.ui.Rect;

public record GuiLayout(Rect window, Rect header, Rect navigation, Rect content) {
    private static final int[] TAB_WIDTHS = new int[]{32, 48, 38, 47, 39};

    public static GuiLayout compute(int screenWidth, int screenHeight) {
        int availableWidth = Math.max(1, screenWidth - 16);
        int availableHeight = Math.max(1, screenHeight - 16);
        int width = Math.min(availableWidth, Math.max(304, Math.round((float)screenWidth * 0.66f)));
        int height = Math.min(availableHeight, Math.max(224, Math.round((float)screenHeight * 0.84f)));
        int x = (screenWidth - width) / 2;
        int y = (screenHeight - height) / 2;
        Rect window = new Rect(x, y, width, height);
        return new GuiLayout(window, new Rect(x + 14, y + 10, Math.max(1, width - 28), 27), new Rect(x + 14, y + 44, Math.max(1, width - 28), 23), new Rect(x + 7, y + 73, Math.max(1, width - 14), Math.max(1, height - 83)));
    }

    public Rect tab(int index) {
        if (index < 0 || index >= 5) {
            throw new IndexOutOfBoundsException(index);
        }
        int start = this.navigation.x();
        for (int i = 0; i < index; ++i) {
            start += TAB_WIDTHS[i] + 5;
        }
        return new Rect(start, this.navigation.y(), TAB_WIDTHS[index], this.navigation.h());
    }

    public Rect headerIcon(int index) {
        if (index < 0 || index >= 5) {
            throw new IndexOutOfBoundsException(index);
        }
        return new Rect(this.window.right() - 14 - (5 - index) * 18 - (4 - index) * 5, this.window.y() + 14, 18, 18);
    }

    public boolean compact() {
        return this.window.w() < 430;
    }
}

