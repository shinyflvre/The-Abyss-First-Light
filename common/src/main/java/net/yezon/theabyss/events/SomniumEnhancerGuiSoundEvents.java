package net.yezon.theabyss.events;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.registries.BuiltInRegistries;

public class SomniumEnhancerGuiSoundEvents {
    public static void execute(LevelAccessor world, double x, double y, double z) {
        if (world instanceof Level _level) {
            if (!_level.isClientSide()) {
                _level.playSound(
                    (Player)null,
                    BlockPos.containing(x, y, z),
                    BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:somnium_infuser_bubble")),
                    SoundSource.NEUTRAL,
                    0.3F,
                    1.0F
                );
            } else {
                _level.playLocalSound(
                    x,
                    y,
                    z,
                    BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:somnium_infuser_bubble")),
                    SoundSource.NEUTRAL,
                    0.3F,
                    1.0F,
                    false
                );
            }
        }
    }
}
