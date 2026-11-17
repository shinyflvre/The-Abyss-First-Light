package net.yezon.theabyss.block.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.mixin.accessor.BaseContainerBlockEntityAccessor;
import org.jspecify.annotations.Nullable;

public final class AbyssContainerLock {
    private AbyssContainerLock() {
    }

    public static boolean canOpen(RandomizableContainerBlockEntity container, Player player) {
        return (container.getLootTable() == null || !player.isSpectator())
            && (player.isSpectator() || ((BaseContainerBlockEntityAccessor) container).theabyss$getLockKey().unlocksWith(player.getMainHandItem()));
    }

    public static @Nullable AbstractContainerMenu createMenu(RandomizableContainerBlockEntity container, int containerId, Inventory inventory, Player player,
                                                             MenuFactory factory) {
        if (container.canOpen(player)) {
            container.unpackLootTable(inventory.player);
            return factory.create(containerId, inventory);
        }
        if (!player.isSpectator()) {
            sendLockedNotifications(Vec3.atCenterOf(container.getBlockPos()), player, container.getDisplayName());
        }
        return null;
    }

    public static void sendLockedNotifications(Vec3 pos, Player player, Component displayName) {
        Level level = player.level();
        player.displayClientMessage(Component.translatable("container.isLocked", displayName), true);
        if (!level.isClientSide()) {
            level.playSound(null, pos.x(), pos.y(), pos.z(), SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @FunctionalInterface
    public interface MenuFactory {
        AbstractContainerMenu create(int containerId, Inventory inventory);
    }
}
