package net.yezon.theabyss.fabric.mixin.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.yezon.theabyss.fabric.entity.FabricPersistentData;
import net.yezon.theabyss.fabric.entity.FabricPersistentDataHolder;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityPersistentDataMixin implements FabricPersistentDataHolder {
    @Unique
    private @Nullable CompoundTag theabyss$persistentData;

    @Override
    public @Nullable CompoundTag theabyss$persistentDataOrNull() {
        return this.theabyss$persistentData;
    }

    @Override
    public CompoundTag theabyss$persistentData() {
        if (this.theabyss$persistentData == null) {
            this.theabyss$persistentData = new CompoundTag();
        }
        return this.theabyss$persistentData;
    }

    @Override
    public void theabyss$setPersistentData(CompoundTag data) {
        this.theabyss$persistentData = data;
    }

    @Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V"))
    private void theabyss$savePersistentData(ValueOutput output, CallbackInfo ci) {
        FabricPersistentData.save((Entity)(Object)this, output);
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V"))
    private void theabyss$loadPersistentData(ValueInput input, CallbackInfo ci) {
        FabricPersistentData.load((Entity)(Object)this, input);
    }
}
