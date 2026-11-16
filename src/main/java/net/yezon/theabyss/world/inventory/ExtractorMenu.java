package net.yezon.theabyss.world.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.yezon.theabyss.init.TheabyssModMenus;
import org.jspecify.annotations.Nullable;

public class ExtractorMenu extends MachineMenu {
    public ExtractorMenu(int id, Inventory inv, @Nullable RegistryFriendlyByteBuf extraData) {
        this(id, inv, readPos(extraData));
    }

    public ExtractorMenu(int id, Inventory inv, @Nullable BlockPos pos) {
        super(TheabyssModMenus.EXTRACTOR.get(), id, inv, pos);
    }
}
