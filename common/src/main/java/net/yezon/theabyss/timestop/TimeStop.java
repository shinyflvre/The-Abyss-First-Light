package net.yezon.theabyss.timestop;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.tick.EntityTickEvent;
import net.yezon.theabyss.platform.event.tick.ServerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.SomniumUpgrades;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class TimeStop {
    public static final double RING_SOMNIUM_COST = 80.0;
    public static final int RING_DURATION = 400;
    public static final double RING_RADIUS = 200.0;
    public static final int RING_DURABILITY_COST = 4;
    public static final int RING_COOLDOWN = 2000;

    public static final int TOTEM_DURATION = 200;
    public static final double TOTEM_RADIUS = 20.0;

    private static final int FREEZE_AMPLIFIER = 40;
    private static final Identifier SPELL_SOUND = Identifier.fromNamespaceAndPath("theabyss", "spell_of_time");

    private record RingCast(long startTick, int cooldown, List<WeakReference<LivingEntity>> frozen) {
    }

    private static final Map<UUID, RingCast> RING_CASTS = new HashMap<>();
    private static final Map<LivingEntity, float[]> FROZEN_POSES = new WeakHashMap<>();

    private TimeStop() {
    }

    public static boolean useRing(ServerPlayer caster, ItemStack ring) {
        if (RING_CASTS.containsKey(caster.getUUID())) {
            endRingCast(caster);
            return true;
        }
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(caster);
        double cost = RING_SOMNIUM_COST * SomniumUpgrades.costFactor(vars);
        if (vars.Mana < cost) {
            caster.sendOverlayMessage(Component.translatable("ring.theabyss.low_energy"));
            return false;
        }
        ServerLevel level = caster.level();
        caster.addEffect(casterEffect(RING_DURATION));
        List<LivingEntity> frozen = freezeAround(caster, RING_RADIUS, RING_DURATION, vars.FriendMode);
        List<WeakReference<LivingEntity>> references = new ArrayList<>(frozen.size());
        frozen.forEach(entity -> references.add(new WeakReference<>(entity)));
        net.yezon.theabyss.ring.fx.RingAccent.send(level, net.yezon.theabyss.ring.fx.RingAccent.TIME, caster.position(), caster, frozen);
        net.yezon.theabyss.ring.fx.SpellCircle.on(level, net.yezon.theabyss.ring.fx.SpellCircle.TECH, caster, 0xFFD36A, 3.0F, 60);
        RING_CASTS.put(caster.getUUID(), new RingCast(level.getServer().getTickCount(), (int)(RING_COOLDOWN * vars.ManaCooldown), references));
        net.yezon.theabyss.ring.RingWear.wear(ring, level, caster, RING_DURABILITY_COST);
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(SPELL_SOUND);
        if (sound != null) {
            level.playSound(null, caster.blockPosition(), sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
        double remaining = vars.Mana - cost;
        TheabyssModVariables.updatePlayerVariables(caster, variables -> {
            variables.Mana = remaining;
            variables.syncPlayerVariables(caster);
        });
        return true;
    }

    public static boolean isStoppingTime(Player player) {
        return RING_CASTS.containsKey(player.getUUID());
    }

    private static void endRingCast(ServerPlayer caster) {
        RingCast cast = RING_CASTS.remove(caster.getUUID());
        if (cast == null) {
            return;
        }
        caster.removeEffect(TheabyssModMobEffects.TIME_STOP_POTION_EFFECT.getDelegate());
        for (WeakReference<LivingEntity> reference : cast.frozen) {
            LivingEntity entity = reference.get();
            if (entity != null && entity.isAlive()) {
                unfreeze(entity);
            }
        }
        int left = cast.cooldown - (caster.level().getServer().getTickCount() - (int)cast.startTick);
        if (left > 0) {
            caster.getCooldowns().addCooldown(new ItemStack(TheabyssModItems.RING_OF_TIME.get()), left);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (RING_CASTS.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        List<ServerPlayer> ended = new ArrayList<>();
        for (Iterator<Map.Entry<UUID, RingCast>> it = RING_CASTS.entrySet().iterator(); it.hasNext();) {
            Map.Entry<UUID, RingCast> entry = it.next();
            ServerPlayer caster = server.getPlayerList().getPlayer(entry.getKey());
            if (caster == null) {
                it.remove();
            } else if (!caster.hasEffect(TheabyssModMobEffects.TIME_STOP_POTION_EFFECT.getDelegate())) {
                ended.add(caster);
            }
        }
        ended.forEach(TimeStop::endRingCast);
    }

    public static void totemActivated(LivingEntity owner) {
        if (!freezeAround(owner, TOTEM_RADIUS, TOTEM_DURATION, false).isEmpty()) {
            owner.addEffect(casterEffect(TOTEM_DURATION));
        }
    }

    public static List<LivingEntity> freezeAround(LivingEntity caster, double radius, int duration, boolean spareFriends) {
        Vec3 centre = caster.position();
        List<LivingEntity> targets = caster.level().getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(radius),
            target -> target != caster && !(spareFriends && target instanceof Player));
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, FREEZE_AMPLIFIER, false, false));
            target.addEffect(new MobEffectInstance(TheabyssModMobEffects.TIME_STOP_POTION_IRATOR.getDelegate(), duration, FREEZE_AMPLIFIER, false, false));
        }
        return targets;
    }

    private static void unfreeze(LivingEntity entity) {
        entity.removeEffect(TheabyssModMobEffects.TIME_STOP_POTION_IRATOR.getDelegate());
        MobEffectInstance slowness = entity.getEffect(MobEffects.SLOWNESS);
        if (slowness != null && slowness.getAmplifier() == FREEZE_AMPLIFIER) {
            entity.removeEffect(MobEffects.SLOWNESS);
        }
    }

    public static MobEffectInstance casterEffect(int duration) {
        return new MobEffectInstance(TheabyssModMobEffects.TIME_STOP_POTION_EFFECT.getDelegate(), duration, 0);
    }

    public static void freezeStarted(LivingEntity entity) {
        if (!(entity instanceof Player)) {
            FROZEN_POSES.put(entity, new float[] {entity.getYRot(), entity.getXRot(), entity.getYHeadRot(), entity.yBodyRot});
        }
    }

    public static void freezeEnded(LivingEntity entity) {
        FROZEN_POSES.remove(entity);
    }

    public static void holdStill(LivingEntity entity) {
        entity.makeStuckInBlock(Blocks.AIR.defaultBlockState(), new Vec3(0.25, 0.05, 0.25));
        entity.setDeltaMovement(Vec3.ZERO);
        restorePose(entity);
    }

    @SubscribeEvent
    public static void onEntityTicked(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity && !entity.level().isClientSide() && FROZEN_POSES.containsKey(entity)) {
            if (entity.hasEffect(TheabyssModMobEffects.TIME_STOP_POTION_IRATOR.getDelegate())) {
                restorePose(entity);
            } else {
                FROZEN_POSES.remove(entity);
            }
        }
    }

    private static void restorePose(LivingEntity entity) {
        float[] pose = FROZEN_POSES.get(entity);
        if (pose == null) {
            return;
        }
        entity.setYRot(pose[0]);
        entity.setXRot(pose[1]);
        entity.setYHeadRot(pose[2]);
        entity.setYBodyRot(pose[3]);
        entity.yRotO = pose[0];
        entity.xRotO = pose[1];
        entity.yHeadRotO = pose[2];
        entity.yBodyRotO = pose[3];
    }
}
