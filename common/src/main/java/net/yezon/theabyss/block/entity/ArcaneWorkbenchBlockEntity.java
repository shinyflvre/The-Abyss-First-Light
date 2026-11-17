package net.yezon.theabyss.block.entity;

import io.netty.buffer.Unpooled;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.init.TheabyssModBlockEntities;
import net.yezon.theabyss.world.inventory.ArcaneMenu;
import org.jspecify.annotations.Nullable;

public class ArcaneWorkbenchBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> stacks = NonNullList.withSize(10, ItemStack.EMPTY);

    public ArcaneWorkbenchBlockEntity(BlockPos position, BlockState state) {
        super(TheabyssModBlockEntities.ARCANE_WORKBENCH.get(), position, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!this.tryLoadLootTable(tag)) {
            this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        }

        ContainerHelper.loadAllItems(tag, this.stacks, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.stacks, registries);
        }
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
    protected Component getDefaultName() {
        return Component.translatable("block.theabyss.arcane_workbench");
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        RegistryFriendlyByteBuf extraData = new RegistryFriendlyByteBuf(Unpooled.buffer(), inventory.player.registryAccess());
        extraData.writeBlockPos(this.worldPosition);
        return new ArcaneMenu(id, inventory, extraData);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.theabyss.arcane_workbench");
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
        return index >= 0 && index <= 8;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
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
