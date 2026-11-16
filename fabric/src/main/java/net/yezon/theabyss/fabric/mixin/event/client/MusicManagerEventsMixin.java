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

@Mixin(MusicManager.class)
public abstract class MusicManagerEventsMixin {
    @Shadow
    private SoundInstance currentMusic;

    @Shadow
    public abstract void stopPlaying();

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getSituationalMusic()Lnet/minecraft/sounds/Music;"))
    private Music theabyss$selectMusic(Minecraft minecraft, Operation<Music> original, @Share("theabyss$musicRemoved") LocalBooleanRef removed) {
        Music situational = original.call(minecraft);
        if (!ClientFabricEvents.listensToMusic()) {
            return situational;
        }
        Music music = ClientFabricEvents.selectMusic(situational);
        removed.set(music == null && situational != null);
        return music;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"))
    private int theabyss$noMusicDelay(int delay, int minimum, Operation<Integer> original, @Share("theabyss$musicRemoved") LocalBooleanRef removed) {
        if (removed.get()) {
            if (this.currentMusic != null) {
                this.stopPlaying();
            }
            return 0;
        }
        return original.call(delay, minimum);
    }
}
