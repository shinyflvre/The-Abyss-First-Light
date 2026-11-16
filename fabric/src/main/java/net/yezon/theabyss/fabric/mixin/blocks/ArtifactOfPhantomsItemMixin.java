package net.yezon.theabyss.fabric.mixin.blocks;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.item.ArtifactOfPhantomsItem;
import net.yezon.theabyss.item.hooks.ReequipAnimationItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ArtifactOfPhantomsItem.class)
public abstract class ArtifactOfPhantomsItemMixin implements FabricItem {
    @Override
    public boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        return ((ReequipAnimationItem)this).shouldCauseReequipAnimation(oldStack, newStack, false);
    }
}
