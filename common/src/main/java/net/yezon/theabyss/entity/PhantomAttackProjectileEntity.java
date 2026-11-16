package net.yezon.theabyss.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.events.PhantomEffectEvents;
import net.yezon.theabyss.events.PhantomTrailEvents;
import net.yezon.theabyss.init.TheabyssModEntities;

public class PhantomAttackProjectileEntity extends AbstractArrow implements ItemSupplier {
    public static final ItemStack PROJECTILE_ITEM = ItemStack.EMPTY;
    private int knockback;

    public PhantomAttackProjectileEntity(EntityType<? extends PhantomAttackProjectileEntity> type, Level world) {
        super(type, world);
    }

    public PhantomAttackProjectileEntity(EntityType<? extends PhantomAttackProjectileEntity> type, double x, double y, double z, Level world) {
        super(type, world);
        this.setPos(x, y, z);
    }

    public PhantomAttackProjectileEntity(EntityType<? extends PhantomAttackProjectileEntity> type, LivingEntity entity, Level world) {
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
    public void playerTouch(Player entity) {
        super.playerTouch(entity);
        PhantomEffectEvents.execute(this.level(), this.getX(), this.getY(), this.getZ(), entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        PhantomEffectEvents.execute(
            this.level(), this.getX(), this.getY(), this.getZ(), entityHitResult.getEntity()
        );
    }

    @Override
    public void tick() {
        super.tick();
        PhantomTrailEvents.execute(this.level(), this.getX(), this.getY(), this.getZ());
        if (this.isInGround()) {
            this.discard();
        }
    }

    public static PhantomAttackProjectileEntity shoot(Level world, LivingEntity entity, RandomSource source) {
        return shoot(world, entity, source, 1.0F, 2.8, 1);
    }

    public static PhantomAttackProjectileEntity shoot(
        Level world, LivingEntity entity, RandomSource random, float power, double damage, int knockback
    ) {
        PhantomAttackProjectileEntity entityarrow = new PhantomAttackProjectileEntity(
            TheabyssModEntities.PHANTOM_ATTACK_PROJECTILE.get(), entity, world
        );
        entityarrow.shoot(entity.getViewVector(1.0F).x, entity.getViewVector(1.0F).y, entity.getViewVector(1.0F).z, power * 2.0F, 0.0F);
        entityarrow.setSilent(true);
        entityarrow.setCritArrow(false);
        entityarrow.setBaseDamage(damage);
        entityarrow.setKnockback(knockback);
        world.addFreshEntity(entityarrow);
        world.playSound(
            null,
            entity.getX(),
            entity.getY(),
            entity.getZ(),
            BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:phantom_attack")),
            SoundSource.PLAYERS,
            1.0F,
            1.0F / (random.nextFloat() * 0.5F + 1.0F) + power / 2.0F
        );
        return entityarrow;
    }

    public static PhantomAttackProjectileEntity shoot(LivingEntity entity, LivingEntity target) {
        PhantomAttackProjectileEntity entityarrow = new PhantomAttackProjectileEntity(
            TheabyssModEntities.PHANTOM_ATTACK_PROJECTILE.get(), entity, entity.level()
        );
        double dx = target.getX() - entity.getX();
        double dy = target.getY() + target.getEyeHeight() - 1.1;
        double dz = target.getZ() - entity.getZ();
        entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * 0.2F, dz, 2.0F, 12.0F);
        entityarrow.setSilent(true);
        entityarrow.setBaseDamage(2.8);
        entityarrow.setKnockback(1);
        entityarrow.setCritArrow(false);
        entity.level().addFreshEntity(entityarrow);
        entity.level().playSound(
            null,
            entity.getX(),
            entity.getY(),
            entity.getZ(),
            BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:phantom_attack")),
            SoundSource.PLAYERS,
            1.0F,
            1.0F / (RandomSource.create().nextFloat() * 0.5F + 1.0F)
        );
        return entityarrow;
    }
}
