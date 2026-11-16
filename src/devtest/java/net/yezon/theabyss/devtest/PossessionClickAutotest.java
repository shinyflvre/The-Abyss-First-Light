package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.AABB;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.possession.PossessionManager;

final class PossessionClickAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;

    private PossessionClickAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("pc arena", () -> {
            command.accept("execute in minecraft:overworld run forceload add -16 -16 16 32");
            command.accept("execute in minecraft:overworld run fill -10 199 -4 10 199 24 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -10 200 -4 10 206 24 minecraft:air");
            command.accept("time set midnight");
            command.accept("difficulty normal");
            command.accept("gamemode survival @s");
            command.accept("clear @s");
            command.accept("kill @e[type=!player]");
            command.accept("execute in minecraft:overworld run tp @s 0.5 200 0.5 0 0");
            command.accept("item replace entity @s weapon.mainhand with theabyss:ring_of_possession");
            command.accept("execute in minecraft:overworld run summon minecraft:skeleton 0.5 200 5.5 {Rotation:[0f,0f],PersistenceRequired:1b,NoAI:1b,"
                + "equipment:{mainhand:{id:\"minecraft:bow\",count:1}}}");
        }, 80);
        add.add("pc mana", () -> onServer(sp -> TheabyssModVariables.updatePlayerVariables(sp, c -> {
            c.Mana = 1000.0;
            c.syncPlayerVariables(sp);
        })), 4);
        add.add("pc aim", () -> {
            mc.player.setYRot(0.0F);
            mc.player.setXRot(10.0F);
        }, 4);
        add.add("pc possess click", () -> {
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] possclick crosshair {} {}", mc.hitResult == null ? "null" : mc.hitResult.getType(),
                mc.crosshairPickEntity == null ? "-" : mc.crosshairPickEntity.getType().toShortString());
            KeyMapping.click(mc.options.keyUse.getKey());
        }, 60);
        add.add("pc check possess", () -> onServer(sp -> check("a real right click possesses the skeleton", PossessionManager.isPossessing(sp))), 4);
        add.add("pc target cow", () -> command.accept("execute in minecraft:overworld run summon minecraft:cow 0.5 200 16.5 {NoAI:1b}"), 10);
        add.add("pc aim cow", () -> {
            mc.player.setYRot(0.0F);
            mc.player.setXRot(0.0F);
        }, 4);
        add.add("pc ability click", () -> {
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] possclick crosshair {} {}", mc.hitResult == null ? "null" : mc.hitResult.getType(),
                mc.crosshairPickEntity == null ? "-" : mc.crosshairPickEntity.getType().toShortString());
            KeyMapping.click(mc.options.keyUse.getKey());
        }, 12);
        add.add("pc check ability", () -> onServer(sp -> {
            int arrows = sp.level().getEntitiesOfClass(AbstractArrow.class, new AABB(-12, 195, -6, 12, 210, 30)).size();
            check("a real right click while possessing shoots (" + arrows + " arrows, cooldown " + sp.getCooldowns().isOnCooldown(sp.getMainHandItem()) + ")",
                arrows > 0);
        }), 4);
        add.add("pc leave", () -> mc.options.keyShift.setDown(true), 10);
        add.add("pc leave up", () -> mc.options.keyShift.setDown(false), 20);
        add.add("pc check left", () -> onServer(sp -> check("sneaking leaves", !PossessionManager.isPossessing(sp))), 2);
        add.add("pc wake", () -> command.accept("execute in minecraft:overworld run data merge entity @e[type=minecraft:skeleton,limit=1] {NoAI:0b}"), 4);
        add.add("pc near", () -> {
            command.accept("kill @e[type=minecraft:cow]");
            command.accept("kill @e[type=minecraft:arrow]");
            command.accept("execute in minecraft:overworld run summon minecraft:zombie 6.5 200 8.5 {PersistenceRequired:1b}");
            command.accept("execute in minecraft:overworld run tp @s 0.5 200 10.5");
        }, 100);
        add.add("pc check hunted", () -> onServer(sp -> {
            List<Mob> mobs = sp.level().getEntitiesOfClass(Mob.class, new AABB(-12, 195, -6, 12, 210, 30));
            StringBuilder info = new StringBuilder();
            boolean skeleton = false;
            boolean zombie = false;
            for (Mob mob : mobs) {
                info.append(mob.getType().toShortString()).append("->").append(mob.getTarget() == null ? "none" : mob.getTarget().getName().getString()).append(' ');
                if (mob.getTarget() == sp) {
                    skeleton |= mob.getType() == net.minecraft.world.entity.EntityTypes.SKELETON;
                    zombie |= mob.getType() == net.minecraft.world.entity.EntityTypes.ZOMBIE;
                }
            }
            check("the freed skeleton hunts the player again (" + info + ", health " + sp.getHealth() + ")", skeleton);
            check("a zombie hunts the player (" + info + ")", zombie);
        }), 2);
        add.add("pc result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT possclick {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] possclick check {}: {}", name, ok ? "ok" : "FAILED");
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
