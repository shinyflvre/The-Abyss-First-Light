package net.yezon.theabyss.neoforge.mixin.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;
import net.yezon.theabyss.block.BlackVoidBlock;
import net.yezon.theabyss.block.BlaruLogBlock;
import net.yezon.theabyss.block.BlaruMossBlock;
import net.yezon.theabyss.block.DirtBlock;
import net.yezon.theabyss.block.FrozenLogBlock;
import net.yezon.theabyss.block.InfectedGrassBlock;
import net.yezon.theabyss.block.InfectedGravelBlock;
import net.yezon.theabyss.block.InfectedPeatBlock;
import net.yezon.theabyss.block.LunaLogBlock;
import net.yezon.theabyss.block.NightgroveDirtBlock;
import net.yezon.theabyss.block.NightgroveLogBlock;
import net.yezon.theabyss.block.NightgroveMossBlock;
import net.yezon.theabyss.block.RenaLogBlock;
import net.yezon.theabyss.block.ShroomlandMossBlock;
import net.yezon.theabyss.block.SlimedLogBlock;
import net.yezon.theabyss.block.SlimedMossBlock;
import net.yezon.theabyss.block.SnowBlockBlock;
import net.yezon.theabyss.block.StrippedBlaruLogBlock;
import net.yezon.theabyss.block.StrippedFrozenLogBlock;
import net.yezon.theabyss.block.StrippedLunaLogBlock;
import net.yezon.theabyss.block.StrippedNightgroveLogBlock;
import net.yezon.theabyss.block.StrippedRenaLogBlock;
import net.yezon.theabyss.block.StrippedSlimedLogBlock;
import net.yezon.theabyss.block.VigilantLogBlock;
import net.yezon.theabyss.block.VigilantMossBlock;
import net.yezon.theabyss.block.hooks.PlantSoilBlock;
import net.yezon.theabyss.block.lush.LushClayBlock;
import net.yezon.theabyss.neoforge.blocks.NeoForgeBlockItemHooks;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({
    BlackVoidBlock.class, BlaruLogBlock.class, BlaruMossBlock.class, DirtBlock.class, FrozenLogBlock.class, InfectedGrassBlock.class,
    InfectedGravelBlock.class, InfectedPeatBlock.class, LunaLogBlock.class, NightgroveDirtBlock.class, NightgroveLogBlock.class,
    NightgroveMossBlock.class, RenaLogBlock.class, ShroomlandMossBlock.class, SlimedLogBlock.class, SlimedMossBlock.class, SnowBlockBlock.class,
    StrippedBlaruLogBlock.class, StrippedFrozenLogBlock.class, StrippedLunaLogBlock.class, StrippedNightgroveLogBlock.class,
    StrippedRenaLogBlock.class, StrippedSlimedLogBlock.class, VigilantLogBlock.class, VigilantMossBlock.class, LushClayBlock.class
})
public abstract class PlantSoilBlockMixin extends Block {
    private PlantSoilBlockMixin(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        return NeoForgeBlockItemHooks.toNeoForge(((PlantSoilBlock)this).sustainsPlant(state, level, soilPosition, facing, plant));
    }
}
