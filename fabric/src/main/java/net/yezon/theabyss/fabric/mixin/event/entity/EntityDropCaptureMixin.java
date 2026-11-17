package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Collection;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.yezon.theabyss.fabric.event.FabricDropCaptureState;
import net.yezon.theabyss.fabric.event.LivingFabricEvents;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityDropCaptureMixin implements FabricDropCaptureState {
    @Unique
    private @Nullable Collection<ItemEntity> theabyss$drops;

    @Override
    public @Nullable Collection<ItemEntity> theabyss$capturedDrops() {
        return this.theabyss$drops;
    }

    @Override
    public @Nullable Collection<ItemEntity> theabyss$captureDrops(@Nullable Collection<ItemEntity> drops) {
        Collection<ItemEntity> previous = this.theabyss$drops;
        this.theabyss$drops = drops;
        return previous;
    }

    @WrapOperation(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean theabyss$captureSpawnedDrop(Level level, Entity drop, Operation<Boolean> original) {
        if (drop instanceof ItemEntity item && LivingFabricEvents.captureDrop((Entity)(Object)this, item)) {
            return true;
        }
        return original.call(level, drop);
    }
}
