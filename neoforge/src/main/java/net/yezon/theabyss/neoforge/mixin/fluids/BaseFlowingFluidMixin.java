package net.yezon.theabyss.neoforge.mixin.fluids;

import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.yezon.theabyss.neoforge.fluids.NeoForgeFluidTypes;
import net.yezon.theabyss.platform.fluids.BaseFlowingFluid;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BaseFlowingFluid.class)
public abstract class BaseFlowingFluidMixin extends FlowingFluid {
    @Unique
    private @Nullable FluidType theabyss$fluidType;

    @Override
    public FluidType getFluidType() {
        FluidType type = this.theabyss$fluidType;
        if (type == null) {
            type = NeoForgeFluidTypes.of(((BaseFlowingFluid)(Object)this).abyssType());
            this.theabyss$fluidType = type;
        }
        return type;
    }
}
