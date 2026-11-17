package net.yezon.theabyss.ring.secondary;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.events.ApplyRingOfPocketRoomEvents;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.SomniumUpgrades;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.util.FallGuard;

public final class BringAlong {
    public static final double RANGE = 12.0;
    private static final ResourceKey<Level> POCKET = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("theabyss:pocket_dimension"));
    private static final int WAIT = 60;
    private static final double COST = 50.0;
    private static final int GATHER = 14;
    private static final int SETTLE = 8;

    private BringAlong() {
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.serverLevel();
        if (player.getCooldowns().isOnCooldown(ring.getItem())) {
            float left = player.getCooldowns().getCooldownPercent(ring.getItem(), 0.0F) * 500.0F * (float)vars.ManaCooldown / 20.0F;
            player.displayClientMessage(Component.translatable("ring.theabyss.secondary.cooldown", Component.translatable("tooltip.theabyss.ring_of_pocket_box.secondary.ability"), Math.max(1, (int)Math.ceil(left))), true);
            return false;
        }
        LivingEntity target = Aim.creature(level, player, RANGE);
        if (target == null) {
            player.displayClientMessage(Component.translatable("ring.theabyss.secondary.no_target", Component.translatable("tooltip.theabyss.ring_of_pocket_box.secondary.ability")), true);
            return false;
        }
        if (vars.Mana < COST * SomniumUpgrades.costFactor(vars)) {
            player.displayClientMessage(Component.translatable("ring.theabyss.low_energy"), true);
            return false;
        }
        SpellFxMessage.around(level, player.position(), SpellFxMessage.TETHER, List.of(player.getId(), target.getId()), List.of(), GATHER + 14);
        target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, false));
        RingVisuals.sound(level, target.position(), net.minecraft.sounds.SoundEvents.CHAIN_PLACE, 1.0F, 1.4F);
        RingVisuals.sound(level, player.position(), net.minecraft.sounds.SoundEvents.BEACON_POWER_SELECT, 0.8F, 1.6F);
        player.getCooldowns().addCooldown(ring.getItem(), GATHER + 4);
        gather(player, ring, target, level, 1);
        return true;
    }

    private static void gather(ServerPlayer player, ItemStack ring, LivingEntity target, ServerLevel level, int tick) {
        TheabyssMod.queueServerWork(1, () -> {
            if (player.isRemoved() || player.level() != level) {
                return;
            }
            if (target.isAlive() && target.level() == level && tick < GATHER) {
                Vec3 ahead = player.getLookAngle().multiply(1.0, 0.0, 1.0);
                ahead = ahead.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : ahead.normalize();
                Vec3 want = player.position().add(ahead.scale(1.4)).add(0.0, 0.3, 0.0);
                target.setDeltaMovement(want.subtract(target.position()).scale(0.25));
                target.hasImpulse = true;
                target.hurtMarked = true;
                gather(player, ring, target, level, tick + 1);
                return;
            }
            Runnable release = hold(level, target);
            boolean started = ApplyRingOfPocketRoomEvents.execute(level, player.getX(), player.getY(), player.getZ(), player, ring);
            if (!started || !target.isAlive()) {
                release.run();
                return;
            }
            RingVisuals.pocketPortal(level, target.position());
            follow(player, target, level, 1, release);
        });
    }

    private static Runnable hold(ServerLevel level, LivingEntity target) {
        ChunkPos chunk = target.chunkPosition();
        if (level.getForcedChunks().contains(chunk.toLong())) {
            return () -> {
            };
        }
        level.setChunkForced(chunk.x, chunk.z, true);
        return () -> level.setChunkForced(chunk.x, chunk.z, false);
    }

    private static void follow(ServerPlayer player, LivingEntity target, ServerLevel from, int tick, Runnable release) {
        TheabyssMod.queueServerWork(1, () -> {
            if (!target.isAlive() || player.isRemoved()) {
                release.run();
                return;
            }
            if (player.level() == from) {
                if (tick < WAIT) {
                    target.setDeltaMovement(0.0, 0.03, 0.0);
                    target.hasImpulse = true;
                    follow(player, target, from, tick + 1, release);
                } else {
                    release.run();
                }
                return;
            }
            TheabyssMod.queueServerWork(SETTLE, () -> {
                fetch(player, target, from);
                release.run();
            });
        });
    }

    private static void fetch(ServerPlayer player, LivingEntity target, ServerLevel from) {
        if (!target.isAlive() || player.isRemoved() || player.level() == from) {
            TheabyssMod.LOGGER.warn("Bring Along: {} could not be fetched (alive {}, caster left {})", target.getName().getString(), target.isAlive(),
                player.level() != from);
            return;
        }
        ServerLevel to = player.serverLevel();
        Vec3 ahead = player.getLookAngle().multiply(1.0, 0.0, 1.0);
        ahead = ahead.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : ahead.normalize();
        Vec3 at = player.position().add(ahead.scale(1.6));
        float yaw = player.getYRot() + 180.0F;
        net.minecraft.world.entity.Entity moved = net.yezon.theabyss.util.AbyssTeleport.transition(target, new net.minecraft.world.level.portal.DimensionTransition(to, at, Vec3.ZERO, yaw, 0.0F,
            net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING));
        TheabyssMod.LOGGER.info("Bring Along: {} -> {} {}", target.getName().getString(), to.dimension().location(), moved == null ? "FAILED" : "at " + moved.position());
        if (moved instanceof LivingEntity arrived) {
            FallGuard.protect(arrived, 60);
            TheabyssMod.queueServerWork(2, () -> {
                RingVisuals.pocketPortal(to, at);
                SpellFxMessage.around(to, at, SpellFxMessage.TETHER, List.of(player.getId(), arrived.getId()), List.of(), 16);
            });
        }
    }
}
