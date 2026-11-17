package net.yezon.theabyss.block;

import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class TallInfectedGrassBlock extends DoublePlantBlock {
    public TallInfectedGrassBlock(BlockBehaviour.Properties properties) {
        super(
            properties
                .mapColor(MapColor.PLANT)
                .sound(SoundType.GRASS)
                .instabreak()
                .noCollission()
                .replaceable()
                .offsetType(OffsetType.XZ)
                .pushReaction(PushReaction.DESTROY)
        );
    }
}
