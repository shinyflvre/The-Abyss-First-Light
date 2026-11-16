package net.yezon.theabyss.fabric.mixin.server;

import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.fabric.server.FabricAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerCreativeFlightMixin {
    @Unique
    private boolean theabyss$attributeFlight = true;

    @Inject(method = "doTick", at = @At("TAIL"))
    private void theabyss$dropAttributeFlight(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        boolean attribute = FabricAttributes.attributeFlight(player);
        if (this.theabyss$attributeFlight && !attribute && player.getAbilities().flying && !player.getAbilities().mayfly) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
        this.theabyss$attributeFlight = attribute;
    }
}
