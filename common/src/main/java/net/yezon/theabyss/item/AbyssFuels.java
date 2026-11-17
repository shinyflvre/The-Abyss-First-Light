package net.yezon.theabyss.item;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.yezon.theabyss.TheabyssMod;
import org.jspecify.annotations.Nullable;

public final class AbyssFuels {
    public static final int DRY_PLANTS = 100;
    public static final int WOOD_ITEMS_LARGE = 200;
    public static final int WOOD_ITEMS_SMALL = 300;
    public static final int WOOD_BLOCKS = 300;
    public static final int HANGING_SIGNS = 800;
    public static final int BOATS = 1200;

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static volatile @Nullable Map<Item, Integer> byItem;

    private AbyssFuels() {
    }

    public static synchronized <T extends Supplier<? extends ItemLike>> T fuel(T item, int burnTicks) {
        ENTRIES.add(new Entry(item, burnTicks));
        byItem = null;
        return item;
    }

    public static synchronized void forEach(BiConsumer<Item, Integer> consumer) {
        for (Entry entry : ENTRIES) {
            consumer.accept(entry.item().get().asItem(), entry.burnTicks());
        }
    }

    public static boolean isModItem(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(TheabyssMod.MODID);
    }

    public static int burnTime(ItemStack stack) {
        Map<Item, Integer> map = byItem;
        if (map == null) {
            map = new IdentityHashMap<>();
            Map<Item, Integer> target = map;
            forEach(target::put);
            byItem = map;
        }
        Integer ticks = map.get(stack.getItem());
        return ticks == null ? -1 : ticks;
    }

    private record Entry(Supplier<? extends ItemLike> item, int burnTicks) {
    }
}
