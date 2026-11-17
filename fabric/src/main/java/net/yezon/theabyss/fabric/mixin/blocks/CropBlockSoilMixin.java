package net.yezon.theabyss.fabric.mixin.blocks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.yezon.theabyss.fabric.blocks.FabricSoil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CropBlock.class)
public abstract class CropBlockSoilMixin {
    @ModifyReturnValue(method = "mayPlaceOn", at = @At("RETURN"))
    private boolean theabyss$modFarmland(boolean original, @Local(argsOnly = true) BlockState soil) {
        return original || FabricSoil.isAbyssFarmland(soil);
    }

    @WrapOperation(method = "getGrowthSpeed", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 0))
    private static boolean theabyss$modSoilGrowth(BlockState soil, Block farmland, Operation<Boolean> original, @Local(argsOnly = true) Block crop,
                                                  @Local(argsOnly = true) BlockGetter level, @Local(ordinal = 1) BlockPos below,
                                                  @Local(ordinal = 0) int xx, @Local(ordinal = 1) int zz) {
        BlockPos soilPos = below.offset(xx, 0, zz);
        boolean vanilla = original.call(soil, farmland) || FabricSoil.isAbyssFarmland(soil);
        return FabricSoil.decide(FabricSoil.decision(soil, level, soilPos, Direction.UP, crop.defaultBlockState()), vanilla);
    }

    @WrapOperation(method = "getGrowthSpeed", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private static Comparable<?> theabyss$modSoilMoisture(BlockState soil, Property<?> property, Operation<Comparable<?>> original) {
        return soil.hasProperty(property) ? original.call(soil, property) : Integer.valueOf(0);
    }
}
