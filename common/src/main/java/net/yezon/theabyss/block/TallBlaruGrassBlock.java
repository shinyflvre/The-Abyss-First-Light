package net.yezon.theabyss.block;

import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class TallBlaruGrassBlock extends DoublePlantBlock {
    public TallBlaruGrassBlock(BlockBehaviour.Properties properties) {
        super(
            properties
                .mapColor(MapColor.PLANT)
                .sound(SoundType.MOSS_CARPET)
                .instabreak()
                .noCollission()
                .replaceable()
                .offsetType(OffsetType.XYZ)
                .pushReaction(PushReaction.DESTROY)
        );
    }
}
