package net.yezon.theabyss.platform.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface IContainerFactory<T extends AbstractContainerMenu> {
    T create(int windowId, Inventory inventory, @Nullable RegistryFriendlyByteBuf data);
}
