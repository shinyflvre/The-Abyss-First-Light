package net.yezon.theabyss.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.util.AbyssTooltip;

public class RingOfEnderChestItem extends Item {
    private static final Component CONTAINER_TITLE = Component.translatable("container.enderchest");

    public RingOfEnderChestItem(Item.Properties properties) {
        super(properties.durability(250).rarity(Rarity.UNCOMMON));
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState state) {
        return 0.0F;
    }

    @Override
    public void appendHoverText(
        ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
    ) {
        AbyssTooltip.ability(tooltip, "tooltip.theabyss.ring_of_ender_chest.ability", "tooltip.theabyss.ring_of_ender_chest.text");
        tooltip.accept(AbyssTooltip.rightClick("tooltip.theabyss.ring_of_ender_chest.action"));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerEnderChestContainer enderChest = player.getEnderChestInventory();
            player.openMenu(new SimpleMenuProvider((containerId, inventory, p) -> ChestMenu.threeRows(containerId, inventory, enderChest), CONTAINER_TITLE));
            player.awardStat(Stats.OPEN_ENDERCHEST);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_CHEST_OPEN, SoundSource.PLAYERS, 0.5F, 1.0F);
            net.yezon.theabyss.ring.RingVisuals.enderChest(serverPlayer.level(), player);
            net.yezon.theabyss.ring.RingWear.wear(player.getItemInHand(hand), serverPlayer.level(), serverPlayer, 1);
        }
        return InteractionResult.SUCCESS;
    }
}
