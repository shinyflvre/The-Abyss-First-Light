package net.yezon.theabyss.fabric.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.yezon.theabyss.fabric.event.FabricHudLayers;
import net.yezon.theabyss.platform.client.event.RegisterGuiLayersEvent;
import net.yezon.theabyss.platform.client.event.RegisterKeyMappingsEvent;
import net.yezon.theabyss.platform.client.event.RegisterMenuScreensEvent;
import net.yezon.theabyss.platform.client.gui.GuiLayer;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.services.GuiPlatformHooks;

public final class FabricGuiRegistrations {
    private FabricGuiRegistrations() {
    }

    public static void installHooks() {
        GuiPlatformHooks.install(new FabricGuiPlatformHooks());
    }

    public static void register() {
        AbyssEventBus.post(new RegisterKeyMappingsEvent() {
            @Override
            public void register(KeyMapping key) {
                KeyBindingHelper.registerKeyBinding(key);
            }
        });
        AbyssEventBus.post(new RegisterMenuScreensEvent() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type,
                ScreenConstructor<M, U> constructor) {
                MenuScreens.<M, U>register(type, constructor::create);
            }
        });
        AbyssEventBus.post(new RegisterGuiLayersEvent() {
            @Override
            public void registerAbove(ResourceLocation other, ResourceLocation id, GuiLayer layer) {
                FabricHudLayers.above(other, id, layer);
            }

            @Override
            public void registerBelow(ResourceLocation other, ResourceLocation id, GuiLayer layer) {
                FabricHudLayers.below(other, id, layer);
            }
        });
        ScreenEvents.AFTER_INIT.register((minecraft, screen, width, height) -> ScreenEvents.afterRender(screen)
            .register((rendered, graphics, mouseX, mouseY, partialTick) -> FabricHudLayers.screenPost(rendered, graphics, mouseX, mouseY, partialTick)));
    }
}
