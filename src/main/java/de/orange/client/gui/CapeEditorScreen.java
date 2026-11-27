package de.orange.client.gui;

import de.orange.client.capes.CapeService;
import de.orange.client.ui.Glass;
import de.orange.client.ui.GlassScreen;
import de.orange.client.ui.Rect;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

public final class CapeEditorScreen extends GlassScreen {
    private static final int CAPE_WIDTH = 64;
    private static final int CAPE_HEIGHT = 32;
    private static final int[] PALETTE = new int[]{
        0xFFFFFFFF, 0xFF20232B, 0xFFFF7A18, 0xFFEA4545,
        0xFF3B82F6, 0xFF35B779, 0xFFB36BFF, 0xFFF3C969
    };

    private final class_437 parent;
    private final int[] pixels = new int[CAPE_WIDTH * CAPE_HEIGHT];
    private final List<CommunityHit> communityHits = new ArrayList<>();
    private List<CapeService.CommunityCape> communityCapes = List.of();
    private int selectedColor = PALETTE[2];
    private boolean painting;
    private boolean published;
    private boolean showCommunity;
    private boolean communityLoading;
    private String message = "64×32 Pixel-Cape · kostenlos · ein Cape pro Spieler";
    private long messageUntil;

    private int scale;
    private int canvasX;
    private int canvasY;
    private int canvasWidth;
    private int canvasHeight;
    private int paletteX;
    private int paletteY;
    private int paletteCell;
    private Rect publishButton;
    private Rect deleteButton;
    private Rect doneButton;
    private Rect communityButton;
    private Rect eraserButton;

    public CapeEditorScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"Orange Client · Cape Studio"));
        this.parent = parent;
    }

    @Override
    protected void method_25426() {
        this.layout();
        this.communityLoading = true;
        CapeService.loadMyCape().whenComplete((status, error) -> class_310.method_1551().execute(() -> {
            if (error != null) {
                this.setMessage(this.errorMessage(error), true);
            } else {
                this.published = status.published();
                this.setMessage(status.message(), false);
                if (status.published()) {
                    CapeService.loadMyCapePng().whenComplete((png, loadError) -> class_310.method_1551().execute(() -> {
                        if (loadError != null) {
                            this.setMessage(this.errorMessage(loadError), true);
                        } else if (png != null) {
                            try {
                                this.loadPixels(png);
                                this.setMessage("Dein veröffentlichtes Cape wurde geladen. Du kannst es direkt bearbeiten.", false);
                            } catch (IOException imageError) {
                                this.setMessage(imageError.getMessage(), true);
                            }
                        }
                    }));
                }
            }
        }));
        CapeService.loadCommunityCapes().whenComplete((capes, error) -> class_310.method_1551().execute(() -> {
            this.communityLoading = false;
            if (error != null) {
                this.setMessage(this.errorMessage(error), true);
            } else {
                UUID ownUuid = class_310.method_1551().method_1548().method_44717();
                this.communityCapes = capes.stream()
                    .filter(cape -> ownUuid == null || !ownUuid.equals(cape.minecraftUuid()))
                    .toList();
            }
        }));
    }

    private void layout() {
        int availableWidth = Math.max(128, this.field_22789 - 72);
        int availableHeight = Math.max(96, this.field_22790 - 174);
        this.scale = Math.clamp(Math.min(availableWidth / CAPE_WIDTH, availableHeight / CAPE_HEIGHT), 2, 7);
        this.canvasWidth = CAPE_WIDTH * this.scale;
        this.canvasHeight = CAPE_HEIGHT * this.scale;
        this.canvasX = (this.field_22789 - this.canvasWidth) / 2;
        this.canvasY = 76;
        this.paletteCell = 24;
        int paletteWidth = PALETTE.length * (this.paletteCell + 6) - 6 + 68;
        this.paletteX = (this.field_22789 - paletteWidth) / 2;
        this.paletteY = this.canvasY + this.canvasHeight + 15;
        this.eraserButton = new Rect(
            this.paletteX + PALETTE.length * (this.paletteCell + 6),
            this.paletteY,
            62,
            this.paletteCell
        );

        int buttonY = this.field_22790 - 42;
        this.publishButton = new Rect(this.field_22789 / 2 - 154, buttonY, 104, 27);
        this.deleteButton = new Rect(this.field_22789 / 2 - 44, buttonY, 88, 27);
        this.doneButton = new Rect(this.field_22789 / 2 + 50, buttonY, 104, 27);
        this.communityButton = new Rect(this.field_22789 - 180, 22, 146, 24);
    }

    @Override
    public void method_25394(class_332 c, int mx, int my, float delta) {
        this.layout();
        Glass.theme(0xFFFF7A18, 0.88f);
        c.method_25294(0, 0, this.field_22789, this.field_22790, 0xE8101218);
        Glass.panel(c, 18, 14, this.field_22789 - 36, this.field_22790 - 28, 0.0f, 0.0f, 0.96f);
        Glass.text(c, "ORANGE CAPE STUDIO", 34, 28, 0xFFFF7A18);
        Glass.text(c, "Statische 64×32-PNG-Capes · kostenlos · ein Cape pro Spieler", 34, 47, 0xFFE0E4EB);
        Glass.button(c, this.communityButton, this.showCommunity ? "Zum Editor" : "Community", this.communityButton.contains(mx, my));

        if (this.showCommunity) {
            this.renderCommunity(c, mx, my);
            Glass.text(c, this.message, 34, this.field_22790 - 57, 0xFFD8DDE5);
            Glass.button(c, this.doneButton, "Fertig", this.doneButton.contains(mx, my));
            return;
        }

        Rect canvasPanel = new Rect(this.canvasX - 8, this.canvasY - 8, this.canvasWidth + 16, this.canvasHeight + 16);
        Glass.panel(c, canvasPanel, 0.0f, 0.0f, 1.0f);
        for (int py = 0; py < CAPE_HEIGHT; py++) {
            for (int px = 0; px < CAPE_WIDTH; px++) {
                int x = this.canvasX + px * this.scale;
                int y = this.canvasY + py * this.scale;
                int checker = ((px / 4) + (py / 4)) % 2 == 0 ? 0xFF30343D : 0xFF414650;
                c.method_25294(x, y, x + this.scale, y + this.scale, checker);
                int color = this.pixels[py * CAPE_WIDTH + px];
                if ((color >>> 24) != 0) {
                    c.method_25294(x, y, x + this.scale, y + this.scale, color);
                }
            }
        }

        Glass.text(c, "FARBE", this.paletteX, this.paletteY - 13, 0xFFE0E4EB);
        for (int i = 0; i < PALETTE.length; i++) {
            int x = this.paletteX + i * (this.paletteCell + 6);
            int color = PALETTE[i];
            c.method_25294(x - 2, this.paletteY - 2, x + this.paletteCell + 2, this.paletteY + this.paletteCell + 2,
                color == this.selectedColor ? 0xFFFF7A18 : 0xFF737B89);
            c.method_25294(x, this.paletteY, x + this.paletteCell, this.paletteY + this.paletteCell, color);
        }
        Glass.button(c, this.eraserButton, "Radierer", this.selectedColor == 0 || this.eraserButton.contains(mx, my));

        String state = this.published ? "Dein Cape ist veröffentlicht – Speichern ersetzt es, nicht ein zweites Cape." : "Noch kein Cape veröffentlicht.";
        Glass.text(c, state, 34, this.field_22790 - 72, this.published ? 0xFFFFBE75 : 0xFFBEC5D0);
        Glass.text(c, this.message, 34, this.field_22790 - 57, 0xFFD8DDE5);
        Glass.button(c, this.publishButton, "Veröffentlichen", this.publishButton.contains(mx, my));
        Glass.button(c, this.deleteButton, "Entfernen", this.deleteButton.contains(mx, my));
        Glass.button(c, this.doneButton, "Fertig", this.doneButton.contains(mx, my));
    }

    @Override
    public boolean method_25402(class_11909 click, boolean doubled) {
        if (click.method_74245() != 0) {
            return super.method_25402(click, doubled);
        }
        this.layout();
        int mx = (int)click.comp_4798();
        int my = (int)click.comp_4799();

        if (this.communityButton.contains(mx, my)) {
            this.showCommunity = !this.showCommunity;
            return true;
        }
        if (this.doneButton.contains(mx, my)) {
            this.field_22787.method_1507(this.parent);
            return true;
        }
        if (this.showCommunity) {
            for (CommunityHit hit : this.communityHits) {
                if (hit.bounds().contains(mx, my)) {
                    this.loadCommunityCape(hit.cape());
                    return true;
                }
            }
            return true;
        }
        if (this.publishButton.contains(mx, my)) {
            this.publish();
            return true;
        }
        if (this.deleteButton.contains(mx, my)) {
            this.delete();
            return true;
        }
        for (int i = 0; i < PALETTE.length; i++) {
            int x = this.paletteX + i * (this.paletteCell + 6);
            if (new Rect(x, this.paletteY, this.paletteCell, this.paletteCell).contains(mx, my)) {
                this.selectedColor = PALETTE[i];
                return true;
            }
        }
        if (this.eraserButton.contains(mx, my)) {
            this.selectedColor = 0;
            return true;
        }
        if (this.paintAt(mx, my)) {
            this.painting = true;
            return true;
        }
        return super.method_25402(click, doubled);
    }

    @Override
    public boolean method_25403(class_11909 click, double dx, double dy) {
        if (this.painting) {
            this.layout();
            this.paintAt((int)click.comp_4798(), (int)click.comp_4799());
            return true;
        }
        return super.method_25403(click, dx, dy);
    }

    @Override
    public boolean method_25406(class_11909 click) {
        boolean wasPainting = this.painting;
        this.painting = false;
        return wasPainting || super.method_25406(click);
    }

    @Override
    public void method_25419() {
        this.field_22787.method_1507(this.parent);
    }

    private void renderCommunity(class_332 c, int mx, int my) {
        this.communityHits.clear();
        Glass.text(c, "KOSTENLOSE COMMUNITY-CAPES", 34, 72, 0xFFFF7A18);
        if (this.communityLoading) {
            Glass.centered(c, "Community-Capes werden geladen …", this.field_22789 / 2, 128, 0xFFE0E4EB);
            return;
        }
        if (this.communityCapes.isEmpty()) {
            Glass.centered(c, "Noch keine Community-Capes veröffentlicht.", this.field_22789 / 2, 128, 0xFFE0E4EB);
            return;
        }
        int columns = this.field_22789 >= 650 ? 2 : 1;
        int gap = 10;
        int cardWidth = (this.field_22789 - 68 - gap * (columns - 1)) / columns;
        int visible = Math.min(8, this.communityCapes.size());
        for (int i = 0; i < visible; i++) {
            CapeService.CommunityCape cape = this.communityCapes.get(i);
            int column = i % columns;
            int row = i / columns;
            int x = 34 + column * (cardWidth + gap);
            int y = 94 + row * 57;
            Rect card = new Rect(x, y, cardWidth, 48);
            boolean hover = card.contains(mx, my);
            Glass.panel(c, card, hover ? 0.55f : 0.2f, hover ? 0.35f : 0.0f, 0.95f);
            String owner = cape.username() == null || cape.username().isBlank() ? "Orange-Spieler" : cape.username();
            Glass.text(c, "Cape von " + owner, x + 10, y + 8, 0xFFE9EDF4);
            Glass.text(c, "64×32 PNG · kostenlos · anklicken zum Laden", x + 10, y + 26, 0xFFBEC5D0);
            this.communityHits.add(new CommunityHit(card, cape));
        }
        if (this.communityCapes.size() > visible) {
            Glass.text(c, "Die neuesten 8 Capes werden angezeigt.", 34, 94 + ((visible + columns - 1) / columns) * 57, 0xFFBEC5D0);
        }
    }

    private void loadCommunityCape(CapeService.CommunityCape cape) {
        this.setMessage("Community-Cape wird geladen …", false);
        CapeService.loadCommunityCape(cape).whenComplete((png, error) -> class_310.method_1551().execute(() -> {
            if (error != null) {
                this.setMessage(this.errorMessage(error), true);
                return;
            }
            try {
                this.loadPixels(png);
                this.showCommunity = false;
                this.setMessage("Vorlage von " + cape.username() + " geladen. Beim Veröffentlichen wird nur dein Cape ersetzt.", false);
            } catch (IOException imageError) {
                this.setMessage(imageError.getMessage(), true);
            }
        }));
    }

    private void loadPixels(byte[] png) throws IOException {
        if (png == null || png.length == 0) {
            return;
        }
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        if (image == null || image.getWidth() != CAPE_WIDTH || image.getHeight() != CAPE_HEIGHT) {
            throw new IOException("Cape muss eine gültige 64×32-PNG-Datei sein.");
        }
        image.getRGB(0, 0, CAPE_WIDTH, CAPE_HEIGHT, this.pixels, 0, CAPE_WIDTH);
    }

    private record CommunityHit(Rect bounds, CapeService.CommunityCape cape) {
    }

    private boolean paintAt(int mouseX, int mouseY) {
        if (mouseX < this.canvasX || mouseY < this.canvasY
            || mouseX >= this.canvasX + this.canvasWidth || mouseY >= this.canvasY + this.canvasHeight) {
            return false;
        }
        int x = (mouseX - this.canvasX) / this.scale;
        int y = (mouseY - this.canvasY) / this.scale;
        this.pixels[y * CAPE_WIDTH + x] = this.selectedColor;
        return true;
    }

    private void publish() {
        byte[] png;
        try {
            BufferedImage image = new BufferedImage(CAPE_WIDTH, CAPE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, CAPE_WIDTH, CAPE_HEIGHT, this.pixels, 0, CAPE_WIDTH);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", output);
            png = output.toByteArray();
        } catch (IOException error) {
            this.setMessage("PNG konnte nicht erstellt werden: " + error.getMessage(), true);
            return;
        }

        this.setMessage("Cape wird veröffentlicht …", false);
        CapeService.publishCape(png).whenComplete((result, error) -> class_310.method_1551().execute(() -> {
            if (error != null) {
                this.setMessage(this.errorMessage(error), true);
            } else {
                this.published = true;
                this.setMessage(result, false);
            }
        }));
    }

    private void delete() {
        this.setMessage("Cape wird entfernt …", false);
        CapeService.deleteMyCape().whenComplete((result, error) -> class_310.method_1551().execute(() -> {
            if (error != null) {
                this.setMessage(this.errorMessage(error), true);
            } else {
                this.published = false;
                Arrays.fill(this.pixels, 0);
                this.setMessage(result, false);
            }
        }));
    }

    private void setMessage(String value, boolean error) {
        this.message = value == null ? "" : value;
        this.messageUntil = System.currentTimeMillis() + 8000L;
        if (error) {
            this.message = "Fehler: " + this.message;
        }
    }

    private String errorMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? "Orange-Server ist gerade nicht erreichbar." : current.getMessage();
    }
}
