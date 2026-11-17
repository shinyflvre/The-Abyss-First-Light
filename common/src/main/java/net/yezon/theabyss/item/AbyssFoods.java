package net.yezon.theabyss.item;

import net.minecraft.world.food.FoodProperties;

public final class AbyssFoods {
    private AbyssFoods() {
    }

    public static FoodProperties consumeSeconds(FoodProperties food, float consumeSeconds) {
        return new FoodProperties(food.nutrition(), food.saturation(), food.canAlwaysEat(), consumeSeconds, food.usingConvertsTo(), food.effects());
    }
}
