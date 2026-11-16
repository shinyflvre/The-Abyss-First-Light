package net.yezon.theabyss.fabric.mixin.event.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.BooleanSupplier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.yezon.theabyss.fabric.event.ServerFabricEvents;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerEventsMixin {
    @Inject(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ServerTickRateManager;tick()V"))
    private void theabyss$serverTickPre(BooleanSupplier haveTime, CallbackInfo ci) {
        ServerFabricEvents.serverTickPre();
    }

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void theabyss$serverTickPost(BooleanSupplier haveTime, CallbackInfo ci) {
        ServerFabricEvents.serverTickPost((MinecraftServer)(Object)this);
    }

    @WrapOperation(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;tick(Ljava/util/function/BooleanSupplier;)V"))
    private void theabyss$levelTickPost(ServerLevel level, BooleanSupplier haveTime, Operation<Void> original) {
        original.call(level, haveTime);
        ServerFabricEvents.levelTickPost(level);
    }

    @Inject(method = "runServer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;stopped:Z", opcode = Opcodes.PUTFIELD, ordinal = 0))
    private void theabyss$serverStopping(CallbackInfo ci) {
        ServerFabricEvents.serverStopping((MinecraftServer)(Object)this);
    }

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;onServerExit()V"))
    private void theabyss$serverStopped(CallbackInfo ci) {
        ServerFabricEvents.serverStopped();
    }
}
