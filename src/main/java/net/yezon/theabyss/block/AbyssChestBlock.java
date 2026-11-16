package net.yezon.theabyss.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.block.entity.AbyssChestBlockEntity;
import net.yezon.theabyss.init.TheabyssModBlockEntities;
import net.yezon.theabyss.init.TheabyssModMenus;
import net.yezon.theabyss.init.TheabyssModSounds;
import org.jspecify.annotations.Nullable;

public class AbyssChestBlock extends ChestBlock {
    private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>> MENUS = new DoubleBlockCombiner.Combiner<>() {
        @Override
        public Optional<MenuProvider> acceptDouble(ChestBlockEntity first, ChestBlockEntity second) {
            Container container = new CompoundContainer(first, second);
            return Optional.of(new MenuProvider() {
                @Override
                public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                    if (first.canOpen(player) && second.canOpen(player)) {
                        first.unpackLootTable(inventory.player);
                        second.unpackLootTable(inventory.player);
                        return new ChestMenu(TheabyssModMenus.ABYSS_CHEST_DOUBLE.get(), containerId, inventory, container, 6);
                    }
                    if (!player.isSpectator()) {
                        Direction connected = ChestBlock.getConnectedDirection(first.getBlockState());
                        Vec3 centre = Vec3.atCenterOf(first.getBlockPos()).add(connected.getStepX() / 2.0, 0.0, connected.getStepZ() / 2.0);
                        BaseContainerBlockEntity.sendChestLockedNotifications(centre, player, this.getDisplayName());
                    }
                    return null;
                }

                @Override
                public Component getDisplayName() {
                    if (first.hasCustomName()) {
                        return first.getDisplayName();
                    }
                    if (second.hasCustomName()) {
                        return second.getDisplayName();
                    }
                    return Component.translatable("container.theabyss.chest_double", first.getBlockState().getBlock().getName());
                }
            });
        }

        @Override
        public Optional<MenuProvider> acceptSingle(ChestBlockEntity single) {
            return Optional.of(single);
        }

        @Override
        public Optional<MenuProvider> acceptNone() {
            return Optional.empty();
        }
    };

    private final String wood;

    public AbyssChestBlock(String wood, BlockBehaviour.Properties properties) {
        super(() -> TheabyssModBlockEntities.ABYSS_CHEST.get(), SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE, properties);
        this.wood = wood;
    }

    public String wood() {
        return this.wood;
    }

    @Override
    public SoundEvent getOpenChestSound() {
        return TheabyssModSounds.ABYSS_CHEST_OPEN.get();
    }

    @Override
    public SoundEvent getCloseChestSound() {
        return TheabyssModSounds.ABYSS_CHEST_CLOSE.get();
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return this.combine(state, level, pos, false).apply(MENUS).orElse(null);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AbyssChestBlockEntity(pos, state);
    }
}
