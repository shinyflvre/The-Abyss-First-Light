package net.yezon.theabyss.boss.nightblade;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.init.TheabyssModSounds;
import net.yezon.theabyss.network.NightbladeStormMessage;

final class NightbladeStorm {
    enum Kind {
        HUNT,
        MINEFIELD
    }

    static final double RADIUS = 1.8;
    private static final float RED_DAMAGE = 1.25F;
    private static final ParticleOptions RED_SPARK = new DustParticleOptions(0xFF5959, 0.9F);
    private static final double FIELD_RADIUS = 13.0;

    private record Circle(Vec3 at, int strikeAt) {
    }

    private final List<Circle> circles = new ArrayList<>();
    private final Kind kind;
    private final int total;
    private final int interval;
    private final int delay;
    private final Vec3 field;
    private final boolean red;
    private int placed;
    private int age;

    NightbladeStorm(Kind kind, int phase, Vec3 field) {
        this.kind = kind;
        this.field = field;
        this.red = phase == 3;
        if (kind == Kind.HUNT) {
            this.total = phase == 1 ? 6 : phase == 2 ? 8 : 10;
            this.interval = 14;
            this.delay = 40;
        } else {
            this.total = phase == 1 ? 14 : phase == 2 ? 18 : 22;
            this.interval = phase == 3 ? 5 : 6;
            this.delay = 50;
        }
    }

    boolean tick(NightbladeCombat c, ServerLevel level, LivingEntity target) {
        if (this.placed < this.total && this.age % this.interval == 0) {
            this.placed++;
            Vec3 at = this.kind == Kind.HUNT ? this.hunt(c, target) : this.mine(c, target);
            if (at != null) {
                this.place(c, level, at);
            }
        }
        for (int i = 0; i < this.circles.size(); i++) {
            Circle circle = this.circles.get(i);
            if (this.age == circle.strikeAt) {
                this.strike(c, level, circle.at);
            } else if (this.age < circle.strikeAt && (circle.strikeAt - this.age) % 5 == 0) {
                c.particles(level, this.spark(), circle.at.x, circle.at.y + 0.2, circle.at.z, 4, RADIUS * 0.5, 0.05, RADIUS * 0.5, 0.0);
            }
        }
        this.age++;
        return this.placed >= this.total && this.age > (this.circles.isEmpty() ? 0 : this.circles.get(this.circles.size() - 1).strikeAt) + 10;
    }

    private Vec3 hunt(NightbladeCombat c, LivingEntity target) {
        Vec3 aim = this.placed % 2 == 1 ? c.predict(target, this.delay) : target.position();
        double angle = c.boss.getRandom().nextDouble() * Math.PI * 2.0;
        double off = c.boss.getRandom().nextDouble() * 1.2;
        Vec3 spot = this.ground(c, aim.add(Math.cos(angle) * off, 0.0, Math.sin(angle) * off));
        return spot != null ? spot : this.ground(c, target.position());
    }

    private Vec3 mine(NightbladeCombat c, LivingEntity target) {
        boolean near = this.placed % 4 == 0;
        for (int attempt = 0; attempt < 12; attempt++) {
            Vec3 centre = near ? target.position() : this.field;
            double reach = near ? 3.0 : FIELD_RADIUS;
            double angle = c.boss.getRandom().nextDouble() * Math.PI * 2.0;
            double distance = Math.sqrt(c.boss.getRandom().nextDouble()) * reach;
            Vec3 spot = this.ground(c, new Vec3(centre.x + Math.cos(angle) * distance, target.getY(), centre.z + Math.sin(angle) * distance));
            if (spot != null && this.free(spot)) {
                return spot;
            }
        }
        return null;
    }

    private Vec3 ground(NightbladeCombat c, Vec3 at) {
        double y = c.groundY(at.x, at.y, at.z);
        return Double.isNaN(y) ? null : new Vec3(at.x, y, at.z);
    }

    private boolean free(Vec3 spot) {
        for (Circle circle : this.circles) {
            if (circle.strikeAt > this.age && circle.at.distanceToSqr(spot) < 3.6 * 3.6) {
                return false;
            }
        }
        return true;
    }

    private void place(NightbladeCombat c, ServerLevel level, Vec3 spot) {
        this.circles.add(new Circle(spot, this.age + this.delay));
        NightbladeStormMessage.around(level, c.boss.getId(), spot, this.delay, this.red);
        level.playSound(null, spot.x, spot.y, spot.z, TheabyssModSounds.NIGHTBLADE_STORM_CIRCLE.get(), SoundSource.HOSTILE, 1.6F, 0.9F + c.boss.getRandom().nextFloat() * 0.2F);
    }

    private ParticleOptions spark() {
        return this.red ? RED_SPARK : ParticleTypes.ELECTRIC_SPARK;
    }

    private void strike(NightbladeCombat c, ServerLevel level, Vec3 spot) {
        level.playSound(null, spot.x, spot.y, spot.z, TheabyssModSounds.NIGHTBLADE_STORM_STRIKE.get(), SoundSource.HOSTILE, 3.0F, 0.9F + c.boss.getRandom().nextFloat() * 0.2F);
        c.particles(level, this.spark(), spot.x, spot.y + 0.3, spot.z, 30, 0.6, 0.4, 0.6, 0.25);
        c.particles(level, ParticleTypes.LARGE_SMOKE, spot.x, spot.y + 0.3, spot.z, 6, 0.5, 0.2, 0.5, 0.02);
        c.shake(level, 28.0, 0.9F, 0.35F);
        AABB area = new AABB(spot.x - RADIUS, spot.y - 1.0, spot.z - RADIUS, spot.x + RADIUS, spot.y + 3.0, spot.z + RADIUS);
        for (LivingEntity victim : c.enemiesIn(area)) {
            if (victim.position().subtract(spot).horizontalDistance() <= RADIUS + victim.getBbWidth() * 0.5) {
                victim.hurtServer(level, c.magicDamage(), c.scaled(9.0F) * (this.red ? RED_DAMAGE : 1.0F));
                victim.addEffect(new MobEffectInstance(TheabyssModMobEffects.WEAK_ELECTRICITY.getDelegate(), 60, 0, false, false));
            }
        }
    }
}
