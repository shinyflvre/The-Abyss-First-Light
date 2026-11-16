package net.yezon.theabyss.mixin.accessor;

import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DoorBlock.class)
public interface DoorBlockInvoker {
    @Invoker("<init>")
    static DoorBlock theabyss$create(BlockSetType type, BlockBehaviour.Properties properties) {
        throw new AssertionError();
    }
}
