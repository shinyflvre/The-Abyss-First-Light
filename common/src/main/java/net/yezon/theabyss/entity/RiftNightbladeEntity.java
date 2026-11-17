package net.yezon.theabyss.entity;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.rift.RiftStory;

public class RiftNightbladeEntity extends PathfinderMob implements GeoEntity {
    private static final RawAnimation STAND = RawAnimation.begin().thenLoop("animation.rift_nightblade.stand");
    private static final double LOOK_RANGE = 64.0;
    private static final int OUTLINE = 0x3FD2FF;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RiftNightbladeEntity(EntityType<RiftNightbladeEntity> type, Level world) {
        super(type, world);
        this.xpReward = 0;
        this.setPersistenceRequired();
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this) {
            @Override
            public void clientTick() {
            }
        };
    }

    @Override
    public void tick() {
        super.tick();
        this.yBodyRot = this.getYRot();
        this.yBodyRotO = this.getYRot();
    }

    @Override
    protected void customServerAiStep() {
        ServerLevel level = (ServerLevel) this.level();
        super.customServerAiStep();
        if (!this.getMainHandItem().isEmpty() || !this.getOffhandItem().isEmpty()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
        Player player = level.getNearestPlayer(this.getX(), this.getEyeY(), this.getZ(), LOOK_RANGE, target -> !target.isSpectator());
        if (player != null) {
            this.getLookControl().setLookAt(player, 10.0F, (float)this.getMaxHeadXRot());
        }
    }

    @Override
    public boolean broadcastToPlayer(ServerPlayer player) {
        return !RiftStory.bossDefeated(player) && super.broadcastToPlayer(player);
    }

    @Override
    public boolean isCurrentlyGlowing() {
        return this.level().isClientSide() ? net.yezon.theabyss.client.fx.RiftNightbladeOutline.visible(this) : super.isCurrentlyGlowing();
    }

    @Override
    public int getTeamColor() {
        return OUTLINE;
    }

    @Override
    public int getMaxHeadYRot() {
        return 75;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!(this.level() instanceof ServerLevel level)) {
            return false;
        }
        if (source.getEntity() instanceof ServerPlayer player) {
            RiftStory.onHit(player);
        }
        return source.is(DamageTypes.GENERIC_KILL) && super.hurt(source, amount);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer && hand == InteractionHand.MAIN_HAND) {
            RiftStory.onTalk(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canUsePortal(boolean ignorePassenger) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<RiftNightbladeEntity>(this, "base", 5, test -> test.setAndContinue(STAND)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.0)
            .add(Attributes.MAX_HEALTH, 1000.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
            .add(Attributes.FOLLOW_RANGE, LOOK_RANGE);
    }
}
