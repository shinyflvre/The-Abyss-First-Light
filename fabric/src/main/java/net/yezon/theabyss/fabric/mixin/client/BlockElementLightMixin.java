package net.yezon.theabyss.fabric.mixin.client;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.yezon.theabyss.fabric.client.FabricModelExtras;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.renderer.block.model.BlockElement$Deserializer")
public abstract class BlockElementLightMixin {
    @ModifyReturnValue(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockElement;",
        at = @At("RETURN"))
    private BlockElement theabyss$readLight(BlockElement element, @Local(argsOnly = true) JsonElement json) {
        FabricModelExtras.readElement(element, json);
        return element;
    }
}
