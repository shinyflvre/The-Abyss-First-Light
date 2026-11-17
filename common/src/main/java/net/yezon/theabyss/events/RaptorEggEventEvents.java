package net.yezon.theabyss.events;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.yezon.theabyss.init.TheabyssModEntities;

public class RaptorEggEventEvents {
    public static void execute(LevelAccessor world, double x, double y, double z) {
        world.destroyBlock(BlockPos.containing(x, y, z), false);
        if (world instanceof Level _level) {
            if (!_level.isClientSide()) {
                _level.playSound(
                    (Player)null,
                    BlockPos.containing(x, y, z),
                    BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:raptor_die")),
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.0F
                );
            } else {
                _level.playLocalSound(
                    x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:raptor_die")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                );
            }
        }

        if (world instanceof ServerLevel _level) {
            Entity entityToSpawn = TheabyssModEntities.RAPTOR.get().spawn(_level, BlockPos.containing(x + 8.0, y + 6.0, z), MobSpawnType.MOB_SUMMONED);
            if (entityToSpawn != null) {
            }
        }

        if (world instanceof ServerLevel _level) {
            Entity entityToSpawn = TheabyssModEntities.RAPTOR.get().spawn(_level, BlockPos.containing(x, y + 6.0, z + 8.0), MobSpawnType.MOB_SUMMONED);
            if (entityToSpawn != null) {
            }
        }

        if (world instanceof ServerLevel _level) {
            Entity entityToSpawn = TheabyssModEntities.RAPTOR.get().spawn(_level, BlockPos.containing(x + 8.0, y + 6.0, z + 8.0), MobSpawnType.MOB_SUMMONED);
            if (entityToSpawn != null) {
            }
        }
    }
}
