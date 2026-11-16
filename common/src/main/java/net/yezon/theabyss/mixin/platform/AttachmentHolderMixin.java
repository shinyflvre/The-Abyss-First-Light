package net.yezon.theabyss.mixin.platform;

import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.platform.attachment.IAttachmentHolder;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public abstract class AttachmentHolderMixin implements IAttachmentHolder {
}
