package net.yezon.theabyss.events;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModSounds;
import net.yezon.theabyss.network.DreamShiftMessage;

public final class SwitchRealityEvents {
    private static final int COOLDOWN = 600;
    private static final int WARP_DELAY = 25;
    private static final int ARRIVAL_SOUND_DELAY = 10;
    private static final ResourceKey<Level> THE_ABYSS = ResourceKey.create(Registries.DIMENSION, Identifier.parse("theabyss:the_abyss"));

    private SwitchRealityEvents() {
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        if (!(entity instanceof Player player)) {
            return;
        }
        ResourceKey<Level> here = player.level().dimension();
        ResourceKey<Level> target = here == Level.OVERWORLD ? THE_ABYSS : here == THE_ABYSS ? Level.OVERWORLD : null;
        if (target == null) {
            return;
        }
        player.getCooldowns().addCooldown(itemstack, COOLDOWN);
        if (!(player instanceof ServerPlayer serverPlayer) || !(world instanceof ServerLevel level)) {
            return;
        }
        DreamShiftMessage.send(serverPlayer);
        level.playSound(null, BlockPos.containing(x, y, z), TheabyssModSounds.WARP_ENTERING.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        TheabyssMod.queueServerWork(WARP_DELAY, () -> {
            ServerLevel destination = level.getServer().getLevel(target);
            if (destination == null || serverPlayer.isRemoved()) {
                return;
            }
            serverPlayer.teleportTo(destination, Math.round(serverPlayer.getX()) + 0.5, Math.round(serverPlayer.getY()), Math.round(serverPlayer.getZ()) + 0.5,
                Set.of(), serverPlayer.getYRot(), serverPlayer.getXRot(), true);
            serverPlayer.getCooldowns().addCooldown(itemstack, COOLDOWN - WARP_DELAY);
            TheabyssMod.queueServerWork(ARRIVAL_SOUND_DELAY, () -> serverPlayer.level().playSound(null, serverPlayer.blockPosition(),
                TheabyssModSounds.WARP_ENTERED.get(), SoundSource.NEUTRAL, 2.0F, 1.0F));
        });
    }
}
