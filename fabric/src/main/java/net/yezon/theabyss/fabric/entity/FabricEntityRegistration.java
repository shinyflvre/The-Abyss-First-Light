package net.yezon.theabyss.fabric.entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.levelgen.Heightmap;
import net.yezon.theabyss.fabric.mixin.entity.SpawnPlacementsAccessor;
import net.yezon.theabyss.fabric.mixin.entity.SpawnPlacementsDataAccessor;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.event.entity.EntityAttributeCreationEvent;
import net.yezon.theabyss.platform.event.entity.RegisterSpawnPlacementsEvent;
import org.jspecify.annotations.Nullable;

public final class FabricEntityRegistration {
    private FabricEntityRegistration() {
    }

    public static void registerAttributes() {
        AbyssEventBus.post(new EntityAttributeCreationEvent() {
            @Override
            public void put(EntityType<? extends LivingEntity> type, AttributeSupplier supplier) {
                FabricDefaultAttributeRegistry.register(type, supplier);
            }
        });
    }

    public static void registerSpawnPlacements() {
        Map<EntityType<?>, Placement<?>> placements = new LinkedHashMap<>();
        AbyssEventBus.post(new RegisterSpawnPlacementsEvent() {
            @Override
            public <T extends Entity> void register(EntityType<T> entityType, @Nullable SpawnPlacementType placementType, Heightmap.@Nullable Types heightmap,
                SpawnPlacements.SpawnPredicate<T> predicate, Operation operation) {
                FabricEntityRegistration.register(placements, entityType, placementType, heightmap, predicate, operation);
            }
        });
        Map<EntityType<?>, Object> data = SpawnPlacementsAccessor.theabyss$data();
        placements.forEach((type, placement) -> {
            data.remove(type);
            apply(type, placement);
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> void register(Map<EntityType<?>, Placement<?>> placements, EntityType<T> type, @Nullable SpawnPlacementType placementType,
        Heightmap.@Nullable Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate, RegisterSpawnPlacementsEvent.Operation operation) {
        Placement<T> placement = (Placement<T>)placements.get(type);
        if (placement == null) {
            Object existing = SpawnPlacementsAccessor.theabyss$data().get(type);
            if (existing != null) {
                placement = new Placement<>((SpawnPlacements.SpawnPredicate<T>)((SpawnPlacementsDataAccessor)existing).theabyss$predicate(),
                    SpawnPlacements.getPlacementType(type), SpawnPlacements.getHeightmapType(type));
                placements.put(type, placement);
            }
        }
        if (placement == null) {
            if (placementType == null) {
                throw new NullPointerException("Registering a new spawn predicate requires a placement type: " + BuiltInRegistries.ENTITY_TYPE.getKey(type));
            }
            if (heightmap == null) {
                throw new NullPointerException("Registering a new spawn predicate requires a heightmap type: " + BuiltInRegistries.ENTITY_TYPE.getKey(type));
            }
            placements.put(type, new Placement<>(predicate, placementType, heightmap));
            return;
        }
        if (operation != RegisterSpawnPlacementsEvent.Operation.REPLACE && (heightmap != null || placementType != null)) {
            throw new IllegalStateException("Placement or heightmap types need the REPLACE operation: " + BuiltInRegistries.ENTITY_TYPE.getKey(type));
        }
        placement.merge(operation, predicate, placementType, heightmap);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void apply(EntityType<?> type, Placement<?> placement) {
        SpawnPlacementsAccessor.theabyss$register((EntityType)type, placement.placementType, placement.heightmap, (SpawnPlacements.SpawnPredicate)placement.build());
    }

    private static final class Placement<T extends Entity> {
        private final SpawnPlacements.SpawnPredicate<T> original;
        private final List<SpawnPlacements.SpawnPredicate<T>> any = new ArrayList<>();
        private final List<SpawnPlacements.SpawnPredicate<T>> all = new ArrayList<>();
        private SpawnPlacements.@Nullable SpawnPredicate<T> replacement;
        private SpawnPlacementType placementType;
        private Heightmap.Types heightmap;

        private Placement(SpawnPlacements.SpawnPredicate<T> original, SpawnPlacementType placementType, Heightmap.Types heightmap) {
            this.original = original;
            this.placementType = placementType;
            this.heightmap = heightmap;
        }

        private void merge(RegisterSpawnPlacementsEvent.Operation operation, SpawnPlacements.SpawnPredicate<T> predicate, @Nullable SpawnPlacementType placementType,
            Heightmap.@Nullable Types heightmap) {
            switch (operation) {
                case AND -> this.all.add(predicate);
                case OR -> this.any.add(predicate);
                case REPLACE -> {
                    this.replacement = predicate;
                    if (placementType != null) {
                        this.placementType = placementType;
                    }
                    if (heightmap != null) {
                        this.heightmap = heightmap;
                    }
                }
            }
        }

        private SpawnPlacements.SpawnPredicate<T> build() {
            SpawnPlacements.SpawnPredicate<T> replaced = this.replacement;
            if (replaced != null) {
                return replaced;
            }
            SpawnPlacements.SpawnPredicate<T> base = this.original;
            List<SpawnPlacements.SpawnPredicate<T>> any = List.copyOf(this.any);
            List<SpawnPlacements.SpawnPredicate<T>> all = List.copyOf(this.all);
            return (type, level, reason, pos, random) -> {
                boolean passed = base.test(type, level, reason, pos, random);
                for (int i = 0; !passed && i < any.size(); i++) {
                    passed = any.get(i).test(type, level, reason, pos, random);
                }
                if (!passed) {
                    return false;
                }
                for (SpawnPlacements.SpawnPredicate<T> predicate : all) {
                    if (!predicate.test(type, level, reason, pos, random)) {
                        return false;
                    }
                }
                return true;
            };
        }
    }
}
