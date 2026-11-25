package net.yezon.theabyss.ring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.secondary.Aim;
import net.yezon.theabyss.util.RingMode;

public final class InvertedSky {
    public static final double COST = 70.0;
    public static final int COOLDOWN = 700;
    public static final double RANGE = 32.0;
    public static final double RADIUS = 6.0;
    public static final int OPEN = 10;
    public static final int CLOSE = 80;
    public static final int END = 92;
    public static final int STAR_EVERY = 4;
    public static final int STAR_FLIGHT = 5;
    public static final double LIFT = 5.0;
    public static final int CYAN = 0x5AF0FF;
    public static final int PALE = 0xE0FFFF;
    public static final int AURORA = 0x6BFFC8;
    public static final int VIOLET = 0xB06BFF;
    public static final int DEEP = 0x1A6E8C;

    private InvertedSky() {
    }

    public static void cast(ServerLevel level, LivingEntity caster, TheabyssModVariables.PlayerVariables vars) {
        Vec3 centre = Aim.spot(level, caster, RANGE);
        State state = new State(level, caster, centre, (float)RingDamage.SKY_STAR.at(vars.ManaDamage));
        SpellFxMessage.around(level, centre, new SpellFxMessage(SpellFxMessage.INVERTED_SKY, List.of(caster.getId()),
            List.of(centre.x, centre.y, centre.z, RADIUS), END));
        RingVisuals.sound(level, centre, SoundEvents.BEACON_ACTIVATE, 2.0F, 1.4F);
        RingVisuals.sound(level, centre, SoundEvents.AMETHYST_BLOCK_RESONATE, 2.5F, 0.6F);
        RingVisuals.sound(level, centre, SoundEvents.PORTAL_TRIGGER, 0.6F, 1.8F);
        for (int t = 0; t <= END; t++) {
            int tick = t;
            TheabyssMod.queueServerWork(t, () -> state.tick(tick));
        }
    }

    private static final class State {
        private final ServerLevel level;
        private final LivingEntity caster;
        private final Vec3 centre;
        private final float damage;
        private final Map<UUID, Double> lifted = new HashMap<>();
        private final RandomSource random;

        State(ServerLevel level, LivingEntity caster, Vec3 centre, float damage) {
            this.level = level;
            this.caster = caster;
            this.centre = centre;
            this.damage = damage;
            this.random = RandomSource.create(level.getRandom().nextLong());
        }

        void tick(int t) {
            if (t == CLOSE) {
                this.release();
                RingVisuals.sound(this.level, this.centre, SoundEvents.BEACON_DEACTIVATE, 2.0F, 1.3F);
                RingVisuals.sound(this.level, this.centre, SoundEvents.AMETHYST_CLUSTER_BREAK, 1.6F, 0.6F);
                return;
            }
            if (t < OPEN || t > CLOSE) {
                return;
            }
            List<LivingEntity> victims = this.victims();
            for (LivingEntity victim : victims) {
                double top = this.lifted.computeIfAbsent(victim.getUUID(), k -> this.centre.y + LIFT + this.random.nextDouble() * 2.0);
                double bob = Math.sin((t + victim.getId() * 7) * 0.18) * 0.03;
                double dy = Mth.clamp((top - victim.getY()) * 0.12, -0.3, 0.35) + 0.08 + bob;
                Vec3 v = victim.getDeltaMovement();
                victim.setDeltaMovement(v.x * 0.6, dy, v.z * 0.6);
                victim.resetFallDistance();
                victim.needsSync = true;
                victim.syncVelocity = true;
            }
            if (t >= OPEN + 6 && t <= CLOSE - 6 && (t - OPEN) % STAR_EVERY == 0) {
                this.star(victims);
            }
        }

        private List<LivingEntity> victims() {
            AABB box = new AABB(this.centre, this.centre).inflate(RADIUS, 0.0, RADIUS).expandTowards(0.0, LIFT + 6.0, 0.0).move(0.0, -1.0, 0.0);
            boolean owner = !this.caster.isRemoved() && this.caster.level() == this.level;
            List<LivingEntity> out = new ArrayList<>();
            for (LivingEntity e : this.level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != this.caster)) {
                if (owner && RingMode.spares(this.caster, e)) {
                    continue;
                }
                double dx = e.getX() - this.centre.x;
                double dz = e.getZ() - this.centre.z;
                if (dx * dx + dz * dz <= RADIUS * RADIUS) {
                    out.add(e);
                }
            }
            return out;
        }

        private void star(List<LivingEntity> victims) {
            LivingEntity target = victims.isEmpty() ? null : victims.get(this.random.nextInt(victims.size()));
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double d = Math.sqrt(this.random.nextDouble()) * RADIUS * 0.7;
            Vec3 from = this.centre.add(Math.cos(a) * d, 0.1, Math.sin(a) * d);
            Vec3 to = target == null ? from.add((this.random.nextDouble() - 0.5) * 3.0, 6.0 + this.random.nextDouble() * 4.0, (this.random.nextDouble() - 0.5) * 3.0)
                : target.getBoundingBox().getCenter();
            SpellFxMessage.around(this.level, from, new SpellFxMessage(SpellFxMessage.SKY_STAR, List.of(), SpellFxMessage.points(from, to), STAR_FLIGHT));
            RingVisuals.sound(this.level, from, SoundEvents.AMETHYST_BLOCK_CHIME, 1.4F, 1.4F + this.random.nextFloat() * 0.5F);
            if (target == null) {
                return;
            }
            LivingEntity aimed = target;
            TheabyssMod.queueServerWork(STAR_FLIGHT, () -> {
                if (!aimed.isAlive() || aimed.level() != this.level) {
                    return;
                }
                boolean owner = !this.caster.isRemoved() && this.caster.level() == this.level;
                DamageSource source = owner ? this.level.damageSources().indirectMagic(this.caster, this.caster) : this.level.damageSources().magic();
                aimed.damageCooldownTime = 0;
                aimed.hurtServer(this.level, source, this.damage);
                RingVisuals.sound(this.level, aimed.position(), SoundEvents.FIREWORK_ROCKET_TWINKLE, 1.0F, 1.6F + this.random.nextFloat() * 0.3F);
            });
        }

        private void release() {
            for (LivingEntity victim : this.victims()) {
                if (this.lifted.containsKey(victim.getUUID())) {
                    Vec3 v = victim.getDeltaMovement();
                    victim.setDeltaMovement(v.x * 0.3, -0.25, v.z * 0.3);
                    victim.needsSync = true;
                    victim.syncVelocity = true;
                }
            }
            this.lifted.clear();
        }
    }
}
