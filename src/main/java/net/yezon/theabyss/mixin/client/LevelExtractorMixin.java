package net.yezon.theabyss.mixin.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.gravity.Gravity;
import net.yezon.theabyss.gravity.GravityEntity;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
    @Inject(method = "getViewBlockingStateAndPos", at = @At("HEAD"), cancellable = true)
    private static void theabyss$gravityViewBlocking(LocalPlayer player, Frustum frustum, CallbackInfoReturnable<Pair<BlockState, BlockPos>> cir) {
        if (((GravityEntity)player).theabyss$gravityId() == 0 || player.noPhysics) {
            return;
        }
        Direction gravity = Gravity.of(player);
        Vec3 eye = player.getEyePosition();
        AABB nearPlane = frustum.getNearPlaneBounds().move(eye);
        double wide = player.getBbWidth() * 0.8;
        double thin = 0.1 * player.getScale();
        BlockPos.MutableBlockPos test = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 8; i++) {
            double a = (i & 1) - 0.5;
            double b = (i >> 1 & 1) - 0.5;
            double c = (i >> 2 & 1) - 0.5;
            double dx = gravity.getAxis() == Direction.Axis.X ? b * thin : a * wide;
            double dy = gravity.getAxis() == Direction.Axis.Y ? b * thin : (gravity.getAxis() == Direction.Axis.X ? a * wide : b * wide);
            double dz = gravity.getAxis() == Direction.Axis.Z ? b * thin : c * wide;
            test.set(eye.x + dx, eye.y + dy, eye.z + dz);
            BlockState state = player.level().getBlockState(test);
            if (state.getRenderShape() != RenderShape.INVISIBLE && state.isViewBlocking(player.level(), test, nearPlane)) {
                cir.setReturnValue(Pair.of(state, test.immutable()));
                return;
            }
        }
        cir.setReturnValue(null);
    }
}
