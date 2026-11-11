/*
 * Decompiled with CFR.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package de.orange.client.ui;

import de.orange.client.OrangeClient;
import de.orange.client.ui.Glass;
import de.orange.client.ui.Rect;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class PackImageCache
implements AutoCloseable {
    private static PackImageCache shared;
    private static final long MAX_GPU_BYTES = 0x1000000L;
    private final LinkedHashMap<String, Entry> entries = new LinkedHashMap(64, 0.75f, true);
    private final ExecutorService decoder = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "Orange-Pack-Images");
        t.setDaemon(true);
        return t;
    });
    private long bytes;
    private long sequence;
    private boolean closed;

    public static PackImageCache get() {
        if (shared == null) {
            shared = new PackImageCache();
        }
        return shared;
    }

    public static void closeShared() {
        if (shared != null) {
            shared.close();
            shared = null;
        }
    }

    public void draw(class_332 c, String url, Rect box) {
        if (box.w() <= 0 || box.h() <= 0) {
            return;
        }
        Entry entry = this.request(url);
        if (entry == null || entry.texture == null) {
            Glass.panel(c, box.x(), box.y(), box.w(), box.h(), 0.0f, 0.0f, 0.45f);
            Glass.icon(c, "pack", box.x() + box.w() / 2, box.y() + box.h() / 2, Math.min(27, box.h() / 3), -1713579563);
            return;
        }
        float sourceAspect = (float)entry.width / (float)entry.height;
        float targetAspect = (float)box.w() / (float)box.h();
        float uw = sourceAspect > targetAspect ? targetAspect / sourceAspect : 1.0f;
        float vh = sourceAspect < targetAspect ? sourceAspect / targetAspect : 1.0f;
        c.method_70845(entry.texture, box.x(), box.y(), box.x() + box.w(), box.y() + box.h(), (1.0f - uw) / 2.0f, (1.0f + uw) / 2.0f, (1.0f - vh) / 2.0f, (1.0f + vh) / 2.0f);
    }

    public void drawContain(class_332 c, String url, Rect box) {
        Entry entry = this.request(url);
        if (entry == null || entry.texture == null) {
            this.draw(c, url, box);
            return;
        }
        Glass.panel(c, box.x(), box.y(), box.w(), box.h(), 0.0f, 0.0f, 0.45f);
        float ratio = Math.min((float)box.w() / (float)entry.width, (float)box.h() / (float)entry.height);
        int w = Math.max(1, Math.round((float)entry.width * ratio));
        int h = Math.max(1, Math.round((float)entry.height * ratio));
        int x = box.x() + (box.w() - w) / 2;
        int y = box.y() + (box.h() - h) / 2;
        c.method_70845(entry.texture, x, y, x + w, y + h, 0.0f, 1.0f, 0.0f, 1.0f);
    }

    public boolean ready(String url) {
        Entry e = this.entries.get(url);
        return e != null && e.texture != null;
    }

    public String error(String url) {
        Entry e = this.entries.get(url);
        return e == null ? null : e.error;
    }

    private Entry request(String url) {
        if (this.closed || url == null || url.isBlank()) {
            return null;
        }
        Entry e = this.entries.get(url);
        if (e != null && (e.loading || e.texture != null || System.nanoTime() < e.retryAfter)) {
            return e;
        }
        Entry fresh = new Entry();
        this.entries.put(url, fresh);
        this.trim();
        CompletableFuture<BufferedImage> decoded = OrangeClient.instance().packs()
            .image(url)
            .thenApplyAsync(PackImageCache::decode, this.decoder);
        decoded.whenComplete((image, failure) -> class_310.method_1551().execute(() -> {
            if (this.closed || this.entries.get(url) != fresh) {
                return;
            }
            fresh.loading = false;
            if (failure != null) {
                fresh.error = "Vorschaubild konnte nicht geladen werden.";
                fresh.retryAfter = System.nanoTime() + 60000000000L;
                return;
            }
            class_1011 pixels = null;
            try {
                pixels = new class_1011(image.getWidth(), image.getHeight(), false);
                for (int y = 0; y < image.getHeight(); ++y) {
                    for (int x = 0; x < image.getWidth(); ++x) {
                        pixels.method_61941(x, y, image.getRGB(x, y));
                    }
                }
                fresh.texture = class_2960.method_60655((String)"orange", (String)("pack_image/" + ++this.sequence));
                fresh.width = image.getWidth();
                fresh.height = image.getHeight();
                class_310.method_1551().method_1531().method_4616(fresh.texture, (class_1044)new class_1043(() -> "Orange pack preview", pixels));
                pixels = null;
                this.bytes += (long)fresh.width * (long)fresh.height * 4L;
                this.trim();
            }
            catch (Exception exception) {
                if (pixels != null) {
                    pixels.close();
                }
                fresh.texture = null;
                fresh.error = "Vorschaubild konnte nicht dargestellt werden.";
                fresh.retryAfter = System.nanoTime() + 60000000000L;
            }
        }));
        return fresh;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive exception aggregation
     */
    private static BufferedImage decode(byte[] bytes) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));){
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IOException("Nicht unterst\u00fctztes Bildformat.");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int w = reader.getWidth(0);
                int h = reader.getHeight(0);
                if (w <= 0 || h <= 0 || w > 4096 || h > 4096 || (long)w * (long)h > 0x800000L) {
                    throw new IOException("Bildaufl\u00f6sung zu gro\u00df.");
                }
                ImageReadParam parameters = reader.getDefaultReadParam();
                int sampling = Math.max(1, Math.max(w, h) / 512);
                parameters.setSourceSubsampling(sampling, sampling, 0, 0);
                BufferedImage source = reader.read(0, parameters);
                float scale = Math.min(1.0f, 512.0f / (float)Math.max(source.getWidth(), source.getHeight()));
                BufferedImage result = new BufferedImage(Math.max(1, (int)((float)source.getWidth() * scale)), Math.max(1, (int)((float)source.getHeight() * scale)), 2);
                Graphics2D graphics = result.createGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                graphics.drawImage(source, 0, 0, result.getWidth(), result.getHeight(), null);
                graphics.dispose();
                source.flush();
                BufferedImage bufferedImage = result;
                reader.dispose();
                return bufferedImage;
            }
            catch (Throwable throwable) {
                reader.dispose();
                throw throwable;
            }
        }
        catch (IOException | RuntimeException failure) {
            throw new CompletionException(failure);
        }
    }

    private void trim() {
        Iterator<Map.Entry<String, Entry>> iterator = this.entries.entrySet().iterator();
        while ((this.entries.size() > 64 || this.bytes > 0x1000000L) && iterator.hasNext()) {
            Entry entry = iterator.next().getValue();
            iterator.remove();
            if (entry.texture == null) continue;
            this.bytes -= (long)entry.width * (long)entry.height * 4L;
            class_310.method_1551().method_1531().method_4615(entry.texture);
        }
    }

    @Override
    public void close() {
        this.closed = true;
        this.decoder.shutdownNow();
        for (Entry entry : this.entries.values()) {
            if (entry.texture == null) continue;
            class_310.method_1551().method_1531().method_4615(entry.texture);
        }
        this.entries.clear();
        this.bytes = 0L;
    }

    private static final class Entry {
        class_2960 texture;
        int width;
        int height;
        String error;
        long retryAfter;
        boolean loading = true;

        private Entry() {
        }
    }
}

