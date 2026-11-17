package net.yezon.theabyss.fabric.blocks;

import java.util.function.BiFunction;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.yezon.theabyss.compat.TriState;
import net.yezon.theabyss.fabric.fluids.FluidContact;
import net.yezon.theabyss.platform.services.BlockItemHooks;

public final class FabricBlockItemHooks implements BlockItemHooks {
    @Override
    public boolean canCropGrow(Level level, BlockPos pos, BlockState state, boolean def) {
        return def;
    }

    @Override
    public void fireCropGrowPost(Level level, BlockPos pos, BlockState state) {
    }

    @Override
    public TriState canSustainPlant(BlockState soil, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        return FabricSoil.decision(soil, level, soilPosition, facing, plant);
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder(boolean searchBar, TabContents contents) {
        return FabricItemGroup.builder().displayItems((parameters, output) -> contents.accept(parameters, wrap(output)));
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, ? extends T> factory, Block... blocks) {
        return BlockEntityType.Builder.<T>of(factory::apply, blocks).build(null);
    }

    @Override
    public boolean trampleFarmland(ServerLevel level, BlockPos pos, BlockState state, float fallDistance, Entity entity) {
        if (level.getRandom().nextFloat() < fallDistance - 0.5 && entity instanceof LivingEntity) {
            boolean canTrample = entity instanceof Player player
                ? !level.getServer().isUnderSpawnProtection(level, pos, player)
                : level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            return canTrample && entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight() > 0.512F;
        }
        return false;
    }

    @Override
    public boolean farmlandNearWater(LevelReader level, BlockPos pos) {
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            FluidState fluid = level.getFluidState(checkPos);
            if (fluid.is(FluidTags.WATER) || FluidContact.hydrates(fluid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public float dripstoneDripChance(Fluid fluid) {
        if (fluid == Fluids.WATER) {
            return 0.17578125F;
        }
        return fluid == Fluids.LAVA ? 0.05859375F : -1.0F;
    }

    @Override
    public ParticleOptions dripstoneDripParticle(Fluid fluid) {
        return FluidContact.typeOf(fluid) == null && fluid.is(FluidTags.LAVA) ? ParticleTypes.DRIPPING_DRIPSTONE_LAVA : ParticleTypes.DRIPPING_DRIPSTONE_WATER;
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
