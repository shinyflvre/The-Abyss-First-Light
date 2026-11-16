package net.yezon.theabyss.fabric.mixin.fluids;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.item.ItemEntity;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public abstract class ItemEntityFluidMixin {
    @ModifyExpressionValue(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;isInWater()Z", ordinal = 0)
    )
    private boolean theabyss$floatInOwnFluids(boolean original) {
        return original || FluidContact.highestIfOwn((ItemEntity)(Object)this) > 0.1F;
    }

    @ModifyExpressionValue(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;getFluidHeight(Lnet/minecraft/tags/TagKey;)D", ordinal = 0)
    )
    private double theabyss$ownFluidFloatHeight(double original) {
        double own = FluidContact.highestIfOwn((ItemEntity)(Object)this);
        return own > 0.1F ? own : original;
    }
}
