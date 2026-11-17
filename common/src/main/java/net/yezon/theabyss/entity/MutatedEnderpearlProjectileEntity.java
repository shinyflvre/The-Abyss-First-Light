package net.yezon.theabyss.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.events.TeleportHandlerEvents;
import net.yezon.theabyss.events.TeleportTrailEvents;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;

public class MutatedEnderpearlProjectileEntity extends AbstractArrow implements ItemSupplier {
    public static final ItemStack PROJECTILE_ITEM = new ItemStack(TheabyssModItems.MUTATED_ENDERPEARL.get());
    private int knockback;

    public MutatedEnderpearlProjectileEntity(EntityType<? extends MutatedEnderpearlProjectileEntity> type, Level world) {
        super(type, world);
    }

    public MutatedEnderpearlProjectileEntity(EntityType<? extends MutatedEnderpearlProjectileEntity> type, double x, double y, double z, Level world) {
        super(type, world);
        this.setPos(x, y, z);
    }

    public MutatedEnderpearlProjectileEntity(EntityType<? extends MutatedEnderpearlProjectileEntity> type, LivingEntity entity, Level world) {
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
        TeleportHandlerEvents.execute(
            this.level(), this.getX(), this.getY(), this.getZ(), entityHitResult.getEntity()
        );
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        TeleportHandlerEvents.execute(
            this.level(), blockHitResult.getBlockPos().getX(), blockHitResult.getBlockPos().getY(), blockHitResult.getBlockPos().getZ(), this.getOwner()
        );
    }

    @Override
    public void tick() {
        super.tick();
        TeleportTrailEvents.execute(this.level(), this);
        if (this.inGround) {
            this.discard();
        }
    }

    public static MutatedEnderpearlProjectileEntity shoot(Level world, LivingEntity entity, RandomSource source) {
        return shoot(world, entity, source, 2.0F, 1.0, 2);
    }

    public static MutatedEnderpearlProjectileEntity shoot(
        Level world, LivingEntity entity, RandomSource random, float power, double damage, int knockback
    ) {
        MutatedEnderpearlProjectileEntity entityarrow = new MutatedEnderpearlProjectileEntity(
            TheabyssModEntities.MUTATED_ENDERPEARL_PROJECTILE.get(), entity, world
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
            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:spell_of_teleport")),
            SoundSource.PLAYERS,
            1.0F,
            1.0F / (random.nextFloat() * 0.5F + 1.0F) + power / 2.0F
        );
        return entityarrow;
    }

    public static MutatedEnderpearlProjectileEntity shoot(LivingEntity entity, LivingEntity target) {
        MutatedEnderpearlProjectileEntity entityarrow = new MutatedEnderpearlProjectileEntity(
            TheabyssModEntities.MUTATED_ENDERPEARL_PROJECTILE.get(), entity, entity.level()
        );
        double dx = target.getX() - entity.getX();
        double dy = target.getY() + target.getEyeHeight() - 1.1;
        double dz = target.getZ() - entity.getZ();
        entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * 0.2F, dz, 4.0F, 12.0F);
        entityarrow.setSilent(true);
        entityarrow.setBaseDamage(1.0);
        entityarrow.setKnockback(2);
        entityarrow.setCritArrow(false);
        entity.level().addFreshEntity(entityarrow);
        entity.level().playSound(
            null,
            entity.getX(),
            entity.getY(),
            entity.getZ(),
            BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:spell_of_teleport")),
            SoundSource.PLAYERS,
            1.0F,
            1.0F / (RandomSource.create().nextFloat() * 0.5F + 1.0F)
        );
        return entityarrow;
    }
}
