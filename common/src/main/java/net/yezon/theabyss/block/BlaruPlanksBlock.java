package net.yezon.theabyss.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yezon.theabyss.platform.common.util.DeferredSoundType;

public class BlaruPlanksBlock extends Block {
    public BlaruPlanksBlock(BlockBehaviour.Properties properties) {
        super(
            properties
                .ignitedByLava()
                .instrument(NoteBlockInstrument.BASS)
                .sound(
                    new DeferredSoundType(
                        1.0F,
                        1.0F,
                        () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:wood_place")),
                        () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:wood_step")),
                        () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("theabyss:wood_place")),
                        () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.wood.hit")),
                        () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.wood.fall"))
                    )
                )
                .strength(2.0F, 10.0F)
        );
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this, 1));
    }
}
