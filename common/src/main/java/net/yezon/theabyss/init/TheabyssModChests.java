package net.yezon.theabyss.init;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.yezon.theabyss.platform.registries.DeferredBlock;
import net.yezon.theabyss.platform.registries.DeferredItem;
import net.yezon.theabyss.block.AbyssChestBlock;

public final class TheabyssModChests {
    public static final Map<String, DeferredBlock<Block>> BLOCKS = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<Item>> ITEMS = new LinkedHashMap<>();

    static {
        for (String wood : TheabyssModWoodSets.WOODS) {
            DeferredBlock<Block> block = TheabyssModBlocks.REGISTRY.registerBlock(wood + "_chest", p -> new AbyssChestBlock(wood,
                p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()));
            BLOCKS.put(wood, block);
            ITEMS.put(wood, TheabyssModItems.REGISTRY.registerItem(wood + "_chest", p -> new BlockItem(block.get(), p), Item.Properties::useBlockDescriptionPrefix));
        }
    }

    private TheabyssModChests() {
    }

    public static void init() {
    }

    public static Block[] blocks() {
        return BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new);
    }
}
