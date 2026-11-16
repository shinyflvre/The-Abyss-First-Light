package net.yezon.theabyss.neoforge.mixin.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.yezon.theabyss.block.AberytheOreBlock;
import net.yezon.theabyss.block.BlaruLeavesBlock;
import net.yezon.theabyss.block.FrozenLeavesBlock;
import net.yezon.theabyss.block.FusionOreBlock;
import net.yezon.theabyss.block.GarniteOreBlock;
import net.yezon.theabyss.block.GlacerytheOreBlock;
import net.yezon.theabyss.block.IgnisitheOreBlock;
import net.yezon.theabyss.block.IncorytheOreBlock;
import net.yezon.theabyss.block.NightgroveLeavesBlock;
import net.yezon.theabyss.block.RaptorEggBlock;
import net.yezon.theabyss.block.VigilantLeavesBlock;
import net.yezon.theabyss.block.hooks.PlayerDestroyListener;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({
    AberytheOreBlock.class, BlaruLeavesBlock.class, FrozenLeavesBlock.class, FusionOreBlock.class, GarniteOreBlock.class, GlacerytheOreBlock.class,
    IgnisitheOreBlock.class, IncorytheOreBlock.class, NightgroveLeavesBlock.class, RaptorEggBlock.class, VigilantLeavesBlock.class
})
public abstract class PlayerDestroyListenerMixin extends Block {
    private PlayerDestroyListenerMixin(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest, FluidState fluid) {
        boolean removed = super.onDestroyedByPlayer(state, level, pos, player, toolStack, willHarvest, fluid);
        ((PlayerDestroyListener)this).afterDestroyedByPlayer(state, level, pos, player, toolStack, willHarvest, fluid);
        return removed;
    }
}
