package net.yezon.theabyss.platform.fluids;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.common.SoundAction;
import org.jspecify.annotations.Nullable;

public class FluidType {
    private final @Nullable String descriptionId;
    private final double motionScale;
    private final boolean canPushEntity;
    private final boolean canSwim;
    private final boolean canDrown;
    private final float fallDistanceModifier;
    private final boolean canExtinguish;
    private final boolean canConvertToSource;
    private final boolean supportsBoating;
    private final @Nullable PathType pathType;
    private final @Nullable PathType adjacentPathType;
    private final boolean canHydrate;
    private final int lightLevel;
    private final int density;
    private final int temperature;
    private final int viscosity;
    private final Rarity rarity;
    private final Map<SoundAction, SoundEvent> sounds;

    public FluidType(Properties properties) {
        this.descriptionId = properties.descriptionId;
        this.motionScale = properties.motionScale;
        this.canPushEntity = properties.canPushEntity;
        this.canSwim = properties.canSwim;
        this.canDrown = properties.canDrown;
        this.fallDistanceModifier = properties.fallDistanceModifier;
        this.canExtinguish = properties.canExtinguish;
        this.canConvertToSource = properties.canConvertToSource;
        this.supportsBoating = properties.supportsBoating;
        this.pathType = properties.pathType;
        this.adjacentPathType = properties.adjacentPathType;
        this.canHydrate = properties.canHydrate;
        this.lightLevel = properties.lightLevel;
        this.density = properties.density;
        this.temperature = properties.temperature;
        this.viscosity = properties.viscosity;
        this.rarity = properties.rarity;
        this.sounds = Collections.unmodifiableMap(new LinkedHashMap<>(properties.sounds));
    }

    public boolean move(LivingEntity entity, Vec3 movementVector, double gravity) {
        return false;
    }

    public @Nullable String descriptionId() {
        return this.descriptionId;
    }

    public double motionScale() {
        return this.motionScale;
    }

    public boolean canPushEntity() {
        return this.canPushEntity;
    }

    public boolean canSwim() {
        return this.canSwim;
    }

    public boolean canDrown() {
        return this.canDrown;
    }

    public float getFallDistanceModifier() {
        return this.fallDistanceModifier;
    }

    public boolean canExtinguish() {
        return this.canExtinguish;
    }

    public boolean canConvertToSource() {
        return this.canConvertToSource;
    }

    public boolean supportsBoating() {
        return this.supportsBoating;
    }

    public @Nullable PathType getPathType() {
        return this.pathType;
    }

    public @Nullable PathType getAdjacentPathType() {
        return this.adjacentPathType;
    }

    public boolean canHydrate() {
        return this.canHydrate;
    }

    public int getLightLevel() {
        return this.lightLevel;
    }

    public int getDensity() {
        return this.density;
    }

    public int getTemperature() {
        return this.temperature;
    }

    public int getViscosity() {
        return this.viscosity;
    }

    public Rarity getRarity() {
        return this.rarity;
    }

    public @Nullable SoundEvent getSound(SoundAction action) {
        return this.sounds.get(action);
    }

    public Map<SoundAction, SoundEvent> getSounds() {
        return this.sounds;
    }

    public static final class Properties {
        private @Nullable String descriptionId;
        private double motionScale = 0.014;
        private boolean canPushEntity = true;
        private boolean canSwim = true;
        private boolean canDrown = true;
        private float fallDistanceModifier = 0.5F;
        private boolean canExtinguish;
        private boolean canConvertToSource;
        private boolean supportsBoating;
        private @Nullable PathType pathType = PathType.WATER;
        private @Nullable PathType adjacentPathType = PathType.WATER_BORDER;
        private boolean canHydrate;
        private int lightLevel;
        private int density = 1000;
        private int temperature = 300;
        private int viscosity = 1000;
        private Rarity rarity = Rarity.COMMON;
        private final Map<SoundAction, SoundEvent> sounds = new LinkedHashMap<>();

        private Properties() {
        }

        public static Properties create() {
            return new Properties();
        }

        public Properties descriptionId(String descriptionId) {
            this.descriptionId = descriptionId;
            return this;
        }

        public Properties motionScale(double motionScale) {
            this.motionScale = motionScale;
            return this;
        }

        public Properties canPushEntity(boolean canPushEntity) {
            this.canPushEntity = canPushEntity;
            return this;
        }

        public Properties canSwim(boolean canSwim) {
            this.canSwim = canSwim;
            return this;
        }

        public Properties canDrown(boolean canDrown) {
            this.canDrown = canDrown;
            return this;
        }

        public Properties fallDistanceModifier(float fallDistanceModifier) {
            this.fallDistanceModifier = fallDistanceModifier;
            return this;
        }

        public Properties canExtinguish(boolean canExtinguish) {
            this.canExtinguish = canExtinguish;
            return this;
        }

        public Properties canConvertToSource(boolean canConvertToSource) {
            this.canConvertToSource = canConvertToSource;
            return this;
        }

        public Properties supportsBoating(boolean supportsBoating) {
            this.supportsBoating = supportsBoating;
            return this;
        }

        public Properties pathType(@Nullable PathType pathType) {
            this.pathType = pathType;
            return this;
        }

        public Properties adjacentPathType(@Nullable PathType adjacentPathType) {
            this.adjacentPathType = adjacentPathType;
            return this;
        }

        public Properties sound(SoundAction action, SoundEvent sound) {
            this.sounds.put(action, sound);
            return this;
        }

        public Properties canHydrate(boolean canHydrate) {
            this.canHydrate = canHydrate;
            return this;
        }

        public Properties lightLevel(int lightLevel) {
            if (lightLevel < 0 || lightLevel > 15) {
                throw new IllegalArgumentException("Fluid light level must be within 0..15, got " + lightLevel);
            }
            this.lightLevel = lightLevel;
            return this;
        }

        public Properties density(int density) {
            this.density = density;
            return this;
        }

        public Properties temperature(int temperature) {
            this.temperature = temperature;
            return this;
        }

        public Properties viscosity(int viscosity) {
            if (viscosity < 0) {
                throw new IllegalArgumentException("Fluid viscosity must not be negative, got " + viscosity);
            }
            this.viscosity = viscosity;
            return this;
        }

        public Properties rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }
    }
}
