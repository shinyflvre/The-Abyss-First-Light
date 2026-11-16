package net.yezon.theabyss.jei_recipes;

import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModRecipeTypes;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class WorkstationRecipeSync {
    private WorkstationRecipeSync() {
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(
            TheabyssModRecipeTypes.MORTAR_RECIPE_TYPE.get(),
            TheabyssModRecipeTypes.INFUSER_RECIPE_TYPE.get(),
            TheabyssModRecipeTypes.ARCANE_RECIPE_TYPE.get(),
            TheabyssModRecipeTypes.CRYSTAL_CUTTER_RECIPE_TYPE.get()
        );
    }

    @EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
    public static final class Client {
        private Client() {
        }

        @SubscribeEvent
        public static void onRecipesReceived(RecipesReceivedEvent event) {
            AbstractWorkstationRecipe.setClientRecipes(event.getRecipeMap());
        }

        @SubscribeEvent
        public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            AbstractWorkstationRecipe.setClientRecipes(RecipeMap.EMPTY);
        }
    }
}
