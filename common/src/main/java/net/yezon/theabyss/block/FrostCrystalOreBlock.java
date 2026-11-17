package net.yezon.theabyss.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yezon.theabyss.platform.common.util.DeferredSoundType;
import net.yezon.theabyss.init.TheabyssModBlocks;

public class FrostCrystalOreBlock extends FlowerBlock {
    public FrostCrystalOreBlock(BlockBehaviour.Properties properties) {
        super(
            MobEffects.SATURATION,
            0.0F,
            properties
                .mapColor(MapColor.PLANT)
                .sound(
                    new DeferredSoundType(
                        1.0F,
                        1.0F,
                        () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:crystal_break")),
                        () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.stone.step")),
                        () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.stone.place")),
                        () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.stone.hit")),
                        () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.stone.fall"))
                    )
                )
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
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(TheabyssModBlocks.FROST_CRYSTAL_ORE.get()));
    }

    @Override
    protected boolean mayPlaceOn(BlockState groundState, BlockGetter worldIn, BlockPos pos) {
        return groundState.is(TheabyssModBlocks.DARK_STONE.get())
            || groundState.is(TheabyssModBlocks.SNOW_BLOCK.get())
            || groundState.is(Blocks.SNOW_BLOCK)
            || groundState.is(Blocks.POWDER_SNOW);
    }

    @Override
    protected boolean canSurvive(BlockState blockstate, LevelReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.below();
        BlockState groundState = worldIn.getBlockState(blockpos);
        return this.mayPlaceOn(groundState, worldIn, blockpos);
    }
}
