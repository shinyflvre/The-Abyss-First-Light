package net.yezon.theabyss.infection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.living.LivingChangeTargetEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingDeathEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingHealEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.network.OvergrowthMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.rift.RiftStory;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.RingFx;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class Overgrowth {
    public static final ResourceKey<DamageType> INFECTION = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "infection"));
    public static final ResourceKey<DamageType> COLLAPSE = ResourceKey.create(Registries.DAMAGE_TYPE,
        Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "infection_collapse"));
    public static final int COLLAPSE_AFTER = 1200;
    public static final int COLLAPSE_TICKS = 100;
    private static final int SHRINK_EVERY = 200;
    private static final double MIN_HEALTH = 6.0;
    private static final float LAST_HEART = 2.0F;
    private static final double HUNT_RANGE = 20.0;
    private static final double FRIEND_RANGE = 32.0;
    private static final Identifier SHRINK = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "overgrowth_shrink");
    private static final Identifier ROOTED = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "overgrowth_rooted");
    private static final Identifier ROOTED_JUMP = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "overgrowth_rooted_jump");
    private static final String[] VOICE = {"overgrowth.1", "overgrowth.2", "overgrowth.3", "overgrowth.4"};
    private static final float[] VOICE_AT = {0.0F, 0.3F, 0.6F, 0.9F};
    private static final int DARK = 0x05080C;
    private static final int CYAN = 0x38C5DE;

    private static final class State {
        int ticks;
        int nextBeat;
        int beats;
        int voiced;
        int collapse = -1;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    private Overgrowth() {
    }

    public static boolean active(LivingEntity entity) {
        return STATES.containsKey(entity.getUUID());
    }

    private static float stage(State s) {
        return Mth.clamp(s.ticks / (float)COLLAPSE_AFTER, 0.0F, 1.0F);
    }

    public static void tick(ServerPlayer player) {
        State s = STATES.get(player.getUUID());
        if (s == null) {
            s = new State();
            STATES.put(player.getUUID(), s);
            loseInfected(player);
        }
        ServerLevel level = player.level();
        if (s.collapse >= 0) {
            tickCollapse(level, player, s);
            return;
        }
        s.ticks++;
        float stage = stage(s);
        if (s.voiced < VOICE.length && stage >= VOICE_AT[s.voiced]) {
            if (ConfigConfiguration.storyEnabled()) {
                RiftStory.voice(player, VOICE[s.voiced]);
            }
            s.voiced++;
        }
        shrink(player, s);
        if (--s.nextBeat <= 0) {
            beat(level, player, s, stage);
        }
        if (s.ticks % 40 == 0) {
            loseInfected(player);
            huntBy(level, player);
        }
        if (s.ticks >= COLLAPSE_AFTER) {
            startCollapse(level, player, s);
        }
    }

    public static void clear(ServerPlayer player) {
        State s = STATES.get(player.getUUID());
        if (s == null || s.collapse >= 0) {
            return;
        }
        STATES.remove(player.getUUID());
        removeModifier(player, Attributes.MAX_HEALTH, SHRINK);
        send(player, -1.0F, 0, -1);
    }

    private static void beat(ServerLevel level, ServerPlayer player, State s, float stage) {
        s.nextBeat = Math.round(Mth.lerp(stage, 32.0F, 14.0F));
        s.beats++;
        boolean stagger = stage >= 0.3F && s.beats % 3 == 0;
        if (stagger) {
            player.setSprinting(false);
        }
        float damage = Math.min(1.0F + stage, player.getHealth() - LAST_HEART);
        if (damage > 0.0F) {
            player.hurtServer(level, source(level, INFECTION), damage);
        }
        send(player, stage, OvergrowthMessage.BEAT | (stagger ? OvergrowthMessage.STAGGER : 0), -1);
    }

    private static void shrink(ServerPlayer player, State s) {
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) {
            return;
        }
        AttributeModifier old = health.getModifier(SHRINK);
        double base = health.getValue() - (old == null ? 0.0 : old.amount());
        double cut = Math.min(Math.max(0.0, base - MIN_HEALTH), 2.0 * (s.ticks / SHRINK_EVERY));
        if (old == null || old.amount() != -cut) {
            health.addOrUpdateTransientModifier(new AttributeModifier(SHRINK, -cut, AttributeModifier.Operation.ADD_VALUE));
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    private static void startCollapse(ServerLevel level, ServerPlayer player, State s) {
        s.collapse = 0;
        player.setSprinting(false);
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.addOrUpdateTransientModifier(new AttributeModifier(ROOTED, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        AttributeInstance jump = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (jump != null) {
            jump.addOrUpdateTransientModifier(new AttributeModifier(ROOTED_JUMP, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        if (ConfigConfiguration.storyEnabled()) {
            RiftStory.voice(player, "overgrowth.end");
        }
        sound(level, player.position(), "minecraft:entity.warden.dig", 1.2F, 0.7F);
        sound(level, player.position(), "theabyss:night_notice", 1.0F, 0.6F);
    }

    private static void tickCollapse(ServerLevel level, ServerPlayer player, State s) {
        s.collapse++;
        Vec3 feet = player.position();
        RandomSource random = player.getRandom();
        for (int i = 0; i < 4; i++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double r = 1.2 + random.nextDouble() * 1.6;
            Vec3 at = feet.add(Math.cos(a) * r, 0.05 + random.nextDouble() * 1.4, Math.sin(a) * r);
            Vec3 pull = feet.subtract(at).scale(0.09);
            RingFx.emit(level, i % 2 == 0 ? RingVisuals.smoke(DARK, 0.4F, 18).drag(0.9F) : RingVisuals.sparkle(CYAN, 0.1F, 14), at, pull);
        }
        if (s.collapse % 10 == 0) {
            RingFx.ring(level, RingVisuals.smoke(DARK, 0.45F, 20).drag(0.86F), feet.add(0.0, 0.08, 0.0), 1.8, 14, -0.06, 0.0);
            level.sendParticles(ParticleTypes.SCULK_SOUL, feet.x, feet.y + 0.2, feet.z, 2, 0.4, 0.1, 0.4, 0.01);
        }
        if (s.collapse % Math.max(6, 14 - s.collapse / 10) == 0) {
            send(player, 1.0F, OvergrowthMessage.BEAT, s.collapse);
        }
        if (s.collapse == 40) {
            sound(level, feet, "minecraft:entity.warden.dig", 1.0F, 0.5F);
        }
        if (s.collapse >= COLLAPSE_TICKS) {
            sound(level, feet, "minecraft:block.sculk_shrieker.break", 1.0F, 0.5F);
            player.hurtServer(level, source(level, COLLAPSE), Float.MAX_VALUE);
            if (player.isAlive()) {
                end(player);
            }
        }
    }

    private static void end(ServerPlayer player) {
        STATES.remove(player.getUUID());
        removeModifier(player, Attributes.MAX_HEALTH, SHRINK);
        removeModifier(player, Attributes.MOVEMENT_SPEED, ROOTED);
        removeModifier(player, Attributes.JUMP_STRENGTH, ROOTED_JUMP);
        send(player, -1.0F, 0, -1);
    }

    private static void removeModifier(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, Identifier id) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    private static void send(ServerPlayer player, float stage, int flags, int collapse) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new OvergrowthMessage(player.getId(), stage, flags, collapse));
    }

    private static DamageSource source(ServerLevel level, ResourceKey<DamageType> type) {
        return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(type));
    }

    private static void sound(ServerLevel level, Vec3 at, String id, float volume, float pitch) {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(id));
        if (sound != null) {
            level.playSound(null, at.x, at.y, at.z, sound, SoundSource.PLAYERS, volume, pitch);
        }
    }

    public static boolean infected(Entity entity) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return TheabyssMod.MODID.equals(id.getNamespace()) && (id.getPath().startsWith("infected_") || id.getPath().startsWith("infceted_"));
    }

    private static void loseInfected(ServerPlayer player) {
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(FRIEND_RANGE), m -> infected(m) && m.getTarget() == player)) {
            mob.setTarget(null);
        }
    }

    private static void huntBy(ServerLevel level, ServerPlayer player) {
        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(HUNT_RANGE),
            m -> m instanceof Enemy && !infected(m) && m.isAlive() && m.getTarget() == null)) {
            if (mob.getSensing().hasLineOfSight(player)) {
                mob.setTarget(player);
            }
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof ServerPlayer player && active(player) && infected(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !active(player)) {
            return;
        }
        if (event.getAmount() <= 1.0F) {
            event.setCanceled(true);
        } else {
            event.setAmount(event.getAmount() * 0.5F);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        boolean byInfection = event.getSource().is(INFECTION) || event.getSource().is(COLLAPSE);
        if (STATES.containsKey(player.getUUID())) {
            end(player);
        }
        if (byInfection) {
            TheabyssModVariables.updatePlayerVariables(player, vars -> {
                vars.Infected = 0.0;
                vars.syncPlayerVariables(player);
            });
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && STATES.containsKey(player.getUUID())) {
            end(player);
        }
    }
}
