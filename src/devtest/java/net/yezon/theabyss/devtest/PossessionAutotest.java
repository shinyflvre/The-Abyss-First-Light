package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.possession.PossessionData;

final class PossessionAutotest {
    interface StepAdder {
        void add(String name, Runnable action, int waitAfter);
    }

    private static final List<String> failures = new ArrayList<>();
    private static int checks = 0;
    private static double manaBefore;
    private static double skeletonZ;

    private PossessionAutotest() {
    }

    static void addSteps(StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("possession arena", () -> {
            command.accept("execute in theabyss:the_abyss run forceload add -12 -12 12 12");
            command.accept("execute in theabyss:the_abyss run fill -12 149 -12 12 149 12 minecraft:stone");
            command.accept("execute in theabyss:the_abyss run fill -12 150 -12 12 160 12 minecraft:air");
            command.accept("execute in theabyss:the_abyss run fill -12 161 -12 12 161 12 minecraft:glowstone");
        }, 60);
        add.add("possession goto arena", () -> command.accept("execute in theabyss:the_abyss run tp @s 0 150 -9 0 0"), 100);
        add.add("possession mobs", () -> {
            command.accept("kill @e[type=!minecraft:player,type=!minecraft:mannequin,distance=..40]");
            command.accept("summon minecraft:skeleton 0 150 -2");
            command.accept("summon minecraft:cow 0 150 9 {NoAI:1b,PersistenceRequired:1b}");
            command.accept("item replace entity @s weapon.mainhand with theabyss:ring_of_possession");
            fillSomnium();
        }, 40);
        add.add("possess skeleton", () -> {
            manaBefore = 100.0;
            aimAndUse(mc);
        }, 4);
        add.add("shot flight 1", () -> shot.accept("50a_possession_flight_early"), 5);
        add.add("shot flight 2", () -> shot.accept("50b_possession_flight_middle"), 5);
        add.add("shot flight 3", () -> shot.accept("50c_possession_flight_late"), 16);
        add.add("check possessed", () -> {
            onServer(sp -> {
                check("player rides skeleton", sp.getVehicle() instanceof AbstractSkeleton);
                check("possession synced", PossessionData.of(sp).active());
                check("start cost paid", TheabyssModVariables.getPlayerVariables(sp).Mana <= manaBefore - 20.0 + 0.5);
                check("body mannequin placed", !bodies(sp).isEmpty());
                if (sp.getVehicle() != null) {
                    skeletonZ = sp.getVehicle().getZ();
                }
            });
            check("client camera in skeleton", mc.getCameraEntity() instanceof AbstractSkeleton);
        }, 5);
        add.add("shot possessed", () -> shot.accept("50_possession_inside_skeleton"), 10);
        add.add("walk forward", () -> mc.options.keyUp.setDown(true), 30);
        add.add("stop walking", () -> {
            mc.options.keyUp.setDown(false);
            onServer(sp -> check("skeleton walked forward", sp.getVehicle() != null && sp.getVehicle().getZ() > skeletonZ + 1.5));
        }, 10);
        add.add("shoot bow", () -> aimAndUse(mc), 3);
        add.add("shot arrow", () -> shot.accept("51_possession_skeleton_shoots"), 3);
        add.add("check arrow", () -> onServer(sp -> {
            AABB arena = new AABB(-12, 145, -12, 12, 162, 12).inflate(64);
            boolean arrowFlying = !sp.level().getEntitiesOfClass(AbstractArrow.class, arena, arrow -> arrow.getOwner() instanceof AbstractSkeleton).isEmpty();
            boolean cowHit = sp.level().getEntitiesOfClass(Cow.class, arena, cow -> cow.getHealth() < cow.getMaxHealth()).size() > 0;
            check("skeleton shot (arrow in flight or cow hit)", arrowFlying || cowHit);
        }), 5);
        add.add("sneak to leave", () -> mc.options.keyShift.setDown(true), 6);
        add.add("release sneak", () -> mc.options.keyShift.setDown(false), 20);
        add.add("check left", () -> {
            onServer(sp -> {
                check("left the skeleton", !PossessionData.of(sp).active() && sp.getVehicle() == null);
                check("back at the body position", sp.position().distanceTo(new Vec3(0.5, 150, -8.5)) < 1.5);
                check("body removed", bodies(sp).isEmpty());
            });
            check("client camera back on player", mc.getCameraEntity() == mc.player);
        }, 5);
        add.add("shot left", () -> shot.accept("52_possession_left"), 50);
        add.add("bring skeleton back", () -> command.accept("tp @e[type=minecraft:skeleton] 0 150 -2 180 0"), 20);
        add.add("possess again", () -> aimAndUse(mc), 30);
        add.add("check possessed again", () -> onServer(sp -> check("possessed again", PossessionData.of(sp).active())), 5);
        add.add("kill host", () -> command.accept("kill @e[type=minecraft:skeleton]"), 30);
        add.add("check returned", () -> {
            onServer(sp -> {
                check("player alive after host died", sp.isAlive() && sp.getHealth() >= sp.getMaxHealth());
                check("returned from dead host", !PossessionData.of(sp).active() && sp.getVehicle() == null);
                check("back in body after death", sp.position().distanceTo(new Vec3(0.5, 150, -8.5)) < 1.5);
                check("body removed after death", bodies(sp).isEmpty());
            });
            check("client camera back after death", mc.getCameraEntity() == mc.player);
        }, 5);
        add.add("shot returned", () -> shot.accept("53_possession_returned"), 50);

        add.add("summon blaze", () -> {
            command.accept("kill @e[type=!minecraft:player,type=!minecraft:mannequin,distance=..40]");
            command.accept("summon minecraft:blaze 0 150 -2");
            fillSomnium();
        }, 20);
        add.add("possess blaze", () -> aimAndUse(mc), 20);
        add.add("blaze fireball", () -> aimAndUse(mc), 3);
        add.add("check fireball", () -> onServer(sp -> {
            check("possessed blaze", sp.getVehicle() instanceof Blaze);
            check("blaze shot a fireball", !sp.level().getEntitiesOfClass(SmallFireball.class, new AABB(-12, 145, -12, 12, 162, 12)).isEmpty());
        }), 2);
        add.add("shot blaze", () -> shot.accept("54_possession_blaze_fireball"), 10);
        add.add("leave blaze", () -> mc.options.keyShift.setDown(true), 6);
        add.add("release sneak 2", () -> mc.options.keyShift.setDown(false), 20);
        add.add("check left blaze", () -> onServer(sp -> check("left the blaze", !PossessionData.of(sp).active())), 40);

        add.add("summon zombie", () -> {
            command.accept("kill @e[type=!minecraft:player,type=!minecraft:mannequin,distance=..40]");
            command.accept("summon minecraft:zombie 0 150 -2");
            fillSomnium();
            command.accept("summon minecraft:cow 0 150 -1 {NoAI:1b,PersistenceRequired:1b}");
        }, 3);
        add.add("possess zombie", () -> {
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] ring on cooldown: {}", mc.player.getCooldowns().isOnCooldown(mc.player.getMainHandItem()));
            aimAndUse(mc);
        }, 20);
        add.add("look up", () -> mc.player.setXRot(-60.0F), 3);
        add.add("left click looking up", () -> KeyMapping.click(mc.options.keyAttack.getKey()), 10);
        add.add("check still connected", () -> check("no self attack / still connected", mc.getConnection() != null && mc.player != null), 2);
        add.add("look at cow", () -> mc.player.setXRot(35.0F), 3);
        add.add("left click on cow", () -> KeyMapping.click(mc.options.keyAttack.getKey()), 10);
        add.add("check melee", () -> onServer(sp -> {
            check("possessed zombie", sp.getVehicle() instanceof Zombie);
            check("zombie hurt the cow", !sp.level().getEntitiesOfClass(Cow.class, new AABB(-12, 145, -12, 12, 162, 12), cow -> cow.getHealth() < cow.getMaxHealth()).isEmpty());
            check("player not hurt", sp.getHealth() >= sp.getMaxHealth());
        }), 5);
        add.add("shot zombie", () -> shot.accept("55_possession_zombie_hud"), 10);
        add.add("leave zombie", () -> mc.options.keyShift.setDown(true), 6);
        add.add("release sneak 3", () -> mc.options.keyShift.setDown(false), 20);
        add.add("check left zombie", () -> onServer(sp -> check("left the zombie", !PossessionData.of(sp).active())), 5);
        add.add("possession result", () -> onServer(sp -> TheabyssDevTest.LOGGER.info(
            "[ABYSS-AUTOTEST] RESULT possession {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL", checks,
            failures.isEmpty() ? "" : ", failed: " + failures)), 10);
    }

    private static void fillSomnium() {
        onServer(sp -> TheabyssModVariables.updatePlayerVariables(sp, vars -> {
            vars.Mana = 100.0;
            vars.syncPlayerVariables(sp);
        }));
    }

    private static void aimAndUse(Minecraft mc) {
        mc.player.setYRot(0.0F);
        mc.player.setXRot(0.0F);
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
    }

    private static List<Mannequin> bodies(ServerPlayer sp) {
        return sp.level().getEntitiesOfClass(Mannequin.class, new AABB(-12, 145, -12, 12, 162, 12), m -> m.entityTags().contains("theabyss.possession_body"));
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] possession check {}: {}", name, ok ? "ok" : "FAILED");
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
