package net.yezon.theabyss.world.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.block.entity.SomniumInfuserBlockEntity;
import net.yezon.theabyss.events.SomniumEnhancerGuiSoundEvents;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModMenus;
import net.yezon.theabyss.station.StationEnergy;
import org.jspecify.annotations.Nullable;

public class InfuserMenu extends AbstractBlockMenu {
    private final ContainerData data;

    public InfuserMenu(int id, Inventory inv, @Nullable RegistryFriendlyByteBuf extraData) {
        this(id, inv, readPos(extraData));
    }

    public InfuserMenu(int id, Inventory inv, @Nullable BlockPos pos) {
        super(TheabyssModMenus.INFUSER.get(), id, inv, pos, 7);
        this.data = !inv.player.level().isClientSide() && this.boundBlockEntity instanceof SomniumInfuserBlockEntity be ? be.data : new SimpleContainerData(3);
        this.addCustomSlot(new Slot(this.internal, SomniumInfuserBlockEntity.RESULT, 80, 59) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addCustomSlot(new Slot(this.internal, 1, 28, 42));
        this.addCustomSlot(new Slot(this.internal, 2, 54, 50));
        this.addCustomSlot(new Slot(this.internal, 3, 106, 50));
        this.addCustomSlot(new Slot(this.internal, 4, 132, 42));
        this.addCustomSlot(new Slot(this.internal, SomniumInfuserBlockEntity.SOMNIUM, 36, 11) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(TheabyssModItems.SOMNIUM.get());
            }
        });
        this.addCustomSlot(new Slot(this.internal, SomniumInfuserBlockEntity.ENERGY, 80, 16) {
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
        SomniumEnhancerGuiSoundEvents.execute(this.world, this.x, this.y, this.z);
    }

    public float progress() {
        int total = this.data.get(1);
        return total <= 0 ? 0.0F : Mth.clamp(this.data.get(0) / (float)total, 0.0F, 1.0F);
    }

    public boolean brewing() {
        return this.data.get(0) > 0;
    }

    public int state() {
        return this.data.get(2);
    }

    public float energy() {
        return StationEnergy.left(this.slots.get(6).getItem());
    }

    public int usesLeft() {
        return StationEnergy.usesLeft(this.slots.get(6).getItem());
    }
}
