package net.yezon.theabyss.fabric.mixin.event.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.SectionPos;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorEventsMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private ClientLevel level;
    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Inject(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/extract/LevelExtractor;extractGizmos()V", shift = At.Shift.AFTER))
    private void theabyss$extractLevel(DeltaTracker deltaTracker, Camera camera, float worldPartialTicks, CallbackInfo ci) {
        ClientFabricEvents.extractLevel(camera, deltaTracker, camera.getCullFrustum(), this.level,
            this.minecraft.gameRenderer.gameRenderState().optionsRenderState, this.levelRenderState);
    }

    @WrapOperation(method = "extract", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/chunk/RenderRegionCache;createRegion(Lnet/minecraft/client/multiplayer/ClientLevel;J)Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;"))
    private RenderSectionRegion theabyss$sectionGeometry(RenderRegionCache cache, ClientLevel level, long sectionNode, Operation<RenderSectionRegion> original) {
        ClientFabricEvents.sectionGeometry(this.level, SectionPos.of(sectionNode).origin());
        return original.call(cache, level, sectionNode);
    }
}
