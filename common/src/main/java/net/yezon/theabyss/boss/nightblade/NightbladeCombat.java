package net.yezon.theabyss.boss.nightblade;

import java.util.ArrayDeque;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.entity.NightBladeBossCloneEntity;
import net.yezon.theabyss.entity.NightbladeBossEntity;
import net.yezon.theabyss.entity.SeekerEntity;
import net.yezon.theabyss.events.NightBladeIceWalkEvents;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.network.NightbladeImpactMessage;

public final class NightbladeCombat {
    public record Situation(LivingEntity target, double distance, double heightAbove, boolean airborne, boolean reachable, boolean standingStill,
        int enemiesNear, int phase, boolean field) {
    }

    private static final ResourceLocation PHASE_SPEED = ResourceLocation.fromNamespaceAndPath("theabyss", "nightblade_enraged");
    public static final int MIRROR_LIFETIME = 200;

    final NightbladeBossEntity boss;
    private List<NightbladeMove> moves = NightbladeMoves.all();
    private NightbladeMove active;
    private NightbladeMove last;
    private int moveTick;
    private int pause = 80;
    private int phase = 1;
    private boolean mirrorsAt66;
    private boolean mirrorsAt33;
    private boolean forceMirrors;
    private int dodgeCooldown;
    private int riposteIn = -1;
    private int unreachableTicks;
    private int fieldTicks;
    private NightbladeStorm storm;
    private NightbladeArena arena;
    private int animLockUntil;
    private boolean animHeld;
    private int lastSlash = -100;
    private int vulnerableTicks;
    private int stormRest;
    private int bladeMissing;
    private boolean magicNext;
    private boolean magicMood;
    private int followUpPause = -1;
    private final ArrayDeque<Vec3> targetTrail = new ArrayDeque<>();

    private boolean mirror;
    private int mirrorLife;
    private float damageFactor = 1.0F;

    public NightbladeCombat(NightbladeBossEntity boss) {
        this.boss = boss;
    }

    public void becomeMirror(LivingEntity target, int phase, int delay) {
        this.mirror = true;
        this.mirrorLife = MIRROR_LIFETIME;
        this.damageFactor = 0.4F;
        this.phase = phase;
        this.boss.setPhase(phase);
        this.moves = NightbladeMoves.mirrorMoves();
        this.pause = delay;
        this.boss.setTarget(target);
    }

    public boolean isMirror() {
        return this.mirror;
    }

    public boolean busy() {
        return this.active != null;
    }

    public int phase() {
        return this.phase;
    }

    public void tick(ServerLevel level) {
        for (NightbladeMove move : this.moves) {
            move.cooldownLeft = Math.max(0, move.cooldownLeft - 1);
        }
        this.dodgeCooldown = Math.max(0, this.dodgeCooldown - 1);
        this.fieldTicks = Math.max(0, this.fieldTicks - 1);
        this.stormRest = Math.max(0, this.stormRest - 1);
        this.bladeMissing = this.bladeOut() ? this.bladeMissing + 1 : 0;
        if (this.bladeMissing > 160) {
            this.boss.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new net.minecraft.world.item.ItemStack(net.yezon.theabyss.init.TheabyssModItems.ABYSS_SWORD.get()));
        }
        if (this.vulnerableTicks > 0) {
            this.vulnerableTicks--;
            if (this.vulnerableTicks % 3 == 0) {
                this.particles(level, ParticleTypes.ELECTRIC_SPARK, this.boss.getX(), this.boss.getY() + 1.0, this.boss.getZ(), 3, 0.35, 0.7, 0.35, 0.02);
                this.particles(level, ParticleTypes.SOUL_FIRE_FLAME, this.boss.getX(), this.boss.getY() + 1.0, this.boss.getZ(), 1, 0.3, 0.6, 0.3, 0.01);
            }
        }
        NightBladeIceWalkEvents.execute(level, this.boss.getX(), this.boss.getY(), this.boss.getZ());
        this.aura(level);
        if (this.mirror) {
            if (--this.mirrorLife <= 0) {
                this.vanish(level);
                return;
            }
        } else {
            this.updatePhase(level);
        }

        LivingEntity target = this.target();
        this.recordTarget(target);
        if (this.arena != null && this.arena.tick(this, level)) {
            this.arena = null;
        }
        if (this.storm != null && (target == null || this.storm.tick(this, level, target))) {
            this.storm = null;
            this.stormRest = 125;
        }
        if (target == null) {
            if (this.active != null) {
                this.finishMove();
            }
            return;
        }
        if (this.riposteIn >= 0 && this.riposteIn-- == 0) {
            this.riposte(level, target);
        }
        if (this.active != null) {
            this.boss.getNavigation().stop();
            if (this.active.tick(this, level, target, this.moveTick++)) {
                this.finishMove();
            }
            return;
        }
        Situation situation = this.situation(target);
        this.unreachableTicks = situation.reachable() ? 0 : this.unreachableTicks + 1;
        if (--this.pause <= 0) {
            this.decide(level, situation);
        }
    }

    private void decide(ServerLevel level, Situation situation) {
        NightbladeMove best = null;
        float bestScore = 0.0F;
        for (NightbladeMove move : this.moves) {
            if (this.forceMirrors && move instanceof NightbladeMoves.MirrorImages) {
                best = move;
                break;
            }
            if (move.cooldownLeft > 0 || move.minPhase > situation.phase()) {
                continue;
            }
            float score = move.score(this, situation);
            if (score <= 0.0F) {
                continue;
            }
            if (move == this.last) {
                score *= 0.35F;
            }
            if (move.magic && (this.magicNext || this.magicMood)) {
                score *= this.magicNext ? 1.8F : 1.3F;
            }
            score *= 0.85F + this.boss.getRandom().nextFloat() * 0.3F;
            if (score > bestScore) {
                bestScore = score;
                best = move;
            }
        }
        if (best == null) {
            this.pause = 10;
            return;
        }
        this.forceMirrors = false;
        this.magicNext = false;
        this.active = best;
        this.moveTick = 0;
        this.boss.getNavigation().stop();
        best.start(this, level, situation);
    }

    private void finishMove() {
        NightbladeMove finished = this.active;
        this.active = null;
        this.releaseAnimation();
        finished.stop(this);
        finished.cooldownLeft = this.mirror ? 20 : finished.cooldown(this.phase);
        this.last = finished;
        this.boss.setDashing(false);
        this.boss.setNoGravity(false);
        this.unstick();
        if (this.followUpPause >= 0) {
            this.pause = this.followUpPause;
            this.followUpPause = -1;
        } else if (this.mirror) {
            this.pause = 8 + this.boss.getRandom().nextInt(12);
        } else {
            this.pause = switch (this.phase) {
                case 1 -> 35 + this.boss.getRandom().nextInt(16);
                case 2 -> 24 + this.boss.getRandom().nextInt(13);
                default -> this.boss.getRandom().nextFloat() < 0.3F ? 4 : 14 + this.boss.getRandom().nextInt(11);
            };
        }
        float moodChance = this.phase == 1 ? 0.45F : this.phase == 2 ? 0.55F : 0.65F;
        this.magicMood = !this.mirror && this.boss.getRandom().nextFloat() < moodChance;
    }

    public void abort() {
        this.storm = null;
        if (this.arena != null && this.boss.level() instanceof ServerLevel level) {
            this.arena.cancel(level);
            this.arena = null;
        }
        this.releaseAnimation();
        if (this.active != null) {
            this.active.stop(this);
            this.active = null;
        }
    }

    void followUp(int pauseTicks, boolean magic) {
        this.followUpPause = pauseTicks;
        this.magicNext |= magic;
    }

    void startStorm(NightbladeStorm.Kind kind, Vec3 field) {
        this.storm = new NightbladeStorm(kind, this.phase, field);
    }

    boolean stormRaging() {
        return this.storm != null;
    }

    boolean storming() {
        return this.storm != null || this.stormRest > 0;
    }

    boolean startArena(ServerLevel level, LivingEntity target) {
        this.arena = NightbladeArena.create(this, level, target);
        return this.arena != null;
    }

    boolean arenaActive() {
        return this.arena != null;
    }

    boolean bladeOut() {
        return this.boss.getOffhandItem().isEmpty();
    }

    public boolean interceptHit(ServerLevel level, DamageSource source) {
        return this.active instanceof NightbladeMoves.CounterStance stance && stance.onHit(this, level, source);
    }

    void startField(int ticks) {
        this.fieldTicks = ticks;
    }

    boolean magicMood() {
        return this.magicMood;
    }

    boolean magicReady() {
        for (NightbladeMove move : this.moves) {
            if (move.magic && move.cooldownLeft < 20 && move.minPhase <= this.phase) {
                return true;
            }
        }
        return false;
    }

    public boolean wantsDistance() {
        LivingEntity target = this.target();
        return this.active == null && this.magicMood && !this.mirror && target != null && this.pause > 10 && this.boss.distanceTo(target) < 7.0F
            && this.fieldTicks == 0;
    }

    Vec3 freeSpotNear(Vec3 wanted, double maxUp) {
        for (double up = 0.0; up <= maxUp; up += 0.5) {
            Vec3 at = wanted.add(0.0, up, 0.0);
            if (this.boss.level().noCollision(this.boss, this.boss.getBoundingBox().move(at.subtract(this.boss.position())))) {
                return at;
            }
        }
        return null;
    }

    private void unstick() {
        if (this.boss.level().noCollision(this.boss, this.boss.getBoundingBox())) {
            return;
        }
        Vec3 free = this.freeSpotNear(this.boss.position(), 8.0);
        if (free != null) {
            this.boss.teleportTo(free.x, free.y, free.z);
        }
    }

    LivingEntity target() {
        LivingEntity target = this.boss.getTarget();
        return target != null && target.isAlive() && !(target instanceof Player player && (player.isCreative() || player.isSpectator())) ? target : null;
    }

    private Situation situation(LivingEntity target) {
        double distance = this.boss.distanceTo(target);
        double heightAbove = target.getY() - this.boss.getY();
        boolean airborne = !target.onGround() && this.groundDistance(target) > 2.0;
        boolean reachable = heightAbove < 3.5 && !this.boss.getNavigation().isStuck();
        boolean still = this.targetVelocity().horizontalDistanceSqr() < 0.0025;
        int enemies = this.boss.level().getEntitiesOfClass(Player.class, this.boss.getBoundingBox().inflate(8.0), p -> !p.isSpectator() && !p.isCreative()).size();
        return new Situation(target, distance, heightAbove, airborne, reachable && this.unreachableTicks < 60, still, enemies, this.phase, this.fieldTicks > 0);
    }

    private double groundDistance(Entity entity) {
        BlockPos.MutableBlockPos pos = entity.blockPosition().mutable();
        for (int i = 0; i < 8; i++) {
            if (!entity.level().getBlockState(pos.move(0, -1, 0)).isAir()) {
                return entity.getY() - (pos.getY() + 1.0);
            }
        }
        return 8.0;
    }

    private void recordTarget(LivingEntity target) {
        if (target == null) {
            this.targetTrail.clear();
            return;
        }
        this.targetTrail.addLast(target.position());
        while (this.targetTrail.size() > 6) {
            this.targetTrail.removeFirst();
        }
    }

    Vec3 targetVelocity() {
        if (this.targetTrail.size() < 2) {
            return Vec3.ZERO;
        }
        return this.targetTrail.getLast().subtract(this.targetTrail.getFirst()).scale(1.0 / (this.targetTrail.size() - 1));
    }

    Vec3 predict(LivingEntity target, int ticks) {
        Vec3 velocity = this.targetVelocity();
        Vec3 lead = new Vec3(velocity.x, 0.0, velocity.z).scale(ticks);
        if (lead.length() > 8.0) {
            lead = lead.normalize().scale(8.0);
        }
        return target.position().add(lead);
    }

    private void updatePhase(ServerLevel level) {
        float health = this.boss.getHealth() / this.boss.getMaxHealth();
        int now = health > 0.66F ? 1 : health > 0.33F ? 2 : 3;
        if (now > this.phase) {
            this.phase = now;
            this.boss.setPhase(now);
            this.roar(level);
            if (now == 3) {
                var speed = this.boss.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speed != null && !speed.hasModifier(PHASE_SPEED)) {
                    speed.addPermanentModifier(new AttributeModifier(PHASE_SPEED, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
            }
        }
        if (health <= 0.66F && !this.mirrorsAt66) {
            this.mirrorsAt66 = true;
            this.forceMirrors = true;
            this.pause = Math.min(this.pause, 20);
        }
        if (health <= 0.33F && !this.mirrorsAt33) {
            this.mirrorsAt33 = true;
            this.forceMirrors = true;
            this.pause = Math.min(this.pause, 20);
        }
    }

    private void roar(ServerLevel level) {
        this.sound(SoundEvents.RAVAGER_ROAR, 2.0F, 0.6F);
        this.particles(level, ParticleTypes.SONIC_BOOM, this.boss.getX(), this.boss.getY() + 1.0, this.boss.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        for (LivingEntity near : this.enemiesAround(6.0)) {
            Vec3 away = near.position().subtract(this.boss.position()).normalize();
            near.knockback(1.6, -away.x, -away.z);
            near.hasImpulse = true;
        }
    }

    private void vanish(ServerLevel level) {
        this.abort();
        this.particles(level, ParticleTypes.LARGE_SMOKE, this.boss.getX(), this.boss.getY() + 1.0, this.boss.getZ(), 20, 0.3, 0.8, 0.3, 0.02);
        this.sound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0F, 1.4F);
        this.boss.discard();
    }

    public boolean tryDodge(ServerLevel level, DamageSource source) {
        if (this.mirror || this.vulnerableTicks > 0 || this.active != null || this.dodgeCooldown > 0 || !(source.getEntity() instanceof LivingEntity attacker)
            || attacker.distanceTo(this.boss) > 5.0) {
            return false;
        }
        float chance = this.phase == 1 ? 0.15F : this.phase == 2 ? 0.25F : 0.35F;
        if (this.boss.getRandom().nextFloat() >= chance || !sidestep(this.boss, attacker)) {
            return false;
        }
        this.dodgeCooldown = 100;
        if (this.phase >= 2) {
            this.riposteIn = 5;
        }
        return true;
    }

    public static boolean sidestep(Mob mob, Entity attacker) {
        Vec3 toAttacker = attacker.position().subtract(mob.position());
        Vec3 side = new Vec3(-toAttacker.z, 0.0, toAttacker.x).normalize().scale(3.0);
        if (mob.getRandom().nextBoolean()) {
            side = side.scale(-1.0);
        }
        for (Vec3 offset : new Vec3[] {side, side.scale(-1.0)}) {
            Vec3 to = mob.position().add(offset);
            if (mob.level().noCollision(mob, mob.getBoundingBox().move(offset))) {
                if (mob.level() instanceof ServerLevel level) {
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, mob.getX(), mob.getY() + 1.0, mob.getZ(), 8, 0.2, 0.5, 0.2, 0.01);
                }
                mob.teleportTo(to.x, to.y, to.z);
                mob.lookAt(EntityAnchorArgument.Anchor.EYES, attacker.getEyePosition());
                mob.playSound(dashSound(), 0.8F, 1.3F);
                return true;
            }
        }
        return false;
    }

    private void riposte(ServerLevel level, LivingEntity target) {
        if (this.boss.distanceTo(target) < 4.0) {
            this.boss.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
            this.boss.swing(InteractionHand.OFF_HAND, true);
            this.boss.doHurtTarget(target);
            this.sound(nightbladeSound(), 1.0F, 1.2F);
        }
    }

    private void aura(ServerLevel level) {
        if (this.boss.tickCount % 4 == 0) {
            this.particles(level, TheabyssModParticleTypes.END_SWORD_PT.get(), this.boss.getX(), this.boss.getY() + 1.0, this.boss.getZ(), 1, 0.4, 0.6, 0.4, 0.0);
        }
    }

    List<LivingEntity> enemiesAround(double radius) {
        return this.enemiesIn(this.boss.getBoundingBox().inflate(radius));
    }

    List<LivingEntity> enemiesIn(AABB box) {
        return this.boss.level().getEntitiesOfClass(LivingEntity.class, box, this::isEnemy);
    }

    boolean isEnemy(LivingEntity entity) {
        return entity != this.boss && entity.isAlive() && !(entity instanceof NightbladeBossEntity) && !(entity instanceof NightBladeBossCloneEntity)
            && !(entity instanceof SeekerEntity) && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    DamageSource meleeDamage() {
        return this.boss.damageSources().mobAttack(this.boss);
    }

    DamageSource magicDamage() {
        return this.boss.damageSources().indirectMagic(this.boss, this.boss);
    }

    float scaled(float base) {
        return base * (1.0F + 0.15F * (this.phase - 1)) * this.damageFactor;
    }

    void sound(SoundEvent sound, float volume, float pitch) {
        if (sound != null) {
            this.boss.level().playSound(null, this.boss.getX(), this.boss.getY(), this.boss.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
        }
    }

    void particles(ServerLevel level, ParticleOptions type, double x, double y, double z, int count, double dx, double dy, double dz, double speed) {
        level.sendParticles(type, x, y, z, count, dx, dy, dz, speed);
    }

    double groundY(double x, double y, double z) {
        BlockPos.MutableBlockPos pos = BlockPos.containing(x, y + 3.0, z).mutable();
        for (int i = 0; i < 10; i++) {
            BlockPos below = pos.below();
            if (this.boss.level().getBlockState(pos).getCollisionShape(this.boss.level(), pos).isEmpty()
                && !this.boss.level().getBlockState(below).getCollisionShape(this.boss.level(), below).isEmpty()) {
                return pos.getY();
            }
            pos.move(0, -1, 0);
        }
        return Double.NaN;
    }

    void shake(ServerLevel level, double radius, float degrees, float seconds) {
        net.yezon.theabyss.network.NightbladeShakeMessage.around(level, this.boss.position(), radius, degrees, seconds);
    }

    void animate(String name, boolean hold, int lockTicks) {
        this.boss.triggerAnim("action", name);
        this.animHeld = hold;
        this.animLockUntil = hold ? Integer.MAX_VALUE : this.boss.tickCount + lockTicks;
    }

    public void slash(InteractionHand hand) {
        int now = this.boss.tickCount;
        if (now < this.animLockUntil || now - this.lastSlash < 6) {
            return;
        }
        this.lastSlash = now;
        this.boss.triggerAnim("action", hand == InteractionHand.OFF_HAND ? "slash_left" : "slash_right");
    }

    private void releaseAnimation() {
        if (this.animHeld) {
            this.boss.stopTriggeredAnim("action", null);
        }
        this.animHeld = false;
        this.animLockUntil = 0;
    }

    void parryCue(LivingEntity target, int impactIn) {
        if (target instanceof ServerPlayer player) {
            net.yezon.theabyss.network.NightbladeParryCueMessage.send(player, this.boss.getId(), Math.max(1, impactIn), NightbladeParry.WINDOW);
        }
    }

    void impactFrame(LivingEntity victim) {
        if (victim instanceof ServerPlayer player) {
            NightbladeImpactMessage.send(player, false);
        }
    }

    public float damageMultiplier() {
        return this.vulnerableTicks > 0 ? 2.0F : 1.0F;
    }

    void parried(ServerLevel level, ServerPlayer player) {
        Vec3 between = this.boss.getBoundingBox().getCenter().lerp(player.getBoundingBox().getCenter(), 0.5);
        NightbladeParry.playParryEffect(level, between);
        NightbladeImpactMessage.send(player, true);
        this.shake(level, 16.0, 0.8F, 0.3F);
        Vec3 away = this.boss.position().subtract(player.position()).multiply(1.0, 0.0, 1.0);
        away = away.lengthSqr() < 1.0E-4 ? player.getLookAngle().multiply(1.0, 0.0, 1.0) : away.normalize();
        this.boss.setDashing(false);
        this.boss.setDeltaMovement(away.x * 1.1, 0.35, away.z * 1.1);
        this.boss.hasImpulse = true;
        if (this.mirror) {
            this.mirrorLife = 1;
            return;
        }
        this.vulnerableTicks = 100;
        this.sound(SoundEvents.ELDER_GUARDIAN_HURT, 1.0F, 0.6F);
    }

    static SoundEvent dashSound() {
        return modSound("dash");
    }

    static SoundEvent nightbladeSound() {
        return modSound("nightblade_attack");
    }

    static SoundEvent modSound(String name) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("theabyss", name));
    }
}
