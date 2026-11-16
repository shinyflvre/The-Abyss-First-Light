package net.yezon.theabyss.boss.nightblade;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModBlocks;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.network.NightbladeArenaMessage;
import net.yezon.theabyss.network.NightbladeImpactMessage;

final class NightbladeArena {
    static final double RADIUS = 9.5;
    private static final double SPACING = 4.0;
    private static final int DISSOLVE_TICKS = 20;

    private final Vec3 centre;
    private final int duration;
    private final boolean red;
    private final List<BlockPos> floor = new ArrayList<>();
    private final Map<LivingEntity, Vec3> origins = new LinkedHashMap<>();
    private final Set<LivingEntity> caught = new java.util.HashSet<>();
    private int age;
    private int dissolveStart = -1;

    private NightbladeArena(Vec3 centre, int duration, boolean red) {
        this.centre = centre;
        this.duration = duration;
        this.red = red;
    }

    static NightbladeArena create(NightbladeCombat c, ServerLevel level, LivingEntity target) {
        Vec3 bossAt = c.boss.position();
        Vec3 targetAt = target.position();
        double ground = Math.max(bossAt.y, targetAt.y);
        Vec3 middle = new Vec3((bossAt.x + targetAt.x) * 0.5, ground, (bossAt.z + targetAt.z) * 0.5);
        double top = Double.NaN;
        for (int up = 10; up <= 26 && Double.isNaN(top); up++) {
            if (clear(level, middle.x, Math.floor(ground) + up, middle.z)) {
                top = Math.floor(ground) + up;
            }
        }
        if (Double.isNaN(top)) {
            return null;
        }
        int duration = c.phase() == 1 ? 240 : c.phase() == 2 ? 280 : 320;
        NightbladeArena arena = new NightbladeArena(new Vec3(middle.x, top, middle.z), duration, c.phase() == 3);
        arena.build(level);
        arena.pull(c, level, target);
        NightbladeArenaMessage.around(level, arena.centre, (float)RADIUS, duration, arena.red);
        return arena;
    }

    private static boolean clear(ServerLevel level, double x, double top, double z) {
        for (int i = 0; i < 13; i++) {
            double angle = i * Math.PI * 2.0 / 12.0;
            double r = i == 12 ? 0.0 : (i % 2 == 0 ? RADIUS : RADIUS * 0.5);
            BlockPos column = BlockPos.containing(x + Math.cos(angle) * r, top, z + Math.sin(angle) * r);
            for (int dy = -1; dy <= 3; dy++) {
                if (!level.getBlockState(column.above(dy)).isAir()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void build(ServerLevel level) {
        int r = (int)Math.ceil(RADIUS);
        BlockPos base = BlockPos.containing(this.centre.x, this.centre.y - 1.0, this.centre.z);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz > RADIUS * RADIUS) {
                    continue;
                }
                BlockPos pos = base.offset(dx, 0, dz);
                if (level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, TheabyssModBlocks.SHADOW_PLATFORM.get().defaultBlockState(), 2);
                    this.floor.add(pos);
                }
            }
        }
    }

    private void pull(NightbladeCombat c, ServerLevel level, LivingEntity target) {
        Vec3 axis = target.position().subtract(c.boss.position()).multiply(1.0, 0.0, 1.0);
        axis = axis.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : axis.normalize();
        Vec3 side = new Vec3(-axis.z, 0.0, axis.x);
        List<LivingEntity> foes = new ArrayList<>();
        foes.add(target);
        for (LivingEntity enemy : c.enemiesAround(20.0)) {
            if (enemy instanceof Player && enemy != target) {
                foes.add(enemy);
            }
        }
        this.lift(level, c.boss, this.centre.subtract(axis.scale(SPACING)), axis);
        for (int i = 0; i < foes.size(); i++) {
            double offset = i == 0 ? 0.0 : (i % 2 == 1 ? 1 : -1) * 1.8 * ((i + 1) / 2);
            this.lift(level, foes.get(i), this.centre.add(axis.scale(SPACING)).add(side.scale(offset)), axis.scale(-1.0));
        }
        c.shake(level, 32.0, 1.6F, 0.6F);
        level.playSound(null, this.centre.x, this.centre.y, this.centre.z, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.HOSTILE, 1.6F, 0.6F);
        level.playSound(null, this.centre.x, this.centre.y, this.centre.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.4F, 0.5F);
    }

    private void lift(ServerLevel level, LivingEntity entity, Vec3 to, Vec3 facing) {
        Vec3 from = entity.position();
        this.origins.put(entity, from);
        float yaw = (float)Math.toDegrees(Math.atan2(-facing.x, facing.z));
        this.teleport(level, entity, to, yaw);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, from.x, from.y + 1.0, from.z, 20, 0.3, 1.0, 0.3, 0.05);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, from.x, from.y + 1.0, from.z, 30, 0.3, 1.0, 0.3, 0.3);
        level.sendParticles(TheabyssModParticleTypes.END_SWORD_PT.get(), to.x, to.y + 1.0, to.z, 16, 0.4, 0.8, 0.4, 0.05);
    }

    private void teleport(ServerLevel level, LivingEntity entity, Vec3 to, float yaw) {
        if (entity instanceof ServerPlayer player) {
            player.teleportTo(level, to.x, to.y, to.z, Set.of(), yaw, player.getXRot(), true);
            NightbladeImpactMessage.send(player, false);
        } else {
            entity.teleportTo(to.x, to.y, to.z);
            entity.setYRot(yaw);
            entity.setYHeadRot(yaw);
            entity.setYBodyRot(yaw);
        }
        entity.setDeltaMovement(Vec3.ZERO);
        entity.resetFallDistance();
    }

    boolean tick(NightbladeCombat c, ServerLevel level) {
        this.age++;
        if (this.dissolveStart < 0) {
            if (this.age % 3 == 0) {
                double angle = c.boss.getRandom().nextDouble() * Math.PI * 2.0;
                double x = this.centre.x + Math.cos(angle) * RADIUS;
                double z = this.centre.z + Math.sin(angle) * RADIUS;
                level.sendParticles(ParticleTypes.SMOKE, x, this.centre.y - 0.2, z, 3, 0.2, 0.05, 0.2, 0.01);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, this.centre.y + 0.1, z, 1, 0.1, 0.05, 0.1, 0.0);
            }
            for (LivingEntity entity : this.origins.keySet()) {
                if (entity.isAlive() && entity.getY() < this.centre.y - 2.0 && this.caught.add(entity)) {
                    entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, false));
                }
            }
            if (this.age >= this.duration) {
                this.end(level);
            }
            return false;
        }
        int step = this.age - this.dissolveStart;
        double keep = RADIUS * (1.0 - step / (double)DISSOLVE_TICKS);
        this.floor.removeIf(pos -> {
            double dx = pos.getX() + 0.5 - this.centre.x;
            double dz = pos.getZ() + 0.5 - this.centre.z;
            if (dx * dx + dz * dz < keep * keep) {
                return false;
            }
            this.removeBlock(level, pos);
            return true;
        });
        return this.floor.isEmpty() || step >= DISSOLVE_TICKS;
    }

    private void end(ServerLevel level) {
        this.dissolveStart = this.age;
        NightbladeArenaMessage.around(level, this.centre, (float)RADIUS, 0, this.red);
        this.returnAll(level);
        level.playSound(null, this.centre.x, this.centre.y, this.centre.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.4F, 0.7F);
    }

    private void returnAll(ServerLevel level) {
        for (Map.Entry<LivingEntity, Vec3> entry : this.origins.entrySet()) {
            LivingEntity entity = entry.getKey();
            if (!entity.isAlive() || entity.level() != level) {
                continue;
            }
            Vec3 at = entity.position();
            level.sendParticles(ParticleTypes.LARGE_SMOKE, at.x, at.y + 1.0, at.z, 16, 0.3, 0.8, 0.3, 0.05);
            this.teleport(level, entity, entry.getValue(), entity.getYRot());
            entity.removeEffect(MobEffects.SLOW_FALLING);
        }
        this.origins.clear();
    }

    void cancel(ServerLevel level) {
        if (this.dissolveStart < 0) {
            NightbladeArenaMessage.around(level, this.centre, (float)RADIUS, 0, this.red);
            this.returnAll(level);
        }
        for (BlockPos pos : this.floor) {
            this.removeBlock(level, pos);
        }
        this.floor.clear();
    }

    private void removeBlock(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).is(TheabyssModBlocks.SHADOW_PLATFORM.get())) {
            level.removeBlock(pos, false);
        }
    }
}
