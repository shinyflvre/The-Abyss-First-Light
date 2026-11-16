package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.fabric.event.FabricDamageFrame;
import net.yezon.theabyss.fabric.event.FabricDamageState;
import net.yezon.theabyss.fabric.event.LivingFabricEvents;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.event.entity.living.MobEffectEvent;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityEventsMixin implements FabricDamageState {
    @Shadow
    @Final
    private Map<Holder<MobEffect>, MobEffectInstance> activeEffects;

    @Unique
    private final ArrayDeque<FabricDamageFrame> theabyss$frames = new ArrayDeque<>();

    @Unique
    private boolean theabyss$keptEffect;

    @Shadow
    protected abstract void onEffectsRemoved(Collection<MobEffectInstance> effects);

    @Shadow
    public abstract @Nullable Player getLastHurtByPlayer();

    @Override
    public ArrayDeque<FabricDamageFrame> theabyss$damageFrames() {
        return this.theabyss$frames;
    }

    @Unique
    private LivingEntity theabyss$self() {
        return (LivingEntity)(Object)this;
    }

    @WrapOperation(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;canBeAffected(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private boolean theabyss$effectApplicable(LivingEntity entity, MobEffectInstance effect, Operation<Boolean> original, @Local(argsOnly = true) @Nullable Entity source) {
        return LivingFabricEvents.effectApplicable(entity, effect, source, () -> original.call(entity, effect));
    }

    @WrapOperation(method = "forceAddEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;canBeAffected(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private boolean theabyss$forcedEffectApplicable(LivingEntity entity, MobEffectInstance effect, Operation<Boolean> original,
        @Local(argsOnly = true) @Nullable Entity source) {
        return LivingFabricEvents.effectApplicable(entity, effect, source, () -> original.call(entity, effect));
    }

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.AFTER))
    private void theabyss$effectAdded(MobEffectInstance effect, @Nullable Entity source, CallbackInfoReturnable<Boolean> cir) {
        LivingFabricEvents.effectAdded(this.theabyss$self());
    }

    @Inject(method = "removeEffect", at = @At("HEAD"), cancellable = true)
    private void theabyss$effectRemove(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if (LivingFabricEvents.effectRemove(this.theabyss$self())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "removeAllEffects", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap(Ljava/util/Map;)Ljava/util/HashMap;"),
        cancellable = true)
    private void theabyss$effectRemoveAll(CallbackInfoReturnable<Boolean> cir) {
        if (!AbyssEventBus.hasListeners(MobEffectEvent.Remove.class)) {
            return;
        }
        Map<Holder<MobEffect>, MobEffectInstance> removed = new HashMap<>(this.activeEffects.size());
        for (Map.Entry<Holder<MobEffect>, MobEffectInstance> entry : this.activeEffects.entrySet()) {
            if (!LivingFabricEvents.effectRemove(this.theabyss$self())) {
                removed.put(entry.getKey(), entry.getValue());
            }
        }
        removed.keySet().forEach(this.activeEffects::remove);
        this.onEffectsRemoved(removed.values());
        cir.setReturnValue(true);
    }

    @WrapOperation(method = "tickEffects", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V"))
    private void theabyss$effectExpired(Iterator<?> iterator, Operation<Void> original) {
        if (LivingFabricEvents.effectExpired(this.theabyss$self())) {
            this.theabyss$keptEffect = true;
            return;
        }
        this.theabyss$keptEffect = false;
        original.call(iterator);
    }

    @WrapOperation(method = "tickEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;onEffectsRemoved(Ljava/util/Collection;)V"))
    private void theabyss$effectExpiredRemoved(LivingEntity entity, Collection<MobEffectInstance> effects, Operation<Void> original) {
        if (this.theabyss$keptEffect) {
            this.theabyss$keptEffect = false;
            return;
        }
        original.call(entity, effects);
    }

    @WrapMethod(method = "hurtServer")
    private boolean theabyss$damageFrame(ServerLevel level, DamageSource source, float damage, Operation<Boolean> original) {
        LivingEntity self = this.theabyss$self();
        FabricDamageFrame frame = LivingFabricEvents.pushDamage(self, damage);
        try {
            boolean result = original.call(level, source, damage);
            LivingFabricEvents.damagePost(self, source, frame);
            return result;
        } finally {
            LivingFabricEvents.popDamage(self, frame);
        }
    }

    @Inject(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z", ordinal = 0), cancellable = true)
    private void theabyss$incomingDamage(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir,
        @Local(argsOnly = true) LocalFloatRef amount) {
        Float result = LivingFabricEvents.incomingDamage(this.theabyss$self(), source, amount.get());
        if (result == null) {
            cir.setReturnValue(false);
            return;
        }
        amount.set(result);
    }

    @ModifyExpressionValue(method = "hurtServer", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/LivingEntity;applyItemBlocking(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float theabyss$damageBlocked(float blocked) {
        LivingFabricEvents.damageBlocked(this.theabyss$self(), blocked);
        return blocked;
    }

    @Inject(method = "hurtServer", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/LivingEntity;resolveMobResponsibleForDamage(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void theabyss$damageReachedEnd(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        LivingFabricEvents.damageReachedEnd(this.theabyss$self());
    }

    @ModifyExpressionValue(method = "actuallyHurt", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float theabyss$damagePre(float damage, @Local(argsOnly = true) DamageSource source) {
        return LivingFabricEvents.damagePre(this.theabyss$self(), source, damage);
    }

    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;getEntity()Lnet/minecraft/world/entity/Entity;", ordinal = 0),
        cancellable = true)
    private void theabyss$death(DamageSource source, CallbackInfo ci) {
        if (LivingFabricEvents.death(this.theabyss$self(), source)) {
            ci.cancel();
        }
    }

    @WrapMethod(method = "dropAllDeathLoot")
    private void theabyss$deathDrops(ServerLevel level, DamageSource source, Operation<Void> original) {
        LivingEntity self = this.theabyss$self();
        if (!LivingFabricEvents.startCapturingDrops(self)) {
            original.call(level, source);
            return;
        }
        original.call(level, source);
        LivingFabricEvents.finishCapturingDrops(self);
    }

    @WrapOperation(method = "drop", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean theabyss$captureDrop(Level level, Entity drop, Operation<Boolean> original) {
        if (drop instanceof ItemEntity item && LivingFabricEvents.captureDrop(this.theabyss$self(), item)) {
            return true;
        }
        return original.call(level, drop);
    }

    @ModifyArg(method = "dropExperience", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"), index = 2)
    private int theabyss$experienceDrop(int experience) {
        return LivingFabricEvents.experience(this.theabyss$self(), this.getLastHurtByPlayer(), experience);
    }

    @Inject(method = "causeFallDamage", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/Entity;causeFallDamage(DFLnet/minecraft/world/damagesource/DamageSource;)Z"), cancellable = true)
    private void theabyss$fall(double fallDistance, float damageModifier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (LivingFabricEvents.fall(this.theabyss$self())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void theabyss$heal(float heal, CallbackInfo ci, @Local(argsOnly = true) LocalFloatRef amount) {
        Float result = LivingFabricEvents.heal(this.theabyss$self(), amount.get());
        if (result == null) {
            return;
        }
        if (result <= 0.0F) {
            ci.cancel();
            return;
        }
        amount.set(result);
    }

    @ModifyArg(method = "getVisibilityPercent", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(DDD)D"), index = 0)
    private double theabyss$visibility(double visibility) {
        return LivingFabricEvents.visibility(this.theabyss$self(), visibility);
    }

    @Inject(method = "startUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I"),
        cancellable = true)
    private void theabyss$useItemStart(CallbackInfo ci) {
        if (LivingFabricEvents.useItemStart(this.theabyss$self())) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "completeUsingItem", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack theabyss$useItemFinish(ItemStack stack, Level level, LivingEntity entity, Operation<ItemStack> original) {
        ItemStack copy = stack.copy();
        ItemStack result = original.call(stack, level, entity);
        LivingFabricEvents.useItemFinish(entity, copy);
        return result;
    }
}
