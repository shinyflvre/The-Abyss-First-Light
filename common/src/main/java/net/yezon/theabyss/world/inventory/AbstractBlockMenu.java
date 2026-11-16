package net.yezon.theabyss.world.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.tick.PlayerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public abstract class AbstractBlockMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
    public final Level world;
    public final Player entity;
    public int x;
    public int y;
    public int z;
    protected final Container internal;
    protected final ContainerLevelAccess access;
    protected final @Nullable BlockEntity boundBlockEntity;
    private final Map<Integer, Slot> customSlots = new HashMap<>();
    private int customCount;

    protected AbstractBlockMenu(MenuType<?> type, int id, Inventory inv, @Nullable BlockPos pos, int containerSize) {
        super(type, id);
        this.entity = inv.player;
        this.world = inv.player.level();
        BlockEntity blockEntity = null;
        Container container = null;
        if (pos != null) {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            this.access = ContainerLevelAccess.create(this.world, pos);
            blockEntity = this.world.getBlockEntity(pos);
            if (blockEntity instanceof Container blockContainer && blockContainer.getContainerSize() >= containerSize) {
                container = blockContainer;
            } else {
                blockEntity = null;
            }
        } else {
            this.access = ContainerLevelAccess.NULL;
        }
        this.boundBlockEntity = blockEntity;
        this.internal = container != null ? container : new SimpleContainer(containerSize);
    }

    protected static @Nullable BlockPos readPos(@Nullable RegistryFriendlyByteBuf extraData) {
        return extraData != null ? extraData.readBlockPos() : null;
    }

    protected Slot addCustomSlot(Slot slot) {
        this.customSlots.put(slot.getContainerSlot(), this.addSlot(slot));
        this.customCount++;
        return slot;
    }

    protected Slot addStationSlot(Slot slot) {
        this.addSlot(slot);
        this.customCount++;
        return slot;
    }

    protected void addPlayerInventory(Inventory inv, int top) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inv, column + (row + 1) * 9, 8 + column * 18, top + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inv, column, 8 + column * 18, top + 58));
        }
    }

    @Override
    public Map<Integer, Slot> get() {
        return this.customSlots;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.boundBlockEntity == null || stillValid(this.access, player, this.boundBlockEntity.getBlockState().getBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        int custom = this.customCount;
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < custom) {
                if (!this.moveItemStackTo(itemstack1, custom, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (!this.moveItemStackTo(itemstack1, 0, custom, false)) {
                if (index < custom + 27) {
                    if (!this.moveItemStackTo(itemstack1, custom + 27, this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, custom, custom + 27, false)) {
                    return ItemStack.EMPTY;
                }

                return ItemStack.EMPTY;
            }

            if (itemstack1.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int i = reverseDirection ? endIndex - 1 : startIndex;
        if (stack.isStackable()) {
            while (!stack.isEmpty() && (reverseDirection ? i >= startIndex : i < endIndex)) {
                Slot slot = this.slots.get(i);
                ItemStack itemstack = slot.getItem();
                if (slot.mayPlace(itemstack) && !itemstack.isEmpty() && ItemStack.isSameItemSameComponents(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());
                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.set(itemstack);
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.set(itemstack);
                        flag = true;
                    }
                }

                i += reverseDirection ? -1 : 1;
            }
        }

        if (!stack.isEmpty()) {
            i = reverseDirection ? endIndex - 1 : startIndex;
            while (reverseDirection ? i >= startIndex : i < endIndex) {
                Slot slot1 = this.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (itemstack1.isEmpty() && slot1.mayPlace(stack)) {
                    slot1.setByPlayer(stack.split(Math.min(stack.getCount(), slot1.getMaxStackSize())));
                    slot1.setChanged();
                    flag = true;
                    break;
                }

                i += reverseDirection ? -1 : 1;
            }
        }

        return flag;
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        if (this.boundBlockEntity == null && playerIn instanceof ServerPlayer) {
            this.clearContainer(playerIn, this.internal);
        }
    }

    protected void tickWhileOpen(Player player) {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.containerMenu instanceof AbstractBlockMenu menu) {
            menu.tickWhileOpen(player);
        }
    }
}
