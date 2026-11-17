package net.yezon.theabyss.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.yezon.theabyss.item.AbyssDrinkItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDrinkEventMixin {
    @Inject(
        method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;",
        at = @At("HEAD")
    )
    private void theabyss$rememberDrink(Level level, ItemStack food, FoodProperties properties, CallbackInfoReturnable<ItemStack> cir,
                                        @Share("theabyssDrink") LocalBooleanRef drink) {
        drink.set(food.getItem() instanceof AbyssDrinkItem);
    }

    @WrapOperation(
        method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;gameEvent(Lnet/minecraft/core/Holder;)V")
    )
    private void theabyss$drinkGameEvent(LivingEntity entity, Holder<GameEvent> event, Operation<Void> original,
                                         @Share("theabyssDrink") LocalBooleanRef drink) {
        original.call(entity, drink.get() && event == GameEvent.EAT ? GameEvent.DRINK : event);
    }
}
