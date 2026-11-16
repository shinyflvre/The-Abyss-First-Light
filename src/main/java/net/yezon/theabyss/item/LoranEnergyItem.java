package net.yezon.theabyss.item;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.yezon.theabyss.events.GetLoranEnergyAdvEvents;
import org.jspecify.annotations.Nullable;

public class LoranEnergyItem extends Item {
    public LoranEnergyItem(Item.Properties properties) {
        super(
            properties.durability(100)
                .rarity(Rarity.RARE)
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
        );
    }

    @Override
    public void appendHoverText(
        ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
    ) {
        tooltip.accept(Component.translatable("tooltip.theabyss.loran_energy.info"));
    }

    @Override
    public void inventoryTick(ItemStack itemstack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(itemstack, world, entity, slot);
        if (!(entity instanceof Player)) {
            return;
        }
        if (slot == EquipmentSlot.MAINHAND) {
            GetLoranEnergyAdvEvents.execute(entity);
        }

        GetLoranEnergyAdvEvents.execute(entity);
    }

    @Override
    public void onCraftedBy(ItemStack itemstack, Player entity) {
        super.onCraftedBy(itemstack, entity);
        GetLoranEnergyAdvEvents.execute(entity);
    }
}
