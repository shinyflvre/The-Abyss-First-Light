package net.yezon.theabyss.jei_recipes;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

public final class WorkstationRecipeSync {
    private WorkstationRecipeSync() {
    }

    public static final class Client {
        private Client() {
        }

        public static <T extends AbstractWorkstationRecipe> List<RecipeHolder<T>> recipes(RecipeType<T> type) {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (connection == null) {
                return List.of();
            }
            return List.copyOf(connection.getRecipeManager().getAllRecipesFor(type));
        }
    }
}
