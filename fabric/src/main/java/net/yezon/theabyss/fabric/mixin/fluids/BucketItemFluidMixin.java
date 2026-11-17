package net.yezon.theabyss.fabric.mixin.fluids;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.Fluid;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.platform.common.SoundActions;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BucketItem.class)
public abstract class BucketItemFluidMixin {
    @Shadow
    @Final
    private Fluid content;

    @WrapOperation(
        method = "playEmptySound",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/LevelAccessor;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"
        )
    )
    private void theabyss$ownFluidEmptySound(LevelAccessor level, Player player, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch,
                                             Operation<Void> original) {
        FluidType type = FluidContact.typeOf(this.content);
        SoundEvent own = type == null ? null : type.getSound(SoundActions.BUCKET_EMPTY);
        original.call(level, player, pos, own != null ? own : sound, source, volume, pitch);
    }
}
