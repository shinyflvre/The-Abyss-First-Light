package net.yezon.theabyss.block;

import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

public class SlimedPressurePlateBlock extends PressurePlateBlock {
    public SlimedPressurePlateBlock(BlockBehaviour.Properties properties) {
        super(
            TheabyssBlockSetTypes.IRON_MOBS,
            properties.ignitedByLava().sound(SoundType.WOOD).strength(1.0F, 10.0F).dynamicShape().forceSolidOn()
        );
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this, 1));
    }
}
