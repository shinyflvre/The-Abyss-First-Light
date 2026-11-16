package net.yezon.theabyss.fabric.mixin.fluids;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface FluidEntityAccessor {
    @Accessor("fluidInteraction")
    EntityFluidInteraction theabyss$fluidInteraction();
}
