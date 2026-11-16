package net.yezon.theabyss.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.yezon.theabyss.platform.entity.MultipartEntity;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;
import net.yezon.theabyss.platform.services.EntityHooks;
import net.yezon.theabyss.init.TheabyssModEntities;

public class AbyssaurEntity extends Monster implements GeoEntity, MultipartEntity {
    private static final String ANIM = "animation.abyssaur.";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop(ANIM + "idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop(ANIM + "walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop(ANIM + "run");
    static final String[] ACTIONS = {"bite", "lunge", "tail_sweep", "roar", "breath", "stagger"};
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private AbyssaurAttackGoal attackGoal;
    private final Entity head;
    private float turn;
    private float turnO;
    static final float WALK_TURN = 6.0F;
    float tickStartYaw;

    public AbyssaurEntity(EntityType<AbyssaurEntity> type, Level world) {
        super(type, world);
        this.xpReward = 20;
        this.head = EntityHooks.get().createPart(this, 1.5F, 1.4F);
        this.moveControl = new SlowTurnMoveControl(this);
    }

    @Override
    public Entity[] abyssParts() {
        return new Entity[] {this.head};
    }

    @Override
    public void recreateFromPacket(net.minecraft.network.protocol.game.ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.head.setId(packet.getId() + 1);
    }

    @Override
    public void tick() {
        this.tickStartYaw = this.getYRot();
        super.tick();
        float yaw = this.yBodyRot * ((float)Math.PI / 180.0F);
        double forward = 2.55;
        net.minecraft.world.phys.Vec3 at = this.position().add(-Mth.sin(yaw) * forward, 1.15, Mth.cos(yaw) * forward);
        this.head.xo = this.head.getX();
        this.head.yo = this.head.getY();
        this.head.zo = this.head.getZ();
        this.head.setPos(at.x, at.y, at.z);
        if (this.level().isClientSide()) {
            this.turnO = this.turn;
            this.turn += (Mth.wrapDegrees(this.yBodyRot - this.yBodyRotO) - this.turn) * 0.3F;
        }
    }

    public float debugTickStartYaw() {
        return this.tickStartYaw;
    }

    public float turn(float partialTick) {
        return Mth.lerp(partialTick, this.turnO, this.turn);
    }

    @Override
    public int getMaxHeadYRot() {
        return 30;
    }

    @Override
    public int getHeadRotSpeed() {
        return 4;
    }

    static final class SlowTurnMoveControl extends net.minecraft.world.entity.ai.control.MoveControl {
        SlowTurnMoveControl(AbyssaurEntity mob) {
            super(mob);
        }

        @Override
        public void tick() {
            boolean moving = this.operation == Operation.MOVE_TO;
            float before = ((AbyssaurEntity)this.mob).tickStartYaw;
            double wantedYaw = Math.toDegrees(Math.atan2(this.wantedZ - this.mob.getZ(), this.wantedX - this.mob.getX())) - 90.0;
            float need = Math.abs(Mth.wrapDegrees((float)wantedYaw - before));
            super.tick();
            if (!moving) {
                return;
            }
            float step = Mth.clamp(Mth.wrapDegrees(this.mob.getYRot() - before), -WALK_TURN, WALK_TURN);
            this.mob.setYRot(before + step);
            float factor = need > 70.0F ? 0.25F : need > 35.0F ? 0.6F : 1.0F;
            this.mob.setSpeed(this.mob.getSpeed() * factor);
        }
    }

    static float halfHearts(float hp) {
        return Math.max(1, Math.round(hp));
    }

    public void devHold(boolean hold) {
        if (this.attackGoal != null) {
            this.attackGoal.devHold = hold;
            if (hold) {
                this.attackGoal.devStop();
            }
        }
    }

    public void devAttack(String name, net.minecraft.world.entity.LivingEntity target) {
        if (this.attackGoal != null) {
            this.attackGoal.devForce(name, target);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<AbyssaurEntity>("base", 6, this::baseAnimation));
        AnimationController<AbyssaurEntity> action = new AnimationController<>("action", 3, test -> PlayState.STOP);
        for (String name : ACTIONS) {
            action.triggerableAnim(name, RawAnimation.begin().thenPlay(ANIM + name));
        }
        controllers.add(action);
    }

    private PlayState baseAnimation(AnimationTest<AbyssaurEntity> test) {
        if (test.isMoving()) {
            float speed = this.walkAnimation.speed();
            if (speed > 0.7F) {
                test.controller().setAnimationSpeed(1.0);
                return test.setAndContinue(RUN);
            }
            test.controller().setAnimationSpeed(Mth.clamp(speed * 1.7F, 0.6F, 1.4F));
            return test.setAndContinue(WALK);
        }
        test.controller().setAnimationSpeed(1.0);
        return test.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return distSqr > 128.0 * 128.0 && this.getTarget() == null;
    }

    @Override
    public int getTeamColor() {
        return net.yezon.theabyss.ring.EagleBossTracker.OUTLINE;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.attackGoal = new AbyssaurAttackGoal(this);
        this.goalSelector.addGoal(1, this.attackGoal);
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8, 240));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
    }

    @Override
    public SoundEvent getAmbientSound() {
        return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:abyssaur_idle"));
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:abyssaur_hurt"));
    }

    @Override
    public SoundEvent getDeathSound() {
        return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:abyssaur_death"));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damagesource, float amount) {
        if (damagesource.is(DamageTypes.IN_FIRE) || damagesource.is(DamageTypes.CAMPFIRE)) {
            return false;
        }
        return super.hurtServer(level, damagesource, amount);
    }

    @Override
    public boolean canUsePortal(boolean ignorePassenger) {
        return false;
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
        event.register(
            TheabyssModEntities.ABYSSAUR.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, world, reason, pos, random) -> world.getDifficulty() != Difficulty.PEACEFUL
                && Monster.isDarkEnoughToSpawn(world, pos, random)
                && Mob.checkMobSpawnRules(entityType, world, reason, pos, random),
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.24)
            .add(Attributes.MAX_HEALTH, 200.0)
            .add(Attributes.ARMOR, 0.3)
            .add(Attributes.ATTACK_DAMAGE, 15.0)
            .add(Attributes.FOLLOW_RANGE, 24.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }
}
