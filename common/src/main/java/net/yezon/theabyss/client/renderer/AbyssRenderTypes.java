package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.platform.client.event.RegisterShadersEvent;
import org.jspecify.annotations.Nullable;

public final class AbyssRenderTypes extends RenderType {
    private static @Nullable ShaderInstance glowEyesShader;
    private static @Nullable ShaderInstance lightTrailShader;
    private static final RenderStateShard.ShaderStateShard GLOW_EYES_SHADER = new RenderStateShard.ShaderStateShard(() -> glowEyesShader);
    private static final RenderStateShard.ShaderStateShard LIGHT_TRAIL_SHADER = new RenderStateShard.ShaderStateShard(() -> lightTrailShader);
    private static final Function<ResourceLocation, RenderType> EYES = Util.memoize(texture -> build("theabyss_eyes", true, List.of(
        new RenderStateShard.TextureStateShard(texture, false, false), GLOW_EYES_SHADER, TRANSLUCENT_TRANSPARENCY, LEQUAL_DEPTH_TEST, CULL,
        NO_LIGHTMAP, NO_OVERLAY, NO_LAYERING, MAIN_TARGET, DEFAULT_TEXTURING, COLOR_WRITE, DEFAULT_LINE, NO_COLOR_LOGIC)));
    private static final Function<ResourceLocation, RenderType> LIGHT_TRAIL = Util.memoize(texture -> build("theabyss_light_trail", true, List.of(
        new RenderStateShard.TextureStateShard(texture, false, false), LIGHT_TRAIL_SHADER, ADDITIVE_TRANSPARENCY, LEQUAL_DEPTH_TEST, NO_CULL,
        LIGHTMAP, OVERLAY, NO_LAYERING, MAIN_TARGET, DEFAULT_TEXTURING, COLOR_WRITE, DEFAULT_LINE, NO_COLOR_LOGIC)));

    private AbyssRenderTypes(String name, boolean sortOnUpload, Runnable setup, Runnable clear) {
        super(name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, false, sortOnUpload, setup, clear);
    }

    private static RenderType build(String name, boolean sortOnUpload, List<RenderStateShard> shards) {
        return new AbyssRenderTypes(name, sortOnUpload, () -> shards.forEach(RenderStateShard::setupRenderState),
            () -> shards.forEach(RenderStateShard::clearRenderState));
    }

    static void registerShaders(RegisterShadersEvent event) {
        event.registerShader(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "glow_eyes"), DefaultVertexFormat.NEW_ENTITY,
            shader -> glowEyesShader = shader);
        event.registerShader(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "light_trail"), DefaultVertexFormat.NEW_ENTITY,
            shader -> lightTrailShader = shader);
    }

    public static RenderType eyes(ResourceLocation texture) {
        return EYES.apply(texture);
    }

    public static RenderType lightTrail(ResourceLocation texture) {
        return LIGHT_TRAIL.apply(texture);
    }
}
