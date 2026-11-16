package net.yezon.theabyss.fabric.mixin.event.server;

import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.fabric.event.ServerFabricEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerTickEventMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void theabyss$playerTickPost(CallbackInfo ci) {
        ServerFabricEvents.playerTickPost((Player)(Object)this);
    }
}
