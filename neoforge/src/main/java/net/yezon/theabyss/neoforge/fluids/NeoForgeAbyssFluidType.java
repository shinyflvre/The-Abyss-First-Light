package net.yezon.theabyss.neoforge.fluids;

import java.util.Map;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.fluids.FluidType;

public final class NeoForgeAbyssFluidType extends FluidType {
    private final net.yezon.theabyss.platform.fluids.FluidType common;

    NeoForgeAbyssFluidType(net.yezon.theabyss.platform.fluids.FluidType common) {
        super(properties(common));
        this.common = common;
    }

    public net.yezon.theabyss.platform.fluids.FluidType common() {
        return this.common;
    }

    @Override
    public boolean move(LivingEntity entity, Vec3 movementVector, double gravity) {
        return this.common.move(entity, movementVector, gravity);
    }

    private static FluidType.Properties properties(net.yezon.theabyss.platform.fluids.FluidType common) {
        FluidType.Properties properties = FluidType.Properties.create()
            .motionScale(common.motionScale())
            .canPushEntity(common.canPushEntity())
            .canSwim(common.canSwim())
            .canDrown(common.canDrown())
            .fallDistanceModifier(common.getFallDistanceModifier())
            .canExtinguish(common.canExtinguish())
            .canConvertToSource(common.canConvertToSource())
            .supportsBoating(common.supportsBoating())
            .pathType(common.getPathType())
            .adjacentPathType(common.getAdjacentPathType())
            .canHydrate(common.canHydrate())
            .lightLevel(common.getLightLevel())
            .density(common.getDensity())
            .temperature(common.getTemperature())
            .viscosity(common.getViscosity())
            .rarity(common.getRarity());
        String descriptionId = common.descriptionId();
        if (descriptionId != null) {
            properties.descriptionId(descriptionId);
        }
        for (Map.Entry<net.yezon.theabyss.platform.common.SoundAction, SoundEvent> sound : common.getSounds().entrySet()) {
            properties.sound(SoundAction.get(sound.getKey().name()), sound.getValue());
        }
        return properties;
    }
}
