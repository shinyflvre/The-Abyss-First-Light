package net.yezon.theabyss.fabric.mixin.event.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public abstract class MusicManagerEventsMixin {
    @Shadow
    private SoundInstance currentMusic;

    @Shadow
    private int nextSongDelay;

    @Shadow
    public abstract void stopPlaying();

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getSituationalMusic()Lnet/minecraft/sounds/Music;"))
    private Music theabyss$selectMusic(Minecraft minecraft, Operation<Music> original, @Share("theabyss$noMusic") LocalBooleanRef noMusic) {
        Music situational = original.call(minecraft);
        if (!ClientFabricEvents.listensToMusic()) {
            return situational;
        }
        Music music = ClientFabricEvents.selectMusic(situational);
        noMusic.set(music == null);
        return music != null ? music : situational;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getSituationalMusic()Lnet/minecraft/sounds/Music;",
        shift = At.Shift.AFTER), cancellable = true)
    private void theabyss$noMusic(CallbackInfo ci, @Share("theabyss$noMusic") LocalBooleanRef noMusic) {
        if (noMusic.get()) {
            if (this.currentMusic != null) {
                this.stopPlaying();
            }
            this.nextSongDelay = 0;
            ci.cancel();
        }
    }
}
