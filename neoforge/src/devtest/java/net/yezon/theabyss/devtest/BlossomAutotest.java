package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModBlocks;
import net.yezon.theabyss.init.TheabyssModItems;

final class BlossomAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static final BlockPos FLOOR = new BlockPos(0, 200, 0);

    private BlossomAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        add.add("blossom go", () -> {
            command.accept("gamemode creative @s");
            command.accept("gamerule doMobSpawning false");
            command.accept("effect clear @s");
            command.accept("execute in theabyss:the_abyss run forceload add -16 -16 16 16");
            command.accept("execute in theabyss:the_abyss run fill -6 200 -6 6 207 6 minecraft:air");
            command.accept("execute in theabyss:the_abyss run fill -6 199 -6 6 199 6 theabyss:stone");
            command.accept("execute in theabyss:the_abyss run fill -6 208 -6 6 208 6 theabyss:stone");
            command.accept("execute in theabyss:the_abyss run fill -7 199 -7 7 208 -7 theabyss:stone");
            command.accept("execute in theabyss:the_abyss run fill -7 199 7 7 208 7 theabyss:stone");
            command.accept("execute in theabyss:the_abyss run fill -7 199 -7 -7 208 7 theabyss:stone");
            command.accept("execute in theabyss:the_abyss run fill 7 199 -7 7 208 7 theabyss:stone");
            command.accept("execute in theabyss:the_abyss run tp @s -1 202 5.5 180 -15");
        }, 160);
        add.add("growing place", () -> onServer(sp -> {
            ServerLevel level = (ServerLevel)sp.serverLevel();
            BlockPos base = new BlockPos(-3, 200, 0);
            ItemStack stack = new ItemStack(TheabyssModItems.BLOSSOM_RANK.get());
            sp.setItemInHand(InteractionHand.MAIN_HAND, stack);
            stack.useOn(new UseOnContext(sp, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(base.below()).add(0, 0.5, 0), Direction.UP, base.below(), false)));
            check("planting gives start, close end, end (" + segments(level, base, 5) + ")", segments(level, base, 5).equals("start,close_end,end,"));
            BlockPos top = base.above(2);
            for (int i = 0; i < 40 && !level.getBlockState(base.above(3)).is(TheabyssModBlocks.BLOSSOM_RANK.get()); i++) {
                level.getBlockState(top).randomTick(level, top, level.getRandom());
            }
            check("it grows by itself (" + segments(level, base, 5) + ")", segments(level, base, 5).equals("start,middle,close_end,end,"));
            check("only the blossom glows strongly", level.getLightEmission(base.above(3)) == 10 && level.getLightEmission(base.above(2)) == 1);
            ItemStack more = new ItemStack(TheabyssModItems.BLOSSOM_RANK.get());
            sp.setItemInHand(InteractionHand.MAIN_HAND, more);
            more.useOn(new UseOnContext(sp, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(base.above(3)).add(0, 0.5, 0), Direction.UP, base.above(3), false)));
            check("extending by hand moves the blossom up (" + segments(level, base, 6) + ")", segments(level, base, 6).equals("start,middle,middle,close_end,end,"));
            for (int n = 0; n < 2; n++) {
                BlockPos topNow = base.above(4 + n);
                ItemStack extra = new ItemStack(TheabyssModItems.BLOSSOM_RANK.get());
                sp.setItemInHand(InteractionHand.MAIN_HAND, extra);
                extra.useOn(new UseOnContext(sp, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(topNow).add(0, 0.5, 0), Direction.UP, topNow, false)));
            }
        }), 20);
        add.add("blossom shot lit", () -> shot.accept("129_blossom_rank"), 6);
        add.add("blossom dark", () -> command.accept("execute in theabyss:the_abyss run tp @s 0.5 201 3.5 180 -35"), 20);
        add.add("blossom shot close", () -> shot.accept("129_blossom_rank_close"), 6);
        add.add("growing break", () -> onServer(sp -> {
            ServerLevel level = (ServerLevel)sp.serverLevel();
            level.destroyBlock(new BlockPos(-3, 200, 0), true, sp);
        }), 20);
        add.add("growing check break", () -> onServer(sp -> {
            ServerLevel level = (ServerLevel)sp.serverLevel();
            check("breaking the base pops the whole rank (" + segments(level, new BlockPos(-3, 200, 0), 6) + ")", segments(level, new BlockPos(-3, 200, 0), 6).isEmpty());
        }), 4);
        add.add("blossom result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT blossom {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL", checks,
            failures.isEmpty() ? "" : ", failed: " + failures), 2);
    }

    private static String segments(ServerLevel level, BlockPos base, int max) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < max; i++) {
            var state = level.getBlockState(base.above(i));
            if (!state.is(TheabyssModBlocks.BLOSSOM_RANK.get())) {
                break;
            }
            out.append(state.getValue(net.yezon.theabyss.block.BlossomRankBlock.SEGMENT).getSerializedName()).append(',');
        }
        return out.toString();
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] blossom check {}: {}", name, ok ? "ok" : "FAILED");
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
