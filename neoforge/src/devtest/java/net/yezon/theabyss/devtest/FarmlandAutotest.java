package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class FarmlandAutotest {
    private static final String[] NIGHTGROVE = {"theabyss:nightgrove_dirt", "theabyss:nightgrove_moss"};
    private static final String[] INFECTED = {"theabyss:dirt", "theabyss:infected_grass", "theabyss:blaru_moss", "theabyss:shroomland_moss", "theabyss:slimed_moss"};
    private static final Item[] SEEDS = {Items.WHEAT_SEEDS, Items.CARROT, Items.POTATO, Items.BEETROOT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.TORCHFLOWER_SEEDS};
    private static final List<String> failures = new ArrayList<>();
    private static int checks;

    private FarmlandAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        add.add("fl arena", () -> {
            command.accept("gamemode creative @s");
            command.accept("gamerule doMobSpawning false");
            command.accept("time set noon");
            command.accept("gamerule randomTickSpeed 0");
            command.accept("execute in theabyss:the_abyss run forceload add -16 -16 16 16");
            command.accept("execute in theabyss:the_abyss run fill -9 117 -9 9 119 9 minecraft:stone");
            command.accept("execute in theabyss:the_abyss run fill -9 120 -9 9 126 9 minecraft:air");
            command.accept("execute in theabyss:the_abyss run tp @s 0.5 120 0.5 0 0");
        }, 80);
        add.add("fl sources", () -> {
            List<String> all = all();
            for (int i = 0; i < all.size(); i++) {
                command.accept("execute in theabyss:the_abyss run setblock " + (i - 3) + " 119 2 " + all.get(i));
                command.accept("execute in theabyss:the_abyss run setblock " + (i - 3) + " 119 -2 " + all.get(i));
            }
        }, 10);
        List<String> all = all();
        for (int i = 0; i < all.size(); i++) {
            BlockPos hoePos = new BlockPos(i - 3, 119, 2);
            BlockPos shovelPos = new BlockPos(i - 3, 119, -2);
            add.add("fl hoe " + i, () -> use(Items.NETHERITE_HOE, hoePos), 3);
            add.add("fl shovel " + i, () -> use(Items.NETHERITE_SHOVEL, shovelPos), 3);
        }
        add.add("fl check tools", () -> onServer(level -> {
            for (int i = 0; i < all.size(); i++) {
                String kind = i < NIGHTGROVE.length ? "nightgrove" : "infected";
                check("hoe on " + all.get(i) + " -> " + kind + " farmland", is(level, new BlockPos(i - 3, 119, 2), "theabyss:" + kind + "_farmland"));
                check("shovel on " + all.get(i) + " -> " + kind + " path", is(level, new BlockPos(i - 3, 119, -2), "theabyss:" + kind + "_path"));
            }
        }), 4);
        add.add("fl hoe path nightgrove", () -> use(Items.NETHERITE_HOE, new BlockPos(-3, 119, -2)), 3);
        add.add("fl hoe path infected", () -> use(Items.NETHERITE_HOE, new BlockPos(-1, 119, -2)), 3);
        add.add("fl check path till", () -> onServer(level -> {
            check("hoe on nightgrove path -> nightgrove farmland", is(level, new BlockPos(-3, 119, -2), "theabyss:nightgrove_farmland"));
            check("hoe on infected path -> infected farmland", is(level, new BlockPos(-1, 119, -2), "theabyss:infected_farmland"));
        }), 4);
        add.add("fl water", () -> {
            command.accept("execute in theabyss:the_abyss run setblock 0 119 3 minecraft:water");
            command.accept("execute in theabyss:the_abyss run setblock 0 119 4 minecraft:water");
        }, 4);
        for (int i = 0; i < SEEDS.length; i++) {
            Item seed = SEEDS[i];
            BlockPos soil = new BlockPos(i - 3, 119, 2);
            add.add("fl plant " + i, () -> use(seed, soil), 3);
        }
        add.add("fl check planted", () -> onServer(level -> {
            for (int i = 0; i < SEEDS.length; i++) {
                BlockState crop = level.getBlockState(new BlockPos(i - 3, 120, 2));
                check(BuiltInRegistries.ITEM.getKey(SEEDS[i]) + " planted on " + id(level.getBlockState(new BlockPos(i - 3, 119, 2))) + " (" + id(crop) + ")", !crop.isAir());
            }
        }), 4);
        add.add("fl fast ticks", () -> command.accept("gamerule randomTickSpeed 1000"), 300);
        add.add("fl check growth", () -> onServer(level -> {
            for (int i = 0; i < SEEDS.length; i++) {
                BlockPos soil = new BlockPos(i - 3, 119, 2);
                BlockState farm = level.getBlockState(soil);
                BlockState crop = level.getBlockState(soil.above());
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] farmland {} moisture {} crop {}", id(farm), farm.hasProperty(FarmBlock.MOISTURE) ? farm.getValue(FarmBlock.MOISTURE) : -1, crop);
                check("soil stays farmland under " + BuiltInRegistries.ITEM.getKey(SEEDS[i]), farm.getBlock() instanceof FarmBlock);
                check("farmland near water is wet " + soil, farm.hasProperty(FarmBlock.MOISTURE) && farm.getValue(FarmBlock.MOISTURE) == FarmBlock.MAX_MOISTURE);
            }
            BlockState wheat = level.getBlockState(new BlockPos(-3, 120, 2));
            BlockState potato = level.getBlockState(new BlockPos(-1, 120, 2));
            check("wheat on nightgrove farmland grew (" + wheat + ")", wheat.getBlock() instanceof CropBlock c && c.getAge(wheat) > 0);
            check("potato on infected farmland grew (" + potato + ")", potato.getBlock() instanceof CropBlock c && c.getAge(potato) > 0);
        }), 4);
        add.add("fl shot", () -> {
            command.accept("gamerule randomTickSpeed 3");
            command.accept("execute in theabyss:the_abyss run setblock -3 119 -5 theabyss:nightgrove_farmland");
            command.accept("execute in theabyss:the_abyss run setblock -2 119 -5 theabyss:nightgrove_path");
            command.accept("execute in theabyss:the_abyss run setblock 0 119 -5 theabyss:infected_farmland");
            command.accept("execute in theabyss:the_abyss run setblock 1 119 -5 theabyss:infected_path");
            command.accept("execute in theabyss:the_abyss run setblock -1 119 -5 theabyss:nightgrove_dirt");
            command.accept("execute in theabyss:the_abyss run setblock 2 119 -5 theabyss:dirt");
            command.accept("time set noon");
            command.accept("execute in theabyss:the_abyss run tp @s 0.5 123.5 -9.5 0 32");
        }, 40);
        add.add("fl shot take", () -> shot.accept("120_farmland_path"), 10);
        add.add("fl stone above", () -> {
            command.accept("execute in theabyss:the_abyss run setblock -2 120 -5 minecraft:stone");
            command.accept("execute in theabyss:the_abyss run setblock 1 120 -5 minecraft:stone");
            command.accept("execute in theabyss:the_abyss run setblock -3 120 -5 minecraft:stone");
            command.accept("execute in theabyss:the_abyss run setblock 0 120 -5 minecraft:stone");
        }, 10);
        add.add("fl check revert", () -> onServer(level -> {
            check("nightgrove farmland under stone -> nightgrove dirt", is(level, new BlockPos(-3, 119, -5), "theabyss:nightgrove_dirt"));
            check("nightgrove path under stone -> nightgrove dirt", is(level, new BlockPos(-2, 119, -5), "theabyss:nightgrove_dirt"));
            check("infected farmland under stone -> infected dirt", is(level, new BlockPos(0, 119, -5), "theabyss:dirt"));
            check("infected path under stone -> infected dirt", is(level, new BlockPos(1, 119, -5), "theabyss:dirt"));
            BlockPos dry = new BlockPos(-1, 119, -2);
            BlockState farm = level.getBlockState(dry);
            if (farm.getBlock() instanceof net.yezon.theabyss.block.AbyssFarmlandBlock block) {
                block.turnToBaseBlock(null, farm, level, dry);
            }
            check("trampled infected farmland -> infected dirt", is(level, dry, "theabyss:dirt"));
            for (String b : List.of("nightgrove_farmland", "infected_farmland", "nightgrove_path", "infected_path")) {
                Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("theabyss", b));
                List<ItemStack> drops = Block.getDrops(block.defaultBlockState(), level, BlockPos.ZERO, null);
                String expected = b.startsWith("nightgrove") ? "theabyss:nightgrove_dirt" : "theabyss:dirt";
                check(b + " drops " + expected + " " + drops, drops.size() == 1 && BuiltInRegistries.ITEM.getKey(drops.get(0).getItem()).toString().equals(expected));
            }
        }), 4);
        add.add("fl showcase", () -> {
            command.accept("execute in minecraft:overworld run forceload add -16 -16 16 16");
            command.accept("execute in minecraft:overworld run fill -6 197 -10 6 199 2 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -6 200 -10 6 206 2 minecraft:air");
            String[] row = {"theabyss:nightgrove_dirt", "theabyss:nightgrove_farmland[moisture=0]", "theabyss:nightgrove_farmland[moisture=7]", "theabyss:nightgrove_path",
                "minecraft:stone", "theabyss:dirt", "theabyss:infected_farmland[moisture=0]", "theabyss:infected_farmland[moisture=7]", "theabyss:infected_path"};
            for (int i = 0; i < row.length; i++) {
                command.accept("execute in minecraft:overworld run setblock " + (i - 4) + " 199 -4 " + row[i]);
                command.accept("execute in minecraft:overworld run setblock " + (i - 4) + " 199 -6 " + row[i]);
            }
            command.accept("execute in minecraft:overworld run setblock -3 200 -6 minecraft:wheat[age=7]");
            command.accept("execute in minecraft:overworld run setblock -2 200 -6 minecraft:carrots[age=7]");
            command.accept("execute in minecraft:overworld run setblock 2 200 -6 minecraft:potatoes[age=7]");
            command.accept("execute in minecraft:overworld run setblock 3 200 -6 minecraft:beetroots[age=3]");
            command.accept("time set noon");
            command.accept("execute in minecraft:overworld run tp @s 0.5 202.6 -0.5 180 38");
        }, 60);
        add.add("fl showcase shot", () -> shot.accept("120_farmland_path_overworld"), 10);
        add.add("fl result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT farmland {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
    }

    private static List<String> all() {
        List<String> all = new ArrayList<>(List.of(NIGHTGROVE));
        all.addAll(List.of(INFECTED));
        return all;
    }

    private static void use(Item item, BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) {
            return;
        }
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp != null) {
                sp.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item, 16));
                String before = id(sp.serverLevel().getBlockState(pos));
                Object result = sp.gameMode.useItemOn(sp, sp.serverLevel(), sp.getMainHandItem(), InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos).add(0.0, 0.5, 0.0), Direction.UP, pos, false));
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] farmland use {} on {} {} -> {} ({})", BuiltInRegistries.ITEM.getKey(item), pos, before,
                    id(sp.serverLevel().getBlockState(pos)), result);
            }
        });
    }

    private static boolean is(ServerLevel level, BlockPos pos, String id) {
        return id(level.getBlockState(pos)).equals(id);
    }

    private static String id(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }

    private static void onServer(Consumer<ServerLevel> action) {
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) {
            return;
        }
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp != null) {
                action.accept(sp.serverLevel());
            }
        });
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] farmland check {}: {}", name, ok ? "ok" : "FAILED");
    }
}
