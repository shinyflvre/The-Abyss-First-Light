package net.yezon.theabyss.mixin;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.gravity.Gravity;
import net.yezon.theabyss.gravity.GravityEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "canPlayerFitWithinBlocksAndEntitiesWhen", at = @At("HEAD"), cancellable = true)
    private void theabyss$gravityFit(Pose pose, CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player)(Object)this;
        if (((GravityEntity)self).theabyss$gravityId() == 0) {
            return;
        }
        EntityDimensions dims = self.getDimensions(pose);
        cir.setReturnValue(self.level().noCollision(self, Gravity.box(self.position(), Gravity.of(self), dims.width(), dims.height()).deflate(1.0E-7)));
    }
}
