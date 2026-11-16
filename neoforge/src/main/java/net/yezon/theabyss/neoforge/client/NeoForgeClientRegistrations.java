package net.yezon.theabyss.neoforge.client;

import java.util.function.BiConsumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.yezon.theabyss.platform.client.CustomSkyboxRenderer;
import net.yezon.theabyss.platform.client.event.RegisterCustomEnvironmentEffectRendererEvent;
import net.yezon.theabyss.platform.client.event.RegisterGuiLayersEvent;
import net.yezon.theabyss.platform.client.event.RegisterMenuScreensEvent;
import net.yezon.theabyss.platform.client.event.RegisterParticleProvidersEvent;
import net.yezon.theabyss.platform.client.gui.GuiLayer;
import net.yezon.theabyss.platform.client.renderstate.RegisterRenderStateModifiersEvent;
import net.yezon.theabyss.platform.event.AbyssEventBus;

public final class NeoForgeClientRegistrations {
    private NeoForgeClientRegistrations() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(net.neoforged.neoforge.client.event.RegisterGuiLayersEvent.class, e -> AbyssEventBus.post(new RegisterGuiLayersEvent() {
            @Override
            public void registerAbove(Identifier other, Identifier id, GuiLayer layer) {
                e.registerAbove(other, id, layer::render);
            }

            @Override
            public void registerBelow(Identifier other, Identifier id, GuiLayer layer) {
                e.registerBelow(other, id, layer::render);
            }
        }));
        bus.addListener(net.neoforged.neoforge.client.event.RegisterCustomEnvironmentEffectRendererEvent.class,
            e -> AbyssEventBus.post(new RegisterCustomEnvironmentEffectRendererEvent() {
                @Override
                public void registerSkyboxRenderer(Identifier id, CustomSkyboxRenderer renderer) {
                    e.registerSkyboxRenderer(id, new net.neoforged.neoforge.client.CustomSkyboxRenderer() {
                        @Override
                        public boolean renderSky(net.minecraft.client.renderer.state.level.LevelRenderState levelRenderState,
                            net.minecraft.client.renderer.state.level.SkyRenderState skyRenderState, org.joml.Matrix4fc modelViewMatrix,
                            com.mojang.renderpearl.api.buffers.GpuBufferSlice skyFog) {
                            return renderer.renderSky(levelRenderState, skyRenderState, modelViewMatrix, skyFog);
                        }
                    });
                }
            }));
        bus.addListener(net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent.class,
            e -> AbyssEventBus.post(new RegisterRenderStateModifiersEvent() {
                @Override
                public <E extends Entity, S extends EntityRenderState> void registerEntityModifier(
                    Class<? extends EntityRenderer<? extends E, ? extends S>> baseRenderer, BiConsumer<E, S> modifier) {
                    e.registerEntityModifier(baseRenderer, modifier);
                }
            }));
        bus.addListener(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent.class, e -> AbyssEventBus.post(new RegisterMenuScreensEvent() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type,
                ScreenConstructor<M, U> constructor) {
                e.<M, U>register(type, constructor::create);
            }
        }));
        bus.addListener(net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent.class,
            e -> AbyssEventBus.post(new RegisterParticleProvidersEvent() {
                @Override
                public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, SpriteParticleRegistration<T> registration) {
                    e.registerSpriteSet(type, registration::create);
                }
            }));
    }
}
