package net.yezon.theabyss.possession.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.client.event.InputEvent;
import net.yezon.theabyss.platform.client.event.RenderFrameEvent;
import net.yezon.theabyss.platform.client.event.RenderHandEvent;
import net.yezon.theabyss.platform.client.event.RenderPlayerEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.compat.GuiGraphicsCompat;
import net.yezon.theabyss.possession.PossessionData;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class PossessionClient {
    private static final ResourceLocation HEART_CONTAINER = ResourceLocation.withDefaultNamespace("hud/heart/container");
    private static final ResourceLocation HEART_FULL = ResourceLocation.withDefaultNamespace("hud/heart/full");
    private static final ResourceLocation HEART_HALF = ResourceLocation.withDefaultNamespace("hud/heart/half");

    private PossessionClient() {
    }

    public static void renderHostHealth(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || !(possessedMob(mc) instanceof LivingEntity host)) {
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
            GuiGraphicsCompat.blitSprite(graphics, HEART_CONTAINER, hx, hy, 9, 9);
            if (i * 2 + 1 < health) {
                GuiGraphicsCompat.blitSprite(graphics, HEART_FULL, hx, hy, 9, 9);
            } else if (i * 2 + 1 == health) {
                GuiGraphicsCompat.blitSprite(graphics, HEART_HALF, hx, hy, 9, 9);
            }
        }
        GuiGraphicsCompat.text(graphics, mc.font, host.getDisplayName(), x, y - (rows - 1) * 10 - 10, 0xFFE0B0FF, true);
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
            camera, from, to, box, e -> !e.isSpectator() && e.isPickable() && e != player && !e.isPassengerOfSameVehicle(camera), maxDistanceSq
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
        Direction direction = Direction.getNearest(location.x - from.x, location.y - from.y, location.z - from.z);
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
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && event.getEntity() instanceof Player player && PossessionData.of(player).active()) {
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
