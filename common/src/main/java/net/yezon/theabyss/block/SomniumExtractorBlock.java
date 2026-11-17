package net.yezon.theabyss.block;

import io.netty.buffer.Unpooled;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.yezon.theabyss.block.entity.SomniumExtractorBlockEntity;
import net.yezon.theabyss.platform.services.ServerHooks;
import net.yezon.theabyss.world.inventory.ExtractorMenu;
import org.jspecify.annotations.Nullable;

public class SomniumExtractorBlock extends Block implements EntityBlock {
    public SomniumExtractorBlock(BlockBehaviour.Properties properties) {
        super(
            properties
                .instrument(NoteBlockInstrument.BASEDRUM)
                .sound(SoundType.STONE)
                .strength(1.0F, 10.0F)
                .noOcclusion()
                .isRedstoneConductor((bs, br, bp) -> false)
        );
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this, 1));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockstate, Level world, BlockPos pos, Player entity, BlockHitResult hit) {
        if (entity instanceof ServerPlayer player) {
            if (world.getBlockEntity(pos) instanceof net.yezon.theabyss.block.entity.SomniumExtractorBlockEntity machine) {
                ServerHooks.get().openMenu(player, machine, pos);
            }
        }

        return InteractionResult.sidedSuccess(world.isClientSide());
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        return worldIn.getBlockEntity(pos) instanceof MenuProvider menuProvider ? menuProvider : null;
    }

    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.@org.jspecify.annotations.Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                                                            net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        if (!(level instanceof ServerLevel) || !(type == net.yezon.theabyss.init.TheabyssModBlockEntities.SOMNIUM_EXTRACTOR.get())) {
            return null;
        }
        return (l, pos, st, be) -> net.yezon.theabyss.block.entity.StationMachineBlockEntity.serverTick((ServerLevel)l, pos, st,
            (net.yezon.theabyss.block.entity.StationMachineBlockEntity)be);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SomniumExtractorBlockEntity(pos, state);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level world, BlockPos pos, int eventID, int eventParam) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(eventID, eventParam);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof SomniumExtractorBlockEntity be ? AbstractContainerMenu.getRedstoneSignalFromContainer(be) : 0;
    }
}
