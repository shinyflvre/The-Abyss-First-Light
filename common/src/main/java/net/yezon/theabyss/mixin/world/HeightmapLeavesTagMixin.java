package net.yezon.theabyss.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.function.Predicate;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Heightmap.Types.class)
public abstract class HeightmapLeavesTagMixin {
    @Unique
    private static Predicate<BlockState> theabyss$source;
    @Unique
    private static Predicate<BlockState> theabyss$wrapped;

    @ModifyReturnValue(method = "isOpaque", at = @At("RETURN"))
    private Predicate<BlockState> theabyss$leavesByTag(Predicate<BlockState> original) {
        if ((Object)this != Heightmap.Types.MOTION_BLOCKING_NO_LEAVES) {
            return original;
        }
        if (theabyss$source != original || theabyss$wrapped == null) {
            theabyss$wrapped = state -> original.test(state) && !state.is(BlockTags.LEAVES);
            theabyss$source = original;
        }
        return theabyss$wrapped;
    }
}
