/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_424
 *  net.minecraft.class_437
 *  net.minecraft.class_442
 *  net.minecraft.class_634
 *  net.minecraft.class_638
 *  net.minecraft.class_746
 */
package de.orange.client.input;

import de.orange.client.OrangeClient;
import de.orange.client.gui.premium.ClickGuiScreen;
import de.orange.client.input.PressLatch;
import de.orange.client.input.WorldGate;
import de.orange.client.ui.GlassScreen;
import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_424;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_634;
import net.minecraft.class_638;
import net.minecraft.class_746;

public final class GuiAccess {
    private static final PressLatch menuPress = new PressLatch();
    private static final PressLatch escapePress = new PressLatch();
    private static boolean departing;
    private static boolean sessionOpen;
    private static class_437 transitionScreen;

    private GuiAccess() {
    }

    public static boolean isOwned(class_437 screen) {
        return screen instanceof GlassScreen;
    }

    public static boolean worldReady(class_310 client) {
        if (client == null) {
            return false;
        }
        class_638 world = client.field_1687;
        class_746 player = client.field_1724;
        class_634 handler = client.method_1562();
        boolean handlerAttached = handler != null && world != null && player != null && handler.method_2890() == world && player.field_3944 == handler;
        return new WorldGate(world != null, player != null, client.field_1761 != null, player != null && world != null && player.method_73183() == world && !player.method_31481(), player != null && world != null && world.method_8469(player.method_5628()) == player, handlerAttached, handler != null && handler.method_48296().method_10758(), handler != null && handler.method_76760(), departing).ready();
    }

    public static boolean canOpen(class_310 client) {
        return GuiAccess.worldReady(client) && client.field_1755 == null && client.method_18506() == null && client.method_1569();
    }

    public static boolean allowOwnedScreen(class_310 client) {
        return sessionOpen && GuiAccess.worldReady(client);
    }

    public static boolean processKey(class_310 client, int action, class_11908 input) {
        PressLatch latch;
        boolean escape;
        OrangeClient orange = OrangeClient.instance();
        if (orange == null || orange.menuKey() == null) {
            return false;
        }
        boolean menu = orange.menuKey().method_1417(input);
        boolean bl = escape = input.comp_4795() == 256;
        if (!menu && !escape) {
            return false;
        }
        PressLatch pressLatch = latch = menu ? menuPress : escapePress;
        if (action == 0) {
            latch.release();
            return false;
        }
        boolean owned = GuiAccess.isOwned(client.field_1755);
        if (action != 1) {
            return owned || latch.claimed();
        }
        if (!latch.press()) {
            return owned || latch.claimed();
        }
        if (owned) {
            latch.claim();
            if (!GuiAccess.worldReady(client)) {
                GuiAccess.closeToGame(client);
                return true;
            }
            class_437 class_4372 = client.field_1755;
            if (class_4372 instanceof ClickGuiScreen) {
                ClickGuiScreen clickGui = (ClickGuiScreen)class_4372;
                if (!escape || !clickGui.onEscape()) {
                    clickGui.requestClose();
                }
            } else {
                GuiAccess.closeToGame(client);
            }
            return true;
        }
        if (menu && GuiAccess.canOpen(client)) {
            latch.claim();
            sessionOpen = true;
            client.method_1507((class_437)new ClickGuiScreen());
            return true;
        }
        return false;
    }

    public static void tick(class_310 client) {
        OrangeClient orange = OrangeClient.instance();
        if (orange != null && orange.menuKey() != null) {
            while (orange.menuKey().method_1436()) {
            }
        }
        if (!GuiAccess.worldReady(client)) {
            sessionOpen = false;
            if (GuiAccess.isOwned(client.field_1755)) {
                GuiAccess.closeToGame(client);
            }
        }
        if (client.field_1755 == transitionScreen && transitionScreen != null && GuiAccess.worldReady(client)) {
            transitionScreen = null;
            client.method_1507(null);
        }
    }

    public static void connectionStarted() {
        departing = false;
        sessionOpen = false;
        transitionScreen = null;
    }

    public static void beginDisconnect(class_310 client) {
        departing = true;
        sessionOpen = false;
        if (client != null && GuiAccess.isOwned(client.field_1755)) {
            if (OrangeClient.instance() != null) {
                OrangeClient.instance().save();
            }
            client.method_1507((class_437)new class_424((class_2561)class_2561.method_43470((String)"Welt wird verlassen \u2026")));
        }
        transitionScreen = null;
    }

    public static void closeToGame(class_310 client) {
        if (client == null || !GuiAccess.isOwned(client.field_1755)) {
            return;
        }
        sessionOpen = false;
        OrangeClient orange = OrangeClient.instance();
        if (orange != null) {
            orange.save();
        }
        if (!departing && client.field_1687 != null && client.field_1724 != null) {
            client.method_1507(null);
        } else if (!departing && client.field_1687 == null) {
            client.method_1507((class_437)new class_442());
        } else {
            transitionScreen = new class_424((class_2561)class_2561.method_43470((String)"Welt wird geladen \u2026"));
            client.method_1507(transitionScreen);
        }
    }
}

