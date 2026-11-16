package net.yezon.theabyss.mixin.accessor;

import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FlowerBedBlock.class)
public interface FlowerBedBlockInvoker {
    @Invoker("<init>")
    static FlowerBedBlock theabyss$create(BlockBehaviour.Properties properties, int maxSegments) {
        throw new AssertionError();
    }
}
