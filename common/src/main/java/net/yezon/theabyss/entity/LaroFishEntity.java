package net.yezon.theabyss.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.PathType;
import net.yezon.theabyss.platform.common.NeoForgeMod;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;

public class LaroFishEntity extends Squid {
    public LaroFishEntity(EntityType<LaroFishEntity> type, Level world) {
        super(type, world);
        this.xpReward = 0;
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.moveControl = new MoveControl(this) {
            @Override
            public void tick() {
                LaroFishEntity fish = LaroFishEntity.this;
                if (fish.isInWater()) {
                    fish.setDeltaMovement(fish.getDeltaMovement().add(0.0, 0.005, 0.0));
                }

                if (this.operation == MoveControl.Operation.MOVE_TO && !fish.getNavigation().isDone()) {
                    double dx = this.wantedX - fish.getX();
                    double dy = this.wantedY - fish.getY();
                    double dz = this.wantedZ - fish.getZ();
                    float yaw = (float)(Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
                    float speed = (float)(this.speedModifier * fish.getAttributeValue(Attributes.MOVEMENT_SPEED));
                    fish.setYRot(this.rotlerp(fish.getYRot(), yaw, 10.0F));
                    fish.yBodyRot = fish.getYRot();
                    fish.yHeadRot = fish.getYRot();
                    if (fish.isInWater()) {
                        fish.setSpeed((float)fish.getAttributeValue(Attributes.MOVEMENT_SPEED));
                        float pitch = -((float)(Mth.atan2(dy, (float)Math.sqrt(dx * dx + dz * dz)) * (180.0 / Math.PI)));
                        pitch = Mth.clamp(Mth.wrapDegrees(pitch), -85.0F, 85.0F);
                        fish.setXRot(this.rotlerp(fish.getXRot(), pitch, 5.0F));
                        float cosPitch = Mth.cos(fish.getXRot() * (float) (Math.PI / 180.0));
                        fish.setZza(cosPitch * speed);
                        fish.setYya((float)(speed * dy));
                    } else {
                        fish.setSpeed(speed * 0.05F);
                    }
                } else {
                    fish.setSpeed(0.0F);
                    fish.setYya(0.0F);
                    fish.setZza(0.0F);
                }
            }
        };
    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        return new WaterBoundPathNavigation(this, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new RandomSwimmingGoal(this, 1.0, 40));
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHitIn) {
        super.dropCustomDeathLoot(level, source, recentlyHitIn);
        this.spawnAtLocation(new ItemStack(TheabyssModItems.FISH.get()));
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
    public boolean checkSpawnObstruction(LevelReader world) {
        return world.isUnobstructed(this);
    }


    @Override
    public boolean canBeLeashed() {
        return false;
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
        event.register(
            TheabyssModEntities.LARO_FISH.get(),
            SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, world, reason, pos, random) -> world.getBlockState(pos).is(Blocks.WATER) && world.getBlockState(pos.above()).is(Blocks.WATER),
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.MAX_HEALTH, 4.0)
            .add(Attributes.ARMOR, 0.0)
            .add(Attributes.ATTACK_DAMAGE, 0.0)
            .add(Attributes.FOLLOW_RANGE, 16.0)
            .add(NeoForgeMod.SWIM_SPEED, 0.3);
    }
}
