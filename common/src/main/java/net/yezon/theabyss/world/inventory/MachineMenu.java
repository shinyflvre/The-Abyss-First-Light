package net.yezon.theabyss.world.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.block.entity.StationMachineBlockEntity;
import net.yezon.theabyss.station.StationEnergy;
import org.jspecify.annotations.Nullable;

public abstract class MachineMenu extends AbstractBlockMenu {
    private final ContainerData data;

    protected MachineMenu(MenuType<?> type, int id, Inventory inv, @Nullable BlockPos pos) {
        super(type, id, inv, pos, 3);
        this.data = !inv.player.level().isClientSide() && this.boundBlockEntity instanceof StationMachineBlockEntity be ? be.data : new SimpleContainerData(3);
        StationMachineBlockEntity machine = this.boundBlockEntity instanceof StationMachineBlockEntity be ? be : null;
        this.addCustomSlot(new Slot(this.internal, StationMachineBlockEntity.INPUT, 26, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return machine == null || machine.isInput(stack);
            }
        });
        this.addCustomSlot(new Slot(this.internal, StationMachineBlockEntity.OUTPUT, 134, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addCustomSlot(new Slot(this.internal, StationMachineBlockEntity.ENERGY, 80, 26) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return StationEnergy.isEnergy(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addPlayerInventory(inv, 84);
        this.addDataSlots(this.data);
    }

    public float progress() {
        int total = this.data.get(1);
        return total <= 0 ? 0.0F : Mth.clamp(this.data.get(0) / (float)total, 0.0F, 1.0F);
    }

    public int state() {
        return this.data.get(2);
    }

    public float energy() {
        return StationEnergy.left(this.slots.get(2).getItem());
    }

    public int usesLeft() {
        return StationEnergy.usesLeft(this.slots.get(2).getItem());
    }
}
