package net.yezon.theabyss.fabric.mixin.client;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.yezon.theabyss.fabric.client.FabricClientRegistrations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemColors.class, priority = 1100)
public abstract class ItemColorsRegisterMixin {
    @Inject(method = "createDefault", at = @At("RETURN"))
    private static void theabyss$registerItemColors(BlockColors blockColors, CallbackInfoReturnable<ItemColors> cir) {
        FabricClientRegistrations.itemColors(cir.getReturnValue(), blockColors);
    }
}
