package net.yezon.theabyss.entity;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.GeckoLibUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.boss.nightblade.NightbladeCombat;
import net.yezon.theabyss.boss.nightblade.NightbladeMoveGoal;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import org.jspecify.annotations.Nullable;

public class NightbladeBossEntity extends Monster implements GeoEntity {
    private static final String ANIM = "animation.nightblade.";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop(ANIM + "idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop(ANIM + "walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop(ANIM + "run");
    private static final RawAnimation DASH = RawAnimation.begin().thenLoop(ANIM + "dash");
    private static final RawAnimation HOVER = RawAnimation.begin().thenLoop(ANIM + "hover");
    public static final String[] ACTIONS = {"slash_right", "slash_left", "signature_charge", "signature_blink_a", "signature_blink_b",
        "signature_lunge", "signature_recover", "stagger", "stagger_long", "counter_stance", "counter_strike", "blade_throw", "blade_catch", "rift_stab", "arena_lift"};
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private boolean unavoidable;
    public static final EntityDataAccessor<Boolean> DASHING = SynchedEntityData.defineId(NightbladeBossEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(NightbladeBossEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> MIRROR = SynchedEntityData.defineId(NightbladeBossEntity.class, EntityDataSerializers.BOOLEAN);
    private NightbladeCombat combat;
    private boolean mirror;
    private static final double MIRROR_HEALTH = 40.0;
    private final ServerBossEvent bossInfo = new ServerBossEvent(
        this.getDisplayName(), BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS
    );

    public NightbladeBossEntity(EntityType<NightbladeBossEntity> type, Level world) {
        super(type, world);
        this.xpReward = 1000;
        this.setCustomName(Component.translatable("entity.theabyss.nightblade_boss"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TheabyssModItems.ABYSS_SWORD.get()));
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(TheabyssModItems.ABYSS_SWORD.get()));
    }

    @Override
    public int getTeamColor() {
        return net.yezon.theabyss.ring.EagleBossTracker.OUTLINE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DASHING, false);
        builder.define(PHASE, 1);
        builder.define(MIRROR, false);
    }

    public boolean isDashing() {
        return this.entityData.get(DASHING);
    }

    public void setDashing(boolean dashing) {
        this.entityData.set(DASHING, dashing);
    }

    public int getPhase() {
        return this.entityData.get(PHASE);
    }

    public void setPhase(int phase) {
        this.entityData.set(PHASE, phase);
    }

    public boolean isMirrorSynced() {
        return this.entityData.get(MIRROR);
    }

    public static void spawnMirror(ServerLevel level, NightbladeBossEntity original, Vec3 at, LivingEntity target, int phase, int delay) {
        NightbladeBossEntity image = net.yezon.theabyss.init.TheabyssModEntities.NIGHTBLADE_BOSS.get().create(level);
        if (image == null) {
            return;
        }
        image.mirror = true;
        image.entityData.set(MIRROR, true);
        image.xpReward = 0;
        image.moveTo(at.x, at.y, at.z, original.getYRot(), original.getXRot());
        var health = image.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(MIRROR_HEALTH);
        }
        image.setHealth((float)MIRROR_HEALTH);
        var attack = image.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.setBaseValue(attack.getBaseValue() * 0.4);
        }
        image.combat.becomeMirror(target, phase, delay);
        level.addFreshEntity(image);
    }

    public static @Nullable NightbladeBossEntity spawnPuppet(ServerLevel level, NightbladeBossEntity original, Vec3 at) {
        NightbladeBossEntity image = net.yezon.theabyss.init.TheabyssModEntities.NIGHTBLADE_BOSS.get().create(level);
        if (image == null) {
            return null;
        }
        image.mirror = true;
        image.entityData.set(MIRROR, true);
        image.xpReward = 0;
        image.moveTo(at.x, at.y, at.z, original.getYRot(), original.getXRot());
        var health = image.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(MIRROR_HEALTH);
        }
        image.setHealth((float)MIRROR_HEALTH);
        image.setNoAi(true);
        image.setNoGravity(true);
        level.addFreshEntity(image);
        return image;
    }

    public boolean isMirror() {
        return this.mirror;
    }

    @Override
    public boolean shouldBeSaved() {
        return !this.mirror && super.shouldBeSaved();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.combat = new NightbladeCombat(this);
        this.goalSelector.addGoal(0, new NightbladeMoveGoal(this.combat));
        this.goalSelector.addGoal(1, new net.yezon.theabyss.boss.nightblade.NightbladeKeepDistanceGoal(this, this.combat));
        this.goalSelector.addGoal(2, new AbyssMeleeAttackGoal(this, 1.2, false));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHitIn) {
        if (this.mirror) {
            return;
        }
        super.dropCustomDeathLoot(level, source, recentlyHitIn);
        this.spawnAtLocation(new ItemStack(TheabyssModItems.ABYSS_SWORD.get()));
    }

    public void hurtUnavoidable(ServerLevel level, DamageSource source, float amount) {
        this.unavoidable = true;
        try {
            this.hurt(source, amount);
        } finally {
            this.unavoidable = false;
        }
    }

    @Override
    public boolean hurt(DamageSource damagesource, float amount) {
        if (!(this.level() instanceof ServerLevel level)) {
            return false;
        }
        if (!this.unavoidable && this.combat != null && this.combat.interceptHit(level, damagesource)) {
            return false;
        }
        if (!this.unavoidable && this.combat != null && this.combat.tryDodge(level, damagesource)) {
            return false;
        }
        if (damagesource.is(DamageTypes.IN_FIRE)
            || damagesource.is(DamageTypes.CAMPFIRE)
            || damagesource.getDirectEntity() instanceof AbstractArrow
            || damagesource.getDirectEntity() instanceof ThrownPotion
            || damagesource.getDirectEntity() instanceof AreaEffectCloud
            || damagesource.is(DamageTypes.FALL)
            || damagesource.is(DamageTypes.CACTUS)
            || damagesource.is(DamageTypes.DROWN)
            || damagesource.is(DamageTypes.EXPLOSION)
            || damagesource.is(DamageTypes.TRIDENT)
            || damagesource.is(DamageTypes.FALLING_ANVIL)
            || damagesource.is(DamageTypes.DRAGON_BREATH)
            || damagesource.is(DamageTypes.WITHER)
            || damagesource.is(DamageTypes.WITHER_SKULL)) {
            return false;
        }
        if (this.combat != null) {
            amount *= this.combat.damageMultiplier();
        }
        return super.hurt(damagesource, amount);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
        ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData livingdata
    ) {
        return super.finalizeSpawn(world, difficulty, reason, livingdata);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() && this.isDashing()) {
            Vec3 from = new Vec3(this.xo, this.yo, this.zo);
            Vec3 to = this.position();
            int steps = Math.max(1, (int)Math.ceil(from.distanceTo(to) / 0.2));
            for (int i = 0; i < steps; i++) {
                Vec3 at = from.lerp(to, (i + this.random.nextFloat()) / steps);
                this.level().addParticle(TheabyssModParticleTypes.NIGHTBLADE_TRAIL.get(), at.x, at.y + 1.3, at.z, 0.0, 0.0, 0.0);
                this.level().addParticle(TheabyssModParticleTypes.NIGHTBLADE_TRAIL.get(), at.x, at.y + 0.6, at.z, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<NightbladeBossEntity>(this, "base", 5, this::baseAnimation));
        AnimationController<NightbladeBossEntity> action = new AnimationController<>(this, "action", 2, test -> PlayState.STOP);
        for (String name : ACTIONS) {
            action.triggerableAnim(name, RawAnimation.begin().thenPlay(ANIM + name));
        }
        controllers.add(action);
    }

    private PlayState baseAnimation(AnimationState<NightbladeBossEntity> test) {
        if (this.isDashing()) {
            return test.setAndContinue(DASH);
        }
        if (!this.onGround() && (this.isNoGravity() || this.getDeltaMovement().y > -0.6)) {
            return test.setAndContinue(HOVER);
        }
        if (test.isMoving()) {
            return test.setAndContinue(this.walkAnimation.speed() > 0.55F ? RUN : WALK);
        }
        return test.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void swing(InteractionHand hand, boolean updateSelf) {
        super.swing(hand, updateSelf);
        if (!this.level().isClientSide() && this.combat != null) {
            this.combat.slash(hand);
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.combat != null) {
            this.combat.abort();
        }
        super.remove(reason);
    }

    @Override
    public boolean canUsePortal(boolean ignorePassenger) {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (!this.mirror) {
            this.bossInfo.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossInfo.removePlayer(player);
    }

    @Override
    protected void customServerAiStep() {
        ServerLevel level = (ServerLevel) this.level();
        super.customServerAiStep();
        this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
        this.combat.tick(level);
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.4)
            .add(Attributes.MAX_HEALTH, 1024.0)
            .add(Attributes.ARMOR, 14.0)
            .add(Attributes.ATTACK_DAMAGE, 10.0)
            .add(Attributes.FOLLOW_RANGE, 128.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 4.0)
            .add(Attributes.ATTACK_KNOCKBACK, 2.0);
    }
}
