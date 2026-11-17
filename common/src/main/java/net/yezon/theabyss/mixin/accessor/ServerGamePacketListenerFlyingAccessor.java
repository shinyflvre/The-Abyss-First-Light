package net.yezon.theabyss.mixin.accessor;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerFlyingAccessor {
    @Accessor("aboveGroundTickCount")
    void theabyss$setAboveGroundTickCount(int count);

    @Accessor("aboveGroundVehicleTickCount")
    void theabyss$setAboveGroundVehicleTickCount(int count);
}
