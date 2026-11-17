package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.client.codex.CodexScreen;
import net.yezon.theabyss.init.TheabyssModBlocks;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.lore.ChronicleData;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.station.ArcanePower;

final class ChronicleRealAutotest {
    private static final ResourceKey<Level> ABYSS = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("theabyss:the_abyss"));
    private static final BlockPos ARCANE = new BlockPos(0, 200, 0);
    private static final double[][] ALTAR_PILLARS = {
        {6, 5, 0}, {-6, 5, 0}, {0, 5, 6}, {0, 5, -6}, {3, 4, -3}, {3, 4, 3}, {-3, 4, 3}, {-3, 4, -3}
    };
    private static final List<String> failures = new ArrayList<>();
    private static final int[] noted = new int[ChronicleData.COUNT + 1];
    private static int checks;
    private static BlockPos lush;
    private static BlockPos altar;

    private ChronicleRealAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("cr arena", () -> {
            command.accept("difficulty normal");
            command.accept("gamerule doMobSpawning false");
            command.accept("time set noon");
            command.accept("execute in minecraft:overworld run forceload add -16 -16 16 16");
            command.accept("execute in minecraft:overworld run fill -10 199 -10 10 199 10 minecraft:grass_block");
            command.accept("execute in minecraft:overworld run fill -10 200 -10 10 208 10 minecraft:air");
            command.accept("execute in minecraft:overworld run setblock 0 200 0 theabyss:arcane_workbench");
            for (int[] c : ArcanePower.CORNERS) {
                command.accept("execute in minecraft:overworld run setblock " + c[0] + " 200 " + c[1] + " theabyss:abyssal_energetica");
            }
            command.accept("execute in minecraft:overworld run fill 7 199 7 9 199 9 minecraft:water");
            command.accept("execute in theabyss:the_abyss run forceload add -16 -16 16 16");
            command.accept("execute in theabyss:the_abyss run fill -10 199 -10 10 199 10 minecraft:deepslate_tiles");
            command.accept("execute in theabyss:the_abyss run fill -10 200 -10 10 208 10 minecraft:air");
            command.accept("execute in minecraft:overworld run tp @s 0.5 200 -6.5 0 0");
            command.accept("gamemode survival @s");
            command.accept("attribute @s minecraft:generic.max_health base set 200");
            command.accept("clear @s");
            command.accept("effect clear @s");
            onServer(sp -> {
                net.yezon.theabyss.platform.attachment.Attachments.setData(sp, ChronicleData.TYPE, ChronicleData.NONE);
                sp.setHealth(sp.getMaxHealth());
                TheabyssModVariables.updatePlayerVariables(sp, v -> {
                    v.Mana = 0.0;
                    v.syncPlayerVariables(sp);
                });
            });
        }, 60);
        add.add("cr nothing yet", () -> onServer(sp -> check("starts with no page (" + ChronicleData.of(sp).count() + ")", ChronicleData.of(sp).count() == 0)), 2);

        for (int i = 0; i < 4; i++) {
            int corner = i;
            add.add("cr pillar aim " + i, () -> {
                command.accept("item replace entity @s hotbar." + corner + " with theabyss:loran_energy");
                int[] c = ArcanePower.CORNERS[corner];
                Vec3 target = new Vec3(c[0] + 0.5, 200.95, c[1] + 0.5);
                Vec3 stand = new Vec3(c[0] * 2 + 0.5, 200.0, c[1] * 2 + 0.5);
                Vec3 d = target.subtract(stand.add(0.0, 1.62, 0.0));
                float yaw = (float)Math.toDegrees(Math.atan2(-d.x, d.z));
                float pitch = (float)-Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)));
                command.accept("execute in minecraft:overworld run tp @s " + stand.x + " " + stand.y + " " + stand.z + " " + yaw + " " + pitch);
                mc.player.getInventory().selected = corner;
            }, 12);
            add.add("cr pillar charge " + i, () -> KeyMapping.click(mc.options.keyUse.getKey()), 8);
        }
        expect(add, "13 Energetica charged with Loran Energy", 13);

        add.add("cr arcane open", () -> onServer(sp -> {
            Container c = (Container)sp.serverLevel().getBlockEntity(ARCANE);
            c.setItem(0, new ItemStack(item("ring_of_invisibility")));
            for (int i = 1; i <= 8; i++) {
                c.setItem(i, new ItemStack(item(i % 2 == 1 ? "phantom_essence" : "hollow_powder")));
            }
            sp.openMenu((MenuProvider)sp.serverLevel().getBlockEntity(ARCANE), ARCANE);
        }), 10);
        add.add("cr arcane craft", () -> mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, 9, 0,
            net.minecraft.world.inventory.ClickType.QUICK_MOVE, mc.player), 6);
        add.add("cr arcane close", () -> onServer(ServerPlayer::closeContainer), 6);
        expect(add, "4 ring crafted at the Arcane", 4);

        add.add("cr drink item", () -> {
            command.accept("execute in minecraft:overworld run tp @s 0.5 200 -6.5 0 0");
            command.accept("item replace entity @s hotbar.0 with theabyss:somnium_regen_upgrade");
            mc.player.getInventory().selected = 0;
        }, 10);
        add.add("cr drink", () -> mc.options.keyUse.setDown(true), 50);
        add.add("cr drink release", () -> mc.options.keyUse.setDown(false), 6);
        expect(add, "10 Somnium Upgrade drunk", 10);

        add.add("cr shifter", () -> {
            command.accept("item replace entity @s hotbar.0 with theabyss:dream_shifter");
            mc.player.getInventory().selected = 0;
        }, 6);
        add.add("cr shift to abyss", () -> KeyMapping.click(mc.options.keyUse.getKey()), 70);
        add.add("cr in abyss", () -> onServer(sp -> check("dream shifter took us to the Abyss", sp.serverLevel().dimension() == ABYSS)), 2);
        expect(add, "1 entering the Abyss with the Dream Shifter", 1);

        add.add("cr kill wolf", () -> kill(TheabyssModEntities.INFECTED_WOLF.get()), 10);
        expect(add, "2 first infected creature killed", 2);
        expect(add, "6 first infected wolf killed", 6);
        add.add("cr kill phantom", () -> kill(TheabyssModEntities.INFECTED_PHANTOM.get()), 10);
        expect(add, "7 first phantom killed", 7);
        add.add("cr kill abyssaur", () -> kill(TheabyssModEntities.ABYSSAUR.get()), 40);
        expect(add, "11 first abyssaur defeated", 11);

        add.add("cr somnium full", () -> onServer(sp -> TheabyssModVariables.updatePlayerVariables(sp, v -> {
            v.Mana = 30.0;
            v.syncPlayerVariables(sp);
        })), 20);
        add.add("cr cast until empty", () -> onServer(sp -> TheabyssModVariables.updatePlayerVariables(sp, v -> {
            v.Mana = 0.2;
            v.syncPlayerVariables(sp);
        })), 20);
        expect(add, "8 Somnium ran empty", 8);

        add.add("cr find lush", () -> onServer(sp -> {
            ServerLevel level = (ServerLevel)sp.serverLevel();
            var found = level.findClosestBiome3d(h -> h.is(ResourceKey.create(Registries.BIOME, ResourceLocation.parse("theabyss:lush_glow_cave"))),
                sp.blockPosition(), 6000, 32, 64);
            lush = found == null ? null : found.getFirst();
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] chronicles real lush at {}", lush);
        }), 10);
        add.add("cr go lush", () -> {
            if (lush != null) {
                command.accept("gamemode creative @s");
                command.accept("execute in theabyss:the_abyss run tp @s " + lush.getX() + " " + lush.getY() + " " + lush.getZ());
            }
        }, 120);
        expect(add, "5 entering the Lush Glow Cave", 5);
        add.add("cr back to platform", () -> {
            command.accept("execute in theabyss:the_abyss run tp @s 0.5 200 0.5");
            command.accept("gamemode survival @s");
        }, 40);

        add.add("cr die", () -> command.accept("kill @s"), 30);
        add.add("cr respawn", () -> {
            if (mc.player != null) {
                mc.player.respawn();
            }
            mc.setScreen(null);
        }, 60);
        expect(add, "9 respawn after dying in the Abyss", 9);

        add.add("cr to arena", () -> command.accept("execute in minecraft:overworld run tp @s 0.5 200 -6.5 0 0"), 40);
        shift(add, command, "cr shift again to abyss");
        shift(add, command, "cr shift back");
        add.add("cr in overworld", () -> onServer(sp -> check("dream shifter took us back", sp.serverLevel().dimension() == Level.OVERWORLD)), 2);
        expect(add, "3 first return with the Dream Shifter", 3);

        add.add("cr frost", () -> command.accept("execute in theabyss:frost_world run tp @s 0.5 150 0.5"), 80);
        expect(add, "12 entering the Frost World", 12);

        add.add("cr find altar", () -> onServer(sp -> {
            ServerLevel abyss = sp.serverLevel().getServer().getLevel(ABYSS);
            var holder = abyss.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(
                ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse("theabyss:altar")));
            var found = abyss.getChunkSource().getGenerator().findNearestMapStructure(abyss, HolderSet.direct(holder), new BlockPos(0, 100, 0), 100, false);
            altar = found == null ? null : found.getFirst();
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] chronicles real altar structure near {}", altar);
        }), 10);
        add.add("cr go altar area", () -> {
            command.accept("gamemode creative @s");
            if (altar != null) {
                command.accept("execute in theabyss:the_abyss run forceload add " + (altar.getX() - 32) + " " + (altar.getZ() - 32) + " "
                    + (altar.getX() + 32) + " " + (altar.getZ() + 32));
                command.accept("execute in theabyss:the_abyss run tp @s " + altar.getX() + " 220 " + altar.getZ());
            }
        }, 200);
        add.add("cr find altar block", () -> onServer(sp -> {
            altar = null;
            ServerLevel level = (ServerLevel)sp.serverLevel();
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            BlockPos c = sp.blockPosition();
            for (int x = c.getX() - 40; x <= c.getX() + 40 && altar == null; x++) {
                for (int z = c.getZ() - 40; z <= c.getZ() + 40 && altar == null; z++) {
                    for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight() - 1; y++) {
                        if (level.getBlockState(p.set(x, y, z)).is(TheabyssModBlocks.NIGHT_ALTAR.get())) {
                            altar = p.immutable();
                            break;
                        }
                    }
                }
            }
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] chronicles real night altar block at {}", altar);
            check("night altar block found", altar != null);
        }), 10);
        add.add("cr stand at altar", () -> {
            if (altar != null) {
                command.accept("execute in theabyss:the_abyss run tp @s " + (altar.getX() + 0.5) + " " + (altar.getY() + 1) + " " + (altar.getZ() + 2.5));
            }
        }, 60);
        expect(add, "14 finding the Night Altar", 14);
        add.add("cr ignite pillars", () -> onServer(sp -> {
            if (altar == null) {
                return;
            }
            for (double[] o : ALTAR_PILLARS) {
                BlockPos pos = altar.offset((int)o[0], (int)o[1], (int)o[2]);
                if (!sp.serverLevel().getBlockState(pos).is(TheabyssModBlocks.STONE_BRICK_PILLAR.get())) {
                    TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] chronicles real no pillar at {} ({})", pos, sp.serverLevel().getBlockState(pos));
                    continue;
                }
                ItemStack heart = new ItemStack(item("soul_heart"));
                sp.setItemInHand(InteractionHand.MAIN_HAND, heart);
                sp.gameMode.useItemOn(sp, sp.serverLevel(), heart, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
            }
            sp.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }), 10);
        expect(add, "15 altar pillar ignited with a Soul Heart", 15);
        add.add("cr hold eye", () -> onServer(sp -> sp.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("eye_of_abyss")))), 20);
        expect(add, "16 holding the Eye of Abyss", 16);
        add.add("cr summon", () -> onServer(sp -> {
            if (altar != null) {
                sp.gameMode.useItemOn(sp, sp.serverLevel(), sp.getMainHandItem(), InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(altar), Direction.UP, altar, false));
            }
        }), 220);
        add.add("cr kill boss", () -> onServer(sp -> {
            ServerLevel level = (ServerLevel)sp.serverLevel();
            List<? extends Entity> bosses = level.getEntitiesOfClass(net.yezon.theabyss.entity.NightbladeBossEntity.class, new AABB(sp.blockPosition()).inflate(64));
            check("the altar summoned the Nightblade (" + bosses.size() + ")", !bosses.isEmpty());
            for (Entity e : bosses) {
                if (e instanceof LivingEntity boss) {
                    boss.hurt(sp.damageSources().playerAttack(sp), 100000.0F);
                    if (boss.isAlive()) {
                        boss.kill();
                    }
                }
            }
        }), 60);
        expect(add, "17 Nightblade defeated", 17);

        add.add("cr survival again", () -> command.accept("gamemode survival @s"), 4);
        add.add("cr shifter home", () -> onServer(sp -> {
            sp.getCooldowns().removeCooldown(item("dream_shifter"));
            sp.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("dream_shifter")));
        }), 10);
        add.add("cr shifter home client", () -> {
            mc.player.getCooldowns().removeCooldown(item("dream_shifter"));
            KeyMapping.click(mc.options.keyUse.getKey());
        }, 80);
        expect(add, "19 return to the Overworld after the fight", 19);
        add.add("cr water", () -> command.accept("execute in minecraft:overworld run tp @s 8.5 200 8.5"), 40);
        expect(add, "18 stepping into water after the fight", 18);
        expect(add, "20 all other pages written", 20);

        add.add("cr codex", () -> CodexScreen.openCategory("chronicles"), 20);
        add.add("cr shot codex", () -> shot.accept("116_chronicles_real_list"), 4);
        add.add("cr close", () -> mc.setScreen(null), 4);
        add.add("cr order", () -> {
            StringBuilder b = new StringBuilder();
            for (int i = 1; i <= ChronicleData.COUNT; i++) {
                b.append(i).append(noted[i] > 0 ? " ok" : " MISSING").append(i < ChronicleData.COUNT ? ", " : "");
            }
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] chronicles real pages: {}", b);
        }, 2);
        add.add("cr result", () -> {
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT chronicles_real {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
                checks, failures.isEmpty() ? "" : ", failed: " + failures);
            command.accept("attribute @s minecraft:generic.max_health base set 20");
        }, 2);
    }

    private static void shift(PossessionAutotest.StepAdder add, Consumer<String> command, String name) {
        Minecraft mc = Minecraft.getInstance();
        add.add(name + " ready", () -> {
            command.accept("item replace entity @s hotbar.0 with theabyss:dream_shifter");
            mc.player.getInventory().selected = 0;
            onServer(sp -> sp.getCooldowns().removeCooldown(item("dream_shifter")));
        }, 6);
        add.add(name, () -> {
            mc.player.getCooldowns().removeCooldown(item("dream_shifter"));
            KeyMapping.click(mc.options.keyUse.getKey());
        }, 70);
    }

    private static void expect(PossessionAutotest.StepAdder add, String name, int page) {
        add.add("cr expect " + page, () -> onServer(sp -> {
            boolean ok = ChronicleData.of(sp).has(page);
            if (ok) {
                noted[page]++;
            }
            check(name + " (page " + page + ")", ok);
        }), 2);
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("theabyss", id));
    }

    private static void kill(EntityType<? extends LivingEntity> type) {
        onServer(sp -> {
            ServerLevel level = (ServerLevel)sp.serverLevel();
            LivingEntity e = type.create(level);
            if (e == null) {
                check("spawn " + type, false);
                return;
            }
            e.moveTo(sp.getX() + 3.0, sp.getY(), sp.getZ(), 0.0F, 0.0F);
            level.addFreshEntity(e);
            e.hurt(sp.damageSources().playerAttack(sp), 100000.0F);
            if (e.isAlive()) {
                e.kill();
            }
        });
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] chronicles real check {}: {}", name, ok ? "ok" : "FAILED");
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
