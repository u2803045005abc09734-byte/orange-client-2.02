/*
 * Decompiled with CFR.
 */
package de.orange.client.ui;

public final class Anim {
    private float value;

    public Anim() {
        this(0.0f);
    }

    public Anim(float initial) {
        this.value = initial;
    }

    public float value() {
        return this.value;
    }

    public void snap(float target) {
        this.value = target;
    }

    public float to(float target, float deltaSeconds, boolean reduced) {
        if (reduced) {
            this.value = target;
            return this.value;
        }
        float dt = Math.clamp(deltaSeconds, 0.0f, 0.25f);
        this.value += (target - this.value) * (float)(-Math.expm1(-14.0f * dt));
        if (Math.abs(this.value - target) < 1.0E-4f) {
            this.value = target;
        }
        return this.value;
    }

    public static float easeOutCubic(float time) {
        float t = Math.clamp(time, 0.0f, 1.0f);
        return 1.0f - (1.0f - t) * (1.0f - t) * (1.0f - t);
    }
}

