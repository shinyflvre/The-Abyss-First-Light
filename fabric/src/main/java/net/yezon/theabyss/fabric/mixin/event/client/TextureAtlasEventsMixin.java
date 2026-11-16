package net.yezon.theabyss.fabric.mixin.event.client;

import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.yezon.theabyss.fabric.event.ClientFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasEventsMixin {
    @Inject(method = "upload", at = @At("TAIL"))
    private void theabyss$atlasStitched(SpriteLoader.Preparations preparations, CallbackInfo ci) {
        ClientFabricEvents.atlasStitched((TextureAtlas)(Object)this);
    }
}
