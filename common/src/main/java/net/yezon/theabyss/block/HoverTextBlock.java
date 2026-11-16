package net.yezon.theabyss.block;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.common.tooltip.TooltipLocation;
import net.yezon.theabyss.platform.event.RegisterTooltipAppendersEvent;
import net.yezon.theabyss.TheabyssMod;

public interface HoverTextBlock {
    void appendHoverText(ItemStack itemstack, Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag);

    @EventBusSubscriber(modid = TheabyssMod.MODID)
    final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        public static void registerTooltipAppenders(RegisterTooltipAppendersEvent event) {
            event.registerAppender(TooltipLocation.POST_CUSTOM, (stack, context, display, player, flag, tooltip) -> {
                if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof HoverTextBlock block) {
                    block.appendHoverText(stack, context, tooltip, flag);
                }
            });
        }
    }
}
