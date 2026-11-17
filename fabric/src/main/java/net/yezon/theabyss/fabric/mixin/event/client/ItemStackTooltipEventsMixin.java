package net.yezon.theabyss.fabric.mixin.event.client;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackTooltipEventsMixin {
    @Inject(method = "getTooltipLines", at = @At(value = "RETURN", ordinal = 1))
    private void theabyss$itemTooltip(Item.TooltipContext context, Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir) {
        ClientFabricEvents.itemTooltip(player, flag, (ItemStack)(Object)this, cir.getReturnValue());
    }
}
