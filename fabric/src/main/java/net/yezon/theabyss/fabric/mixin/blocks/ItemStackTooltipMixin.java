package net.yezon.theabyss.fabric.mixin.blocks;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.yezon.theabyss.fabric.blocks.FabricTooltipAppenders;
import net.yezon.theabyss.platform.common.tooltip.TooltipLocation;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackTooltipMixin {
    @Inject(method = "addDetailsToTooltip", at = @At("HEAD"))
    private void theabyss$headAppenders(Item.TooltipContext context, TooltipDisplay display, @Nullable Player player, TooltipFlag flag, Consumer<Component> builder,
                                        CallbackInfo ci) {
        FabricTooltipAppenders.append(TooltipLocation.HEAD, (ItemStack)(Object)this, context, display, player, flag, builder);
    }

    @Inject(method = "addDetailsToTooltip", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/Item;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/item/component/TooltipDisplay;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V",
        shift = At.Shift.AFTER))
    private void theabyss$postCustomAppenders(Item.TooltipContext context, TooltipDisplay display, @Nullable Player player, TooltipFlag flag,
                                              Consumer<Component> builder, CallbackInfo ci) {
        FabricTooltipAppenders.append(TooltipLocation.POST_CUSTOM, (ItemStack)(Object)this, context, display, player, flag, builder);
    }

    @Inject(method = "addDetailsToTooltip", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;addAttributeTooltips(Ljava/util/function/Consumer;Lnet/minecraft/world/item/component/TooltipDisplay;Lnet/minecraft/world/entity/player/Player;)V"))
    private void theabyss$preItemInfoAppenders(Item.TooltipContext context, TooltipDisplay display, @Nullable Player player, TooltipFlag flag,
                                               Consumer<Component> builder, CallbackInfo ci) {
        FabricTooltipAppenders.append(TooltipLocation.PRE_ITEM_INFO, (ItemStack)(Object)this, context, display, player, flag, builder);
    }

    @Inject(method = "addDetailsToTooltip", at = @At("TAIL"))
    private void theabyss$tailAppenders(Item.TooltipContext context, TooltipDisplay display, @Nullable Player player, TooltipFlag flag, Consumer<Component> builder,
                                        CallbackInfo ci) {
        FabricTooltipAppenders.append(TooltipLocation.TAIL, (ItemStack)(Object)this, context, display, player, flag, builder);
    }
}
