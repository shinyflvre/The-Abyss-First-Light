package net.yezon.theabyss.mixin.blocks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.block.AbyssBeeFlowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Bee.class)
public abstract class BeeAbyssFlowerMixin {
    @ModifyReturnValue(method = "isFlowerValid(Lnet/minecraft/core/BlockPos;)Z", at = @At("RETURN"))
    private boolean theabyss$abyssFlowerValid(boolean original, @Local(argsOnly = true) BlockPos pos) {
        Level level = ((Bee) (Object) this).level();
        if (!level.isLoaded(pos)) {
            return original;
        }
        BlockState state = level.getBlockState(pos);
        return AbyssBeeFlowers.isModBlock(state) ? AbyssBeeFlowers.attractsBees(state) : original;
    }

    @ModifyReturnValue(method = "isFood(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"))
    private boolean theabyss$abyssBeeFood(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return AbyssBeeFlowers.isModItem(stack) ? AbyssBeeFlowers.isBeeFood(stack) : original;
    }

    @WrapOperation(
        method = "registerGoals()V",
        at = @At(value = "NEW",
            target = "(Lnet/minecraft/world/entity/PathfinderMob;DLjava/util/function/Predicate;Z)Lnet/minecraft/world/entity/ai/goal/TemptGoal;")
    )
    private TemptGoal theabyss$abyssBeeTempt(PathfinderMob mob, double speedModifier, Predicate<ItemStack> items, boolean canScare,
                                             Operation<TemptGoal> original) {
        Predicate<ItemStack> abyssItems = stack -> AbyssBeeFlowers.isModItem(stack) ? AbyssBeeFlowers.isBeeFood(stack) : items.test(stack);
        return original.call(mob, speedModifier, abyssItems, canScare);
    }
}
