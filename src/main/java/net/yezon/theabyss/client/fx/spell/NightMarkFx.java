package net.yezon.theabyss.client.fx.spell;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.client.fx.NightGraceClient;

public final class NightMarkFx extends Effect {
    private static final int SHADOW_BLACK = 0x120818;
    private static final int SHADOW_VIOLET = 0x2E1446;
    private static final int WISP = 0x8A4BFF;
    private static final int BURST = 0x9B5CFF;
    private static final int OUTLINE_DARK = 0x9A4DFF;
    private static final int OUTLINE_LIGHT = 0xF0E2FF;
    private static final int BURST_TICKS = 14;
    private static final RandomSource RANDOM = RandomSource.create();

    private final int mob;
    private final int grace;
    private boolean burst;

    public NightMarkFx(int mob, int grace) {
        this.mob = mob;
        this.grace = Math.max(1, grace);
    }

    @Override
    public float life() {
        return this.grace + BURST_TICKS;
    }

    private float progress() {
        return Mth.clamp(this.age() / this.grace, 0.0F, 1.0F);
    }

    @Override
    public int outline(Entity entity) {
        if (entity.getId() != this.mob || this.age() >= this.grace) {
            return 0;
        }
        float mix = Mth.clamp(0.3F * this.progress() + 0.7F * NightGraceClient.pulse(), 0.0F, 1.0F);
        return ARGB.opaque(ARGB.srgbLerp(mix, OUTLINE_DARK, OUTLINE_LIGHT));
    }

    @Override
    public void tick(ClientLevel level) {
        Entity e = level.getEntity(this.mob);
        if (e == null || !e.isAlive()) {
            this.kill();
            return;
        }
        double w = e.getBbWidth() * 0.5 + 0.2;
        double h = e.getBbHeight();
        if (this.age() < this.grace) {
            int puffs = this.progress() > 0.7F ? 3 : 2;
            for (int i = 0; i < puffs; i++) {
                double a = RANDOM.nextDouble() * Math.PI * 2.0;
                Vec3 at = e.position().add(Math.cos(a) * w, RANDOM.nextDouble() * h * 0.9, Math.sin(a) * w);
                emit(level, smoke(RANDOM.nextBoolean() ? SHADOW_BLACK : SHADOW_VIOLET, 0.35F + RANDOM.nextFloat() * 0.25F, 22 + RANDOM.nextInt(10)),
                    at, new Vec3(Math.cos(a) * 0.01, 0.025 + RANDOM.nextDouble() * 0.02, Math.sin(a) * 0.01));
            }
            if (e.tickCount % 3 == 0) {
                double a = RANDOM.nextDouble() * Math.PI * 2.0;
                emit(level, wisp(WISP, 0.18F, 18), e.position().add(Math.cos(a) * w * 0.8, RANDOM.nextDouble() * h, Math.sin(a) * w * 0.8),
                    new Vec3(0.0, 0.04, 0.0));
            }
        } else if (!this.burst) {
            this.burst = true;
            Vec3 centre = e.position().add(0.0, h * 0.5, 0.0);
            for (int i = 0; i < 22; i++) {
                double a = RANDOM.nextDouble() * Math.PI * 2.0;
                double up = RANDOM.nextDouble() * 0.12 - 0.02;
                emit(level, smoke(RANDOM.nextBoolean() ? SHADOW_BLACK : SHADOW_VIOLET, 0.5F + RANDOM.nextFloat() * 0.3F, 20 + RANDOM.nextInt(10)),
                    centre, new Vec3(Math.cos(a) * 0.16, up, Math.sin(a) * 0.16));
            }
            for (int i = 0; i < 10; i++) {
                double a = RANDOM.nextDouble() * Math.PI * 2.0;
                emit(level, sparkle(OUTLINE_LIGHT, 0.14F, 12), centre, new Vec3(Math.cos(a) * 0.22, RANDOM.nextDouble() * 0.15, Math.sin(a) * 0.22));
            }
            emit(level, groundRing(OUTLINE_DARK, 2.6F, 14), e.position().add(0.0, 0.05, 0.0), Vec3.ZERO);
        }
    }

    @Override
    public void draw(Frame f) {
        Entity e = f.level.getEntity(this.mob);
        if (e == null || !e.isAlive()) {
            return;
        }
        float age = this.age();
        if (age >= this.grace) {
            Vec3 centre = e.getPosition(f.pt).add(0.0, e.getBbHeight() * 0.5, 0.0);
            float k = 1.0F - Mth.clamp((age - this.grace) / BURST_TICKS, 0.0F, 1.0F);
            f.flare(centre, 3.2F * (1.5F - k), BURST, 0.6F * k);
        }
    }
}
