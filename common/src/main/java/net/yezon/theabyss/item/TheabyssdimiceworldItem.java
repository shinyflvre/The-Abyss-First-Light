package net.yezon.theabyss.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.block.TheabyssdimiceworldPortalBlock;

public class TheabyssdimiceworldItem extends Item {
    public TheabyssdimiceworldItem(Item.Properties properties) {
        super(properties.durability(64));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player entity = context.getPlayer();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        ItemStack itemstack = context.getItemInHand();
        Level world = context.getLevel();
        if (entity == null || !entity.mayUseItemAt(pos, context.getClickedFace(), itemstack)) {
            return InteractionResult.FAIL;
        }

        if (world.isEmptyBlock(pos)) {
            TheabyssdimiceworldPortalBlock.portalSpawn(world, pos);
            itemstack.hurtAndBreak(1, entity, context.getHand());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
}
