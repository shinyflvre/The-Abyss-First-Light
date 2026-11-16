package net.yezon.theabyss.mixin.accessor;

import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ButtonBlock.class)
public interface ButtonBlockInvoker {
    @Invoker("<init>")
    static ButtonBlock theabyss$create(BlockSetType type, int ticksToStayPressed, BlockBehaviour.Properties properties) {
        throw new AssertionError();
    }
}
