package net.yezon.theabyss.neoforge.blocks;

import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.FarmlandWaterManager;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.yezon.theabyss.compat.TriState;
import net.yezon.theabyss.platform.services.BlockItemHooks;
import org.jspecify.annotations.Nullable;

public final class NeoForgeBlockItemHooks implements BlockItemHooks {
    @Override
    public boolean canCropGrow(Level level, BlockPos pos, BlockState state, boolean def) {
        return CommonHooks.canCropGrow(level, pos, state, def);
    }

    @Override
    public void fireCropGrowPost(Level level, BlockPos pos, BlockState state) {
        CommonHooks.fireCropGrowPost(level, pos, state);
    }

    @Override
    public TriState canSustainPlant(BlockState soil, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        return toCommon(soil.canSustainPlant(level, soilPosition, facing, plant));
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder(boolean searchBar, TabContents contents) {
        CreativeModeTab.Builder builder = CreativeModeTab.builder();
        if (searchBar) {
            builder.withSearchBar();
        }
        return builder.displayItems((parameters, output) -> contents.accept(parameters, wrap(output)));
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, ? extends T> factory, Block... blocks) {
        return BlockEntityType.Builder.<T>of(factory::apply, blocks).build(null);
    }

    @Override
    public boolean trampleFarmland(ServerLevel level, BlockPos pos, BlockState state, float fallDistance, Entity entity) {
        if (entity.canTrample(state, pos, fallDistance) && !(entity instanceof Player player && level.getServer().isUnderSpawnProtection(level, pos, player))) {
            BlockEvent.FarmlandTrampleEvent event = new BlockEvent.FarmlandTrampleEvent(level, pos, state, fallDistance, entity);
            NeoForge.EVENT_BUS.post(event);
            return !event.isCanceled();
        }
        return false;
    }

    @Override
    public boolean farmlandNearWater(LevelReader level, BlockPos pos) {
        BlockState farmland = level.getBlockState(pos);
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            if (farmland.canBeHydrated(level, pos, level.getFluidState(checkPos), checkPos)) {
                return true;
            }
        }
        return FarmlandWaterManager.hasBlockWaterTicket(level, pos);
    }

    @Override
    public float dripstoneDripChance(Fluid fluid) {
        FluidType.DripstoneDripInfo info = fluid.getFluidType().getDripInfo();
        return info != null ? info.chance() : -1.0F;
    }

    @Override
    public @Nullable ParticleOptions dripstoneDripParticle(Fluid fluid) {
        FluidType.DripstoneDripInfo info = fluid.getFluidType().getDripInfo();
        return info != null ? info.dripParticle() : ParticleTypes.DRIPPING_DRIPSTONE_WATER;
    }

    public static TriState toCommon(net.neoforged.neoforge.common.util.TriState value) {
        return switch (value) {
            case TRUE -> TriState.TRUE;
            case FALSE -> TriState.FALSE;
            default -> TriState.DEFAULT;
        };
    }

    public static net.neoforged.neoforge.common.util.TriState toNeoForge(TriState value) {
        return switch (value) {
            case TRUE -> net.neoforged.neoforge.common.util.TriState.TRUE;
            case FALSE -> net.neoforged.neoforge.common.util.TriState.FALSE;
            default -> net.neoforged.neoforge.common.util.TriState.DEFAULT;
        };
    }

    private static TabOutput wrap(CreativeModeTab.Output output) {
        return new TabOutput() {
            @Override
            public void accept(ItemStack stack) {
                output.accept(stack);
            }

            @Override
            public void accept(ItemLike item) {
                output.accept(item);
            }
        };
    }
}
