package net.yezon.theabyss.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.client.fx.TrueInvisibilityClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class TrueInvisibilityRevealMixin {
    @ModifyReturnValue(method = "isBodyVisible", at = @At("RETURN"))
    private boolean theabyss$revealNearby(boolean visible, LivingEntity entity) {
        return visible || TrueInvisibilityClient.revealed(entity);
    }
}
