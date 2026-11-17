package net.yezon.theabyss.entity;


import net.minecraft.nbt.CompoundTag;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.compat.ValueInput;
import net.yezon.theabyss.compat.ValueOutput;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;
import org.jspecify.annotations.Nullable;

public class PlayerBodyEntity extends Monster implements GeoEntity {
    private static final RawAnimation FLOAT = RawAnimation.begin().thenLoop("animation.player_body.float");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final int LIFETIME = 800;
    private int age;

    public PlayerBodyEntity(EntityType<PlayerBodyEntity> type, Level world) {
        super(type, world);
        this.xpReward = 0;
        this.setNoAi(true);
        this.setPersistenceRequired();
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.death"));
    }

    @Override
    public boolean hurt(DamageSource damagesource, float amount) {
        if (!(this.level() instanceof ServerLevel level)) {
            return false;
        }
        if (damagesource.getDirectEntity() instanceof ThrownPotion
            || damagesource.getDirectEntity() instanceof AreaEffectCloud
            || damagesource.is(DamageTypes.FALL)
            || damagesource.is(DamageTypes.DROWN)
            || damagesource.is(DamageTypes.LIGHTNING_BOLT)
            || damagesource.is(DamageTypes.EXPLOSION)
            || damagesource.is(DamageTypes.TRIDENT)
            || damagesource.is(DamageTypes.FALLING_ANVIL)
            || damagesource.is(DamageTypes.DRAGON_BREATH)) {
            return false;
        }
        return super.hurt(damagesource, amount);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ENCHANT, this.getX(), this.getY(), this.getZ(), 20, 2.0, 2.0, 2.0, 0.05);
            if (++this.age >= LIFETIME) {
                this.discard();
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag outputTag) {
        ValueOutput output = ValueOutput.of(outputTag, this.registryAccess());
        super.addAdditionalSaveData(output.tag());
        output.putInt("BodyAge", this.age);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag inputTag) {
        ValueInput input = ValueInput.of(inputTag, this.registryAccess());
        super.readAdditionalSaveData(input.tag());
        this.age = input.getIntOr("BodyAge", 0);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<PlayerBodyEntity>(this, "float", 0, state -> state.setAndContinue(FLOAT)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.0)
            .add(Attributes.MAX_HEALTH, 10.0)
            .add(Attributes.ARMOR, 0.0)
            .add(Attributes.ATTACK_DAMAGE, 0.0)
            .add(Attributes.FOLLOW_RANGE, 16.0);
    }
}
