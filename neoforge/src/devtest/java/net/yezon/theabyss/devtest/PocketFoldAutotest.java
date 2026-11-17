package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.events.ApplyRingOfPocketRoomEvents;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.pocket.PocketCells;

final class PocketFoldAutotest {
    private static final ResourceKey<Level> ABYSS = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("theabyss:the_abyss"));
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static Vec3 start = Vec3.ZERO;

    private PocketFoldAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        add.add("pf abyss", () -> {
            command.accept("gamemode creative @s");
            command.accept("execute in theabyss:the_abyss run forceload add -8 -8 8 8");
            command.accept("execute in theabyss:the_abyss run fill -3 119 -3 3 119 3 minecraft:stone");
            command.accept("execute in theabyss:the_abyss run fill -3 120 -3 3 124 3 minecraft:air");
            command.accept("execute in theabyss:the_abyss run tp @s 0.5 120 0.5 0 0");
        }, 80);
        add.add("pf cast in", () -> cast(), 4);
        add.add("pf shot fold 1", () -> shot.accept("119_pocket_fold_1_leave_02"), 5);
        add.add("pf shot fold 2", () -> shot.accept("119_pocket_fold_2_leave_045"), 5);
        add.add("pf shot fold 3", () -> shot.accept("119_pocket_fold_3_leave_07"), 8);
        for (int n = 0; n < 8; n++) {
            int shotNo = n;
            add.add("pf shot arrive " + n, () -> shot.accept("119_pocket_fold_arrive_" + shotNo), 5);
        }
        add.add("pf settle", () -> {
        }, 20);
        add.add("pf in pocket", () -> onServer(sp -> check("from the Abyss into the pocket box (" + sp.serverLevel().dimension().location() + ")",
            sp.serverLevel().dimension() == PocketCells.DIMENSION)), 4);
        add.add("pf cast out", () -> cast(), 40);
        add.add("pf back", () -> onServer(sp -> check("back in the Abyss where it started (" + sp.serverLevel().dimension().location() + " " + sp.position() + ")",
            sp.serverLevel().dimension() == ABYSS && sp.position().distanceTo(start) < 2.5)), 4);
        add.add("pf result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT pocket_fold {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
    }

    private static void cast() {
        onServer(sp -> {
            if (sp.serverLevel().dimension() == ABYSS) {
                start = sp.position();
            }
            ItemStack ring = new ItemStack(TheabyssModItems.RING_OF_POCKET_BOX.get());
            sp.setItemInHand(InteractionHand.MAIN_HAND, ring);
            TheabyssModVariables.updatePlayerVariables(sp, v -> {
                v.Mana = 100.0;
                v.syncPlayerVariables(sp);
            });
            sp.getCooldowns().removeCooldown(ring.getItem());
            boolean ok = ApplyRingOfPocketRoomEvents.execute(sp.serverLevel(), sp.getX(), sp.getY(), sp.getZ(), sp, sp.getMainHandItem());
            check("pocket box journey starts in " + sp.serverLevel().dimension().location(), ok);
        });
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] pocket fold check {}: {}", name, ok ? "ok" : "FAILED");
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
