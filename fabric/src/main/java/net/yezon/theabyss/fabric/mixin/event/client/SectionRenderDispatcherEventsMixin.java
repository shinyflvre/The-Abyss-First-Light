package net.yezon.theabyss.fabric.mixin.event.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SectionRenderDispatcher.RenderSection.class)
public abstract class SectionRenderDispatcherEventsMixin {
    @WrapOperation(method = "createCompileTask", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/chunk/RenderRegionCache;createRegion(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/SectionPos;)Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;"))
    private RenderChunkRegion theabyss$sectionGeometry(RenderRegionCache cache, Level level, SectionPos section, Operation<RenderChunkRegion> original) {
        ClientFabricEvents.sectionGeometry(level, section.origin());
        return original.call(cache, level, section);
    }
}
