package net.yezon.theabyss.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;

final class PhantomHazards {
    static final double BOLT_SPEED = 1.15;
    static final int BOLT_LIFE = 36;
    static final float BOLT_DAMAGE = 4.0F;
    static final int CLOUD_TICKS = 70;
    static final double CLOUD_RADIUS = 4.0;

    private static final class Bolt {
        final int id;
        final Vec3 velocity;
        Vec3 pos;
        int age;

        Bolt(int id, Vec3 pos, Vec3 velocity) {
            this.id = id;
            this.pos = pos;
            this.velocity = velocity;
        }
    }

    private static final class Cloud {
        final Vec3 at;
        int age;

        Cloud(Vec3 at) {
            this.at = at;
        }
    }

    private final InfectedPhantomEntity phantom;
    private final List<Bolt> bolts = new ArrayList<>();
    private final List<Cloud> clouds = new ArrayList<>();
    private int next;

    PhantomHazards(InfectedPhantomEntity phantom) {
        this.phantom = phantom;
    }

    boolean victim(LivingEntity e) {
        if (e == this.phantom || !e.isAlive() || e instanceof InfectedPhantomEntity) {
            return false;
        }
        if (e instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return e == this.phantom.getTarget();
    }

    void fire(ServerLevel level, Vec3 from, Vec3 direction) {
        int id = this.phantom.getId() * 64 + (this.next++ & 63);
        Bolt bolt = new Bolt(id, from, direction.normalize().scale(BOLT_SPEED));
        this.bolts.add(bolt);
        SpellFxMessage.around(level, from, SpellFxMessage.PHANTOM_BOLT, List.of(id), SpellFxMessage.points(from, bolt.velocity), BOLT_LIFE);
    }

    void cloud(ServerLevel level, Vec3 at) {
        this.clouds.add(new Cloud(at));
    }

    static void splash(ServerLevel level, Vec3 at, int id) {
        SpellFxMessage.around(level, at, SpellFxMessage.PHANTOM_SPLASH, List.of(id), SpellFxMessage.points(at), 0);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.HOSTILE, 1.0F, 1.3F);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.HOSTILE, 0.8F, 0.9F);
    }

    void tick(ServerLevel level) {
        Iterator<Bolt> bolts = this.bolts.iterator();
        while (bolts.hasNext()) {
            Bolt bolt = bolts.next();
            Vec3 from = bolt.pos;
            Vec3 to = from.add(bolt.velocity);
            BlockHitResult block = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.phantom));
            Vec3 end = block.getType() == HitResult.Type.MISS ? to : block.getLocation();
            LivingEntity hit = null;
            Vec3 hitAt = null;
            double best = Double.MAX_VALUE;
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(from, end).inflate(0.8), this::victim)) {
                AABB box = e.getBoundingBox().inflate(0.25);
                Optional<Vec3> clip = box.contains(from) ? Optional.of(from) : box.clip(from, end);
                if (clip.isPresent() && from.distanceToSqr(clip.get()) < best) {
                    best = from.distanceToSqr(clip.get());
                    hit = e;
                    hitAt = clip.get();
                }
            }
            if (hit != null) {
                if (hit.hurt(this.phantom.damageSources().indirectMagic(this.phantom, this.phantom), BOLT_DAMAGE)) {
                    hit.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0), this.phantom);
                }
                splash(level, hitAt, bolt.id);
                bolts.remove();
                continue;
            }
            if (block.getType() != HitResult.Type.MISS) {
                splash(level, end, bolt.id);
                bolts.remove();
                continue;
            }
            bolt.pos = to;
            if (++bolt.age >= BOLT_LIFE) {
                SpellFxMessage.around(level, to, SpellFxMessage.PHANTOM_SPLASH, List.of(bolt.id), SpellFxMessage.points(to), 1);
                bolts.remove();
            }
        }
        Iterator<Cloud> clouds = this.clouds.iterator();
        while (clouds.hasNext()) {
            Cloud cloud = clouds.next();
            if (++cloud.age % 10 == 0) {
                AABB box = new AABB(cloud.at, cloud.at).inflate(CLOUD_RADIUS, 2.5, CLOUD_RADIUS);
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, this::victim)) {
                    double dx = e.getX() - cloud.at.x;
                    double dz = e.getZ() - cloud.at.z;
                    MobEffectInstance poison = e.getEffect(MobEffects.POISON);
                    if (dx * dx + dz * dz <= CLOUD_RADIUS * CLOUD_RADIUS && (poison == null || poison.getDuration() < 15)) {
                        e.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0), this.phantom);
                    }
                }
            }
            if (cloud.age >= CLOUD_TICKS) {
                clouds.remove();
            }
        }
    }
}
