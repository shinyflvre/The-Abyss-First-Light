package net.yezon.theabyss.init;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.block.wood.AbyssBoat;
import net.yezon.theabyss.block.wood.AbyssBoatItem;
import net.yezon.theabyss.block.wood.AbyssChestBoat;
import net.yezon.theabyss.block.wood.AbyssShelfBlock;
import net.yezon.theabyss.item.AbyssFuels;
import net.yezon.theabyss.mixin.accessor.WoodTypeInvoker;
import net.yezon.theabyss.platform.event.BlockEntityTypeAddBlocksEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.registries.DeferredBlock;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredItem;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class TheabyssModWoodSets {
    public static final String[] WOODS = {"blaru", "nightgrove", "bog", "sal", "slimed", "frozen", "rena", "luna", "vigilant"};

    public record WoodSet(String name, WoodType woodType, DeferredBlock<Block> sign, DeferredBlock<Block> wallSign, DeferredBlock<Block> hangingSign,
                          DeferredBlock<Block> wallHangingSign, DeferredBlock<Block> shelf, DeferredItem<Item> signItem, DeferredItem<Item> hangingSignItem,
                          DeferredItem<Item> shelfItem, DeferredItem<Item> boatItem, DeferredItem<Item> chestBoatItem,
                          DeferredHolder<EntityType<?>, EntityType<AbyssBoat>> boat, DeferredHolder<EntityType<?>, EntityType<AbyssChestBoat>> chestBoat) {
    }

    public static final List<WoodSet> ALL = new ArrayList<>();

    static {
        for (String wood : WOODS) {
            ALL.add(create(wood));
        }
    }

    private TheabyssModWoodSets() {
    }

    public static void init() {
    }

    public static Block[] shelves() {
        return ALL.stream().map(set -> set.shelf().get()).toArray(Block[]::new);
    }

    private static WoodSet create(String wood) {
        WoodType type = WoodTypeInvoker.theabyss$register(new WoodType(TheabyssMod.MODID + ":" + wood, BlockSetType.OAK));
        var blocks = TheabyssModBlocks.REGISTRY;
        var items = TheabyssModItems.REGISTRY;

        DeferredBlock<Block> sign = blocks.registerBlock(wood + "_sign", p -> new StandingSignBlock(type,
            p.mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
        DeferredBlock<Block> wallSign = blocks.registerBlock(wood + "_wall_sign", p -> new WallSignBlock(type,
            p.dropsLike(sign.get()).mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
        DeferredBlock<Block> hangingSign = blocks.registerBlock(wood + "_hanging_sign", p -> new CeilingHangingSignBlock(type,
            p.mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
        DeferredBlock<Block> wallHangingSign = blocks.registerBlock(wood + "_wall_hanging_sign", p -> new WallHangingSignBlock(type,
            p.dropsLike(hangingSign.get()).mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
        DeferredBlock<Block> shelf = blocks.registerBlock(wood + "_shelf", p -> new AbyssShelfBlock(
            p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).sound(SoundType.CHISELED_BOOKSHELF).ignitedByLava().strength(2.0F, 3.0F)));

        DeferredItem<Item> signItem = AbyssFuels.fuel(items.registerItem(wood + "_sign",
            p -> new SignItem(p, sign.get(), wallSign.get()), p -> p.stacksTo(16)), AbyssFuels.WOOD_ITEMS_LARGE);
        DeferredItem<Item> hangingSignItem = AbyssFuels.fuel(items.registerItem(wood + "_hanging_sign",
            p -> new HangingSignItem(hangingSign.get(), wallHangingSign.get(), p), p -> p.stacksTo(16)), AbyssFuels.HANGING_SIGNS);
        DeferredItem<Item> shelfItem = AbyssFuels.fuel(items.registerItem(wood + "_shelf",
            p -> new BlockItem(shelf.get(), p), p -> p.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)), AbyssFuels.WOOD_BLOCKS);

        @SuppressWarnings("unchecked")
        DeferredItem<Item>[] boatItemRef = new DeferredItem[2];
        DeferredHolder<EntityType<?>, EntityType<AbyssBoat>> boat = TheabyssModEntities.REGISTRY.registerEntityType(wood + "_boat",
            (t, level) -> new AbyssBoat(t, level, () -> boatItemRef[0].get()), MobCategory.MISC,
            b -> b.sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10));
        DeferredHolder<EntityType<?>, EntityType<AbyssChestBoat>> chestBoat = TheabyssModEntities.REGISTRY.registerEntityType(wood + "_chest_boat",
            (t, level) -> new AbyssChestBoat(t, level, () -> boatItemRef[1].get()), MobCategory.MISC,
            b -> b.sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10));
        DeferredItem<Item> boatItem = AbyssFuels.fuel(items.registerItem(wood + "_boat", p -> new AbyssBoatItem(boat, p), p -> p.stacksTo(1)),
            AbyssFuels.BOATS);
        DeferredItem<Item> chestBoatItem = AbyssFuels.fuel(items.registerItem(wood + "_chest_boat", p -> new AbyssBoatItem(chestBoat, p), p -> p.stacksTo(1)),
            AbyssFuels.BOATS);
        boatItemRef[0] = boatItem;
        boatItemRef[1] = chestBoatItem;

        return new WoodSet(wood, type, sign, wallSign, hangingSign, wallHangingSign, shelf, signItem, hangingSignItem, shelfItem, boatItem, chestBoatItem,
            boat, chestBoat);
    }

    @SubscribeEvent
    public static void onBlockEntityBlocks(BlockEntityTypeAddBlocksEvent event) {
        for (WoodSet set : ALL) {
            event.modify(BlockEntityType.SIGN, set.sign().get(), set.wallSign().get());
            event.modify(BlockEntityType.HANGING_SIGN, set.hangingSign().get(), set.wallHangingSign().get());
        }
    }
}
