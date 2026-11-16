package net.yezon.theabyss.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.yezon.theabyss.block.entity.AbyssalEnergeticaBlockEntity;
import net.yezon.theabyss.station.StationEnergy;
import org.jspecify.annotations.Nullable;

public class AbyssalEnergeticaBlock extends Block implements EntityBlock {
    public static final BooleanProperty CHARGED = BooleanProperty.create("charged");
    private static final VoxelShape SHAPE = Shapes.or(
        box(1.0, 12.0, 1.0, 15.0, 15.0, 15.0),
        box(2.0, 10.0, 2.0, 14.0, 12.0, 14.0),
        box(6.0, 0.0, 6.0, 10.0, 10.0, 10.0),
        box(7.0, 0.0, 0.0, 9.0, 10.0, 16.0),
        box(0.0, 0.0, 7.0, 16.0, 10.0, 9.0)
    );

    public AbyssalEnergeticaBlock(BlockBehaviour.Properties properties) {
        super(properties.instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.STONE).strength(1.5F, 6.0F).noOcclusion()
            .lightLevel(state -> state.getValue(CHARGED) ? 11 : 0).isRedstoneConductor((s, g, p) -> false));
        this.registerDefaultState(this.stateDefinition.any().setValue(CHARGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        if (!StationEnergy.isEnergy(stack)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!(level.getBlockEntity(pos) instanceof AbyssalEnergeticaBlockEntity pillar) || pillar.charged()) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel) {
            pillar.setEnergy(stack.copyWithCount(1));
            net.yezon.theabyss.lore.Chronicles.note(player, net.yezon.theabyss.lore.Chronicles.PILLARS);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.7F, 1.6F);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, 1.2F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof AbyssalEnergeticaBlockEntity pillar) || !pillar.charged()) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel) {
            ItemStack energy = pillar.energy();
            pillar.setEnergy(ItemStack.EMPTY);
            player.getInventory().placeItemBackInInventory(energy, net.minecraft.util.Prediction.SERVER_ONLY);
            level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.6F, 1.8F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        return !drops.isEmpty() ? drops : Collections.singletonList(new ItemStack(this, 1));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AbyssalEnergeticaBlockEntity(pos, state);
    }
}
