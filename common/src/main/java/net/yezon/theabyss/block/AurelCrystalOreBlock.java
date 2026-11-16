package net.yezon.theabyss.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yezon.theabyss.block.hooks.FireSpreadingBlock;
import net.yezon.theabyss.block.hooks.FlammableBlock;
import net.yezon.theabyss.platform.common.util.DeferredSoundType;
import net.yezon.theabyss.events.CaveMossGenEvents;
import net.yezon.theabyss.init.TheabyssModBlocks;

public class AurelCrystalOreBlock extends FlowerBlock implements FlammableBlock, FireSpreadingBlock {
    public AurelCrystalOreBlock(BlockBehaviour.Properties properties) {
        super(
            MobEffects.SATURATION,
            0.0F,
            properties
                .mapColor(MapColor.PLANT)
                .sound(
                    new DeferredSoundType(
                        1.0F,
                        1.0F,
                        () -> BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:crystal_break")),
                        () -> BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.stone.step")),
                        () -> BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.stone.place")),
                        () -> BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.stone.hit")),
                        () -> BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.snow.fall"))
                    )
                )
                .instabreak()
                .postProcess((bs, br, bp) -> bp)
                .emissiveRendering(bs -> true)
                .lightLevel(s -> 10)
                .noCollision()
                .offsetType(OffsetType.XZ)
                .pushReaction(PushReaction.POPPED)
        );
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 100;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 60;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(TheabyssModBlocks.AUREL_CRYSTAL_ORE.get()));
    }

    @Override
    protected boolean mayPlaceOn(BlockState groundState, BlockGetter worldIn, BlockPos pos) {
        boolean additionalCondition = !(worldIn instanceof LevelAccessor) || CaveMossGenEvents.execute(pos.getY() + 1);
        return groundState.is(TheabyssModBlocks.STONE.get()) && additionalCondition;
    }

    @Override
    protected boolean canSurvive(BlockState blockstate, LevelReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.below();
        BlockState groundState = worldIn.getBlockState(blockpos);
        return this.mayPlaceOn(groundState, worldIn, blockpos);
    }
}
