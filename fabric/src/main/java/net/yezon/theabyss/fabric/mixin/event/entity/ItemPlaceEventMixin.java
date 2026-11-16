package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.yezon.theabyss.fabric.event.LevelFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemPlaceEventMixin {
    @WrapOperation(method = "useOn", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/Item;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult theabyss$placeEvent(Item item, UseOnContext context, Operation<InteractionResult> original) {
        return LevelFabricEvents.placeItem((ItemStack)(Object)this, item, context, original);
    }
}
