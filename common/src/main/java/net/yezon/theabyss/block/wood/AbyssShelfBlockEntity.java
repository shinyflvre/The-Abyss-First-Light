package net.yezon.theabyss.block.wood;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.yezon.theabyss.init.TheabyssModBlockEntities;
import org.jspecify.annotations.Nullable;

public class AbyssShelfBlockEntity extends BlockEntity implements Container {
    public static final int MAX_ITEMS = 3;
    private static final String ALIGN_ITEMS_TO_BOTTOM_TAG = "align_items_to_bottom";
    private final NonNullList<ItemStack> items = NonNullList.withSize(MAX_ITEMS, ItemStack.EMPTY);
    private boolean alignItemsToBottom;

    public AbyssShelfBlockEntity(BlockPos pos, BlockState state) {
        super(TheabyssModBlockEntities.SHELF.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items.clear();
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.alignItemsToBottom = tag.getBoolean(ALIGN_ITEMS_TO_BOTTOM_TAG);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, true, registries);
        tag.putBoolean(ALIGN_ITEMS_TO_BOTTOM_TAG, this.alignItemsToBottom);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, this.items, true, registries);
        tag.putBoolean(ALIGN_ITEMS_TO_BOTTOM_TAG, this.alignItemsToBottom);
        return tag;
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack result = ContainerHelper.removeItem(this.items, slot, count);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.removeItem(this.items, slot, this.getMaxStackSize());
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack itemStack) {
        return this.getItem(slot).isEmpty() || this.getItem(slot).getCount() < this.getMaxStackSize(itemStack);
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        this.setItemNoUpdate(slot, itemStack);
        this.setChanged();
    }

    public void setItemNoUpdate(int slot, ItemStack itemStack) {
        this.items.set(slot, itemStack);
        itemStack.limitSize(this.getMaxStackSize(itemStack));
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    public ItemStack swapItemNoUpdate(int slot, ItemStack heldItemStack) {
        ItemStack retrievedItem = this.removeItemNoUpdate(slot);
        this.setItemNoUpdate(slot, heldItemStack);
        return retrievedItem;
    }

    public void setChanged(@Nullable Holder<GameEvent> event) {
        super.setChanged();
        if (this.level != null) {
            if (event != null) {
                this.level.gameEvent(event, this.worldPosition, GameEvent.Context.of(this.getBlockState()));
            }
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public void setChanged() {
        this.setChanged(GameEvent.BLOCK_ACTIVATE);
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput components) {
        super.applyImplicitComponents(components);
        components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.items);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.items));
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove("Items");
    }

    public float getVisualRotationYInDegrees() {
        return this.getBlockState().getValue(AbyssShelfBlock.FACING).getOpposite().toYRot();
    }

    public boolean getAlignItemsToBottom() {
        return this.alignItemsToBottom;
    }
}
