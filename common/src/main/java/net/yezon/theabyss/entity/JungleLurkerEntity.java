package net.yezon.theabyss.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import org.jspecify.annotations.Nullable;

public class JungleLurkerEntity extends Monster implements GeoEntity {
    private static final String ANIM = "animation.lurker.";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop(ANIM + "idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop(ANIM + "walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop(ANIM + "run");
    private static final RawAnimation STALK = RawAnimation.begin().thenLoop(ANIM + "stalk");
    static final String[] ACTIONS = {"bite", "spit", "pounce"};
    private static final EntityDataAccessor<Boolean> CAMOUFLAGED = SynchedEntityData.defineId(JungleLurkerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final float CAMO_STEP = 1.0F / 14.0F;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private LurkerAttackGoal attackGoal;
    private float camo;
    private float camoO;

    public JungleLurkerEntity(EntityType<JungleLurkerEntity> type, Level world) {
        super(type, world);
        this.xpReward = 15;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CAMOUFLAGED, false);
    }

    public boolean isCamouflaged() {
        return this.entityData.get(CAMOUFLAGED);
    }

    void setCamouflaged(boolean on) {
        this.entityData.set(CAMOUFLAGED, on);
    }

    public float camouflage(float partialTick) {
        return Mth.lerp(partialTick, this.camoO, this.camo);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.camoO = this.camo;
            this.camo = this.isCamouflaged() ? Math.min(1.0F, this.camo + CAMO_STEP) : Math.max(0.0F, this.camo - CAMO_STEP * 2.0F);
        }
    }

    @Override
    public boolean shouldShowName() {
        return !this.isCamouflaged() && super.shouldShowName();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.attackGoal = new LurkerAttackGoal(this);
        this.goalSelector.addGoal(1, this.attackGoal);
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<JungleLurkerEntity>("base", 5, this::baseAnimation));
        AnimationController<JungleLurkerEntity> action = new AnimationController<>("action", 2, test -> PlayState.STOP);
        for (String name : ACTIONS) {
            action.triggerableAnim(name, RawAnimation.begin().thenPlay(ANIM + name));
        }
        controllers.add(action);
    }

    private PlayState baseAnimation(AnimationTest<JungleLurkerEntity> test) {
        if (this.isCamouflaged()) {
            test.controller().setAnimationSpeed(test.isMoving() ? 1.0 : 0.4);
            return test.setAndContinue(STALK);
        }
        if (test.isMoving()) {
            float speed = this.walkAnimation.speed();
            if (speed > 0.55F) {
                test.controller().setAnimationSpeed(1.0);
                return test.setAndContinue(RUN);
            }
            test.controller().setAnimationSpeed(Mth.clamp(speed * 2.0F, 0.6F, 1.3F));
            return test.setAndContinue(WALK);
        }
        test.controller().setAnimationSpeed(1.0);
        return test.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHitIn) {
        super.dropCustomDeathLoot(level, source, recentlyHitIn);
        this.spawnAtLocation(level, new ItemStack(TheabyssModItems.LURKER_SOBBER.get()));
    }

    @Override
    public @Nullable SoundEvent getAmbientSound() {
        return this.isCamouflaged() ? null : super.getAmbientSound();
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.generic.hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.generic.death"));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damagesource, float amount) {
        if (damagesource.is(DamageTypes.IN_FIRE)
            || damagesource.is(DamageTypes.CAMPFIRE)
            || damagesource.is(DamageTypes.FALL)
            || damagesource.is(DamageTypes.CACTUS)) {
            return false;
        }
        boolean hurt = super.hurtServer(level, damagesource, amount);
        if (hurt && this.isCamouflaged() && this.attackGoal != null) {
            this.attackGoal.reveal(level, false);
        }
        return hurt;
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
        event.register(
            TheabyssModEntities.JUNGLE_LURKER.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, world, reason, pos, random) -> world.getDifficulty() != Difficulty.PEACEFUL
                && Monster.isDarkEnoughToSpawn(world, pos, random)
                && Mob.checkMobSpawnRules(entityType, world, reason, pos, random),
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.22)
            .add(Attributes.MAX_HEALTH, 60.0)
            .add(Attributes.ARMOR, 2.0)
            .add(Attributes.ATTACK_DAMAGE, 12.0)
            .add(Attributes.FOLLOW_RANGE, 16.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 2.0);
    }
}
