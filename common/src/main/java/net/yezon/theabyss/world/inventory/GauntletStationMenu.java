package net.yezon.theabyss.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.init.TheabyssModBlocks;
import net.yezon.theabyss.init.TheabyssModMenus;
import net.yezon.theabyss.ring.Gauntlet;
import org.jspecify.annotations.Nullable;

public class GauntletStationMenu extends AbstractContainerMenu {
    public static final int[] GAUNTLET_SLOT = {80, 52};
    public static final int[][] RING_SLOTS = {{44, 64}, {52, 30}, {80, 19}, {108, 30}, {116, 64}};
    public static final int INVENTORY_TOP = 101;
    public static final int WIDTH = 176;
    public static final int HEIGHT = 200;
    private static final int FIRST_RING = 1;
    private static final int FIRST_INVENTORY = FIRST_RING + Gauntlet.SLOTS;

    private final ContainerLevelAccess access;
    private boolean loading;
    private final SimpleContainer gauntlet = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            GauntletStationMenu.this.load();
        }
    };
    private final SimpleContainer rings = new SimpleContainer(Gauntlet.SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            GauntletStationMenu.this.save();
        }
    };

    public GauntletStationMenu(int id, Inventory inv, @Nullable RegistryFriendlyByteBuf extraData) {
        this(id, inv, ContainerLevelAccess.NULL);
    }

    public GauntletStationMenu(int id, Inventory inv, ContainerLevelAccess access) {
        super(TheabyssModMenus.GAUNTLET_STATION.get(), id);
        this.access = access;
        this.addSlot(new Slot(this.gauntlet, 0, GAUNTLET_SLOT[0], GAUNTLET_SLOT[1]) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return Gauntlet.is(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int i = 0; i < Gauntlet.SLOTS; i++) {
            this.addSlot(new Slot(this.rings, i, RING_SLOTS[i][0], RING_SLOTS[i][1]) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return Gauntlet.isRing(stack) && Gauntlet.is(GauntletStationMenu.this.gauntlet.getItem(0));
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inv, column + (row + 1) * 9, 8 + column * 18, INVENTORY_TOP + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inv, column, 8 + column * 18, INVENTORY_TOP + 58));
        }
    }

    private void load() {
        this.loading = true;
        ItemStack held = this.gauntlet.getItem(0);
        NonNullList<ItemStack> stored = Gauntlet.is(held) ? Gauntlet.rings(held) : NonNullList.withSize(Gauntlet.SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < Gauntlet.SLOTS; i++) {
            this.rings.setItem(i, stored.get(i).copy());
        }
        this.loading = false;
    }

    private void save() {
        if (this.loading) {
            return;
        }
        ItemStack held = this.gauntlet.getItem(0);
        if (!Gauntlet.is(held)) {
            return;
        }
        NonNullList<ItemStack> out = NonNullList.withSize(Gauntlet.SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < Gauntlet.SLOTS; i++) {
            out.set(i, this.rings.getItem(i).copy());
        }
        Gauntlet.setRings(held, out);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, TheabyssModBlocks.GAUNTLET_STATION.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.gauntlet));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < FIRST_INVENTORY) {
            if (!this.moveItemStackTo(stack, FIRST_INVENTORY, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (Gauntlet.is(stack)) {
            if (!this.moveItemStackTo(stack, 0, FIRST_RING, false)) {
                return ItemStack.EMPTY;
            }
        } else if (Gauntlet.isRing(stack) && Gauntlet.is(this.gauntlet.getItem(0))) {
            if (!this.moveItemStackTo(stack, FIRST_RING, FIRST_INVENTORY, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < FIRST_INVENTORY + 27) {
            if (!this.moveItemStackTo(stack, FIRST_INVENTORY + 27, this.slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, FIRST_INVENTORY, FIRST_INVENTORY + 27, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }
}
