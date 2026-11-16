package net.yezon.theabyss.world.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.init.TheabyssModMenus;
import net.yezon.theabyss.jei_recipes.MortarRecipeTypeRecipe;
import net.yezon.theabyss.station.StationRecipes;
import org.jspecify.annotations.Nullable;

public class MortarMenu extends CraftingStationMenu {
    private static final int MIDDLE = 5;
    private static final int[] OUTER = {1, 2, 3, 4};

    public MortarMenu(int id, Inventory inv, @Nullable RegistryFriendlyByteBuf extraData) {
        this(id, inv, readPos(extraData));
    }

    public MortarMenu(int id, Inventory inv, @Nullable BlockPos pos) {
        super(TheabyssModMenus.MORTAR.get(), id, inv, pos, 6, 0);
        this.addResultSlot(134, 43);
        this.addCustomSlot(new Slot(this.internal, 1, 26, 43));
        this.addCustomSlot(new Slot(this.internal, 2, 44, 25));
        this.addCustomSlot(new Slot(this.internal, 3, 62, 43));
        this.addCustomSlot(new Slot(this.internal, 4, 44, 61));
        this.addCustomSlot(new Slot(this.internal, MIDDLE, 44, 43));
        this.addPlayerInventory(inv, 101);
    }

    @Override
    protected ItemStack computeResult(ServerLevel level) {
        ItemStack[] outer = new ItemStack[OUTER.length];
        for (int i = 0; i < OUTER.length; i++) {
            outer[i] = this.item(OUTER[i]);
        }
        MortarRecipeTypeRecipe recipe = StationRecipes.mortar(level, outer, this.item(MIDDLE));
        return recipe == null ? ItemStack.EMPTY : recipe.getResultItem(level.registryAccess());
    }

    @Override
    protected void consume(Player player) {
        for (int i : OUTER) {
            this.useOne(i);
        }
        this.useOne(MIDDLE);
    }

    @Override
    protected SoundEvent craftSound() {
        return SoundEvents.GRINDSTONE_USE;
    }
}
