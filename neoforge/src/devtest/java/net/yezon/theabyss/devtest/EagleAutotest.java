package net.yezon.theabyss.devtest;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;

final class EagleAutotest {
    private static final String[] BOSSES = {"the_roka", "abyssaur", "magician", "elder", "crystal_golem"};

    private EagleAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("eagle arena", () -> {
            command.accept("execute in minecraft:overworld run forceload add -48 -48 48 112");
            command.accept("execute in minecraft:overworld run fill -40 199 -8 40 199 40 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -40 200 -8 40 215 40 minecraft:air");
            command.accept("time set noon");
            command.accept("weather clear");
            command.accept("difficulty normal");
            command.accept("gamemode creative @s");
        }, 80);
        add.add("eagle place", () -> {
            command.accept("execute in minecraft:overworld run tp @s 0 200 -4 0 5");
            for (int i = 0; i < BOSSES.length; i++) {
                double x = -15 + i * 6;
                double z = 12 + (i % 2) * 8;
                command.accept("execute in minecraft:overworld run summon theabyss:" + BOSSES[i] + " " + x + " 200 " + z
                    + " {NoAI:1b,PersistenceRequired:1b,Rotation:[180f,0f]}");
            }
            command.accept("execute in minecraft:overworld run summon theabyss:nightblade_boss 0 200 100 {NoAI:1b,PersistenceRequired:1b}");
        }, 80);
        add.add("eagle shot before", () -> shot.accept("99_eagle_1_before"), 10);
        add.add("eagle cast", () -> onServer(sp -> net.yezon.theabyss.ring.EagleBossTracker.start(sp, 200)), 20);
        add.add("eagle shot outlines", () -> shot.accept("99_eagle_2_outlines"), 10);
        add.add("eagle check", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] eagle compass shows {} bosses (expected {})",
            net.yezon.theabyss.client.fx.EagleBossCompass.shown(), BOSSES.length + 1), 2);
        add.add("eagle glowing", () -> {
            for (var e : mc.level.entitiesForRendering()) {
                if (e instanceof net.minecraft.world.entity.LivingEntity l && net.yezon.theabyss.ring.EagleBossTracker.isBoss(l)) {
                    TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] eagle client {} glowing {} outline {}", e.getType().getDescriptionId(),
                        mc.shouldEntityAppearGlowing(e), Integer.toHexString(e.getTeamColor()));
                }
            }
        }, 2);
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
