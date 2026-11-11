/*
 * Decompiled with CFR.
 */
package de.orange.client.module;

public enum Category {
    PVP("PvP", "pvp"),
    HUD("HUD", "hud"),
    VISUALS("Visuals", "visuals"),
    PLAYER("Player", "player"),
    MISC("Misc", "misc"),
    OPTIMIZER("Optimizer", "optimizer");

    private final String displayName;
    private final String icon;

    private Category(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String displayName() {
        return this.displayName;
    }

    public String icon() {
        return this.icon;
    }
}

