package net.yezon.theabyss.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModParticleTypes;

public class AbyssEchoEntity extends PathfinderMob {
    public static final EntityDataAccessor<String> OWNER = SynchedEntityData.defineId(AbyssEchoEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> DASHING = SynchedEntityData.defineId(AbyssEchoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int MAX_AGE = 200;

    public AbyssEchoEntity(EntityType<AbyssEchoEntity> type, Level level) {
        super(type, level);
        this.setNoAi(true);
        this.setNoGravity(true);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TheabyssModItems.ABYSS_SWORD.get()));
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(TheabyssModItems.ABYSS_SWORD.get()));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, "");
        builder.define(DASHING, false);
    }

    public String ownerId() {
        return this.entityData.get(OWNER);
    }

    public void setOwnerId(String id) {
        this.entityData.set(OWNER, id);
    }

    public boolean isDashing() {
        return this.entityData.get(DASHING);
    }

    public void setDashing(boolean dashing) {
        this.entityData.set(DASHING, dashing);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (this.isDashing()) {
                Vec3 from = new Vec3(this.xo, this.yo, this.zo);
                Vec3 to = this.position();
                int steps = Math.max(1, (int)Math.ceil(from.distanceTo(to) / 0.2));
                for (int i = 0; i < steps; i++) {
                    Vec3 at = from.lerp(to, (i + this.random.nextFloat()) / steps);
                    this.level().addParticle(TheabyssModParticleTypes.NIGHTBLADE_TRAIL.get(), at.x, at.y + 1.3, at.z, 0.0, 0.0, 0.0);
                    this.level().addParticle(TheabyssModParticleTypes.NIGHTBLADE_TRAIL.get(), at.x, at.y + 0.6, at.z, 0.0, 0.0, 0.0);
                }
            }
        } else if (this.tickCount > MAX_AGE) {
            this.discard();
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
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0);
    }
}
