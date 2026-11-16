package net.yezon.theabyss.platform.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class DeferredItem<T extends Item> extends DeferredHolder<Item, T> implements ItemLike {
    protected DeferredItem(ResourceKey<Item> key) {
        super(key);
    }

    public static <T extends Item> DeferredItem<T> createItem(Identifier key) {
        return createItem(ResourceKey.create(Registries.ITEM, key));
    }

    public static <T extends Item> DeferredItem<T> createItem(ResourceKey<Item> key) {
        return new DeferredItem<>(key);
    }

    public ItemStack toStack() {
        return this.toStack(1);
    }

    public ItemStack toStack(int count) {
        ItemStack stack = this.get().getDefaultInstance();
        stack.setCount(count);
        return stack;
    }

    @Override
    public Item asItem() {
        return this.get();
    }
}
