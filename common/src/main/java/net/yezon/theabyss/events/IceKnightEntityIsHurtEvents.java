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

public class IceKnightEntityIsHurtEvents {
    public static void execute(LevelAccessor world, double x, double y, double z) {
        if (Math.random() < 0.3) {
            if (world instanceof ServerLevel _level) {
                Entity entityToSpawn = TheabyssModEntities.SEEKER.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                if (entityToSpawn != null) {
                    entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                }
            }

            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.blaze.shoot")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F
                    );
                } else {
                    _level.playLocalSound(
                        x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.blaze.shoot")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                    );
                }
            }
        }
    }
}
