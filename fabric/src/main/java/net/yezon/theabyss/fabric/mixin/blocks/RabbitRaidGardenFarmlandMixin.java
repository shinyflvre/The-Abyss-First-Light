package net.yezon.theabyss.fabric.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.fabric.blocks.FabricSoil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.entity.animal.Rabbit$RaidGardenGoal")
public abstract class RabbitRaidGardenFarmlandMixin {
    @WrapOperation(method = "isValidTarget", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 0))
    private boolean theabyss$modFarmland(BlockState state, Block farmland, Operation<Boolean> original) {
        return original.call(state, farmland) || FabricSoil.isAbyssFarmland(state);
    }
}
