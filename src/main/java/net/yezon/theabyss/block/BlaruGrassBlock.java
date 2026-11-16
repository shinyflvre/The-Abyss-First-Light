package net.yezon.theabyss.block;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class BlaruGrassBlock extends FlowerBlock {
    public BlaruGrassBlock(BlockBehaviour.Properties properties) {
        super(
            MobEffects.SATURATION,
            0.0F,
            properties
                .mapColor(MapColor.PLANT)
                .sound(SoundType.MOSS_CARPET)
                .instabreak()
                .noCollision()
                .replaceable()
                .offsetType(OffsetType.XYZ)
                .pushReaction(PushReaction.POPPED)
        );
    }
}
