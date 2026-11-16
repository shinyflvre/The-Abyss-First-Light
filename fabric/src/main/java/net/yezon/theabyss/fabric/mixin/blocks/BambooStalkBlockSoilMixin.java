package net.yezon.theabyss.fabric.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.fabric.blocks.FabricSoil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BambooStalkBlock.class)
public abstract class BambooStalkBlockSoilMixin {
    @WrapOperation(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean theabyss$modSoilPlacement(BlockState soil, TagKey<Block> tag, Operation<Boolean> original, @Local(argsOnly = true) BlockPlaceContext context) {
        BlockPos soilPos = context.getClickedPos().below();
        BlockState plant = ((Block)(Object)this).defaultBlockState();
        return FabricSoil.decide(FabricSoil.decision(soil, context.getLevel(), soilPos, Direction.UP, plant), original.call(soil, tag));
    }
}
