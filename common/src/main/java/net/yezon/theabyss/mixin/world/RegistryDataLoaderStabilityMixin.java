package net.yezon.theabyss.mixin.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Lifecycle;
import java.util.function.Function;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.yezon.theabyss.TheabyssMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RegistryDataLoader.class)
public abstract class RegistryDataLoaderStabilityMixin {
    @WrapOperation(method = "loadContentsFromManager", at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object theabyss$modEntriesStable(Function<Object, Object> function, Object pack, Operation<Object> original,
        @Local ResourceKey<?> key) {
        Object info = original.call(function, pack);
        if (info instanceof RegistrationInfo registration && registration.lifecycle() != Lifecycle.stable()
            && TheabyssMod.MODID.equals(key.location().getNamespace())) {
            return new RegistrationInfo(registration.knownPackInfo(), Lifecycle.stable());
        }
        return info;
    }
}
