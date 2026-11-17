package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.OptionalInt;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.yezon.theabyss.fabric.event.LivingFabricEvents;
import net.yezon.theabyss.fabric.event.PlayerFabricEvents;
import org.objectweb.asm.Opcodes;
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

    @Inject(method = "changeDimension(Lnet/minecraft/world/level/portal/DimensionTransition;)Lnet/minecraft/world/entity/Entity;", at = @At(value = "FIELD",
        target = "Lnet/minecraft/server/level/ServerPlayer;lastSentFood:I", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void theabyss$changedDimension(DimensionTransition transition, CallbackInfoReturnable<Entity> cir, @Local ResourceKey<Level> lastDimension) {
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

    @Inject(method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V", at = @At("TAIL"))
    private void theabyss$clone(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo ci) {
        PlayerFabricEvents.clone(this.theabyss$self(), oldPlayer);
    }
}
