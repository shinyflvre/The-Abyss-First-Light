package net.yezon.theabyss.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;

public class VigilantGrassBlock extends FlowerBlock {
    public VigilantGrassBlock(BlockBehaviour.Properties properties) {
        super(
            MobEffects.SATURATION,
            0.0F,
            properties
                .mapColor(MapColor.PLANT)
                .sound(SoundType.MOSS_CARPET)
                .strength(-1.0F, 3600000.0F)
                .hasPostProcess((bs, br, bp) -> true)
                .emissiveRendering((bs, br, bp) -> true)
                .lightLevel(s -> 4)
                .noCollission()
                .replaceable()
                .offsetType(OffsetType.XZ)
                .pushReaction(PushReaction.DESTROY)
        );
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this));
    }
}
