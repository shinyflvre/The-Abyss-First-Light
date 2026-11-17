package net.yezon.theabyss.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.common.NeoForgeMod;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;
import net.yezon.theabyss.events.SummonedSeekerOnEntityTickUpdateEvents;
import org.jspecify.annotations.Nullable;

public class SummonedHollowSeekerEntity extends PathfinderMob implements RangedAttackMob {
    public SummonedHollowSeekerEntity(EntityType<SummonedHollowSeekerEntity> type, Level world) {
        super(type, world);
        this.xpReward = 0;
        this.moveControl = new FlyingMoveControl(this, 10, true);
    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        return new FlyingPathNavigation(this, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AbyssMeleeAttackGoal(this, 4.0, false));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, false, false));
        this.goalSelector.addGoal(3, new Goal() {
            {
                this.setFlags(EnumSet.of(Goal.Flag.MOVE));
            }

            @Override
            public boolean canUse() {
                SummonedHollowSeekerEntity seeker = SummonedHollowSeekerEntity.this;
                return seeker.getTarget() != null && !seeker.getMoveControl().hasWanted();
            }

            @Override
            public boolean canContinueToUse() {
                SummonedHollowSeekerEntity seeker = SummonedHollowSeekerEntity.this;
                return seeker.getMoveControl().hasWanted() && seeker.getTarget() != null && seeker.getTarget().isAlive();
            }

            @Override
            public void start() {
                Vec3 eyes = SummonedHollowSeekerEntity.this.getTarget().getEyePosition(1.0F);
                SummonedHollowSeekerEntity.this.moveControl.setWantedPosition(eyes.x, eyes.y, eyes.z, 1.0);
            }

            @Override
            public void tick() {
                SummonedHollowSeekerEntity seeker = SummonedHollowSeekerEntity.this;
                LivingEntity target = seeker.getTarget();
                if (seeker.getBoundingBox().intersects(target.getBoundingBox())) {
                    seeker.doHurtTarget(target);
                } else if (seeker.distanceToSqr(target) < 400.0) {
                    Vec3 eyes = target.getEyePosition(1.0F);
                    seeker.moveControl.setWantedPosition(eyes.x, eyes.y, eyes.z, 1.0);
                }
            }
        });
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.25, 20, 10.0F) {
            @Override
            public boolean canContinueToUse() {
                return this.canUse();
            }
        });
    }

    @Override
    public SoundEvent getAmbientSound() {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:seeker_ambient"));
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.blaze.hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:seeker_ambient"));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageModifier, DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource damagesource, float amount) {
        if (!(this.level() instanceof ServerLevel level)) {
            return false;
        }
        if (damagesource.is(DamageTypes.IN_FIRE)
            || damagesource.is(DamageTypes.CAMPFIRE)
            || damagesource.getDirectEntity() instanceof ThrownPotion
            || damagesource.getDirectEntity() instanceof AreaEffectCloud
            || damagesource.is(DamageTypes.FALL)
            || damagesource.is(DamageTypes.DROWN)
            || damagesource.is(DamageTypes.FALLING_ANVIL)) {
            return false;
        }
        return super.hurt(damagesource, amount);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
        ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData livingdata
    ) {
        SpawnGroupData retval = super.finalizeSpawn(world, difficulty, reason, livingdata);
        SummonedSeekerOnEntityTickUpdateEvents.execute(world, this.getX(), this.getY(), this.getZ(), this);
        return retval;
    }

    @Override
    public void awardKillScore(Entity entity, int score, DamageSource damageSource) {
        super.awardKillScore(entity, score, damageSource);
        SummonedSeekerOnEntityTickUpdateEvents.execute(this.level(), this.getX(), this.getY(), this.getZ(), entity);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float flval) {
        RingOfBlackStrikeAttackProjectileEntity.shoot(this, target);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader world) {
        return world.isUnobstructed(this);
    }


    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.setNoGravity(true);
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.75)
            .add(Attributes.MAX_HEALTH, 8.0)
            .add(Attributes.ARMOR, 0.0)
            .add(Attributes.ATTACK_DAMAGE, 8.0)
            .add(Attributes.FOLLOW_RANGE, 350.0)
            .add(Attributes.FLYING_SPEED, 0.75)
            .add(NeoForgeMod.SWIM_SPEED, 0.75);
    }
}
