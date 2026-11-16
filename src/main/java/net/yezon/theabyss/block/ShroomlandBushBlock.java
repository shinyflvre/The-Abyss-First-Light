package net.yezon.theabyss.block;

import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class ShroomlandBushBlock extends BushBlock {
    public ShroomlandBushBlock(BlockBehaviour.Properties properties) {
        super(properties.mapColor(MapColor.PLANT).replaceable().noCollision().instabreak().sound(SoundType.GRASS).ignitedByLava()
            .pushReaction(PushReaction.POPPED));
    }
}
