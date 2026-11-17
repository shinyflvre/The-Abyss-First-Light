package net.yezon.theabyss.item;

import net.yezon.theabyss.item.TheabyssModToolMaterials.ToolMaterial;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.InteractionResultHolder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.events.SwordParticleEvents;
import net.yezon.theabyss.events.UseDashEvents;
import net.yezon.theabyss.events.UseSliceEvents;
import org.jspecify.annotations.Nullable;
import net.yezon.theabyss.util.AbyssTooltip;

public class AbyssSwordItem extends SwordItem {
    private static final ToolMaterial MATERIAL = TheabyssModToolMaterials.ABYSS;

    public AbyssSwordItem(Item.Properties properties) {
        super(MATERIAL, TheabyssModToolMaterials.tool(properties, MATERIAL, 19.0F, -2.0F));
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        AbyssTooltip.ability(tooltip, "tooltip.theabyss.abyss_sword.ability", "tooltip.theabyss.abyss_sword.text");
        tooltip.accept(AbyssTooltip.leftClick("tooltip.theabyss.abyss_sword.left"));
        tooltip.accept(AbyssTooltip.rightClick("tooltip.theabyss.abyss_sword.right"));
        tooltip.accept(AbyssTooltip.middleClick("tooltip.theabyss.abyss_sword.middle"));
        tooltip.accept(AbyssTooltip.somnium(40));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(world, entity, hand);
        UseDashEvents.execute(entity);
        return result;
    }

    @Override
    public boolean hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
        boolean retval = super.hurtEnemy(itemstack, entity, sourceentity);
        UseSliceEvents.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
        return retval;
    }

    @Override
    public void inventoryTick(ItemStack itemstack, Level level, Entity entity, int slotId, boolean selected) {
        super.inventoryTick(itemstack, level, entity, slotId, selected);
        if (!(level instanceof ServerLevel world)) {
            return;
        }
        EquipmentSlot slot = InventoryTickSlot.of(entity, itemstack);
        if (entity instanceof Player && slot == EquipmentSlot.MAINHAND) {
            SwordParticleEvents.execute(world, entity.getX(), entity.getY(), entity.getZ());
        }
    }
}
