package net.yezon.theabyss.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.yezon.theabyss.fabric.client.FabricClientRegistrations;
import net.yezon.theabyss.platform.client.CustomSkyboxRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererCustomSkyboxMixin {
    @WrapMethod(method = "render")
    private void theabyss$customSkybox(GpuBufferSlice skyFog, SkyRenderState state, Operation<Void> original) {
        Minecraft minecraft = Minecraft.getInstance();
        CustomSkyboxRenderer custom = FabricClientRegistrations.skybox(minecraft.level);
        if (custom != null) {
            LevelRenderState levelRenderState = minecraft.gameRenderer.gameRenderState().levelRenderState;
            if (custom.renderSky(levelRenderState, state, levelRenderState.cameraRenderState.viewRotationMatrix, skyFog)) {
                return;
            }
        }
        original.call(skyFog, state);
    }
}
