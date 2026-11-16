package net.yezon.theabyss.fabric.mixin.event.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public abstract class SoundEngineEventsMixin {
    @Shadow
    private boolean loaded;

    @WrapMethod(method = "play")
    private SoundEngine.PlayResult theabyss$playSound(SoundInstance instance, Operation<SoundEngine.PlayResult> original) {
        if (!this.loaded) {
            return original.call(instance);
        }
        SoundInstance sound = ClientFabricEvents.playSound(instance);
        if (sound == null) {
            return SoundEngine.PlayResult.NOT_STARTED;
        }
        return original.call(sound);
    }

    @WrapOperation(method = "play", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenAccept(Ljava/util/function/Consumer;)Ljava/util/concurrent/CompletableFuture;",
        ordinal = 0))
    private <T> CompletableFuture<Void> theabyss$soundSource(CompletableFuture<T> future, Consumer<? super T> action, Operation<CompletableFuture<Void>> original,
        @Local ChannelAccess.ChannelHandle handle, @Local(argsOnly = true) SoundInstance instance) {
        if (!ClientFabricEvents.listensToSoundSources()) {
            return original.call(future, action);
        }
        Consumer<T> wrapped = value -> {
            action.accept(value);
            handle.execute(channel -> ClientFabricEvents.playSoundSource(channel, instance));
        };
        return original.call(future, wrapped);
    }

    @WrapOperation(method = "play", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenAccept(Ljava/util/function/Consumer;)Ljava/util/concurrent/CompletableFuture;",
        ordinal = 1))
    private <T> CompletableFuture<Void> theabyss$streamingSource(CompletableFuture<T> future, Consumer<? super T> action, Operation<CompletableFuture<Void>> original,
        @Local ChannelAccess.ChannelHandle handle, @Local(argsOnly = true) SoundInstance instance) {
        if (!ClientFabricEvents.listensToStreamingSources()) {
            return original.call(future, action);
        }
        Consumer<T> wrapped = value -> {
            action.accept(value);
            handle.execute(channel -> ClientFabricEvents.playStreamingSource(channel, instance));
        };
        return original.call(future, wrapped);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void theabyss$engineCreated(CallbackInfo ci) {
        ClientFabricEvents.soundEngineLoad();
    }

    @Inject(method = "reload", at = @At("TAIL"))
    private void theabyss$engineReloaded(CallbackInfo ci) {
        ClientFabricEvents.soundEngineLoad();
    }
}
