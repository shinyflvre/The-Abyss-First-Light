package net.yezon.theabyss.neoforge.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;
import net.yezon.theabyss.platform.event.entity.living.MobEffectEvent;
import net.yezon.theabyss.platform.event.entity.living.MobSpawnEvent;
import org.jspecify.annotations.Nullable;

public final class NeoForgeEntityEvents {
    private NeoForgeEntityEvents() {
    }

    public static void register(IEventBus bus) {
        if (AbyssEventBus.hasListeners(RegisterSpawnPlacementsEvent.class)) {
            bus.addListener(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.class, e -> AbyssEventBus.post(new RegisterSpawnPlacementsEvent() {
                @Override
                public <T extends Entity> void register(EntityType<T> entityType, @Nullable SpawnPlacementType placementType, Heightmap.@Nullable Types heightmap,
                    SpawnPlacements.SpawnPredicate<T> predicate, Operation operation) {
                    e.register(entityType, placementType, heightmap, predicate,
                        net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.valueOf(operation.name()));
                }
            }));
        }
        for (EventPriority p : AbyssEventBus.priorities(MobSpawnEvent.PositionCheck.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.living.MobSpawnEvent.PositionCheck.class, e -> {
                MobSpawnEvent.PositionCheck c = new MobSpawnEvent.PositionCheck(e.getEntity(), e.getLevel(), e.getSpawnType(),
                    MobSpawnEvent.PositionCheck.Result.valueOf(e.getResult().name()));
                AbyssEventBus.post(p, c);
                e.setResult(net.neoforged.neoforge.event.entity.living.MobSpawnEvent.PositionCheck.Result.valueOf(c.getResult().name()));
            });
        }
        for (EventPriority p : AbyssEventBus.priorities(MobEffectEvent.Applicable.class)) {
            NeoForge.EVENT_BUS.addListener(priority(p), true, net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable.class, e -> {
                MobEffectEvent.Applicable c = new MobEffectEvent.Applicable(e.getEffectInstance(), e.getEffectSource(), e.getEntity(),
                    MobEffectEvent.Applicable.Result.valueOf(e.getResult().name()));
                AbyssEventBus.post(p, c);
                e.setResult(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable.Result.valueOf(c.getResult().name()));
            });
        }
    }

    private static net.neoforged.bus.api.EventPriority priority(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> net.neoforged.bus.api.EventPriority.HIGHEST;
            case HIGH -> net.neoforged.bus.api.EventPriority.HIGH;
            case NORMAL -> net.neoforged.bus.api.EventPriority.NORMAL;
            case LOW -> net.neoforged.bus.api.EventPriority.LOW;
            case LOWEST -> net.neoforged.bus.api.EventPriority.LOWEST;
        };
    }
}
