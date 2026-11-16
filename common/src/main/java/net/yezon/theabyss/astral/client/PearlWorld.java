package net.yezon.theabyss.astral.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTexture;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.client.event.RegisterColorHandlersEvent;
import net.yezon.theabyss.platform.client.event.TextureAtlasStitchedEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.astral.AstralData;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class PearlWorld {
    private static final int WHITE = 0xFFFFFFFF;

    private static volatile boolean pearl;
    private static boolean applied;
    private static List<TextureAtlasSprite> sprites = List.of();

    private PearlWorld() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegisterTints(RegisterColorHandlersEvent.BlockTintSources event) {
        int wrapped = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            List<BlockTintSource> sources = event.getBlockColors().getTintSources(block.defaultBlockState());
            if (sources.isEmpty() || sources.get(0) instanceof PearlTint) {
                continue;
            }
            List<BlockTintSource> pearlSources = new ArrayList<>(sources.size());
            for (BlockTintSource source : sources) {
                pearlSources.add(source == null ? null : new PearlTint(source));
            }
            event.register(pearlSources, block);
            wrapped++;
        }
        TheabyssMod.LOGGER.debug("Astral pearl world: wrapped tint sources of {} blocks", wrapped);
    }

    private record PearlTint(BlockTintSource original) implements BlockTintSource {
        @Override
        public int color(BlockState state) {
            return pearl ? WHITE : this.original.color(state);
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            return pearl ? WHITE : this.original.colorInWorld(state, level, pos);
        }

        @Override
        public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            return pearl ? WHITE : this.original.colorAsTerrainParticle(state, level, pos);
        }

        @Override
        public java.util.Set<Property<?>> relevantProperties() {
            return this.original.relevantProperties();
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        boolean astral = mc.player != null && mc.level != null && AstralData.isAstral(mc.player);
        if (astral && !applied) {
            apply(mc);
        } else if (!astral && applied) {
            restore(mc);
        }
    }

    @SubscribeEvent
    public static void onAtlasStitched(TextureAtlasStitchedEvent event) {
        if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            sprites = List.of();
            if (applied) {
                applied = false;
            }
        }
    }

    private static void apply(Minecraft mc) {
        TextureAtlas atlas = blockAtlas(mc);
        if (atlas == null) {
            return;
        }
        collectSprites(atlas);
        pearl = true;
        for (TextureAtlasSprite sprite : sprites) {
            write(atlas, sprite, true);
        }
        applied = true;
        mc.levelExtractor.allChanged();
        invalidateGuiItemIcons(mc);
    }

    private static void restore(Minecraft mc) {
        pearl = false;
        applied = false;
        TextureAtlas atlas = blockAtlas(mc);
        if (atlas != null) {
            for (TextureAtlasSprite sprite : sprites) {
                write(atlas, sprite, false);
            }
        }
        if (mc.level != null) {
            mc.levelExtractor.allChanged();
        }
        invalidateGuiItemIcons(mc);
    }

    private static void invalidateGuiItemIcons(Minecraft mc) {
        try {
            if (guiScaleField == null) {
                java.lang.reflect.Field rendererField = net.minecraft.client.renderer.GameRenderer.class.getDeclaredField("guiRenderer");
                rendererField.setAccessible(true);
                guiRendererField = rendererField;
                java.lang.reflect.Field scaleField = net.minecraft.client.gui.render.GuiRenderer.class.getDeclaredField("cachedGuiScale");
                scaleField.setAccessible(true);
                guiScaleField = scaleField;
            }
            guiScaleField.setInt(guiRendererField.get(mc.gameRenderer), -1);
        } catch (ReflectiveOperationException | RuntimeException e) {
            TheabyssMod.LOGGER.warn("[PearlWorld] could not refresh the GUI item icons", e);
        }
    }

    private static java.lang.reflect.Field guiRendererField;
    private static java.lang.reflect.Field guiScaleField;

    private static TextureAtlas blockAtlas(Minecraft mc) {
        AbstractTexture texture = mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        return texture instanceof TextureAtlas atlas ? atlas : null;
    }

    private static void collectSprites(TextureAtlas atlas) {
        List<TextureAtlasSprite> list = new ArrayList<>();
        for (TextureAtlasSprite sprite : ((net.yezon.theabyss.mixin.accessor.client.TextureAtlasAccessor)atlas).theabyss$texturesByName().values()) {
            if (!sprite.isAnimated()) {
                list.add(sprite);
            }
        }
        sprites = list;
    }

    private static void write(TextureAtlas atlas, TextureAtlasSprite sprite, boolean asPearl) {
        GpuTexture texture = atlas.getTexture();
        int atlasWidth = texture.getWidth(0);
        int padding = Math.max(0, Math.round(sprite.getU0() * atlasWidth) - sprite.getX());
        NativeImage[] levels = ((net.yezon.theabyss.mixin.accessor.client.SpriteContentsAccessor)sprite.contents()).theabyss$byMipLevel();
        int width = sprite.contents().width();
        int height = sprite.contents().height();
        int mipLevels = Math.min(levels.length, texture.getMipLevels());
        for (int level = 0; level < mipLevels; level++) {
            NativeImage source = levels[level];
            if (source == null) {
                continue;
            }
            int paddedWidth = width + padding * 2 >> level;
            int paddedHeight = height + padding * 2 >> level;
            int offset = padding >> level;
            if (paddedWidth <= 0 || paddedHeight <= 0) {
                continue;
            }
            try (NativeImage out = new NativeImage(paddedWidth, paddedHeight, false)) {
                fill(out, source, offset, asPearl);
                RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture, out, level, 0, sprite.getX() >> level, sprite.getY() >> level);
            }
        }
    }

    private static void fill(NativeImage out, NativeImage source, int offset, boolean asPearl) {
        int sw = source.getWidth();
        int sh = source.getHeight();
        float mean = 0.0F;
        if (asPearl) {
            int opaque = 0;
            for (int y = 0; y < sh; y++) {
                for (int x = 0; x < sw; x++) {
                    int argb = source.getPixel(x, y);
                    if (ARGB.alpha(argb) > 0) {
                        mean += luminance(argb);
                        opaque++;
                    }
                }
            }
            mean = opaque > 0 ? mean / opaque : 0.5F;
        }
        for (int y = 0; y < out.getHeight(); y++) {
            int sy = Mth.clamp(y - offset, 0, sh - 1);
            for (int x = 0; x < out.getWidth(); x++) {
                int sx = Mth.clamp(x - offset, 0, sw - 1);
                int argb = source.getPixel(sx, sy);
                out.setPixel(x, y, asPearl ? pearl(argb, mean) : argb);
            }
        }
    }

    private static int pearl(int argb, float mean) {
        int alpha = ARGB.alpha(argb);
        if (alpha == 0) {
            return 0;
        }
        float detail = (luminance(argb) - mean) * 0.05F;
        float base = Mth.clamp(0.92F + detail, 0.0F, 1.0F);
        return ARGB.color(alpha, unit(base * 0.975F), unit(base * 0.965F), unit(base * 0.995F));
    }

    private static float luminance(int argb) {
        return (ARGB.red(argb) * 0.299F + ARGB.green(argb) * 0.587F + ARGB.blue(argb) * 0.114F) / 255.0F;
    }

    private static int unit(float value) {
        return Mth.clamp(Math.round(value * 255.0F), 0, 255);
    }
}
