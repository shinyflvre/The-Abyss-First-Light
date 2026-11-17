package net.yezon.theabyss.mixin.accessor;

import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WalkAnimationState.class)
public interface WalkAnimationStateAccessor {
    @Accessor("speedOld")
    void theabyss$setSpeedOld(float speedOld);

    @Accessor("speed")
    void theabyss$setSpeed(float speed);

    @Accessor("position")
    void theabyss$setPosition(float position);
}
