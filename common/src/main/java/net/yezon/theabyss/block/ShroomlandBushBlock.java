package net.yezon.theabyss.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class ShroomlandBushBlock extends AbyssBushBlock {
    public static final MapCodec<ShroomlandBushBlock> CODEC = simpleCodec(ShroomlandBushBlock::new);

    public ShroomlandBushBlock(BlockBehaviour.Properties properties) {
        super(properties.mapColor(MapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundType.GRASS).ignitedByLava()
            .pushReaction(PushReaction.DESTROY));
    }

    @Override
    protected MapCodec<ShroomlandBushBlock> codec() {
        return CODEC;
    }
}
