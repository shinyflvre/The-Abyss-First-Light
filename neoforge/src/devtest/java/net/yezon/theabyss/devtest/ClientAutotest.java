package net.yezon.theabyss.devtest;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.FileUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.gui.LoadingErrorScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public final class ClientAutotest {
    private static final Deque<Step> steps = new ArrayDeque<>();
    private static int wait = 0;
    private static boolean worldRequested = false;
    private static boolean scriptBuilt = false;
    private static int inWorldTicks = 0;
    private static String pendingShot = null;
    private static int pendingShotDelay = 0;

    private record Step(String name, Runnable action, int waitAfter) {
    }

    private ClientAutotest() {
    }

    static boolean enabled() {
        return Boolean.getBoolean("theabyss.devtest.client");
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!enabled()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        background(mc);
        if (mc.level == null || mc.player == null) {
            handleMenus(mc);
            return;
        }
        if (mc.screen instanceof ConfirmScreen confirm) {
            accept(confirm);
        }
        mc.options.pauseOnLostFocus = false;
        if (mc.screen instanceof PauseScreen) {
            mc.setScreen(null);
        }
        if (!scriptBuilt) {
            if (++inWorldTicks < 200) {
                return;
            }
            buildScript();
            scriptBuilt = true;
        }
        if (pendingShot != null && --pendingShotDelay <= 0) {
            Screenshot.grab(mc.gameDirectory, "devtest_" + pendingShot + ".png", mc.getMainRenderTarget(), msg -> {});
            pendingShot = null;
        }
        if (wait > 0) {
            wait--;
            return;
        }
        Step step = steps.poll();
        if (step == null) {
            return;
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] step {}", step.name());
        try {
            step.action().run();
        } catch (Throwable t) {
            TheabyssDevTest.LOGGER.error("[ABYSS-AUTOTEST] step {} failed", step.name(), t);
        }
        wait = step.waitAfter();
    }

    private static boolean iconified;

    private static void background(Minecraft mc) {
        if (iconified || !Boolean.getBoolean("theabyss.devtest.background")) {
            return;
        }
        iconified = true;
        try {
            Class.forName("org.lwjgl.glfw.GLFW").getMethod("glfwIconifyWindow", long.class).invoke(null, mc.getWindow().getWindow());
        } catch (ReflectiveOperationException | RuntimeException e) {
            TheabyssDevTest.LOGGER.warn("[ABYSS-AUTOTEST] could not minimize the window", e);
        }
        mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(0.0);
    }

    private static void handleMenus(Minecraft mc) {
        if (mc.screen instanceof LoadingErrorScreen loading) {
            proceedPastWarnings(loading);
            return;
        }
        if (mc.screen instanceof ConfirmScreen confirm) {
            accept(confirm);
            return;
        }
        if (!worldRequested && mc.screen instanceof TitleScreen title && multiplayerAddress() != null) {
            worldRequested = true;
            String address = multiplayerAddress();
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] connecting to dedicated server {}", address);
            ConnectScreen.startConnecting(title, mc, ServerAddress.parseString(address), new ServerData("abyss-devtest", address, ServerData.Type.OTHER), false, null);
            return;
        }
        if (!worldRequested && mc.screen instanceof TitleScreen) {
            worldRequested = true;
            try {
                String folder = FileUtil.findAvailableName(mc.getLevelSource().getBaseDir(), "abyss-devtest", "");
                LevelSettings settings = new LevelSettings(
                    "abyss-devtest", GameType.CREATIVE, false, net.minecraft.world.Difficulty.NORMAL, true, new net.minecraft.world.level.GameRules(), WorldDataConfiguration.DEFAULT
                );
                mc.createWorldOpenFlows().createFreshLevel(folder, settings, new net.minecraft.world.level.levelgen.WorldOptions(20261008L, true, false), WorldPresets::createNormalWorldDimensions, mc.screen);
            } catch (Exception e) {
                TheabyssDevTest.LOGGER.error("[ABYSS-AUTOTEST] could not create world", e);
            }
        }
    }

    private static void proceedPastWarnings(LoadingErrorScreen screen) {
        try {
            Field errors = LoadingErrorScreen.class.getDeclaredField("modLoadErrors");
            errors.setAccessible(true);
            if (!((List<?>) errors.get(screen)).isEmpty()) {
                TheabyssDevTest.LOGGER.error("[ABYSS-AUTOTEST] RESULT client FAIL: mod loading errors {}", errors.get(screen));
                Minecraft.getInstance().stop();
                return;
            }
            Field next = LoadingErrorScreen.class.getDeclaredField("nextScreenTask");
            next.setAccessible(true);
            TheabyssDevTest.LOGGER.warn("[ABYSS-AUTOTEST] mod loading warnings shown, proceeding");
            ((Runnable) next.get(screen)).run();
        } catch (ReflectiveOperationException e) {
            TheabyssDevTest.LOGGER.error("[ABYSS-AUTOTEST] cannot handle loading screen", e);
        }
    }

    private static void accept(ConfirmScreen screen) {
        try {
            Field f = ConfirmScreen.class.getDeclaredField("callback");
            f.setAccessible(true);
            ((BooleanConsumer) f.get(screen)).accept(true);
        } catch (ReflectiveOperationException e) {
            TheabyssDevTest.LOGGER.error("[ABYSS-AUTOTEST] cannot confirm screen", e);
        }
    }

    private static String multiplayerAddress() {
        return System.getProperty("theabyss.devtest.mp");
    }

    private static void command(String cmd) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null) {
            mc.player.connection.sendCommand(cmd);
            return;
        }
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp != null) {
                server.getCommands().performPrefixedCommand(sp.createCommandSourceStack().withPermission(4), cmd);
            }
        });
    }

    private static void shot(String name) {
        Minecraft.getInstance().gui.getChat().clearMessages(false);
        pendingShot = name;
        pendingShotDelay = 2;
    }

    private static void add(String name, Runnable action, int waitAfter) {
        steps.add(new Step(name, action, waitAfter));
    }

    private static String scenario() {
        return System.getProperty("theabyss.devtest.scenario", "all");
    }

    private static void buildScript() {
        Minecraft mc = Minecraft.getInstance();
        add("creative", () -> command("gamemode creative @s"), 20);
        if (scenario().equals("fx")) {
            FxAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("book")) {
            BookAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("eagle")) {
            EagleAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("abyssaur")) {
            AbyssaurAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("gauntlet")) {
            GauntletAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("lushblocks")) {
            LushBlocksAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("blossom")) {
            BlossomAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("modlist")) {
            add("open mod list", () -> mc.setScreen(new net.neoforged.neoforge.client.gui.ModListScreen(mc.screen)), 40);
            add("search abyss", () -> {
                try {
                    var f = net.neoforged.neoforge.client.gui.ModListScreen.class.getDeclaredField("search");
                    f.setAccessible(true);
                    ((net.minecraft.client.gui.components.EditBox)f.get(mc.screen)).setValue("other side");
                } catch (ReflectiveOperationException e) {
                    TheabyssDevTest.LOGGER.error("[ABYSS-AUTOTEST] mod list search failed", e);
                }
            }, 20);
            add("shot list", () -> shot("128_modlist"), 10);
            add("select abyss", () -> {
                try {
                    var f = net.neoforged.neoforge.client.gui.ModListScreen.class.getDeclaredField("modList");
                    f.setAccessible(true);
                    var list = (net.minecraft.client.gui.components.AbstractSelectionList<?>)f.get(mc.screen);
                    Object first = ((java.util.List<?>)list.children()).get(0);
                    java.lang.reflect.Method m = null;
                    for (var candidate : net.minecraft.client.gui.components.AbstractSelectionList.class.getDeclaredMethods()) {
                        if (candidate.getName().equals("setSelected") && candidate.getParameterCount() == 1) {
                            m = candidate;
                        }
                    }
                    m.setAccessible(true);
                    ((net.neoforged.neoforge.client.gui.ModListScreen)mc.screen).setSelected((net.neoforged.neoforge.client.gui.widget.ModListWidget.ModEntry)first);
                    m.invoke(list, first);
                } catch (ReflectiveOperationException | RuntimeException e) {
                    TheabyssDevTest.LOGGER.error("[ABYSS-AUTOTEST] mod list select failed", e);
                }
            }, 20);
            add("shot selected", () -> shot("128_modlist_selected"), 10);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("abyssarch")) {
            FrostArchAutotest.abyss();
            FrostArchAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("frostarch")) {
            FrostArchAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("frostgen")) {
            FrostGenAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("frost")) {
            FrostAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("ores")) {
            OresAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("fear")) {
            FearAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("rootcracks")) {
            RootCracksAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("farmland")) {
            FarmlandAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("thunder")) {
            ThunderAutotest.addSteps(ClientAutotest::add, ClientAutotest::command);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("pocketfold")) {
            PocketFoldAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().startsWith("lizard")) {
            LizardAutotest.prefix = scenario().contains(":") ? "118_lizard_" + scenario().substring(scenario().indexOf(':') + 1) : "118_lizard";
            LizardAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().startsWith("spawnaudit")) {
            SpawnAuditAutotest.only = scenario().contains(":") ? scenario().substring(scenario().indexOf(':') + 1) : "";
            SpawnAuditAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("nightgrove")) {
            NightgroveAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("chronicles_real")) {
            ChronicleRealAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("chronicles")) {
            ChronicleAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("phantom")) {
            PhantomAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("lush")) {
            LushCaveAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("codexmb")) {
            add("mb arcane", () -> net.yezon.theabyss.client.codex.CodexScreen.openEntry("arcane", 0), 30);
            add("mb shot arcane a", () -> shot("111_codex_multiblock_arcane_a"), 30);
            add("mb shot arcane b", () -> shot("111_codex_multiblock_arcane_b"), 10);
            add("mb arcane 1", () -> net.yezon.theabyss.client.codex.CodexScreen.openEntry("arcane", 1), 20);
            add("mb shot arcane 1", () -> shot("111_codex_arcane_1_powering"), 10);
            add("mb arcane 2", () -> net.yezon.theabyss.client.codex.CodexScreen.openEntry("arcane", 2), 20);
            add("mb shot arcane 2", () -> shot("111_codex_arcane_2_recipes"), 10);
            add("mb energetica 1", () -> net.yezon.theabyss.client.codex.CodexScreen.openEntry("energetica", 1), 20);
            add("mb shot energetica 1", () -> shot("111_codex_energetica_1"), 10);
            add("mb energetica", () -> net.yezon.theabyss.client.codex.CodexScreen.openEntry("energetica", 0), 30);
            add("mb shot energetica", () -> shot("111_codex_multiblock_energetica"), 10);
            add("mb close", () -> mc.setScreen(null), 5);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("energetica")) {
            EnergeticaAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("possclick")) {
            PossessionClickAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("handlight")) {
            HandLightAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("hand")) {
            InfectedHandAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("pocket")) {
            PocketAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("stations")) {
            StationsAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("ringfx")) {
            RingFxAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            SecondaryAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot, "");
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().startsWith("secondary")) {
            String filter = scenario().contains(":") ? scenario().substring(scenario().indexOf(':') + 1) : "";
            SecondaryAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot, filter);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("telekinesis")) {
            TelekinesisAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("crystalring")) {
            CrystalRingAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("roka")) {
            RokaAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("procedures")) {
            ProceduresAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("golem")) {
            GolemAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("codex")) {
            CodexAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("wildflowers")) {
            WildflowerAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("dripstone")) {
            DripstoneAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("woodsets")) {
            WoodSetAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("village")) {
            VillageAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        if (scenario().equals("possession")) {
            PossessionAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
            add("done", () -> {
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
                mc.stop();
            }, 0);
            return;
        }
        add("overworld", () -> shot("00_overworld"), 20);
        add("self test", () -> command("abysstest all"), 400);

        String[] dims = {"the_abyss", "frost_world", "pocket_dimension"};
        for (int d = 0; d < dims.length; d++) {
            String dim = dims[d];
            String prefix = String.format("%02d_%s", 10 + d, dim);
            add("goto " + dim, () -> command("execute in theabyss:" + dim + " run spreadplayers 0 0 0 64 false @s"), 300);
            add("look ahead", () -> command("tp @s ~ ~ ~ 0 10"), 60);
            add("shot " + dim, () -> shot(prefix + "_view"), 20);
            add("look up", () -> command("tp @s ~ ~ ~ 0 -50"), 40);
            add("shot sky " + dim, () -> shot(prefix + "_sky"), 20);
        }

        add("arena load", () -> command("execute in theabyss:the_abyss run forceload add -12 -12 12 12"), 60);
        add("arena floor", () -> command("execute in theabyss:the_abyss run fill -12 149 -12 12 149 12 minecraft:stone"), 10);
        add("arena air", () -> command("execute in theabyss:the_abyss run fill -12 150 -12 12 160 12 minecraft:air"), 10);
        add("arena light", () -> command("execute in theabyss:the_abyss run fill -12 161 -12 12 161 12 minecraft:glowstone"), 20);
        add("goto abyss arena", () -> command("execute in theabyss:the_abyss run tp @s 0 150 -7 0 15"), 200);
        List<String> mobs = new ArrayList<>();
        for (ResourceLocation id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            if (ServerSelfTest.NS.equals(id.getNamespace()) && !id.getPath().contains("projectile") && !id.getPath().equals("magic")) {
                mobs.add(id.toString());
            }
        }
        mobs.sort(String::compareTo);
        for (int g = 0; g * 5 < mobs.size(); g++) {
            List<String> group = mobs.subList(g * 5, Math.min(mobs.size(), g * 5 + 5));
            int gi = g;
            add("mobs " + g, () -> {
                command("tp @s 0 150 -7 0 15");
                for (int k = 0; k < group.size(); k++) {
                    command(String.format("summon %s %d 150 1 {NoAI:1b,PersistenceRequired:1b,Rotation:[180f,0f]}", group.get(k), -6 + k * 3));
                }
            }, 60);
            add("shot mobs " + g, () -> shot(String.format("20_mobs_%02d_%s", gi, String.join("+", group.stream().map(s -> s.substring(9)).toList()))), 10);
            add("clear mobs " + g, () -> command("kill @e[type=!minecraft:player,distance=..30]"), 10);
        }

        add("daylight arena", () -> {
            command("execute in minecraft:overworld run forceload add -12 -12 12 12");
            command("execute in minecraft:overworld run fill -12 199 -12 12 199 12 minecraft:stone");
            command("execute in minecraft:overworld run fill -12 200 -12 12 210 12 minecraft:air");
            command("time set noon");
            command("weather clear");
        }, 60);
        add("goto daylight arena", () -> command("execute in minecraft:overworld run tp @s 0 200 -6 0 20"), 120);
        add("glow mobs", () -> {
            String[] glow = {"minecraft:cow", "theabyss:infceted_cow", "theabyss:infected_creeper", "theabyss:infected_zombie", "theabyss:ice_skeleton", "theabyss:abyssal_lion"};
            for (int k = 0; k < glow.length; k++) {
                command(String.format("summon %s %d 200 1 {NoAI:1b,PersistenceRequired:1b,Rotation:[180f,0f]}", glow[k], -7 + k * 3));
            }
        }, 60);
        add("shot glow mobs", () -> shot("25_glow_mobs_daylight"), 10);
        add("clear glow mobs", () -> command("kill @e[type=!minecraft:player,distance=..30]"), 10);
        add("night vision", () -> command("effect give @s minecraft:night_vision 600 0 true"), 5);
        add("goto blaru forest", () -> command("execute in theabyss:the_abyss run spreadplayers 40 56 0 8 false @s"), 300);
        add("look at forest floor", () -> command("tp @s ~ ~ ~ 0 35"), 60);
        add("shot blaru forest", () -> shot("15_blaru_forest_floor"), 10);
        add("goto abyss plains", () -> command("execute in theabyss:the_abyss run spreadplayers 0 32 0 8 false @s"), 300);
        add("look at plains", () -> command("tp @s ~ ~ ~ 0 35"), 60);
        add("shot abyss plains", () -> shot("16_abyss_plains"), 10);
        add("back to arena", () -> {
            command("effect clear @s minecraft:night_vision");
            command("execute in theabyss:the_abyss run tp @s 0 150 -6 0 25");
        }, 100);

        String[] stations = {"arcane_workbench", "crystal_cutter", "somnium_infuser", "somnium_extractor", "mortar_and_pestle", "research_table"};
        add("station row", () -> {
            command("tp @s 0 150 -6 0 25");
            for (int k = 0; k < stations.length; k++) {
                command("setblock " + (-5 + k * 2) + " 150 -2 theabyss:" + stations[k]);
            }
        }, 40);
        add("shot station row", () -> shot("29_workstation_blocks"), 10);
        add("clear station row", () -> command("fill -6 150 -2 6 150 -2 minecraft:air"), 10);
        for (String station : stations) {
            add("place " + station, () -> command("setblock 0 150 -5 theabyss:" + station), 20);
            add("open " + station, () -> {
                BlockPos pos = new BlockPos(0, 150, -5);
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
            }, 30);
            add("shot " + station, () -> shot("30_gui_" + station), 10);
            add("close " + station, () -> {
                if (mc.player != null) {
                    mc.player.closeContainer();
                }
            }, 10);
        }

        add("open config screen", () -> net.neoforged.fml.ModList.get().getModContainerById("theabyss").ifPresent(c ->
            mc.setScreen(new net.neoforged.neoforge.client.gui.ConfigurationScreen(c, null))), 30);
        add("shot config screen", () -> shot("35_config_screen"), 10);
        add("close config screen", () -> mc.setScreen(null), 10);
        add("give items", () -> {
            command("give @s theabyss:aberythe_sword");
            command("give @s theabyss:aberythe_armor_chestplate");
            command("give @s theabyss:the_roka_spawn_egg");
        }, 40);
        add("shot hotbar", () -> shot("40_hotbar"), 20);
        if (mc.getSingleplayerServer() != null) {
            PossessionAutotest.addSteps(ClientAutotest::add, ClientAutotest::command, ClientAutotest::shot);
        }
        add("done", () -> {
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] DONE");
            mc.stop();
        }, 0);
    }
}
