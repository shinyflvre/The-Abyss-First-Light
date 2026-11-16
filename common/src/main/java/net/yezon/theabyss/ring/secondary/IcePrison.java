package net.yezon.theabyss.ring.secondary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.tick.ServerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.entity.NightbladeSpikeEntity;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.RingFx;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class IcePrison {
    public static final double RANGE = 25.0;
    public static final int HOLD = 100;
    public static final int FORM = 6;

    private static final class Prison {
        final ServerLevel level;
        final ServerPlayer caster;
        final LivingEntity target;
        final Vec3 at;
        final float yaw;
        final float damage;
        final boolean hadNoAi;
        int age;

        Prison(ServerLevel level, ServerPlayer caster, LivingEntity target, float damage) {
            this.level = level;
            this.caster = caster;
            this.target = target;
            this.at = target.position();
            this.yaw = target.getYRot();
            this.damage = damage;
            this.hadNoAi = target instanceof Mob mob && mob.isNoAi();
        }
    }

    private static final List<Prison> PRISONS = new ArrayList<>();
    private static final String NO_AI_TAG = "theabyss_ice_prison";

    @SubscribeEvent
    public static void onJoin(net.yezon.theabyss.platform.event.entity.EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Mob mob && mob.entityTags().contains(NO_AI_TAG)
            && PRISONS.stream().noneMatch(p -> p.target == mob)) {
            mob.setNoAi(false);
            mob.removeTag(NO_AI_TAG);
        }
    }

    private IcePrison() {
    }

    public static double damage(double upgrade) {
        return net.yezon.theabyss.ring.RingDamage.ICE_PRISON.at(upgrade);
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.level();
        LivingEntity target = Aim.creature(level, player, RANGE);
        if (target == null) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.secondary.no_target", Component.translatable("tooltip.theabyss.ring_of_freeze.secondary.ability")));
            return false;
        }
        for (Prison p : PRISONS) {
            if (p.target == target) {
                return false;
            }
        }
        Prison prison = new Prison(level, player, target, (float)damage(vars.ManaDamage));
        PRISONS.add(prison);
        target.addEffect(new MobEffectInstance(TheabyssModMobEffects.FROZEN.getDelegate(), HOLD, 0, false, false));
        if (target instanceof Mob mob && !mob.isNoAi()) {
            mob.setNoAi(true);
            mob.addTag(NO_AI_TAG);
        }
        SpellFxMessage.around(level, target.position(), SpellFxMessage.ICE, List.of(target.getId()), SpellFxMessage.points(RingVisuals.hand(player)), HOLD);
        RingVisuals.freezeBurst(level, player.position());
        RingVisuals.freezeBurst(level, target.position());
        int spikes = 5;
        for (int i = 0; i < spikes; i++) {
            double a = i * Math.PI * 2.0 / spikes + level.getRandom().nextDouble() * 0.4;
            double r = target.getBbWidth() * 0.5 + 0.9;
            Vec3 at = Aim.ground(level, target.position().add(Math.cos(a) * r, 1.5, Math.sin(a) * r), 4);
            NightbladeSpikeEntity spike = TheabyssModEntities.NIGHTBLADE_SPIKE.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (spike != null) {
                spike.setPos(at.x, at.y, at.z);
                spike.setupFrost(player, 2 + i, HOLD - 2 - i, (float)Math.toDegrees(a) + 90.0F);
                level.addFreshEntity(spike);
            }
        }
        RingFx.wave(level, target.position().add(0.0, 0.1, 0.0), 4.0F, 12, RingVisuals.ICE);
        RingVisuals.sound(level, target.position(), SoundEvents.AMETHYST_BLOCK_RESONATE, 1.6F, 0.5F);
        RingVisuals.sound(level, target.position(), SoundEvents.GLASS_PLACE, 1.2F, 0.6F);
        var freeze = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getValue(net.minecraft.resources.Identifier.parse("theabyss:spell_of_freeze"));
        if (freeze != null) {
            RingVisuals.sound(level, target.position(), freeze, 1.0F, 1.2F);
        }
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (Iterator<Prison> it = PRISONS.iterator(); it.hasNext(); ) {
            Prison p = it.next();
            p.age++;
            LivingEntity t = p.target;
            if (!t.isAlive() || t.level() != p.level) {
                release(p, false);
                it.remove();
                continue;
            }
            if (p.age >= HOLD) {
                release(p, true);
                it.remove();
                continue;
            }
            if (t.position().distanceToSqr(p.at) > 0.01) {
                t.teleportTo(p.at.x, p.at.y, p.at.z);
            }
            t.setDeltaMovement(0.0, Math.min(0.0, t.getDeltaMovement().y), 0.0);
            t.setYRot(p.yaw);
            t.setYHeadRot(p.yaw);
            t.setYBodyRot(p.yaw);
            t.needsSync = true;
        }
    }

    private static void release(Prison p, boolean burst) {
        LivingEntity t = p.target;
        if (t instanceof Mob mob && !p.hadNoAi) {
            mob.setNoAi(false);
            mob.removeTag(NO_AI_TAG);
        }
        t.removeEffect(TheabyssModMobEffects.FROZEN.getDelegate());
        Vec3 c = t.position().add(0.0, t.getBbHeight() * 0.5, 0.0);
        SpellFxMessage.around(p.level, c, SpellFxMessage.ICE_BURST, List.of(t.getId()), SpellFxMessage.points(c), 18);
        p.level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()), c.x, c.y, c.z, 40,
            t.getBbWidth() * 0.5, t.getBbHeight() * 0.4, t.getBbWidth() * 0.5, 0.15);
        RingVisuals.sound(p.level, c, SoundEvents.GLASS_BREAK, 1.6F, 0.7F);
        RingVisuals.sound(p.level, c, SoundEvents.AMETHYST_CLUSTER_BREAK, 1.2F, 0.6F);
        if (burst && t.isAlive()) {
            t.damageCooldownTime = 0;
            t.hurtServer(p.level, p.level.damageSources().indirectMagic(p.caster, p.caster), p.damage);
        }
    }

    public static boolean jailed(LivingEntity e) {
        return PRISONS.stream().anyMatch(p -> p.target == e);
    }
}
