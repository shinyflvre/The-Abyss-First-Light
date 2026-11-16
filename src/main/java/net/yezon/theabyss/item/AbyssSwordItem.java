package net.yezon.theabyss.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
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

public class AbyssSwordItem extends Item {
    public AbyssSwordItem(Item.Properties properties) {
        super(properties.sword(TheabyssModToolMaterials.ABYSS, 19.0F, -2.0F));
    }

    @Override
    public void appendHoverText(
        ItemStack itemstack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
    ) {
        AbyssTooltip.ability(tooltip, "tooltip.theabyss.abyss_sword.ability", "tooltip.theabyss.abyss_sword.text");
        tooltip.accept(AbyssTooltip.leftClick("tooltip.theabyss.abyss_sword.left"));
        tooltip.accept(AbyssTooltip.rightClick("tooltip.theabyss.abyss_sword.right"));
        tooltip.accept(AbyssTooltip.middleClick("tooltip.theabyss.abyss_sword.middle"));
        tooltip.accept(AbyssTooltip.somnium(40));
    }

    @Override
    public InteractionResult use(Level world, Player entity, InteractionHand hand) {
        InteractionResult result = super.use(world, entity, hand);
        UseDashEvents.execute(entity);
        return result;
    }

    @Override
    public void hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
        super.hurtEnemy(itemstack, entity, sourceentity);
        UseSliceEvents.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
    }

    @Override
    public void inventoryTick(ItemStack itemstack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(itemstack, world, entity, slot);
        if (entity instanceof Player && slot == EquipmentSlot.MAINHAND) {
            SwordParticleEvents.execute(world, entity.getX(), entity.getY(), entity.getZ());
        }
    }
}
