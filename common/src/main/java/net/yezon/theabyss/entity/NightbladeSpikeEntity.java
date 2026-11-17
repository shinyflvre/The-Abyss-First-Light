package net.yezon.theabyss.entity;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.compat.ValueInput;
import net.yezon.theabyss.compat.ValueOutput;
import net.minecraft.world.phys.AABB;

public class NightbladeSpikeEntity extends Entity {
    public static final EntityDataAccessor<Integer> WARMUP = SynchedEntityData.defineId(NightbladeSpikeEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> RED = SynchedEntityData.defineId(NightbladeSpikeEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> FROST = SynchedEntityData.defineId(NightbladeSpikeEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> GOLEM = SynchedEntityData.defineId(NightbladeSpikeEntity.class, EntityDataSerializers.BOOLEAN);
    public static final int LIFE = 22;
    public static final EntityDataAccessor<Integer> LIFE_TICKS = SynchedEntityData.defineId(NightbladeSpikeEntity.class, EntityDataSerializers.INT);
    private static final double RADIUS = 0.8;

    private LivingEntity owner;
    private float damage = 6.0F;
    private boolean knockUp = true;

    public NightbladeSpikeEntity(EntityType<? extends NightbladeSpikeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void setup(LivingEntity owner, int warmup, float damage, boolean red, float lineYaw) {
        this.owner = owner;
        this.damage = damage;
        this.entityData.set(WARMUP, warmup);
        this.entityData.set(RED, red);
        this.setYRot(lineYaw);
    }

    public void setupRing(LivingEntity caster, int warmup, float damage, float lineYaw) {
        this.setup(caster, warmup, damage, false, lineYaw);
        this.knockUp = false;
    }

    public void setupFrost(LivingEntity caster, int warmup, int hold, float lineYaw) {
        this.setup(caster, warmup, 0.0F, false, lineYaw);
        this.knockUp = false;
        this.entityData.set(LIFE_TICKS, hold);
        this.entityData.set(FROST, true);
    }

    public void setupWall(LivingEntity caster, int warmup, int hold, float lineYaw) {
        this.setup(caster, warmup, 0.0F, false, lineYaw);
        this.knockUp = false;
        this.entityData.set(LIFE_TICKS, hold);
    }

    public void setupGolem(LivingEntity golem, int warmup, float damage, float lineYaw) {
        this.setup(golem, warmup, damage, false, lineYaw);
        this.entityData.set(GOLEM, true);
    }

    public boolean isGolem() {
        return this.entityData.get(GOLEM);
    }

    public int life() {
        return this.entityData.get(LIFE_TICKS);
    }

    public int warmup() {
        return this.entityData.get(WARMUP);
    }

    public boolean isFrost() {
        return this.entityData.get(FROST);
    }

    public boolean isRed() {
        return this.entityData.get(RED);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(WARMUP, 10);
        builder.define(LIFE_TICKS, LIFE);
        builder.define(RED, false);
        builder.define(FROST, false);
        builder.define(GOLEM, false);
    }

    @Override
    public void tick() {
        super.tick();
        int age = this.tickCount - this.warmup();
        if (this.level().isClientSide()) {
            if (age < 0 && this.tickCount % 3 == 0) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX() + (this.random.nextDouble() - 0.5) * 0.8, this.getY() + 0.05,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.8, 0.0, 0.02, 0.0);
            }
            return;
        }
        ServerLevel level = (ServerLevel)this.level();
        if (age == 0) {
            this.burst(level);
        }
        if (age >= this.life()) {
            if (this.isFrost()) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.PACKED_ICE.defaultBlockState()), this.getX(),
                    this.getY() + 0.8, this.getZ(), 18, 0.25, 0.6, 0.25, 0.1);
                level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.6F, 1.2F + this.random.nextFloat() * 0.3F);
            }
            this.discard();
        }
    }

    private void burst(ServerLevel level) {
        if (this.isFrost()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.PACKED_ICE.defaultBlockState()), this.getX(),
                this.getY() + 0.2, this.getZ(), 16, 0.3, 0.2, 0.3, 0.15);
            level.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 1.0, this.getZ(), 8, 0.2, 0.6, 0.2, 0.02);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.PLAYERS, 0.9F, 1.3F + this.random.nextFloat() * 0.3F);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.7F, 0.6F);
            return;
        }
        BlockState ground = level.getBlockState(this.blockPosition().below());
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), this.getX(), this.getY() + 0.1, this.getZ(), 14, 0.4, 0.1, 0.4, 0.15);
        }
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 1.0F, 0.5F + this.random.nextFloat() * 0.2F);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.HOSTILE, 0.8F, 0.6F);
        AABB area = new AABB(this.getX() - RADIUS, this.getY() - 0.5, this.getZ() - RADIUS, this.getX() + RADIUS, this.getY() + 2.2, this.getZ() + RADIUS);
        if (this.damage <= 0.0F) {
            return;
        }
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area, this::isEnemy)) {
            DamageSource source = this.isGolem() && this.owner != null ? this.damageSources().mobAttack(this.owner) : this.damageSources().indirectMagic(this, this.owner);
            if (victim.hurt(source, this.damage) && this.knockUp) {
                victim.setDeltaMovement(victim.getDeltaMovement().multiply(0.4, 0.0, 0.4).add(0.0, 0.75, 0.0));
                victim.resetFallDistance();
                victim.hasImpulse = true;
                victim.hurtMarked = true;
            }
        }
    }

    private boolean isEnemy(LivingEntity entity) {
        if (this.isGolem()) {
            if (!entity.isAlive() || entity == this.owner || entity instanceof CrystalGolemEntity) {
                return false;
            }
            if (entity instanceof net.minecraft.world.entity.player.Player p) {
                return !p.isCreative() && !p.isSpectator();
            }
            return !(entity instanceof net.minecraft.world.entity.monster.Monster);
        }
        if (this.owner instanceof net.minecraft.world.entity.player.Player) {
            return entity.isAlive() && entity != this.owner && !net.yezon.theabyss.util.RingMode.spares(this.owner, entity);
        }
        return entity.isAlive() && entity != this.owner && !(entity instanceof NightbladeBossEntity) && !(entity instanceof NightBladeBossCloneEntity)
            && !(entity instanceof SeekerEntity) && !(entity instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator()));
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
