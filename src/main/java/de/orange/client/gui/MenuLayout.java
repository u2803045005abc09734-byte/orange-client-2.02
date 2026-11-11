/*
 * Decompiled with CFR.
 */
package de.orange.client.gui;

import de.orange.client.ui.Rect;
import java.util.ArrayList;
import java.util.List;

public record MenuLayout(List<Rect> cards, Rect dock, List<Rect> dockItems, int logoY, int logoRadius, int titleY, boolean compact) {
    public static MenuLayout compute(int width, int height, boolean titleMode) {
        int i;
        boolean compact = height < 300 || width < 500;
        int topBar = titleMode ? 34 : 22;
        int header = compact ? 50 : Math.min(118, height / 3);
        int dockHeight = compact ? 30 : 40;
        int dockWidth = Math.min(width - 32, 430);
        Rect dock = new Rect((width - dockWidth) / 2, height - dockHeight - (compact ? 12 : 20), dockWidth, dockHeight);
        int columns = width >= 560 ? 6 : 3;
        int rows = 6 / columns;
        int gap = compact ? 8 : 10;
        int top = topBar + header + 16;
        int available = dock.y() - 16 - top;
        int cardHeight = Math.max(30, Math.min(compact ? 65 : 105, (available - gap * (rows - 1)) / rows));
        int totalWidth = Math.min(width - 32, 900);
        int cardWidth = (totalWidth - (columns - 1) * gap) / columns;
        int left = (width - (cardWidth * columns + gap * (columns - 1))) / 2;
        ArrayList<Rect> cards = new ArrayList<Rect>();
        ArrayList<Rect> items = new ArrayList<Rect>();
        for (i = 0; i < 6; ++i) {
            cards.add(new Rect(left + i % columns * (cardWidth + gap), top + i / columns * (cardHeight + gap), cardWidth, cardHeight));
        }
        for (i = 0; i < 4; ++i) {
            int x = dock.x() + 4 + i * (dock.w() - 8) / 4;
            int right = dock.x() + 4 + (i + 1) * (dock.w() - 8) / 4;
            items.add(new Rect(x, dock.y() + 3, right - x, dock.h() - 6));
        }
        int logoY = topBar + (compact ? 15 : 36);
        return new MenuLayout(List.copyOf(cards), dock, List.copyOf(items), logoY, compact ? 12 : 25, topBar + (compact ? 31 : 73), compact);
    }
}

