package net.yezon.theabyss.platform.common.tooltip;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface TooltipAppender {
    void append(ItemStack stack, Item.TooltipContext context, @Nullable Player player, TooltipFlag tooltipFlag, List<Component> tooltip);
}
