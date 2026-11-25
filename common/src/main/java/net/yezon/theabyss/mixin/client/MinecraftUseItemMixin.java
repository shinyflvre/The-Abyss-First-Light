package net.yezon.theabyss.mixin.client;

import net.minecraft.client.Minecraft;
import net.yezon.theabyss.client.compat.ObscureSkillsClientCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftUseItemMixin {
    @Inject(method = "startUseItem", at = @At("TAIL"))
    private void theabyss$obscureIdleUse(CallbackInfo ci) {
        ObscureSkillsClientCompat.onIdleUse((Minecraft)(Object)this);
    }
}
