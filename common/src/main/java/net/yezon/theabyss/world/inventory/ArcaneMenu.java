package net.yezon.theabyss.world.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.init.TheabyssModMenus;
import net.yezon.theabyss.jei_recipes.ArcaneRecipeTypeRecipe;
import net.yezon.theabyss.station.ArcanePower;
import net.yezon.theabyss.station.StationRecipes;
import org.jspecify.annotations.Nullable;

public class ArcaneMenu extends CraftingStationMenu {
    private final DataSlot powered = DataSlot.standalone();

    public ArcaneMenu(int id, Inventory inv, @Nullable RegistryFriendlyByteBuf extraData) {
        this(id, inv, readPos(extraData));
    }

    public ArcaneMenu(int id, Inventory inv, @Nullable BlockPos pos) {
        super(TheabyssModMenus.ARCANE.get(), id, inv, pos, 10, 9);
        int[][] at = {{44, 43}, {8, 43}, {17, 16}, {44, 7}, {71, 16}, {80, 43}, {71, 70}, {44, 79}, {17, 70}};
        for (int i = 0; i < 9; i++) {
            this.addCustomSlot(new Slot(this.internal, i, at[i][0], at[i][1]));
        }
        this.addResultSlot(152, 43);
        this.addPlayerInventory(inv, 101);
        this.powered.set(1);
        this.addDataSlot(this.powered);
    }

    public boolean powered() {
        return this.powered.get() != 0;
    }

    private ItemStack[] grid() {
        ItemStack[] grid = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            grid[i] = this.item(i);
        }
        return grid;
    }

    @Override
    protected ItemStack computeResult(ServerLevel level) {
        boolean power = ArcanePower.powered(level, new BlockPos(this.x, this.y, this.z));
        this.powered.set(power ? 1 : 0);
        if (!power) {
            return ItemStack.EMPTY;
        }
        ArcaneRecipeTypeRecipe recipe = StationRecipes.arcane(level, this.grid());
        return recipe == null ? ItemStack.EMPTY : recipe.getResultItem(level.registryAccess());
    }

    @Override
    protected void consume(Player player) {
        for (int i = 0; i < 9; i++) {
            this.useOne(i);
        }
    }

    @Override
    protected SoundEvent craftSound() {
        return SoundEvents.ENCHANTMENT_TABLE_USE;
    }
}
