package net.yezon.theabyss.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.material.Fluid;
import net.yezon.theabyss.block.cave.AbyssPointedDripstoneBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractCauldronBlock.class)
public abstract class AbstractCauldronBlockDripMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/PointedDripstoneBlock;findStalactiteTipAboveCauldron(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"))
    private BlockPos theabyss$abyssStalactite(Level level, BlockPos pos, Operation<BlockPos> original) {
        BlockPos found = original.call(level, pos);
        return found != null ? found : AbyssPointedDripstoneBlock.findStalactiteTipAboveCauldron(level, pos);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/PointedDripstoneBlock;getCauldronFillFluidType(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/Fluid;"))
    private Fluid theabyss$abyssFillFluid(ServerLevel level, BlockPos pos, Operation<Fluid> original) {
        if (level.getBlockState(pos).getBlock() instanceof AbyssPointedDripstoneBlock) {
            return AbyssPointedDripstoneBlock.getCauldronFillFluidType(level, pos);
        }
        return original.call(level, pos);
    }
}
