package de.orange.client.gui;

import de.orange.client.capes.CapeService;
import de.orange.client.ui.Glass;
import de.orange.client.ui.GlassScreen;
import de.orange.client.ui.Rect;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    private int selectedColor = PALETTE[2];
    private boolean painting;
    private boolean published;
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

    public CapeEditorScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"Orange Client · Cape Studio"));
        this.parent = parent;
    }

    @Override
    protected void method_25426() {
        this.layout();
        CapeService.loadMyCape().whenComplete((status, error) -> class_310.method_1551().execute(() -> {
            if (error != null) {
                this.setMessage(this.errorMessage(error), true);
            } else {
                this.published = status.published();
                this.setMessage(status.message(), false);
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
        this.paletteX = (this.field_22789 - PALETTE.length * (this.paletteCell + 6) + 6) / 2;
        this.paletteY = this.canvasY + this.canvasHeight + 15;

        int buttonY = this.field_22790 - 42;
        this.publishButton = new Rect(this.field_22789 / 2 - 154, buttonY, 104, 27);
        this.deleteButton = new Rect(this.field_22789 / 2 - 44, buttonY, 88, 27);
        this.doneButton = new Rect(this.field_22789 / 2 + 50, buttonY, 104, 27);
    }

    @Override
    public void method_25394(class_332 c, int mx, int my, float delta) {
        this.layout();
        Glass.theme(0xFFFF7A18, 0.88f);
        c.method_25294(0, 0, this.field_22789, this.field_22790, 0xE8101218);
        Glass.panel(c, 18, 14, this.field_22789 - 36, this.field_22790 - 28, 0.0f, 0.0f, 0.96f);
        Glass.text(c, "ORANGE CAPE STUDIO", 34, 28, 0xFFFF7A18);
        Glass.text(c, "Zeichne dein eigenes Cape – 64×32 Pixel, gratis und nicht animiert.", 34, 47, 0xFFE0E4EB);

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

        String state = this.published ? "Du hast bereits ein Cape veröffentlicht – ein neues ersetzt es." : "Noch kein Cape veröffentlicht.";
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

        if (this.doneButton.contains(mx, my)) {
            this.field_22787.method_1507(this.parent);
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
