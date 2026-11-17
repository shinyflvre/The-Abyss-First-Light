package net.yezon.theabyss.block.wood;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AbyssBoat extends Boat {
    private final Supplier<Item> dropItem;

    public AbyssBoat(EntityType<? extends Boat> type, Level level, Supplier<Item> dropItem) {
        super(type, level);
        this.dropItem = dropItem;
    }

    @Override
    public Item getDropItem() {
        return this.dropItem.get();
    }

    @Override
    protected Component getTypeName() {
        return this.getType().getDescription();
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        if (onGround && !this.isPassenger()) {
            this.resetFallDistance();
        }
        super.checkFallDamage(y, onGround, state, pos);
    }
}
