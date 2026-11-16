package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.astral.AstralData;
import net.yezon.theabyss.network.TheabyssModVariables;

final class FxAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static double mana;
    private static final List<net.minecraft.world.entity.Mob> timeMobs = new ArrayList<>();
    private static final List<float[]> timePoses = new ArrayList<>();

    private FxAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("abyss sky", () -> {
            command.accept("time set noon");
            command.accept("execute in theabyss:the_abyss run spreadplayers 0 0 0 64 false @s");
        }, 200);
        add.add("abyss look up", () -> command.accept("tp @s ~ ~ ~ 30 -35"), 40);
        add.add("shot abyss sky", () -> shot.accept("58a_abyss_nebula_sky"), 10);
        add.add("abyss horizon", () -> command.accept("tp @s ~ ~ ~ 120 -5"), 30);
        add.add("shot abyss horizon", () -> shot.accept("58b_abyss_nebula_horizon"), 10);
        add.add("abyss night", () -> {
            command.accept("time set midnight");
            command.accept("tp @s ~ ~ ~ 0 -45");
        }, 40);
        add.add("shot abyss night", () -> shot.accept("58c_abyss_nebula_night"), 10);
        add.add("abyss lightning", () -> {
            command.accept("tp @s ~ ~ ~ 0 -10");
            command.accept("summon minecraft:lightning_bolt ~12 ~ ~30");
        }, 1);
        add.add("check red flash", () -> check("abyss lightning: red flash active", net.yezon.theabyss.weather.client.AbyssLightningClient.flash() > 0.5F), 0);
        add.add("hold flash", () -> mc.level.setSkyFlashTime(30), 4);
        add.add("shot abyss lightning", () -> shot.accept("58g_abyss_red_lightning"), 30);
        add.add("abyss fog view", () -> command.accept("tp @s ~ ~ ~ 200 8"), 30);
        add.add("shot abyss fog", () -> shot.accept("58d_volumetric_fog"), 10);
        add.add("abyss fog view 2", () -> command.accept("tp @s ~ ~ ~ 290 3"), 30);
        add.add("shot abyss fog 2", () -> shot.accept("58e_volumetric_fog_2"), 10);
        add.add("abyss grading on", () -> {
            net.yezon.theabyss.configuration.ConfigConfiguration.ABYSS_HUE.set(25);
            net.yezon.theabyss.configuration.ConfigConfiguration.ABYSS_SATURATION.set(20);
        }, 10);
        add.add("check abyss grading", () -> check("abyss colour grading on", mc.player.getActivePostEffects().contains(net.yezon.theabyss.client.fx.ScreenFx.ABYSS_GRADE_EFFECT)), 2);
        add.add("shot abyss grading", () -> shot.accept("58f_abyss_colour_grading"), 10);
        add.add("abyss grading off", () -> {
            net.yezon.theabyss.configuration.ConfigConfiguration.ABYSS_HUE.set(0);
            net.yezon.theabyss.configuration.ConfigConfiguration.ABYSS_SATURATION.set(0);
        }, 10);
        add.add("check abyss grading off", () -> check("abyss colour grading off at 0", !mc.player.getActivePostEffects().contains(net.yezon.theabyss.client.fx.ScreenFx.ABYSS_GRADE_EFFECT)), 2);
        add.add("fx arena", () -> {
            command.accept("execute in minecraft:overworld run forceload add -12 -12 12 12");
            command.accept("execute in minecraft:overworld run fill -12 199 -12 12 199 12 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -12 200 -12 12 210 12 minecraft:air");
            command.accept("time set noon");
            command.accept("weather clear");
        }, 60);
        add.add("fx goto arena", () -> command.accept("execute in minecraft:overworld run tp @s 0 200 -6 0 10"), 100);
        add.add("glow plants", () -> {
            command.accept("time set midnight");
            command.accept("fill -6 199 0 6 199 0 theabyss:dirt");
            String[] plants = {"blue_cave_shroom", "glow_shroom", "ralom_plant", "rub_shroom", "toxic_sun_flower", "exolius_plant", "hollow_crystal_ore"};
            for (int k = 0; k < plants.length; k++) {
                command.accept("setblock " + (-6 + k * 2) + " 200 0 theabyss:" + plants[k]);
            }
        }, 40);
        add.add("shot plant glow", () -> shot.accept("59_plants_night_vanilla"), 10);
        add.add("light engine on", () -> {
            net.yezon.theabyss.configuration.ConfigConfiguration.COLORED_LIGHT_ENGINE.set(true);
            command.accept("fill -8 200 3 8 204 3 minecraft:stone");
            command.accept("setblock -3 200 1 minecraft:stone");
            command.accept("setblock -3 201 1 minecraft:stone");
        }, 20);
        add.add("shot light fading in", () -> shot.accept("59a_colored_light_fading_in"), 40);
        add.add("light engine active", () -> check("coloured light engine: volume built and post chain on",
            net.yezon.theabyss.client.fx.ColoredLight.active()
                && mc.player.getActivePostEffects().contains(net.yezon.theabyss.client.fx.ScreenFx.LIGHT_ENGINE_EFFECT)), 2);
        add.add("shot light engine", () -> shot.accept("59b_colored_light_engine"), 10);
        add.add("light engine close", () -> command.accept("tp @s 0 200 -2 0 25"), 30);
        add.add("shot light engine close", () -> shot.accept("59c_colored_light_engine_close"), 10);
        add.add("light engine off", () -> {
            net.yezon.theabyss.configuration.ConfigConfiguration.COLORED_LIGHT_ENGINE.set(false);
            command.accept("fill -8 200 1 8 204 3 minecraft:air");
            command.accept("tp @s 0 200 -6 0 10");
        }, 20);
        add.add("light engine inactive", () -> check("coloured light engine off again", !net.yezon.theabyss.client.fx.ColoredLight.active()), 2);
        add.add("glow plants away", () -> {
            command.accept("fill -6 200 0 6 200 0 minecraft:air");
            command.accept("fill -6 199 0 6 199 0 minecraft:stone");
            command.accept("time set noon");
        }, 10);
        add.add("fx mobs", () -> {
            command.accept("summon minecraft:husk 0 200 3 {PersistenceRequired:1b}");
            command.accept("summon minecraft:cow 3 200 4 {NoAI:1b,PersistenceRequired:1b}");
            command.accept("item replace entity @s weapon.mainhand with theabyss:ring_of_spectral");
            command.accept("item replace entity @s hotbar.1 with minecraft:grass_block");
            command.accept("item replace entity @s hotbar.2 with theabyss:unstable_obsidian");
            command.accept("item replace entity @s hotbar.3 with minecraft:oak_log");
            command.accept("gamemode survival");
            command.accept("effect give @s minecraft:resistance 600 4 true");
            fillSomnium();
        }, 60);
        add.add("husk sees player", () -> onServer(sp -> check("baseline: husk targets the player", husk(sp) != null && husk(sp).getTarget() == sp)), 2);

        add.add("enter astral", () -> use(mc), 10);
        add.add("shot astral fade", () -> shot.accept("60a_astral_fading_in"), 30);
        add.add("check astral", () -> onServer(sp -> {
            check("astral state", AstralData.isAstral(sp));
            check("astral player silent", sp.isSilent());
            check("crossing cost paid", TheabyssModVariables.getPlayerVariables(sp).Mana <= 100.0 - 15.0 + 0.5);
            check("husk lost the player", husk(sp) == null || husk(sp).getTarget() != sp);
            check("client sees itself astral", AstralData.isAstral(mc.player));
        }), 2);
        add.add("shot astral arena", () -> shot.accept("60_astral_arena"), 10);
        add.add("astral look up", () -> mc.player.setXRot(-25.0F), 5);
        add.add("shot astral sky", () -> shot.accept("60b_astral_sky"), 10);
        add.add("astral look down", () -> mc.player.setXRot(35.0F), 5);
        add.add("shot astral ground", () -> shot.accept("60c_astral_ground"), 10);
        add.add("astral turn", () -> mc.player.setYRot(mc.player.getYRot() + 40.0F), 5);
        add.add("shot astral turned", () -> shot.accept("60d_astral_ground_turned"), 10);
        add.add("astral turn back", () -> mc.player.setYRot(mc.player.getYRot() - 40.0F), 3);
        add.add("astral look ahead", () -> mc.player.setXRot(10.0F), 3);
        add.add("attack from the other side", () -> onServer(sp -> {
            Husk husk = husk(sp);
            if (husk != null) {
                sp.attack(husk);
                check("attack does nothing", husk.getHealth() >= husk.getMaxHealth());
            }
        }), 5);
        add.add("break block", () -> {
            command.accept("setblock 2 200 -4 minecraft:stone");
            onServer(sp -> mana = TheabyssModVariables.getPlayerVariables(sp).Mana);
        }, 3);
        add.add("break block 2", () -> onServer(sp -> {
            boolean broken = sp.gameMode.destroyBlock(new BlockPos(2, 200, -4));
            double now = TheabyssModVariables.getPlayerVariables(sp).Mana;
            check("block can be broken", broken);
            check("breaking costs Somnium", now <= mana - 1.5);
        }), 5);
        add.add("place chest", () -> command.accept("setblock 0 200 -3 minecraft:chest"), 5);
        add.add("open chest", () -> {
            onServer(sp -> mana = TheabyssModVariables.getPlayerVariables(sp).Mana);
            BlockPos pos = new BlockPos(0, 200, -3);
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
        }, 10);
        add.add("check chest", () -> {
            check("chest GUI opens", mc.player.containerMenu instanceof ChestMenu);
            onServer(sp -> check("opening a GUI costs Somnium", TheabyssModVariables.getPlayerVariables(sp).Mana <= mana - 2.5));
            mc.player.closeContainer();
        }, 10);
        add.add("astral forest", () -> command.accept("execute in theabyss:the_abyss run spreadplayers 40 56 0 8 false @s"), 200);
        add.add("astral look", () -> command.accept("tp @s ~ ~ ~ 0 20"), 40);
        add.add("shot astral forest", () -> shot.accept("61_astral_blaru_forest"), 10);
        add.add("leave astral", () -> use(mc), 3);
        add.add("shot astral leave", () -> shot.accept("62_astral_leaving_flash"), 30);
        add.add("check left astral", () -> onServer(sp -> {
            check("left the astral plane", !AstralData.isAstral(sp));
            check("audible again", !sp.isSilent());
        }), 5);
        add.add("shot gui after astral", () -> shot.accept("62b_hotbar_after_astral"), 20);

        add.add("time ring", () -> {
            command.accept("item replace entity @s weapon.mainhand with theabyss:ring_of_time");
            command.accept("time set noon");
            command.accept("execute in minecraft:overworld run tp @s 0 200 -4 0 5");
            command.accept("execute in minecraft:overworld run summon minecraft:zombie 2 200 6 {PersistenceRequired:1b}");
            command.accept("execute in minecraft:overworld run summon minecraft:pig -3 200 8 {PersistenceRequired:1b}");
            command.accept("execute in minecraft:overworld run setblock 4 200 3 minecraft:red_wool");
            command.accept("execute in minecraft:overworld run setblock -4 200 3 minecraft:lime_wool");
            fillSomnium();
        }, 30);
        add.add("time mana before", () -> onServer(sp -> {
            mana = TheabyssModVariables.getPlayerVariables(sp).Mana;
            timeMobs.clear();
            timeMobs.addAll(sp.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, new AABB(sp.blockPosition()).inflate(16)));
        }), 2);
        add.add("use ring of time", () -> use(mc), 8);
        add.add("shot time spreading", () -> shot.accept("65a_time_stop_spreading"), 14);
        add.add("shot time spreading far", () -> shot.accept("65b_time_stop_spreading_far"), 40);
        add.add("check time stop", () -> {
            onServer(sp -> {
                check("time stop: caster has the Time Stop effect", sp.hasEffect(net.yezon.theabyss.init.TheabyssModMobEffects.TIME_STOP_POTION_EFFECT.getDelegate()));
                check("time stop: 80 Somnium paid", Math.abs(mana - TheabyssModVariables.getPlayerVariables(sp).Mana - 80.0) < 0.01);
                check("time stop: no cooldown while time stands still (ring can end it)", !sp.getCooldowns().isOnCooldown(sp.getMainHandItem()));
                var mobs = timeMobs;
                timePoses.clear();
                mobs.forEach(m -> timePoses.add(new float[] {m.getYRot(), m.getYHeadRot(), m.yBodyRot, m.getXRot()}));
                check("time stop: nearby mobs frozen", !mobs.isEmpty() && mobs.stream().allMatch(m -> m.hasEffect(net.yezon.theabyss.init.TheabyssModMobEffects.TIME_STOP_POTION_IRATOR.getDelegate())));
                check("time stop: frozen mobs do not move", mobs.stream().allMatch(m -> m.getDeltaMovement().horizontalDistanceSqr() < 1.0E-6));
            });
            check("time stop: screen effect on", mc.player.getActivePostEffects().contains(net.yezon.theabyss.client.fx.ScreenFx.TIME_STOP_EFFECT));
        }, 2);
        add.add("shot time stopped", () -> shot.accept("65c_time_stopped"), 30);
        add.add("check no turning", () -> onServer(sp -> {
            boolean still = !timeMobs.isEmpty();
            for (int i = 0; i < timeMobs.size(); i++) {
                var m = timeMobs.get(i);
                float[] before = timePoses.get(i);
                still &= before[0] == m.getYRot() && before[1] == m.getYHeadRot() && before[2] == m.yBodyRot && before[3] == m.getXRot();
            }
            check("time stop: frozen mobs do not turn", still);
        }), 2);
        add.add("time look around", () -> command.accept("tp @s 0 200 -4 40 15"), 10);
        add.add("shot time stopped turned", () -> shot.accept("65d_time_stopped_turned"), 10);
        add.add("time resumes", () -> use(mc), 12);
        add.add("shot time resuming", () -> shot.accept("65e_time_resuming"), 40);
        add.add("check time resumed", () -> {
            check("time stop: screen effect off again", !mc.player.getActivePostEffects().contains(net.yezon.theabyss.client.fx.ScreenFx.TIME_STOP_EFFECT));
            onServer(sp -> {
                check("time stop: ended by right-click", !sp.hasEffect(net.yezon.theabyss.init.TheabyssModMobEffects.TIME_STOP_POTION_EFFECT.getDelegate()));
                check("time stop: mobs free again", timeMobs.stream().noneMatch(m -> m.hasEffect(net.yezon.theabyss.init.TheabyssModMobEffects.TIME_STOP_POTION_IRATOR.getDelegate())));
                check("time stop: ring on cooldown afterwards", sp.getCooldowns().isOnCooldown(sp.getMainHandItem()));
            });
        }, 2);
        add.add("time cleanup", () -> {
            command.accept("kill @e[type=minecraft:zombie]");
            command.accept("kill @e[type=minecraft:pig]");
            command.accept("time set noon");
            command.accept("execute in theabyss:the_abyss run spreadplayers 0 0 0 64 false @s");
        }, 100);

        add.add("dream shifter", () -> {
            command.accept("item replace entity @s weapon.mainhand with theabyss:dream_shifter");
            command.accept("gamemode creative");
        }, 20);
        add.add("shift", () -> use(mc), 10);
        add.add("shot shift leaving", () -> shot.accept("63a_dreamshift_leaving"), 9);
        add.add("shot shift dark", () -> shot.accept("63b_dreamshift_darkness"), 25);
        add.add("shot shift rift", () -> shot.accept("63c_dreamshift_rift_opening"), 15);
        add.add("shot shift open", () -> shot.accept("63d_dreamshift_rift_wide"), 50);
        add.add("check shift", () -> {
            check("arrived in the overworld", mc.level.dimension() == Level.OVERWORLD);
            check("dream shifter cooldown visible after arriving", mc.player.getCooldowns().isOnCooldown(mc.player.getMainHandItem()));
        }, 5);

        add.add("ender ring", () -> command.accept("item replace entity @s weapon.mainhand with theabyss:ring_of_ender_chest"), 10);
        add.add("open ender chest", () -> use(mc), 15);
        add.add("check ender chest", () -> check("ender chest opens", mc.player.containerMenu instanceof ChestMenu), 2);
        add.add("shot ender chest", () -> shot.accept("64_ring_of_enderchest"), 10);
        add.add("close ender chest", () -> mc.player.closeContainer(), 10);
        add.add("fx result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT fx {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 10);
    }

    private static void use(Minecraft mc) {
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
    }

    private static Husk husk(ServerPlayer sp) {
        return sp.level().getEntitiesOfClass(Husk.class, new AABB(sp.blockPosition()).inflate(20)).stream().findFirst().orElse(null);
    }

    private static void fillSomnium() {
        onServer(sp -> TheabyssModVariables.updatePlayerVariables(sp, vars -> {
            vars.Mana = 100.0;
            vars.syncPlayerVariables(sp);
        }));
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] fx check {}: {}", name, ok ? "ok" : "FAILED");
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
