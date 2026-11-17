package net.yezon.theabyss.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;
import net.yezon.theabyss.events.SeekerParticlesEvents;
import net.yezon.theabyss.events.SummonedSeekerOnEntityTickUpdateEvents;
import org.jspecify.annotations.Nullable;

public class SummonedSeekerEntity extends PathfinderMob {
    public SummonedSeekerEntity(EntityType<SummonedSeekerEntity> type, Level world) {
        super(type, world);
        this.xpReward = 0;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AbyssMeleeAttackGoal(this, 2.0, false));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, false, false));
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
    public boolean hurt(DamageSource damagesource, float amount) {
        if (!(this.level() instanceof ServerLevel level)) {
            return false;
        }
        if (damagesource.is(DamageTypes.IN_FIRE)
            || damagesource.is(DamageTypes.CAMPFIRE)
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
    public void baseTick() {
        super.baseTick();
        SeekerParticlesEvents.execute(this.level(), this.getX(), this.getY(), this.getZ());
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.34)
            .add(Attributes.MAX_HEALTH, 8.0)
            .add(Attributes.ARMOR, 0.0)
            .add(Attributes.ATTACK_DAMAGE, 8.0)
            .add(Attributes.FOLLOW_RANGE, 500.0);
    }
}
