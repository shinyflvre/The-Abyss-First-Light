package net.yezon.theabyss.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.yezon.theabyss.client.gravity.GravityClient;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @WrapMethod(method = "renderEntityInInventory(Lnet/minecraft/client/gui/GuiGraphics;FFFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/world/entity/LivingEntity;)V")
    private static void theabyss$gravityUpright(GuiGraphics graphics, float x, float y, float scale, Vector3f translate, Quaternionf pose,
        Quaternionf cameraOrientation, LivingEntity entity, Operation<Void> original) {
        GravityClient.inventoryRender(true);
        try {
            original.call(graphics, x, y, scale, translate, pose, cameraOrientation, entity);
        } finally {
            GravityClient.inventoryRender(false);
        }
    }
}
