package net.yezon.theabyss.devtest;

import java.util.function.Consumer;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

final class RingFxAutotest {
    private static final String[][] RINGS = {
        {"blackstrike", "ring_of_blackstrike"}, {"crystals", "ring_of_crystals"}, {"eagle", "ring_of_eagle"},
        {"electro", "ring_of_electro"}, {"fangs", "ring_of_fangs"}, {"fire", "ring_of_fire"}, {"firestrike", "ring_of_firestrike"},
        {"freeze", "ring_of_freeze"}, {"nature", "ring_of_nature"}, {"nightblade", "ring_of_nightblade"}, {"ocean", "ring_of_ocean"},
        {"reg", "ring_of_reg"}, {"seeker", "ring_of_seeker"}, {"speed", "ring_of_speed"}, {"telekinetic", "ring_of_telekinetic"},
        {"teleport", "ring_of_teleport"}, {"thunder", "ring_of_thunder"}, {"curse", "ring_of_curse"}, {"jugger", "ring_of_jugger"},
        {"invisibility", "ring_of_invisibility"}, {"firestorm", "ring_of_firestorm"}, {"time", "ring_of_time"},
        {"ghosts", "ring_of_ghosts"}, {"home", "ring_of_home"}, {"spectral", "ring_of_spectral"}, {"slide", "ring_of_slide"},
        {"flight", "ring_of_flight"}, {"silent_echoes", "ring_of_silent_echoes"}, {"ender_chest", "ring_of_ender_chest"},
        {"pocket_box", "ring_of_pocket_box"}, {"possession", "ring_of_possession"},
    };

    private RingFxAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("ringfx arena", () -> {
            command.accept("execute in minecraft:overworld run forceload add -32 -32 32 32");
            command.accept("execute in minecraft:overworld run fill -30 199 -30 30 199 30 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -30 200 -30 30 225 30 minecraft:air");
            command.accept("time set 13000");
            command.accept("weather clear");
            command.accept("difficulty normal");
            command.accept("gamerule doMobSpawning false");
            command.accept("gamemode creative @s");
            command.accept("ta2 somnium set infinite");
        }, 80);
        add.add("ringfx camera", () -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK), 10);
        for (String[] ring : RINGS) {
            String name = ring[0];
            add.add(name + " setup", () -> {
                command.accept("kill @e[type=minecraft:husk]");
                command.accept("kill @e[type=minecraft:item]");
                command.accept("effect clear @s");
                command.accept("gamemode creative @s");
                if (mc.player != null) {
                    mc.player.closeContainer();
                }
                command.accept("execute in minecraft:overworld run fill -30 200 -30 30 210 30 minecraft:air replace minecraft:fire");
                String height = name.equals("flight") || name.equals("slide") ? "204" : "200";
                command.accept("execute in minecraft:overworld run tp @s 0.5 " + height + " -6 0 " + (name.equals("possession") ? "5" : "32"));
                command.accept("item replace entity @s weapon.mainhand with theabyss:" + ring[1]);
                for (String at : new String[] {"0.5 200 1.5", "3.5 200 3.5", "-2.5 200 4.5"}) {
                    command.accept("execute in minecraft:overworld run summon minecraft:husk " + at + " {NoAI:1b,PersistenceRequired:1b}");
                }
            }, 30);
            add.add(name + " cast", () -> KeyMapping.click(mc.options.keyUse.getKey()), name.equals("ender_chest") ? 2 : 5);
            if (name.equals("ender_chest")) {
                add.add(name + " close", () -> {
                    if (mc.player != null) {
                        mc.player.closeContainer();
                    }
                }, 3);
            }
            add.add(name + " shot a", () -> shot.accept("120_" + name + "_a"), 12);
            add.add(name + " shot b", () -> shot.accept("120_" + name + "_b"), name.equals("firestorm") ? 220 : 40);
            if (name.equals("spectral")) {
                add.add(name + " leave", () -> KeyMapping.click(mc.options.keyUse.getKey()), 30);
            }
        }
        add.add("ringfx end possession", () -> {
            command.accept("kill @e[type=minecraft:husk]");
            command.accept("gamemode creative @s");
        }, 30);
        add.add("ringfx camera back", () -> mc.options.setCameraType(CameraType.FIRST_PERSON), 10);
    }
}
