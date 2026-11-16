package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.OptionalInt;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.yezon.theabyss.fabric.event.LivingFabricEvents;
import net.yezon.theabyss.fabric.event.PlayerFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEventsMixin {
    @Unique
    private ServerPlayer theabyss$self() {
        return (ServerPlayer)(Object)this;
    }

    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;gameEvent(Lnet/minecraft/core/Holder;)V", ordinal = 0,
        shift = At.Shift.AFTER), cancellable = true)
    private void theabyss$death(DamageSource source, CallbackInfo ci) {
        if (LivingFabricEvents.death(this.theabyss$self(), source)) {
            ci.cancel();
        }
    }

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerPlayer;teleportSpectators(Lnet/minecraft/world/level/portal/TeleportTransition;Lnet/minecraft/server/level/ServerLevel;)V",
        shift = At.Shift.AFTER))
    private void theabyss$changedDimension(TeleportTransition transition, CallbackInfoReturnable<ServerPlayer> cir,
        @Local(name = "lastDimension") ResourceKey<Level> lastDimension) {
        PlayerFabricEvents.changedDimension(this.theabyss$self(), lastDimension, transition.newLevel().dimension());
    }

    @Inject(method = "openMenu", at = @At(value = "INVOKE", target = "Ljava/util/OptionalInt;of(I)Ljava/util/OptionalInt;"))
    private void theabyss$menuOpen(MenuProvider provider, CallbackInfoReturnable<OptionalInt> cir) {
        PlayerFabricEvents.containerOpen(this.theabyss$self());
    }

    @Inject(method = "openHorseInventory", at = @At("TAIL"))
    private void theabyss$horseMenuOpen(AbstractHorse horse, Container container, CallbackInfo ci) {
        PlayerFabricEvents.containerOpen(this.theabyss$self());
    }

    @Inject(method = "openNautilusInventory", at = @At("TAIL"))
    private void theabyss$nautilusMenuOpen(AbstractNautilus nautilus, Container container, CallbackInfo ci) {
        PlayerFabricEvents.containerOpen(this.theabyss$self());
    }

    @Inject(method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V", at = @At("TAIL"))
    private void theabyss$clone(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo ci) {
        PlayerFabricEvents.clone(this.theabyss$self(), oldPlayer);
    }
}
