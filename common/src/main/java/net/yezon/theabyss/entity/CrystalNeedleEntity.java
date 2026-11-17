package net.yezon.theabyss.entity;


import net.minecraft.nbt.CompoundTag;
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
import net.yezon.theabyss.compat.ValueInput;
import net.yezon.theabyss.compat.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.util.RingMode;

public class CrystalNeedleEntity extends Entity {
    public static final double SPEED = 2.5;
    private static final int MAX_AGE = 40;
    public static final int FADE = 5;
    private static final EntityDataAccessor<Integer> STOP = SynchedEntityData.defineId(CrystalNeedleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ROLL = SynchedEntityData.defineId(CrystalNeedleEntity.class, EntityDataSerializers.FLOAT);
    private static long lastSoundTick = -1;

    private LivingEntity owner;
    private float damage = 2.0F;
    private int stoppedAt = -1;

    public CrystalNeedleEntity(EntityType<? extends CrystalNeedleEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void launch(LivingEntity caster, Vec3 velocity, float damage, float roll) {
        this.owner = caster;
        this.damage = damage;
        this.setDeltaMovement(velocity);
        this.entityData.set(ROLL, roll);
    }

    public float roll() {
        return this.entityData.get(ROLL);
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
        builder.define(ROLL, 0.0F);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 from = this.position();
        Vec3 velocity = this.getDeltaMovement();
        Vec3 to = from.add(velocity);
        if (this.level().isClientSide()) {
            if (this.stopTick() >= 0) {
                return;
            }
            if (this.random.nextInt(5) < 2) {
                double dx = (this.random.nextDouble() * 2 - 1) * 0.05;
                double dy = (this.random.nextDouble() * 2 - 1) * 0.05;
                double dz = (this.random.nextDouble() * 2 - 1) * 0.05;
                this.level().addParticle(TheabyssModParticleTypes.CRYSTAL_DROP.get(), from.x + dx, from.y + dy, from.z + dz, dx, dy, dz);
            }
            HitResult block = this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            Vec3 end = block.getType() == HitResult.Type.MISS ? to : block.getLocation();
            Vec3 at = this.firstCrossed(from, end, e -> e instanceof LivingEntity living && living.isAlive() && e.isPickable() && !e.isSpectator()
                && !(e instanceof net.minecraft.world.entity.player.Player));
            if (at != null || block.getType() != HitResult.Type.MISS) {
                this.setPos(at != null ? at : end);
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
        HitResult block = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 end = block.getType() == HitResult.Type.MISS ? to : block.getLocation();
        LivingEntity victim = null;
        Vec3 victimAt = this.firstCrossed(from, end, this::canHit);
        if (victimAt != null) {
            victim = this.lastCrossed;
        }
        if (victim != null) {
            this.strike(level, victim, victimAt);
            return;
        }
        if (block.getType() != HitResult.Type.MISS) {
            this.shatter(level, block.getLocation());
            return;
        }
        this.setPos(to);
    }

    private LivingEntity lastCrossed;

    private Vec3 firstCrossed(Vec3 from, Vec3 end, java.util.function.Predicate<Entity> filter) {
        Vec3 found = null;
        double best = Double.MAX_VALUE;
        this.lastCrossed = null;
        for (Entity e : this.level().getEntities(this, new AABB(from, end).inflate(1.0), filter)) {
            var crossing = e.getBoundingBox().inflate(0.3).clip(from, end);
            if (crossing.isPresent() && from.distanceToSqr(crossing.get()) < best) {
                best = from.distanceToSqr(crossing.get());
                found = crossing.get();
                this.lastCrossed = (LivingEntity)e;
            }
        }
        return found;
    }

    private boolean canHit(Entity e) {
        return e instanceof LivingEntity living && living.isAlive() && e != this.owner && e.isPickable() && !e.isSpectator()
            && !RingMode.spares(this.owner, living);
    }

    private void strike(ServerLevel level, LivingEntity victim, Vec3 at) {
        float before = victim.getHealth();
        victim.invulnerableTime = 0;
        DamageSource source = this.damageSources().indirectMagic(this, this.owner);
        if (victim.hurt(source, this.damage)) {
            float dealt = Math.max(0.0F, before - victim.getHealth());
            if (dealt > 0.0F && this.owner.isAlive()) {
                this.owner.heal(dealt * 0.25F);
            }
        }
        this.shatter(level, at);
    }

    private void shatter(ServerLevel level, Vec3 at) {
        if (lastSoundTick != level.getGameTime()) {
            lastSoundTick = level.getGameTime();
            level.playSound(null, at.x, at.y, at.z, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.6F, 1.2F + this.random.nextFloat() * 0.3F);
        }
        level.sendParticles(TheabyssModParticleTypes.CRYSTAL_DROP.get(), at.x, at.y, at.z, 3, 0.1, 0.1, 0.1, 0.18);
        this.setPos(at);
        this.entityData.set(STOP, this.tickCount);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!(this.level() instanceof ServerLevel level)) {
            return false;
        }
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
    public void readAdditionalSaveData(CompoundTag inputTag) {
        ValueInput input = ValueInput.of(inputTag, this.registryAccess());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag outputTag) {
        ValueOutput output = ValueOutput.of(outputTag, this.registryAccess());
    }
}
