package net.yezon.theabyss.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.yezon.theabyss.events.SetMiniBossEvents;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import org.jspecify.annotations.Nullable;

public class TheRokaEntity extends Monster implements GeoEntity {
    private static final String ANIM = "animation.the_roka.";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop(ANIM + "idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop(ANIM + "walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop(ANIM + "run");
    static final String[] ACTIONS = {"combo", "smash", "roar", "charge_windup", "charge_gore", "charge_stop", "leap_crouch", "leap_land", "stagger"};
    static final String[] LOOPS = {"charge_run", "leap_air"};
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private RokaAttackGoal attackGoal;
    static final double SPEED = 0.2625;

    public TheRokaEntity(EntityType<TheRokaEntity> type, Level world) {
        super(type, world);
        this.xpReward = 50;
    }

    static float halfHearts(float hp) {
        return Math.max(1, Math.round(hp));
    }

    public void devHold(boolean hold) {
        if (this.attackGoal != null) {
            this.attackGoal.devHold = hold;
            if (hold) {
                this.attackGoal.devStop();
            }
        }
    }

    public void devAttack(String name, net.minecraft.world.entity.LivingEntity target) {
        if (this.attackGoal != null) {
            this.attackGoal.devForce(name, target);
        }
    }

    public boolean isEnraged() {
        return this.attackGoal != null && this.attackGoal.enraged();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<TheRokaEntity>("base", 5, this::baseAnimation));
        AnimationController<TheRokaEntity> action = new AnimationController<>("action", 2, test -> PlayState.STOP);
        for (String name : ACTIONS) {
            action.triggerableAnim(name, RawAnimation.begin().thenPlay(ANIM + name));
        }
        for (String name : LOOPS) {
            action.triggerableAnim(name, RawAnimation.begin().thenLoop(ANIM + name));
        }
        controllers.add(action);
    }

    private PlayState baseAnimation(AnimationTest<TheRokaEntity> test) {
        if (test.isMoving()) {
            float speed = this.walkAnimation.speed();
            if (speed > 0.75F) {
                test.controller().setAnimationSpeed(1.0);
                return test.setAndContinue(RUN);
            }
            test.controller().setAnimationSpeed(Mth.clamp(speed * 1.6F, 0.6F, 1.4F));
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
    public boolean removeWhenFarAway(double distSqr) {
        return distSqr > 128.0 * 128.0 && this.getTarget() == null;
    }

    @Override
    public int getTeamColor() {
        return net.yezon.theabyss.ring.EagleBossTracker.OUTLINE;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.attackGoal = new RokaAttackGoal(this);
        this.goalSelector.addGoal(1, this.attackGoal);
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHitIn) {
        super.dropCustomDeathLoot(level, source, recentlyHitIn);
        this.spawnAtLocation(level, new ItemStack(TheabyssModItems.ROKA_HORN.get()));
    }

    @Override
    public SoundEvent getAmbientSound() {
        return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.zoglin.ambient"));
    }

    @Override
    public void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.zoglin.step")), 0.15F, 1.0F);
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.zoglin.hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.zoglin.death"));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damagesource, float amount) {
        if (damagesource.is(DamageTypes.IN_FIRE)
            || damagesource.is(DamageTypes.CAMPFIRE)
            || damagesource.getDirectEntity() instanceof AbstractArrow
            || damagesource.getDirectEntity() instanceof AbstractThrownPotion
            || damagesource.getDirectEntity() instanceof AreaEffectCloud
            || damagesource.is(DamageTypes.FALL)
            || damagesource.is(DamageTypes.CACTUS)
            || damagesource.is(DamageTypes.DROWN)) {
            return false;
        }
        return super.hurtServer(level, damagesource, amount);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
        ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData livingdata
    ) {
        SpawnGroupData retval = super.finalizeSpawn(world, difficulty, reason, livingdata);
        SetMiniBossEvents.execute(world, this.getX(), this.getY(), this.getZ());
        return retval;
    }

    @Override
    public boolean canUsePortal(boolean ignorePassenger) {
        return false;
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
        event.register(
            TheabyssModEntities.THE_ROKA.get(),
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
            .add(Attributes.MOVEMENT_SPEED, SPEED)
            .add(Attributes.MAX_HEALTH, 300.0)
            .add(Attributes.ARMOR, 4.0)
            .add(Attributes.ATTACK_DAMAGE, 16.0)
            .add(Attributes.FOLLOW_RANGE, 24.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 4.0)
            .add(Attributes.ATTACK_KNOCKBACK, 1.0);
    }
}
