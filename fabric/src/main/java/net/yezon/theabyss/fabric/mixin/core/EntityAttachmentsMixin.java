package net.yezon.theabyss.fabric.mixin.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.yezon.theabyss.fabric.core.AttachmentStorage;
import net.yezon.theabyss.fabric.core.FabricAttachments;
import net.yezon.theabyss.platform.attachment.AttachmentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityAttachmentsMixin implements AttachmentStorage {
    @Unique
    private final Map<AttachmentType<?>, Object> theabyss$attachmentMap = new ConcurrentHashMap<>();

    @Override
    public Map<AttachmentType<?>, Object> theabyss$attachments() {
        return this.theabyss$attachmentMap;
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void theabyss$saveAttachments(ValueOutput output, CallbackInfo ci) {
        FabricAttachments.save((Entity)(Object)this, output);
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void theabyss$loadAttachments(ValueInput input, CallbackInfo ci) {
        FabricAttachments.load((Entity)(Object)this, input);
    }
}
