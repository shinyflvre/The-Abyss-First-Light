package net.yezon.theabyss.mixin.client;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.client.gravity.GravityClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
        at = @At("RETURN"))
    private static void theabyss$gravityUpright(LivingEntity entity, CallbackInfoReturnable<EntityRenderState> cir) {
        EntityRenderState state = cir.getReturnValue();
        if (state != null && net.yezon.theabyss.platform.client.renderstate.RenderData.get(state, GravityClient.POSE) != null) {
            net.yezon.theabyss.platform.client.renderstate.RenderData.set(state, GravityClient.POSE, null);
        }
    }
}
