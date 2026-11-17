package net.yezon.theabyss.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.client.gravity.GravityGeoHead;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mixin(GeoEntityRenderer.class)
public abstract class GeoEntityRendererGravityMixin {
    @WrapOperation(method = "actuallyRender(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/Entity;Lsoftware/bernie/geckolib/cache/object/BakedGeoModel;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZFIII)V",
        at = @At(value = "INVOKE", target = "Lsoftware/bernie/geckolib/animation/AnimationState;setData(Lsoftware/bernie/geckolib/constant/dataticket/DataTicket;Ljava/lang/Object;)V"))
    private void theabyss$gravityHead(AnimationState<?> state, DataTicket<?> ticket, Object value, Operation<Void> original,
        @Local(argsOnly = true) Entity entity) {
        original.call(state, ticket, GravityGeoHead.adjust(ticket, value, entity));
    }
}
