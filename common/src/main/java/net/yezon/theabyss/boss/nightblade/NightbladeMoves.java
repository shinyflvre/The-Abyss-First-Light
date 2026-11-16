package net.yezon.theabyss.boss.nightblade;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.entity.SeekerEntity;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.init.TheabyssModParticleTypes;

final class NightbladeMoves {
    private NightbladeMoves() {
    }

    static List<NightbladeMove> all() {
        return List.of(new SignatureStrike(), new BlinkDance(), new GravityRift(), new RepulseBurst(), new MirrorImages(), new EclipseCircle(), new GroundSlam(),
            new FangLines(), new StormMarks(), new StormField(), new ShadowStep(), new CrescentSweep(), new SummonSeekers(), new CounterStance(), new BladeBoomerang());
    }

    static List<NightbladeMove> mirrorMoves() {
        NightbladeMove signature = new SignatureStrike();
        NightbladeMove dance = new BlinkDance();
        signature.cooldownLeft = 0;
        dance.cooldownLeft = 0;
        return List.of(signature, dance);
    }

    private static void look(NightbladeCombat c, Entity at) {
        c.boss.lookAt(EntityAnchorArgument.Anchor.EYES, at.getEyePosition());
        c.boss.getLookControl().setLookAt(at, 90.0F, 90.0F);
    }

    private static void swing(NightbladeCombat c, InteractionHand hand) {
        c.boss.swing(hand, SwingAnimation.DEFAULT, true);
    }

    private static void dashStep(NightbladeCombat c, Vec3 from, Vec3 to, int step, int steps) {
        Vec3 position = from.lerp(to, (step + 1) / (double)steps);
        Vec3 free = c.freeSpotNear(position, 3.0);
        if (free != null) {
            c.boss.setPos(free.x, free.y, free.z);
        }
        c.boss.setDeltaMovement(to.subtract(from).scale(1.0 / steps));
        c.boss.needsSync = true;
    }

    private static Vec3 horizontal(Vec3 v) {
        Vec3 flat = new Vec3(v.x, 0.0, v.z);
        return flat.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : flat.normalize();
    }

    private static void push(NightbladeCombat c, LivingEntity victim, Vec3 direction, double power) {
        victim.knockback(power, -direction.x, -direction.z, c.meleeDamage(), 0.0F);
        victim.needsSync = true;
    }

    private static final class Blinker {
        final List<Vec3> offsets = new ArrayList<>();
        private int start;
        private int segmentTicks;
        private int segment;
        private boolean grounded;
        private Vec3 from;
        private Vec3 to;
        private boolean animated;

        Blinker animated() {
            this.animated = true;
            return this;
        }

        void begin(int startTick, int segmentTicks, boolean grounded) {
            this.start = startTick;
            this.segmentTicks = segmentTicks;
            this.grounded = grounded;
            this.segment = -1;
        }

        int duration() {
            return this.offsets.size() * this.segmentTicks;
        }

        boolean tick(NightbladeCombat c, LivingEntity target, int t) {
            int index = (t - this.start) / this.segmentTicks;
            if (index >= this.offsets.size()) {
                return true;
            }
            if (index != this.segment) {
                this.segment = index;
                this.from = c.boss.position();
                this.to = this.around(c, target, this.offsets.get(index));
                c.sound(NightbladeCombat.dashSound(), 0.9F, 1.1F + c.boss.getRandom().nextFloat() * 0.5F);
                if (this.animated) {
                    c.animate(index % 2 == 0 ? "signature_blink_a" : "signature_blink_b", true, 0);
                }
            }
            dashStep(c, this.from, this.to, (t - this.start) % this.segmentTicks, this.segmentTicks);
            return false;
        }

        private Vec3 around(NightbladeCombat c, LivingEntity target, Vec3 offset) {
            Vec3 forward = horizontal(target.position().subtract(c.boss.position()));
            Vec3 side = new Vec3(-forward.z, 0.0, forward.x);
            Vec3 point = target.position().add(side.scale(offset.x)).add(forward.scale(offset.z));
            if (this.grounded) {
                double y = c.groundY(point.x, target.getY(), point.z);
                return new Vec3(point.x, Double.isNaN(y) ? target.getY() : y, point.z);
            }
            return point.add(0.0, offset.y, 0.0);
        }
    }

    private static final class Lunge {
        private static final int STEPS = 5;
        private final Set<Integer> hit = new HashSet<>();
        private boolean parryable;
        private boolean parried;
        private Vec3 from;
        private Vec3 to;
        private int start = -1;
        private float damage;

        void begin(NightbladeCombat c, LivingEntity target, int t, float damage) {
            this.hit.clear();
            this.start = t;
            this.damage = damage;
            this.from = c.boss.position();
            Vec3 aim = target.getBoundingBox().getCenter().add(c.targetVelocity().scale(2.0)).subtract(0.0, c.boss.getBbHeight() * 0.5, 0.0);
            this.to = aim.add(aim.subtract(this.from).normalize().scale(2.5));
            c.sound(NightbladeCombat.dashSound(), 1.4F, 0.8F);
            if (this.parryable) {
                c.animate("signature_lunge", true, 0);
                double stepLength = this.to.subtract(this.from).length() / STEPS;
                double reach = c.boss.getBbWidth() * 0.5 + 1.0 + target.getBbWidth() * 0.5;
                double distance = target.getBoundingBox().getCenter().distanceTo(c.boss.getBoundingBox().getCenter());
                c.parryCue(target, (int)Math.ceil(Math.max(0.0, distance - reach) / Math.max(0.1, stepLength)) + 1);
            }
        }

        boolean started() {
            return this.start >= 0;
        }

        void reset() {
            this.start = -1;
            this.parried = false;
        }

        Lunge parryable() {
            this.parryable = true;
            return this;
        }

        boolean wasParried() {
            return this.parried;
        }

        boolean tick(NightbladeCombat c, ServerLevel level, int t) {
            int step = t - this.start;
            if (step >= STEPS) {
                return true;
            }
            dashStep(c, this.from, this.to, step, STEPS);
            swing(c, step % 2 == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            Vec3 direction = this.to.subtract(this.from).normalize();
            for (LivingEntity victim : c.enemiesIn(c.boss.getBoundingBox().inflate(1.0))) {
                if (this.hit.add(victim.getId())) {
                    if (this.parryable && victim instanceof net.minecraft.server.level.ServerPlayer player && NightbladeParry.consume(player)) {
                        this.parried = true;
                        c.parried(level, player);
                        return true;
                    }
                    victim.hurtServer(level, c.meleeDamage(), c.scaled(this.damage));
                    push(c, victim, direction, 1.2);
                    c.impactFrame(victim);
                    c.sound(NightbladeCombat.nightbladeSound(), 1.5F, 0.8F);
                    c.particles(level, ParticleTypes.SWEEP_ATTACK, victim.getX(), victim.getY() + 1.0, victim.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
            return false;
        }
    }

    static final class SignatureStrike extends NightbladeMove {
        private final Blinker blinker = new Blinker().animated();
        private final Lunge lunge = new Lunge().parryable();
        private boolean aerial;
        private int recoverStart;
        private int recovery;
        private int segmentTicks;

        SignatureStrike() {
            super("signature_strike", 1, 160);
        }

        @Override
        int cooldown(int phase) {
            return phase == 1 ? 420 : phase == 2 ? 320 : 240;
        }

        @Override
        float score(NightbladeCombat c, NightbladeCombat.Situation s) {
            if (s.distance() < 3.0 || s.distance() > 22.0 || c.stormRaging() || c.bladeOut()) {
                return 0.0F;
            }
            return 3.0F + (s.field() ? 2.5F : 0.0F) + (s.reachable() ? 0.0F : 1.0F);
        }

        @Override
        void start(NightbladeCombat c, ServerLevel level, NightbladeCombat.Situation s) {
            this.aerial = s.airborne() || s.field() || (c.isMirror() && c.boss.getRandom().nextBoolean());
            this.lunge.reset();
            this.recoverStart = -1;
            this.blinker.offsets.clear();
            if (this.aerial) {
                this.blinker.offsets.add(new Vec3(4.0, 3.0, 0.0));
                this.blinker.offsets.add(new Vec3(-4.0, 2.0, 0.0));
                this.blinker.offsets.add(new Vec3(0.0, 4.0, -4.0));
                this.blinker.offsets.add(new Vec3(4.5, 1.5, 1.0));
            } else {
                this.blinker.offsets.add(new Vec3(4.0, 0.0, 0.5));
                this.blinker.offsets.add(new Vec3(-4.0, 0.0, 0.5));
                this.blinker.offsets.add(new Vec3(0.0, 0.0, -4.0));
                this.blinker.offsets.add(new Vec3(3.5, 0.0, -2.0));
            }
            if (s.phase() == 3) {
                this.blinker.offsets.add(this.aerial ? new Vec3(0.0, 3.0, 4.0) : new Vec3(-3.0, 0.0, 2.5));
            }
            this.segmentTicks = s.phase() == 3 ? 3 : 4;
            this.blinker.begin(8, this.segmentTicks, !this.aerial);
            c.sound(NightbladeCombat.modSound("spell_of_telekinetic"), 1.2F, 0.8F);
            c.animate("signature_charge", true, 0);
        }

        @Override
        boolean tick(NightbladeCombat c, ServerLevel level, LivingEntity target, int t) {
            if (!this.lunge.wasParried()) {
                look(c, target);
            }
            if (t < 8) {
                if (t % 2 == 0) {
                    c.particles(level, TheabyssModParticleTypes.END_SWORD_PT.get(), c.boss.getX(), c.boss.getY() + 1.0, c.boss.getZ(), 3, 0.3, 0.5, 0.3, 0.02);
                }
                return false;
            }
            if (t == 8) {
                c.boss.setNoGravity(true);
                c.boss.setDashing(true);
            }
            if (t == 8) {
                c.parryCue(target, this.blinker.offsets.size() * this.segmentTicks + 3);
            }
            if (t == 8 + (this.blinker.offsets.size() - 1) * this.segmentTicks) {
                c.parryCue(target, this.segmentTicks + 3);
            }
            if (!this.lunge.started()) {
                if (this.blinker.tick(c, target, t)) {
                    this.lunge.begin(c, target, t, 14.0F);
                }
                return false;
            }
            if (this.recoverStart < 0) {
                if (this.lunge.tick(c, level, t)) {
                    this.recoverStart = t;
                    c.boss.setDashing(false);
                    c.boss.setNoGravity(false);
                    if (!this.lunge.wasParried()) {
                        c.boss.setDeltaMovement(Vec3.ZERO);
                        c.animate("signature_recover", false, 20);
                        this.recovery = this.aerial ? 25 : 18;
                    } else if (c.boss.getRandom().nextBoolean()) {
                        c.animate("stagger_long", false, 100);
                        this.recovery = 100;
                    } else {
                        c.animate("stagger", false, 36);
                        this.recovery = 40;
                    }
                }
                return false;
            }
            if (this.lunge.wasParried()) {
                c.boss.setDeltaMovement(c.boss.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            }
            return t - this.recoverStart >= this.recovery;
        }

        @Override
        void stop(NightbladeCombat c) {
            c.boss.setDashing(false);
            c.boss.setNoGravity(false);
        }
    }

    static final class BlinkDance extends NightbladeMove {
        private final Blinker blinker = new Blinker();
        private final Lunge lunge = new Lunge();
        private boolean finishWithStrike;
        private int recoverStart;

        BlinkDance() {
            super("blink_dance", 1, 220);
        }

        @Override
        int cooldown(int phase) {
            return phase == 1 ? 300 : phase == 2 ? 240 : 180;
        }

        @Override
        float score(NightbladeCombat c, NightbladeCombat.Situation s) {
            return s.distance() <= 14.0 ? 2.0F + (s.field() ? 1.0F : 0.0F) : 0.0F;
        }

        @Override
        void start(NightbladeCombat c, ServerLevel level, NightbladeCombat.Situation s) {
            boolean aerial = s.airborne() || s.field();
            int count = s.phase() == 1 ? 6 : s.phase() == 2 ? 7 : 9;
            this.blinker.offsets.clear();
            double angle = c.boss.getRandom().nextDouble() * Math.PI * 2.0;
            for (int i = 0; i < count; i++) {
                angle += Math.toRadians(70.0 + c.boss.getRandom().nextDouble() * 150.0);
                double radius = 3.0 + c.boss.getRandom().nextDouble() * 3.0;
                double height = aerial ? 0.5 + c.boss.getRandom().nextDouble() * 3.0 : 0.0;
                this.blinker.offsets.add(new Vec3(Math.cos(angle) * radius, height, Math.sin(angle) * radius));
            }
            this.blinker.begin(4, 3, !aerial);
            this.finishWithStrike = c.boss.getRandom().nextBoolean();
            this.lunge.reset();
            this.recoverStart = -1;
            c.sound(NightbladeCombat.dashSound(), 1.0F, 0.7F);
        }

        @Override
        boolean tick(NightbladeCombat c, ServerLevel level, LivingEntity target, int t) {
            look(c, target);
            if (t < 4) {
                return false;
            }
            if (t == 4) {
                c.boss.setNoGravity(true);
                c.boss.setDashing(true);
            }
            if (!this.lunge.started() && this.recoverStart < 0) {
                if (this.blinker.tick(c, target, t)) {
                    if (this.finishWithStrike) {
                        this.lunge.begin(c, target, t, 11.0F);
                    } else {
                        c.followUp(3, true);
                        return true;
                    }
                }
                return false;
            }
            if (this.recoverStart < 0) {
                if (this.lunge.tick(c, level, t)) {
                    this.recoverStart = t;
                    c.boss.setDashing(false);
                    c.boss.setNoGravity(false);
                    c.boss.setDeltaMovement(Vec3.ZERO);
                }
                return false;
            }
            return t - this.recoverStart >= 14;
        }

        @Override
        void stop(NightbladeCombat c) {
            c.boss.setDashing(false);
            c.boss.setNoGravity(false);
        }
    }

    static final class GravityRift extends NightbladeMove {
        private static final int LIFT = 12;

        GravityRift() {
            super("gravity_rift", 1, 200);
        }

        @Override
        int cooldown(int phase) {
            return phase == 1 ? 460 : phase == 2 ? 400 : 340;
        }

        @Override
        float score(NightbladeCombat c, NightbladeCombat.Situation s) {
            if (c.arenaActive() || c.stormRaging() || s.distance() > 18.0 || s.airborne()) {
                return 0.0F;
            }
            return 3.1F + (s.enemiesNear() >= 2 ? 0.6F : 0.0F);
        }

        @Override
        void start(NightbladeCombat c, ServerLevel level, NightbladeCombat.Situation s) {
            c.animate("arena_lift", false, LIFT + 8);
            c.sound(NightbladeCombat.modSound("spell_of_telekinetic"), 1.6F, 0.5F);
            c.sound(SoundEvents.BEACON_ACTIVATE, 1.2F, 0.5F);
        }

        @Override
        boolean tick(NightbladeCombat c, ServerLevel level, LivingEntity target, int t) {
            look(c, target);
            if (t < LIFT && t % 2 == 0) {
                c.particles(level, TheabyssModParticleTypes.END_SWORD_PT.get(), c.boss.getX(), c.boss.getY() + 0.2, c.boss.getZ(), 6, 1.5, 0.1, 1.5, 0.05);
                c.particles(level, ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY() + 0.2, target.getZ(), 6, 0.6, 0.1, 0.6, 0.1);
            }
            if (t == LIFT) {
                c.startArena(level, target);
                c.followUp(10, false);
            }
            return t >= LIFT + 6;
        }
    }

    static final class RepulseBurst extends NightbladeMove {
        RepulseBurst() {
            super("repulse_burst", 1, 60);
        }

        @Override
        int cooldown(int phase) {
            return phase == 1 ? 200 : phase == 2 ? 160 : 120;
        }

        @Override
        float score(NightbladeCombat c, NightbladeCombat.Situation s) {
            return s.distance() <= 4.5 && c.magicReady() ? 2.4F + (s.enemiesNear() >= 2 ? 0.8F : 0.0F) : 0.0F;
        }

        @Override
        void start(NightbladeCombat c, ServerLevel level, NightbladeCombat.Situation s) {
            c.sound(NightbladeCombat.modSound("spell_of_misc"), 1.2F, 1.2F);
        }

        @Override
        boolean tick(NightbladeCombat c, ServerLevel level, LivingEntity target, int t) {
            look(c, target);
            if (t == 6) {
                swing(c, InteractionHand.OFF_HAND);
                c.sound(SoundEvents.GENERIC_EXPLODE.value(), 0.7F, 1.4F);
                c.sound(SoundEvents.EVOKER_CAST_SPELL, 1.2F, 1.2F);
                for (int i = 0; i < 16; i++) {
                    double angle = i * Math.PI / 8.0;
                    c.particles(level, ParticleTypes.GUST, c.boss.getX() + Math.cos(angle) * 2.0, c.boss.getY() + 0.8, c.boss.getZ() + Math.sin(angle) * 2.0, 1, 0.0, 0.0, 0.0, 0.0);
                }
                for (LivingEntity victim : c.enemiesAround(5.0)) {
                    Vec3 away = horizontal(victim.position().subtract(c.boss.position()));
                    victim.hurtServer(level, c.magicDamage(), c.scaled(3.0F));
                    push(c, victim, away, 2.2);
                    victim.setDeltaMovement(victim.getDeltaMovement().add(0.0, 0.3, 0.0));
                }
                c.followUp(4, true);
            }
            return t >= 12;
        }
    }

    static final class MirrorImages extends NightbladeMove {
        MirrorImages() {
            super("mirror_images", 2, 400);
        }

        @Override
        int cooldown(int phase) {
            return phase == 2 ? 900 : 700;
        }

        @Override
        float score(NightbladeCombat c, NightbladeCombat.Situation s) {
            boolean mirrorsAlive = !c.boss.level().getEntitiesOfClass(net.yezon.theabyss.entity.NightbladeBossEntity.class,
                c.boss.getBoundingBox().inflate(40.0), net.yezon.theabyss.entity.NightbladeBossEntity::isMirror).isEmpty();
            return s.distance() <= 18.0 && !mirrorsAlive ? 2.2F : 0.0F;
        }

        @Override
        void start(NightbladeCombat c, ServerLevel level, NightbladeCombat.Situation s) {
            c.sound(SoundEvents.ILLUSIONER_PREPARE_MIRROR, 1.6F, 0.7F);
        }

        @Override
        boolean tick(NightbladeCombat c, ServerLevel level, LivingEntity target, int t) {
            look(c, target);
            if (t < 10) {
                if (t % 2 == 0) {
                    c.particles(level, ParticleTypes.LARGE_SMOKE, c.boss.getX(), c.boss.getY() + 1.0, c.boss.getZ(), 4, 0.4, 0.8, 0.4, 0.01);
                }
                return false;
            }
            if (t == 10) {
                int count = c.phase() == 3 ? 5 : 2;
                double base = Math.atan2(target.getZ() - c.boss.getZ(), target.getX() - c.boss.getX()) + Math.PI / 2.0;
                for (int i = 0; i < count; i++) {
                    double angle = base + i * Math.PI * 2.0 / count;
                    Vec3 wanted = c.boss.position().add(Math.cos(angle) * 3.0, 0.0, Math.sin(angle) * 3.0);
                    Vec3 at = c.freeSpotNear(wanted, 3.0);
                    if (at == null) {
                        continue;
                    }
                    net.yezon.theabyss.entity.NightbladeBossEntity.spawnMirror(level, c.boss, at, target, c.phase(), 4 + i * (count > 2 ? 8 : 14));
                    c.particles(level, ParticleTypes.LARGE_SMOKE, at.x, at.y + 1.0, at.z, 16, 0.3, 0.8, 0.3, 0.02);
                }
                c.sound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.6F, 0.8F);
                swing(c, InteractionHand.MAIN_HAND);
                c.followUp(10, false);
            }
            return t >= 14;
        }
    }

    static final class EclipseCircle extends NightbladeMove {
        private static final double GAZE_DEGREES = 9.0;
        private static final double SWEEP_STEP_DEGREES = 4.0;
        private static final double RADIUS = 6.0;
        private static final double HEIGHT = 2.5;
        private static final int STRIKE_TICKS = 5;
        private final List<net.yezon.theabyss.entity.NightbladeBossEntity> images = new ArrayList<>();
        private final List<Integer> order = new ArrayList<>();
        private final Lunge lunge = new Lunge();
        private final Set<Integer> hit = new HashSet<>();
        private Vec3 centre;
        private Vec3 rise;
        private Vec3 riseFrom;
        private double turn;
        private int interval;
        private int firstStrike;
        private int striking = -1;
        private int strikeStart;
        private Vec3 strikeFrom;
        private Vec3 strikeTo;
        private int recoverStart;
        private int count;
        private final java.util.Map<Integer, Vec3> lastLook = new java.util.HashMap<>();

        EclipseCircle() {
            super("eclipse_circle", 2, 600);
        }

        @Override
        int cooldown(int phase) {
            return phase == 2 ? 1000 : 800;
        }

        @Override
        float score(NightbladeCombat c, NightbladeCombat.Situation s) {
            if (c.stormRaging() || c.bladeOut()) {
                return 0.0F;
            }
            return s.distance() <= 16.0 ? 2.8F : 0.0F;
        }

        @Override
        void start(NightbladeCombat c, ServerLevel level, NightbladeCombat.Situation s) {
            this.images.clear();
            this.order.clear();
            this.lunge.reset();
            this.striking = -1;
            this.recoverStart = -1;
            this.turn = 0.0;
            this.centre = s.target().position();
            this.count = s.phase() == 3 ? 16 : 12;
            this.lastLook.clear();
            this.interval = s.phase() == 3 ? 5 : 7;
            this.riseFrom = c.boss.position();
            Vec3 above = c.freeSpotNear(this.riseFrom.add(0.0, 6.0, 0.0), 3.0);
            this.rise = above != null ? above : this.riseFrom;
            c.boss.setNoGravity(true);
            c.sound(SoundEvents.ELDER_GUARDIAN_CURSE, 1.0F, 0.6F);
            c.sound(NightbladeCombat.modSound("spell_of_telekinetic"), 1.4F, 0.5F);
        }

        private Vec3 slot(int index) {
            double angle = this.turn + index * Math.PI * 2.0 / this.count;
            return this.centre.add(Math.cos(angle) * RADIUS, HEIGHT, Math.sin(angle) * RADIUS);
        }

        @Override
        boolean tick(NightbladeCombat c, ServerLevel level, LivingEntity target, int t) {
            look(c, target);
            this.centre = this.centre.lerp(target.position(), 0.12);
            this.turn += 0.02;
            if (t < 12) {
                dashStep(c, this.riseFrom, this.rise, t, 12);
                return false;
            }
            if (t == 12) {
                this.summon(c, level);
                this.firstStrike = t + 14;
            }
            this.placeImages(target);
            this.gazeAt(c, level);
            if (!this.lunge.started()) {
                if (this.striking < 0 && !this.order.isEmpty() && t >= this.firstStrike && (t - this.firstStrike) % this.interval == 0) {
                    this.beginStrike(c, target, t);
                }
                if (this.striking >= 0) {
                    this.strike(c, level, t);
                }
                if (t > this.firstStrike && this.order.isEmpty() && this.striking < 0) {
                    c.boss.setDashing(true);
                    this.lunge.begin(c, target, t, 12.0F);
                }
                return false;
            }
            if (this.recoverStart < 0) {
                if (this.lunge.tick(c, level, t)) {
                    this.recoverStart = t;
                    c.boss.setDashing(false);
                    c.boss.setNoGravity(false);
                    c.boss.setDeltaMovement(Vec3.ZERO);
                }
                return false;
            }
            return t - this.recoverStart >= 25;
        }

        private void summon(NightbladeCombat c, ServerLevel level) {
            for (int i = 0; i < this.count; i++) {
                Vec3 at = this.slot(i);
                net.yezon.theabyss.entity.NightbladeBossEntity image = net.yezon.theabyss.entity.NightbladeBossEntity.spawnPuppet(level, c.boss, at);
                this.images.add(image);
                if (image != null) {
                    this.order.add(i);
                    c.particles(level, ParticleTypes.LARGE_SMOKE, at.x, at.y + 1.0, at.z, 10, 0.3, 0.6, 0.3, 0.02);
                }
            }
            java.util.Collections.shuffle(this.order, new java.util.Random(c.boss.getRandom().nextLong()));
            c.sound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.8F, 0.6F);
            c.shake(level, 24.0, 1.2F, 0.8F);
        }

        private void placeImages(LivingEntity target) {
            for (int i = 0; i < this.images.size(); i++) {
                net.yezon.theabyss.entity.NightbladeBossEntity image = this.images.get(i);
                if (image == null || !image.isAlive() || i == this.striking) {
                    continue;
                }
                Vec3 at = this.slot(i);
                image.setPos(at.x, at.y, at.z);
                image.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
                image.setYHeadRot(image.getYRot());
                image.setYBodyRot(image.getYRot());
                image.needsSync = true;
            }
        }

        private void gazeAt(NightbladeCombat c, ServerLevel level) {
            List<Vec3> eyes = new ArrayList<>();
            List<List<Vec3>> sweeps = new ArrayList<>();
            List<net.minecraft.world.entity.player.Player> watchers = new ArrayList<>();
            for (LivingEntity enemy : c.enemiesAround(32.0)) {
                if (enemy instanceof net.minecraft.world.entity.player.Player player) {
                    Vec3 now = player.getLookAngle();
                    Vec3 before = this.lastLook.getOrDefault(player.getId(), now);
                    this.lastLook.put(player.getId(), now);
                    watchers.add(player);
                    eyes.add(player.getEyePosition());
                    sweeps.add(sweep(before, now));
                }
            }
            for (int i = 0; i < this.images.size(); i++) {
                net.yezon.theabyss.entity.NightbladeBossEntity image = this.images.get(i);
                if (image == null || !image.isAlive()) {
                    continue;
                }
                for (int w = 0; w < watchers.size(); w++) {
                    Vec3 to = image.getBoundingBox().getCenter().subtract(eyes.get(w));
                    double length = to.length();
                    if (length < 0.1) {
                        continue;
                    }
                    double cos = Math.cos(Math.max(Math.toRadians(GAZE_DEGREES), Math.atan2(1.1, length)));
                    Vec3 dir = to.scale(1.0 / length);
                    boolean looked = false;
                    for (Vec3 look : sweeps.get(w)) {
                        if (look.dot(dir) >= cos) {
                            looked = true;
                            break;
                        }
                    }
                    if (looked && watchers.get(w).hasLineOfSight(image)) {
                        this.dissolve(level, image, i);
                        break;
                    }
                }
            }
        }

        private static List<Vec3> sweep(Vec3 from, Vec3 to) {
            double angle = Math.acos(Mth.clamp(from.dot(to), -1.0, 1.0));
            int steps = Math.min(48, (int)Math.ceil(Math.toDegrees(angle) / SWEEP_STEP_DEGREES));
            List<Vec3> looks = new ArrayList<>(steps + 1);
            looks.add(to);
            if (steps < 1 || angle > Math.PI - 1.0E-3) {
                return looks;
            }
            double sin = Math.sin(angle);
            for (int k = 0; k < steps; k++) {
                double f = k / (double)steps;
                double a = Math.sin((1.0 - f) * angle) / sin;
                double b = Math.sin(f * angle) / sin;
                looks.add(from.scale(a).add(to.scale(b)));
            }
            return looks;
        }

        private void dissolve(ServerLevel level, net.yezon.theabyss.entity.NightbladeBossEntity image, int index) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE, image.getX(), image.getY() + 1.0, image.getZ(), 14, 0.3, 0.7, 0.3, 0.03);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, image.getX(), image.getY() + 1.0, image.getZ(), 8, 0.3, 0.6, 0.3, 0.02);
            level.playSound(null, image.getX(), image.getY(), image.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.5F);
            image.discard();
            this.order.remove(Integer.valueOf(index));
            if (this.striking == index) {
                this.striking = -1;
            }
        }

        private void beginStrike(NightbladeCombat c, LivingEntity target, int t) {
            while (!this.order.isEmpty()) {
                int index = this.order.remove(0);
                net.yezon.theabyss.entity.NightbladeBossEntity image = this.images.get(index);
                if (image != null && image.isAlive()) {
                    this.striking = index;
                    this.strikeStart = t;
                    this.strikeFrom = image.position();
                    Vec3 aim = target.getBoundingBox().getCenter().subtract(0.0, image.getBbHeight() * 0.5, 0.0);
                    this.strikeTo = aim.add(aim.subtract(this.strikeFrom).normalize().scale(4.0));
                    this.hit.clear();
                    image.setDashing(true);
                    image.playSound(SoundEvents.PHANTOM_SWOOP, 1.2F, 0.7F + c.boss.getRandom().nextFloat() * 0.3F);
                    return;
                }
            }
        }

        private void strike(NightbladeCombat c, ServerLevel level, int t) {
            net.yezon.theabyss.entity.NightbladeBossEntity image = this.images.get(this.striking);
            int step = t - this.strikeStart;
            if (image == null || !image.isAlive()) {
                this.striking = -1;
                return;
            }
            if (step < STRIKE_TICKS) {
                Vec3 at = this.strikeFrom.lerp(this.strikeTo, (step + 1) / (double)STRIKE_TICKS);
                image.setPos(at.x, at.y, at.z);
                image.setDeltaMovement(this.strikeTo.subtract(this.strikeFrom).scale(1.0 / STRIKE_TICKS));
                image.needsSync = true;
                image.swing(step % 2 == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, SwingAnimation.DEFAULT, true);
                Vec3 direction = this.strikeTo.subtract(this.strikeFrom).normalize();
                for (LivingEntity victim : c.enemiesIn(image.getBoundingBox().inflate(0.8))) {
                    if (this.hit.add(victim.getId())) {
                        victim.hurtServer(level, image.damageSources().mobAttack(image), 3.0F);
                        push(c, victim, direction, 0.5);
                        c.sound(NightbladeCombat.nightbladeSound(), 1.0F, 1.3F);
                    }
                }
                return;
            }
            c.particles(level, ParticleTypes.LARGE_SMOKE, image.getX(), image.getY() + 1.0, image.getZ(), 10, 0.3, 0.6, 0.3, 0.02);
            image.discard();
            this.striking = -1;
        }

        @Override
        void stop(NightbladeCombat c) {
            for (net.yezon.theabyss.entity.NightbladeBossEntity image : this.images) {
                if (image != null && image.isAlive()) {
                    image.discard();
                }
            }
            this.images.clear();
            c.boss.setDashing(false);
            c.boss.setNoGravity(false);
        }
    }

    static final class GroundSlam extends NightbladeMove {
        private static final double WAVE_SPEED = 0.8;
        private static final double WAVE_RANGE = 17.0;
        private Vec3 start;
        private double ground;
        private int slamTick = -1;
        private final Set<Integer> hitFirst = new HashSet<>();
        private final Set<Integer> hitSecond = new HashSet<>();
        private BlockState groundState;

        GroundSlam() {
            super("ground_slam", 1, 120);
        }

        @Override
        int cooldown(int phase) {
            return phase == 1 ? 260 : phase == 2 ? 220 : 170;
        }

        @Override
        float score(NightbladeCombat c, NightbladeCombat.Situation s) {
            if (s.distance() > 12.0 || s.airborne() || s.field() || !s.reachable()) {
                return 0.0F;
            }
            return 2.6F + (s.enemiesNear() >= 2 ? 1.0F : 0.0F) + (s.distance() < 6.0 ? 0.4F : 0.0F);
        }

        @Override
        void start(NightbladeCombat c, ServerLevel level, NightbladeCombat.Situation s) {
            this.start = c.boss.position();
            double y = c.groundY(this.start.x, this.start.y, this.start.z);
            this.ground = Double.isNaN(y) ? this.start.y : y;
            this.slamTick = -1;
            this.hitFirst.clear();
            this.hitSecond.clear();
            this.groundState = level.getBlockState(BlockPos.containing(this.start.x, this.ground - 1.0, this.start.z));
            if (this.groundState.isAir()) {
                this.groundState = Blocks.STONE.defaultBlockState();
            }
            c.boss.setNoGravity(true);
            c.sound(NightbladeCombat.dashSound(), 1.2F, 0.6F);
        }

        @Override
        boolean tick(NightbladeCombat c, ServerLevel level, LivingEntity target, int t) {
            look(c, target);
            if (t < 10) {
                dashStep(c, this.start, this.start.add(0.0, 5.0, 0.0), t, 10);
                return false;
            }
            if (t < 15) {
                if (t == 10) {
                    c.sound(NightbladeCombat.modSound("spell_of_jugger"), 1.2F, 0.7F);
                }
                c.boss.setDeltaMovement(Vec3.ZERO);
                return false;
            }
            if (t < 18) {
                Vec3 top = this.start.add(0.0, 5.0, 0.0);
                dashStep(c, top, new Vec3(top.x, this.ground, top.z), t - 15, 3);
                return false;
            }
            if (t == 18) {
                this.slam(c, level);
            }
            this.wave(c, level, t - this.slamTick, this.hitFirst);
            if (c.phase() == 3) {
                this.wave(c, level, t - this.slamTick - 12, this.hitSecond);
            }
            int waveTicks = (int)Math.ceil(WAVE_RANGE / WAVE_SPEED);
            return t - this.slamTick >= waveTicks + (c.phase() == 3 ? 12 : 0) + 8;
        }

        private void slam(NightbladeCombat c, ServerLevel level) {
            this.slamTick = 18;
            c.boss.setNoGravity(false);
            c.boss.setDeltaMovement(Vec3.ZERO);
            c.sound(SoundEvents.GENERIC_EXPLODE.value(), 1.2F, 0.6F);
            c.sound(SoundEvents.ANVIL_LAND, 0.8F, 0.5F);
            c.particles(level, ParticleTypes.EXPLOSION, c.boss.getX(), this.ground + 0.2, c.boss.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            c.shake(level, 24.0, 2.6F, 0.6F);
            for (LivingEntity victim : c.enemiesAround(2.5)) {
                victim.hurtServer(level, c.meleeDamage(), c.scaled(8.0F));
            }
        }

        private void wave(NightbladeCombat c, ServerLevel level, int age, Set<Integer> hit) {
            if (age < 0) {
                return;
            }
            double radius = 1.5 + age * WAVE_SPEED;
            if (radius > WAVE_RANGE) {
                return;
            }
            int points = Mth.clamp((int)(radius * 2.0 * Math.PI / 1.1), 8, 96);
            BlockParticleOption dust = new BlockParticleOption(ParticleTypes.BLOCK, this.groundState);
            for (int i = 0; i < points; i++) {
                double angle = i * Math.PI * 2.0 / points;
                double x = this.start.x + Math.cos(angle) * radius;
                double z = this.start.z + Math.sin(angle) * radius;
                c.particles(level, dust, x, this.ground + 0.15, z, 2, 0.15, 0.1, 0.15, 0.15);
                if (i % 6 == 0) {
                    c.particles(level, TheabyssModParticleTypes.END_SWORD_PT.get(), x, this.ground + 0.3, z, 1, 0.05, 0.1, 0.05, 0.0);
                }
            }
            if (age == 0) {
                c.sound(SoundEvents.WARDEN_SONIC_BOOM, 0.6F, 1.6F);
            }
            AABB band = new AABB(this.start.x - radius - 1.0, this.ground - 1.0, this.start.z - radius - 1.0, this.start.x + radius + 1.0,
                this.ground + 1.5, this.start.z + radius + 1.0);
            for (LivingEntity victim : c.enemiesIn(band)) {
                double distance = Math.hypot(victim.getX() - this.start.x, victim.getZ() - this.start.z);
                boolean onWaveGround = victim.getY() - this.ground < 0.6 && victim.onGround();
                if (Math.abs(distance - radius) < 0.8 && onWaveGround && hit.add(victim.getId())) {
                    victim.hurtServer(level, c.meleeDamage(), c.scaled(9.0F));
                    push(c, victim, horizontal(victim.position().subtract(this.start)), 0.8);
                    victim.setDeltaMovement(victim.getDeltaMovement().add(0.0, 0.4, 0.0));
                }
            }
        }

        @Override
        void stop(NightbladeCombat c) {
            c.boss.setNoGravity(false);
        }
    }

    static final class FangLines extends NightbladeMove {
        private static final double SPACING = 1.25;

        FangLines() {
            super("abyss_rifts", 1, 40);
            this.magic = true;
        }

        @Override
        int cooldown(int phase) {
            return phase == 1 ? 150 : phase == 2 ? 120 : 90;
        }

        @Override
        float score(NightbladeCombat c, NightbladeCombat.Situation s) {
            if (c.stormRaging()) {
                return 0.0F;
            }
            return s.distance() >= 3.0 && s.distance() <= 16.0 && !s.airborne() && !s.field() ? 2.3F : 0.0F;
        }

        @Override
        void start(NightbladeCombat c, ServerLevel level, NightbladeCombat.Situation s) {
            c.animate("rift_stab", false, 22);
            c.sound(NightbladeCombat.modSound("spell_of_misc"), 1.2F, 0.5F);
        }

        @Override
        boolean tick(NightbladeCombat c, ServerLevel level, LivingEntity target, int t) {
            look(c, target);
            if (t == 7) {
                c.shake(level, 20.0, 0.8F, 0.3F);
                c.sound(SoundEvents.AMETHYST_BLOCK_BREAK, 1.6F, 0.5F);
                Vec3 aim = c.predict(target, 16);
                Vec3 origin = c.boss.position();
                double baseAngle = Math.atan2(aim.z - origin.z, aim.x - origin.x);
                double length = Math.min(origin.distanceTo(aim) + 4.0, 18.0);
                double[] spread = c.phase() == 1 ? new double[] {0.0} : c.phase() == 2 ? new double[] {-18.0, 0.0, 18.0} : new double[] {-30.0, -12.0, 0.0, 12.0, 30.0};
                for (double degrees : spread) {
                    double angle = baseAngle + Math.toRadians(degrees);
                    float yaw = (float)Math.toDegrees(Math.atan2(-Math.cos(angle), Math.sin(angle)));
                    for (int i = 0; 1.5 + i * SPACING <= length; i++) {
                        double d = 1.5 + i * SPACING;
                        this.spike(c, level, origin.x + Math.cos(angle) * d, origin.y, origin.z + Math.sin(angle) * d, 9 + i, yaw);
                    }
                }
                if (c.phase() == 3 && c.boss.distanceTo(target) < 5.0) {
                    for (int i = 0; i < 8; i++) {
                        double angle = i * Math.PI / 4.0;
                        float yaw = (float)Math.toDegrees(Math.atan2(-Math.cos(angle + Math.PI / 2.0), Math.sin(angle + Math.PI / 2.0)));
                        this.spike(c, level, origin.x + Math.cos(angle) * 2.0, origin.y, origin.z + Math.sin(angle) * 2.0, 12, yaw);
                    }
                }
            }
            return t >= 30;
        }

        private void spike(NightbladeCombat c, ServerLevel level, double x, double fromY, double z, int warmup, float yaw) {
            double y = c.groundY(x, fromY, z);
            if (Double.isNaN(y)) {
                return;
            }
            var spike = TheabyssModEntities.NIGHTBLADE_SPIKE.get().create(level, EntitySpawnReason.TRIGGERED);
            if (spike != null) {
                spike.setPos(x, y, z);
                spike.setup(c.boss, warmup, c.scaled(6.0F), c.phase() == 3, yaw);
                level.addFreshEntity(spike);
            }
        }
    }

    static final class StormMarks extends NightbladeMove {
        private static final int CAST = 16;

        StormMarks() {
            super("storm_marks", 1, 80);
            this.magic = true;
        }

        @Override
        int cooldown(int phase) {
            return phase == 1 ? 300 : phase == 2 ? 325 : 350;
        }

        @Override
        float score(NightbladeCombat c, NightbladeCombat.Situation s) {
            if (s.distance() > 24.0 || s.field() || c.storming()) {
                return 0.0F;
            }
            return 2.4F + (s.standingStill() ? 1.2F : 0.0F) + (s.reachable() ? 0.0F : 0.8F);
        }

        @Override
        void start(NightbladeCombat c, ServerLevel level, NightbladeCombat.Situation s) {
            c.sound(NightbladeCombat.modSound("spell_of_elec"), 1.4F, 0.8F);
            swing(c, InteractionHand.MAIN_HAND);
        }

        @Override
        boolean tick(NightbladeCombat c, ServerLevel level, LivingEntity target, int t) {
            look(c, target);
            if (t % 3 == 0) {
                c.particles(level, ParticleTypes.ELECTRIC_SPARK, c.boss.getX(), c.boss.getY() + 2.4, c.boss.getZ(), 3, 0.3, 0.3, 0.3, 0.05);
            }
            if (t == 6) {
                swing(c, InteractionHand.OFF_HAND);
                c.startStorm(NightbladeStorm.Kind.HUNT, target.position());
            }
            return t >= CAST;
        }
    }

    static final class StormField extends NightbladeMove {
        private static final int CAST = 22;

        StormField() {
            super("storm_field", 1, 120);
            this.magic = true;
        }

        @Override
        int cooldown(int phase) {
            return phase == 1 ? 400 : phase == 2 ? 375 : 350;
        }

        @Override
        float score(NightbladeCombat c, NightbladeCombat.Situation s) {
            if (s.distance() > 20.0 || s.field() || c.storming()) {
                return 0.0F;
            }
            return 2.2F + (s.standingStill() ? 0.0F : 0.9F) + (s.enemiesNear() > 1 ? 0.8F : 0.0F);
        }

        @Override
        void start(NightbladeCombat c, ServerLevel level, NightbladeCombat.Situation s) {
            c.sound(NightbladeCombat.modSound("spell_of_elec"), 1.6F, 0.6F);
            swing(c, InteractionHand.MAIN_HAND);
        }

        @Override
        boolean tick(NightbladeCombat c, ServerLevel level, LivingEntity target, int t) {
            look(c, target);
            if (t % 2 == 0) {
                c.particles(level, ParticleTypes.ELECTRIC_SPARK, c.boss.getX(), c.boss.getY() + 2.6, c.boss.getZ(), 4, 0.2, 0.6, 0.2, 0.08);
            }
            if (t == 10) {
                swing(c, InteractionHand.OFF_HAND);
                c.sound(NightbladeCombat.modSound("spell_of_elec"), 1.2F, 1.1F);
                c.shake(level, 24.0, 0.6F, 0.4F);
                c.startStorm(NightbladeStorm.Kind.MINEFIELD, target.position());
            }
            return t >= CAST;
        }
    }

    static final class CounterStance extends NightbladeMove {
        private static final int GUARD_TICKS = 60;
        private static final float COUNTER_DAMAGE = 15.0F;
        private static final float HEAL_ON_BLOCK = 0.1F;
        private boolean guarding;
        private boolean broken;
        private LivingEntity countered;
        private int counterStart;
        private int endAt;

        CounterStance() {
            super("counter_stance", 2, 200);
        }

        @Override
        int cooldown(int phase) {
            return phase == 2 ? 420 : 320;
        }

        @Override
        float score(NightbladeCombat c, NightbladeCombat.Situation s) {
            if (s.distance() > 7.0 || !s.reachable() || c.bladeOut()) {
                return 0.0F;
            }
            return 2.4F + (s.distance() < 4.0 ? 0.8F : 0.0F);
        }

        @Override
        void start(NightbladeCombat c, ServerLevel level, NightbladeCombat.Situation s) {
            this.guarding = true;
            this.broken = false;
            this.countered = null;
            this.counterStart = -1;
            this.endAt = -1;
            c.boss.setDeltaMovement(Vec3.ZERO);
            c.animate("counter_stance", true, 0);
            c.sound(SoundEvents.ARMOR_EQUIP_NETHERITE.value(), 1.2F, 0.6F);
            c.sound(NightbladeCombat.modSound("spell_of_telekinetic"), 0.8F, 1.4F);
        }

        boolean onHit(NightbladeCombat c, ServerLevel level, net.minecraft.world.damagesource.DamageSource source) {
            if (!this.guarding || !(source.getEntity() instanceof LivingEntity attacker) || !c.isEnemy(attacker)) {
                return false;
            }
            boolean melee = source.getDirectEntity() == attacker && attacker.distanceTo(c.boss) < 6.0F;
            if (!melee) {
                return false;
            }
            Vec3 facing = Vec3.directionFromRotation(0.0F, c.boss.getYRot());
            Vec3 toAttacker = horizontal(attacker.position().subtract(c.boss.position()));
            if (facing.dot(toAttacker) < -0.25) {
                this.guarding = false;
                this.broken = true;
                return false;
            }
            this.guarding = false;
            this.countered = attacker;
            Vec3 at = c.boss.getEyePosition().add(facing.scale(0.6));
            c.particles(level, ParticleTypes.ELECTRIC_SPARK, at.x, at.y - 0.4, at.z, 24, 0.2, 0.2, 0.2, 0.3);
            level.playSound(null, at.x, at.y, at.z, SoundEvents.ANVIL_LAND, net.minecraft.sounds.SoundSource.HOSTILE, 0.7F, 1.8F);
            c.boss.heal(c.boss.getMaxHealth() * HEAL_ON_BLOCK);
            c.particles(level, TheabyssModParticleTypes.END_SWORD_PT.get(), c.boss.getX(), c.boss.getY() + 1.2, c.boss.getZ(), 30, 0.4, 0.9, 0.4, 0.08);
            c.particles(level, ParticleTypes.SOUL, c.boss.getX(), c.boss.getY() + 1.0, c.boss.getZ(), 10, 0.4, 0.8, 0.4, 0.04);
            level.playSound(null, c.boss.getX(), c.boss.getY() + 1.0, c.boss.getZ(), SoundEvents.BEACON_POWER_SELECT, net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 0.6F);
            return true;
        }

        @Override
        boolean tick(NightbladeCombat c, ServerLevel level, LivingEntity target, int t) {
            if (this.broken) {
                if (this.endAt < 0) {
                    this.endAt = t + 36;
                    c.animate("stagger", false, 36);
                    c.sound(SoundEvents.SHIELD_BREAK.value(), 1.0F, 0.7F);
                }
                return t >= this.endAt;
            }
            if (this.countered != null) {
                if (this.counterStart < 0) {
                    this.counterStart = t;
                    c.animate("counter_strike", false, 14);
                    c.sound(NightbladeCombat.dashSound(), 1.4F, 0.7F);
                }
                int step = t - this.counterStart;
                look(c, this.countered);
                if (step == 2 && this.countered.isAlive() && this.countered.distanceTo(c.boss) < 6.0F) {
                    this.countered.hurtServer(level, c.meleeDamage(), c.scaled(COUNTER_DAMAGE));
                    push(c, this.countered, horizontal(this.countered.position().subtract(c.boss.position())), 1.3);
                    c.impactFrame(this.countered);
                    c.shake(level, 16.0, 1.4F, 0.35F);
                    c.sound(NightbladeCombat.nightbladeSound(), 1.5F, 0.7F);
                    c.particles(level, ParticleTypes.SWEEP_ATTACK, this.countered.getX(), this.countered.getY() + 1.0, this.countered.getZ(), 2, 0.4, 0.0, 0.4, 0.0);
                }
                return step >= 16;
            }
            look(c, target);
            c.boss.setDeltaMovement(c.boss.getDeltaMovement().multiply(0.0, 1.0, 0.0));
            if (t % 4 == 0) {
                Vec3 eye = c.boss.getEyePosition();
                c.particles(level, TheabyssModParticleTypes.END_SWORD_PT.get(), eye.x, eye.y - 0.5, eye.z, 2, 0.35, 0.3, 0.35, 0.0);
            }
            return t >= GUARD_TICKS;
        }

        @Override
        void stop(NightbladeCombat c) {
            this.guarding = false;
        }
    }

    static final class BladeBoomerang extends NightbladeMove {
        private static final double BACKSTEP = 6.0;
        private static final int WINDUP = 3;
        private static final int RELEASE = WINDUP + 7;

        BladeBoomerang() {
            super("blade_boomerang", 1, 120);
        }

        @Override
        int cooldown(int phase) {
            return 300;
        }

        @Override
        float score(NightbladeCombat c, NightbladeCombat.Situation s) {
            if (s.distance() < 4.0 || s.distance() > 24.0 || c.bladeOut() || c.stormRaging()) {
                return 0.0F;
            }
            boolean flying = s.airborne() || !s.reachable() || s.field();
            if (s.phase() < 3 && !flying) {
                return 0.0F;
            }
            return 2.6F + (flying ? 2.2F : 0.0F) + (s.distance() > 12.0 ? 0.8F : 0.0F);
        }

        @Override
        void start(NightbladeCombat c, ServerLevel level, NightbladeCombat.Situation s) {
            c.boss.getNavigation().stop();
            this.backstep(c, level, s.target());
        }

        private void backstep(NightbladeCombat c, ServerLevel level, LivingEntity target) {
            Vec3 from = c.boss.position();
            Vec3 away = horizontal(from.subtract(target.position()));
            if (away.lengthSqr() < 1.0E-4) {
                away = Vec3.directionFromRotation(0.0F, c.boss.getYRot() + 180.0F);
            }
            for (double distance = BACKSTEP; distance >= 2.0; distance -= 1.5) {
                Vec3 wanted = from.add(away.scale(distance));
                double y = c.groundY(wanted.x, from.y, wanted.z);
                Vec3 spot = c.freeSpotNear(new Vec3(wanted.x, Double.isNaN(y) ? from.y : y, wanted.z), 2.0);
                if (spot != null) {
                    c.particles(level, ParticleTypes.LARGE_SMOKE, from.x, from.y + 1.0, from.z, 12, 0.3, 0.6, 0.3, 0.02);
                    c.boss.teleportTo(spot.x, spot.y, spot.z);
                    c.boss.setDeltaMovement(Vec3.ZERO);
                    c.particles(level, ParticleTypes.LARGE_SMOKE, spot.x, spot.y + 1.0, spot.z, 12, 0.3, 0.6, 0.3, 0.02);
                    c.particles(level, TheabyssModParticleTypes.END_SWORD_PT.get(), spot.x, spot.y + 1.0, spot.z, 8, 0.3, 0.6, 0.3, 0.02);
                    c.sound(NightbladeCombat.dashSound(), 1.2F, 0.9F);
                    return;
                }
            }
        }

        @Override
        boolean tick(NightbladeCombat c, ServerLevel level, LivingEntity target, int t) {
            look(c, target);
            if (t == WINDUP) {
                c.animate("blade_throw", false, 16);
            }
            if (t == RELEASE) {
                var blade = TheabyssModEntities.NIGHTBLADE_BLADE.get().create(level, EntitySpawnReason.TRIGGERED);
                if (blade != null) {
                    Vec3 facing = Vec3.directionFromRotation(0.0F, c.boss.getYRot());
                    Vec3 left = new Vec3(facing.z, 0.0, -facing.x);
                    Vec3 hand = c.boss.position().add(0.0, 1.3, 0.0).add(left.scale(0.5)).add(facing.scale(0.4));
                    blade.launch(c.boss, hand, target, c.scaled(10.0F), c.phase() == 3);
                    level.addFreshEntity(blade);
                    c.boss.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, net.minecraft.world.item.ItemStack.EMPTY);
                    c.sound(SoundEvents.TRIDENT_THROW.value(), 1.4F, 0.7F);
                    c.sound(NightbladeCombat.dashSound(), 1.0F, 1.4F);
                }
            }
            return t >= RELEASE + 9;
        }
    }

    static final class ShadowStep extends NightbladeMove {
        ShadowStep() {
            super("shadow_step", 1, 60);
        }

        @Override
        int cooldown(int phase) {
            return phase == 1 ? 120 : phase == 2 ? 90 : 70;
        }

        @Override
        float score(NightbladeCombat c, NightbladeCombat.Situation s) {
            if (s.airborne() || s.field()) {
                return 0.0F;
            }
            if (!s.reachable()) {
                return 3.0F;
            }
            return s.distance() >= 9.0 && !c.magicMood() ? 1.0F + (float)(s.distance() / 10.0) : 0.0F;
        }

        @Override
        void start(NightbladeCombat c, ServerLevel level, NightbladeCombat.Situation s) {
            LivingEntity target = s.target();
            c.particles(level, ParticleTypes.LARGE_SMOKE, c.boss.getX(), c.boss.getY() + 1.0, c.boss.getZ(), 14, 0.3, 0.7, 0.3, 0.02);
            Vec3 back = horizontal(target.getLookAngle()).scale(-2.2);
            Vec3 side = new Vec3(-back.z, 0.0, back.x).normalize().scale(2.2);
            for (Vec3 offset : new Vec3[] {back, side, side.scale(-1.0), back.scale(-1.0)}) {
                Vec3 to = target.position().add(offset);
                if (level.noCollision(c.boss, c.boss.getBoundingBox().move(to.subtract(c.boss.position())))) {
                    c.boss.teleportTo(to.x, to.y, to.z);
                    break;
                }
            }
            c.sound(NightbladeCombat.dashSound(), 1.2F, 0.9F);
            look(c, target);
        }

        @Override
        boolean tick(NightbladeCombat c, ServerLevel level, LivingEntity target, int t) {
            look(c, target);
            if (t == 3) {
                swing(c, InteractionHand.MAIN_HAND);
                if (c.boss.distanceTo(target) < 3.5) {
                    c.boss.doHurtTarget(level, target);
                    c.sound(NightbladeCombat.nightbladeSound(), 1.0F, 1.1F);
                }
            }
            return t >= 8;
        }
    }

    static final class CrescentSweep extends NightbladeMove {
        CrescentSweep() {
            super("crescent_sweep", 1, 40);
        }

        @Override
        int cooldown(int phase) {
            return phase == 1 ? 120 : phase == 2 ? 95 : 75;
        }

        @Override
        float score(NightbladeCombat c, NightbladeCombat.Situation s) {
            return s.distance() <= 5.0 && !s.field() ? 2.0F + (s.enemiesNear() >= 2 ? 1.0F : 0.0F) : 0.0F;
        }

        @Override
        void start(NightbladeCombat c, ServerLevel level, NightbladeCombat.Situation s) {
            c.sound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.5F);
        }

        @Override
        boolean tick(NightbladeCombat c, ServerLevel level, LivingEntity target, int t) {
            if (t < 12) {
                look(c, target);
                if (t % 3 == 0) {
                    Vec3 hand = c.boss.position().add(horizontal(c.boss.getLookAngle()).scale(0.6)).add(0.0, 1.2, 0.0);
                    c.particles(level, TheabyssModParticleTypes.END_SWORD_PT.get(), hand.x, hand.y, hand.z, 2, 0.1, 0.1, 0.1, 0.0);
                }
                return false;
            }
            if (t == 12) {
                Vec3 forward = horizontal(c.boss.getLookAngle());
                swing(c, InteractionHand.MAIN_HAND);
                c.sound(NightbladeCombat.nightbladeSound(), 1.3F, 0.7F);
                for (int i = -3; i <= 3; i++) {
                    double angle = Math.atan2(forward.z, forward.x) + Math.toRadians(i * 20.0);
                    c.particles(level, ParticleTypes.SWEEP_ATTACK, c.boss.getX() + Math.cos(angle) * 2.5, c.boss.getY() + 1.0,
                        c.boss.getZ() + Math.sin(angle) * 2.5, 1, 0.0, 0.0, 0.0, 0.0);
                }
                for (LivingEntity victim : c.enemiesAround(6.5)) {
                    Vec3 to = horizontal(victim.position().subtract(c.boss.position()));
                    if (to.dot(forward) > Math.cos(Math.toRadians(65.0)) && victim.distanceTo(c.boss) <= 6.5) {
                        victim.hurtServer(level, c.meleeDamage(), c.scaled(11.0F));
                        push(c, victim, to, 1.0);
                    }
                }
            }
            if (c.phase() == 3 && t == 24) {
                swing(c, InteractionHand.OFF_HAND);
                c.sound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, 0.6F);
                for (int i = 0; i < 10; i++) {
                    double angle = i * Math.PI / 5.0;
                    c.particles(level, ParticleTypes.SWEEP_ATTACK, c.boss.getX() + Math.cos(angle) * 2.2, c.boss.getY() + 1.0,
                        c.boss.getZ() + Math.sin(angle) * 2.2, 1, 0.0, 0.0, 0.0, 0.0);
                }
                for (LivingEntity victim : c.enemiesAround(5.0)) {
                    victim.hurtServer(level, c.meleeDamage(), c.scaled(8.0F));
                    push(c, victim, horizontal(victim.position().subtract(c.boss.position())), 1.2);
                }
            }
            return t >= (c.phase() == 3 ? 32 : 20);
        }
    }

    static final class SummonSeekers extends NightbladeMove {
        SummonSeekers() {
            super("summon_seekers", 1, 200);
        }

        @Override
        int cooldown(int phase) {
            return phase == 1 ? 600 : phase == 2 ? 500 : 400;
        }

        @Override
        float score(NightbladeCombat c, NightbladeCombat.Situation s) {
            if (c.stormRaging()) {
                return 0.0F;
            }
            int alive = c.boss.level().getEntitiesOfClass(SeekerEntity.class, c.boss.getBoundingBox().inflate(40.0)).size();
            if (alive >= 4 || s.field()) {
                return 0.0F;
            }
            return !s.reachable() ? 2.5F : s.distance() > 18.0 ? 1.2F : 0.0F;
        }

        @Override
        void start(NightbladeCombat c, ServerLevel level, NightbladeCombat.Situation s) {
            int count = s.phase() == 3 ? 3 : 2;
            for (int i = 0; i < count; i++) {
                double angle = i * Math.PI * 2.0 / count;
                BlockPos pos = BlockPos.containing(c.boss.getX() + Math.cos(angle) * 2.0, c.boss.getY() + 2.0, c.boss.getZ() + Math.sin(angle) * 2.0);
                Entity seeker = TheabyssModEntities.SEEKER.get().spawn(level, pos, EntitySpawnReason.MOB_SUMMONED);
                if (seeker instanceof Mob mob) {
                    mob.setTarget(s.target());
                }
                c.particles(level, ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.02);
            }
            c.sound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.4F, 0.7F);
            swing(c, InteractionHand.MAIN_HAND);
        }

        @Override
        boolean tick(NightbladeCombat c, ServerLevel level, LivingEntity target, int t) {
            look(c, target);
            return t >= 10;
        }
    }
}
