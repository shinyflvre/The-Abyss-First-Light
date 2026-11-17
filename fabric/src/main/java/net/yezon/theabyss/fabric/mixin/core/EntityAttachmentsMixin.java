package net.yezon.theabyss.fabric.mixin.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.compat.ValueInput;
import net.yezon.theabyss.compat.ValueOutput;
import net.yezon.theabyss.fabric.core.AttachmentStorage;
import net.yezon.theabyss.fabric.core.FabricAttachments;
import net.yezon.theabyss.platform.attachment.AttachmentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityAttachmentsMixin implements AttachmentStorage {
    @Unique
    private final Map<AttachmentType<?>, Object> theabyss$attachmentMap = new ConcurrentHashMap<>();

    @Override
    public Map<AttachmentType<?>, Object> theabyss$attachments() {
        return this.theabyss$attachmentMap;
    }

    @Inject(method = "saveWithoutId(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
    private void theabyss$saveAttachments(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        Entity self = (Entity)(Object)this;
        FabricAttachments.save(self, ValueOutput.of(tag, self.registryAccess()));
    }

    @Inject(method = "load(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void theabyss$loadAttachments(CompoundTag tag, CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        FabricAttachments.load(self, ValueInput.of(tag, self.registryAccess()));
    }
}
