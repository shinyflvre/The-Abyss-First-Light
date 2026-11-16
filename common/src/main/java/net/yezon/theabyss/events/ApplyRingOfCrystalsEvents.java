package net.yezon.theabyss.events;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.entity.CrystalNeedleEntity;
import net.yezon.theabyss.entity.CrystalSlashEntity;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingCast;
import net.yezon.theabyss.ring.SomniumUpgrades;

public class ApplyRingOfCrystalsEvents {
    public static final double SLASH_COST = 12.0;
    public static final double NEEDLES_COST = 15.0;
    public static final int SLASH_COOLDOWN = 10;
    public static final int NEEDLES_COOLDOWN = 20;
    private static final double WEAKER = 0.45;
    private static final double RANGE = 32.0;
    private static final int NEEDLES = 5;
    public static final int NEEDLES_LEVEL = 3;
    private static final Map<UUID, Long> NEEDLES_READY = new HashMap<>();

    private ApplyRingOfCrystalsEvents() {
    }

    private static float halfHearts(double hp) {
        return Math.max(1, Math.round((float)hp));
    }

    public static float slashDamage(double upgrade) {
        return halfHearts(WEAKER * (6.0 + 2.0 * upgrade));
    }

    public static float needleDamage(double upgrade) {
        return (float)net.yezon.theabyss.ring.RingDamage.CRYSTAL_NEEDLES.at(upgrade);
    }

    public static void slash(LevelAccessor world, Entity entity, ItemStack itemstack) {
        if (!(entity instanceof LivingEntity caster)) {
            return;
        }
        RingCast cast = RingCast.start(world, caster, itemstack, SLASH_COST, caster.getEyePosition());
        if (cast == null) {
            return;
        }
        ServerLevel level = cast.server();
        if (level != null) {
            Vec3 eye = caster.getEyePosition();
            net.yezon.theabyss.ring.fx.SpellCircle.on(level, net.yezon.theabyss.ring.fx.SpellCircle.RIPPLE, caster, net.yezon.theabyss.ring.RingVisuals.LILAC, 1.2F, 12);
            CrystalSlashEntity slash = TheabyssModEntities.CRYSTAL_SLASH.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (slash != null) {
                slash.setPos(eye.add(0.0, -0.25, 0.0).add(caster.getLookAngle().scale(0.8)));
                slash.launch(caster, caster.getLookAngle().scale(CrystalSlashEntity.SPEED), slashDamage(cast.vars().ManaDamage));
                level.addFreshEntity(slash);
            }
            caster.swing(hand(caster, itemstack), SwingAnimation.DEFAULT, true);
            level.playSound(null, eye.x, eye.y, eye.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 1.3F);
            level.playSound(null, eye.x, eye.y, eye.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2F, 1.6F);
        }
        cast.durability(TheabyssModItems.RING_OF_CRYSTALS.get(), 1).cooldown(SLASH_COOLDOWN).charge(SLASH_COST);
    }

    private static InteractionHand hand(LivingEntity caster, ItemStack ring) {
        if (caster.getMainHandItem() == ring) {
            return InteractionHand.MAIN_HAND;
        }
        if (caster.getOffhandItem() == ring) {
            return InteractionHand.OFF_HAND;
        }
        return net.yezon.theabyss.ring.Gauntlet.is(caster.getOffhandItem()) && !net.yezon.theabyss.ring.Gauntlet.is(caster.getMainHandItem())
            ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    public static ItemStack heldRing(LivingEntity player) {
        if (player.getMainHandItem().is(TheabyssModItems.RING_OF_CRYSTALS.get())) {
            return player.getMainHandItem();
        }
        return player.getOffhandItem().is(TheabyssModItems.RING_OF_CRYSTALS.get()) ? player.getOffhandItem() : ItemStack.EMPTY;
    }

    public static void needles(ServerPlayer player) {
        needles(player, heldRing(player));
    }

    public static void needles(ServerPlayer player, ItemStack ring) {
        if (ring.isEmpty() || player.isSpectator()) {
            return;
        }
        if (SomniumUpgrades.level(TheabyssModVariables.getPlayerVariables(player), SomniumUpgrades.Kind.DAMAGE) < NEEDLES_LEVEL) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.crystals.needles_locked", NEEDLES_LEVEL));
            return;
        }
        ServerLevel level = player.level();
        long now = level.getGameTime();
        Long ready = NEEDLES_READY.get(player.getUUID());
        if (ready != null && now < ready) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.crystals.needles_cooldown", (int)Math.ceil((ready - now) / 20.0)));
            return;
        }
        RingCast cast = RingCast.start(level, player, ring, NEEDLES_COST, player.getEyePosition());
        if (cast == null) {
            return;
        }
        TheabyssModVariables.PlayerVariables vars = cast.vars();
        NEEDLES_READY.put(player.getUUID(), now + (long)(NEEDLES_COOLDOWN * vars.ManaCooldown));
        net.yezon.theabyss.ring.fx.SpellCircle.on(level, net.yezon.theabyss.ring.fx.SpellCircle.RIPPLE, player, net.yezon.theabyss.ring.RingVisuals.LILAC, 1.2F, 12);
        player.swing(hand(player, ring), SwingAnimation.DEFAULT, true);
        fire(level, player, (float)vars.ManaDamage);
        cast.durability(TheabyssModItems.RING_OF_CRYSTALS.get(), 1).charge(NEEDLES_COST);
    }

    private static void fire(ServerLevel level, LivingEntity caster, float damageUpgrade) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        HitResult hit = level.clip(new ClipContext(eye, eye.add(look.scale(RANGE)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        Vec3 end = hit.getType() == HitResult.Type.MISS ? eye.add(look.scale(RANGE)) : hit.getLocation();
        Vec3 aim = end;
        double best = Double.MAX_VALUE;
        for (Entity e : level.getEntities(caster, new net.minecraft.world.phys.AABB(eye, end).inflate(1.0), e -> e instanceof LivingEntity l && l.isAlive() && !e.isSpectator())) {
            var crossing = e.getBoundingBox().inflate(0.3).clip(eye, end);
            if (crossing.isPresent() && eye.distanceToSqr(crossing.get()) < best) {
                best = eye.distanceToSqr(crossing.get());
                aim = e.getBoundingBox().getCenter();
            }
        }
        float damage = needleDamage(damageUpgrade);
        float xRot = caster.getXRot() * ((float)Math.PI / 180.0F);
        float yRot = caster.getYRot() * ((float)Math.PI / 180.0F);
        for (int i = 0; i < NEEDLES; i++) {
            float roll = -36.0F + i * 72.0F;
            float around = (float)Math.toRadians(roll);
            Vec3 offset = new Vec3(0.0, 1.5, 0.0).zRot(around).xRot(-xRot).yRot(-yRot);
            Vec3 start = eye.add(offset);
            Vec3 velocity = aim.subtract(start).normalize().scale(CrystalNeedleEntity.SPEED);
            CrystalNeedleEntity needle = TheabyssModEntities.CRYSTAL_NEEDLE.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (needle == null) {
                continue;
            }
            needle.setPos(start);
            needle.launch(caster, velocity, damage, roll);
            level.addFreshEntity(needle);
        }
        level.playSound(null, eye.x, eye.y, eye.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.4F, 1.4F);
        level.playSound(null, eye.x, eye.y, eye.z, SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.PLAYERS, 1.0F, 0.8F);
    }
}
