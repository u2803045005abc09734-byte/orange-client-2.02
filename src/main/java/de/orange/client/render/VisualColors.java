/*
 * Decompiled with CFR.
 */
package de.orange.client.render;

import de.orange.client.config.RenderSettings;

public final class VisualColors {
    private VisualColors() {
    }

    public static int outline(RenderSettings.Target target, RenderSettings settings, double seconds, double worldX, double worldZ) {
        int color = switch (target.mode) {
            default -> throw new MatchException(null, null);
            case RenderSettings.ColourMode.SOLID -> target.primary;
            case RenderSettings.ColourMode.CUSTOM -> target.custom;
            case RenderSettings.ColourMode.GRADIENT -> {
                double phase = seconds * (double)settings.gradientSpeed;
                if (settings.gradientSpread > 0.0f) {
                    phase += (worldX + worldZ) / (double)settings.gradientSpread;
                }
                yield VisualColors.mix(target.primary, target.secondary, (float)(0.5 + 0.5 * Math.sin(phase * Math.PI * 2.0)));
            }
            case RenderSettings.ColourMode.RAINBOW -> VisualColors.rainbow(seconds * (double)settings.rainbowSpeed);
        };
        return VisualColors.intensity(color, target.strength);
    }

    public static int intensity(int color, float strength) {
        float value = Float.isFinite(strength) ? Math.clamp(strength, 0.0f, 1.0f) : 0.0f;
        return 0xFF000000 | Math.round((float)(color >>> 16 & 0xFF) * value) << 16 | Math.round((float)(color >>> 8 & 0xFF) * value) << 8 | Math.round((float)(color & 0xFF) * value);
    }

    public static int mix(int first, int second, float amount) {
        float t = Float.isFinite(amount) ? Math.clamp(amount, 0.0f, 1.0f) : 0.0f;
        int red = Math.round((float)(first >>> 16 & 0xFF) * (1.0f - t) + (float)(second >>> 16 & 0xFF) * t);
        int green = Math.round((float)(first >>> 8 & 0xFF) * (1.0f - t) + (float)(second >>> 8 & 0xFF) * t);
        int blue = Math.round((float)(first & 0xFF) * (1.0f - t) + (float)(second & 0xFF) * t);
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    private static int rainbow(double turns) {
        double hue = (turns - Math.floor(turns)) * 6.0;
        int segment = (int)hue;
        float fraction = (float)(hue - (double)segment);
        float low = 0.25f;
        float up = low + (1.0f - low) * fraction;
        float down = 1.0f - (1.0f - low) * fraction;
        return switch (segment) {
            case 0 -> VisualColors.rgb(1.0f, up, low);
            case 1 -> VisualColors.rgb(down, 1.0f, low);
            case 2 -> VisualColors.rgb(low, 1.0f, up);
            case 3 -> VisualColors.rgb(low, down, 1.0f);
            case 4 -> VisualColors.rgb(up, low, 1.0f);
            default -> VisualColors.rgb(1.0f, low, down);
        };
    }

    private static int rgb(float red, float green, float blue) {
        return 0xFF000000 | Math.round(red * 255.0f) << 16 | Math.round(green * 255.0f) << 8 | Math.round(blue * 255.0f);
    }
}

