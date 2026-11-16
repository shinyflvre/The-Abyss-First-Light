package net.yezon.theabyss.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.init.TheabyssModBlockEntities;
import net.yezon.theabyss.init.TheabyssModMenus;

public class AbyssChestBlockEntity extends ChestBlockEntity {
    public AbyssChestBlockEntity(BlockPos pos, BlockState state) {
        super(TheabyssModBlockEntities.ABYSS_CHEST.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return this.getBlockState().getBlock().getName();
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new ChestMenu(TheabyssModMenus.ABYSS_CHEST.get(), containerId, inventory, this, 3);
    }
}
