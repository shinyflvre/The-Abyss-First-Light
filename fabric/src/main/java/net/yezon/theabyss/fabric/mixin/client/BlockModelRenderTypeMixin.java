package net.yezon.theabyss.fabric.mixin.client;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.yezon.theabyss.fabric.client.FabricModelExtras;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockModel.Deserializer.class)
public abstract class BlockModelRenderTypeMixin {
    @ModifyReturnValue(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockModel;",
        at = @At("RETURN"))
    private BlockModel theabyss$readRenderType(BlockModel model, @Local(argsOnly = true) JsonElement json) {
        FabricModelExtras.readModel(model, json);
        return model;
    }
}
