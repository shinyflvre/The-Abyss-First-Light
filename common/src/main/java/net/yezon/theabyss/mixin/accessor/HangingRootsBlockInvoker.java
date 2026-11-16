package net.yezon.theabyss.mixin.accessor;

import net.minecraft.world.level.block.HangingRootsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HangingRootsBlock.class)
public interface HangingRootsBlockInvoker {
    @Invoker("<init>")
    static HangingRootsBlock theabyss$create(BlockBehaviour.Properties properties) {
        throw new AssertionError();
    }
}
