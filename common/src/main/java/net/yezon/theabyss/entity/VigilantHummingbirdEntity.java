package net.yezon.theabyss.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;
import net.yezon.theabyss.init.TheabyssModEntities;

public class VigilantHummingbirdEntity extends HummingbirdEntity {
    private static final int NEARBY_RANGE = 40;
    private static final int MAX_NEARBY = 6;

    public VigilantHummingbirdEntity(EntityType<? extends VigilantHummingbirdEntity> type, Level world) {
        super(type, world);
        this.setTexture("vigilant_hummingbird");
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean isPreventingPlayerRest(Player player) {
        return false;
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
        event.register(TheabyssModEntities.VIGILANT_HUMMINGBIRD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            VigilantHummingbirdEntity::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static boolean checkSpawnRules(EntityType<VigilantHummingbirdEntity> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos,
        RandomSource random) {
        if (!Mob.checkMobSpawnRules(type, level, reason, pos, random)) {
            return false;
        }
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) {
            return true;
        }
        return random.nextInt(3) == 0 && level.getEntitiesOfClass(VigilantHummingbirdEntity.class, new AABB(pos).inflate(NEARBY_RANGE)).size() < MAX_NEARBY;
    }
}
