package net.yezon.theabyss.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.item.AbyssTotemEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerTotemMixin {
    @WrapOperation(method = "findTotem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private static boolean theabyss$abyssTotem(ItemStack stack, Item item, Operation<Boolean> original) {
        return original.call(stack, item) || AbyssTotemEffect.of(stack) != null;
    }
}
