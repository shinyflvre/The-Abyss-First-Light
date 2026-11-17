package net.yezon.theabyss.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.yezon.theabyss.init.TheabyssModBlocks;

public class NightgroveBushBlock extends AbyssBushBlock {
    public static final MapCodec<NightgroveBushBlock> CODEC = simpleCodec(NightgroveBushBlock::new);

    public NightgroveBushBlock(BlockBehaviour.Properties properties) {
        super(properties.mapColor(MapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundType.GRASS).ignitedByLava()
            .pushReaction(PushReaction.DESTROY));
    }

    @Override
    protected boolean mayPlaceOn(BlockState ground, BlockGetter level, BlockPos pos) {
        return ground.is(TheabyssModBlocks.NIGHTGROVE_MOSS.get()) || ground.is(TheabyssModBlocks.NIGHTGROVE_DIRT.get()) || super.mayPlaceOn(ground, level, pos);
    }

    @Override
    protected MapCodec<NightgroveBushBlock> codec() {
        return CODEC;
    }
}
