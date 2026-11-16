package net.yezon.theabyss.fluid.types;

import net.minecraft.sounds.SoundEvents;
import net.yezon.theabyss.platform.common.SoundActions;
import net.yezon.theabyss.platform.fluids.FluidType;

public class ArenoFluidType extends FluidType {
    public ArenoFluidType() {
        super(
            FluidType.Properties.create()
                .fallDistanceModifier(0.0F)
                .canExtinguish(true)
                .supportsBoating(true)
                .canHydrate(true)
                .motionScale(0.007)
                .density(500)
                .viscosity(500)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
        );
    }

    @Override
    public boolean move(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.phys.Vec3 movementVector, double gravity) {
        return FluidSwimming.swimLikeWater(entity, movementVector, gravity);
    }
}
