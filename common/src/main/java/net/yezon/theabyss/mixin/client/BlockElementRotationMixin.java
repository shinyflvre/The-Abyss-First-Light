package net.yezon.theabyss.mixin.client;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.yezon.theabyss.client.model.ElementRotations;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.client.renderer.block.model.BlockElement$Deserializer")
public abstract class BlockElementRotationMixin {
    @WrapMethod(method = "getRotation(Lcom/google/gson/JsonObject;)Lnet/minecraft/client/renderer/block/model/BlockElementRotation;")
    private BlockElementRotation theabyss$freeRotation(JsonObject element, Operation<BlockElementRotation> original) {
        BlockElementRotation free = ElementRotations.parse(element);
        return free != null ? free : original.call(element);
    }
}
