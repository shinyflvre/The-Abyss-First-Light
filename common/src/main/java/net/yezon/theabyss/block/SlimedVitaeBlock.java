package net.yezon.theabyss.block;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.yezon.theabyss.platform.common.CommonHooks;
import net.yezon.theabyss.events.HealFlowerEffectEvents;

public class SlimedVitaeBlock extends SugarCaneBlock implements HoverTextBlock {
    public SlimedVitaeBlock(BlockBehaviour.Properties properties) {
        super(
            properties
                .mapColor(MapColor.PLANT)
                .randomTicks()
                .sound(SoundType.GRASS)
                .instabreak()
                .hasPostProcess((bs, br, bp) -> true)
                .emissiveRendering((bs, br, bp) -> true)
                .lightLevel(s -> 10)
                .noCollission()
                .offsetType(OffsetType.XZ)
                .pushReaction(PushReaction.DESTROY)
        );
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        tooltip.accept(Component.translatable("tooltip.theabyss.plant_cures"));
    }

    @Override
    protected void randomTick(BlockState blockstate, ServerLevel world, BlockPos blockpos, RandomSource random) {
        if (world.isEmptyBlock(blockpos.above())) {
            int i = 1;

            while (world.getBlockState(blockpos.below(i)).is(this)) {
                i++;
            }

            if (i < 10) {
                int j = blockstate.getValue(AGE);
                if (CommonHooks.canCropGrow(world, blockpos, blockstate, true)) {
                    if (j == 15) {
                        world.setBlockAndUpdate(blockpos.above(), this.defaultBlockState());
                        world.setBlock(blockpos, blockstate.setValue(AGE, 0), 4);
                    } else {
                        world.setBlock(blockpos, blockstate.setValue(AGE, j + 1), 4);
                    }
                }
            }
        }
    }

    @Override
    protected void entityInside(BlockState blockstate, Level world, BlockPos pos, Entity entity) {
        super.entityInside(blockstate, world, pos, entity);
        HealFlowerEffectEvents.execute(entity);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return super.canSurvive(state, level, pos) || AbyssPlantSupport.supportsVegetation(level.getBlockState(pos.below()));
    }
}
