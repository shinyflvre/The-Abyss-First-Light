package net.yezon.theabyss.fabric.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.block.hooks.ClimbableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityClimbMixin {
    @WrapOperation(method = "onClimbable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean theabyss$modLadders(BlockState state, TagKey<Block> tag, Operation<Boolean> original) {
        if (tag == BlockTags.CLIMBABLE && state.getBlock() instanceof ClimbableBlock block) {
            LivingEntity self = (LivingEntity)(Object)this;
            return block.isLadder(state, self.level(), self.blockPosition(), self);
        }
        return original.call(state, tag);
    }
}
