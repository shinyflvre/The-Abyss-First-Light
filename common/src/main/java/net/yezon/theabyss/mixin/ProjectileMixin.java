package net.yezon.theabyss.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.gravity.Gravity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {
    @WrapMethod(method = "shootFromRotation")
    private void theabyss$gravityShoot(Entity source, float xRot, float yRot, float yOffset, float pow, float uncertainty, Operation<Void> original) {
        if (source instanceof Player && Gravity.active(source)) {
            Vec3 world = Gravity.toWorld(source.calculateViewVector(xRot, yRot), Gravity.of(source));
            xRot = (float)Math.toDegrees(-Math.asin(Mth.clamp(world.y, -1.0, 1.0)));
            yRot = (float)Math.toDegrees(Math.atan2(-world.x, world.z));
        }
        original.call(source, xRot, yRot, yOffset, pow, uncertainty);
    }
}
