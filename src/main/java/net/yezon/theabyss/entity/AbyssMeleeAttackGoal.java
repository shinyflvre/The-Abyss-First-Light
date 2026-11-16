package net.yezon.theabyss.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class AbyssMeleeAttackGoal extends MeleeAttackGoal {
    public AbyssMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
    }

    @Override
    protected boolean canPerformAttack(LivingEntity target) {
        double reach = this.mob.getBbWidth() * this.mob.getBbWidth() + target.getBbWidth();
        return this.isTimeToAttack() && this.mob.distanceToSqr(target) <= reach;
    }
}
