package net.yezon.theabyss.mixin.accessor;

import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractCauldronBlock.class)
public interface AbstractCauldronBlockInvoker {
    @Invoker("canReceiveStalactiteDrip")
    boolean theabyss$canReceiveStalactiteDrip(Fluid fluid);
}
