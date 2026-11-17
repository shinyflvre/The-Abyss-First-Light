package net.yezon.theabyss.fabric.event;

import java.util.ArrayDeque;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public interface FabricDamageState {
    ArrayDeque<FabricDamageFrame> theabyss$damageFrames();

    @Nullable Player theabyss$lastHurtByPlayer();
}
