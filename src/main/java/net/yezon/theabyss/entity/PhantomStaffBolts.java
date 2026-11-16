package net.yezon.theabyss.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.util.RingMode;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class PhantomStaffBolts {
    private static final List<Bolt> BOLTS = new ArrayList<>();
    private static int next;

    private PhantomStaffBolts() {
    }

    private static final class Bolt {
        final ServerLevel level;
        final UUID owner;
        final int id;
        final Vec3 velocity;
        Vec3 pos;
        int age;

        Bolt(ServerLevel level, UUID owner, int id, Vec3 pos, Vec3 velocity) {
            this.level = level;
            this.owner = owner;
            this.id = id;
            this.pos = pos;
            this.velocity = velocity;
        }
    }

    public static void fire(ServerPlayer player) {
        ServerLevel level = player.level();
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 from = player.getEyePosition().add(look.scale(0.6)).subtract(0.0, 0.15, 0.0);
        int id = player.getId() * 64 + (next++ & 63);
        Bolt bolt = new Bolt(level, player.getUUID(), id, from, look.scale(PhantomHazards.BOLT_SPEED));
        BOLTS.add(bolt);
        SpellFxMessage.around(level, from, SpellFxMessage.PHANTOM_BOLT, List.of(id), SpellFxMessage.points(from, bolt.velocity), PhantomHazards.BOLT_LIFE);
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:phantom_attack"));
        if (sound != null) {
            level.playSound(null, player.getX(), player.getY() + 1.0, player.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.2F);
        }
    }

    private static boolean victim(Entity owner, LivingEntity e) {
        return e != owner && e.isAlive() && !e.isSpectator() && !RingMode.spares(owner, e);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (BOLTS.isEmpty() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        for (Bolt bolt : List.copyOf(BOLTS)) {
            if (bolt.level == level && !step(level, bolt)) {
                BOLTS.remove(bolt);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        BOLTS.clear();
    }

    private static boolean step(ServerLevel level, Bolt bolt) {
        Entity owner = level.getEntity(bolt.owner);
        if (owner == null) {
            SpellFxMessage.around(level, bolt.pos, SpellFxMessage.PHANTOM_SPLASH, List.of(bolt.id), SpellFxMessage.points(bolt.pos), 1);
            return false;
        }
        Vec3 from = bolt.pos;
        Vec3 to = from.add(bolt.velocity);
        BlockHitResult block = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        Vec3 end = block.getType() == HitResult.Type.MISS ? to : block.getLocation();
        LivingEntity hit = null;
        Vec3 hitAt = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(from, end).inflate(0.8), e -> victim(owner, e))) {
            AABB box = e.getBoundingBox().inflate(0.25);
            Optional<Vec3> clip = box.contains(from) ? Optional.of(from) : box.clip(from, end);
            if (clip.isPresent() && from.distanceToSqr(clip.get()) < best) {
                best = from.distanceToSqr(clip.get());
                hit = e;
                hitAt = clip.get();
            }
        }
        if (hit != null) {
            DamageSource source = level.damageSources().indirectMagic(owner, owner);
            if (hit.hurtServer(level, source, PhantomHazards.BOLT_DAMAGE)) {
                hit.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0), owner);
            }
            PhantomHazards.splash(level, hitAt, bolt.id);
            return false;
        }
        if (block.getType() != HitResult.Type.MISS) {
            PhantomHazards.splash(level, end, bolt.id);
            return false;
        }
        bolt.pos = to;
        if (++bolt.age >= PhantomHazards.BOLT_LIFE) {
            SpellFxMessage.around(level, to, SpellFxMessage.PHANTOM_SPLASH, List.of(bolt.id), SpellFxMessage.points(to), 1);
            return false;
        }
        return true;
    }
}
