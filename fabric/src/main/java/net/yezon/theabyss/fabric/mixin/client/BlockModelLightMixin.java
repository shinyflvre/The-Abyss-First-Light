package net.yezon.theabyss.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.Direction;
import net.yezon.theabyss.fabric.client.FabricBlockModelParent;
import net.yezon.theabyss.fabric.client.FabricModelExtras;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockModel.class)
public abstract class BlockModelLightMixin implements FabricBlockModelParent {
    @Shadow
    protected @Nullable BlockModel parent;

    @Override
    public @Nullable BlockModel theabyss$parent() {
        return this.parent;
    }

    @ModifyReturnValue(method = "bakeFace(Lnet/minecraft/client/renderer/block/model/BlockElement;Lnet/minecraft/client/renderer/block/model/BlockElementFace;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/core/Direction;Lnet/minecraft/client/resources/model/ModelState;)Lnet/minecraft/client/renderer/block/model/BakedQuad;",
        at = @At("RETURN"))
    private static BakedQuad theabyss$bakeLight(BakedQuad quad, @Local(argsOnly = true) BlockElement element, @Local(argsOnly = true) Direction facing) {
        return FabricModelExtras.bakeFace(quad, element, facing);
    }
}
