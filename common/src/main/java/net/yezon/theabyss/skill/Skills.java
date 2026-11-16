package net.yezon.theabyss.skill;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.ProjectileImpactEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingDeathEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingExperienceDropEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingIncomingDamageEvent;
import net.yezon.theabyss.platform.event.entity.living.MobEffectEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.platform.event.tick.PlayerTickEvent;
import net.yezon.theabyss.platform.event.tick.ServerTickEvent;
import net.yezon.theabyss.platform.network.PacketDistributor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.network.SkillSyncMessage;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.RingFx;
import net.yezon.theabyss.platform.services.ServerHooks;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class Skills {
    private static final String DATA = "theabyss_skills";
    private static final int STORY_LEVELS = 3;
    private static final int BLEED_TICKS = 80;
    private static final float BLEED_CHANCE = 0.25F;
    private static final int HEART_INTERVAL = 120;
    private static final Map<UUID, Set<Skill>> CACHE = new ConcurrentHashMap<>();
    private static final Map<LivingEntity, Long> BLEEDS = new HashMap<>();
    private static final Map<Projectile, UUID> TURNED = new WeakHashMap<>();
    private static final Map<Projectile, UUID> ROLLED = new WeakHashMap<>();
    private static final RandomSource FORESIGHT_RANDOM = RandomSource.create();
    private static final Identifier SPEED = id("skill_light_feet");
    private static final Identifier OXYGEN = id("skill_deep_lungs");
    private static final Identifier FALL = id("skill_root_grip");
    private static final Identifier KNOCKBACK = id("skill_unbroken");
    private static boolean scaling;

    private Skills() {
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(TheabyssMod.MODID, path);
    }

    private static CompoundTag data(ServerPlayer player) {
        CompoundTag root = net.yezon.theabyss.platform.PersistentData.get(player);
        if (!root.contains(DATA)) {
            root.put(DATA, new CompoundTag());
        }
        return root.getCompoundOrEmpty(DATA);
    }

    public static Set<Skill> learned(ServerPlayer player) {
        return CACHE.computeIfAbsent(player.getUUID(), uuid -> {
            Set<Skill> set = EnumSet.of(Skill.REFLECTION);
            for (String part : data(player).getStringOr("learned", "").split(",")) {
                Skill skill = Skill.byId(part);
                if (skill != null) {
                    set.add(skill);
                }
            }
            return set;
        });
    }

    public static boolean has(ServerPlayer player, Skill skill) {
        return learned(player).contains(skill);
    }

    public static int spent(ServerPlayer player) {
        return data(player).getIntOr("spent", 0);
    }

    private static void store(ServerPlayer player, Set<Skill> set, int spent) {
        List<String> ids = new ArrayList<>();
        for (Skill skill : set) {
            if (skill != Skill.REFLECTION) {
                ids.add(skill.id());
            }
        }
        data(player).putString("learned", String.join(",", ids));
        data(player).putInt("spent", Math.max(0, spent));
        Set<Skill> copy = EnumSet.of(Skill.REFLECTION);
        copy.addAll(set);
        CACHE.put(player.getUUID(), copy);
        apply(player);
        sync(player, false);
    }

    public static void learn(ServerPlayer player, Skill skill) {
        Set<Skill> set = EnumSet.copyOf(learned(player));
        boolean open = skill.parents().stream().anyMatch(set::contains);
        if (skill == Skill.REFLECTION || set.contains(skill) || !open) {
            sync(player, false);
            return;
        }
        if (player.experienceLevel < skill.cost()) {
            player.sendOverlayMessage(Component.translatable("skill.theabyss.not_enough", skill.cost()));
            sync(player, false);
            return;
        }
        player.giveExperienceLevels(-skill.cost());
        set.add(skill);
        store(player, set, spent(player) + skill.cost());
        sound(player.level(), player.blockPosition(), "minecraft:block.respawn_anchor.charge", 0.7F, 1.2F);
        sound(player.level(), player.blockPosition(), "minecraft:particle.soul_escape", 1.4F, 0.6F);
    }

    public static void forget(ServerPlayer player) {
        int refund = spent(player) / 2;
        store(player, EnumSet.of(Skill.REFLECTION), 0);
        if (refund > 0) {
            player.giveExperienceLevels(refund);
        }
        sound(player.level(), player.blockPosition(), "minecraft:block.respawn_anchor.deplete", 0.8F, 0.8F);
    }

    public static void debugReset(ServerPlayer player) {
        store(player, EnumSet.of(Skill.REFLECTION), 0);
    }

    public static void unlockAll(ServerPlayer player) {
        store(player, EnumSet.allOf(Skill.class), spent(player));
    }

    public static void open(ServerPlayer player) {
        sync(player, true);
    }

    public static void sync(ServerPlayer player, boolean open) {
        List<String> ids = new ArrayList<>();
        for (Skill skill : learned(player)) {
            ids.add(skill.id());
        }
        if (player.connection != null && ServerHooks.get().hasChannel(player.connection, SkillSyncMessage.TYPE)) {
            PacketDistributor.sendToPlayer(player, new SkillSyncMessage(ids, spent(player), open));
        }
    }

    public static void storyProgress(ServerPlayer player, int page) {
        if (page < 0 || page > 30) {
            return;
        }
        CompoundTag data = data(player);
        int done = data.getIntOr("story", 0);
        if ((done & 1 << page) != 0) {
            return;
        }
        data.putInt("story", done | 1 << page);
        player.giveExperienceLevels(STORY_LEVELS);
    }

    static void sound(ServerLevel level, BlockPos pos, String id, float volume, float pitch) {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(id));
        if (sound != null) {
            level.playSound(null, pos, sound, SoundSource.PLAYERS, volume, pitch);
        }
    }

    public static void apply(ServerPlayer player) {
        Set<Skill> set = learned(player);
        modifier(player, Attributes.MOVEMENT_SPEED, SPEED, set.contains(Skill.LIGHT_FEET), 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        modifier(player, Attributes.OXYGEN_BONUS, OXYGEN, set.contains(Skill.DEEP_LUNGS), 1.0, AttributeModifier.Operation.ADD_VALUE);
        modifier(player, Attributes.SAFE_FALL_DISTANCE, FALL, set.contains(Skill.ROOT_GRIP), 3.0, AttributeModifier.Operation.ADD_VALUE);
        modifier(player, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK, set.contains(Skill.UNBROKEN), 0.4, AttributeModifier.Operation.ADD_VALUE);
    }

    private static void modifier(ServerPlayer player, Holder<Attribute> attribute, Identifier id, boolean on, double amount, AttributeModifier.Operation op) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (on) {
            instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, op));
        } else {
            instance.removeModifier(id);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CACHE.remove(player.getUUID());
            apply(player);
            sync(player, false);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        CACHE.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CACHE.remove(player.getUUID());
            apply(player);
            sync(player, false);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        Tag data = net.yezon.theabyss.platform.PersistentData.get(event.getOriginal()).get(DATA);
        if (data != null) {
            net.yezon.theabyss.platform.PersistentData.get(event.getEntity()).put(DATA, data.copy());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        if (victim instanceof ServerPlayer player && has(player, Skill.HARDENED_SKIN)) {
            event.setAmount(event.getAmount() * 0.9F);
        }
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && event.getSource().getDirectEntity() == attacker && victim != attacker
            && has(attacker, Skill.BLEEDING_EDGE) && attacker.getRandom().nextFloat() < BLEED_CHANCE) {
            BLEEDS.put(victim, victim.level().getGameTime() + BLEED_TICKS);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (Map.Entry<LivingEntity, Long> entry : new ArrayList<>(BLEEDS.entrySet())) {
            LivingEntity victim = entry.getKey();
            if (!(victim.level() instanceof ServerLevel level) || victim.isRemoved() || !victim.isAlive() || level.getGameTime() > entry.getValue()) {
                BLEEDS.remove(victim);
                continue;
            }
            long left = entry.getValue() - level.getGameTime();
            if (left % 20 == 0) {
                victim.setInvulnerableTime(0);
                victim.hurtServer(level, level.damageSources().magic(), 1.0F);
            }
            if (left % 4 == 0) {
                level.sendParticles(new DustParticleOptions(0x7A0A12, 1.0F), victim.getX(), victim.getY() + victim.getBbHeight() * 0.6, victim.getZ(), 2,
                    victim.getBbWidth() * 0.3, victim.getBbHeight() * 0.25, victim.getBbWidth() * 0.3, 0.0);
            }
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        BLEEDS.remove(victim);
        if (event.getSource().getEntity() instanceof ServerPlayer killer && victim instanceof Enemy && has(killer, Skill.FEAST)
            && killer.getRandom().nextBoolean()) {
            killer.heal(1.0F);
            killer.level().sendParticles(ParticleTypes.SOUL, killer.getX(), killer.getY() + 1.0, killer.getZ(), 4, 0.3, 0.4, 0.3, 0.01);
        }
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (event.getAttackingPlayer() instanceof ServerPlayer player && has(player, Skill.HUNGRY_MIND)
            && TheabyssMod.MODID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).getNamespace())) {
            event.setDroppedExperience((int)Math.ceil(event.getDroppedExperience() * 1.25));
        }
    }

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (scaling || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MobEffectInstance instance = event.getEffectInstance();
        if (instance.isInfiniteDuration() || instance.getDuration() <= 1) {
            return;
        }
        float factor = factor(player, instance);
        if (factor >= 1.0F) {
            return;
        }
        event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        scaling = true;
        try {
            player.addEffect(instance.withScaledDuration(factor), event.getEffectSource());
        } finally {
            scaling = false;
        }
    }

    private static float factor(ServerPlayer player, MobEffectInstance effect) {
        if (effect.is(TheabyssModMobEffects.FEAR.getDelegate()) && has(player, Skill.CALM_MIND)) {
            return 0.5F;
        }
        if ((effect.is(MobEffects.BLINDNESS) || effect.is(MobEffects.DARKNESS)) && has(player, Skill.CLEAR_EYES)) {
            return 0.5F;
        }
        if ((effect.is(MobEffects.POISON) || effect.is(MobEffects.WEAKNESS) || effect.is(MobEffects.SLOWNESS) || effect.is(MobEffects.MINING_FATIGUE))
            && has(player, Skill.IRON_WILL)) {
            return 0.75F;
        }
        return 1.0F;
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();
        if (projectile.level().isClientSide() || !(event.getRayTraceResult() instanceof EntityHitResult hit)
            || !(hit.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        UUID turnedBy = TURNED.get(projectile);
        if (turnedBy != null) {
            if (turnedBy.equals(player.getUUID())) {
                event.setCanceled(true);
            }
            return;
        }
        if (projectile.getOwner() == player || !has(player, Skill.FORESIGHT) || player.getUUID().equals(ROLLED.get(projectile))) {
            return;
        }
        ROLLED.put(projectile, player.getUUID());
        if (!FORESIGHT_RANDOM.nextBoolean()) {
            return;
        }
        event.setCanceled(true);
        TURNED.put(projectile, player.getUUID());
        turnBack(player, projectile);
    }

    private static void turnBack(ServerPlayer player, Projectile projectile) {
        Entity shooter = projectile.getOwner();
        double speed = Math.max(0.9, projectile.getDeltaMovement().length() * 0.85);
        projectile.deflect(ProjectileDeflection.REVERSE, player, EntityReference.of(player), false, 1.0);
        Vec3 at = projectile.position();
        if (shooter != null && shooter.isAlive() && shooter.level() == projectile.level() && shooter.distanceToSqr(at) < 64.0 * 64.0) {
            Vec3 way = shooter.position().add(0.0, shooter.getBbHeight() * 0.6, 0.0).subtract(at);
            double ticks = way.length() / speed;
            projectile.setDeltaMovement(way.normalize().scale(speed).add(0.0, 0.5 * projectile.getGravity() * ticks, 0.0));
        } else {
            projectile.setDeltaMovement(projectile.getDeltaMovement().normalize().scale(speed));
        }
        projectile.needsSync = true;
        ServerLevel level = player.level();
        RingFx.burst(level, RingVisuals.smoke(0x0D0A12, 0.45F, 22).gravity(-0.002F).drag(0.85F), at, 10, 0.18, 0.04);
        RingFx.burst(level, RingVisuals.sparkle(0x9C8BD0, 0.12F, 10), at, 6, 0.1, 0.12);
        level.sendParticles(ParticleTypes.SQUID_INK, at.x, at.y, at.z, 5, 0.15, 0.15, 0.15, 0.03);
        sound(level, BlockPos.containing(at), "minecraft:entity.breeze.deflect", 0.9F, 0.7F);
        sound(level, BlockPos.containing(at), "minecraft:particle.soul_escape", 1.2F, 0.6F);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % HEART_INTERVAL != 0 || !player.isAlive()
            || player.getHealth() >= player.getMaxHealth() || !has(player, Skill.ABYSS_HEART)) {
            return;
        }
        int light = player.level().getMaxLocalRawBrightness(BlockPos.containing(player.getEyePosition()));
        float chance = light <= 7 ? 1.0F : 0.6F - (light - 8) * (0.4F / 7.0F);
        if (player.getRandom().nextFloat() < chance) {
            player.heal(1.0F);
        }
    }
}
