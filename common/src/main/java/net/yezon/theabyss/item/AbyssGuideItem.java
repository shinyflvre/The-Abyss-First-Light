package net.yezon.theabyss.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.util.AbyssTooltip;

public class AbyssGuideItem extends Item {
    public AbyssGuideItem(Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            Client.open();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        AbyssTooltip.ability(tooltip, "tooltip.theabyss.the_abyss_guide.ability", "tooltip.theabyss.the_abyss_guide.text");
        tooltip.accept(AbyssTooltip.rightClick("tooltip.theabyss.the_abyss_guide.action"));
    }

    private static final class Client {
        static void open() {
            net.yezon.theabyss.client.codex.CodexScreen.open();
        }
    }
}
