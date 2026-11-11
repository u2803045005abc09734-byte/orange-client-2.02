/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_332
 */
package de.orange.client.ui.premium.components;

import de.orange.client.ui.Anim;
import de.orange.client.ui.Rect;
import de.orange.client.ui.premium.GuiFrame;
import java.util.Objects;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_332;

public final class PopupManager {
    private Popup popup;
    private Popup outgoing;
    private Object owner;
    private Rect limit;
    private float openedFor;
    private float closedFor;

    public void open(Object owner, Popup popup) {
        Objects.requireNonNull(owner, "popup owner");
        Objects.requireNonNull(popup, "popup");
        this.close();
        this.outgoing = null;
        this.owner = owner;
        this.popup = popup;
        this.openedFor = 0.0f;
        if (this.limit != null) {
            popup.layout(this.limit);
        }
    }

    public boolean isOpen() {
        return this.popup != null;
    }

    public boolean isOpen(Object owner) {
        return this.popup != null && this.owner == owner;
    }

    public void close() {
        Popup previous = this.popup;
        this.popup = null;
        this.owner = null;
        if (previous != null) {
            previous.close();
            this.outgoing = previous;
            this.closedFor = 0.0f;
        }
    }

    public void render(class_332 context, GuiFrame frame, Rect limit) {
        float alpha;
        this.limit = Objects.requireNonNull(limit, "popup limit");
        if (this.outgoing != null) {
            this.closedFor += frame.dt();
            float f = alpha = frame.reduced() ? 0.0f : 1.0f - Anim.easeOutCubic(Math.clamp(this.closedFor / 0.14f, 0.0f, 1.0f));
            if (alpha <= 0.001f) {
                this.outgoing = null;
            } else {
                this.outgoing.layout(limit);
                this.outgoing.render(context, PopupManager.withAlpha(frame, alpha));
            }
        }
        if (this.popup != null) {
            this.popup.layout(limit);
            this.openedFor += frame.dt();
            alpha = frame.reduced() ? 1.0f : Anim.easeOutCubic(Math.clamp(this.openedFor / 0.16f, 0.0f, 1.0f));
            this.popup.render(context, PopupManager.withAlpha(frame, alpha));
        }
    }

    public boolean mouseClicked(double x, double y, int button) {
        if (this.popup == null) {
            return false;
        }
        if (!this.popup.bounds().contains(x, y)) {
            this.close();
        } else {
            this.popup.mouseClicked(x, y, button);
        }
        return true;
    }

    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (this.popup == null) {
            return false;
        }
        this.popup.mouseDragged(x, y, button, dx, dy);
        return true;
    }

    public boolean mouseReleased(double x, double y, int button) {
        if (this.popup == null) {
            return false;
        }
        this.popup.mouseReleased(x, y, button);
        return true;
    }

    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (this.popup == null) {
            return false;
        }
        this.popup.mouseScrolled(x, y, horizontal, vertical);
        return true;
    }

    public boolean keyPressed(class_11908 input) {
        if (this.popup == null) {
            return false;
        }
        this.popup.keyPressed(input);
        return true;
    }

    public boolean charTyped(class_11905 input) {
        if (this.popup == null) {
            return false;
        }
        this.popup.charTyped(input);
        return true;
    }

    public boolean handleEscape() {
        if (this.popup == null) {
            return false;
        }
        if (!this.popup.onEscape()) {
            this.close();
        }
        return true;
    }

    public static Rect clamp(Rect desired, Rect limit) {
        int width = Math.clamp((long)desired.w(), 0, Math.max(0, limit.w()));
        int height = Math.clamp((long)desired.h(), 0, Math.max(0, limit.h()));
        int x = Math.clamp((long)desired.x(), limit.x(), Math.max(limit.x(), limit.right() - width));
        int y = Math.clamp((long)desired.y(), limit.y(), Math.max(limit.y(), limit.bottom() - height));
        return new Rect(x, y, width, height);
    }

    private static GuiFrame withAlpha(GuiFrame frame, float alpha) {
        return new GuiFrame(frame.mouseX(), frame.mouseY(), frame.dt(), frame.alpha() * alpha, frame.reduced());
    }

    public static interface Popup {
        public Rect bounds();

        public void render(class_332 var1, GuiFrame var2);

        public boolean mouseClicked(double var1, double var3, int var5);

        default public void layout(Rect limit) {
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

        default public boolean onEscape() {
            return false;
        }

        default public void close() {
        }
    }
}

