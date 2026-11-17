package net.yezon.theabyss.client.fx.spell;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.platform.client.event.RegisterShadersEvent;
import org.jspecify.annotations.Nullable;

final class SpellRenderTypes extends RenderType {
    private static @Nullable ShaderInstance unlitParticle;
    private static final RenderStateShard.ShaderStateShard UNLIT_PARTICLE_SHADER = new RenderStateShard.ShaderStateShard(() -> unlitParticle);
    private static final RenderStateShard.TransparencyStateShard PREMULTIPLIED_GLOW = new RenderStateShard.TransparencyStateShard("theabyss_premultiplied_glow",
        () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        },
        () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        });

    private SpellRenderTypes(String name, Runnable setup, Runnable clear) {
        super(name, DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 1536, false, false, setup, clear);
    }

    static void registerShaders(RegisterShadersEvent event) {
        event.registerShader(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "unlit_particle"), DefaultVertexFormat.POSITION_TEX_COLOR,
            shader -> unlitParticle = shader);
    }

    static RenderType unlit(String name, ResourceLocation texture, float tint) {
        float linearTint = (float)Math.pow(tint, 2.2);
        RenderStateShard tintShard = new RenderStateShard("theabyss_unlit_tint", () -> {
            ShaderInstance shader = unlitParticle;
            if (shader != null) {
                shader.safeGetUniform("Tint").set(linearTint);
            }
        }, () -> {
        }) {
        };
        List<RenderStateShard> shards = List.of(new RenderStateShard.TextureStateShard(texture, false, false), UNLIT_PARTICLE_SHADER, tintShard,
            PREMULTIPLIED_GLOW, LEQUAL_DEPTH_TEST, NO_CULL, NO_LIGHTMAP, NO_OVERLAY, NO_LAYERING, MAIN_TARGET, DEFAULT_TEXTURING, COLOR_WRITE,
            DEFAULT_LINE, NO_COLOR_LOGIC);
        return new SpellRenderTypes(name, () -> shards.forEach(RenderStateShard::setupRenderState),
            () -> shards.forEach(RenderStateShard::clearRenderState));
    }
}
