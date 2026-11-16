package net.yezon.theabyss.ring.secondary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.NightbladeShakeMessage;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.RingFx;
import net.yezon.theabyss.util.FallGuard;
import net.yezon.theabyss.util.RingMode;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class VoidRift {
    public static final double RANGE = 30.0;
    public static final double HOVER = 1.6;
    public static final int FORM = 10;
    public static final int PULL = 40;
    public static final double PULL_RADIUS = 9.0;
    public static final double BLAST_RADIUS = 5.0;

    private static final List<Rift> RIFTS = new ArrayList<>();

    private record Rift(ServerLevel level, ServerPlayer caster, Vec3 centre, float damage, int[] age) {
    }

    private VoidRift() {
    }

    public static double damage(double upgrade) {
        return net.yezon.theabyss.ring.RingDamage.VOID_RIFT.at(upgrade);
    }

    public static int end() {
        return FORM + PULL;
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.level();
        Vec3 centre = Aim.spot(level, player, RANGE).add(0.0, HOVER, 0.0);
        RIFTS.add(new Rift(level, player, centre, (float)damage(vars.ManaDamage), new int[1]));
        SpellFxMessage.around(level, centre, SpellFxMessage.VOID, List.of(player.getId()), SpellFxMessage.points(centre), end());
        RingVisuals.sound(level, centre, net.yezon.theabyss.init.TheabyssModSounds.SPELL_OF_VOID_CHARGE.get(), 1.0F, 0.5F);
        RingVisuals.sound(level, centre, SoundEvents.END_PORTAL_SPAWN, 0.3F, 1.6F);
        RingVisuals.sound(level, player.position(), SoundEvents.WITHER_SHOOT, 0.35F, 0.6F);
        RingVisuals.sound(level, centre, net.yezon.theabyss.init.TheabyssModSounds.SPELL_OF_BLACKHOLE_SPAWN.get(), 2.0F, RingVisuals.castPitch(player.getRandom()));
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        for (Iterator<Rift> it = RIFTS.iterator(); it.hasNext(); ) {
            Rift rift = it.next();
            int age = ++rift.age()[0];
            if (age >= end()) {
                collapse(rift);
                it.remove();
            } else if (age >= FORM / 2) {
                pull(rift, age);
            }
        }
    }

    private static void pull(Rift rift, int age) {
        Vec3 c = rift.centre();
        double grip = Math.min(1.0, (age - FORM / 2) / 10.0);
        AABB box = new AABB(c, c).inflate(PULL_RADIUS);
        for (Entity e : rift.level().getEntities((Entity)null, box, e -> e.isAlive() && e != rift.caster() && !e.isSpectator()
            && (e instanceof ItemEntity || e instanceof LivingEntity && !RingMode.spares(rift.caster(), e)))) {
            Vec3 at = e.position().add(0.0, e.getBbHeight() * 0.5, 0.0);
            Vec3 to = c.subtract(at);
            double d = to.length();
            if (d > PULL_RADIUS || d < 1.0E-3) {
                continue;
            }
            Vec3 dir = to.scale(1.0 / d);
            Vec3 v = e.getDeltaMovement();
            if (d < 1.4) {
                e.setDeltaMovement(to.scale(0.5));
            } else {
                double strength = grip * (0.09 + 0.16 * (1.0 - d / PULL_RADIUS));
                Vec3 swirl = new Vec3(-dir.z, 0.0, dir.x).scale(strength * 0.35);
                e.setDeltaMovement(v.scale(0.78).add(dir.scale(strength * 2.2)).add(swirl).add(0.0, 0.04, 0.0));
            }
            e.resetFallDistance();
            e.needsSync = true;
            if (e instanceof LivingEntity living) {
                FallGuard.protect(living, 60);
            }
            e.syncVelocity = true;
        }
        if (age % 10 == 0) {
            RingVisuals.sound(rift.level(), c, SoundEvents.RESPAWN_ANCHOR_AMBIENT, 1.6F, 0.5F + age * 0.01F);
        }
        if (age == end() - 8) {
            RingVisuals.sound(rift.level(), c, SoundEvents.BEACON_DEACTIVATE, 2.0F, 0.5F);
        }
    }

    private static void collapse(Rift rift) {
        ServerLevel level = rift.level();
        Vec3 c = rift.centre();
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(BLAST_RADIUS),
            e -> e.isAlive() && e != rift.caster() && !RingMode.spares(rift.caster(), e))) {
            Vec3 at = victim.position().add(0.0, victim.getBbHeight() * 0.5, 0.0);
            double d = at.distanceTo(c);
            if (d > BLAST_RADIUS) {
                continue;
            }
            victim.damageCooldownTime = 0;
            victim.hurtServer(level, level.damageSources().indirectMagic(rift.caster(), rift.caster()), rift.damage());
            Vec3 out = at.subtract(c);
            out = out.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 1.0, 0.0) : out.normalize();
            double push = 1.4 * (1.0 - d / BLAST_RADIUS) + 0.5;
            victim.setDeltaMovement(out.x * push, 0.45 + Math.max(0.0, out.y) * 0.5, out.z * push);
            victim.needsSync = true;
            victim.syncVelocity = true;
            FallGuard.protect(victim, 80);
        }
        RingFx.implode(level, c, 6.0F, 10, RingVisuals.BLACKSTRIKE);
        TheabyssMod.queueServerWork(4, () -> RingFx.wave(level, Aim.ground(level, c, 8).add(0.0, 0.1, 0.0), 10.0F, 20, RingVisuals.BLACKSTRIKE));
        NightbladeShakeMessage.around(level, c, 28.0, 1.2F, 0.5F);
        RingVisuals.sound(level, c, net.yezon.theabyss.init.TheabyssModSounds.SPELL_OF_VOID_BOOM.get(), 1.0F, 0.6F);
        RingVisuals.sound(level, c, net.yezon.theabyss.init.TheabyssModSounds.SPELL_OF_VOID_EXPLODE.get(), 1.0F, 0.5F);
        RingVisuals.sound(level, c, net.yezon.theabyss.init.TheabyssModSounds.SPELL_OF_VOID_DEPLETE.get(), 1.0F, 0.6F);
    }
}
