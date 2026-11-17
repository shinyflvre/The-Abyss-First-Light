package net.yezon.theabyss.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;
import net.yezon.theabyss.events.GuardSpawnEffectEvents;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import org.jspecify.annotations.Nullable;

public class SoulGuardEntity extends Monster {
    public SoulGuardEntity(EntityType<SoulGuardEntity> type, Level world) {
        super(type, world);
        this.xpReward = 35;
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TheabyssModItems.BONE_SWORD_ITEM.get()));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TheabyssModItems.BONE_ARMOR_HELMET.get()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TheabyssModItems.BONE_ARMOR_CHESTPLATE.get()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TheabyssModItems.BONE_ARMOR_LEGGINGS.get()));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TheabyssModItems.BONE_ARMOR_BOOTS.get()));
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
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHitIn) {
        super.dropCustomDeathLoot(level, source, recentlyHitIn);
        this.spawnAtLocation(new ItemStack(TheabyssModItems.SOUL_HEART.get()));
    }

    @Override
    public boolean hurt(DamageSource damagesource, float amount) {
        if (!(this.level() instanceof ServerLevel level)) {
            return false;
        }
        if (damagesource.is(DamageTypes.IN_FIRE)
            || damagesource.is(DamageTypes.CAMPFIRE)
            || damagesource.getDirectEntity() instanceof AbstractArrow
            || damagesource.getDirectEntity() instanceof ThrownPotion
            || damagesource.getDirectEntity() instanceof AreaEffectCloud
            || damagesource.is(DamageTypes.FALL)
            || damagesource.is(DamageTypes.CACTUS)
            || damagesource.is(DamageTypes.DROWN)) {
            return false;
        }
        return super.hurt(damagesource, amount);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
        ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData livingdata
    ) {
        SpawnGroupData retval = super.finalizeSpawn(world, difficulty, reason, livingdata);
        GuardSpawnEffectEvents.execute(world, this.getX(), this.getY(), this.getZ(), this);
        return retval;
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
        event.register(
            TheabyssModEntities.SOUL_GUARD.get(),
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
            .add(Attributes.MOVEMENT_SPEED, 0.34)
            .add(Attributes.MAX_HEALTH, 40.0)
            .add(Attributes.ARMOR, 2.0)
            .add(Attributes.ATTACK_DAMAGE, 10.0)
            .add(Attributes.FOLLOW_RANGE, 16.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.2);
    }
}
