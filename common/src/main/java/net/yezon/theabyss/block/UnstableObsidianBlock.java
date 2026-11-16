package net.yezon.theabyss.block;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.loot.LootParams;

public class UnstableObsidianBlock extends Block implements HoverTextBlock {
    public static final EnumProperty<Axis> AXIS = BlockStateProperties.AXIS;

    public UnstableObsidianBlock(BlockBehaviour.Properties properties) {
        super(
            properties
                .instrument(NoteBlockInstrument.BASEDRUM)
                .sound(SoundType.STONE)
                .strength(1.3F, 11.0F)
                .lightLevel(s -> 1)
                .requiresCorrectToolForDrops()
                .postProcess((bs, br, bp) -> bp)
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Axis.Y));
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.theabyss.unstable_obsidian"));
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return 3;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        if (rot == Rotation.CLOCKWISE_90 || rot == Rotation.COUNTERCLOCKWISE_90) {
            if (state.getValue(AXIS) == Axis.X) {
                return state.setValue(AXIS, Axis.Z);
            }

            if (state.getValue(AXIS) == Axis.Z) {
                return state.setValue(AXIS, Axis.X);
            }
        }

        return state;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this, 1));
    }
}
