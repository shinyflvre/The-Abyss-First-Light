package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.yezon.theabyss.client.debug.OreHighlighter;

final class OresAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;

    private OresAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        add.add("ores go", () -> {
            command.accept("gamemode spectator @s");
            command.accept("execute in theabyss:the_abyss run tp @s 8 40 8 45 20");
        }, 200);
        add.add("ores all", () -> client("ta2 debug ores all"), 40);
        add.add("ores all check", () -> {
            int found = found();
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] ores all highlighted {}", found);
            check("all: ores found and outlined (" + found + ")", found > 0);
        }, 2);
        add.add("ores shot all", () -> shot.accept("124_ores_all"), 6);
        add.add("ores one", () -> {
            client("ta2 debug ores reset");
            client("ta2 debug ores garnite_ore");
        }, 40);
        add.add("ores one check", () -> {
            int found = found();
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] ores garnite highlighted {}", found);
            double nearest = Double.MAX_VALUE;
            for (Object f : (List<?>)field("FOUND")) {
                try {
                    var accessor = f.getClass().getRecordComponents()[0].getAccessor();
                    accessor.setAccessible(true);
                    var pos = (net.minecraft.core.BlockPos)accessor.invoke(f);
                    nearest = Math.min(nearest, Math.sqrt(pos.distToCenterSqr(Minecraft.getInstance().gameRenderer.mainCamera().position())));
                } catch (ReflectiveOperationException e) {
                    break;
                }
            }
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] ores nearest garnite {} blocks, fog end {}", nearest,
                Minecraft.getInstance().options.getEffectiveRenderDistance() * 16);
            check("single ore: only garnite ore outlined (" + found + ")", found > 0 && onlyColor());
        }, 2);
        add.add("ores shot one", () -> shot.accept("124_ores_garnite"), 6);
        add.add("ores crystals", () -> client("ta2 debug crystals all"), 40);
        add.add("ores crystals check", () -> {
            int found = found();
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] ores + crystals highlighted {}, targets {}", found, ((java.util.Map<?, ?>)field("TARGETS")).size());
            check("ores and crystals at the same time (" + ((java.util.Map<?, ?>)field("TARGETS")).size() + " targets)", ((java.util.Map<?, ?>)field("TARGETS")).size() == 9);
        }, 2);
        add.add("ores shot crystals", () -> shot.accept("124_ores_crystals"), 6);
        add.add("ores reset", () -> {
            client("ta2 debug ores reset");
            client("ta2 debug crystals reset");
        }, 20);
        add.add("ores reset check", () -> check("reset clears everything (" + found() + ")", found() == 0), 2);
        add.add("ores shot reset", () -> shot.accept("124_ores_reset"), 6);
        add.add("ores result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT ores {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL", checks,
            failures.isEmpty() ? "" : ", failed: " + failures), 2);
    }

    private static void client(String command) {
        Minecraft.getInstance().player.connection.sendCommand(command);
    }

    private static int found() {
        return ((List<?>)field("FOUND")).size();
    }

    private static boolean onlyColor() {
        return ((java.util.Map<?, ?>)field("TARGETS")).size() == 1;
    }

    private static Object field(String name) {
        try {
            var f = OreHighlighter.class.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] ores check {}: {}", name, ok ? "ok" : "FAILED");
    }
}
