package net.yezon.theabyss.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftCameraEntityAccessor {
    @Accessor("cameraEntity")
    void theabyss$setCameraEntity(Entity entity);
}
