package net.yezon.theabyss.entity;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.boss.nightblade.NightbladeParry;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.network.NightbladeImpactMessage;

public class NightbladeBladeEntity extends Entity {
    public static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> RED = SynchedEntityData.defineId(
        NightbladeBladeEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private static final int PASS_TICKS = 23;
    private static final int RETURN_TICKS = 8;
    private static final int MAX_AGE = 120;
    public static final int PARRY_WINDOW = NightbladeParry.WINDOW;
    private static final float DEFLECT_DAMAGE = 24.0F;

    private enum Stage { OUT, BACK, DEFLECTED }

    private NightbladeBossEntity owner;
    private LivingEntity caster;
    private int passTicks = PASS_TICKS;
    private float damage;
    private Stage stage = Stage.OUT;
    private int stageTick;
    private Vec3 from = Vec3.ZERO;
    private Vec3 control = Vec3.ZERO;
    private Vec3 to = Vec3.ZERO;
    private Vec3 side = Vec3.ZERO;
    private ServerPlayer deflector;
    private final Set<Integer> hit = new HashSet<>();
    private Vec3 lastClientPos;
    private Vec3 clientVelocity = Vec3.ZERO;

    public NightbladeBladeEntity(EntityType<? extends NightbladeBladeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void launch(NightbladeBossEntity owner, Vec3 start, LivingEntity target, float damage, boolean red) {
        this.owner = owner;
        this.entityData.set(RED, red);
        this.damage = damage;
        Vec3 through = target.getBoundingBox().getCenter();
        Vec3 dir = through.subtract(start).multiply(1.0, 0.0, 1.0);
        dir = dir.lengthSqr() < 1.0E-4 ? owner.getLookAngle().multiply(1.0, 0.0, 1.0).normalize() : dir.normalize();
        this.side = new Vec3(-dir.z, 0.0, dir.x).scale(owner.getRandom().nextBoolean() ? 1.0 : -1.0);
        Vec3 turn = through.add(dir.scale(6.0)).add(this.side.scale(4.0));
        this.begin(Stage.OUT, start, through, turn);
        this.setPos(start);
    }

    public void launchRing(LivingEntity caster, Vec3 start, Vec3 through, float damage, int passTicks) {
        this.caster = caster;
        this.damage = damage;
        this.passTicks = passTicks;
        Vec3 dir = through.subtract(start).multiply(1.0, 0.0, 1.0);
        dir = dir.lengthSqr() < 1.0E-4 ? caster.getLookAngle().multiply(1.0, 0.0, 1.0).normalize() : dir.normalize();
        this.side = new Vec3(-dir.z, 0.0, dir.x).scale(caster.getRandom().nextBoolean() ? 1.0 : -1.0);
        Vec3 turn = through.add(dir.scale(4.0)).add(this.side.scale(3.0));
        this.begin(Stage.OUT, start, through, turn);
        this.setPos(start);
    }

    private LivingEntity master() {
        return this.owner != null ? this.owner : this.caster;
    }

    private void begin(Stage next, Vec3 a, Vec3 via, Vec3 b) {
        this.stage = next;
        this.stageTick = 0;
        this.from = a;
        this.to = b;
        this.control = via.scale(2.0).subtract(a.add(b).scale(0.5));
        this.hit.clear();
    }

    private Vec3 curve(double t) {
        double u = 1.0 - t;
        return this.from.scale(u * u).add(this.control.scale(2.0 * u * t)).add(this.to.scale(t * t));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RED, false);
    }

    @Override
    public void tick() {
        Vec3 before = this.position();
        super.tick();
        if (this.level().isClientSide()) {
            Vec3 now = this.position();
            this.clientVelocity = this.lastClientPos == null ? Vec3.ZERO : now.subtract(this.lastClientPos);
            this.trail(this.lastClientPos == null ? now : this.lastClientPos);
            this.lastClientPos = now;
            return;
        }
        ServerLevel level = (ServerLevel)this.level();
        if (this.master() == null || !this.master().isAlive() || this.master().level() != level || this.tickCount > MAX_AGE) {
            this.finish(false);
            return;
        }
        this.stageTick++;
        Vec3 next;
        switch (this.stage) {
            case OUT -> {
                next = this.curve(this.stageTick / (double)this.passTicks);
                if (this.stageTick >= this.passTicks) {
                    LivingEntity target = this.owner != null ? this.owner.getTarget() : null;
                    Vec3 via = target != null ? target.getBoundingBox().getCenter() : this.from.lerp(this.to, 0.5);
                    this.side = this.side.scale(-1.0);
                    this.begin(Stage.BACK, this.to, via.add(this.side.scale(0.5)), this.hand());
                    level.playSound(null, next.x, next.y, next.z, SoundEvents.TRIDENT_RETURN, SoundSource.HOSTILE, 1.0F, 1.3F);
                }
            }
            case BACK -> {
                this.to = this.hand();
                next = this.curve(Math.min(1.0, this.stageTick / (double)this.passTicks));
                if (this.stageTick >= this.passTicks) {
                    this.finish(true);
                    return;
                }
            }
            default -> {
                Vec3 aim = this.owner.getBoundingBox().getCenter();
                next = this.position().lerp(aim, Math.min(1.0, 1.0 / Math.max(1, RETURN_TICKS - this.stageTick)));
                if (next.distanceTo(aim) < 1.2 || this.stageTick >= RETURN_TICKS) {
                    this.strikeOwner(level);
                    return;
                }
            }
        }
        this.setPos(next);
        if (this.tickCount % 4 == 0) {
            level.playSound(null, next.x, next.y, next.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 0.6F, 1.6F);
        }
        if (this.stage != Stage.DEFLECTED) {
            this.hitAlong(level, before, next);
        }
    }

    private Vec3 hand() {
        return this.master().position().add(0.0, this.master().getBbHeight() * 0.6, 0.0);
    }

    private void hitAlong(ServerLevel level, Vec3 a, Vec3 b) {
        AABB sweep = new AABB(a, b).inflate(0.6);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, sweep, this::isEnemy)) {
            if (!victim.getBoundingBox().inflate(0.45).clip(a, b).isPresent() && !victim.getBoundingBox().inflate(0.45).contains(b)) {
                continue;
            }
            if (!this.hit.add(victim.getId())) {
                continue;
            }
            if (this.owner != null && victim instanceof ServerPlayer player && NightbladeParry.consume(player, PARRY_WINDOW)) {
                this.deflect(level, player);
                return;
            }
            DamageSource source = this.owner != null ? this.damageSources().mobProjectile(this, this.owner) : this.damageSources().indirectMagic(this, this.caster);
            victim.damageCooldownTime = 0;
            victim.hurtServer(level, source, this.damage);
            Vec3 push = b.subtract(a).normalize().scale(0.6);
            victim.push(push.x, 0.2, push.z);
            if (this.owner != null) {
                level.sendParticles(new DustParticleOptions(0xFF5959, 1.2F), victim.getX(), victim.getY() + 1.0, victim.getZ(), 12, 0.3, 0.4, 0.3, 0.0);
            } else {
                net.yezon.theabyss.ring.fx.RingFx.burst(level, net.yezon.theabyss.ring.RingVisuals.sparkle(net.yezon.theabyss.ring.RingVisuals.NIGHTBLADE, 0.14F, 18)
                    .flags(net.yezon.theabyss.ring.fx.FxOptions.FLICKER | net.yezon.theabyss.ring.fx.FxOptions.HOT), victim.getBoundingBox().getCenter(), 14, 0.25, 0.2);
                level.playSound(null, victim.getX(), victim.getY() + 1.0, victim.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.8F);
            }
        }
    }

    private boolean isEnemy(LivingEntity entity) {
        if (this.caster != null) {
            return entity.isAlive() && entity != this.caster && !entity.isSpectator() && !net.yezon.theabyss.util.RingMode.spares(this.caster, entity);
        }
        return entity.isAlive() && entity != this.owner && !(entity instanceof NightbladeBossEntity) && !(entity instanceof NightBladeBossCloneEntity)
            && !(entity instanceof SeekerEntity) && !(entity instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator()));
    }

    private void deflect(ServerLevel level, ServerPlayer player) {
        this.deflector = player;
        this.stage = Stage.DEFLECTED;
        this.stageTick = 0;
        Vec3 at = this.position();
        NightbladeParry.playParryEffect(level, at);
        NightbladeImpactMessage.send(player, true);
    }

    private void strikeOwner(ServerLevel level) {
        Vec3 at = this.owner.getBoundingBox().getCenter();
        DamageSource source = this.deflector != null ? this.damageSources().playerAttack(this.deflector) : this.damageSources().magic();
        this.owner.hurtUnavoidable(level, source, DEFLECT_DAMAGE);
        level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles(new DustParticleOptions(0xFF5959, 1.4F), at.x, at.y, at.z, 24, 0.4, 0.6, 0.4, 0.0);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.4F, 0.7F);
        this.finish(false);
    }

    private void finish(boolean caught) {
        if (this.caster != null && caught) {
            this.level().playSound(null, this.caster.getX(), this.caster.getY() + 1.0, this.caster.getZ(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.2F, 1.1F);
        }
        if (this.owner != null && this.owner.isAlive()) {
            this.owner.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(TheabyssModItems.ABYSS_SWORD.get()));
            if (caught) {
                this.owner.triggerAnim("action", "blade_catch");
                this.level().playSound(null, this.owner.getX(), this.owner.getY() + 1.0, this.owner.getZ(), SoundEvents.TRIDENT_RETURN, SoundSource.HOSTILE, 1.2F, 0.9F);
            }
        }
        this.discard();
    }

    public Vec3 clientVelocity() {
        return this.clientVelocity;
    }

    private void trail(Vec3 before) {
        Vec3 now = this.position();
        int steps = Math.max(1, (int)Math.ceil(before.distanceTo(now) / 0.15));
        for (int i = 0; i < steps; i++) {
            Vec3 at = before.lerp(now, (i + this.random.nextFloat()) / steps);
            this.level().addParticle(TheabyssModParticleTypes.NIGHTBLADE_TRAIL.get(), at.x, at.y, at.z, this.entityData.get(RED) ? 1.0 : 0.0, 0.0, 0.0);
        }
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
