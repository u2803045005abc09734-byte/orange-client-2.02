/*
 * Decompiled with CFR.
 */
package de.orange.client.ui;

public record Rect(int x, int y, int w, int h) {
    public boolean contains(double mx, double my) {
        return mx >= (double)this.x && my >= (double)this.y && mx < (double)(this.x + this.w) && my < (double)(this.y + this.h);
    }

    public int right() {
        return this.x + this.w;
    }

    public int bottom() {
        return this.y + this.h;
    }

    public int centerX() {
        return this.x + this.w / 2;
    }

    public int centerY() {
        return this.y + this.h / 2;
    }

    public Rect inset(int amount) {
        return new Rect(this.x + amount, this.y + amount, Math.max(0, this.w - amount * 2), Math.max(0, this.h - amount * 2));
    }
}

