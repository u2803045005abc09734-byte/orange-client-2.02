package de.orange.client.gui.premium;

import de.orange.client.OrangeClient;
import de.orange.client.module.ClientModule;
import de.orange.client.ui.Rect;
import de.orange.client.ui.premium.GuiFrame;
import de.orange.client.ui.premium.GuiPrimitives;
import de.orange.client.ui.premium.GuiText;
import de.orange.client.ui.premium.GuiTheme;
import de.orange.client.ui.premium.components.PopupManager;
import java.util.Set;
import net.minecraft.class_332;

/**
 * A conservative, client-only performance preset. It disables optional visual
 * overlays owned by Orange Client; it never changes packets, combat logic or
 * server-side behaviour.
 */
final class OptimizerInfoPopup implements PopupManager.Popup {
    private static final Set<String> OPTIONAL_OVERLAYS = Set.of("renders", "nametags", "hitboxes", "ambience");

    private final Rect preferred;
    private final PopupManager popups;
    private Rect bounds;
    private Rect apply;
    private Rect done;

    OptimizerInfoPopup(Rect viewport, PopupManager popups) {
        this.popups = popups;
        this.preferred = this.bounds = new Rect(viewport.centerX() - 146, viewport.centerY() - 86, 292, 172);
        this.layout(viewport);
    }

    @Override
    public Rect bounds() {
        return this.bounds;
    }

    @Override
    public void layout(Rect limit) {
        this.bounds = PopupManager.clamp(this.preferred, limit);
        int buttonY = this.bounds.bottom() - 32;
        this.apply = new Rect(this.bounds.x() + 14, buttonY, 158, 22);
        this.done = new Rect(this.bounds.right() - 88, buttonY, 74, 22);
    }

    @Override
    public void render(class_332 c, GuiFrame frame) {
        GuiTheme theme = GuiTheme.current();
        GuiPrimitives.borderedPanel(c, this.bounds, GuiTheme.opacity(theme.secondary(), frame.alpha()), GuiTheme.opacity(theme.border(), frame.alpha()), 10);
        GuiPrimitives.icon(c, "optimizer", this.bounds.centerX(), this.bounds.y() + 22, 20, GuiTheme.opacity(theme.accent(), frame.alpha()));
        GuiText.centered(c, "Optimizer", this.bounds.centerX(), this.bounds.y() + 43, GuiTheme.opacity(theme.text(), frame.alpha()));
        GuiText.small(c, "Lightweight preset", this.bounds.x() + 16, this.bounds.y() + 63, GuiTheme.opacity(theme.accent(), frame.alpha()));
        GuiText.small(c, "Disables optional Orange visual overlays:", this.bounds.x() + 16, this.bounds.y() + 79, GuiTheme.opacity(theme.muted(), frame.alpha()));
        GuiText.small(c, "Renders, Nametags, Hitboxes and Ambience.", this.bounds.x() + 16, this.bounds.y() + 92, GuiTheme.opacity(theme.muted(), frame.alpha()));
        GuiText.small(c, "Client-side only; no combat or network changes.", this.bounds.x() + 16, this.bounds.y() + 105, GuiTheme.opacity(theme.disabled(), frame.alpha()));

        boolean applyHover = this.apply.contains(frame.mouseX(), frame.mouseY());
        GuiPrimitives.borderedPanel(c, this.apply,
                GuiTheme.opacity(applyHover ? GuiTheme.mix(theme.accent(), theme.panel(), 0.25f) : theme.accent(), frame.alpha()),
                GuiTheme.opacity(theme.accent(), frame.alpha()), 6);
        GuiText.centered(c, "Apply preset", this.apply.centerX(), this.apply.centerY() - 4,
                GuiTheme.opacity(0xFFFFFFFF, frame.alpha()));

        boolean doneHover = this.done.contains(frame.mouseX(), frame.mouseY());
        GuiPrimitives.borderedPanel(c, this.done,
                GuiTheme.opacity(doneHover ? theme.hover() : theme.panel(), frame.alpha()),
                GuiTheme.opacity(theme.border(), frame.alpha()), 6);
        GuiText.centered(c, "Done", this.done.centerX(), this.done.centerY() - 4,
                GuiTheme.opacity(theme.text(), frame.alpha()));
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button != 0) {
            return true;
        }
        if (this.apply.contains(x, y)) {
            OrangeClient client = OrangeClient.instance();
            if (client != null) {
                for (ClientModule module : client.modules().all()) {
                    if (OPTIONAL_OVERLAYS.contains(module.id())) {
                        module.setEnabled(false);
                    }
                }
                client.save();
            }
            this.popups.close();
            return true;
        }
        if (this.done.contains(x, y)) {
            this.popups.close();
        }
        return true;
    }
}
