package net.yezon.theabyss.fabric.mixin.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.fabric.client.FabricRenderStateModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererStateModifiersMixin {
    @Inject(method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;", at = @At("RETURN"))
    private void theabyss$applyModifiers(Entity entity, float partialTicks, CallbackInfoReturnable<EntityRenderState> cir) {
        FabricRenderStateModifiers.apply((EntityRenderer<?, ?>)(Object)this, entity, cir.getReturnValue());
    }
}
