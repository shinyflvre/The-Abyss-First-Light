package net.yezon.theabyss.gravity;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.attachment.AttachmentSyncHandler;
import net.yezon.theabyss.platform.attachment.AttachmentType;
import net.yezon.theabyss.platform.attachment.IAttachmentHolder;
import net.yezon.theabyss.platform.event.entity.EntityJoinLevelEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.platform.event.tick.EntityTickEvent;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.platform.registries.NeoForgeRegistries;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.block.ChiseledGravityStoneBlock;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class Gravity {
    public static final double RANGE = 4.0;
    private static final double TOUCH = 0.12;
    private static final double BRIDGE = 3.0;
    private static final double BRIDGE_SINK = 0.25;
    private static final int[][] COLS = {
        {1, 0, 0, 0, 1, 0, 0, 0, 1},
        {-1, 0, 0, 0, -1, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 1, 0, -1, 0},
        {1, 0, 0, 0, 0, -1, 0, 1, 0},
        {0, -1, 0, 1, 0, 0, 0, 0, 1},
        {0, 1, 0, -1, 0, 0, 0, 0, 1}
    };
    private static final List<Vec3> OFFSETS = offsets();
    private static final Set<EntityType<?>> NO_FRAME = ConcurrentHashMap.newKeySet();

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TheabyssMod.MODID);
    public static final Supplier<AttachmentType<Direction>> TYPE = ATTACHMENTS.register(
        "gravity", () -> AttachmentType.builder(() -> Direction.DOWN)
            .serialize(Direction.CODEC.fieldOf("down"), d -> d != Direction.DOWN)
            .sync(new Sync())
            .build()
    );

    public static ClientHooks client = new ClientHooks();

    private Gravity() {
    }

    public static class ClientHooks {
        public void received(Entity entity, Direction to) {
            setRaw(entity, to);
            entity.setPos(entity.position());
        }

        public void switched(LivingEntity entity, Direction from, Direction to, Vec3 oldEye, Vec3 pivot) {
        }
    }

    private static final class Sync implements AttachmentSyncHandler<Direction> {
        @Override
        public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
            return holder != to;
        }

        @Override
        public void write(RegistryFriendlyByteBuf buf, Direction attachment, boolean initialSync) {
            buf.writeByte(attachment.get3DDataValue());
        }

        @Override
        public Direction read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable Direction previousValue) {
            Direction to = Direction.from3DDataValue(buf.readByte());
            if (holder instanceof Entity entity) {
                client.received(entity, to);
            }
            return to;
        }
    }

    public static Direction of(Entity entity) {
        return Direction.from3DDataValue(((GravityEntity)entity).theabyss$gravityId());
    }

    public static boolean active(Entity entity) {
        return ((GravityEntity)entity).theabyss$gravityId() != 0;
    }

    public static void setRaw(Entity entity, Direction gravity) {
        ((GravityEntity)entity).theabyss$setGravityId(gravity.get3DDataValue());
    }

    public static Vec3 toWorld(Vec3 v, Direction gravity) {
        if (gravity == Direction.DOWN) {
            return v;
        }
        int[] c = COLS[gravity.get3DDataValue()];
        return new Vec3(
            v.x * c[0] + v.y * c[3] + v.z * c[6],
            v.x * c[1] + v.y * c[4] + v.z * c[7],
            v.x * c[2] + v.y * c[5] + v.z * c[8]
        );
    }

    public static Vec3 toLocal(Vec3 v, Direction gravity) {
        if (gravity == Direction.DOWN) {
            return v;
        }
        int[] c = COLS[gravity.get3DDataValue()];
        return new Vec3(
            v.x * c[0] + v.y * c[1] + v.z * c[2],
            v.x * c[3] + v.y * c[4] + v.z * c[5],
            v.x * c[6] + v.y * c[7] + v.z * c[8]
        );
    }

    public static Quaternionf frame(Direction gravity) {
        return switch (gravity) {
            case DOWN -> new Quaternionf();
            case UP -> new Quaternionf().rotationZ((float)Math.PI);
            case EAST -> new Quaternionf().rotationZ((float)(Math.PI / 2.0));
            case WEST -> new Quaternionf().rotationZ((float)(-Math.PI / 2.0));
            case SOUTH -> new Quaternionf().rotationX((float)(-Math.PI / 2.0));
            case NORTH -> new Quaternionf().rotationX((float)(Math.PI / 2.0));
        };
    }

    public static Vec3 up(Direction gravity) {
        return new Vec3(-gravity.getStepX(), -gravity.getStepY(), -gravity.getStepZ());
    }

    public static AABB box(Vec3 feet, Direction gravity, double width, double height) {
        double r = width * 0.5;
        double x0 = feet.x - r, x1 = feet.x + r, y0 = feet.y - r, y1 = feet.y + r, z0 = feet.z - r, z1 = feet.z + r;
        switch (gravity) {
            case DOWN -> {
                y0 = feet.y;
                y1 = feet.y + height;
            }
            case UP -> {
                y0 = feet.y - height;
                y1 = feet.y;
            }
            case WEST -> {
                x0 = feet.x;
                x1 = feet.x + height;
            }
            case EAST -> {
                x0 = feet.x - height;
                x1 = feet.x;
            }
            case NORTH -> {
                z0 = feet.z;
                z1 = feet.z + height;
            }
            case SOUTH -> {
                z0 = feet.z - height;
                z1 = feet.z;
            }
        }
        return new AABB(x0, y0, z0, x1, y1, z1);
    }

    public static boolean canFeel(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive() || entity.isSpectator() || entity.isNoGravity()) {
            return false;
        }
        if (entity.isPassenger() || entity.isVehicle() || living.isFallFlying() || living.isSleeping() || entity.isInWater() || entity.isInLava()) {
            return false;
        }
        if (entity instanceof Player player && player.getAbilities().flying) {
            return false;
        }
        return entity.getBbWidth() <= 3.0F && entity.getBbHeight() <= 4.5F && !NO_FRAME.contains(entity.getType());
    }

    public static void tick(LivingEntity entity, boolean detach, boolean mayAttach) {
        Level level = entity.level();
        Direction gravity = of(entity);
        if (gravity == Direction.DOWN && !GravityIndex.any(level)) {
            return;
        }
        if (!canFeel(entity)) {
            if (gravity != Direction.DOWN) {
                release(entity);
            }
            return;
        }
        if (gravity != Direction.DOWN && !level.isClientSide() && entity.tickCount - ((GravityEntity)entity).theabyss$lastTravel() > 3
            && !(entity instanceof Player)) {
            if (!(entity instanceof Mob mob) || !mob.isNoAi()) {
                NO_FRAME.add(entity.getType());
            }
            release(entity);
            return;
        }
        if (detach && gravity != Direction.DOWN) {
            release(entity);
            return;
        }
        AABB box = entity.getBoundingBox();
        LongArrayList near = new LongArrayList();
        GravityIndex.collect(level, box.inflate(RANGE + 1.5), near);
        if (near.isEmpty()) {
            if (gravity != Direction.DOWN) {
                release(entity);
            }
            return;
        }
        int blocked = ((GravityEntity)entity).theabyss$blocked();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean stay = gravity == Direction.DOWN;
        boolean bridge = false;
        boolean airborne = !entity.onGround();
        Direction touch = null;
        double touchPlane = 0.0;
        double touchArea = 0.0;
        for (int i = 0; i < near.size(); i++) {
            pos.set(near.getLong(i));
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof ChiseledGravityStoneBlock)) {
                continue;
            }
            Direction facing = state.getValue(ChiseledGravityStoneBlock.FACING);
            if (ChiseledGravityStoneBlock.covered(level, pos, facing)) {
                continue;
            }
            Direction pull = facing.getOpposite();
            Direction.Axis axis = facing.getAxis();
            double plane = plane(pos, facing);
            double feet = pull.getAxisDirection() == Direction.AxisDirection.POSITIVE ? box.max(axis) : box.min(axis);
            double out = (feet - plane) * facing.getAxisDirection().getStep();
            double area = overlap(box, pos, axis);
            if (area <= 0.0) {
                if (pull == gravity && airborne && !bridge && out >= -BRIDGE_SINK && out <= RANGE && gap(box, pos, axis) <= BRIDGE) {
                    bridge = true;
                }
                continue;
            }
            if (pull == gravity) {
                if (out >= -0.6 && out <= RANGE) {
                    stay = true;
                }
            } else if (mayAttach && (blocked & (1 << pull.get3DDataValue())) != 0 && Math.abs(out) <= TOUCH && area > touchArea) {
                touch = pull;
                touchPlane = plane;
                touchArea = area;
            }
        }
        if (touch != null && touchArea > 0.01 && switchTo(entity, touch, touchPlane, null, null)) {
            return;
        }
        if (!stay && !wrap(entity, gravity, near) && !bridge) {
            release(entity);
        }
    }

    private static boolean wrap(LivingEntity entity, Direction gravity, LongArrayList near) {
        if (gravity == Direction.DOWN) {
            return false;
        }
        Level level = entity.level();
        AABB box = entity.getBoundingBox();
        Direction.Axis gAxis = gravity.getAxis();
        int gStep = gravity.getAxisDirection().getStep();
        double feet = gStep > 0 ? box.max(gAxis) : box.min(gAxis);
        Vec3 centre = box.getCenter();
        Vec3 motion = entity.getDeltaMovement();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Direction best = null;
        double bestPlane = 0.0;
        double bestDepth = 0.0;
        double bestScore = Double.MAX_VALUE;
        for (int i = 0; i < near.size(); i++) {
            pos.set(near.getLong(i));
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof ChiseledGravityStoneBlock)) {
                continue;
            }
            Direction facing = state.getValue(ChiseledGravityStoneBlock.FACING);
            if (facing.getAxis() == gAxis || ChiseledGravityStoneBlock.covered(level, pos, facing)) {
                continue;
            }
            if (motion.x * facing.getStepX() + motion.y * facing.getStepY() + motion.z * facing.getStepZ() < 0.003) {
                continue;
            }
            Direction.Axis mAxis = facing.getAxis();
            double plane = plane(pos, facing);
            double beyond = (centre.get(mAxis) - plane) * facing.getAxisDirection().getStep();
            if (beyond < -1.6 || beyond > 0.9) {
                continue;
            }
            double s0 = (pos.get(gAxis) - feet) * gStep;
            double s1 = (pos.get(gAxis) + 1 - feet) * gStep;
            double depth = Math.min(s0, s1);
            if (depth < -0.6 || depth > 1.1) {
                continue;
            }
            Direction.Axis tAxis = third(gAxis, mAxis);
            double side = Math.min(box.max(tAxis), pos.get(tAxis) + 1.0) - Math.max(box.min(tAxis), pos.get(tAxis));
            if (side <= 0.05) {
                continue;
            }
            double score = Math.abs(beyond) + Math.max(0.0, depth);
            if (score < bestScore) {
                bestScore = score;
                best = facing;
                bestPlane = plane;
                bestDepth = depth;
            }
        }
        if (best == null) {
            return false;
        }
        Direction to = best.getOpposite();
        Direction.Axis mAxis = best.getAxis();
        double inset = Math.max(entity.getBbWidth() * 0.5 + 0.02, bestDepth + 0.25);
        Vec3 target = with(with(centre, mAxis, bestPlane), gAxis, feet + inset * gStep);
        Vec3 pivot = with(with(centre, mAxis, bestPlane), gAxis, feet);
        return switchTo(entity, to, bestPlane, target, pivot);
    }

    public static boolean steer(Mob mob) {
        GravityEntity body = (GravityEntity)mob;
        if (body.theabyss$gravityId() == 0) {
            return false;
        }
        Direction gravity = of(mob);
        body.theabyss$setSteer(0.0, 0.0, 0.0, false);
        Vec3 goal = null;
        LivingEntity target = mob.getTarget();
        if (target != null && target.isAlive() && target.level() == mob.level()) {
            goal = target.position();
        } else if (!mob.getNavigation().isDone() && mob.getNavigation().getTargetPos() != null) {
            goal = Vec3.atBottomCenterOf(mob.getNavigation().getTargetPos());
        } else if (mob.getMoveControl().hasWanted()) {
            goal = new Vec3(mob.getMoveControl().getWantedX(), mob.getMoveControl().getWantedY(), mob.getMoveControl().getWantedZ());
        }
        if (goal == null) {
            mob.setZza(0.0F);
            return true;
        }
        Vec3 delta = with(goal.subtract(mob.position()), gravity.getAxis(), 0.0);
        double length = delta.length();
        if (length < 0.3) {
            mob.setZza(0.0F);
            return true;
        }
        Vec3 dir = delta.scale(1.0 / length);
        body.theabyss$setSteer(dir.x, dir.y, dir.z, true);
        double modifier = mob.getMoveControl().hasWanted() ? mob.getMoveControl().getSpeedModifier() : 1.0;
        mob.setSpeed((float)(modifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
        Vec3 facing = gravity == Direction.UP ? dir : toLocal(dir, gravity);
        float yaw = (float)(Mth.atan2(facing.z, facing.x) * Mth.RAD_TO_DEG) - 90.0F;
        mob.setYRot(Mth.approachDegrees(mob.getYRot(), yaw, 90.0F));
        if (mob.horizontalCollision && mob.onGround()) {
            mob.getJumpControl().jump();
        }
        return true;
    }

    public static void release(LivingEntity entity) {
        if (of(entity) != Direction.DOWN) {
            switchTo(entity, Direction.DOWN, Double.NaN, null, null);
            entity.setOnGround(false);
        }
    }

    public static boolean switchTo(LivingEntity entity, Direction to, double plane, @Nullable Vec3 hint, @Nullable Vec3 pivot) {
        Direction from = of(entity);
        if (from == to) {
            return true;
        }
        Level level = entity.level();
        AABB old = entity.getBoundingBox();
        EntityDimensions dims = entity.getDimensions(entity.getPose());
        double width = dims.width();
        double height = dims.height();
        Vec3 centre = old.getCenter();
        boolean onPlane = !Double.isNaN(plane);
        Vec3 base;
        if (hint != null) {
            base = hint;
        } else if (onPlane) {
            base = with(centre, to.getAxis(), plane);
        } else {
            base = centre.add(to.getStepX() * height * 0.5, to.getStepY() * height * 0.5, to.getStepZ() * height * 0.5);
        }
        Vec3 feet = null;
        for (Vec3 offset : OFFSETS) {
            Vec3 shift = onPlane ? with(offset, to.getAxis(), 0.0) : offset;
            Vec3 candidate = base.add(shift);
            if (level.noCollision(entity, box(candidate, to, width, height).deflate(1.0E-7))) {
                feet = candidate;
                break;
            }
        }
        if (feet == null) {
            if (onPlane) {
                return false;
            }
            feet = base;
        }
        Vec3 oldEye = entity.getEyePosition();
        if (pivot == null) {
            pivot = from.getAxis() == to.getAxis() ? centre : with(entity.position(), to.getAxis(), feet.get(to.getAxis()));
        }
        setRaw(entity, to);
        ((GravityEntity)entity).theabyss$setSteer(0.0, 0.0, 0.0, false);
        entity.setPos(feet);
        entity.resetFallDistance();
        if (!level.isClientSide()) {
            net.yezon.theabyss.platform.attachment.Attachments.setData(entity, TYPE, to);
            if (!(entity instanceof Player)) {
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BREEZE_JUMP, entity.getSoundSource(), 0.3F,
                    0.55F + entity.getRandom().nextFloat() * 0.1F);
            }
        } else {
            client.switched(entity, from, to, oldEye, pivot);
        }
        return true;
    }

    public static void acceptPlayer(ServerPlayer player, Direction to, Vec3 feet) {
        Direction from = of(player);
        setRaw(player, to);
        player.setPos(feet);
        player.connection.resetPosition();
        player.resetFallDistance();
        net.yezon.theabyss.platform.attachment.Attachments.setData(player, TYPE, to);
        if (from != to) {
            player.level().playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BREEZE_JUMP, player.getSoundSource(), 0.3F, 0.6F);
        }
    }

    public static boolean fieldNear(Level level, Vec3 feet, Direction gravity) {
        LongArrayList near = new LongArrayList();
        GravityIndex.collect(level, new AABB(feet, feet).inflate(RANGE + 3.0), near);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < near.size(); i++) {
            pos.set(near.getLong(i));
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof ChiseledGravityStoneBlock && state.getValue(ChiseledGravityStoneBlock.FACING) == gravity.getOpposite()) {
                return true;
            }
        }
        return false;
    }

    private static double plane(BlockPos pos, Direction facing) {
        Direction.Axis axis = facing.getAxis();
        return facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? pos.get(axis) + 1.0 : pos.get(axis);
    }

    private static double overlap(AABB box, BlockPos pos, Direction.Axis normal) {
        double area = 1.0;
        for (Direction.Axis axis : Direction.Axis.values()) {
            if (axis == normal) {
                continue;
            }
            double o = Math.min(box.max(axis), pos.get(axis) + 1.0) - Math.max(box.min(axis), pos.get(axis));
            if (o <= 0.0) {
                return 0.0;
            }
            area *= o;
        }
        return area;
    }

    private static double gap(AABB box, BlockPos pos, Direction.Axis normal) {
        double gap = 0.0;
        for (Direction.Axis axis : Direction.Axis.values()) {
            if (axis == normal) {
                continue;
            }
            gap = Math.max(gap, Math.max(pos.get(axis) - box.max(axis), box.min(axis) - (pos.get(axis) + 1.0)));
        }
        return gap;
    }

    private static Direction.Axis third(Direction.Axis a, Direction.Axis b) {
        for (Direction.Axis axis : Direction.Axis.values()) {
            if (axis != a && axis != b) {
                return axis;
            }
        }
        return a;
    }

    public static Vec3 with(Vec3 v, Direction.Axis axis, double value) {
        return switch (axis) {
            case X -> new Vec3(value, v.y, v.z);
            case Y -> new Vec3(v.x, value, v.z);
            case Z -> new Vec3(v.x, v.y, value);
        };
    }

    private static List<Vec3> offsets() {
        List<Vec3> list = new ArrayList<>();
        list.add(Vec3.ZERO);
        for (double step : new double[] {0.15, 0.3, 0.5, 0.75, 1.0}) {
            list.add(new Vec3(0.0, step, 0.0));
            list.add(new Vec3(0.0, -step, 0.0));
            list.add(new Vec3(step, 0.0, 0.0));
            list.add(new Vec3(-step, 0.0, 0.0));
            list.add(new Vec3(0.0, 0.0, step));
            list.add(new Vec3(0.0, 0.0, -step));
        }
        return List.copyOf(list);
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide() || entity instanceof Player || !(entity instanceof LivingEntity living)) {
            return;
        }
        if (!active(entity) && !GravityIndex.any(entity.level())) {
            return;
        }
        tick(living, false, true);
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity && !(entity instanceof Player) && net.yezon.theabyss.platform.attachment.Attachments.hasData(entity, TYPE)) {
            Direction stored = net.yezon.theabyss.platform.attachment.Attachments.getData(entity, TYPE);
            if (stored != of(entity)) {
                setRaw(entity, stored);
                entity.setPos(entity.position());
            }
        }
        if (!event.loadedFromDisk() && entity instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity owner && active(owner)) {
            Vec3 eye = owner.getEyePosition();
            Direction gravity = of(owner);
            projectile.setPos(eye.x + gravity.getStepX() * 0.1, eye.y + gravity.getStepY() * 0.1, eye.z + gravity.getStepZ() * 0.1);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && active(player)) {
            release(player);
        }
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && active(player)) {
            setRaw(player, Direction.DOWN);
            net.yezon.theabyss.platform.attachment.Attachments.setData(player, TYPE, Direction.DOWN);
            player.setPos(player.position());
        }
    }
}
