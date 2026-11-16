package net.yezon.theabyss.entity;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;

public class PhantomAttackGoal extends Goal {
    private static final double HOVER_RADIUS = 7.0;
    private static final double MIASMA_RADIUS = 5.0;
    private static final float MIASMA_DAMAGE = 4.0F;
    private static final double REND_SPEED = 1.05;
    private static final float REND_DAMAGE = 6.0F;

    private final InfectedPhantomEntity phantom;
    private byte attack = InfectedPhantomEntity.NONE;
    private int tick;
    private int cooldown = 20;
    private int venomCooldown;
    private int miasmaCooldown = 40;
    private int rendCooldown = 60;
    private int repath;
    private int orbitDir = 1;
    private LivingEntity victim;
    private Vec3 rift;
    private Vec3 lungeDir = Vec3.ZERO;
    private boolean rendHit;

    public PhantomAttackGoal(InfectedPhantomEntity phantom) {
        this.phantom = phantom;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.phantom.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.attack != InfectedPhantomEntity.NONE || this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        if (this.attack != InfectedPhantomEntity.NONE) {
            this.end();
        }
        this.phantom.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (!(this.phantom.level() instanceof ServerLevel level)) {
            return;
        }
        this.cooldown--;
        this.venomCooldown--;
        this.miasmaCooldown--;
        this.rendCooldown--;
        if (this.attack != InfectedPhantomEntity.NONE) {
            this.tickAttack(level);
            return;
        }
        LivingEntity target = this.phantom.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        this.phantom.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (this.phantom.devAttack != InfectedPhantomEntity.NONE) {
            byte forced = this.phantom.devAttack;
            this.phantom.devAttack = InfectedPhantomEntity.NONE;
            if (forced == InfectedPhantomEntity.REND) {
                this.rift = this.findRift(level, target);
                if (this.rift != null) {
                    this.begin(level, forced, target);
                }
            } else {
                this.begin(level, forced, target);
            }
            return;
        }
        if (this.phantom.devHold) {
            this.phantom.getNavigation().stop();
            this.phantom.setDeltaMovement(this.phantom.getDeltaMovement().scale(0.5));
            return;
        }
        this.hover(target);
        if (this.cooldown <= 0 && this.phantom.getSensing().hasLineOfSight(target)) {
            this.choose(level, target);
        }
    }

    private void hover(LivingEntity target) {
        if (--this.repath > 0) {
            return;
        }
        this.repath = 8;
        if (this.phantom.getRandom().nextInt(8) == 0) {
            this.orbitDir = -this.orbitDir;
        }
        double angle = Math.atan2(this.phantom.getZ() - target.getZ(), this.phantom.getX() - target.getX()) + 0.45 * this.orbitDir;
        double x = target.getX() + Math.cos(angle) * HOVER_RADIUS;
        double z = target.getZ() + Math.sin(angle) * HOVER_RADIUS;
        double y = target.getY() + 2.0 + Math.sin(this.phantom.tickCount * 0.05) * 0.6;
        if (!this.phantom.getNavigation().moveTo(x, y, z, 1.2)) {
            this.phantom.getMoveControl().setWantedPosition(x, y, z, 1.2);
        }
    }

    private void choose(ServerLevel level, LivingEntity target) {
        double distance = this.phantom.distanceTo(target);
        if (distance < 4.5 && this.miasmaCooldown <= 0) {
            this.begin(level, InfectedPhantomEntity.MIASMA, target);
        } else if (distance > 5.5 && distance < 15.0 && this.rendCooldown <= 0 && this.phantom.getRandom().nextFloat() < 0.45F
            && (this.rift = this.findRift(level, target)) != null) {
            this.begin(level, InfectedPhantomEntity.REND, target);
        } else if (distance < 16.0 && this.venomCooldown <= 0) {
            this.begin(level, InfectedPhantomEntity.VENOM, target);
        }
    }

    private void begin(ServerLevel level, byte kind, LivingEntity target) {
        this.attack = kind;
        this.tick = 0;
        this.victim = target;
        this.rendHit = false;
        this.phantom.setAttack(kind);
        this.phantom.getNavigation().stop();
        Vec3 at = this.phantom.position();
        int id = this.phantom.getId();
        if (kind == InfectedPhantomEntity.VENOM) {
            this.venomCooldown = 50 + this.phantom.getRandom().nextInt(30);
            SpellFxMessage.around(level, at, SpellFxMessage.PHANTOM_CHARGE, List.of(id), List.of(), InfectedPhantomEntity.VENOM_FIRE);
            this.sound(level, SoundEvents.BREWING_STAND_BREW, 1.0F, 1.6F);
        } else if (kind == InfectedPhantomEntity.MIASMA) {
            this.miasmaCooldown = 150;
            SpellFxMessage.around(level, at, SpellFxMessage.PHANTOM_GATHER, List.of(id), List.of(MIASMA_RADIUS), InfectedPhantomEntity.MIASMA_BURST);
            this.sound(level, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:phantom_ambient")), 1.2F, 0.7F);
            this.sound(level, SoundEvents.BREWING_STAND_BREW, 1.0F, 0.6F);
        } else if (kind == InfectedPhantomEntity.REND) {
            this.rendCooldown = 160;
            Vec3 body = at.add(0.0, this.phantom.getBbHeight() * 0.5, 0.0);
            SpellFxMessage.around(level, at, this.rift, new SpellFxMessage(SpellFxMessage.PHANTOM_REND, List.of(id, target.getId()),
                SpellFxMessage.points(body, this.rift), InfectedPhantomEntity.REND_END));
            this.sound(level, SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0F, 1.2F);
        }
    }

    private void end() {
        this.attack = InfectedPhantomEntity.NONE;
        this.phantom.setAttack(InfectedPhantomEntity.NONE);
        this.cooldown = 26 + this.phantom.getRandom().nextInt(14);
        this.victim = null;
    }

    private void sound(ServerLevel level, SoundEvent sound, float volume, float pitch) {
        if (sound != null) {
            level.playSound(null, this.phantom.getX(), this.phantom.getY() + 1.0, this.phantom.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
        }
    }

    private void face(Vec3 at) {
        double dx = at.x - this.phantom.getX();
        double dz = at.z - this.phantom.getZ();
        float yaw = (float)(Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        this.phantom.setYRot(yaw);
        this.phantom.setYBodyRot(yaw);
        this.phantom.setYHeadRot(yaw);
    }

    private void tickAttack(ServerLevel level) {
        this.tick++;
        LivingEntity target = this.victim;
        boolean alive = target != null && target.isAlive();
        if (this.attack != InfectedPhantomEntity.REND || this.tick < InfectedPhantomEntity.REND_LUNGE) {
            this.phantom.getNavigation().stop();
            this.phantom.setDeltaMovement(this.phantom.getDeltaMovement().scale(0.6));
        }
        if (this.attack == InfectedPhantomEntity.VENOM) {
            if (alive) {
                this.face(target.position());
                this.phantom.getLookControl().setLookAt(target, 60.0F, 60.0F);
            }
            if (this.tick == InfectedPhantomEntity.VENOM_FIRE && alive) {
                Vec3 from = InfectedPhantomEntity.hand(this.phantom.position(), this.phantom.yBodyRot);
                Vec3 aim = target.getEyePosition().subtract(0.0, 0.35, 0.0);
                this.phantom.hazards.fire(level, from, aim.subtract(from));
                this.sound(level, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:phantom_attack")), 1.0F, 1.2F);
            }
            if (this.tick >= InfectedPhantomEntity.VENOM_END) {
                this.end();
            }
        } else if (this.attack == InfectedPhantomEntity.MIASMA) {
            if (alive) {
                this.face(target.position());
            }
            if (this.tick < InfectedPhantomEntity.MIASMA_BURST) {
                this.phantom.setDeltaMovement(this.phantom.getDeltaMovement().add(0.0, 0.012, 0.0));
            }
            if (this.tick == InfectedPhantomEntity.MIASMA_BURST) {
                this.burst(level);
            }
            if (this.tick >= InfectedPhantomEntity.MIASMA_END) {
                this.end();
            }
        } else if (this.attack == InfectedPhantomEntity.REND) {
            this.tickRend(level, target, alive);
        }
    }

    private void burst(ServerLevel level) {
        Vec3 centre = this.phantom.position().add(0.0, this.phantom.getBbHeight() * 0.5, 0.0);
        Vec3 ground = this.ground(level, this.phantom.position());
        AABB box = this.phantom.getBoundingBox().inflate(MIASMA_RADIUS + 1.0);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, this.phantom.hazards::victim)) {
            Vec3 body = e.getBoundingBox().getCenter();
            if (body.distanceTo(centre) > MIASMA_RADIUS + e.getBbWidth() * 0.5) {
                continue;
            }
            if (level.clip(new ClipContext(centre, body, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.phantom)).getType() != HitResult.Type.MISS) {
                continue;
            }
            if (e.hurtServer(level, this.phantom.damageSources().indirectMagic(this.phantom, this.phantom), MIASMA_DAMAGE)) {
                e.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1), this.phantom);
            }
            Vec3 push = new Vec3(body.x - centre.x, 0.0, body.z - centre.z);
            push = push.lengthSqr() < 1.0E-4 ? Vec3.ZERO : push.normalize().scale(0.9);
            e.push(push.x, 0.35, push.z);
            e.syncVelocity = true;
        }
        this.phantom.hazards.cloud(level, ground);
        SpellFxMessage.around(level, centre, SpellFxMessage.PHANTOM_MIASMA, List.of(this.phantom.getId()),
            List.of(centre.x, centre.y, centre.z, ground.x, ground.y, ground.z, MIASMA_RADIUS), PhantomHazards.CLOUD_TICKS);
        level.playSound(null, centre.x, centre.y, centre.z, SoundEvents.PUFFER_FISH_BLOW_UP, SoundSource.HOSTILE, 1.6F, 0.55F);
        level.playSound(null, centre.x, centre.y, centre.z, SoundEvents.BREWING_STAND_BREW, SoundSource.HOSTILE, 1.0F, 0.5F);
    }

    private Vec3 ground(ServerLevel level, Vec3 feet) {
        HitResult down = level.clip(new ClipContext(feet, feet.add(0.0, -8.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.phantom));
        return down.getType() == HitResult.Type.MISS ? feet : down.getLocation();
    }

    private void tickRend(ServerLevel level, LivingEntity target, boolean alive) {
        if (this.tick == InfectedPhantomEntity.REND_JUMP) {
            AABB box = this.phantom.getDimensions(this.phantom.getPose()).makeBoundingBox(this.rift);
            if (level.noCollision(this.phantom, box)) {
                this.phantom.teleportTo(this.rift.x, this.rift.y, this.rift.z);
                this.phantom.setDeltaMovement(Vec3.ZERO);
            }
        }
        if (this.tick < InfectedPhantomEntity.REND_LUNGE && alive) {
            if (this.tick >= InfectedPhantomEntity.REND_JUMP) {
                this.face(target.position());
            }
            this.phantom.getLookControl().setLookAt(target, 60.0F, 60.0F);
        }
        if (this.tick == InfectedPhantomEntity.REND_LUNGE) {
            Vec3 from = this.phantom.position().add(0.0, this.phantom.getBbHeight() * 0.5, 0.0);
            Vec3 to = alive ? target.getBoundingBox().getCenter() : from.add(this.phantom.getLookAngle());
            this.lungeDir = to.subtract(from).normalize();
            this.sound(level, SoundEvents.PHANTOM_SWOOP, 1.4F, 1.1F);
        }
        if (this.tick >= InfectedPhantomEntity.REND_LUNGE && this.tick < InfectedPhantomEntity.REND_END) {
            this.phantom.getNavigation().stop();
            this.phantom.setDeltaMovement(this.lungeDir.scale(REND_SPEED));
            if (alive && !this.rendHit && this.phantom.getBoundingBox().inflate(0.45).intersects(target.getBoundingBox())) {
                this.rendHit = true;
                if (target.hurtServer(level, this.phantom.damageSources().mobAttack(this.phantom), REND_DAMAGE)) {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1), this.phantom);
                }
                target.push(this.lungeDir.x * 0.8, 0.3, this.lungeDir.z * 0.8);
                target.syncVelocity = true;
                PhantomHazards.splash(level, target.getBoundingBox().getCenter(), -1);
            }
        }
        if (this.tick >= InfectedPhantomEntity.REND_END) {
            this.phantom.setDeltaMovement(this.lungeDir.scale(0.2));
            this.end();
        }
    }

    private Vec3 findRift(ServerLevel level, LivingEntity target) {
        Vec3 away = new Vec3(target.getX() - this.phantom.getX(), 0.0, target.getZ() - this.phantom.getZ());
        if (away.lengthSqr() < 1.0E-4) {
            return null;
        }
        away = away.normalize();
        for (float turn : new float[] {0.0F, 0.7F, -0.7F, 1.4F, -1.4F}) {
            Vec3 d = away.yRot(turn);
            Vec3 at = new Vec3(target.getX() + d.x * 3.2, target.getY() + 0.4, target.getZ() + d.z * 3.2);
            AABB box = this.phantom.getDimensions(this.phantom.getPose()).makeBoundingBox(at);
            if (!level.noCollision(this.phantom, box)) {
                continue;
            }
            Vec3 eye = at.add(0.0, 1.2, 0.0);
            if (level.clip(new ClipContext(eye, target.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.phantom)).getType()
                != HitResult.Type.MISS) {
                continue;
            }
            return at;
        }
        return null;
    }
}
