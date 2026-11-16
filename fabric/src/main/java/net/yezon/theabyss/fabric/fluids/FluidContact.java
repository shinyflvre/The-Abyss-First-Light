package net.yezon.theabyss.fabric.fluids;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.fabric.mixin.fluids.FluidEntityAccessor;
import net.yezon.theabyss.fabric.mixin.fluids.FluidInteractionAccessor;
import net.yezon.theabyss.fabric.mixin.fluids.FluidTrackerAccessor;
import net.yezon.theabyss.platform.fluids.BaseFlowingFluid;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.jspecify.annotations.Nullable;

public final class FluidContact {
    private static final Predicate<FluidType> ANY = type -> true;

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

    private static List<?> trackers(Entity entity) {
        return ((FluidInteractionAccessor)((FluidEntityAccessor)entity).theabyss$fluidInteraction()).theabyss$fluidTrackers();
    }

    public static boolean touching(Entity entity) {
        return height(entity, ANY) > 0.0;
    }

    public static double height(Entity entity, Predicate<FluidType> filter) {
        double best = 0.0;
        for (Object element : trackers(entity)) {
            FluidTrackerAccessor tracker = (FluidTrackerAccessor)element;
            double height = tracker.theabyss$height();
            if (height > best) {
                FluidType type = typeOf(tracker.theabyss$fluidType().value());
                if (type != null && filter.test(type)) {
                    best = height;
                }
            }
        }
        return best;
    }

    public static double height(Entity entity, FluidType wanted) {
        return height(entity, type -> type == wanted);
    }

    public static boolean eyeIn(Entity entity, Predicate<FluidType> filter) {
        for (Object element : trackers(entity)) {
            FluidTrackerAccessor tracker = (FluidTrackerAccessor)element;
            if (tracker.theabyss$eyesInside()) {
                FluidType type = typeOf(tracker.theabyss$fluidType().value());
                if (type != null && filter.test(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static double highestIfOwn(Entity entity) {
        double best = 0.0;
        FluidType bestType = null;
        for (Object element : trackers(entity)) {
            FluidTrackerAccessor tracker = (FluidTrackerAccessor)element;
            double height = tracker.theabyss$height();
            if (height > best) {
                best = height;
                bestType = typeOf(tracker.theabyss$fluidType().value());
            }
        }
        return bestType != null ? best : 0.0;
    }

    public static boolean lavaOnlyFromOwnFluids(Entity entity) {
        boolean own = false;
        for (Object element : trackers(entity)) {
            FluidTrackerAccessor tracker = (FluidTrackerAccessor)element;
            if (tracker.theabyss$height() > 0.0) {
                Holder<Fluid> holder = tracker.theabyss$fluidType();
                if (holder.is(FluidTags.LAVA)) {
                    if (typeOf(holder.value()) == null) {
                        return false;
                    }
                    own = true;
                }
            }
        }
        return own;
    }

    public static float fallDistanceModifier(Entity entity) {
        float lowest = 1.0F;
        for (Object element : trackers(entity)) {
            FluidTrackerAccessor tracker = (FluidTrackerAccessor)element;
            if (tracker.theabyss$height() > 0.0) {
                FluidType type = typeOf(tracker.theabyss$fluidType().value());
                if (type != null && type.getFallDistanceModifier() < lowest) {
                    lowest = type.getFallDistanceModifier();
                }
            }
        }
        return lowest;
    }

    public static boolean move(LivingEntity entity, Vec3 input, double gravity) {
        List<FluidType> candidates = null;
        for (Object element : trackers(entity)) {
            FluidTrackerAccessor tracker = (FluidTrackerAccessor)element;
            if (tracker.theabyss$height() > 0.0) {
                FluidType type = typeOf(tracker.theabyss$fluidType().value());
                if (type != null) {
                    if (candidates == null) {
                        candidates = new ArrayList<>(2);
                    }
                    candidates.add(type);
                }
            }
        }
        if (candidates == null) {
            return false;
        }
        for (FluidType type : candidates) {
            if (type.move(entity, input, gravity)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canStartSwimming(Entity entity) {
        if (!swimmable(entity, entity.level().getFluidState(entity.blockPosition()))) {
            return false;
        }
        for (Object element : trackers(entity)) {
            FluidTrackerAccessor tracker = (FluidTrackerAccessor)element;
            if (tracker.theabyss$eyesInside() && swimmable(entity, tracker.theabyss$fluidType())) {
                return true;
            }
        }
        return false;
    }

    private static boolean swimmable(Entity entity, FluidState state) {
        return !state.isEmpty() && swimmable(entity, state.typeHolder());
    }

    private static boolean swimmable(Entity entity, Holder<Fluid> fluid) {
        FluidType type = typeOf(fluid.value());
        if (type != null) {
            return type.canSwim();
        }
        if (fluid.is(FluidTags.WATER)) {
            return !(entity instanceof LivingEntity living) || !living.isSensitiveToWater();
        }
        return false;
    }

    public static void pushByCurrents(Entity entity) {
        if (!entity.isPushedByFluid() || height(entity, FluidType::canPushEntity) <= 0.0) {
            return;
        }
        AABB box = entity.getFluidInteractionBox();
        if (box == null) {
            return;
        }
        int x0 = Mth.floor(box.minX);
        int y0 = Mth.floor(box.minY);
        int z0 = Mth.floor(box.minZ);
        int x1 = Mth.ceil(box.maxX) - 1;
        int y1 = Mth.ceil(box.maxY) - 1;
        int z1 = Mth.ceil(box.maxZ) - 1;
        double entityY = entity.getBoundingBox().minY;
        Level level = entity.level();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Map<Fluid, double[]> heights = new IdentityHashMap<>(4);
        Map<FluidType, Current> currents = new IdentityHashMap<>(2);
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    pos.set(x, y, z);
                    FluidState state = level.getFluidState(pos);
                    FluidType type = typeOf(state);
                    if (type == null || !type.canPushEntity()) {
                        continue;
                    }
                    double top = y + state.getHeight(level, pos);
                    if (top < box.minY) {
                        continue;
                    }
                    double[] fluidHeight = heights.computeIfAbsent(state.getType(), fluid -> new double[1]);
                    fluidHeight[0] = Math.max(top - entityY, fluidHeight[0]);
                    Current current = currents.computeIfAbsent(type, key -> new Current());
                    current.height = Math.max(fluidHeight[0], current.height);
                    Vec3 flow = state.getFlow(level, pos);
                    if (current.height < 0.4) {
                        flow = flow.scale(current.height);
                    }
                    current.sum = current.sum.add(flow);
                    current.count++;
                }
            }
        }
        for (Map.Entry<FluidType, Current> entry : currents.entrySet()) {
            if (entry.getValue().height > 0.0) {
                entry.getValue().push(entity, entry.getKey().motionScale());
            }
        }
    }

    private static final class Current {
        private double height;
        private Vec3 sum = Vec3.ZERO;
        private int count;

        private void push(Entity entity, double scale) {
            if (this.count == 0 || this.sum.lengthSqr() < 1.0E-5F) {
                return;
            }
            Vec3 impulse = entity instanceof Player ? this.sum.scale(1.0 / this.count) : this.sum.normalize();
            impulse = impulse.scale(scale);
            Vec3 motion = entity.getDeltaMovement();
            if (Math.abs(motion.x) < 0.003 && Math.abs(motion.z) < 0.003 && impulse.length() < 0.0045000000000000005) {
                impulse = impulse.normalize().scale(0.0045000000000000005);
            }
            entity.addDeltaMovement(impulse);
        }
    }
}
