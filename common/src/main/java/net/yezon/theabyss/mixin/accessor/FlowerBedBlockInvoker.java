package net.yezon.theabyss.mixin.accessor;

import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PinkPetalsBlock.class)
public interface FlowerBedBlockInvoker {
    @Invoker("<init>")
    static PinkPetalsBlock theabyss$create(BlockBehaviour.Properties properties) {
        throw new AssertionError();
    }
}
