package net.yezon.theabyss.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModEntities;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class SpawnFixes {
    private static final ResourceKey<Level> FROST = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "frost_world"));

    private SpawnFixes() {
    }

    public static void register(RegisterSpawnPlacementsEvent event) {
        monster(event, TheabyssModEntities.INFECTED_WOLF.get());
        monster(event, TheabyssModEntities.POWERED_WOLF.get());
        monster(event, TheabyssModEntities.GLOW_PUG.get());
        monster(event, TheabyssModEntities.HUMMINGBIRD.get());
        monster(event, TheabyssModEntities.INFECTED_DRAGONFLY.get());
        event.register(EntityTypes.POLAR_BEAR, null, null,
            (type, level, reason, pos, random) -> level.getLevel().dimension() == FROST && Mob.checkMobSpawnRules(type, level, reason, pos, random),
            RegisterSpawnPlacementsEvent.Operation.OR);
    }

    private static <T extends Monster> void monster(RegisterSpawnPlacementsEvent event, EntityType<T> type) {
        event.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (t, level, reason, pos, random) -> level.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn(level, pos, random)
                && Mob.checkMobSpawnRules(t, level, reason, pos, random),
            RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public static void onPositionCheck(MobSpawnEvent.PositionCheck event) {
        if (event.getEntity() instanceof PolarBear bear && event.getLevel().getLevel().dimension() == FROST
            && (event.getSpawnType() == EntitySpawnReason.NATURAL || event.getSpawnType() == EntitySpawnReason.CHUNK_GENERATION)
            && event.getLevel().noCollision(bear) && !event.getLevel().containsAnyLiquid(bear.getBoundingBox())) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.SUCCEED);
        }
    }
}
