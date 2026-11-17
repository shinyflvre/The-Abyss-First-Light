package net.yezon.theabyss.block.entity;

import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.compat.ValueInput;
import net.yezon.theabyss.compat.ValueOutput;
import net.yezon.theabyss.station.StationEnergy;
import org.jspecify.annotations.Nullable;

public abstract class StationMachineBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    public static final int INPUT = 0;
    public static final int OUTPUT = 1;
    public static final int ENERGY = 2;
    public static final int OK = 0;
    public static final int NEEDS_RESEARCH = 1;
    public static final int NO_ENERGY = 2;
    public static final int OUTPUT_FULL = 3;

    private NonNullList<ItemStack> stacks = NonNullList.withSize(3, ItemStack.EMPTY);
    protected int progress;
    protected int state;

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> StationMachineBlockEntity.this.progress;
                case 1 -> StationMachineBlockEntity.this.duration();
                default -> StationMachineBlockEntity.this.state;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> StationMachineBlockEntity.this.progress = value;
                case 2 -> StationMachineBlockEntity.this.state = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    protected StationMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract int duration();

    public abstract boolean isInput(ItemStack stack);

    protected abstract ItemStack result(ServerLevel level, ItemStack input);

    protected ItemStack finish(ServerLevel level, ItemStack result) {
        return result.copy();
    }

    protected void onFinished(ServerLevel level) {
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState blockState, StationMachineBlockEntity be) {
        int before = be.progress;
        int beforeState = be.state;
        be.state = OK;
        be.keepOneEnergy(level, pos);
        ItemStack input = be.stacks.get(INPUT);
        ItemStack result = input.isEmpty() ? ItemStack.EMPTY : be.result(level, input);
        if (result.isEmpty()) {
            be.progress = 0;
        } else if (!StationEnergy.available(be.stacks.get(ENERGY))) {
            be.state = NO_ENERGY;
        } else if (!be.fits(result)) {
            be.state = OUTPUT_FULL;
        } else if (++be.progress >= be.duration()) {
            be.progress = 0;
            StationEnergy.pay(level, be, ENERGY);
            ItemStack made = be.finish(level, result);
            input.shrink(1);
            if (!made.isEmpty()) {
                ItemStack out = be.stacks.get(OUTPUT);
                if (out.isEmpty()) {
                    be.stacks.set(OUTPUT, made);
                } else {
                    out.grow(made.getCount());
                }
            }
            be.onFinished(level);
        }
        if (be.progress != before || be.state != beforeState) {
            be.setChanged();
        }
    }

    private void keepOneEnergy(ServerLevel level, BlockPos pos) {
        ItemStack energy = this.stacks.get(ENERGY);
        if (energy.getCount() > 1) {
            ItemStack extra = energy.split(energy.getCount() - 1);
            net.minecraft.world.Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, extra);
            this.setChanged();
        }
    }

    private boolean fits(ItemStack result) {
        ItemStack out = this.stacks.get(OUTPUT);
        return out.isEmpty() || ItemStack.isSameItemSameComponents(out, result) && out.getCount() + result.getCount() <= out.getMaxStackSize();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ValueInput input = ValueInput.of(tag, registries);
        if (!this.tryLoadLootTable(tag)) {
            this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        }
        ContainerHelper.loadAllItems(tag, this.stacks, registries);
        this.progress = input.getIntOr("progress", 0);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ValueOutput output = ValueOutput.of(tag, registries);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.stacks, registries);
        }
        output.putInt("progress", this.progress);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithFullMetadata(registries);
    }

    @Override
    public int getContainerSize() {
        return this.stacks.size();
    }

    @Override
    public boolean isEmpty() {
        return this.stacks.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return index == INPUT ? this.isInput(stack) : index == ENERGY && StationEnergy.isEnergy(stack) && this.stacks.get(ENERGY).isEmpty();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? new int[] {INPUT} : side == Direction.DOWN ? new int[] {OUTPUT} : new int[] {ENERGY};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == OUTPUT;
    }

    public int[] allSlots() {
        return IntStream.range(0, this.getContainerSize()).toArray();
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
