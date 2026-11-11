/*
 * Decompiled with CFR.
 */
package de.orange.client.input;

public record WorldGate(boolean worldPresent, boolean playerPresent, boolean interactionReady, boolean playerAttached, boolean playerRegistered, boolean handlerAttached, boolean connectionOpen, boolean playerLoaded, boolean transitioning) {
    public boolean ready() {
        return this.worldPresent && this.playerPresent && this.interactionReady && this.playerAttached && this.playerRegistered && this.handlerAttached && this.connectionOpen && this.playerLoaded && !this.transitioning;
    }
}

