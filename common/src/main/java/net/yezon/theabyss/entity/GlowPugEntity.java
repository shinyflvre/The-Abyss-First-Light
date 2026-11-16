package net.yezon.theabyss.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FollowMobGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;

public class GlowPugEntity extends Monster {
    public GlowPugEntity(EntityType<GlowPugEntity> type, Level world) {
        super(type, world);
        this.xpReward = 15;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AbyssMeleeAttackGoal(this, 1.2, false));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new FollowMobGoal(this, 1.0, 10.0F, 5.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.12)
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.ARMOR, 1.0)
            .add(Attributes.ATTACK_DAMAGE, 8.0)
            .add(Attributes.FOLLOW_RANGE, 16.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
            .add(Attributes.ATTACK_KNOCKBACK, 0.1);
    }
}
