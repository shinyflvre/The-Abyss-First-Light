package net.yezon.theabyss.entity;

import net.minecraft.world.Difficulty;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;
import net.yezon.theabyss.events.MagicianAbilitiesEvents;
import net.yezon.theabyss.events.MagicianSpawnEvents;
import net.yezon.theabyss.init.TheabyssModEntities;

public class MagicianEntity extends Monster {
    public MagicianEntity(EntityType<MagicianEntity> type, Level world) {
        super(type, world);
        this.xpReward = 0;
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
        this.goalSelector.addGoal(1, new AbyssMeleeAttackGoal(this, 1.2, false));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.death"));
    }

    @Override
    public boolean hurt(DamageSource damagesource, float amount) {
        if (!(this.level() instanceof ServerLevel level)) {
            return false;
        }
        if (damagesource.is(DamageTypes.IN_FIRE)
            || damagesource.is(DamageTypes.CAMPFIRE)
            || damagesource.is(DamageTypes.FALL)
            || damagesource.is(DamageTypes.CACTUS)
            || damagesource.is(DamageTypes.EXPLOSION)
            || damagesource.is(DamageTypes.WITHER)
            || damagesource.is(DamageTypes.WITHER_SKULL)) {
            return false;
        }
        return super.hurt(damagesource, amount);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        MagicianAbilitiesEvents.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
    }

    @Override
    public boolean canUsePortal(boolean ignorePassenger) {
        return false;
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
        event.register(
            TheabyssModEntities.MAGICIAN.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, world, reason, pos, random) -> world.getDifficulty() != Difficulty.PEACEFUL && MagicianSpawnEvents.execute(world),
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.2)
            .add(Attributes.MAX_HEALTH, 100.0)
            .add(Attributes.ARMOR, 0.0)
            .add(Attributes.ATTACK_DAMAGE, 10.0)
            .add(Attributes.FOLLOW_RANGE, 16.0);
    }
}
