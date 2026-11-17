package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.yezon.theabyss.weather.client.FrostStormClient;

final class FrostAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static int groundY;
    private static float frostBefore;

    private FrostAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        add.add("frost go", () -> {
            command.accept("gamemode survival @s");
            command.accept("gamerule doMobSpawning false");
            command.accept("effect give @s minecraft:resistance 9999 4 true");
            command.accept("effect give @s minecraft:fire_resistance 9999 0 true");
            command.accept("execute in theabyss:frost_world run forceload add -32 -32 32 32");
            command.accept("execute in theabyss:frost_world run tp @s 0.5 250 0.5 0 10");
        }, 200);
        add.add("frost ground", () -> onServer(sp -> {
            groundY = sp.serverLevel().getHeight(Heightmap.Types.MOTION_BLOCKING, sp.getBlockX(), sp.getBlockZ());
            command.accept("execute in theabyss:frost_world run tp @s 0.5 " + groundY + " 0.5 0 5");
        }), 200);
        add.add("frost check storm", () -> {
            Minecraft mc = Minecraft.getInstance();
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] frost rain {} gust {} frost {} particles {}", mc.level.getRainLevel(1.0F), FrostStormClient.gust(),
                FrostStormClient.frost(), mc.particleEngine.countParticles());
            check("snow storm is on (rain level " + mc.level.getRainLevel(1.0F) + ")", mc.level.getRainLevel(1.0F) >= 0.99F);
            check("frost slowly building up outside (" + FrostStormClient.frost() + ")", FrostStormClient.frost() > 0.0F && FrostStormClient.frost() < 0.4F);
            frostBefore = FrostStormClient.frost();
        }, 2);
        add.add("frost shot storm", () -> shot.accept("125_frost_storm"), 6);
        add.add("frost look around", () -> command.accept("tp @s ~ ~ ~ 120 -5"), 20);
        add.add("frost shot storm 2", () -> shot.accept("125_frost_storm_2"), 6);
        add.add("frost full", () -> setFrost(0.8F), 10);
        add.add("frost shot overlay", () -> shot.accept("125_frost_overlay"), 6);
        add.add("frost roof", () -> {
            command.accept("execute in theabyss:frost_world run fill -2 " + (groundY + 3) + " -2 2 " + (groundY + 3) + " 2 minecraft:stone");
            frostBefore = FrostStormClient.frost();
        }, 200);
        add.add("frost check shelter", () -> {
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] frost under the roof {} -> {}", frostBefore, FrostStormClient.frost());
            check("frost fades under a roof (" + frostBefore + " -> " + FrostStormClient.frost() + ")", FrostStormClient.frost() < frostBefore - 0.3F);
        }, 2);
        add.add("frost fire", () -> {
            setFrost(0.8F);
            command.accept("execute in theabyss:frost_world run fill -2 " + (groundY + 3) + " -2 2 " + (groundY + 3) + " 2 minecraft:air");
            command.accept("execute in theabyss:frost_world run setblock 2 " + groundY + " 0 minecraft:campfire");
        }, 100);
        add.add("frost check fire", () -> check("a campfire warms you outside (" + FrostStormClient.frost() + ")", FrostStormClient.frost() < 0.3F), 2);
        add.add("frost leave", () -> command.accept("execute in minecraft:overworld run tp @s 0.5 200 0.5"), 80);
        add.add("frost check leave", () -> check("frost and storm end in the overworld (" + FrostStormClient.frost() + ")", FrostStormClient.frost() == 0.0F), 2);
        add.add("frost result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT frost {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL", checks,
            failures.isEmpty() ? "" : ", failed: " + failures), 2);
    }

    private static void setFrost(float value) {
        try {
            var f = FrostStormClient.class.getDeclaredField("frost");
            f.setAccessible(true);
            f.setFloat(null, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] frost check {}: {}", name, ok ? "ok" : "FAILED");
    }

    private static void onServer(Consumer<ServerPlayer> action) {
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) {
            return;
        }
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp != null) {
                action.accept(sp);
            }
        });
    }
}
