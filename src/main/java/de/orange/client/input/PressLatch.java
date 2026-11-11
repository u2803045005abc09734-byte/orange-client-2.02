/*
 * Decompiled with CFR.
 */
package de.orange.client.input;

public final class PressLatch {
    private boolean held;
    private boolean claimed;

    public boolean press() {
        if (this.held) {
            return false;
        }
        this.held = true;
        return true;
    }

    public void claim() {
        if (this.held) {
            this.claimed = true;
        }
    }

    public boolean claimed() {
        return this.claimed;
    }

    public void release() {
        this.held = false;
        this.claimed = false;
    }

    public boolean held() {
        return this.held;
    }
}

