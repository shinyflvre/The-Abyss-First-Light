package net.yezon.theabyss.fabric.mixin.event.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.fabric.event.LevelFabricEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Explosion.class)
public abstract class ExplosionDetonateMixin {
    @Shadow
    @Final
    private Level level;

    @Shadow
    @Final
    private ObjectArrayList<BlockPos> toBlow;

    @ModifyExpressionValue(method = "explode", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
    private List<Entity> theabyss$detonate(List<Entity> entities) {
        LevelFabricEvents.explosionDetonate(this.level, this.toBlow);
        return entities;
    }
}
