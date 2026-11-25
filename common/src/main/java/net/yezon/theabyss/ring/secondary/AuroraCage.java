package net.yezon.theabyss.ring.secondary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.util.RingMode;

public final class AuroraCage {
    public static final double RANGE = 24.0;
    public static final double RADIUS = 5.0;
    public static final double HEIGHT = 5.0;
    public static final int LIFE = 160;
    public static final int RISE = 10;
    public static final int HIT_GAP = 10;
    public static final double WALL = 0.9;

    private AuroraCage() {
    }

    public static double damage(double upgrade) {
        return net.yezon.theabyss.ring.RingDamage.AURORA_CAGE.at(upgrade);
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.level();
        Vec3 centre = Aim.spot(level, player, RANGE);
        float hit = (float)damage(vars.ManaDamage);
        SpellFxMessage.around(level, centre, new SpellFxMessage(SpellFxMessage.AURORA_CAGE, List.of(player.getId()),
            List.of(centre.x, centre.y, centre.z, RADIUS, HEIGHT), LIFE));
        RingVisuals.sound(level, centre, SoundEvents.BEACON_POWER_SELECT, 1.6F, 1.5F);
        RingVisuals.sound(level, centre, SoundEvents.AMETHYST_BLOCK_RESONATE, 2.0F, 1.2F);
        Map<UUID, Long> last = new HashMap<>();
        for (int t = RISE; t < LIFE; t++) {
            int tick = t;
            TheabyssMod.queueServerWork(t, () -> tick(level, player, centre, hit, last, tick));
        }
        TheabyssMod.queueServerWork(LIFE, () -> RingVisuals.sound(level, centre, SoundEvents.BEACON_DEACTIVATE, 1.4F, 1.6F));
        return true;
    }

    private static void tick(ServerLevel level, ServerPlayer caster, Vec3 centre, float hit, Map<UUID, Long> last, int t) {
        boolean owner = !caster.isRemoved() && caster.level() == level;
        AABB box = new AABB(centre, centre).inflate(RADIUS + 2.0, 0.0, RADIUS + 2.0).expandTowards(0.0, HEIGHT + 1.0, 0.0).move(0.0, -1.0, 0.0);
        List<LivingEntity> inside = new ArrayList<>();
        List<LivingEntity> touching = new ArrayList<>();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (owner && RingMode.spares(caster, e)) {
                continue;
            }
            double dx = e.getX() - centre.x;
            double dz = e.getZ() - centre.z;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d <= RADIUS + WALL && d >= RADIUS - WALL) {
                touching.add(e);
            } else if (d < RADIUS) {
                inside.add(e);
            }
        }
        for (LivingEntity e : inside) {
            if (t % 10 == 0) {
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 2, false, false));
            }
        }
        DamageSource source = owner ? level.damageSources().indirectMagic(caster, caster) : level.damageSources().magic();
        for (LivingEntity e : touching) {
            if (!e.isAlive()) {
                continue;
            }
            Vec3 in = centre.subtract(e.position()).multiply(1.0, 0.0, 1.0);
            in = in.lengthSqr() < 1.0E-4 ? Vec3.ZERO : in.normalize().scale(0.7);
            e.setDeltaMovement(in.x, 0.25, in.z);
            e.needsSync = true;
            e.syncVelocity = true;
            Long before = last.get(e.getUUID());
            if (before == null || t - before >= HIT_GAP) {
                last.put(e.getUUID(), (long)t);
                e.damageCooldownTime = 0;
                e.hurtServer(level, source, hit);
                RingVisuals.sound(level, e.position(), SoundEvents.AMETHYST_BLOCK_HIT, 1.0F, 1.6F);
            }
        }
    }
}
