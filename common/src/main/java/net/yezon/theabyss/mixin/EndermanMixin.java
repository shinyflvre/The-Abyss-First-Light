package net.yezon.theabyss.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.entity.EndSpiderEntity;
import net.yezon.theabyss.entity.EndSpiderFx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enderman.class)
public abstract class EndermanMixin {
    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private void theabyss$endSpiderAmbient(Level level, ParticleOptions particle, double x, double y, double z, double xd, double yd, double zd,
                                           Operation<Void> original) {
        if (!((Object)this instanceof EndSpiderEntity)) {
            original.call(level, particle, x, y, z, xd, yd, zd);
            return;
        }
        int roll = level.getRandom().nextInt(12);
        if (roll < 2) {
            level.addParticle(EndSpiderFx.ambientShadow(), x, y, z, xd * 0.02, 0.01, zd * 0.02);
        } else if (roll == 2) {
            level.addParticle(EndSpiderFx.AMBIENT, x, y, z, xd * 0.02, 0.01 + level.getRandom().nextDouble() * 0.02, zd * 0.02);
        }
    }
}
