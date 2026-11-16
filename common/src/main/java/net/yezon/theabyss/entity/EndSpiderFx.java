package net.yezon.theabyss.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.FxOptions;
import net.yezon.theabyss.ring.fx.GlowParticleOptions;
import net.yezon.theabyss.ring.fx.RingFx;

public final class EndSpiderFx {
    public static final int EYE = 0x90FFFA;
    private static final int EYE_CORE = 0xE8FFFE;
    public static final GlowParticleOptions AMBIENT = GlowParticleOptions.of(EYE, 0.03F, 20).drag(0.9F)
        .flags(GlowParticleOptions.SHRINK | GlowParticleOptions.FADE_IN);
    private static final GlowParticleOptions GLINT = GlowParticleOptions.of(EYE, 0.05F, 14);
    private static final GlowParticleOptions EYE_FLASH = GlowParticleOptions.of(EYE_CORE, 0.1F, 5);

    private EndSpiderFx() {
    }

    public static FxOptions ambientShadow() {
        return RingVisuals.smoke(RingVisuals.STEP_SMOKE_DARK, 0.18F, 22).gravity(-0.003F).drag(0.9F);
    }

    public static void charge(ServerLevel level, Entity spider, int step) {
        Vec3 centre = spider.position();
        double radius = 1.3 - step * 0.08;
        double turn = step * 0.6;
        FxOptions shadow = RingVisuals.smoke(RingVisuals.STEP_SMOKE_DARK, 0.35F, 14).drag(0.85F);
        for (int i = 0; i < 3; i++) {
            double a = turn + i * Math.PI * 2.0 / 3.0;
            Vec3 at = centre.add(Math.cos(a) * radius, 0.1 + step * 0.05, Math.sin(a) * radius);
            Vec3 motion = new Vec3(-Math.cos(a), 0.0, -Math.sin(a)).scale(0.05).add(new Vec3(-Math.sin(a), 0.0, Math.cos(a)).scale(0.04));
            RingFx.emit(level, shadow, at, motion);
        }
        if (step % 2 == 0) {
            RingFx.emit(level, GLINT, spider.getEyePosition().add(spider.getForward().scale(0.6)), Vec3.ZERO);
        }
    }

    public static void teleport(ServerLevel level, Entity spider, Vec3 from, Vec3 to) {
        Vec3 eyes = to.add(0.0, spider.getEyeHeight(), 0.0);
        RingVisuals.teleportStepArrive(level, from);
        RingVisuals.teleportStepTrail(level, from, to);
        RingVisuals.teleportStepArrive(level, to);
        RingFx.burst(level, EYE_FLASH, eyes, 4, 0.15, 0.0);
        RingFx.burst(level, GLINT, eyes, 8, 0.3, 0.02);
        if (!spider.isSilent()) {
            level.playSound(null, from.x, from.y, from.z, SoundEvents.PLAYER_TELEPORT, spider.getSoundSource(), 0.8F, 0.6F);
            level.playSound(null, from.x, from.y, from.z, SoundEvents.SOUL_ESCAPE.value(), spider.getSoundSource(), 1.0F, 0.6F);
            level.playSound(null, to.x, to.y, to.z, SoundEvents.PLAYER_TELEPORT, spider.getSoundSource(), 0.7F, 0.7F);
            level.playSound(null, to.x, to.y, to.z, SoundEvents.SOUL_ESCAPE.value(), spider.getSoundSource(), 1.0F, 0.7F);
        }
    }
}
