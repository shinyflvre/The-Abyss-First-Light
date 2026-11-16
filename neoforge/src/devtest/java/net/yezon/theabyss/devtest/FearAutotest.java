package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.client.fx.FearClient;
import net.yezon.theabyss.events.FearPotionStartedappliedEvents;
import net.yezon.theabyss.init.TheabyssModMobEffects;

final class FearAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static final List<String> seen = new ArrayList<>();
    private static String lastStage = "";
    private static float healthBefore;
    private static boolean shotPeripheral;
    private static boolean shotLook;
    private static boolean shotHunt;
    private static boolean shotScare;
    private static boolean flee;
    private static float healthAfter = -1.0F;
    private static Vec3 fleeDirection;
    private static int musicDuringFear;
    private static int musicSamples;
    private static final List<String> heard = new ArrayList<>();
    private static final net.minecraft.client.sounds.SoundEventListener LISTENER = (sound, events, range) -> {
        String id = sound.getIdentifier().toString();
        if (id.startsWith("theabyss:fear_")) {
            heard.add(id + "=" + (sound.getSound() != null ? sound.getSound().getLocation() : "?"));
        }
    };

    private FearAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        add.add("fear arena", () -> {
            command.accept("gamemode survival @s");
            command.accept("gamerule spawn_mobs false");
            command.accept("execute in theabyss:the_abyss run forceload add -40 -40 40 40");
            command.accept("execute in theabyss:the_abyss run fill -30 149 -30 30 149 0 theabyss:nightgrove_moss");
            command.accept("execute in theabyss:the_abyss run fill -30 149 1 30 149 30 theabyss:nightgrove_moss");
            command.accept("execute in theabyss:the_abyss run fill -30 150 -30 30 156 0 minecraft:air");
            command.accept("execute in theabyss:the_abyss run fill -30 150 1 30 156 30 minecraft:air");
            command.accept("execute in theabyss:the_abyss run tp @s 0.5 150 0.5 0 5");
            command.accept("effect give @s minecraft:resistance 9999 4 true");
        }, 100);
        for (int round = 0; round < 2; round++) {
            boolean escape = round == 1;
            add.add("fear start " + round, () -> {
                seen.clear();
                lastStage = "";
                flee = escape;
                healthAfter = -1.0F;
                fleeDirection = null;
                musicDuringFear = 0;
                musicSamples = 0;
                heard.clear();
                Minecraft.getInstance().getSoundManager().removeListener(LISTENER);
                Minecraft.getInstance().getSoundManager().addListener(LISTENER);
                shotPeripheral = shotLook = shotHunt = shotScare = false;
                command.accept("effect clear @s minecraft:resistance");
                command.accept("gamemode " + (escape ? "creative" : "survival") + " @s");
                command.accept("execute in theabyss:the_abyss run tp @s 0.5 150 0.5 0 5");
                onServer(sp -> {
                    sp.setHealth(sp.getMaxHealth());
                    sp.getFoodData().setFoodLevel(17);
                    sp.getFoodData().setSaturation(0.0F);
                    sp.addEffect(new MobEffectInstance(TheabyssModMobEffects.FEAR.getDelegate(), FearPotionStartedappliedEvents.DURATION, 0));
                });
            }, 60);
            add.add("fear shot calm " + round, () -> {
                if (!escape) {
                    shot.accept("123_fear_calm");
                }
            }, 2);
            add.add("fear health " + round, () -> healthBefore = Minecraft.getInstance().player.getHealth(), 1);
            for (int t = 0; t < 900; t++) {
                add.add("fear watch " + round + " " + t, () -> watch(shot, escape), 1);
            }
            add.add("fear check " + round, () -> {
                Minecraft mc = Minecraft.getInstance();
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] fear stages round {}: {}", escape ? "escape" : "caught", seen);
                check("shade appeared (" + seen + ")", seen.contains("SHADE"));
                check("hunt started", seen.contains("HUNT"));
                check("no music while afraid (" + musicDuringFear + " of " + musicSamples + " samples)", musicSamples > 50 && musicDuringFear == 0);
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] fear sounds heard {}", heard);
                check("fear ambience played once", heard.stream().filter(h -> h.startsWith("theabyss:fear_ambience=theabyss:fear/fear_ambience")).count() == 1);
                check("fear ambience stopped afterwards", field("ambience") == null || !Minecraft.getInstance().getSoundManager().isActive(
                    (net.minecraft.client.resources.sounds.SoundInstance)field("ambience")));
                if (escape) {
                    check("escaped without a jumpscare", !seen.contains("JUMPSCARE"));
                    check("no jumpscare sound when escaping", heard.stream().noneMatch(h -> h.startsWith("theabyss:fear_jumpscare")));
                } else {
                    check("caught -> jumpscare", seen.contains("JUMPSCARE"));
                    check("one of the 4 jumpscare sounds played", heard.stream().anyMatch(h -> h.matches("theabyss:fear_jumpscare=theabyss:fear/jumpscare_0[1-4]")));
                    check("caught costs 2 hearts (" + healthBefore + " -> " + healthAfter + ")", Math.abs(healthBefore - healthAfter - 4.0F) < 0.01F);
                    check("fear effect removed after the catch", !mc.player.hasEffect(TheabyssModMobEffects.FEAR.getDelegate()));
                }
            }, 2);
            add.add("fear clear " + round, () -> command.accept("effect clear @s theabyss:fear"), 80);
        }
        add.add("fear result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT fear {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL", checks,
            failures.isEmpty() ? "" : ", failed: " + failures), 2);
    }

    private static void watch(Consumer<String> shot, boolean escape) {
        Minecraft mc = Minecraft.getInstance();
        String stage = String.valueOf(field("stage"));
        if (!stage.equals(lastStage)) {
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] fear stage {} (intensity {})", stage, FearClient.intensity());
            lastStage = stage;
            if (!seen.contains(stage)) {
                seen.add(stage);
            }
        }
        if (!stage.equals("NONE") && !stage.equals("CALM")) {
            musicSamples++;
            try {
                var f = net.minecraft.client.sounds.MusicManager.class.getDeclaredField("currentMusic");
                f.setAccessible(true);
                if (f.get(mc.getMusicManager()) != null) {
                    musicDuringFear++;
                }
            } catch (ReflectiveOperationException e) {
                musicDuringFear = -1;
            }
        }
        Vec3 shade = (Vec3)field("shadePos");
        int stageTicks = (Integer)field("stageTicks");
        if (stage.equals("SHADE") && shade != null) {
            if (!escape && !shotPeripheral && stageTicks >= 6) {
                shot.accept("123_fear_shade_edge");
                shotPeripheral = true;
            }
            if (stageTicks >= 14) {
                lookAt(mc, shade.add(0.0, 1.6, 0.0));
            }
            if (!escape && !shotLook && stageTicks >= 16) {
                shot.accept("123_fear_shade_looked_at");
                shotLook = true;
            }
        }
        if ((stage.equals("WINDUP") || stage.equals("HUNT")) && shade != null) {
            lookAt(mc, shade.add(0.0, 1.6, 0.0));
            if (!escape && !shotHunt && stage.equals("HUNT") && stageTicks >= 14) {
                shot.accept("123_fear_hunt");
                shotHunt = true;
            }
            if (escape && stage.equals("HUNT")) {
                if (fleeDirection == null) {
                    fleeDirection = mc.player.position().subtract(shade).multiply(1.0, 0.0, 1.0).normalize();
                    mc.player.getAbilities().flying = true;
                }
                Vec3 to = mc.player.position().add(fleeDirection.scale(0.56));
                mc.player.setPos(to.x, 151.0, to.z);
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] fear flee tick {} distance {}", stageTicks, mc.player.position().distanceTo(shade));
            }
        }
        if (stage.equals("JUMPSCARE") && !escape && !shotScare && stageTicks >= 2) {
            shot.accept("123_fear_jumpscare");
            shotScare = true;
        }
        if (stage.equals("RESOLVED") && !escape && healthAfter < 0.0F && seen.contains("JUMPSCARE")) {
            healthAfter = mc.player.getHealth();
        }
    }

    private static void lookAt(Minecraft mc, Vec3 target) {
        Vec3 eye = mc.player.getEyePosition();
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        mc.player.setYRot((float)(Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F);
        mc.player.setXRot((float)(-(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * Mth.RAD_TO_DEG)));
        mc.player.yRotO = mc.player.getYRot();
        mc.player.xRotO = mc.player.getXRot();
    }

    private static Object field(String name) {
        try {
            var f = FearClient.class.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(null);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] fear check {}: {}", name, ok ? "ok" : "FAILED");
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
