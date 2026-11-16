package net.yezon.theabyss.events;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.living.LivingFallEvent;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.network.TeleportDashMessage;
import net.yezon.theabyss.ring.RingCast;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.TrueInvisibility;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public class ApplyRingOfTeleportEvents {
    private static final int NO_FALL_TICKS = 100;
    private static final int RUSH_TICKS = 8;
    private static final Map<UUID, Long> NO_FALL_UNTIL = new HashMap<>();

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        if (!(entity instanceof LivingEntity caster) || itemstack.getItem() != TheabyssModItems.RING_OF_TELEPORT.get()) {
            return;
        }
        RingCast cast = RingCast.start(world, entity, itemstack, 20.0, entity.position());
        if (cast == null) {
            return;
        }
        cast.durability(TheabyssModItems.RING_OF_TELEPORT.get(), 1).cooldown(20);
        ServerLevel level = cast.server();
        if (level != null) {
            step(level, caster, distance(level(cast.vars().ManaUpgrade)));
        }
        cast.charge(20.0);
    }

    public static int level(double manaUpgrade) {
        return net.yezon.theabyss.ring.RingWear.level(manaUpgrade);
    }

    public static float distance(int level) {
        return 2.25F * (8.0F + (level - 1));
    }

    private static void step(ServerLevel level, LivingEntity caster, float distance) {
        Vec3 from = caster.position();
        Vec3 fromEye = caster.getEyePosition();
        float fromYaw = caster.getYRot();
        float fromPitch = caster.getXRot();
        RingVisuals.teleportStepDepart(level, caster);
        net.yezon.theabyss.ring.fx.SpellCircle.at(level, net.yezon.theabyss.ring.fx.SpellCircle.SHOCK, from, RingVisuals.STEP_SMOKE_GREY, 1.2F, 12);
        Vec3 end = fromEye.add(caster.getLookAngle().normalize().scale(distance));
        BlockHitResult blockHit = level.clip(new ClipContext(fromEye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        LivingEntity target = firstCreature(level, caster, fromEye, blockHit.getLocation());
        if (caster.isPassenger()) {
            caster.stopRiding();
        }
        Vec3 dest;
        float yaw = fromYaw;
        float pitch = fromPitch;
        if (target != null) {
            dest = target.position();
            for (int i = 0; i < 8; i++) {
                dest = target.position().subtract(new Vec3(0.0, 0.0, 1.5).yRot(-(target.getYRot() + i * 45) * Mth.DEG_TO_RAD));
                if (level.getBlockState(BlockPos.containing(dest).above()).isAir()) {
                    break;
                }
            }
            dest = dest.add(0.0, 1.0, 0.0);
            Vec3 look = target.getEyePosition().subtract(0.0, 0.15, 0.0).subtract(dest.add(0.0, caster.getEyeHeight(), 0.0));
            yaw = (float)(Mth.atan2(look.z, look.x) * Mth.RAD_TO_DEG) - 90.0F;
            pitch = (float)(-(Mth.atan2(look.y, look.horizontalDistance()) * Mth.RAD_TO_DEG));
        } else {
            dest = destination(level, caster, blockHit.getBlockPos(), blockHit.getLocation());
        }
        if (caster instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new TeleportDashMessage(fromEye.x, fromEye.y, fromEye.z, fromYaw, fromPitch));
        }
        caster.teleportTo(dest.x, dest.y, dest.z);
        caster.setYRot(yaw);
        caster.setXRot(pitch);
        caster.setYHeadRot(yaw);
        if (caster instanceof ServerPlayer player) {
            player.connection.teleport(dest.x, dest.y, dest.z, yaw, pitch);
        }
        caster.resetFallDistance();
        NO_FALL_UNTIL.put(caster.getUUID(), level.getGameTime() + NO_FALL_TICKS);
        var sound = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:spell_of_teleport"));
        if (sound != null) {
            level.playSound(null, dest.x, dest.y, dest.z, sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
        Vec3 to = dest;
        for (int tick = 0; tick < RUSH_TICKS; tick++) {
            float a = tick / (float)RUSH_TICKS;
            float b = (tick + 1) / (float)RUSH_TICKS;
            TheabyssMod.queueServerWork(tick + 1, () -> RingVisuals.teleportStepTrail(level, from.lerp(to, rush(a)), from.lerp(to, rush(b))));
        }
        TheabyssMod.queueServerWork(RUSH_TICKS, () -> net.yezon.theabyss.ring.fx.SpellCircle.at(level, net.yezon.theabyss.ring.fx.SpellCircle.SHOCK, to, RingVisuals.STEP_SMOKE_GREY, 1.4F, 14));
        TheabyssMod.queueServerWork(RUSH_TICKS, () -> RingVisuals.teleportStepArrive(level, to));
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false, true));
        if (caster instanceof ServerPlayer player) {
            TrueInvisibility.startStep(player, 100);
        }
    }

    private static float rush(float t) {
        float k = 1.0F - t;
        return 1.0F - k * k * k;
    }

    private static LivingEntity firstCreature(ServerLevel level, LivingEntity caster, Vec3 start, Vec3 end) {
        AABB range = caster.getBoundingBox().expandTowards(end.subtract(start));
        Entity nearest = null;
        double best = Double.MAX_VALUE;
        for (Entity e : level.getEntities(caster, range, c -> c.isPickable() && c.isAlive() && !c.isSpectator())) {
            Optional<Vec3> hit = e.getBoundingBox().clip(start, end);
            if (hit.isPresent() && hit.get().distanceToSqr(start) < best) {
                best = hit.get().distanceToSqr(start);
                nearest = e;
            }
        }
        return nearest instanceof LivingEntity living ? living : null;
    }

    private static Vec3 destination(ServerLevel level, LivingEntity entity, BlockPos pos, Vec3 hit) {
        Vec3 bbOffset = entity.getForward().normalize().multiply(entity.getBbWidth() / 3.0, 0.0, entity.getBbHeight() / 3.0);
        Vec3 bbImpact = hit.subtract(bbOffset);
        double ledgeY = level.clip(new ClipContext(Vec3.atBottomCenterOf(pos).add(0.0, 3.0, 0.0), Vec3.atBottomCenterOf(pos), ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, CollisionContext.empty())).getLocation().y;
        boolean isAir = level.getBlockState(new BlockPos(pos.getX(), (int)ledgeY, pos.getZ()).above()).isAir();
        boolean clear = level.clip(new ClipContext(bbImpact, bbImpact.add(0.0, ledgeY - pos.getY(), 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
            entity)).getType() == HitResult.Type.MISS;
        if (isAir && clear && Math.abs(ledgeY - pos.getY()) <= 3.0) {
            return new Vec3(pos.getX() + 0.5, ledgeY + 0.001, pos.getZ() + 0.5);
        }
        return level.clip(new ClipContext(bbImpact, bbImpact.add(0.0, -entity.getBbHeight(), 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity))
            .getLocation().add(0.0, 0.001, 0.0);
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        Long until = NO_FALL_UNTIL.get(entity.getUUID());
        if (until == null) {
            return;
        }
        if (entity.level().getGameTime() <= until) {
            event.setCanceled(true);
        } else {
            NO_FALL_UNTIL.remove(entity.getUUID());
        }
    }
}
