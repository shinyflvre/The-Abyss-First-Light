package net.yezon.theabyss.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.TheabyssMod;

public final class AbyssPlantSupport {
    public static final TagKey<Block> SUPPORTS_VEGETATION = TagKey.create(Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "supports_vegetation"));

    private AbyssPlantSupport() {
    }

    public static boolean supportsVegetation(BlockState state) {
        return state.is(SUPPORTS_VEGETATION);
    }
}
