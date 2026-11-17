package net.yezon.theabyss.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModEntities;
import org.jspecify.annotations.Nullable;

public class EndSpiderEntity extends EnderMan {
    private static final TagKey<Block> DOES_NOT_TELEPORT_TO = TagKey.create(Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "enderman_does_not_teleport_to"));
    private static final ResourceLocation ENDERMAN_ATTACK_SPEED = ResourceLocation.withDefaultNamespace("attacking");
    private static final double BLINK_MIN = 6.0;
    private static final double BLINK_MAX = 24.0;
    private static final int BLINK_CHARGE = 10;
    private static final int BLINK_COOLDOWN = 100;
    private static final int BLINK_COOLDOWN_SPREAD = 60;
    private static final int BLINK_TRIES = 12;
    private static final float BLINK_ON_HIT = 0.35F;
    private int blinkCooldown = BLINK_COOLDOWN;
    private int blinkCharge;

    public EndSpiderEntity(EntityType<EndSpiderEntity> type, Level world) {
        super(type, world);
        this.xpReward = 6;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AbyssMeleeAttackGoal(this, 1.2, false));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(ENDERMAN_ATTACK_SPEED);
        }
    }

    @Override
    public SoundEvent getAmbientSound() {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.spider.ambient"));
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
        if (damagesource.is(DamageTypes.IN_FIRE)
            || damagesource.is(DamageTypes.CAMPFIRE)
            || damagesource.is(DamageTypes.FALL)
            || damagesource.is(DamageTypes.CACTUS)) {
            return false;
        }
        boolean hurt = damagesource.is(DamageTypeTags.IS_PROJECTILE) && !this.isPassenger()
            ? super.hurt(struck(level, damagesource), amount)
            : super.hurt(damagesource, amount);
        if (hurt && this.isAlive() && this.blinkCooldown <= 0 && this.blinkCharge == 0 && damagesource.getEntity() instanceof LivingEntity attacker
            && attacker != this && this.random.nextFloat() < BLINK_ON_HIT) {
            this.blinkTo(attacker);
        }
        return hurt;
    }

    @Override
    public void playStareSound() {
    }

    private static DamageSource struck(ServerLevel level, DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker instanceof Player player) {
            return level.damageSources().playerAttack(player);
        }
        if (attacker instanceof LivingEntity living) {
            return level.damageSources().mobAttack(living);
        }
        return level.damageSources().generic();
    }

    @Override
    protected void customServerAiStep() {
        ServerLevel level = (ServerLevel) this.level();
        super.customServerAiStep();
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || this.isPassenger()) {
            this.blinkCharge = 0;
            return;
        }
        if (this.blinkCharge > 0) {
            this.blinkCharge--;
            EndSpiderFx.charge(level, this, BLINK_CHARGE - this.blinkCharge);
            if (this.blinkCharge == 0) {
                this.blinkTo(target);
            }
            return;
        }
        if (this.blinkCooldown > 0) {
            this.blinkCooldown--;
            return;
        }
        double distance = this.distanceTo(target);
        if (distance > BLINK_MIN && distance < BLINK_MAX) {
            this.blinkCharge = BLINK_CHARGE;
        }
    }

    private void blinkTo(LivingEntity target) {
        this.blinkCooldown = BLINK_COOLDOWN + this.random.nextInt(BLINK_COOLDOWN_SPREAD);
        Vec3 look = target.getLookAngle();
        double behind = Math.atan2(-look.z, -look.x);
        for (int i = 0; i < BLINK_TRIES; i++) {
            double angle = behind + (this.random.nextDouble() - 0.5) * Math.PI;
            double radius = 2.0 + this.random.nextDouble() * 1.5;
            double x = target.getX() + Math.cos(angle) * radius;
            double y = target.getY() + this.random.nextInt(3) - 1;
            double z = target.getZ() + Math.sin(angle) * radius;
            if (this.teleportSpider(x, y, z)) {
                return;
            }
        }
    }

    public boolean teleportSpider(double x, double y, double z) {
        if (this.isPassenger() || !(this.level() instanceof ServerLevel level)) {
            return false;
        }
        Vec3 from = this.position();
        boolean result = this.randomTeleportAvoiding(x, y, z);
        if (result) {
            level.gameEvent(GameEvent.TELEPORT, from, GameEvent.Context.of(this));
            EndSpiderFx.teleport(level, this, from, this.position());
        }
        return result;
    }

    private static boolean avoided(BlockState state) {
        return state.is(DOES_NOT_TELEPORT_TO);
    }

    private boolean randomTeleportAvoiding(double xx, double yy, double zz) {
        double y = yy;
        Level level = this.level();
        WorldBorder border = level.getWorldBorder();
        double cx = Mth.clamp(xx, border.getMinX(), border.getMaxX() - 1.0E-5F);
        double cz = Mth.clamp(zz, border.getMinZ(), border.getMaxZ() - 1.0E-5F);
        BlockPos.MutableBlockPos pos = BlockPos.containing(cx, y, cz).mutable();
        if (level.hasChunkAt(pos)) {
            while (pos.getY() > level.getMinBuildHeight()) {
                pos.move(Direction.DOWN);
                BlockState state = level.getBlockState(pos);
                if (state.blocksMotion()) {
                    return this.checkPositionAndTeleport(cx, y, cz, state, level);
                }
                y--;
            }
        }
        return false;
    }

    private boolean checkPositionAndTeleport(double xx, double y, double zz, BlockState state, Level level) {
        if (avoided(state)) {
            return false;
        }
        AABB aabb = this.getDimensions(this.getPose()).makeBoundingBox(new Vec3(xx, y, zz));
        if (!level.noCollision(aabb) || level.containsAnyLiquid(aabb)) {
            return false;
        }
        BlockPos min = BlockPos.containing(aabb.minX, aabb.minY, aabb.minZ);
        BlockPos max = BlockPos.containing(aabb.maxX, aabb.maxY, aabb.maxZ);
        for (BlockPos p : BlockPos.betweenClosed(min, max)) {
            if (avoided(level.getBlockState(p))) {
                return false;
            }
        }
        BlockState below = level.getBlockState(BlockPos.containing(xx, y, zz).below());
        if (below.getFluidState().is(FluidTags.WATER)) {
            return false;
        }
        this.teleportTo(xx, y, zz);
        this.getNavigation().stop();
        return true;
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
        event.register(
            TheabyssModEntities.END_SPIDER.get(),
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
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.MAX_HEALTH, 25.0)
            .add(Attributes.ARMOR, 0.1)
            .add(Attributes.ATTACK_DAMAGE, 6.0)
            .add(Attributes.FOLLOW_RANGE, 16.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.1);
    }
}
