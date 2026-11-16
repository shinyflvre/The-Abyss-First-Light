package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;

public abstract class RegisterMenuScreensEvent extends Event {
    public abstract <M extends net.minecraft.world.inventory.AbstractContainerMenu, U extends net.minecraft.client.gui.screens.Screen & net.minecraft.client.gui.screens.inventory.MenuAccess<M>> void register(net.minecraft.world.inventory.MenuType<? extends M> a0, ScreenConstructor<M, U> a1);

    @FunctionalInterface
    public interface ScreenConstructor<M extends net.minecraft.world.inventory.AbstractContainerMenu, U extends net.minecraft.client.gui.screens.Screen & net.minecraft.client.gui.screens.inventory.MenuAccess<M>> {
        U create(M menu, net.minecraft.world.entity.player.Inventory inventory, net.minecraft.network.chat.Component title);
    }
}
