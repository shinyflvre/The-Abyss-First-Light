package net.yezon.theabyss.item;

import java.util.function.BooleanSupplier;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;

public final class AbyssTotemProtection {
    @FunctionalInterface
    public interface UseCheck {
        boolean allow(LivingEntity entity, DamageSource source, ItemStack stack, InteractionHand hand);
    }

    public static final UseCheck ALWAYS = (entity, source, stack, hand) -> true;

    private AbyssTotemProtection() {
    }

    public static boolean check(LivingEntity entity, DamageSource source, UseCheck useCheck, BooleanSupplier vanilla) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return vanilla.getAsBoolean();
        }
        boolean vanillaTried = false;
        boolean lastRefused = false;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = entity.getItemInHand(hand);
            lastRefused = false;
            if (stack.is(Items.TOTEM_OF_UNDYING)) {
                if (!vanillaTried) {
                    vanillaTried = true;
                    if (vanilla.getAsBoolean()) {
                        return true;
                    }
                }
                continue;
            }
            AbyssTotemEffect protection = AbyssTotemEffect.of(stack);
            if (protection == null) {
                continue;
            }
            if (useCheck.allow(entity, source, stack, hand)) {
                ItemStack used = stack.copy();
                stack.shrink(1);
                activate(entity, used, protection);
                return true;
            }
            lastRefused = true;
        }
        return lastRefused || !vanillaTried && vanilla.getAsBoolean();
    }

    private static void activate(LivingEntity entity, ItemStack used, AbyssTotemEffect protection) {
        if (entity instanceof ServerPlayer player) {
            player.awardStat(Stats.ITEM_USED.get(used.getItem()));
            CriteriaTriggers.USED_TOTEM.trigger(player, used);
            entity.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        }
        entity.setHealth(1.0F);
        protection.apply(entity.level(), used, entity);
        entity.level().broadcastEntityEvent(entity, (byte)35);
    }
}
