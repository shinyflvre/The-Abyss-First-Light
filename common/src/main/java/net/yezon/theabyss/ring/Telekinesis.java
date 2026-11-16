package net.yezon.theabyss.ring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.tick.ServerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.NightbladeImpactMessage;
import net.yezon.theabyss.network.NightbladeShakeMessage;
import net.yezon.theabyss.network.TelekinesisMessage;
import net.yezon.theabyss.ring.fx.RingFx;
import net.yezon.theabyss.util.FallGuard;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class Telekinesis {
    public static final int MAX_TARGETS = 12;
    public static final double RADIUS = 30.0;
    public static final int GAP = 2;
    public static final int FLIGHT = 6;
    public static final int LIFT = 22;
    public static final int HANG = 16;
    public static final double HEIGHT = 7.0;
    public static final double SLAM_SPEED = 3.2;
    public static final int SLAM_MAX = 16;
    public static final int COLOUR = 0x9A3CF0;

    private static final List<Cast> CASTS = new ArrayList<>();

    private Telekinesis() {
    }

    public static int launch(int i) {
        return 1 + i * GAP;
    }

    public static int latch(int i) {
        return launch(i) + FLIGHT;
    }

    public static int slam(int count) {
        return latch(count - 1) + LIFT + HANG;
    }

    private static final class Bound {
        final LivingEntity target;
        final int index;
        Vec3 anchor;
        double startY;
        double lastY;
        boolean landed;

        Bound(LivingEntity target, int index) {
            this.target = target;
            this.index = index;
        }
    }

    private static final class Cast {
        final ServerLevel level;
        final LivingEntity caster;
        final List<Bound> bound = new ArrayList<>();
        final float damage;
        final int slamTick;
        int age;
        boolean blowLanded;

        Cast(ServerLevel level, LivingEntity caster, List<LivingEntity> targets, float damage) {
            this.level = level;
            this.caster = caster;
            this.damage = damage;
            for (int i = 0; i < targets.size(); i++) {
                this.bound.add(new Bound(targets.get(i), i));
            }
            this.slamTick = slam(targets.size());
        }
    }

    public static void start(ServerLevel level, LivingEntity caster, List<LivingEntity> targets, float damage) {
        Cast cast = new Cast(level, caster, targets, damage);
        CASTS.add(cast);
        TelekinesisMessage.around(level, caster.position(), new TelekinesisMessage(TelekinesisMessage.START, caster.getId(),
            targets.stream().map(LivingEntity::getId).toList()));
        Vec3 at = caster.position();
        sound(level, at, SoundEvents.ELDER_GUARDIAN_CURSE, 1.0F, 0.55F);
        sound(level, at, SoundEvents.TRIAL_SPAWNER_OMINOUS_ACTIVATE, 1.6F, 0.6F);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        for (Iterator<Cast> it = CASTS.iterator(); it.hasNext(); ) {
            Cast cast = it.next();
            cast.age++;
            if (!tick(cast)) {
                it.remove();
            }
        }
    }

    private static boolean tick(Cast cast) {
        ServerLevel level = cast.level;
        int age = cast.age;
        boolean busy = false;
        if (age == 1 && cast.caster.isAlive()) {
            cast.caster.swing(RingMagic.swingHand(cast.caster), SwingAnimation.DEFAULT, true);
        }
        if (age == cast.slamTick - HANG) {
            sound(level, cast.caster.position(), SoundEvents.WARDEN_SONIC_CHARGE, 1.6F, 1.25F);
        }
        if (age == cast.slamTick) {
            sound(level, cast.caster.position(), SoundEvents.WARDEN_SONIC_BOOM, 1.4F, 0.75F);
            if (cast.caster.isAlive()) {
                cast.caster.swing(RingMagic.swingHand(cast.caster), SwingAnimation.DEFAULT, true);
            }
        }
        for (Bound b : cast.bound) {
            if (b.landed) {
                continue;
            }
            LivingEntity t = b.target;
            if (!t.isAlive() || t.level() != level) {
                b.landed = true;
                TelekinesisMessage.around(level, t.position(), new TelekinesisMessage(TelekinesisMessage.IMPACT, cast.caster.getId(), List.of(t.getId())));
                continue;
            }
            busy = true;
            int launch = launch(b.index);
            int latch = latch(b.index);
            if (age == launch) {
                sound(level, cast.caster.position(), SoundEvents.CHAIN_PLACE, 1.4F, 0.55F + level.getRandom().nextFloat() * 0.15F);
                sound(level, cast.caster.position(), SoundEvents.TRIDENT_THROW.value(), 0.9F, 0.6F);
            }
            if (age < latch) {
                continue;
            }
            RingVisuals.cursedTick(level, t);
            if (age == latch) {
                b.anchor = t.position();
                b.startY = t.getY();
                b.lastY = t.getY();
                sound(level, t.position(), SoundEvents.CHAIN_HIT, 1.6F, 0.5F);
                sound(level, t.position(), SoundEvents.ANVIL_LAND, 0.5F, 0.6F);
                FallGuard.protect(t, cast.slamTick - age + SLAM_MAX + 40);
            }
            if (age < cast.slamTick) {
                hold(cast, b, age - latch);
            } else {
                slam(cast, b, age - cast.slamTick);
            }
        }
        return busy && age < cast.slamTick + SLAM_MAX + 4;
    }

    private static void hold(Cast cast, Bound b, int since) {
        LivingEntity t = b.target;
        double k = Math.min(1.0, since / (double)LIFT);
        double ease = k * k * (3.0 - 2.0 * k);
        double wantY = b.startY + HEIGHT * ease;
        double jitterX = 0.0;
        double jitterZ = 0.0;
        if (since > LIFT) {
            double strain = Math.min(1.0, (since - LIFT) / (double)HANG);
            jitterX = (t.getRandom().nextDouble() - 0.5) * 0.16 * strain;
            jitterZ = (t.getRandom().nextDouble() - 0.5) * 0.16 * strain;
            wantY += Math.sin(since * 1.7) * 0.04;
            if (since % 5 == 0) {
                sound(cast.level, t.position(), SoundEvents.CHAIN_STEP, 0.8F, 0.5F + (float)strain * 0.4F);
            }
        } else if (since % 6 == 0) {
            sound(cast.level, t.position(), SoundEvents.CHAIN_STEP, 0.9F, 0.6F);
        }
        Vec3 pos = t.position();
        t.setDeltaMovement((b.anchor.x - pos.x) * 0.45 + jitterX, (wantY - pos.y) * 0.55 + 0.04, (b.anchor.z - pos.z) * 0.45 + jitterZ);
        t.resetFallDistance();
        t.setYRot(t.getYRot() + 6.0F);
        t.setYBodyRot(t.getYRot());
        t.setYHeadRot(t.getYRot());
        sync(t);
    }

    private static void slam(Cast cast, Bound b, int since) {
        LivingEntity t = b.target;
        boolean stuck = since > 2 && b.lastY - t.getY() < 0.05;
        if (since > 0 && (t.onGround() || stuck || since >= SLAM_MAX)) {
            impact(cast, b);
            return;
        }
        b.lastY = t.getY();
        t.setDeltaMovement(0.0, -SLAM_SPEED, 0.0);
        t.resetFallDistance();
        sync(t);
    }

    private static void impact(Cast cast, Bound b) {
        b.landed = true;
        ServerLevel level = cast.level;
        LivingEntity t = b.target;
        Vec3 at = t.position();
        t.setDeltaMovement(0.0, 0.0, 0.0);
        t.resetFallDistance();
        sync(t);
        t.damageCooldownTime = 0;
        t.hurtServer(level, level.damageSources().indirectMagic(cast.caster, cast.caster), cast.damage);
        if (!cast.blowLanded) {
            cast.blowLanded = true;
            if (level.getRandom().nextBoolean()) {
                for (ServerPlayer player : level.players()) {
                    if (player.distanceToSqr(at) <= 48.0 * 48.0) {
                        NightbladeImpactMessage.send(player, false);
                    }
                }
            }
        }
        TelekinesisMessage.around(level, at, new TelekinesisMessage(TelekinesisMessage.IMPACT, cast.caster.getId(), List.of(t.getId())));
        RingFx.wave(level, at.add(0.0, 0.1, 0.0), 5.5F, 18, COLOUR);
        NightbladeShakeMessage.around(level, at, 18.0, 0.8F, 0.45F);
        BlockState ground = level.getBlockState(BlockPos.containing(at.x, at.y - 0.5, at.z));
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), at.x, at.y + 0.1, at.z, 36, 0.9, 0.1, 0.9, 0.25);
        }
        sound(level, at, SoundEvents.MACE_SMASH_GROUND_HEAVY, 1.8F, 0.75F);
        sound(level, at, SoundEvents.GENERIC_EXPLODE.value(), 0.9F, 0.55F + level.getRandom().nextFloat() * 0.1F);
        sound(level, at, SoundEvents.CHAIN_BREAK, 1.4F, 0.5F);
    }

    private static void sync(LivingEntity t) {
        t.needsSync = true;
        t.syncVelocity = true;
    }

    private static void sound(ServerLevel level, Vec3 at, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, at.x, at.y, at.z, sound, SoundSource.PLAYERS, volume, Mth.clamp(pitch, 0.5F, 2.0F));
    }
}
