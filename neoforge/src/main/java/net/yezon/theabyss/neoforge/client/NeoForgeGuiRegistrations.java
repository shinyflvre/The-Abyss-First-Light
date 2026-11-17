package net.yezon.theabyss.neoforge.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.yezon.theabyss.platform.client.event.RegisterGuiLayersEvent;
import net.yezon.theabyss.platform.client.event.RegisterMenuScreensEvent;
import net.yezon.theabyss.platform.client.gui.GuiLayer;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.services.GuiPlatformHooks;

public final class NeoForgeGuiRegistrations {
    private NeoForgeGuiRegistrations() {
    }

    public static void installHooks() {
        GuiPlatformHooks.install(new NeoForgeGuiPlatformHooks());
    }

    public static void register(IEventBus bus) {
        bus.addListener(net.neoforged.neoforge.client.event.RegisterGuiLayersEvent.class, e -> AbyssEventBus.post(new RegisterGuiLayersEvent() {
            @Override
            public void registerAbove(ResourceLocation other, ResourceLocation id, GuiLayer layer) {
                e.registerAbove(other, id, layer::render);
            }

            @Override
            public void registerBelow(ResourceLocation other, ResourceLocation id, GuiLayer layer) {
                e.registerBelow(other, id, layer::render);
            }
        }));
        bus.addListener(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent.class, e -> AbyssEventBus.post(new RegisterMenuScreensEvent() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type,
                ScreenConstructor<M, U> constructor) {
                e.<M, U>register(type, constructor::create);
            }
        }));
    }
}
