package net.yezon.theabyss.world.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.yezon.theabyss.init.TheabyssModMenus;
import org.jspecify.annotations.Nullable;

public class ResearchMenu extends AbstractBlockMenu {
    public ResearchMenu(int id, Inventory inv, @Nullable RegistryFriendlyByteBuf extraData) {
        this(id, inv, readPos(extraData));
    }

    public ResearchMenu(int id, Inventory inv, @Nullable BlockPos pos) {
        super(TheabyssModMenus.RESEARCH.get(), id, inv, pos, 1);
        this.addCustomSlot(new Slot(this.internal, 0, 43, 44));
        this.addPlayerInventory(inv, 84);
    }
}
