package net.yezon.theabyss.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;
import net.yezon.theabyss.init.TheabyssModItems;

public class NightBladeBossCloneEntity extends Monster {
    private final ServerBossEvent bossInfo = new ServerBossEvent(
        this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS
    );

    public NightBladeBossCloneEntity(EntityType<NightBladeBossCloneEntity> type, Level world) {
        super(type, world);
        this.xpReward = 37;
        this.setCustomName(Component.translatable("entity.theabyss.night_blade_boss_clone"));
        this.setCustomNameVisible(true);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TheabyssModItems.ABYSS_SWORD.get()));
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(TheabyssModItems.ABYSS_SWORD.get()));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AbyssMeleeAttackGoal(this, 1.2, false));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, AbyssalLionEntity.class, false, false));
    }

    @Override
    public boolean hurt(DamageSource damagesource, float amount) {
        if (!(this.level() instanceof ServerLevel level)) {
            return false;
        }
        if (damagesource.getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker && attacker.distanceTo(this) < 5.0F
            && this.getRandom().nextFloat() < 0.2F && net.yezon.theabyss.boss.nightblade.NightbladeCombat.sidestep(this, attacker)) {
            return false;
        }
        if (damagesource.is(DamageTypes.IN_FIRE)
            || damagesource.is(DamageTypes.CAMPFIRE)
            || damagesource.getDirectEntity() instanceof AbstractArrow
            || damagesource.getDirectEntity() instanceof ThrownPotion
            || damagesource.getDirectEntity() instanceof AreaEffectCloud
            || damagesource.is(DamageTypes.FALL)
            || damagesource.is(DamageTypes.CACTUS)
            || damagesource.is(DamageTypes.DROWN)
            || damagesource.is(DamageTypes.EXPLOSION)
            || damagesource.is(DamageTypes.TRIDENT)
            || damagesource.is(DamageTypes.FALLING_ANVIL)
            || damagesource.is(DamageTypes.DRAGON_BREATH)
            || damagesource.is(DamageTypes.WITHER)
            || damagesource.is(DamageTypes.WITHER_SKULL)) {
            return false;
        }
        return super.hurt(damagesource, amount);
    }

    @Override
    public boolean canUsePortal(boolean ignorePassenger) {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossInfo.removePlayer(player);
    }

    @Override
    protected void customServerAiStep() {
        ServerLevel level = (ServerLevel) this.level();
        super.customServerAiStep();
        this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.MAX_HEALTH, 150.0)
            .add(Attributes.ARMOR, 5.0)
            .add(Attributes.ATTACK_DAMAGE, 2.0)
            .add(Attributes.FOLLOW_RANGE, 64.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 2.0)
            .add(Attributes.ATTACK_KNOCKBACK, 2.0);
    }
}
