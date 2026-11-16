package net.yezon.theabyss.boss.nightblade;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class NightbladeKeepDistanceGoal extends Goal {
    private final PathfinderMob boss;
    private final NightbladeCombat combat;

    public NightbladeKeepDistanceGoal(PathfinderMob boss, NightbladeCombat combat) {
        this.boss = boss;
        this.combat = combat;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.combat.wantsDistance();
    }

    @Override
    public void start() {
        LivingEntity target = this.boss.getTarget();
        if (target == null) {
            return;
        }
        Vec3 away = DefaultRandomPos.getPosAway(this.boss, 10, 4, target.position());
        if (away != null) {
            this.boss.getNavigation().moveTo(away.x, away.y, away.z, 1.3);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.combat.wantsDistance() && !this.boss.getNavigation().isDone();
    }

    @Override
    public void tick() {
        LivingEntity target = this.boss.getTarget();
        if (target != null) {
            this.boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }
}
