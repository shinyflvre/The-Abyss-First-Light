package net.yezon.theabyss.block;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yezon.theabyss.events.GetIncorytheOreAdvEvents;
import net.yezon.theabyss.init.TheabyssModItems;

public class IncorytheOreBlock extends Block implements HoverTextBlock {
    public IncorytheOreBlock(BlockBehaviour.Properties properties) {
        super(properties.instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.STONE).strength(4.0F, 10.0F).requiresCorrectToolForDrops());
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.theabyss.craft_armor_1"));
        tooltip.accept(Component.translatable("tooltip.theabyss.craft_armor_2"));
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(TheabyssModItems.INCORYTHE_GEM.get()));
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState blockstate, Level world, BlockPos pos, Player entity, ItemStack toolStack, boolean willHarvest, FluidState fluid) {
        boolean retval = super.onDestroyedByPlayer(blockstate, world, pos, entity, toolStack, willHarvest, fluid);
        GetIncorytheOreAdvEvents.execute(entity);
        return retval;
    }
}
