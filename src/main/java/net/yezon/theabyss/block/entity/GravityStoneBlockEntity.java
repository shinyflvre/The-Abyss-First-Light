package net.yezon.theabyss.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.gravity.GravityIndex;
import net.yezon.theabyss.init.TheabyssModBlockEntities;

public class GravityStoneBlockEntity extends BlockEntity {
    private boolean indexed;

    public GravityStoneBlockEntity(BlockPos pos, BlockState state) {
        super(TheabyssModBlockEntities.GRAVITY_STONE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && !this.indexed) {
            GravityIndex.add(this.level, this.worldPosition);
            this.indexed = true;
        }
    }

    @Override
    public void setRemoved() {
        if (this.level != null && this.indexed) {
            GravityIndex.remove(this.level, this.worldPosition);
            this.indexed = false;
        }
        super.setRemoved();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (this.level != null && !this.indexed) {
            GravityIndex.add(this.level, this.worldPosition);
            this.indexed = true;
        }
    }
}
