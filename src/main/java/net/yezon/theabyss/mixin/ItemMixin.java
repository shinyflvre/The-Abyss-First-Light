package net.yezon.theabyss.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.gravity.Gravity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public abstract class ItemMixin {
    @WrapMethod(method = "getPlayerPOVHitResult")
    private static BlockHitResult theabyss$gravityPov(Level level, Player player, ClipContext.Fluid fluid, Operation<BlockHitResult> original) {
        if (!Gravity.active(player)) {
            return original.call(level, player, fluid);
        }
        Vec3 from = player.getEyePosition();
        Vec3 to = from.add(player.getViewVector(1.0F).scale(player.blockInteractionRange()));
        return level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, fluid, player));
    }
}
