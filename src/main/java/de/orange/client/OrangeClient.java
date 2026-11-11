/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_2960
 *  net.minecraft.class_304
 *  net.minecraft.class_304$class_11900
 *  net.minecraft.class_310
 *  net.minecraft.class_437
 *  org.slf4j.LoggerFactory
 */
package de.orange.client;

import de.orange.client.config.ClientConfig;
import de.orange.client.config.ProfileManager;
import de.orange.client.hud.HudRenderer;
import de.orange.client.input.GuiAccess;
import de.orange.client.modrinth.ModrinthService;
import de.orange.client.module.FreelookController;
import de.orange.client.module.ModuleRegistry;
import de.orange.client.render.VisualModules;
import de.orange.client.ui.Glass;
import de.orange.client.ui.PackImageCache;
import java.io.IOException;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_437;
import org.slf4j.LoggerFactory;

public final class OrangeClient
implements ClientModInitializer {
    private static OrangeClient instance;
    private ProfileManager profiles;
    private ModuleRegistry modules;
    private HudRenderer hud;
    private ModrinthService packs;
    private class_304 menuKey;

    public void onInitializeClient() {
        instance = this;
        class_310 mc = class_310.method_1551();
        Path configDirectory = FabricLoader.getInstance().getConfigDir();
        this.profiles = new ProfileManager(configDirectory);
        this.modules = new ModuleRegistry(this::config);
        VisualModules.initialize(this::config);
        this.hud = new HudRenderer(this::config);
        this.packs = new ModrinthService(mc.field_1697.toPath().resolve("resourcepacks"), configDirectory.resolve("orange-client/cache"));
        this.menuKey = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.orange.menu", 344, class_304.class_11900.method_74698((class_2960)class_2960.method_60655((String)"orange", (String)"client"))));
        FreelookController.initialize(this::config);
        ClientPlayConnectionEvents.INIT.register((handler, client) -> GuiAccess.connectionStarted());
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> GuiAccess.connectionStarted());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(() -> {
            if (client.method_1562() == null || client.method_1562() == handler) {
                GuiAccess.beginDisconnect(client);
            }
        }));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Glass.theme(this.config().accent, this.config().uiOpacity);
            FreelookController.tick(client);
            GuiAccess.tick(client);
        });
        HudElementRegistry.attachElementBefore((class_2960)VanillaHudElements.CHAT, (class_2960)class_2960.method_60655((String)"orange", (String)"hud"), (context, ticks) -> {
            Glass.theme(this.config().accent, this.config().uiOpacity);
            this.hud.render(context, ticks);
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            GuiAccess.beginDisconnect(client);
            FreelookController.stop();
            this.save();
            PackImageCache.closeShared();
            this.packs.close();
        });
    }

    public static OrangeClient instance() {
        return instance;
    }

    public ClientConfig config() {
        return this.profiles.config();
    }

    public ProfileManager profiles() {
        return this.profiles;
    }

    public ModuleRegistry modules() {
        return this.modules;
    }

    public HudRenderer hud() {
        return this.hud;
    }

    public ModrinthService packs() {
        return this.packs;
    }

    public class_304 menuKey() {
        return this.menuKey;
    }

    public void save() {
        try {
            this.profiles.save();
        }
        catch (IOException e) {
            LoggerFactory.getLogger((String)"Orange Client").error("Einstellungen konnten nicht gespeichert werden", (Throwable)e);
        }
    }

    public static void returnTo(class_437 parent) {
        class_310.method_1551().method_1507(parent);
    }
}

