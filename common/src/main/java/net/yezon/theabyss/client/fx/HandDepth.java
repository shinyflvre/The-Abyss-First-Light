package net.yezon.theabyss.client.fx;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.mixin.accessor.client.GameRendererAccessor;

public final class HandDepth extends AbstractTexture {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "textures/effect/hand_depth.png");
    private static HandDepth instance;

    private HandDepth() {
    }

    public static void register(Minecraft mc) {
        if (instance == null) {
            instance = new HandDepth();
            mc.getTextureManager().register(TEXTURE, instance);
        }
    }

    private static RenderTarget target() {
        return ((GameRendererAccessor)Minecraft.getInstance().gameRenderer).theabyss$hud3DTarget();
    }

    @Override
    public GpuTexture getTexture() {
        GpuTexture depth = target().getDepthTexture();
        return depth != null ? depth : super.getTexture();
    }

    @Override
    public GpuTextureView getTextureView() {
        GpuTextureView depth = target().getDepthTextureView();
        return depth != null ? depth : super.getTextureView();
    }
}
