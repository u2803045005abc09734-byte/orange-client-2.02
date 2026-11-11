/*
 * Decompiled with CFR.
 */
package de.orange.client.module;

import de.orange.client.config.ClientConfig;
import de.orange.client.module.Category;
import de.orange.client.module.FreelookController;
import java.util.Set;
import java.util.function.Supplier;

public final class ClientModule {
    private final String id;
    private final String name;
    private final String description;
    private final String icon;
    private final Set<Category> categories;
    private final boolean hud;
    private final Supplier<ClientConfig> config;

    public ClientModule(String id, String name, String description, String icon, boolean hud, Supplier<ClientConfig> config, Category ... categories) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.hud = hud;
        this.config = config;
        this.categories = Set.of(categories);
    }

    public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public String description() {
        return this.description;
    }

    public String icon() {
        return this.icon;
    }

    public Set<Category> categories() {
        return this.categories;
    }

    public boolean hud() {
        return this.hud;
    }

    public boolean enabled() {
        return this.config.get().module((String)this.id).enabled;
    }

    public void setEnabled(boolean enabled) {
        this.config.get().module((String)this.id).enabled = enabled;
        if (this.id.equals("freelook") && !enabled) {
            FreelookController.stop();
        }
    }
}

