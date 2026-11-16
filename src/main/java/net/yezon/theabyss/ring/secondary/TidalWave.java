package net.yezon.theabyss.ring.secondary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.util.FallGuard;
import net.yezon.theabyss.util.RingMode;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class TidalWave {
    public static final double START = 1.5;
    public static final double LENGTH = 18.0;
    public static final double SPEED = 0.6;
    public static final double HALF_WIDTH = 3.0;
    public static final double SPREAD = 0.15;

    private static final List<Wave> WAVES = new ArrayList<>();

    private record Wave(ServerLevel level, ServerPlayer caster, Vec3 origin, Vec3 dir, int[] age) {
    }

    private TidalWave() {
    }

    public static int duration() {
        return (int)Math.ceil((LENGTH - START) / SPEED);
    }

    public static double front(double age) {
        return START + SPEED * age;
    }

    public static double halfWidth(double front) {
        return HALF_WIDTH + front * SPREAD;
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.level();
        Vec3 dir = player.getLookAngle().multiply(1.0, 0.0, 1.0);
        dir = dir.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : dir.normalize();
        Vec3 origin = player.position();
        WAVES.add(new Wave(level, player, origin, dir, new int[1]));
        List<Double> data = new ArrayList<>(SpellFxMessage.points(origin));
        data.add(dir.x);
        data.add(dir.z);
        SpellFxMessage.around(level, origin, SpellFxMessage.TIDAL, List.of(player.getId()), data, duration());
        RingVisuals.sound(level, origin, SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 1.6F, 0.6F);
        RingVisuals.sound(level, origin, SoundEvents.BUCKET_EMPTY, 1.4F, 0.5F);
        RingVisuals.sound(level, origin, SoundEvents.ELDER_GUARDIAN_AMBIENT, 0.6F, 1.6F);
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        for (Iterator<Wave> it = WAVES.iterator(); it.hasNext(); ) {
            Wave wave = it.next();
            int age = ++wave.age()[0];
            if (age > duration()) {
                it.remove();
                continue;
            }
            roll(wave, age);
        }
    }

    private static void roll(Wave wave, int age) {
        ServerLevel level = wave.level();
        Vec3 dir = wave.dir();
        Vec3 side = new Vec3(-dir.z, 0.0, dir.x);
        double front = front(age);
        double half = halfWidth(front);
        Vec3 crest = wave.origin().add(dir.scale(front));
        AABB box = new AABB(crest, crest).inflate(half + 1.0, 3.0, half + 1.0).expandTowards(dir.scale(-3.5));
        for (Entity e : level.getEntities(wave.caster(), box, e -> e.isAlive() && !e.isSpectator()
            && (e instanceof ItemEntity || e instanceof LivingEntity && !RingMode.spares(wave.caster(), e)))) {
            Vec3 rel = e.position().subtract(wave.origin());
            double along = rel.dot(dir);
            double across = Math.abs(rel.dot(side));
            if (along < front - 3.4 || along > front + 1.2 || across > half) {
                continue;
            }
            e.setDeltaMovement(dir.scale(SPEED * 1.6).add(0.0, 0.32, 0.0));
            e.resetFallDistance();
            e.clearFire();
            e.needsSync = true;
            e.syncVelocity = true;
            if (e instanceof LivingEntity living) {
                FallGuard.protect(living, 60);
            }
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (double s = -half; s <= half; s += 1.0) {
            Vec3 at = crest.add(side.scale(s));
            for (int dy = -2; dy <= 2; dy++) {
                pos.set(at.x, at.y + dy, at.z);
                if (level.getBlockState(pos).getBlock() instanceof BaseFireBlock) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, net.minecraft.sounds.SoundSource.BLOCKS, 0.6F, 1.4F);
                }
            }
        }
        if (age % 5 == 0) {
            RingVisuals.sound(level, crest, SoundEvents.PLAYER_SPLASH, 1.4F, 0.6F + age * 0.01F);
        }
        if (age == duration()) {
            RingVisuals.sound(level, crest, SoundEvents.GENERIC_SPLASH, 1.8F, 0.6F);
        }
    }
}
