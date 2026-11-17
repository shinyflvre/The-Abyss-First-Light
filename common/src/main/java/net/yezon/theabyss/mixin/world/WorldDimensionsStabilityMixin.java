package net.yezon.theabyss.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Lifecycle;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.yezon.theabyss.TheabyssMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WorldDimensions.class)
public abstract class WorldDimensionsStabilityMixin {
    @ModifyReturnValue(method = "checkStability", at = @At("RETURN"))
    private static Lifecycle theabyss$modStemsStable(Lifecycle original, ResourceKey<LevelStem> key, LevelStem stem) {
        return TheabyssMod.MODID.equals(key.location().getNamespace()) ? Lifecycle.stable() : original;
    }

    @ModifyArg(method = "bake", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/MappedRegistry;<init>(Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Lifecycle;)V"), index = 1)
    private Lifecycle theabyss$ignoreModStems(Lifecycle original, @Local(argsOnly = true) Registry<LevelStem> registry) {
        if (original == Lifecycle.stable()) {
            return original;
        }
        long others = Stream.concat(registry.registryKeySet().stream(), ((WorldDimensions)(Object)this).dimensions().keySet().stream())
            .distinct()
            .filter(key -> !TheabyssMod.MODID.equals(key.location().getNamespace()))
            .count();
        return others == 3 ? Lifecycle.stable() : original;
    }
}
