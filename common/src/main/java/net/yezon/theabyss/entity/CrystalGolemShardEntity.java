package net.yezon.theabyss.entity;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.yezon.theabyss.compat.ValueInput;
import net.yezon.theabyss.compat.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.boss.nightblade.NightbladeParry;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.network.NightbladeImpactMessage;
import net.yezon.theabyss.ring.fx.FxOptions;
import net.yezon.theabyss.ring.fx.RingFx;

public class CrystalGolemShardEntity extends Entity {
    static final double GRAVITY = 0.05;
    static final double SPLASH = 2.5;
    private static final double PARRY_RANGE = 3.2;
    private static final int MAX_AGE = 120;

    private CrystalGolemEntity owner;
    private Vec3 velocity = Vec3.ZERO;
    private float damage = 14.0F;
    private boolean reflected;
    private Vec3 clientVelocity = Vec3.ZERO;
    private Vec3 lastClientPos;

    public CrystalGolemShardEntity(EntityType<? extends CrystalGolemShardEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    void launch(CrystalGolemEntity golem, Vec3 velocity, float damage) {
        this.owner = golem;
        this.velocity = velocity;
        this.damage = damage;
    }

    public Vec3 clientVelocity() {
        return this.clientVelocity;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            Vec3 now = this.position();
            this.clientVelocity = this.lastClientPos == null ? Vec3.ZERO : now.subtract(this.lastClientPos);
            if (this.lastClientPos != null) {
                for (int i = 0; i < 3; i++) {
                    Vec3 at = this.lastClientPos.lerp(now, this.random.nextDouble());
                    this.level().addParticle(TheabyssModParticleTypes.FX_SPARKLE.get().of(i == 0 ? CrystalGolemFx.PALE : CrystalGolemFx.PINK, 0.16F, 10, FxOptions.FLICKER),
                        at.x, at.y + 0.3, at.z, 0.0, 0.01, 0.0);
                }
            }
            this.lastClientPos = now;
            return;
        }
        ServerLevel level = (ServerLevel)this.level();
        if (this.owner == null || !this.owner.isAlive() || this.tickCount > MAX_AGE) {
            this.discard();
            return;
        }
        if (!this.reflected) {
            this.velocity = this.velocity.add(0.0, -GRAVITY, 0.0);
            if (this.tryParry(level)) {
                return;
            }
        } else {
            Vec3 chest = this.owner.position().add(0.0, 1.8, 0.0);
            Vec3 to = chest.subtract(this.position());
            if (to.length() < 1.6) {
                this.hitGolem(level);
                return;
            }
            this.velocity = to.normalize().scale(1.1);
        }
        Vec3 from = this.position();
        Vec3 to = from.add(this.velocity);
        BlockHitResult block = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (block.getType() != HitResult.Type.MISS) {
            this.setPos(block.getLocation());
            this.shatter(level);
            return;
        }
        if (!this.reflected) {
            AABB swept = new AABB(from, to).inflate(0.6);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, swept, this::hittable)) {
                this.setPos(e.getBoundingBox().getCenter());
                this.shatter(level);
                return;
            }
        }
        this.setPos(to);
    }

    private boolean tryParry(ServerLevel level) {
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(PARRY_RANGE), this::hittable)) {
            if (NightbladeParry.consume(player, NightbladeParry.MINI_BOSS_WINDOW)) {
                this.reflected = true;
                NightbladeParry.playParryEffect(level, this.position());
                NightbladeImpactMessage.send(player, true);
                return true;
            }
        }
        return false;
    }

    private void hitGolem(ServerLevel level) {
        Vec3 at = this.position();
        CrystalGolemFx.shardShatter(level, at);
        DamageSource source = this.damageSources().thrown(this, null);
        this.owner.hurt(source, CrystalGolemEntity.halfHearts(this.damage * 2.0F));
        this.owner.dazeFromReflectedShard();
        this.discard();
    }

    private void shatter(ServerLevel level) {
        Vec3 at = this.position();
        CrystalGolemFx.shardShatter(level, at);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_BLOCK.defaultBlockState()), at.x, at.y + 0.2, at.z, 30, 0.5, 0.3, 0.5, 0.2);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(SPLASH), this::hittable)) {
            if (victim.getBoundingBox().getCenter().distanceTo(at) <= SPLASH + victim.getBbWidth() * 0.5) {
                victim.hurt(this.damageSources().mobProjectile(this, this.owner), this.damage);
            }
        }
        this.discard();
    }

    private boolean hittable(LivingEntity e) {
        if (!e.isAlive() || e == this.owner || e instanceof CrystalGolemEntity) {
            return false;
        }
        if (e instanceof Player p) {
            return !p.isCreative() && !p.isSpectator();
        }
        return !(e instanceof Monster);
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
