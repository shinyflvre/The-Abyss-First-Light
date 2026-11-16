package net.yezon.theabyss.neoforge.mixin.entity;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.entity.PartEntity;
import net.yezon.theabyss.entity.AbyssaurEntity;
import net.yezon.theabyss.platform.entity.MultipartEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbyssaurEntity.class)
public abstract class AbyssaurEntityMixin {
    public boolean isMultipartEntity() {
        return true;
    }

    public PartEntity<?>[] getParts() {
        Entity[] parts = ((MultipartEntity)this).abyssParts();
        PartEntity<?>[] result = new PartEntity<?>[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = (PartEntity<?>)parts[i];
        }
        return result;
    }
}
