package net.yezon.theabyss.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;
import net.yezon.theabyss.init.TheabyssModEntities;
import org.jspecify.annotations.Nullable;

public class VersaWhaleEntity extends Monster {
    private static final double CRUISE_SPEED = 0.14;
    private static final double FLEE_SPEED = 0.32;
    private static final double HOME_RADIUS = 100.0;
    private static final double LOOK_AHEAD = 14.0;
    private static final float MAX_TURN = (float)Math.toRadians(1.1);

    private @Nullable Vec3 home;
    private float heading = Float.NaN;
    private float wantedHeading;
    private double cruiseHeight = 26.0;
    private int nextCourseChange;
    private int fleeTicks;

    public VersaWhaleEntity(EntityType<VersaWhaleEntity> type, Level world) {
        super(type, world);
        this.xpReward = 0;
        this.setNoGravity(true);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.home == null) {
            this.home = this.position();
        }
        if (Float.isNaN(this.heading)) {
            this.heading = this.random.nextFloat() * Mth.TWO_PI;
            this.wantedHeading = this.heading;
            this.cruiseHeight = 18.0 + this.random.nextDouble() * 16.0;
        }
        this.fleeTicks = Math.max(0, this.fleeTicks - 1);

        if (--this.nextCourseChange <= 0) {
            this.nextCourseChange = 120 + this.random.nextInt(160);
            this.wantedHeading = this.heading + (this.random.nextFloat() - 0.5F) * Mth.PI;
            if (this.random.nextInt(3) == 0) {
                this.cruiseHeight = 18.0 + this.random.nextDouble() * 16.0;
            }
        }
        if (this.home.distanceToSqr(this.getX(), this.home.y, this.getZ()) > HOME_RADIUS * HOME_RADIUS) {
            this.wantedHeading = (float)Mth.atan2(this.home.z - this.getZ(), this.home.x - this.getX());
        }
        this.pod(level);

        double ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.getBlockX(), this.getBlockZ());
        double wantedY = ground + this.cruiseHeight + (this.fleeTicks > 0 ? 10.0 : 0.0);
        Vec3 forward = new Vec3(Mth.cos(this.heading), 0.0, Mth.sin(this.heading));
        Vec3 ahead = this.position().add(forward.scale(LOOK_AHEAD));
        double groundAhead = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(ahead.x), Mth.floor(ahead.z));
        wantedY = Math.max(wantedY, groundAhead + this.cruiseHeight * 0.7);
        Vec3 eye = this.position().add(0.0, this.getBbHeight() * 0.5, 0.0);
        if (level.clip(new ClipContext(eye, eye.add(forward.scale(LOOK_AHEAD)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType()
            != HitResult.Type.MISS) {
            this.wantedHeading = this.heading + (this.freeSide(level, eye) ? 1.0F : -1.0F) * Mth.HALF_PI;
            wantedY += 8.0;
        }
        wantedY = Math.min(wantedY, level.getMaxY() - 12.0);

        float turn = Mth.wrapDegrees((this.wantedHeading - this.heading) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
        this.heading += Mth.clamp(turn, -MAX_TURN * (this.fleeTicks > 0 ? 2.5F : 1.0F), MAX_TURN * (this.fleeTicks > 0 ? 2.5F : 1.0F));
        double speed = this.fleeTicks > 0 ? FLEE_SPEED : CRUISE_SPEED;
        double vy = Mth.clamp((wantedY - this.getY()) * 0.02, -0.07, 0.07) + Mth.sin(this.tickCount * 0.03F) * 0.015;
        Vec3 wanted = new Vec3(Mth.cos(this.heading) * speed, vy, Mth.sin(this.heading) * speed);
        this.setDeltaMovement(this.getDeltaMovement().lerp(wanted, 0.08));

        Vec3 motion = this.getDeltaMovement();
        float yaw = (float)(Mth.atan2(motion.z, motion.x) * Mth.RAD_TO_DEG) - 90.0F;
        float pitch = (float)(-Mth.atan2(motion.y, motion.horizontalDistance()) * Mth.RAD_TO_DEG);
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
        this.setXRot(Mth.clamp(pitch, -30.0F, 30.0F));
    }

    private void pod(ServerLevel level) {
        List<VersaWhaleEntity> others = level.getEntitiesOfClass(VersaWhaleEntity.class, this.getBoundingBox().inflate(24.0), w -> w != this);
        if (others.isEmpty()) {
            return;
        }
        float sx = 0.0F;
        float sz = 0.0F;
        for (VersaWhaleEntity other : others) {
            if (!Float.isNaN(other.heading)) {
                sx += Mth.cos(other.heading);
                sz += Mth.sin(other.heading);
            }
            double distance = this.distanceTo(other);
            if (distance < 8.0) {
                float away = (float)Mth.atan2(this.getZ() - other.getZ(), this.getX() - other.getX());
                this.wantedHeading = away;
                return;
            }
        }
        if (sx != 0.0F || sz != 0.0F) {
            float average = (float)Mth.atan2(sz, sx);
            float difference = Mth.wrapDegrees((average - this.wantedHeading) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
            this.wantedHeading += difference * 0.05F;
        }
    }

    private boolean freeSide(ServerLevel level, Vec3 eye) {
        Vec3 left = new Vec3(Mth.cos(this.heading - Mth.HALF_PI), 0.0, Mth.sin(this.heading - Mth.HALF_PI)).scale(LOOK_AHEAD);
        return level.clip(new ClipContext(eye, eye.add(left), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            this.fleeTicks = 120;
            if (source.getEntity() != null) {
                this.wantedHeading = (float)Mth.atan2(this.getZ() - source.getEntity().getZ(), this.getX() - source.getEntity().getX());
            }
        }
        return hurt;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
        ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData
    ) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        var scale = this.getAttribute(Attributes.SCALE);
        if (scale != null) {
            float spread = this.random.nextFloat() * this.random.nextFloat();
            scale.setBaseValue(this.random.nextBoolean() ? 1.0 - 0.3 * spread : 1.0 + 0.35 * spread);
        }
        if (reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION) {
            double ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.getBlockX(), this.getBlockZ());
            double y = ground + 18.0 + this.random.nextDouble() * 16.0;
            if (level.noCollision(this, this.getBoundingBox().move(0.0, y - this.getY(), 0.0))) {
                this.setPos(this.getX(), y, this.getZ());
            }
        }
        this.home = null;
        return data;
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
        event.register(
            TheabyssModEntities.VERSA_WHALE.get(),
            SpawnPlacementTypes.NO_RESTRICTIONS,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, world, reason, pos, random) -> world.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                && pos.getY() >= world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) - 1
                && world.canSeeSky(pos),
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double size = 16.0 * this.getScale() * 64.0 * net.minecraft.world.entity.Entity.getViewScale();
        return distance < size * size;
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    @Override
    public SoundEvent getAmbientSound() {
        return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:versa_whale_idle"));
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }

    @Override
    protected float getSoundVolume() {
        return 3.0F;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.generic.hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.generic.death"));
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.MAX_HEALTH, 200.0)
            .add(Attributes.ARMOR, 0.0)
            .add(Attributes.ATTACK_DAMAGE, 10.0)
            .add(Attributes.FOLLOW_RANGE, 16.0)
            .add(Attributes.FLYING_SPEED, 0.3);
    }
}
