package net.yezon.theabyss.fabric.mixin.fluids;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.platform.common.SoundActions;
import net.yezon.theabyss.platform.fluids.FluidType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BucketItem.class)
public abstract class BucketItemFluidMixin {
    @Shadow
    @Final
    private Fluid content;

    @ModifyVariable(method = "playEmptySound", at = @At("STORE"), ordinal = 0)
    private SoundEvent theabyss$ownFluidEmptySound(SoundEvent original) {
        FluidType type = FluidContact.typeOf(this.content);
        if (type == null) {
            return original;
        }
        SoundEvent sound = type.getSound(SoundActions.BUCKET_EMPTY);
        return sound != null ? sound : original;
    }
}
