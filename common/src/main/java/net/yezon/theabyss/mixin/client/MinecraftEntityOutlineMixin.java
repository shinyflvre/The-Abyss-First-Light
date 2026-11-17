package net.yezon.theabyss.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.platform.client.EntityOutlines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MinecraftEntityOutlineMixin {
    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    private boolean theabyss$effectOutline(boolean glowing, Entity entity) {
        return glowing || EntityOutlines.colorOf(entity) != 0;
    }
}
