package net.yezon.theabyss.fabric.client;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.yezon.theabyss.fabric.mixin.client.BlockColorsAccessor;
import net.yezon.theabyss.platform.client.event.RegisterColorHandlersEvent;
import net.yezon.theabyss.platform.client.event.RegisterDimensionSpecialEffectsEvent;
import net.yezon.theabyss.platform.client.event.RegisterItemPropertiesEvent;
import net.yezon.theabyss.platform.client.event.RegisterItemRenderersEvent;
import net.yezon.theabyss.platform.client.event.RegisterParticleProvidersEvent;
import net.yezon.theabyss.platform.client.event.RegisterShadersEvent;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import org.jspecify.annotations.Nullable;

public final class FabricClientRegistrations {
    private FabricClientRegistrations() {
    }

    public static void register() {
        AbyssEventBus.post(new RegisterParticleProvidersEvent() {
            @Override
            public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, SpriteParticleRegistration<T> registration) {
                ParticleFactoryRegistry.getInstance().register(type, registration::create);
            }

            @Override
            public <T extends ParticleOptions> void registerSpecial(ParticleType<T> type, ParticleProvider<T> provider) {
                ParticleFactoryRegistry.getInstance().register(type, provider);
            }
        });
        AbyssEventBus.post(new RegisterDimensionSpecialEffectsEvent() {
            @Override
            public void register(ResourceLocation effectsId, DimensionSpecialEffects effects) {
                DimensionRenderingRegistry.registerDimensionEffects(effectsId, effects);
            }
        });
        AbyssEventBus.post(new RegisterItemPropertiesEvent() {
            @Override
            public void register(Item item, ResourceLocation property, ClampedItemPropertyFunction function) {
                FabricModelPredicateProviderRegistry.register(item, property, function);
            }

            @Override
            public void registerGeneric(ResourceLocation property, ClampedItemPropertyFunction function) {
                FabricModelPredicateProviderRegistry.register(property, function);
            }
        });
        AbyssEventBus.post(new RegisterItemRenderersEvent() {
            @Override
            public void register(ItemRenderer renderer, ItemLike... items) {
                for (ItemLike item : items) {
                    BuiltinItemRendererRegistry.INSTANCE.register(item, renderer::render);
                }
            }
        });
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            if (!AbyssEventBus.hasListeners(RegisterShadersEvent.class)) {
                return;
            }
            AbyssEventBus.post(new RegisterShadersEvent() {
                @Override
                public void registerShader(ResourceLocation id, VertexFormat format, Consumer<ShaderInstance> onLoad) {
                    try {
                        context.register(id, format, onLoad);
                    } catch (IOException exception) {
                        throw new UncheckedIOException("Failed to load shader " + id, exception);
                    }
                }
            });
        });
    }

    public static void blockColors(BlockColors colors) {
        if (!AbyssEventBus.hasListeners(RegisterColorHandlersEvent.Block.class)) {
            return;
        }
        AbyssEventBus.post(new RegisterColorHandlersEvent.Block() {
            @Override
            public BlockColors getBlockColors() {
                return colors;
            }

            @Override
            public void register(BlockColor color, net.minecraft.world.level.block.Block... blocks) {
                colors.register(color, blocks);
            }

            @Override
            public @Nullable BlockColor getColor(net.minecraft.world.level.block.Block block) {
                return ((BlockColorsAccessor)colors).theabyss$blockColors().byId(BuiltInRegistries.BLOCK.getId(block));
            }
        });
    }

    public static void itemColors(ItemColors colors, BlockColors blockColors) {
        if (!AbyssEventBus.hasListeners(RegisterColorHandlersEvent.Item.class)) {
            return;
        }
        AbyssEventBus.post(new RegisterColorHandlersEvent.Item() {
            @Override
            public ItemColors getItemColors() {
                return colors;
            }

            @Override
            public BlockColors getBlockColors() {
                return blockColors;
            }

            @Override
            public void register(ItemColor color, ItemLike... items) {
                colors.register(color, items);
            }
        });
    }
}
