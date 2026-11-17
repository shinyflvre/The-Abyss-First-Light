package net.yezon.theabyss.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.yezon.theabyss.block.AbyssChestBlock;
import net.yezon.theabyss.init.TheabyssModBlockEntities;
import net.yezon.theabyss.init.TheabyssModMenus;
import org.jspecify.annotations.Nullable;

public class AbyssChestBlockEntity extends ChestBlockEntity {
    private final ContainerOpenersCounter abyssOpeners = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            if (state.getBlock() instanceof AbyssChestBlock chest) {
                AbyssChestBlockEntity.playChestSound(level, pos, state, chest.getOpenChestSound());
            }
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            if (state.getBlock() instanceof AbyssChestBlock chest) {
                AbyssChestBlockEntity.playChestSound(level, pos, state, chest.getCloseChestSound());
            }
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int previous, int current) {
            AbyssChestBlockEntity.this.signalOpenCount(level, pos, state, previous, current);
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            if (!(player.containerMenu instanceof ChestMenu menu)) {
                return false;
            }
            Container container = menu.getContainer();
            return container == AbyssChestBlockEntity.this
                || container instanceof CompoundContainer compound && compound.contains(AbyssChestBlockEntity.this);
        }
    };

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

    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.abyssOpeners.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.abyssOpeners.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void recheckOpen() {
        if (!this.remove) {
            this.abyssOpeners.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    private static void playChestSound(Level level, BlockPos pos, BlockState state, SoundEvent event) {
        ChestType type = state.getValue(ChestBlock.TYPE);
        if (type != ChestType.LEFT) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;
            if (type == ChestType.RIGHT) {
                Direction direction = ChestBlock.getConnectedDirection(state);
                x += direction.getStepX() * 0.5;
                z += direction.getStepZ() * 0.5;
            }
            level.playSound(null, x, y, z, event, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
        }
    }

    @Override
    public boolean canOpen(Player player) {
        return AbyssContainerLock.canOpen(this, player);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return AbyssContainerLock.createMenu(this, containerId, inventory, player, this::createMenu);
    }
}
