package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.pocket.PocketCells;

final class PocketAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static int cell = -1;
    private static UUID cowId;

    private PocketAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("pk setup", () -> {
            command.accept("execute in minecraft:overworld run forceload add -16 -16 16 16");
            command.accept("execute in minecraft:overworld run fill -6 199 -6 6 199 6 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -6 200 -6 6 206 6 minecraft:air");
            command.accept("time set noon");
            command.accept("execute in minecraft:overworld run tp @s 0.5 200 0.5 0 -30");
            command.accept("gamemode survival @s");
            command.accept("clear @s");
            command.accept("item replace entity @s weapon.mainhand with theabyss:ring_of_pocket_box");
        }, 20);
        add.add("pk cells", () -> onServer(sp -> {
            TheabyssModVariables.updatePlayerVariables(sp, c -> {
                c.Mana = 1000.0;
                c.syncPlayerVariables(sp);
            });
            PocketCells cells = PocketCells.get(sp.level().getServer());
            Set<Integer> got = new HashSet<>();
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                int c = cells.cellOf(UUID.randomUUID());
                got.add(c);
                list.add(c);
            }
            check("five new players at once get five different cells " + list, got.size() == 5);
            boolean apart = true;
            boolean found = true;
            for (int a : list) {
                found &= PocketCells.at(PocketCells.arrival(a).x, PocketCells.arrival(a).z) == a;
                for (int b : list) {
                    if (a != b) {
                        apart &= Math.max(Math.abs(PocketCells.originX(a) - PocketCells.originX(b)), Math.abs(PocketCells.originZ(a) - PocketCells.originZ(b)))
                            >= PocketCells.PITCH;
                    }
                }
            }
            check("their cells lie at least " + PocketCells.PITCH + " blocks apart", apart);
            check("every arrival point lies in its own cell", found);
        }), 4);
        add.add("pk enter", () -> KeyMapping.click(mc.options.keyUse.getKey()), 60);
        add.add("pk check enter", () -> onServer(sp -> {
            cell = PocketCells.get(sp.level().getServer()).find(sp.getUUID());
            Vec3 arrival = cell < 0 ? Vec3.ZERO : PocketCells.arrival(cell);
            check("the ring takes the player into the pocket dimension (" + sp.level().dimension().identifier() + ")",
                sp.level().dimension() == PocketCells.DIMENSION);
            check("arrival at the north wall of the own box (cell " + cell + ", at " + sp.position() + ", want " + arrival + ")",
                cell >= 0 && sp.position().distanceTo(arrival) < 1.0 && PocketCells.inBox(cell, sp.getX(), sp.getZ(), 0.0));
            check("looking into the box (yaw " + sp.getYRot() + ")", Math.abs(net.minecraft.util.Mth.wrapDegrees(sp.getYRot())) < 1.0F);
            BlockState under = sp.level().getBlockState(BlockPos.containing(sp.getX(), sp.getY() - 0.5, sp.getZ()));
            check("standing on grass (" + under + " at y " + sp.getY() + ")", under.is(Blocks.GRASS_BLOCK));
        }), 4);
        add.add("pk check client", () -> {
            check("client: no vanilla world border in the box (" + mc.level.getWorldBorder().getSize() + ")", mc.level.getWorldBorder().getSize() > 1000.0);
            check("client: short render distance (" + mc.options.getEffectiveRenderDistance() + ")", mc.options.getEffectiveRenderDistance() == 3);
        }, 30);
        add.add("pk shot arrival", () -> shot.accept("108_pocket_1_arrival"), 2);
        add.add("pk check land", () -> onServer(sp -> {
            ServerLevel level = sp.level();
            int ox = PocketCells.originX(cell);
            int oz = PocketCells.originZ(cell);
            int logs = 0;
            int water = 0;
            int leaves = 0;
            int decaying = 0;
            int flowers = 0;
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            for (int x = 0; x < PocketCells.CELL; x++) {
                for (int z = 0; z < PocketCells.CELL; z++) {
                    for (int y = PocketCells.FLOOR - 4; y < PocketCells.FLOOR + 24; y++) {
                        BlockState s = level.getBlockState(p.set(ox + x, y, oz + z));
                        if (s.is(Blocks.OAK_LOG) || s.is(Blocks.BIRCH_LOG)) {
                            logs++;
                        } else if (s.is(Blocks.WATER)) {
                            water++;
                        } else if (s.getBlock() instanceof LeavesBlock) {
                            leaves++;
                            if (!s.getValue(LeavesBlock.PERSISTENT)) {
                                decaying++;
                            }
                        } else if (s.is(net.minecraft.tags.BlockTags.SMALL_FLOWERS)) {
                            flowers++;
                        }
                    }
                }
            }
            check("landscape: trees (" + logs + " logs, " + leaves + " leaves), water (" + water + "), flowers (" + flowers + ")",
                logs > 10 && leaves > 50 && water > 10 && flowers > 0);
            check("landscape: leaves do not decay (" + decaying + ")", decaying == 0);
            boolean floor = true;
            int bx = PocketCells.boxX(cell);
            int bz = PocketCells.boxZ(cell);
            for (int x = 0; x < PocketCells.BOX; x += 31) {
                for (int z = 0; z < PocketCells.BOX; z += 31) {
                    floor &= level.getBlockState(new BlockPos(bx + x, PocketCells.FLOOR, bz + z)).is(Blocks.GRASS_BLOCK)
                        && level.getBlockState(new BlockPos(bx + x, PocketCells.FLOOR + 2, bz + z)).isAir();
                }
            }
            check("the box is a flat grass floor 32 x 32 with nothing on its corners", floor);
            boolean bedrock = true;
            for (int x = 0; x < PocketCells.CELL; x += 7) {
                for (int z = 0; z < PocketCells.CELL; z += 7) {
                    bedrock &= level.getBlockState(new BlockPos(ox + x, PocketCells.BOTTOM, oz + z)).is(Blocks.BEDROCK)
                        && level.getBlockState(new BlockPos(ox + x, PocketCells.BOTTOM - 1, oz + z)).isAir();
                }
            }
            check("bedrock at the bottom of box and landscape", bedrock);
            boolean empty = true;
            for (int y = PocketCells.BOTTOM - 8; y < PocketCells.FLOOR + 30; y++) {
                empty &= level.getBlockState(new BlockPos(ox - 20, y, oz + 30)).isAir() && level.getBlockState(new BlockPos(ox + 30, y, oz + 90)).isAir();
            }
            check("outside the cell the dimension is empty", empty);
            sp.level().setBlockAndUpdate(new BlockPos(bx + 5, PocketCells.FLOOR + 1, bz + 5), Blocks.GOLD_BLOCK.defaultBlockState());
        }), 4);
        add.add("pk walk setup", () -> onServer(sp -> sp.teleportTo(sp.level(), PocketCells.boxX(cell) + 3.0, PocketCells.FLOOR + 1, PocketCells.centreZ(cell),
            java.util.Set.of(), 90.0F, 10.0F, true)), 20);
        add.add("pk walk", () -> {
            mc.options.keyUp.setDown(true);
            mc.options.keySprint.setDown(true);
        }, 40);
        add.add("pk shot walk", () -> shot.accept("108_pocket_2a_wall_walk"), 2);
        add.add("pk walk stop", () -> {
            mc.options.keyUp.setDown(false);
            mc.options.keySprint.setDown(false);
            double u = mc.player.getX() - PocketCells.boxX(cell);
            check("walking into the wall stops at the edge (client x in box " + u + ")", u >= 0.29 && u < 0.6);
        }, 4);
        add.add("pk walk server", () -> onServer(sp -> check("the server agrees, nobody was caught (x in box " + (sp.getX() - PocketCells.boxX(cell)) + ")",
            sp.getX() - PocketCells.boxX(cell) >= 0.25 && sp.getX() - PocketCells.boxX(cell) < 0.7)), 2);
        add.add("pk fly up", () -> onServer(sp -> {
            sp.getAbilities().mayfly = true;
            sp.getAbilities().flying = true;
            sp.onUpdateAbilities();
            sp.teleportTo(sp.level(), PocketCells.centreX(cell), PocketCells.TOP_BLOCK - 2.0, PocketCells.centreZ(cell), java.util.Set.of(), 0.0F, -60.0F, true);
        }), 20);
        add.add("pk rise", () -> mc.options.keyJump.setDown(true), 40);
        add.add("pk shot ceiling", () -> shot.accept("108_pocket_2c_ceiling"), 2);
        add.add("pk rise stop", () -> {
            mc.options.keyJump.setDown(false);
            double head = mc.player.getY() + mc.player.getBbHeight();
            check("flying up stops under the ceiling (head at " + head + ", ceiling " + PocketCells.CEILING + ")",
                head <= PocketCells.CEILING + 0.01 && head > PocketCells.CEILING - 0.5);
        }, 4);
        add.add("pk build high", () -> onServer(sp -> {
            check("the server agrees under the ceiling, nobody was caught (y " + sp.getY() + ")", sp.getY() > PocketCells.TOP_BLOCK - 3.0);
            sp.getAbilities().flying = false;
            sp.getAbilities().mayfly = false;
            sp.onUpdateAbilities();
            ServerLevel level = sp.level();
            int x = (int)PocketCells.centreX(cell) + 3;
            int z = (int)PocketCells.centreZ(cell);
            level.setBlockAndUpdate(new BlockPos(x, PocketCells.TOP_BLOCK - 1, z), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(x + 1, PocketCells.TOP_BLOCK, z), Blocks.STONE.defaultBlockState());
            net.minecraft.world.item.ItemStack stone = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE, 8);
            sp.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, stone);
            sp.gameMode.useItemOn(sp, level, stone, net.minecraft.world.InteractionHand.OFF_HAND, new net.minecraft.world.phys.BlockHitResult(
                new Vec3(x + 0.5, PocketCells.TOP_BLOCK, z + 0.5), net.minecraft.core.Direction.UP, new BlockPos(x, PocketCells.TOP_BLOCK - 1, z), false));
            sp.gameMode.useItemOn(sp, level, stone, net.minecraft.world.InteractionHand.OFF_HAND, new net.minecraft.world.phys.BlockHitResult(
                new Vec3(x + 1.5, PocketCells.TOP_BLOCK + 1, z + 0.5), net.minecraft.core.Direction.UP, new BlockPos(x + 1, PocketCells.TOP_BLOCK, z), false));
            check("building up to 32 blocks above the floor works (y " + PocketCells.TOP_BLOCK + ")",
                level.getBlockState(new BlockPos(x, PocketCells.TOP_BLOCK, z)).is(Blocks.STONE));
            check("one block higher is refused (y " + (PocketCells.TOP_BLOCK + 1) + ")",
                level.getBlockState(new BlockPos(x + 1, PocketCells.TOP_BLOCK + 1, z)).isAir());
            sp.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, net.minecraft.world.item.ItemStack.EMPTY);
        }), 4);
        add.add("pk break outside", () -> onServer(sp -> {
            BlockPos outside = new BlockPos(PocketCells.boxX(cell) - 1, PocketCells.FLOOR, PocketCells.centreZ(cell) > 0 ? (int)PocketCells.centreZ(cell) : 0);
            BlockState before = sp.level().getBlockState(outside);
            boolean broken = sp.gameMode.destroyBlock(outside);
            check("a block outside the box cannot be broken (" + before + ", broken " + broken + ")", !broken && sp.level().getBlockState(outside) == before);
        }), 2);
        add.add("pk centre", () -> onServer(sp -> sp.teleportTo(sp.level(), PocketCells.centreX(cell), PocketCells.FLOOR + 1, PocketCells.centreZ(cell),
            java.util.Set.of(), 90.0F, 0.0F, true)), 30);
        add.add("pk shot centre", () -> shot.accept("108_pocket_2b_centre"), 2);
        add.add("pk to wall", () -> onServer(sp -> sp.teleportTo(sp.level(), PocketCells.centreX(cell) - 6, PocketCells.FLOOR + 1, PocketCells.boxZ(cell) + 1.2,
            java.util.Set.of(), 160.0F, 5.0F, true)), 30);
        add.add("pk shot border", () -> shot.accept("108_pocket_2_border"), 2);
        add.add("pk to overview", () -> onServer(sp -> {
            sp.getAbilities().mayfly = true;
            sp.getAbilities().flying = true;
            sp.onUpdateAbilities();
            sp.teleportTo(sp.level(), PocketCells.boxX(cell) + 2.0, PocketCells.FLOOR + 14, PocketCells.boxZ(cell) + 2.0, java.util.Set.of(), -45.0F, 25.0F, true);
        }), 30);
        add.add("pk shot overview", () -> shot.accept("108_pocket_3_overview"), 2);
        add.add("pk turn", () -> onServer(sp -> sp.teleportTo(sp.level(), PocketCells.boxX(cell) + 30.0, PocketCells.FLOOR + 6, PocketCells.boxZ(cell) + 30.0,
            java.util.Set.of(), 120.0F, 10.0F, true)), 30);
        add.add("pk shot pond side", () -> shot.accept("108_pocket_4_landscape"), 2);
        add.add("pk night", () -> command.accept("time set midnight"), 100);
        add.add("pk shot night", () -> shot.accept("108_pocket_5_night"), 2);
        add.add("pk check night", () -> onServer(sp -> {
            int monsters = sp.level().getEntitiesOfClass(Monster.class, new AABB(sp.blockPosition()).inflate(64)).size();
            check("no monsters spawn at night (" + monsters + ")", monsters == 0);
            sp.getAbilities().flying = false;
            sp.getAbilities().mayfly = false;
            sp.onUpdateAbilities();
        }), 2);
        add.add("pk day", () -> command.accept("time set noon"), 4);
        add.add("pk fall", () -> onServer(sp -> sp.teleportTo(sp.level(), PocketCells.centreX(cell), PocketCells.BOTTOM - 20, PocketCells.centreZ(cell),
            java.util.Set.of(), 0.0F, 0.0F, true)), 12);
        add.add("pk check fall", () -> onServer(sp -> check("falling into the void puts you back into the own box (" + sp.position() + ", health " + sp.getHealth() + ")",
            sp.level().dimension() == PocketCells.DIMENSION && sp.position().distanceTo(PocketCells.arrival(cell)) < 1.0 && sp.getHealth() >= sp.getMaxHealth())), 2);
        add.add("pk out", () -> onServer(sp -> sp.teleportTo(sp.level(), PocketCells.boxX(cell) - 6.0, PocketCells.FLOOR + 6, PocketCells.centreZ(cell),
            java.util.Set.of(), 0.0F, 0.0F, true)), 12);
        add.add("pk check out", () -> onServer(sp -> check("standing outside the border puts you back into the box (" + sp.position() + ")",
            PocketCells.inBox(cell, sp.getX(), sp.getZ(), 0.0))), 2);
        add.add("pk cow", () -> onServer(sp -> {
            var cow = net.minecraft.world.entity.EntityTypes.COW.spawn(sp.level(), BlockPos.containing(PocketCells.boxX(cell) - 5.0, PocketCells.FLOOR + 8, PocketCells.centreZ(cell)),
                EntitySpawnReason.COMMAND);
            cowId = cow == null ? null : cow.getUUID();
        }), 45);
        add.add("pk check cow", () -> onServer(sp -> {
            var cow = cowId == null ? null : sp.level().getEntity(cowId);
            check("a creature outside the box is set back inside (" + (cow == null ? "gone" : cow.position()) + ")",
                cow != null && PocketCells.inBox(cell, cow.getX(), cow.getZ(), 0.0));
        }), 2);
        add.add("pk prepare back", () -> onServer(sp -> {
            sp.getCooldowns().removeCooldown(sp.getCooldowns().getCooldownGroup(sp.getMainHandItem()));
            sp.setXRot(-30.0F);
        }), 4);
        add.add("pk back", () -> KeyMapping.click(mc.options.keyUse.getKey()), 60);
        add.add("pk check back", () -> onServer(sp -> check("the ring takes you back to where you were (" + sp.level().dimension().identifier() + " " + sp.position() + ")",
            sp.level().dimension() == Level.OVERWORLD && sp.position().distanceTo(new Vec3(1.0, 200.0, 1.0)) < 1.5)), 4);
        add.add("pk check client back", () -> {
            check("client: render distance and border normal again (" + mc.options.getEffectiveRenderDistance() + ", border " + mc.level.getWorldBorder().getSize() + ")",
                mc.options.getEffectiveRenderDistance() > 2 && mc.level.getWorldBorder().getSize() > 1000.0);
        }, 2);
        add.add("pk again", () -> onServer(sp -> {
            sp.getCooldowns().removeCooldown(sp.getCooldowns().getCooldownGroup(sp.getMainHandItem()));
            sp.setXRot(-30.0F);
        }), 4);
        add.add("pk enter again", () -> KeyMapping.click(mc.options.keyUse.getKey()), 60);
        add.add("pk check again", () -> onServer(sp -> {
            check("the second journey goes into the same box, the gold block is still there",
                PocketCells.get(sp.level().getServer()).find(sp.getUUID()) == cell && sp.level().dimension() == PocketCells.DIMENSION
                    && sp.level().getBlockState(new BlockPos(PocketCells.boxX(cell) + 5, PocketCells.FLOOR + 1, PocketCells.boxZ(cell) + 5)).is(Blocks.GOLD_BLOCK));
        }), 4);
        add.add("pk leave", () -> onServer(sp -> {
            sp.getCooldowns().removeCooldown(sp.getCooldowns().getCooldownGroup(sp.getMainHandItem()));
            sp.setXRot(-30.0F);
        }), 4);
        add.add("pk leave use", () -> KeyMapping.click(mc.options.keyUse.getKey()), 40);
        add.add("pk result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT pocket {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] pocket check {}: {}", name, ok ? "ok" : "FAILED");
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
