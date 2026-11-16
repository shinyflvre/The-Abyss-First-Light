package net.yezon.theabyss.init;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class TheabyssModWoodSets {
    public static final String[] WOODS = {"blaru", "nightgrove", "bog", "sal", "slimed", "frozen", "rena", "luna", "vigilant"};

    public record WoodSet(String name, WoodType woodType, DeferredBlock<Block> sign, DeferredBlock<Block> wallSign, DeferredBlock<Block> hangingSign,
                          DeferredBlock<Block> wallHangingSign, DeferredBlock<Block> shelf, DeferredItem<Item> signItem, DeferredItem<Item> hangingSignItem,
                          DeferredItem<Item> shelfItem, DeferredItem<Item> boatItem, DeferredItem<Item> chestBoatItem,
                          DeferredHolder<EntityType<?>, EntityType<Boat>> boat, DeferredHolder<EntityType<?>, EntityType<ChestBoat>> chestBoat) {
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

    private static WoodSet create(String wood) {
        WoodType type = WoodType.register(new WoodType(TheabyssMod.MODID + ":" + wood, BlockSetType.OAK));
        var blocks = TheabyssModBlocks.REGISTRY;
        var items = TheabyssModItems.REGISTRY;

        DeferredBlock<Block> sign = blocks.registerBlock(wood + "_sign", p -> new StandingSignBlock(type,
            p.mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0F).ignitedByLava()));
        DeferredBlock<Block> wallSign = blocks.registerBlock(wood + "_wall_sign", p -> new WallSignBlock(type,
            p.overrideLootTable(sign.get().getLootTable()).overrideDescription(sign.get().getDescriptionId())
                .mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0F).ignitedByLava()));
        DeferredBlock<Block> hangingSign = blocks.registerBlock(wood + "_hanging_sign", p -> new CeilingHangingSignBlock(type,
            p.mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0F).ignitedByLava()));
        DeferredBlock<Block> wallHangingSign = blocks.registerBlock(wood + "_wall_hanging_sign", p -> new WallHangingSignBlock(type,
            p.overrideLootTable(hangingSign.get().getLootTable()).overrideDescription(hangingSign.get().getDescriptionId())
                .mapColor(MapColor.WOOD).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0F).ignitedByLava()));
        DeferredBlock<Block> shelf = blocks.registerBlock(wood + "_shelf", p -> new ShelfBlock(
            p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).sound(SoundType.SHELF).ignitedByLava().strength(2.0F, 3.0F)));

        DeferredItem<Item> signItem = items.registerItem(wood + "_sign",
            p -> new StandingAndWallBlockItem(sign.get(), wallSign.get(), Direction.DOWN, p), p -> p.useBlockDescriptionPrefix().stacksTo(16).signText().cookingFuel(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE));
        DeferredItem<Item> hangingSignItem = items.registerItem(wood + "_hanging_sign",
            p -> new HangingSignItem(hangingSign.get(), wallHangingSign.get(), p), p -> p.useBlockDescriptionPrefix().stacksTo(16).signText().cookingFuel(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COOKING_TIME_HANGING_SIGNS));
        DeferredItem<Item> shelfItem = items.registerItem(wood + "_shelf",
            p -> new BlockItem(shelf.get(), p), p -> p.useBlockDescriptionPrefix().component(DataComponents.CONTAINER, ItemContainerContents.EMPTY).cookingFuel(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COOKING_TIME_WOOD_BLOCKS));

        DeferredItem<Item>[] boatItemRef = new DeferredItem[2];
        DeferredHolder<EntityType<?>, EntityType<Boat>> boat = TheabyssModEntities.REGISTRY.registerEntityType(wood + "_boat",
            (t, level) -> new Boat(t, level, () -> boatItemRef[0].get()), MobCategory.MISC,
            b -> b.noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10));
        DeferredHolder<EntityType<?>, EntityType<ChestBoat>> chestBoat = TheabyssModEntities.REGISTRY.registerEntityType(wood + "_chest_boat",
            (t, level) -> new ChestBoat(t, level, () -> boatItemRef[1].get()), MobCategory.MISC,
            b -> b.noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10));
        DeferredItem<Item> boatItem = items.registerItem(wood + "_boat", p -> new BoatItem(boat.get(), p), p -> p.stacksTo(1).cookingFuel(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COOKING_TIME_BOATS));
        DeferredItem<Item> chestBoatItem = items.registerItem(wood + "_chest_boat", p -> new BoatItem(chestBoat.get(), p), p -> p.stacksTo(1).cookingFuel(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COOKING_TIME_BOATS));
        boatItemRef[0] = boatItem;
        boatItemRef[1] = chestBoatItem;

        return new WoodSet(wood, type, sign, wallSign, hangingSign, wallHangingSign, shelf, signItem, hangingSignItem, shelfItem, boatItem, chestBoatItem,
            boat, chestBoat);
    }

    @SubscribeEvent
    public static void onBlockEntityBlocks(BlockEntityTypeAddBlocksEvent event) {
        for (WoodSet set : ALL) {
            event.modify(BlockEntityTypes.SIGN, set.sign().get(), set.wallSign().get());
            event.modify(BlockEntityTypes.HANGING_SIGN, set.hangingSign().get(), set.wallHangingSign().get());
            event.modify(BlockEntityTypes.SHELF, set.shelf().get());
        }
    }
}
