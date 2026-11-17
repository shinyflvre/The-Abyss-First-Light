package net.yezon.theabyss.events;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.living.LivingDeathEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.item.AbyssTotemEffect;
import net.yezon.theabyss.network.TotemActivationMessage;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class TotemHandlerEvents {
    private TotemHandlerEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            AbyssTotemEffect protection = AbyssTotemEffect.of(stack);
            if (protection == null || !isAbyssTotem(stack)) {
                continue;
            }
            ItemStack used = stack.copy();
            stack.shrink(1);
            event.setCanceled(true);
            player.awardStat(Stats.ITEM_USED.get(used.getItem()));
            CriteriaTriggers.USED_TOTEM.trigger(player, used);
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
            player.setHealth(1.0F);
            protection.apply(player.level(), used, player);
            TotemActivationMessage.send(player, used);
            player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 1.0F, 1.0F);
            return;
        }
    }

    private static boolean isAbyssTotem(ItemStack stack) {
        return stack.is(TheabyssModItems.TOTEM_OF_ABYSS.get()) || stack.is(TheabyssModItems.TOTEM_OF_TIME.get()) || stack.is(TheabyssModItems.TOTEM_OF_THUNDER.get());
    }
}
