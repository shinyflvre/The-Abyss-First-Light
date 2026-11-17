package net.yezon.theabyss.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.network.TheabyssModVariables;

public class Friendly1Events {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null) {
            return;
        }

        TheabyssModVariables.updatePlayerVariables(entity, capability -> {
            capability.FriendMode = true;
            capability.syncPlayerVariables(entity);
        });
        if (world instanceof Level _level) {
            SoundEvent _sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.note_block.hat"));
            if (!_level.isClientSide()) {
                _level.playSound((Player)null, BlockPos.containing(x, y, z), _sound, SoundSource.PLAYERS, 1.0F, 1.0F);
            } else {
                _level.playLocalSound(x, y, z, _sound, SoundSource.PLAYERS, 1.0F, 1.0F, false);
            }
        }

        if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.translatable("ring.theabyss.mode.only_mobs"), true);
        }
    }
}
