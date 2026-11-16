package net.yezon.theabyss.boss.nightblade;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public abstract class NightbladeMove {
    final String name;
    final int minPhase;
    boolean magic;
    int cooldownLeft;

    protected NightbladeMove(String name, int minPhase, int initialCooldown) {
        this.name = name;
        this.minPhase = minPhase;
        this.cooldownLeft = initialCooldown;
    }

    abstract int cooldown(int phase);

    abstract float score(NightbladeCombat combat, NightbladeCombat.Situation situation);

    void start(NightbladeCombat combat, ServerLevel level, NightbladeCombat.Situation situation) {
    }

    abstract boolean tick(NightbladeCombat combat, ServerLevel level, LivingEntity target, int t);

    void stop(NightbladeCombat combat) {
    }
}
