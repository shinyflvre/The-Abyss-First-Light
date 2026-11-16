package net.yezon.theabyss.station;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.init.TheabyssModBlocks;
import net.yezon.theabyss.network.TheabyssModVariables.PlayerVariables;

public final class CrystalResearch {
    public record Crystal(String id, Supplier<Item> ore, Function<PlayerVariables, Boolean> known, BiConsumer<PlayerVariables, Boolean> set) {
        public boolean is(ItemStack stack) {
            return !stack.isEmpty() && stack.is(this.ore.get());
        }
    }

    public static final List<Crystal> ALL = List.of(
        new Crystal("crimson", () -> TheabyssModBlocks.CRIMSON_CRYSTAL_ORE.get().asItem(), v -> v.CrimsonCrystalResearch, (v, b) -> v.CrimsonCrystalResearch = b),
        new Crystal("warped", () -> TheabyssModBlocks.WARPED_CRYSTAL_ORE.get().asItem(), v -> v.WarpedCrystalResearch, (v, b) -> v.WarpedCrystalResearch = b),
        new Crystal("frost", () -> TheabyssModBlocks.FROST_CRYSTAL_ORE.get().asItem(), v -> v.FrostCrystalResearch, (v, b) -> v.FrostCrystalResearch = b),
        new Crystal("abyss", () -> TheabyssModBlocks.ABYSS_CRYSTAL_ORE.get().asItem(), v -> v.AbyssCrystalResearch, (v, b) -> v.AbyssCrystalResearch = b),
        new Crystal("hollow", () -> TheabyssModBlocks.HOLLOW_CRYSTAL_ORE.get().asItem(), v -> v.HollowCrystalResearch, (v, b) -> v.HollowCrystalResearch = b),
        new Crystal("ender", () -> TheabyssModBlocks.ENDER_CRYSTAL_ORE.get().asItem(), v -> v.EnderCrystalResearch, (v, b) -> v.EnderCrystalResearch = b),
        new Crystal("caverna", () -> TheabyssModBlocks.CAVERNA_CRYSTAL_ORE.get().asItem(), v -> v.CavernaCrystalResearch, (v, b) -> v.CavernaCrystalResearch = b),
        new Crystal("aurel", () -> TheabyssModBlocks.AUREL_CRYSTAL_ORE.get().asItem(), v -> v.AurelCrystalResearch, (v, b) -> v.AurelCrystalResearch = b)
    );

    private CrystalResearch() {
    }

    public static Crystal of(ItemStack stack) {
        for (Crystal c : ALL) {
            if (c.is(stack)) {
                return c;
            }
        }
        return null;
    }
}
