package net.yezon.theabyss.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.yezon.theabyss.client.model.ElementRotations;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FaceBakery.class)
public abstract class FaceBakeryRotationMixin {
    @WrapMethod(method = "applyElementRotation(Lorg/joml/Vector3f;Lnet/minecraft/client/renderer/block/model/BlockElementRotation;)V")
    private void theabyss$freeRotation(Vector3f vertex, @Nullable BlockElementRotation rotation, Operation<Void> original) {
        if (!ElementRotations.apply(vertex, rotation)) {
            original.call(vertex, rotation);
        }
    }
}
