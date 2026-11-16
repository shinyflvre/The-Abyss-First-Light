package net.yezon.theabyss.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.events.RingOfFireStrikeParticleEvents;
import net.yezon.theabyss.init.TheabyssModEntities;

public class RingOfFireStrikeAttackProjectileEntity extends AbstractArrow implements ItemSupplier {
    public static final ItemStack PROJECTILE_ITEM = new ItemStack(Items.ARROW);
    private int knockback;
    private byte pierce;

    public RingOfFireStrikeAttackProjectileEntity(EntityType<? extends RingOfFireStrikeAttackProjectileEntity> type, Level world) {
        super(type, world);
    }

    public RingOfFireStrikeAttackProjectileEntity(EntityType<? extends RingOfFireStrikeAttackProjectileEntity> type, double x, double y, double z, Level world) {
        super(type, world);
        this.setPos(x, y, z);
    }

    public RingOfFireStrikeAttackProjectileEntity(EntityType<? extends RingOfFireStrikeAttackProjectileEntity> type, LivingEntity entity, Level world) {
        this(type, entity.getX(), entity.getEyeY() - 0.1F, entity.getZ(), world);
        this.setOwner(entity);
    }

    @Override
    public ItemStack getItem() {
        return PROJECTILE_ITEM;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return PROJECTILE_ITEM.copy();
    }

    public void setKnockback(int knockback) {
        this.knockback = knockback;
    }

    public void setPierce(int pierce) {
        this.pierce = (byte)pierce;
    }

    @Override
    public byte getPierceLevel() {
        return (byte)Math.max(super.getPierceLevel(), this.pierce);
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
            net.yezon.theabyss.ring.RingVisuals.fireStrikeHit(level, entityHitResult.getEntity().getBoundingBox().getCenter());
        }
    }

    @Override
    public void tick() {
        super.tick();
        RingOfFireStrikeParticleEvents.execute(this.level(), this);
        if (this.isInGround()) {
            if (this.level() instanceof net.minecraft.server.level.ServerLevel level) {
                net.yezon.theabyss.ring.RingVisuals.fireStrikeHit(level, this.position());
            }
            this.discard();
        }
    }

    public static RingOfFireStrikeAttackProjectileEntity shoot(Level world, LivingEntity entity, RandomSource source) {
        return shoot(world, entity, source, 3.0F, 2.0, 2);
    }

    public static RingOfFireStrikeAttackProjectileEntity shoot(
        Level world, LivingEntity entity, RandomSource random, float power, double damage, int knockback
    ) {
        RingOfFireStrikeAttackProjectileEntity entityarrow = new RingOfFireStrikeAttackProjectileEntity(
            TheabyssModEntities.RING_OF_FIRE_STRIKE_ATTACK_PROJECTILE.get(), entity, world
        );
        entityarrow.shoot(entity.getViewVector(1.0F).x, entity.getViewVector(1.0F).y, entity.getViewVector(1.0F).z, power * 2.0F, 0.0F);
        entityarrow.setSilent(true);
        entityarrow.setCritArrow(false);
        entityarrow.setBaseDamage(damage);
        entityarrow.setKnockback(knockback);
        entityarrow.igniteForSeconds(100);
        world.addFreshEntity(entityarrow);
        return entityarrow;
    }

    public static RingOfFireStrikeAttackProjectileEntity shoot(LivingEntity entity, LivingEntity target) {
        RingOfFireStrikeAttackProjectileEntity entityarrow = new RingOfFireStrikeAttackProjectileEntity(
            TheabyssModEntities.RING_OF_FIRE_STRIKE_ATTACK_PROJECTILE.get(), entity, entity.level()
        );
        double dx = target.getX() - entity.getX();
        double dy = target.getY() + target.getEyeHeight() - 1.1;
        double dz = target.getZ() - entity.getZ();
        entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * 0.2F, dz, 6.0F, 12.0F);
        entityarrow.setSilent(true);
        entityarrow.setBaseDamage(2.0);
        entityarrow.setKnockback(2);
        entityarrow.setCritArrow(false);
        entityarrow.igniteForSeconds(100);
        entity.level().addFreshEntity(entityarrow);
        return entityarrow;
    }
}
