package net.yezon.theabyss.client.gravity;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.fml.event.lifecycle.FMLClientSetupEvent;
import net.yezon.theabyss.platform.client.event.ClientPlayerNetworkEvent;
import net.yezon.theabyss.platform.client.event.RenderLivingEvent;
import net.yezon.theabyss.platform.client.network.ClientPacketDistributor;
import net.yezon.theabyss.platform.client.renderstate.RegisterRenderStateModifiersEvent;
import net.yezon.theabyss.platform.event.tick.EntityTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.gravity.Gravity;
import net.yezon.theabyss.gravity.GravityIndex;
import net.yezon.theabyss.gravity.GravityMoteOptions;
import net.yezon.theabyss.network.GravityMessage;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class GravityClient {
    public static final ContextKey<Quaternionf> POSE = new ContextKey<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "gravity_pose"));
    private static final Quaternionfc IDENTITY = new Quaternionf();
    private static final long TURN_NANOS = 380_000_000L;
    private static final int DETACH_COOLDOWN = 10;
    private static final Int2ObjectOpenHashMap<Turn> TURNS = new Int2ObjectOpenHashMap<>();
    private static int cooldown;

    private GravityClient() {
    }

    private record Turn(Quaternionf pre, long start, @Nullable Vec3 pivot, @Nullable Vec3 residual) {
        float progress() {
            return Math.min(1.0F, (System.nanoTime() - this.start) / (float)TURN_NANOS);
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        Gravity.client = new Hooks();
    }

    private static final class Hooks extends Gravity.ClientHooks {
        @Override
        public void received(Entity entity, Direction to) {
            Minecraft mc = Minecraft.getInstance();
            if (!mc.isSameThread()) {
                mc.execute(() -> this.received(entity, to));
                return;
            }
            if (entity == mc.player) {
                return;
            }
            Direction from = Gravity.of(entity);
            if (from == to) {
                return;
            }
            if (entity instanceof LivingEntity living && entity.tickCount > 1) {
                Quaternionf before = frameAt(living, living.yBodyRot);
                if (before == null) {
                    before = new Quaternionf();
                }
                Gravity.setRaw(entity, to);
                entity.setPos(entity.position());
                Quaternionf target = baseFrame(living, to, living.yBodyRot);
                TURNS.put(entity.getId(), new Turn(before.mul(target.conjugate(new Quaternionf())), System.nanoTime(), null, null));
            } else {
                super.received(entity, to);
            }
        }

        @Override
        public void switched(LivingEntity entity, Direction from, Direction to, Vec3 oldEye, Vec3 pivot) {
            Minecraft mc = Minecraft.getInstance();
            if (entity != mc.player) {
                return;
            }
            LocalPlayer player = mc.player;
            Quaternionf fromFrame = Gravity.frame(from);
            Quaternionf toFrame = Gravity.frame(to);
            float yaw = player.getYRot() * Mth.DEG_TO_RAD;
            Quaternionf turn;
            if (from.getAxis() == to.getAxis()) {
                Vector3f forward = fromFrame.transform(new Vector3f(-Mth.sin(yaw), 0.0F, Mth.cos(yaw)));
                turn = new Quaternionf().fromAxisAngleRad(forward.normalize(), (float)Math.PI);
            } else {
                Vector3f axis = new Vector3f(from.getStepX(), from.getStepY(), from.getStepZ()).cross(to.getStepX(), to.getStepY(), to.getStepZ());
                turn = new Quaternionf().fromAxisAngleRad(axis.normalize(), (float)(Math.PI / 2.0));
            }
            Quaternionf relative = new Quaternionf(toFrame).conjugate().mul(turn).mul(fromFrame);
            Vector3f look = relative.transform(new Vector3f(-Mth.sin(yaw), 0.0F, Mth.cos(yaw)));
            float newYaw = (float)Math.toDegrees(Math.atan2(-look.x, look.z));
            float delta = Mth.wrapDegrees(newYaw - player.getYRot());
            player.setYRot(player.getYRot() + delta);
            player.yRotO += delta;
            player.yHeadRot += delta;
            player.yHeadRotO += delta;
            player.yBodyRot += delta;
            player.yBodyRotO += delta;
            Quaternionf pre = new Quaternionf(turn).conjugate();
            Vec3 newEye = player.getEyePosition();
            Vector3f rel = pre.transform(new Vector3f((float)(newEye.x - pivot.x), (float)(newEye.y - pivot.y), (float)(newEye.z - pivot.z)));
            Vec3 residual = oldEye.subtract(pivot.x + rel.x, pivot.y + rel.y, pivot.z + rel.z);
            TURNS.put(player.getId(), new Turn(pre, System.nanoTime(), pivot, residual));
            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.BREEZE_JUMP, SoundSource.PLAYERS, 0.3F,
                0.6F + player.getRandom().nextFloat() * 0.08F, false);
            burst(player, pivot);
        }
    }

    private static void burst(LocalPlayer player, Vec3 pivot) {
        RandomSource random = player.getRandom();
        GravityMoteOptions mote = new GravityMoteOptions(0);
        for (int i = 0; i < 2; i++) {
            double dx = (random.nextDouble() - 0.5) * 2.4;
            double dy = (random.nextDouble() - 0.5) * 2.4;
            double dz = (random.nextDouble() - 0.5) * 2.4;
            player.level().addParticle(mote, pivot.x + dx, pivot.y + dy, pivot.z + dz, -dx, -dy, -dz);
        }
    }

    public static void corrected(Direction to, Vec3 feet) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Direction from = Gravity.of(player);
        Gravity.setRaw(player, to);
        player.setPos(feet);
        if (from != to) {
            Quaternionf pre = Gravity.frame(from).mul(Gravity.frame(to).conjugate());
            TURNS.put(player.getId(), new Turn(pre, System.nanoTime(), null, null));
        }
    }

    private static float ease(float t) {
        return t < 0.5F ? 4.0F * t * t * t : 1.0F - (float)Math.pow(-2.0F * t + 2.0F, 3.0) / 2.0F;
    }

    private static Quaternionf baseFrame(LivingEntity entity, Direction gravity, float bodyYaw) {
        if (gravity == Direction.DOWN) {
            return new Quaternionf();
        }
        if (entity instanceof Player || gravity != Direction.UP) {
            return Gravity.frame(gravity);
        }
        float yaw = bodyYaw * Mth.DEG_TO_RAD;
        return new Quaternionf().rotationAxis((float)Math.PI, -Mth.sin(yaw), 0.0F, Mth.cos(yaw));
    }

    private static @Nullable Quaternionf frameAt(LivingEntity entity, float bodyYaw) {
        Direction gravity = Gravity.of(entity);
        Turn turn = TURNS.isEmpty() ? null : TURNS.get(entity.getId());
        if (gravity == Direction.DOWN && turn == null) {
            return null;
        }
        Quaternionf base = baseFrame(entity, gravity, bodyYaw);
        if (turn == null) {
            return base;
        }
        float t = turn.progress();
        if (t >= 1.0F) {
            TURNS.remove(entity.getId());
            return gravity == Direction.DOWN ? null : base;
        }
        return new Quaternionf(turn.pre()).slerp(IDENTITY, ease(t)).mul(base);
    }

    public static @Nullable Quaternionf cameraFrame(Entity entity, float partialTick) {
        if (!(entity instanceof LivingEntity living)) {
            return null;
        }
        return frameAt(living, living instanceof Player ? 0.0F : living.getPreciseBodyRotation(partialTick));
    }

    public static Vec3 cameraEye(Entity entity, float partialTick, float eyeHeight) {
        Direction gravity = Gravity.of(entity);
        double x = Mth.lerp(partialTick, entity.xo, entity.getX()) - gravity.getStepX() * eyeHeight;
        double y = Mth.lerp(partialTick, entity.yo, entity.getY()) - gravity.getStepY() * eyeHeight;
        double z = Mth.lerp(partialTick, entity.zo, entity.getZ()) - gravity.getStepZ() * eyeHeight;
        Turn turn = TURNS.isEmpty() ? null : TURNS.get(entity.getId());
        if (turn == null || turn.pivot() == null || turn.residual() == null) {
            return new Vec3(x, y, z);
        }
        float k = ease(turn.progress());
        if (k >= 1.0F) {
            return new Vec3(x, y, z);
        }
        Vec3 pivot = turn.pivot();
        Quaternionf q = new Quaternionf(turn.pre()).slerp(IDENTITY, k);
        Vector3f rel = q.transform(new Vector3f((float)(x - pivot.x), (float)(y - pivot.y), (float)(z - pivot.z)));
        double rest = 1.0 - k;
        return new Vec3(
            pivot.x + rel.x + turn.residual().x * rest,
            pivot.y + rel.y + turn.residual().y * rest,
            pivot.z + rel.z + turn.residual().z * rest
        );
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || event.getEntity() != player) {
            return;
        }
        if (cooldown > 0) {
            cooldown--;
        }
        Direction before = Gravity.of(player);
        if (before == Direction.DOWN && !GravityIndex.any(player.level())) {
            return;
        }
        boolean detach = before != Direction.DOWN && player.input.keyPresses.shift() && player.input.keyPresses.jump();
        Gravity.tick(player, detach, cooldown == 0);
        Direction after = Gravity.of(player);
        if (after != before) {
            if (detach) {
                cooldown = DETACH_COOLDOWN;
            }
            ClientPacketDistributor.sendToServer(new GravityMessage(after.get3DDataValue(), player.position()));
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        TURNS.clear();
        cooldown = 0;
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void registerModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier((Class<EntityRenderer<Entity, EntityRenderState>>)(Class<?>)EntityRenderer.class, (entity, state) -> {
            if (!(entity instanceof LivingEntity living) || !(state instanceof LivingEntityRenderState livingState)) {
                return;
            }
            if (!Gravity.active(living) && (TURNS.isEmpty() || !TURNS.containsKey(living.getId()))) {
                return;
            }
            Quaternionf pose = frameAt(living, livingState.bodyRot);
            if (pose != null) {
                net.yezon.theabyss.platform.client.renderstate.RenderData.set(state, POSE, pose);
            }
            Direction gravity = Gravity.of(living);
            if (!(living instanceof Player) && gravity != Direction.DOWN) {
                if (gravity == Direction.UP) {
                    livingState.xRot = -livingState.xRot;
                } else {
                    livingState.yRot = 0.0F;
                    livingState.xRot = 0.0F;
                }
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLiving(RenderLivingEvent.Pre event) {
        Quaternionf pose = net.yezon.theabyss.platform.client.renderstate.RenderData.get(event.getRenderState(), POSE);
        if (pose == null) {
            return;
        }
        event.getPoseStack().rotate(pose);
        event.getRenderState().shadowPieces.clear();
    }
}
