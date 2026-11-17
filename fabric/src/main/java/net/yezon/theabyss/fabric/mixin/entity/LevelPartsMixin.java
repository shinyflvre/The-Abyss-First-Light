package net.yezon.theabyss.fabric.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.yezon.theabyss.fabric.entity.FabricPartLevel;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class LevelPartsMixin implements FabricPartLevel {
    @Unique
    private final Int2ObjectMap<Entity> theabyss$partMap = new Int2ObjectOpenHashMap<>();

    @Override
    public Int2ObjectMap<Entity> theabyss$parts() {
        return this.theabyss$partMap;
    }

    @ModifyReturnValue(method = "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;", at = @At("RETURN"))
    private List<Entity> theabyss$addParts(List<Entity> output, @Local(argsOnly = true) @Nullable Entity except, @Local(argsOnly = true) AABB bb,
        @Local(argsOnly = true) Predicate<? super Entity> selector) {
        if (this.theabyss$partMap.isEmpty()) {
            return output;
        }
        for (Entity part : this.theabyss$partMap.values()) {
            if (part != except && part.getBoundingBox().intersects(bb) && selector.test(part)) {
                output.add(part);
            }
        }
        return output;
    }

    @Inject(method = "getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;I)V", at = @At("TAIL"))
    private <T extends Entity> void theabyss$addTypedParts(EntityTypeTest<Entity, T> type, AABB bb, Predicate<? super T> selector, List<? super T> output, int maxResults,
        CallbackInfo ci) {
        if (this.theabyss$partMap.isEmpty()) {
            return;
        }
        for (Entity part : this.theabyss$partMap.values()) {
            T cast = type.tryCast(part);
            if (cast != null && cast.getBoundingBox().intersects(bb) && selector.test(cast)) {
                output.add(cast);
                if (output.size() >= maxResults) {
                    break;
                }
            }
        }
    }
}
