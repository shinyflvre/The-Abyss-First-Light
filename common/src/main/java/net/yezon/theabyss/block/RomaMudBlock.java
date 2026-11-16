package net.yezon.theabyss.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yezon.theabyss.platform.common.util.DeferredSoundType;

public class RomaMudBlock extends Block {
    public RomaMudBlock(BlockBehaviour.Properties properties) {
        super(
            properties
                .sound(
                    new DeferredSoundType(
                        1.0F,
                        1.0F,
                        () -> BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:mud_step")),
                        () -> BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:mud_step")),
                        () -> BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:mud_step")),
                        () -> BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:mud_step")),
                        () -> BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:mud_step"))
                    )
                )
                .strength(1.0F, 10.0F)
                .speedFactor(0.6F)
        );
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this, 1));
    }
}
