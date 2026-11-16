package net.yezon.theabyss.entity;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.util.RingMode;

public class CrystalSlashEntity extends Entity {
    public static final double SPEED = 1.0;
    public static final int MAX_AGE = 24;
    public static final int FADE = 6;
    private static final double HEIGHT = 0.5;
    private static final EntityDataAccessor<Integer> STOP = SynchedEntityData.defineId(CrystalSlashEntity.class, EntityDataSerializers.INT);

    private LivingEntity owner;
    private float damage = 6.0F;
    private final Set<Integer> hit = new HashSet<>();
    private int stoppedAt = -1;

    public CrystalSlashEntity(EntityType<? extends CrystalSlashEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void launch(LivingEntity caster, Vec3 velocity, float damage) {
        this.owner = caster;
        this.damage = damage;
        this.setDeltaMovement(velocity);
    }

    public static double halfWidth(float age) {
        return Math.min(3.0, 0.6 + 0.12 * age);
    }

    public int stopTick() {
        int synced = this.entityData.get(STOP);
        if (this.stoppedAt < 0) {
            return synced;
        }
        return synced < 0 ? this.stoppedAt : Math.min(synced, this.stoppedAt);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(STOP, -1);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 from = this.position();
        Vec3 velocity = this.getDeltaMovement();
        Vec3 to = from.add(velocity);
        double half = halfWidth(this.tickCount);
        Vec3 side = new Vec3(-velocity.z, 0.0, velocity.x);
        side = side.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
        if (this.level().isClientSide()) {
            if (this.stopTick() >= 0) {
                return;
            }
            for (double s = -half + this.random.nextDouble() * 1.25; s <= half; s += 1.25) {
                Vec3 at = from.add(side.scale(s));
                this.level().addParticle(TheabyssModParticleTypes.CRYSTAL_DROP.get(), at.x, at.y, at.z,
                    (this.random.nextDouble() * 2 - 1) * 0.1, (this.random.nextDouble() * 2 - 1) * 0.1, (this.random.nextDouble() * 2 - 1) * 0.1);
            }
            HitResult block = this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (block.getType() != HitResult.Type.MISS) {
                this.setPos(block.getLocation());
                this.stoppedAt = this.tickCount;
                return;
            }
            this.setPos(to);
            return;
        }
        ServerLevel level = (ServerLevel)this.level();
        int stop = this.entityData.get(STOP);
        if (stop >= 0) {
            if (this.tickCount >= stop + FADE) {
                this.discard();
            }
            return;
        }
        if (this.owner == null || this.tickCount > MAX_AGE) {
            this.discard();
            return;
        }
        AABB box = new AABB(from, to).inflate(Math.abs(side.x) * half + 0.3, HEIGHT, Math.abs(side.z) * half + 0.3);
        for (Entity e : level.getEntities(this, box, this::canHit)) {
            LivingEntity victim = (LivingEntity)e;
            if (this.hit.add(victim.getId())) {
                this.cut(level, victim);
            }
        }
        HitResult block = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (block.getType() != HitResult.Type.MISS) {
            this.setPos(block.getLocation());
            this.entityData.set(STOP, this.tickCount);
            return;
        }
        this.setPos(to);
    }

    private boolean canHit(Entity e) {
        return e instanceof LivingEntity living && living.isAlive() && e != this.owner && !e.isSpectator() && !RingMode.spares(this.owner, living);
    }

    private void cut(ServerLevel level, LivingEntity victim) {
        float before = victim.getHealth();
        DamageSource source = this.damageSources().indirectMagic(this, this.owner);
        if (victim.hurtServer(level, source, this.damage)) {
            float dealt = Math.max(0.0F, before - victim.getHealth());
            if (dealt > 0.0F && this.owner.isAlive()) {
                this.owner.heal(dealt * 0.15F);
            }
        }
        Vec3 feet = victim.position();
        level.playSound(null, feet.x, feet.y, feet.z, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.2F, 0.9F + this.random.nextFloat() * 0.3F);
        level.sendParticles(TheabyssModParticleTypes.CRYSTAL_DROP.get(), feet.x, feet.y, feet.z, 10, 0.0, 0.0, 0.0, 0.5);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
