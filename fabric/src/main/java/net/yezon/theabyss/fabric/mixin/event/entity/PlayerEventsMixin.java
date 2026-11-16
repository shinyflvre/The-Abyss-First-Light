package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.fabric.event.LivingFabricEvents;
import net.yezon.theabyss.fabric.event.PlayerFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEventsMixin {
    @Unique
    private Player theabyss$self() {
        return (Player)(Object)this;
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void theabyss$death(DamageSource source, CallbackInfo ci) {
        if (LivingFabricEvents.death(this.theabyss$self(), source)) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "actuallyHurt", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float theabyss$damagePre(float damage, @Local(argsOnly = true) DamageSource source) {
        return LivingFabricEvents.damagePre(this.theabyss$self(), source, damage);
    }

    @Inject(method = "interactOn", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;", ordinal = 0),
        cancellable = true)
    private void theabyss$entityInteract(Entity entity, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        if (PlayerFabricEvents.entityInteract(this.theabyss$self(), hand, entity)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void theabyss$attackEntity(Entity entity, CallbackInfo ci) {
        if (PlayerFabricEvents.attackEntity(this.theabyss$self(), entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "stopSleepInBed", at = @At("HEAD"))
    private void theabyss$wakeUp(boolean forcefulWakeUp, boolean updateLevelList, CallbackInfo ci) {
        PlayerFabricEvents.wakeUp(this.theabyss$self());
    }
}
