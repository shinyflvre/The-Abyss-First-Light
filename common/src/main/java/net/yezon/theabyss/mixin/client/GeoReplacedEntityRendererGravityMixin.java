package net.yezon.theabyss.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.client.gravity.GravityGeoHead;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

@Mixin(GeoReplacedEntityRenderer.class)
public abstract class GeoReplacedEntityRendererGravityMixin {
    @Shadow
    protected Entity currentEntity;

    @WrapOperation(method = "actuallyRender(Lcom/mojang/blaze3d/vertex/PoseStack;Lsoftware/bernie/geckolib/animatable/GeoAnimatable;Lsoftware/bernie/geckolib/cache/object/BakedGeoModel;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZFIII)V",
        at = @At(value = "INVOKE", target = "Lsoftware/bernie/geckolib/animation/AnimationState;setData(Lsoftware/bernie/geckolib/constant/dataticket/DataTicket;Ljava/lang/Object;)V"))
    private void theabyss$gravityHead(AnimationState<?> state, DataTicket<?> ticket, Object value, Operation<Void> original) {
        original.call(state, ticket, GravityGeoHead.adjust(ticket, value, this.currentEntity));
    }
}
