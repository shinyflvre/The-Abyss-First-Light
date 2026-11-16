package net.yezon.theabyss.devtest;

import java.util.function.Consumer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

final class InfectedHandAutotest {
    private InfectedHandAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("hand arena", () -> {
            command.accept("execute in minecraft:overworld run forceload add -16 -16 16 16");
            command.accept("execute in minecraft:overworld run fill -8 199 -8 8 199 8 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -8 200 -8 8 206 8 minecraft:air");
            command.accept("time set noon");
            command.accept("gamemode survival @s");
            command.accept("clear @s");
            command.accept("execute in minecraft:overworld run tp @s 0.5 200 0.5 180 10");
            command.accept("item replace entity @s weapon.mainhand with theabyss:infected_gauntlet");
        }, 40);
        add.add("hand fp main", () -> mc.options.setCameraType(CameraType.FIRST_PERSON), 10);
        add.add("hand shot fp main", () -> shot.accept("109_hand_1_fp_main"), 2);
        add.add("hand tp", () -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT), 10);
        add.add("hand shot tp main", () -> shot.accept("109_hand_2_tp_main"), 2);
        add.add("hand night", () -> command.accept("time set midnight"), 20);
        add.add("hand shot tp night", () -> shot.accept("109_hand_3_tp_night"), 2);
        add.add("hand fp night", () -> mc.options.setCameraType(CameraType.FIRST_PERSON), 10);
        add.add("hand shot fp night", () -> shot.accept("109_hand_4_fp_night"), 2);
        add.add("hand day", () -> {
            command.accept("time set noon");
            command.accept("item replace entity @s weapon.mainhand with minecraft:air");
            command.accept("item replace entity @s weapon.offhand with theabyss:infected_gauntlet");
        }, 20);
        add.add("hand shot fp off", () -> shot.accept("109_hand_5_fp_offhand"), 2);
        add.add("hand tp off", () -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT), 10);
        add.add("hand shot tp off", () -> shot.accept("109_hand_6_tp_offhand"), 2);
        add.add("hand back", () -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK), 10);
        add.add("hand shot tp back", () -> shot.accept("109_hand_7_tp_back"), 2);
        add.add("hand reset", () -> {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            command.accept("clear @s");
        }, 4);
        add.add("hand result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT hand DONE (skin model {})",
            mc.player == null ? "?" : mc.player.getSkin().model()), 2);
    }
}
