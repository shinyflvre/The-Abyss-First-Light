package net.yezon.theabyss.devtest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.yezon.theabyss.events.GiveBookOnFirstJoinEvents;
import net.yezon.theabyss.network.TheabyssModVariables;

final class BookAutotest {
    private static final Identifier BOOK = Identifier.fromNamespaceAndPath("theabyss", "the_abyss");
    private static final List<String> failures = new ArrayList<>();
    private static int checks;

    private BookAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("patchouli loaded", () -> check("patchouli is loaded", ModList.get().isLoaded("patchouli")), 2);
        add.add("first join", () -> onServer(sp -> {
            sp.getInventory().clearContent();
            TheabyssModVariables.updatePlayerVariables(sp, vars -> vars.BookSpawn = false);
            GiveBookOnFirstJoinEvents.execute(sp.level(), sp.getX(), sp.getY(), sp.getZ(), sp);
        }), 20);
        add.add("book in inventory", () -> onServer(sp -> {
            boolean found = false;
            for (int i = 0; i < sp.getInventory().getContainerSize(); i++) {
                ItemStack stack = sp.getInventory().getItem(i);
                if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals("patchouli:guide_book")) {
                    found = true;
                    TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] book stack: {} {}", stack, stack.getComponents());
                }
            }
            check("guide book handed out on first join", found);
            check("handed out only once (BookSpawn set)", TheabyssModVariables.getPlayerVariables(sp).BookSpawn);
        }), 5);
        add.add("shot hotbar book", () -> shot.accept("70_guide_book_hotbar"), 10);
        add.add("open book", () -> check("book GUI opens", openBook(null)), 40);
        add.add("shot book landing", () -> shot.accept("71_guide_book_landing"), 10);
        add.add("open entry", () -> check("entry opens", openBook(Identifier.fromNamespaceAndPath("theabyss", "getting_started/portal"))), 40);
        add.add("shot book entry", () -> shot.accept("72_guide_book_entry"), 10);
        add.add("close book", () -> mc.gui.setScreen(null), 10);
        add.add("book result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT book {} ({} checks{})",
            failures.isEmpty() ? "PASS" : "FAIL", checks, failures.isEmpty() ? "" : ", failed: " + failures), 10);
    }

    private static boolean openBook(Identifier entry) {
        try {
            Class<?> api = Class.forName("vazkii.patchouli.api.PatchouliAPI");
            Object instance = api.getMethod("get").invoke(null);
            for (Method method : instance.getClass().getMethods()) {
                if (entry == null && method.getName().equals("openBookGUI") && method.getParameterCount() == 1
                    && method.getParameterTypes()[0] == Identifier.class) {
                    method.invoke(instance, BOOK);
                    return true;
                }
                if (entry != null && method.getName().equals("openBookEntry") && method.getParameterCount() == 3) {
                    method.invoke(instance, BOOK, entry, 0);
                    return true;
                }
            }
            TheabyssDevTest.LOGGER.error("[ABYSS-AUTOTEST] no matching PatchouliAPI method");
        } catch (ReflectiveOperationException | RuntimeException e) {
            TheabyssDevTest.LOGGER.error("[ABYSS-AUTOTEST] opening the book failed", e);
        }
        return false;
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] book check {}: {}", name, ok ? "ok" : "FAILED");
    }

    private static void onServer(Consumer<ServerPlayer> action) {
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) {
            return;
        }
        ServerPlayer player = server.getPlayerList().getPlayer(mc.player.getUUID());
        if (player != null) {
            server.execute(() -> action.accept(player));
        }
    }
}
