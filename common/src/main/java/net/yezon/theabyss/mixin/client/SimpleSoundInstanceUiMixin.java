package net.yezon.theabyss.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.yezon.theabyss.client.audio.UiSounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SimpleSoundInstance.class)
public abstract class SimpleSoundInstanceUiMixin {
    @ModifyReturnValue(method = "forUI(Lnet/minecraft/sounds/SoundEvent;FF)Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;", at = @At("RETURN"))
    private static SimpleSoundInstance theabyss$markUiSound(SimpleSoundInstance original) {
        return UiSounds.mark(original);
    }
}
