package net.yezon.theabyss.mixin.blocks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.function.Predicate;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.block.AbyssBeeFlowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.entity.animal.Bee$BeePollinateGoal")
public abstract class BeePollinateGoalAbyssFlowerMixin {
    @ModifyExpressionValue(
        method = "findNearbyFlower()Ljava/util/Optional;",
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/animal/Bee$BeePollinateGoal;VALID_POLLINATION_BLOCKS:Ljava/util/function/Predicate;")
    )
    private Predicate<BlockState> theabyss$abyssFlowers(Predicate<BlockState> flowers) {
        return state -> AbyssBeeFlowers.isModBlock(state) ? AbyssBeeFlowers.attractsBees(state) : flowers.test(state);
    }
}
