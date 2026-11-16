package net.yezon.theabyss.init;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.yezon.theabyss.platform.registries.DeferredBlock;
import net.yezon.theabyss.platform.registries.DeferredItem;
import net.yezon.theabyss.block.flora.AbyssPlantBlock;
import net.yezon.theabyss.block.flora.HangingPlantBodyBlock;
import net.yezon.theabyss.block.flora.HangingPlantHeadBlock;
import net.yezon.theabyss.block.flora.HangingPlantItem;
import net.yezon.theabyss.block.flora.TriplePlantBlock;

public final class TheabyssModFlora {
    public record Hanging(DeferredBlock<Block> head, DeferredBlock<Block> body, DeferredItem<Item> item) {
    }

    public static final List<DeferredItem<Item>> ITEMS = new ArrayList<>();
    public static final List<DeferredItem<Item>> RELEASED = new ArrayList<>();
    private static final java.util.Map<DeferredBlock<Block>, DeferredItem<Item>> ITEM_OF = new java.util.HashMap<>();

    public static final Hanging LUMEN_STRANDS = hanging("lumen_strands", 8, true, true);
    public static final Hanging ASHEN_BRAMBLE_VINES = hanging("ashen_bramble_vines", 0, false, false);
    public static final Hanging SOULBUD_VINE = hanging("soulbud_vine", 10, false, false);
    public static final Hanging VIOLET_WEEPER = hanging("violet_weeper", 0, false, false);
    public static final Hanging COILVINE = hanging("coilvine", 0, false, false);
    public static final Hanging SHADOW_TENDRILS = hanging("shadow_tendrils", 0, false, false);

    public static final DeferredBlock<Block> DUSK_CORAL = single("dusk_coral", SoundType.GRASS, 0);
    public static final DeferredBlock<Block> HOOKLEAF = single("hookleaf", SoundType.GRASS, 0);

    public static final DeferredBlock<Block> TALL_DUSK_CORAL = tall("tall_dusk_coral", SoundType.GRASS, 0);
    public static final DeferredBlock<Block> GLOOM_GROOVE = tall("gloom_groove", SoundType.GRASS, 5);

    public static final DeferredBlock<Block> CROOKSTALK = triple("crookstalk", SoundType.GRASS, 0);
    public static final Hanging WYRMCURL = stalk("wyrmcurl", SoundType.GRASS);
    public static final Hanging TWISTED_BLOOM = stalk("twisted_bloom", SoundType.GRASS);

    static {
        RELEASED.add(SOULBUD_VINE.item());
        RELEASED.add(VIOLET_WEEPER.item());
        RELEASED.add(LUMEN_STRANDS.item());
        RELEASED.add(ITEM_OF.get(HOOKLEAF));
        RELEASED.add(ITEM_OF.get(CROOKSTALK));
        RELEASED.add(ITEM_OF.get(GLOOM_GROOVE));
        RELEASED.add(ITEM_OF.get(DUSK_CORAL));
        RELEASED.add(ITEM_OF.get(TALL_DUSK_CORAL));
        RELEASED.add(WYRMCURL.item());
        RELEASED.add(TWISTED_BLOOM.item());
    }

    private TheabyssModFlora() {
    }

    public static void init() {
    }

    private static BlockBehaviour.Properties plant(BlockBehaviour.Properties p, SoundType sound, int light) {
        p = p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(sound).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.POPPED);
        return light > 0 ? p.lightLevel(state -> light) : p;
    }

    private static BlockBehaviour.Properties vine(BlockBehaviour.Properties p, int light, boolean fullBright) {
        p = p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.WEEPING_VINES).pushReaction(PushReaction.POPPED);
        if (fullBright) {
            p = p.emissiveRendering(state -> true);
        }
        return light > 0 ? p.lightLevel(state -> light) : p;
    }

    private static Hanging hanging(String name, int light, boolean fullBright, boolean standing) {
        @SuppressWarnings("unchecked")
        DeferredBlock<Block>[] ref = new DeferredBlock[4];
        ref[0] = TheabyssModBlocks.REGISTRY.registerBlock(name, p -> new HangingPlantHeadBlock(vine(p, light, fullBright).randomTicks(), () -> ref[1].get()));
        ref[1] = TheabyssModBlocks.REGISTRY.registerBlock(name + "_plant", p -> new HangingPlantBodyBlock(vine(p, light, fullBright), () -> ref[0].get()));
        if (!standing) {
            return new Hanging(ref[0], ref[1], item(ref[0], false));
        }
        ref[2] = TheabyssModBlocks.REGISTRY.registerBlock("standing_" + name, p -> new HangingPlantHeadBlock(vine(p, light, fullBright).randomTicks(),
            () -> ref[3].get(), Direction.UP));
        ref[3] = TheabyssModBlocks.REGISTRY.registerBlock("standing_" + name + "_plant", p -> new HangingPlantBodyBlock(vine(p, light, fullBright),
            () -> ref[2].get(), Direction.UP));
        DeferredItem<Item> item = TheabyssModItems.REGISTRY.registerItem(name, p -> new HangingPlantItem(ref[0].get(), () -> ref[2].get(), p),
            Item.Properties::useBlockDescriptionPrefix);
        ITEMS.add(item);
        ITEM_OF.put(ref[0], item);
        return new Hanging(ref[0], ref[1], item);
    }

    private static Hanging stalk(String name, SoundType sound) {
        @SuppressWarnings("unchecked")
        DeferredBlock<Block>[] ref = new DeferredBlock[2];
        ref[0] = TheabyssModBlocks.REGISTRY.registerBlock(name, p -> new HangingPlantHeadBlock(stalkProperties(p, sound).randomTicks(),
            () -> ref[1].get(), Direction.UP));
        ref[1] = TheabyssModBlocks.REGISTRY.registerBlock(name + "_plant", p -> new HangingPlantBodyBlock(stalkProperties(p, sound),
            () -> ref[0].get(), Direction.UP));
        return new Hanging(ref[0], ref[1], item(ref[0], false));
    }

    private static BlockBehaviour.Properties stalkProperties(BlockBehaviour.Properties p, SoundType sound) {
        return p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(sound).pushReaction(PushReaction.POPPED);
    }

    private static DeferredBlock<Block> single(String name, SoundType sound, int light) {
        DeferredBlock<Block> block = TheabyssModBlocks.REGISTRY.registerBlock(name, p -> new AbyssPlantBlock(plant(p, sound, light)));
        item(block, false);
        return block;
    }

    private static DeferredBlock<Block> tall(String name, SoundType sound, int light) {
        DeferredBlock<Block> block = TheabyssModBlocks.REGISTRY.registerBlock(name, p -> new DoublePlantBlock(plant(p, sound, light)));
        item(block, true);
        return block;
    }

    private static DeferredBlock<Block> triple(String name, SoundType sound, int light) {
        DeferredBlock<Block> block = TheabyssModBlocks.REGISTRY.registerBlock(name, p -> new TriplePlantBlock(plant(p, sound, light)));
        item(block, false);
        return block;
    }

    private static DeferredItem<Item> item(DeferredBlock<Block> block, boolean doubleHigh) {
        DeferredItem<Item> item = TheabyssModItems.REGISTRY.registerItem(block.getId().getPath(),
            p -> doubleHigh ? new DoubleHighBlockItem(block.get(), p) : new BlockItem(block.get(), p), Item.Properties::useBlockDescriptionPrefix);
        ITEMS.add(item);
        ITEM_OF.put(block, item);
        return item;
    }
}
