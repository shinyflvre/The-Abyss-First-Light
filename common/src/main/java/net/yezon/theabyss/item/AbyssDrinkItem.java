package net.yezon.theabyss.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class AbyssDrinkItem extends Item {
    private final int consumeTicks;

    public AbyssDrinkItem(Item.Properties properties, float consumeSeconds) {
        super(properties);
        this.consumeTicks = (int)(consumeSeconds * 20.0F);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemstack) {
        return UseAnim.DRINK;
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public int getUseDuration(ItemStack itemstack, LivingEntity entity) {
        return itemstack.has(DataComponents.FOOD) ? super.getUseDuration(itemstack, entity) : this.consumeTicks;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.has(DataComponents.FOOD)) {
            return super.use(level, player, hand);
        }
        if (this.consumeTicks > 0) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }
        return InteractionResultHolder.consume(this.consume(level, player, itemstack));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, Level level, LivingEntity entity) {
        if (itemstack.has(DataComponents.FOOD)) {
            return super.finishUsingItem(itemstack, level, entity);
        }
        return this.consume(level, entity, itemstack);
    }

    private ItemStack consume(Level level, LivingEntity entity, ItemStack itemstack) {
        if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.awardStat(Stats.ITEM_USED.get(itemstack.getItem()));
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, itemstack);
        }
        entity.gameEvent(GameEvent.DRINK);
        itemstack.consume(1, entity);
        return itemstack;
    }
}
