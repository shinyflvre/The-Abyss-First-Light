package net.yezon.theabyss.fabric.mixin.client;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
    @Accessor("ticks")
    int theabyss$ticks();

    @Accessor("cullingFrustum")
    Frustum theabyss$cullingFrustum();

    @Accessor("capturedFrustum")
    Frustum theabyss$capturedFrustum();
}
