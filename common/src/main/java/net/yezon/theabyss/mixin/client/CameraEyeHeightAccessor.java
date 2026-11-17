package net.yezon.theabyss.mixin.client;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public interface CameraEyeHeightAccessor {
    @Accessor("eyeHeight")
    float theabyss$eyeHeight();

    @Accessor("eyeHeightOld")
    float theabyss$eyeHeightOld();
}
