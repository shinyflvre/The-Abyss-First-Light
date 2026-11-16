package net.yezon.theabyss.boss.nightblade;

import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;

public class NightbladeMoveGoal extends Goal {
    private final NightbladeCombat combat;

    public NightbladeMoveGoal(NightbladeCombat combat) {
        this.combat = combat;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return this.combat.busy();
    }

    @Override
    public boolean canContinueToUse() {
        return this.combat.busy();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
