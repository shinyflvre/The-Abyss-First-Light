package net.yezon.theabyss.block.flora;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class HangingPlantItem extends BlockItem {
    private final Supplier<? extends Block> standing;

    public HangingPlantItem(Block hanging, Supplier<? extends Block> standing, Item.Properties properties) {
        super(hanging, properties);
        this.standing = standing;
    }

    @Override
    protected @Nullable BlockState getPlacementState(BlockPlaceContext context) {
        Block first = context.getClickedFace() == Direction.UP ? this.standing.get() : this.getBlock();
        Block second = first == this.getBlock() ? this.standing.get() : this.getBlock();
        for (Block block : List.of(first, second)) {
            BlockState state = block.getStateForPlacement(context);
            if (state != null && this.canPlace(context, state)) {
                return state;
            }
        }
        return null;
    }

    @Override
    public void registerBlocks(Map<Block, Item> map, Item item) {
        super.registerBlocks(map, item);
        map.put(this.standing.get(), item);
    }
}
