package net.yezon.theabyss.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.events.ExplosiveShurikenEvents;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;

public class ShurikenExplosiveProjectileEntity extends AbstractArrow implements ItemSupplier {
    public static final ItemStack PROJECTILE_ITEM = new ItemStack(TheabyssModItems.SHURIKEN_EXPLOSIVE.get());
    private int knockback;

    public ShurikenExplosiveProjectileEntity(EntityType<? extends ShurikenExplosiveProjectileEntity> type, Level world) {
        super(type, world);
    }

    public ShurikenExplosiveProjectileEntity(EntityType<? extends ShurikenExplosiveProjectileEntity> type, double x, double y, double z, Level world) {
        super(type, world);
        this.setPos(x, y, z);
    }

    public ShurikenExplosiveProjectileEntity(EntityType<? extends ShurikenExplosiveProjectileEntity> type, LivingEntity entity, Level world) {
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
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        ExplosiveShurikenEvents.execute(this.level(), this.getX(), this.getY(), this.getZ());
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        ExplosiveShurikenEvents.execute(
            this.level(), blockHitResult.getBlockPos().getX(), blockHitResult.getBlockPos().getY(), blockHitResult.getBlockPos().getZ()
        );
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isInGround()) {
            this.discard();
        }
    }

    public static ShurikenExplosiveProjectileEntity shoot(Level world, LivingEntity entity, RandomSource source) {
        return shoot(world, entity, source, 1.0F, 2.0, 1);
    }

    public static ShurikenExplosiveProjectileEntity shoot(
        Level world, LivingEntity entity, RandomSource random, float power, double damage, int knockback
    ) {
        ShurikenExplosiveProjectileEntity entityarrow = new ShurikenExplosiveProjectileEntity(
            TheabyssModEntities.SHURIKEN_EXPLOSIVE_PROJECTILE.get(), entity, world
        );
        entityarrow.shoot(entity.getViewVector(1.0F).x, entity.getViewVector(1.0F).y, entity.getViewVector(1.0F).z, power * 2.0F, 0.0F);
        entityarrow.setSilent(true);
        entityarrow.setCritArrow(true);
        entityarrow.setBaseDamage(damage);
        entityarrow.setKnockback(knockback);
        world.addFreshEntity(entityarrow);
        world.playSound(
            null,
            entity.getX(),
            entity.getY(),
            entity.getZ(),
            BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.arrow.shoot")),
            SoundSource.PLAYERS,
            1.0F,
            1.0F / (random.nextFloat() * 0.5F + 1.0F) + power / 2.0F
        );
        return entityarrow;
    }

    public static ShurikenExplosiveProjectileEntity shoot(LivingEntity entity, LivingEntity target) {
        ShurikenExplosiveProjectileEntity entityarrow = new ShurikenExplosiveProjectileEntity(
            TheabyssModEntities.SHURIKEN_EXPLOSIVE_PROJECTILE.get(), entity, entity.level()
        );
        double dx = target.getX() - entity.getX();
        double dy = target.getY() + target.getEyeHeight() - 1.1;
        double dz = target.getZ() - entity.getZ();
        entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * 0.2F, dz, 2.0F, 12.0F);
        entityarrow.setSilent(true);
        entityarrow.setBaseDamage(2.0);
        entityarrow.setKnockback(1);
        entityarrow.setCritArrow(true);
        entity.level().addFreshEntity(entityarrow);
        entity.level().playSound(
            null,
            entity.getX(),
            entity.getY(),
            entity.getZ(),
            BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.arrow.shoot")),
            SoundSource.PLAYERS,
            1.0F,
            1.0F / (RandomSource.create().nextFloat() * 0.5F + 1.0F)
        );
        return entityarrow;
    }
}
