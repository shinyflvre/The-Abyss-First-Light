package net.yezon.theabyss.world.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.yezon.theabyss.block.entity.CrystalCutterBlockEntity;
import net.yezon.theabyss.init.TheabyssModMenus;
import org.jspecify.annotations.Nullable;

public class CutterMenu extends MachineMenu {
    public CutterMenu(int id, Inventory inv, @Nullable RegistryFriendlyByteBuf extraData) {
        this(id, inv, readPos(extraData));
    }

    public CutterMenu(int id, Inventory inv, @Nullable BlockPos pos) {
        super(TheabyssModMenus.CUTTER.get(), id, inv, pos);
        if (!inv.player.level().isClientSide() && this.boundBlockEntity instanceof CrystalCutterBlockEntity cutter) {
            cutter.learnFrom(inv.player);
        }
    }
}
