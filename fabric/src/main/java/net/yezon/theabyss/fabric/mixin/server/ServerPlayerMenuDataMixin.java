package net.yezon.theabyss.fabric.mixin.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.yezon.theabyss.fabric.server.FabricServerHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMenuDataMixin {
    @WrapOperation(method = "openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/MenuProvider;createMenu(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/inventory/AbstractContainerMenu;"))
    private AbstractContainerMenu theabyss$plainOpenOfDataMenu(MenuProvider provider, int containerId, Inventory inventory, Player player,
        Operation<AbstractContainerMenu> original, @Local(argsOnly = true) LocalRef<MenuProvider> opened) {
        AbstractContainerMenu menu = original.call(provider, containerId, inventory, player);
        if (menu != null && FabricServerHooks.isExtendedType(menu.getType()) && !(provider instanceof ExtendedScreenHandlerFactory<?>)) {
            opened.set(FabricServerHooks.withoutExtraData(provider));
        }
        return menu;
    }
}
