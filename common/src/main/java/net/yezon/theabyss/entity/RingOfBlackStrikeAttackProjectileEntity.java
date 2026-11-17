package net.yezon.theabyss.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.events.RingOfBlackStrikeParticleEvents;
import net.yezon.theabyss.init.TheabyssModEntities;

public class RingOfBlackStrikeAttackProjectileEntity extends AbstractArrow implements ItemSupplier {
    public static final ItemStack PROJECTILE_ITEM = ItemStack.EMPTY;
    private static final String RANGE_KEY = "theabyss_bolt_range";
    private static final String START_X_KEY = "theabyss_bolt_x";
    private static final String START_Y_KEY = "theabyss_bolt_y";
    private static final String START_Z_KEY = "theabyss_bolt_z";
    private static final double MIN_SPEED_SQR = 0.0025;
    private static final float HIT_VOLUME = 2.5F;
    private int knockback;

    public RingOfBlackStrikeAttackProjectileEntity(EntityType<? extends RingOfBlackStrikeAttackProjectileEntity> type, Level world) {
        super(type, world);
    }

    public RingOfBlackStrikeAttackProjectileEntity(EntityType<? extends RingOfBlackStrikeAttackProjectileEntity> type, double x, double y, double z, Level world) {
        super(type, world);
        this.setPos(x, y, z);
    }

    public RingOfBlackStrikeAttackProjectileEntity(EntityType<? extends RingOfBlackStrikeAttackProjectileEntity> type, LivingEntity entity, Level world) {
        this(type, entity.getX(), entity.getEyeY() - 0.1F, entity.getZ(), world);
        this.setOwner(entity);
    }

    @Override
    public ItemStack getItem() {
        return PROJECTILE_ITEM;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected boolean tryPickup(Player player) {
        return false;
    }

    public void limitRange(double range) {
        net.minecraft.nbt.CompoundTag data = net.yezon.theabyss.platform.PersistentData.get(this);
        data.putDouble(RANGE_KEY, range);
        data.putDouble(START_X_KEY, this.getX());
        data.putDouble(START_Y_KEY, this.getY());
        data.putDouble(START_Z_KEY, this.getZ());
    }

    private boolean outOfRange() {
        net.minecraft.nbt.CompoundTag data = net.yezon.theabyss.platform.PersistentData.get(this);
        double range = net.yezon.theabyss.compat.NbtCompat.getDoubleOr(data, RANGE_KEY, 0.0);
        if (range <= 0.0) {
            return false;
        }
        double dx = this.getX() - net.yezon.theabyss.compat.NbtCompat.getDoubleOr(data, START_X_KEY, this.getX());
        double dy = this.getY() - net.yezon.theabyss.compat.NbtCompat.getDoubleOr(data, START_Y_KEY, this.getY());
        double dz = this.getZ() - net.yezon.theabyss.compat.NbtCompat.getDoubleOr(data, START_Z_KEY, this.getZ());
        return dx * dx + dy * dy + dz * dz >= range * range || this.getDeltaMovement().lengthSqr() < MIN_SPEED_SQR;
    }

    public void setKnockback(int knockback) {
        this.knockback = knockback;
    }

    @Override
    protected void doKnockback(LivingEntity target, DamageSource damageSource) {
        if (this.knockback > 0) {
            double resistance = Math.max(0.0, 1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            Vec3 push = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale(this.knockback * 0.6 * resistance);
            if (push.lengthSqr() > 0.0) {
                target.push(push.x, 0.1, push.z);
            }
        } else {
            super.doKnockback(target, damageSource);
        }
    }

    @Override
    protected void doPostHurtEffects(LivingEntity entity) {
        super.doPostHurtEffects(entity);
        entity.setArrowCount(entity.getArrowCount() - 1);
    }

    @Override
    protected boolean canHitEntity(net.minecraft.world.entity.Entity target) {
        return super.canHitEntity(target) && !net.yezon.theabyss.util.RingMode.spares(this.getOwner(), target);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (this.level() instanceof net.minecraft.server.level.ServerLevel level) {
            net.yezon.theabyss.ring.RingVisuals.blackStrikeHit(level, entityHitResult.getEntity().getBoundingBox().getCenter());
            if (net.yezon.theabyss.compat.NbtCompat.getDoubleOr(net.yezon.theabyss.platform.PersistentData.get(this), RANGE_KEY, 0.0) > 0.0) {
                net.yezon.theabyss.ring.RingVisuals.sound(level, entityHitResult.getEntity().getBoundingBox().getCenter(), net.yezon.theabyss.init.TheabyssModSounds.SPELL_OF_BLACKSTRIKE_HIT.get(), HIT_VOLUME,
                    net.yezon.theabyss.ring.RingVisuals.castPitch(this.getRandom()));
            }
        }
        if (this.getOwner() instanceof NightbladeBossEntity) {
            if (entityHitResult.getEntity() instanceof net.minecraft.world.entity.LivingEntity victim) {
                victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 200, 1));
            }
            return;
        }
        if (entityHitResult.getEntity() instanceof net.minecraft.world.entity.LivingEntity victim && !victim.level().isClientSide()) {
            victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 200, 3));
            victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.CONFUSION, 200, 1));
            victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 200, 1));
        }
    }

    @Override
    public void tick() {
        super.tick();
        RingOfBlackStrikeParticleEvents.execute(this.level(), this);
        if (this.inGround) {
            if (this.level() instanceof net.minecraft.server.level.ServerLevel level) {
                net.yezon.theabyss.ring.RingVisuals.blackStrikeHit(level, this.position());
            }
            this.discard();
        } else if (this.level() instanceof net.minecraft.server.level.ServerLevel level && this.outOfRange()) {
            net.yezon.theabyss.ring.RingVisuals.blackStrikeFade(level, this.position());
            this.discard();
        }
    }

    public static RingOfBlackStrikeAttackProjectileEntity shoot(Level world, LivingEntity entity, RandomSource source) {
        return shoot(world, entity, source, 1.5F, 2.0, 2);
    }

    public static RingOfBlackStrikeAttackProjectileEntity shoot(
        Level world, LivingEntity entity, RandomSource random, float power, double damage, int knockback
    ) {
        RingOfBlackStrikeAttackProjectileEntity entityarrow = new RingOfBlackStrikeAttackProjectileEntity(
            TheabyssModEntities.RING_OF_BLACK_STRIKE_ATTACK_PROJECTILE.get(), entity, world
        );
        entityarrow.shoot(entity.getViewVector(1.0F).x, entity.getViewVector(1.0F).y, entity.getViewVector(1.0F).z, power * 2.0F, 0.0F);
        entityarrow.setSilent(true);
        entityarrow.setCritArrow(true);
        entityarrow.setBaseDamage(damage);
        entityarrow.setKnockback(knockback);
        world.addFreshEntity(entityarrow);
        return entityarrow;
    }

    public static RingOfBlackStrikeAttackProjectileEntity shoot(LivingEntity entity, LivingEntity target) {
        RingOfBlackStrikeAttackProjectileEntity entityarrow = new RingOfBlackStrikeAttackProjectileEntity(
            TheabyssModEntities.RING_OF_BLACK_STRIKE_ATTACK_PROJECTILE.get(), entity, entity.level()
        );
        double dx = target.getX() - entity.getX();
        double dy = target.getY() + target.getEyeHeight() - 1.1;
        double dz = target.getZ() - entity.getZ();
        entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * 0.2F, dz, 3.0F, 12.0F);
        entityarrow.setSilent(true);
        entityarrow.setBaseDamage(2.0);
        entityarrow.setKnockback(2);
        entityarrow.setCritArrow(true);
        entity.level().addFreshEntity(entityarrow);
        return entityarrow;
    }
}
