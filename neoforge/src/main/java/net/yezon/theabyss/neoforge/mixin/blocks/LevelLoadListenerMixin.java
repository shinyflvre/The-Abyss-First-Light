package net.yezon.theabyss.neoforge.mixin.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.block.entity.GravityStoneBlockEntity;
import net.yezon.theabyss.block.hooks.LevelLoadListener;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GravityStoneBlockEntity.class)
public abstract class LevelLoadListenerMixin extends BlockEntity {
    private LevelLoadListenerMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        ((LevelLoadListener)this).onLoadedIntoLevel();
    }
}
