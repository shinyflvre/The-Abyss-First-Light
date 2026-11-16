package net.yezon.theabyss.fabric.mixin.fluids;

import java.util.List;
import net.minecraft.world.entity.EntityFluidInteraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityFluidInteraction.class)
public interface FluidInteractionAccessor {
    @Accessor("fluidTrackers")
    List<?> theabyss$fluidTrackers();
}
