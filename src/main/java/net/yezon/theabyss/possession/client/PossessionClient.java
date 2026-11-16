package net.yezon.theabyss.possession.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.possession.PossessionData;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class PossessionClient {
    private static final Identifier HEART_CONTAINER = Identifier.withDefaultNamespace("hud/heart/container");
    private static final Identifier HEART_FULL = Identifier.withDefaultNamespace("hud/heart/full");
    private static final Identifier HEART_HALF = Identifier.withDefaultNamespace("hud/heart/half");

    private PossessionClient() {
    }

    public static void renderHostHealth(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui.hud.isHidden() || !(possessedMob(mc) instanceof LivingEntity host)) {
            return;
        }
        int x = graphics.guiWidth() / 2 - 91;
        int y = graphics.guiHeight() - 39;
        int health = Mth.ceil(host.getHealth());
        int hearts = Math.min(30, Mth.ceil(host.getMaxHealth() / 2.0F));
        int rows = Mth.ceil(hearts / 10.0F);
        for (int i = 0; i < hearts; i++) {
            int hx = x + (i % 10) * 8;
            int hy = y - (i / 10) * 10;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_CONTAINER, hx, hy, 9, 9);
            if (i * 2 + 1 < health) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_FULL, hx, hy, 9, 9);
            } else if (i * 2 + 1 == health) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_HALF, hx, hy, 9, 9);
            }
        }
        graphics.text(mc.font, host.getDisplayName(), x, y - (rows - 1) * 10 - 10, 0xFFE0B0FF, true);
    }

    private static Entity possessedMob(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return null;
        }
        PossessionData data = PossessionData.of(player);
        if (!data.active()) {
            return null;
        }
        Entity mob = mc.level.getEntity(data.mobId());
        return mob instanceof LivingEntity && mob.isAlive() ? mob : null;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Entity wanted = possessedMob(mc);
        Entity camera = mc.getCameraEntity();
        if (PossessionCameraFlight.flying()) {
            if (wanted == null) {
                PossessionCameraFlight.stop(mc, mc.player);
            }
            return;
        }
        if (wanted != null && camera != wanted) {
            if (PossessionCameraFlight.shouldStart(wanted)) {
                PossessionCameraFlight.start(mc, wanted);
            } else {
                mc.setCameraEntity(wanted);
            }
        } else if (wanted == null && camera != mc.player && camera != null && isOurOldMob(camera)) {
            mc.setCameraEntity(mc.player);
        }
    }

    private static boolean isOurOldMob(Entity camera) {
        Minecraft mc = Minecraft.getInstance();
        return !mc.player.isSpectator() && camera instanceof LivingEntity && !(camera instanceof Player);
    }

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (PossessionCameraFlight.flying()) {
            event.setCanceled(true);
            event.setSwingHand(false);
            return;
        }
        Entity mob = possessedMob(mc);
        if (mob != null && mc.getCameraEntity() == mob) {
            mc.hitResult = pickFrom(mc.player, mob);
            mc.crosshairPickEntity = mc.hitResult instanceof EntityHitResult entityHit ? entityHit.getEntity() : null;
        }
    }

    private static HitResult pickFrom(LocalPlayer player, Entity camera) {
        double blockRange = player.blockInteractionRange();
        double entityRange = player.entityInteractionRange();
        double maxDistance = Math.max(blockRange, entityRange);
        double maxDistanceSq = Mth.square(maxDistance);
        Vec3 from = camera.getEyePosition(1.0F);
        HitResult blockHit = camera.pick(maxDistance, 1.0F, false);
        double blockDistanceSq = blockHit.getLocation().distanceToSqr(from);
        if (blockHit.getType() != HitResult.Type.MISS) {
            maxDistanceSq = blockDistanceSq;
            maxDistance = Math.sqrt(maxDistanceSq);
        }
        Vec3 direction = camera.getViewVector(1.0F);
        Vec3 to = from.add(direction.scale(maxDistance));
        AABB box = camera.getBoundingBox().expandTowards(direction.scale(maxDistance)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
            camera, from, to, box, e -> EntitySelector.CAN_BE_PICKED.test(e) && e != player && !e.isPassengerOfSameVehicle(camera), maxDistanceSq
        );
        return entityHit != null && entityHit.getLocation().distanceToSqr(from) < blockDistanceSq
            ? withinRange(entityHit, from, entityRange)
            : withinRange(blockHit, from, blockRange);
    }

    private static HitResult withinRange(HitResult hit, Vec3 from, double range) {
        Vec3 location = hit.getLocation();
        if (location.closerThan(from, range)) {
            return hit;
        }
        Direction direction = Direction.getApproximateNearest(location.x - from.x, location.y - from.y, location.z - from.z);
        return BlockHitResult.miss(location, direction, BlockPos.containing(location));
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Entity mob = possessedMob(mc);
        if (mob == null || mc.getCameraEntity() != mob) {
            return;
        }
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        float yaw = mc.player.getViewYRot(partial);
        float pitch = mc.player.getViewXRot(partial);
        mob.setYRot(yaw);
        mob.yRotO = yaw;
        mob.setXRot(pitch);
        mob.xRotO = pitch;
        if (mob instanceof LivingEntity living) {
            living.setYHeadRot(yaw);
            living.yHeadRotO = yaw;
            living.yBodyRot = yaw;
            living.yBodyRotO = yaw;
        }
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre<?> event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.getEntity(event.getRenderState().id) instanceof Player player && PossessionData.of(player).active()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (possessedMob(Minecraft.getInstance()) != null) {
            event.setCanceled(true);
        }
    }
}
