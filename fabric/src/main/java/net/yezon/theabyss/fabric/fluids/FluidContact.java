package net.yezon.theabyss.fabric.fluids;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.fluids.BaseFlowingFluid;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.jspecify.annotations.Nullable;

public final class FluidContact {
    private FluidContact() {
    }

    public static @Nullable FluidType typeOf(Fluid fluid) {
        return fluid instanceof BaseFlowingFluid flowing ? flowing.abyssType() : null;
    }

    public static @Nullable FluidType typeOf(FluidState state) {
        return state.isEmpty() ? null : typeOf(state.getType());
    }

    public static boolean hydrates(FluidState state) {
        FluidType type = typeOf(state);
        return type != null && type.canHydrate();
    }

    public static boolean carriesBoats(FluidState state) {
        FluidType type = typeOf(state);
        return type != null && type.supportsBoating();
    }

    private static Object2DoubleArrayMap<FluidType> heights(Entity entity) {
        return ((OwnFluidEntity)entity).theabyss$fluidHeights();
    }

    public static @Nullable FluidType eye(Entity entity) {
        return ((OwnFluidEntity)entity).theabyss$eyeFluid();
    }

    public static boolean touching(Entity entity) {
        return !heights(entity).isEmpty();
    }

    public static boolean present(Entity entity, Predicate<FluidType> filter) {
        for (Object2DoubleMap.Entry<FluidType> entry : heights(entity).object2DoubleEntrySet()) {
            if (filter.test(entry.getKey())) {
                return true;
            }
        }
        return false;
    }

    public static double height(Entity entity, Predicate<FluidType> filter) {
        double best = 0.0;
        for (Object2DoubleMap.Entry<FluidType> entry : heights(entity).object2DoubleEntrySet()) {
            if (entry.getDoubleValue() > best && filter.test(entry.getKey())) {
                best = entry.getDoubleValue();
            }
        }
        return best;
    }

    public static double height(Entity entity, FluidType type) {
        return heights(entity).getDouble(type);
    }

    public static double highest(Entity entity) {
        return height(entity, type -> true);
    }

    public static boolean dominates(Entity entity, double minimum) {
        double own = highest(entity);
        return own > minimum && own >= entity.getFluidHeight(FluidTags.WATER) && own >= entity.getFluidHeight(FluidTags.LAVA);
    }

    public static boolean eyeIn(Entity entity, Predicate<FluidType> filter) {
        FluidType type = eye(entity);
        return type != null && filter.test(type);
    }

    public static boolean canStartSwimming(Entity entity) {
        FluidState feet = entity.level().getFluidState(entity.blockPosition());
        FluidType feetType = typeOf(feet);
        FluidType eyeType = eye(entity);
        if (eyeType != null) {
            return eyeType.canSwim() && (feetType != null ? feetType.canSwim() : feet.is(FluidTags.WATER));
        }
        return feetType != null && feetType.canSwim() && entity.isUnderWater();
    }

    public static void clearEye(Entity entity) {
        ((OwnFluidEntity)entity).theabyss$setEyeFluid(null);
    }

    public static void updateEye(Entity entity, FluidState state) {
        ((OwnFluidEntity)entity).theabyss$setEyeFluid(typeOf(state));
    }

    public static void update(Entity entity) {
        Object2DoubleArrayMap<FluidType> heights = heights(entity);
        heights.clear();
        if (entity.getVehicle() instanceof Boat boat && !boat.isUnderWater()) {
            return;
        }
        if (entity.touchingUnloadedChunk()) {
            return;
        }
        AABB box = entity.getBoundingBox().deflate(0.001);
        int x0 = Mth.floor(box.minX);
        int x1 = Mth.ceil(box.maxX);
        int y0 = Mth.floor(box.minY);
        int y1 = Mth.ceil(box.maxY);
        int z0 = Mth.floor(box.minZ);
        int z1 = Mth.ceil(box.maxZ);
        Level level = entity.level();
        boolean pushed = entity.isPushedByFluid();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Map<FluidType, Current> currents = null;
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                for (int z = z0; z < z1; z++) {
                    pos.set(x, y, z);
                    FluidState state = level.getFluidState(pos);
                    FluidType type = typeOf(state);
                    if (type == null) {
                        continue;
                    }
                    double top = y + state.getHeight(level, pos);
                    if (top < box.minY) {
                        continue;
                    }
                    if (currents == null) {
                        currents = new Object2ObjectArrayMap<>();
                    }
                    Current current = currents.computeIfAbsent(type, key -> new Current());
                    current.height = Math.max(top - box.minY, current.height);
                    if (pushed && type.canPushEntity()) {
                        Vec3 flow = state.getFlow(level, pos);
                        if (current.height < 0.4) {
                            flow = flow.scale(current.height);
                        }
                        current.flow = current.flow.add(flow);
                        current.count++;
                    }
                }
            }
        }
        if (currents == null) {
            return;
        }
        for (Map.Entry<FluidType, Current> entry : currents.entrySet()) {
            entry.getValue().push(entity, entry.getKey().motionScale());
            heights.put(entry.getKey(), entry.getValue().height);
        }
    }

    public static void afterUpdate(Entity entity) {
        Object2DoubleArrayMap<FluidType> heights = heights(entity);
        if (heights.isEmpty() || entity.getVehicle() instanceof Boat) {
            return;
        }
        float modifier = Float.MAX_VALUE;
        boolean extinguish = false;
        for (FluidType type : heights.keySet()) {
            modifier = Math.min(modifier, type.getFallDistanceModifier());
            extinguish |= type.canExtinguish();
        }
        entity.fallDistance *= modifier;
        if (extinguish) {
            entity.clearFire();
        }
    }

    private static final class Current {
        private double height;
        private Vec3 flow = Vec3.ZERO;
        private int count;

        private void push(Entity entity, double scale) {
            if (!(this.flow.length() > 0.0)) {
                return;
            }
            Vec3 impulse = this.flow;
            if (this.count > 0) {
                impulse = impulse.scale(1.0 / this.count);
            }
            if (!(entity instanceof Player)) {
                impulse = impulse.normalize();
            }
            Vec3 motion = entity.getDeltaMovement();
            impulse = impulse.scale(scale);
            if (Math.abs(motion.x) < 0.003 && Math.abs(motion.z) < 0.003 && impulse.length() < 0.0045000000000000005) {
                impulse = impulse.normalize().scale(0.0045000000000000005);
            }
            entity.setDeltaMovement(entity.getDeltaMovement().add(impulse));
        }
    }
}
