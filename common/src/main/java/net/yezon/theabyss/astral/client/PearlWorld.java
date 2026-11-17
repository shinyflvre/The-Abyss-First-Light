package net.yezon.theabyss.astral.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.yezon.theabyss.compat.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.client.event.RegisterColorHandlersEvent;
import net.yezon.theabyss.platform.client.event.TextureAtlasStitchedEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.astral.AstralData;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class PearlWorld {
    private static final int WHITE = 0xFFFFFFFF;

    private static volatile boolean pearl;
    private static boolean applied;
    private static List<TextureAtlasSprite> sprites = List.of();

    private PearlWorld() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegisterTints(RegisterColorHandlersEvent.Block event) {
        int wrapped = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            BlockColor original = event.getColor(block);
            if (original == null || original instanceof PearlTint) {
                continue;
            }
            event.register(new PearlTint(original), block);
            wrapped++;
        }
        TheabyssMod.LOGGER.debug("Astral pearl world: wrapped tint sources of {} blocks", wrapped);
    }

    private record PearlTint(BlockColor original) implements BlockColor {
        @Override
        public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
            return pearl ? WHITE : this.original.getColor(state, level, pos, tintIndex);
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
        mc.levelRenderer.allChanged();
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
            mc.levelRenderer.allChanged();
        }
    }

    private static TextureAtlas blockAtlas(Minecraft mc) {
        AbstractTexture texture = mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        return texture instanceof TextureAtlas atlas ? atlas : null;
    }

    private static void collectSprites(TextureAtlas atlas) {
        List<TextureAtlasSprite> list = new ArrayList<>();
        for (TextureAtlasSprite sprite : ((net.yezon.theabyss.mixin.accessor.client.TextureAtlasAccessor)atlas).theabyss$texturesByName().values()) {
            if (sprite.contents().getUniqueFrames().count() <= 1) {
                list.add(sprite);
            }
        }
        sprites = list;
    }

    private static void write(TextureAtlas atlas, TextureAtlasSprite sprite, boolean asPearl) {
        NativeImage[] levels = ((net.yezon.theabyss.mixin.accessor.client.SpriteContentsAccessor)sprite.contents()).theabyss$byMipLevel();
        int width = sprite.contents().width();
        int height = sprite.contents().height();
        GlStateManager._bindTexture(atlas.getId());
        for (int level = 0; level < levels.length; level++) {
            NativeImage source = levels[level];
            if (source == null) {
                continue;
            }
            int levelWidth = width >> level;
            int levelHeight = height >> level;
            if (levelWidth <= 0 || levelHeight <= 0) {
                continue;
            }
            try (NativeImage out = new NativeImage(levelWidth, levelHeight, false)) {
                fill(out, source, 0, asPearl);
                out.upload(level, sprite.getX() >> level, sprite.getY() >> level, 0, 0, levelWidth, levelHeight, levels.length > 1, false);
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
                    int argb = argbOf(source.getPixelRGBA(x, y));
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
                int argb = argbOf(source.getPixelRGBA(sx, sy));
                out.setPixelRGBA(x, y, FastColor.ABGR32.fromArgb32(asPearl ? pearl(argb, mean) : argb));
            }
        }
    }

    private static int argbOf(int abgr) {
        return ARGB.color(FastColor.ABGR32.alpha(abgr), FastColor.ABGR32.red(abgr), FastColor.ABGR32.green(abgr), FastColor.ABGR32.blue(abgr));
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
