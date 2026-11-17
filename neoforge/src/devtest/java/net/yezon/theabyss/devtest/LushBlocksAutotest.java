package net.yezon.theabyss.devtest;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;

final class LushBlocksAutotest {
    private static final String[] PLACE = {
        "lush_moss_block", "lush_moss_carpet", "lush_azalea", "lush_flowering_azalea", "lush_azalea_leaves", "lush_flowering_azalea_leaves",
        "lush_clay", "lush_rooted_dirt", "lush_spore_blossom", "lush_hanging_roots", "lush_glow_lichen[down=true]", "lush_small_dripleaf",
        "lush_big_dripleaf", "lush_cave_vines[berries=true]", "lush_cave_vines_plant[berries=true]"
    };
    private static final String[] GIVE = {
        "lush_moss_block", "lush_moss_carpet", "lush_azalea", "lush_flowering_azalea", "lush_azalea_leaves", "lush_flowering_azalea_leaves",
        "lush_glow_berries", "lush_glow_lichen", "lush_spore_blossom", "lush_big_dripleaf", "lush_small_dripleaf", "lush_hanging_roots",
        "lush_clay", "lush_rooted_dirt"
    };

    private LushBlocksAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("lb arena", () -> {
            command.accept("execute in minecraft:overworld run forceload add -16 -16 32 16");
            command.accept("execute in minecraft:overworld run fill -2 199 -2 32 199 6 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -2 200 -2 32 208 6 minecraft:air");
            command.accept("time set noon");
            command.accept("gamemode creative @s");
            command.accept("clear @s");
            for (int i = 0; i < PLACE.length; i++) {
                int x = i * 2;
                String block = PLACE[i];
                if (block.startsWith("lush_spore_blossom") || block.startsWith("lush_hanging_roots") || block.startsWith("lush_cave_vines")
                    || block.startsWith("lush_glow_lichen")) {
                    command.accept("execute in minecraft:overworld run setblock " + x + " 203 3 theabyss:lush_moss_block");
                    command.accept("execute in minecraft:overworld run setblock " + x + " 202 3 theabyss:" + block);
                } else if (block.equals("lush_big_dripleaf")) {
                    command.accept("execute in minecraft:overworld run setblock " + x + " 199 3 theabyss:lush_clay");
                    command.accept("execute in minecraft:overworld run setblock " + x + " 200 3 theabyss:lush_big_dripleaf_stem");
                    command.accept("execute in minecraft:overworld run setblock " + x + " 201 3 theabyss:lush_big_dripleaf");
                } else if (block.equals("lush_small_dripleaf") || block.startsWith("lush_azalea") && !block.contains("leaves")
                    || block.startsWith("lush_flowering_azalea") && !block.contains("leaves") || block.equals("lush_moss_carpet")) {
                    command.accept("execute in minecraft:overworld run setblock " + x + " 199 3 theabyss:lush_moss_block");
                    command.accept("execute in minecraft:overworld run setblock " + x + " 200 3 theabyss:" + block);
                } else {
                    command.accept("execute in minecraft:overworld run setblock " + x + " 200 3 theabyss:" + block);
                }
            }
            for (int i = 0; i < GIVE.length && i < 36; i++) {
                command.accept("item replace entity @s " + (i < 9 ? "hotbar." + i : "inventory." + (i - 9)) + " with theabyss:" + GIVE[i]);
            }
            command.accept("execute in minecraft:overworld run tp @s 14 202 -5 0 15");
        }, 60);
        add.add("lb shot row", () -> shot.accept("113_lush_blocks_row"), 2);
        add.add("lb left", () -> command.accept("execute in minecraft:overworld run tp @s 6 201.5 -1.5 0 25"), 20);
        add.add("lb shot left", () -> shot.accept("113_lush_blocks_left"), 2);
        add.add("lb right", () -> command.accept("execute in minecraft:overworld run tp @s 22 201.5 -1.5 0 25"), 20);
        add.add("lb shot right", () -> shot.accept("113_lush_blocks_right"), 2);
        add.add("lb inventory", () -> mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player)), 20);
        add.add("lb shot inventory", () -> shot.accept("113_lush_blocks_inventory"), 2);
        add.add("lb close", () -> mc.setScreen(null), 4);
    }
}
