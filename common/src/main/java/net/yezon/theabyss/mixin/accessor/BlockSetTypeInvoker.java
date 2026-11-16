package net.yezon.theabyss.mixin.accessor;

import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockSetType.class)
public interface BlockSetTypeInvoker {
    @Invoker("register")
    static BlockSetType theabyss$register(BlockSetType type) {
        throw new AssertionError();
    }
}
