package net.yezon.theabyss.mixin.accessor;

import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PressurePlateBlock.class)
public interface PressurePlateBlockInvoker {
    @Invoker("<init>")
    static PressurePlateBlock theabyss$create(BlockSetType type, BlockBehaviour.Properties properties) {
        throw new AssertionError();
    }
}
