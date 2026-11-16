package net.yezon.theabyss.fabric.mixin.client;

import net.minecraft.client.color.block.BlockColors;
import net.yezon.theabyss.fabric.client.FabricClientRegistrations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockColors.class, priority = 1100)
public abstract class BlockColorsTintSourcesMixin {
    @Inject(method = "createDefault", at = @At("RETURN"))
    private static void theabyss$registerTintSources(CallbackInfoReturnable<BlockColors> cir) {
        FabricClientRegistrations.blockTintSources(cir.getReturnValue());
    }
}
