package net.yezon.theabyss.entity;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.compat.ValueInput;
import net.yezon.theabyss.compat.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.events.InfectHandlerEvents;
import net.yezon.theabyss.ring.fx.FxOptions;

public class LurkerSpitEntity extends Entity {
    static final double GRAVITY = 0.035;
    private static final int MAX_AGE = 80;
    private static final float DAMAGE = 4.0F;
    private static final int SLOW_TICKS = 100;
    private static final int INFECTION_BOOST_TICKS = 100;

    private JungleLurkerEntity owner;
    private Vec3 velocity = Vec3.ZERO;
    private Vec3 clientVelocity = Vec3.ZERO;
    private Vec3 lastClientPos;

    public LurkerSpitEntity(EntityType<? extends LurkerSpitEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    void launch(JungleLurkerEntity lurker, Vec3 velocity) {
        this.owner = lurker;
        this.velocity = velocity;
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
                for (int i = 0; i < 2; i++) {
                    Vec3 at = this.lastClientPos.lerp(now, this.random.nextDouble());
                    int color = i == 0 ? LurkerFx.SOBBER : LurkerFx.GLOW;
                    this.level().addParticle(TheabyssModParticleTypes.FX_ORB.get().of(color, 0.08F, 14, FxOptions.SHRINK).gravity(0.03F),
                        at.x, at.y + 0.15, at.z, 0.0, -0.02, 0.0);
                }
            }
            this.lastClientPos = now;
            return;
        }
        ServerLevel level = (ServerLevel)this.level();
        if (this.owner == null || this.tickCount > MAX_AGE) {
            this.discard();
            return;
        }
        this.velocity = this.velocity.add(0.0, -GRAVITY, 0.0);
        Vec3 from = this.position();
        Vec3 to = from.add(this.velocity);
        AABB swept = new AABB(from, to).inflate(0.35);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, swept, this::hittable)) {
            this.setPos(e.getBoundingBox().getCenter());
            this.hitEntity(level, e);
            return;
        }
        BlockHitResult block = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this));
        if (block.getType() != HitResult.Type.MISS) {
            this.setPos(block.getLocation());
            this.hitGround(level, block);
            return;
        }
        this.setPos(to);
    }

    private void hitEntity(ServerLevel level, LivingEntity victim) {
        DamageSource source = this.damageSources().mobProjectile(this, this.owner);
        victim.hurt(source, DAMAGE);
        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_TICKS, 1), this.owner);
        LurkerFx.splash(level, this.position(), true);
        LurkerPuddles.add(level, victim.position(), 1.4, 120, this.owner);
        LurkerPuddles.stick(victim, SLOW_TICKS);
        if (victim instanceof ServerPlayer player) {
            InfectHandlerEvents.boost(player, INFECTION_BOOST_TICKS);
        }
        this.discard();
    }

    private void hitGround(ServerLevel level, BlockHitResult block) {
        Vec3 at = block.getLocation();
        LurkerFx.splash(level, at, false);
        Vec3 floor = level.clip(new ClipContext(at.add(0.0, 0.5, 0.0), at.add(0.0, -3.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this))
            .getLocation();
        LurkerPuddles.add(level, floor, 1.6, 160, this.owner);
        this.discard();
    }

    private boolean hittable(LivingEntity e) {
        if (!e.isAlive() || e == this.owner || e instanceof JungleLurkerEntity) {
            return false;
        }
        if (e instanceof Player p) {
            return !p.isCreative() && !p.isSpectator();
        }
        return !(e instanceof Monster) || e == this.owner.getTarget();
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
