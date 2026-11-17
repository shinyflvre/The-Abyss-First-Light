package net.yezon.theabyss.init;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.yezon.theabyss.mixin.accessor.ButtonBlockInvoker;
import net.yezon.theabyss.mixin.accessor.DoorBlockInvoker;
import net.yezon.theabyss.mixin.accessor.PressurePlateBlockInvoker;
import net.yezon.theabyss.mixin.accessor.StairBlockInvoker;
import net.yezon.theabyss.mixin.accessor.TrapDoorBlockInvoker;
import net.yezon.theabyss.platform.registries.DeferredBlock;
import net.yezon.theabyss.platform.registries.DeferredItem;
import net.yezon.theabyss.block.flora.AbyssLeavesBlock;

public final class TheabyssModTreeSets {
    public record TreeSet(String name, DeferredBlock<Block> log, DeferredBlock<Block> strippedLog, DeferredBlock<Block> wood, DeferredBlock<Block> strippedWood,
                          DeferredBlock<Block> planks, DeferredBlock<Block> stairs, DeferredBlock<Block> slab, DeferredBlock<Block> fence,
                          DeferredBlock<Block> fenceGate, DeferredBlock<Block> door, DeferredBlock<Block> trapdoor, DeferredBlock<Block> button,
                          DeferredBlock<Block> pressurePlate, DeferredBlock<Block> leaves, List<DeferredItem<Item>> items) {
    }

    public static final List<TreeSet> ALL = new ArrayList<>();
    public static final List<TreeSet> RELEASED = new ArrayList<>();

    private TheabyssModTreeSets() {
    }

    public static void init() {
    }

    private static BlockBehaviour.Properties wood(BlockBehaviour.Properties p, MapColor colour, float hardness, float resistance) {
        return p.mapColor(colour).instrument(NoteBlockInstrument.BASS).sound(SoundType.WOOD).strength(hardness, resistance).ignitedByLava();
    }

    private static DeferredBlock<Block> block(String name, Function<BlockBehaviour.Properties, Block> factory) {
        return TheabyssModBlocks.REGISTRY.registerBlock(name, factory);
    }

    private static TreeSet create(String name, MapColor colour) {
        DeferredBlock<Block> log = block(name + "_log", p -> new RotatedPillarBlock(wood(p, colour, 2.0F, 9.5F)));
        DeferredBlock<Block> strippedLog = block("stripped_" + name + "_log", p -> new RotatedPillarBlock(wood(p, colour, 2.0F, 9.5F)));
        DeferredBlock<Block> wood = block(name + "_wood", p -> new RotatedPillarBlock(wood(p, colour, 2.0F, 9.5F)));
        DeferredBlock<Block> strippedWood = block("stripped_" + name + "_wood", p -> new RotatedPillarBlock(wood(p, colour, 2.0F, 9.5F)));
        DeferredBlock<Block> planks = block(name + "_planks", p -> new Block(wood(p, colour, 2.0F, 10.0F)));
        DeferredBlock<Block> stairs = block(name + "_stairs", p -> StairBlockInvoker.theabyss$create(planks.get().defaultBlockState(), wood(p, colour, 2.0F, 10.0F)));
        DeferredBlock<Block> slab = block(name + "_slab", p -> new SlabBlock(wood(p, colour, 2.0F, 10.0F)));
        DeferredBlock<Block> fence = block(name + "_fence", p -> new FenceBlock(wood(p, colour, 2.0F, 10.0F).forceSolidOn()));
        DeferredBlock<Block> fenceGate = block(name + "_fence_gate", p -> new FenceGateBlock(WoodType.OAK, wood(p, colour, 3.0F, 10.0F).forceSolidOn()));
        DeferredBlock<Block> door = block(name + "_door", p -> DoorBlockInvoker.theabyss$create(BlockSetType.OAK, wood(p, colour, 3.0F, 10.0F).noOcclusion().pushReaction(PushReaction.DESTROY)));
        DeferredBlock<Block> trapdoor = block(name + "_trapdoor", p -> TrapDoorBlockInvoker.theabyss$create(BlockSetType.OAK,
            wood(p, colour, 1.0F, 10.0F).noOcclusion().isValidSpawn((state, level, pos, type) -> false)));
        DeferredBlock<Block> button = block(name + "_button", p -> ButtonBlockInvoker.theabyss$create(BlockSetType.OAK, 30,
            wood(p, colour, 1.5F, 10.0F).noCollission().pushReaction(PushReaction.DESTROY)));
        DeferredBlock<Block> pressurePlate = block(name + "_pressure_plate", p -> PressurePlateBlockInvoker.theabyss$create(BlockSetType.OAK,
            wood(p, colour, 1.0F, 10.0F).forceSolidOn().noCollission().pushReaction(PushReaction.DESTROY)));
        DeferredBlock<Block> leaves = block(name + "_leaves", p -> new AbyssLeavesBlock(p.mapColor(MapColor.PLANT).sound(SoundType.CHERRY_LEAVES)
            .strength(0.3F, 5.0F).noOcclusion().isRedstoneConductor((s, l, pos) -> false).isSuffocating((s, l, pos) -> false)
            .isValidSpawn((s, l, pos, type) -> false).ignitedByLava().pushReaction(PushReaction.DESTROY)));

        List<DeferredItem<Item>> items = new ArrayList<>();
        for (DeferredBlock<Block> b : List.of(log, wood, strippedLog, strippedWood, planks, stairs, slab, fence, fenceGate, door, trapdoor, button, pressurePlate, leaves)) {
            boolean doubleHigh = b == door;
            items.add(TheabyssModItems.REGISTRY.registerItem(b.getId().getPath(),
                p -> doubleHigh ? new DoubleHighBlockItem(b.get(), p) : new BlockItem(b.get(), p), p -> p));
        }
        TreeSet set = new TreeSet(name, log, strippedLog, wood, strippedWood, planks, stairs, slab, fence, fenceGate, door, trapdoor, button, pressurePlate, leaves, items);
        ALL.add(set);
        return set;
    }
}
