package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.init.TheabyssModBlocks;

final class NightgroveAutotest {
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static BlockPos jungle;
    private static float overworldBrightness;

    private NightgroveAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("ng alias", () -> {
            check("old id glowing_sun_shroom resolves to the toxic sun flower",
                BuiltInRegistries.BLOCK.get(ResourceLocation.parse("theabyss:glowing_sun_shroom")) == TheabyssModBlocks.TOXIC_SUN_FLOWER.get());
            check("toxic sun flower name", TheabyssModBlocks.TOXIC_SUN_FLOWER.get().getName().getString().equals("Toxic Sun Flower"));
        }, 2);
        add.add("ng overworld light", () -> {
            command.accept("gamemode creative @s");
            command.accept("time set midnight");
            command.accept("execute in minecraft:overworld run tp @s 0 200 0");
        }, 40);
        add.add("ng overworld measure", () -> overworldBrightness = lightmapBrightness(mc), 2);
        add.add("ng find jungle", () -> onServer(sp -> {
            ServerLevel abyss = sp.serverLevel().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("theabyss:the_abyss")));
            var found = abyss.findClosestBiome3d(h -> h.is(ResourceKey.create(Registries.BIOME, ResourceLocation.parse("theabyss:nightgrove"))),
                new BlockPos(0, 100, 0), 3000, 32, 64);
            jungle = found == null ? null : found.getFirst();
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] nightgrove biome at {}", jungle);
        }), 10);
        add.add("ng go jungle", () -> {
            if (jungle != null) {
                command.accept("execute in theabyss:the_abyss run forceload add " + (jungle.getX() - 16) + " " + (jungle.getZ() - 16) + " " + (jungle.getX() + 16) + " "
                    + (jungle.getZ() + 16));
                command.accept("execute in theabyss:the_abyss run tp @s " + jungle.getX() + " 200 " + jungle.getZ());
            }
        }, 160);
        add.add("ng ground", () -> onServer(sp -> {
            ServerLevel level = (ServerLevel)sp.serverLevel();
            int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sp.getBlockX(), sp.getBlockZ());
            BlockPos base = new BlockPos(sp.getBlockX(), y, sp.getBlockZ());
            String at = " " + base.getX() + " ";
            command.accept("execute in theabyss:the_abyss run fill " + (base.getX() - 4) + " " + (y - 1) + " " + (base.getZ() - 2) + " " + (base.getX() + 4) + " "
                + (y + 4) + " " + (base.getZ() + 6) + " minecraft:air");
            command.accept("execute in theabyss:the_abyss run fill " + (base.getX() - 4) + " " + (y - 1) + " " + (base.getZ() - 2) + " " + (base.getX() + 4) + " "
                + (y - 1) + " " + (base.getZ() + 6) + " theabyss:nightgrove_moss");
            command.accept("execute in theabyss:the_abyss run setblock " + (base.getX() - 3) + " " + y + " " + (base.getZ() + 4) + " theabyss:nightgrove_log");
            command.accept("execute in theabyss:the_abyss run setblock " + (base.getX() - 2) + " " + y + " " + (base.getZ() + 4) + " theabyss:nightgrove_planks");
            command.accept("execute in theabyss:the_abyss run setblock " + (base.getX() - 1) + " " + y + " " + (base.getZ() + 4) + " theabyss:toxic_sun_flower");
            command.accept("execute in theabyss:the_abyss run setblock " + base.getX() + " " + y + " " + (base.getZ() + 4) + " theabyss:nightgrove_door[half=lower]");
            command.accept("execute in theabyss:the_abyss run setblock " + base.getX() + " " + (y + 1) + " " + (base.getZ() + 4) + " theabyss:nightgrove_door[half=upper]");
            command.accept("execute in theabyss:the_abyss run setblock " + (base.getX() + 1) + " " + y + " " + (base.getZ() + 4) + " theabyss:nightgrove_sign");
            command.accept("execute in theabyss:the_abyss run setblock " + (base.getX() + 2) + " " + y + " " + (base.getZ() + 4) + " theabyss:nightgrove_trap_door");
            command.accept("execute in theabyss:the_abyss run setblock " + (base.getX() + 3) + " " + y + " " + (base.getZ() + 4) + " theabyss:nightgrove_bush");
            command.accept("execute in theabyss:the_abyss run summon theabyss:nightgrove_boat " + (base.getX() + 3.5) + " " + y + " " + (base.getZ() + 2.5));
            command.accept("execute in theabyss:the_abyss run tp @s " + (base.getX() + 0.5) + " " + (y + 1.2) + " " + (base.getZ() - 1.5) + " 0 25");
            command.accept("item replace entity @s hotbar.0 with theabyss:nightgrove_boat");
            command.accept("item replace entity @s hotbar.1 with theabyss:nightgrove_door");
            command.accept("item replace entity @s hotbar.2 with theabyss:nightgrove_sign");
            command.accept("item replace entity @s hotbar.3 with theabyss:nightgrove_hanging_sign");
            command.accept("item replace entity @s hotbar.4 with theabyss:toxic_sun_flower");
        }), 80);
        add.add("ng moody", () -> {
            float abyss = lightmapBrightness(mc);
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] nightgrove brightness overworld {} abyss {} (gamma {})", overworldBrightness, abyss, mc.options.gamma().get());
            check("abyss brightness is moody (" + abyss + ") while the overworld keeps the Minecraft setting (" + overworldBrightness + ")",
                abyss < 0.01F && Math.abs(overworldBrightness - mc.options.gamma().get().floatValue()) < 0.01F);
        }, 2);
        add.add("ng shot moody", () -> shot.accept("117_nightgrove_moody"), 4);
        add.add("ng bright", () -> ConfigConfiguration.ABYSS_BRIGHTNESS.set(100), 30);
        add.add("ng bright check", () -> check("abyss brightness follows the config (" + lightmapBrightness(mc) + ")",
            lightmapBrightness(mc) > 0.99F), 2);
        add.add("ng shot bright", () -> shot.accept("117_nightgrove_bright"), 4);
        add.add("ng reset", () -> ConfigConfiguration.ABYSS_BRIGHTNESS.set(0), 4);
        add.add("ng old ids", () -> {
            check("old block id jungle_log -> nightgrove_log",
                BuiltInRegistries.BLOCK.get(ResourceLocation.parse("theabyss:jungle_log")) == TheabyssModBlocks.NIGHTGROVE_LOG.get());
            check("old item id jungle_boat -> nightgrove_boat",
                BuiltInRegistries.ITEM.get(ResourceLocation.parse("theabyss:jungle_boat")) == BuiltInRegistries.ITEM.get(ResourceLocation.parse("theabyss:nightgrove_boat")));
            check("old entity id jungle_boat -> nightgrove_boat", BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("theabyss:jungle_boat"))
                == BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("theabyss:nightgrove_boat")));
            check("nightgrove bush registered", BuiltInRegistries.BLOCK.containsKey(ResourceLocation.parse("theabyss:nightgrove_bush")));
        }, 2);
        add.add("ng recipes", () -> onServer(sp -> {
            var recipes = sp.serverLevel().getServer().getRecipeManager();
            String[] ids = {"nightgrove_boat", "nightgrove_button_rcp", "nightgrove_chest_boat", "nightgrove_door_rco", "nightgrove_fence_gate_rcp",
                "nightgrove_fence_rcp", "nightgrove_hanging_sign", "nightgrove_planks_rcp", "nightgrove_planks_rcp_2", "nightgrove_pressure_plate_rcp",
                "nightgrove_shelf", "nightgrove_sign", "nightgrove_slab_rcp", "nightgrove_stairs_rcp", "nightgrove_trap_door_rcp"};
            int found = 0;
            for (String id : ids) {
                if (recipes.byKey(ResourceLocation.fromNamespaceAndPath("theabyss", id)).isPresent()) {
                    found++;
                } else {
                    TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] nightgrove missing recipe {}", id);
                }
            }
            check("all 15 nightgrove recipes loaded (" + found + ")", found == ids.length);
            var input = net.minecraft.world.item.crafting.CraftingInput.of(1, 1,
                java.util.List.of(new net.minecraft.world.item.ItemStack(TheabyssModBlocks.NIGHTGROVE_LOG.get())));
            var result = recipes.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, sp.serverLevel())
                .map(r -> r.value().assemble(input, sp.serverLevel().registryAccess())).orElse(net.minecraft.world.item.ItemStack.EMPTY);
            check("nightgrove log crafts nightgrove planks (" + result + ")", result.is(TheabyssModBlocks.NIGHTGROVE_PLANKS.get().asItem()) && result.getCount() == 4);
        }), 4);
        add.add("ng ruin", () -> onServer(sp -> {
            var templates = sp.serverLevel().getServer().getStructureManager();
            int ok = 0;
            String[] names = {"nightgrove_ruin_1", "nightgrove_ruin_2", "nightgrove_ruin_3", "nightgrove_ruin_4", "nightgrove_ruin_5", "nightgrove_melon"};
            for (String name : names) {
                var template = templates.get(ResourceLocation.fromNamespaceAndPath("theabyss", name));
                if (template.isEmpty()) {
                    TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] nightgrove template missing {}", name);
                    continue;
                }
                String nbt = template.get().save(new net.minecraft.nbt.CompoundTag()).toString();
                boolean old = nbt.contains("theabyss:jungle_");
                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] nightgrove template {} nightgrove ids {} old ids {}", name, nbt.contains("theabyss:nightgrove_"), old);
                if (!old) {
                    ok++;
                }
            }
            check("all renamed templates load without old jungle ids (" + ok + "/" + names.length + ")", ok == names.length);
        }), 10);
        add.add("ng plants", () -> onServer(sp -> {
            ServerLevel level = (ServerLevel)sp.serverLevel();
            java.util.Map<String, Integer> counts = new java.util.TreeMap<>();
            BlockPos c = jungle == null ? sp.blockPosition() : jungle;
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            for (int x = c.getX() - 16; x < c.getX() + 16; x++) {
                for (int z = c.getZ() - 16; z < c.getZ() + 16; z++) {
                    for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight() - 1; y++) {
                        String id = BuiltInRegistries.BLOCK.getKey(level.getBlockState(p.set(x, y, z)).getBlock()).getPath();
                        if (id.equals("nightgrove_bush") || id.equals("nightgrove_grass") || id.equals("nightgrove_leaves") || id.equals("nightgrove_log")) {
                            counts.merge(id, 1, Integer::sum);
                        }
                    }
                }
            }
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] nightgrove plants in 32x32 around {}: {}", c, counts);
            check("nightgrove bushes generate (" + counts.getOrDefault("nightgrove_bush", 0) + ")", counts.getOrDefault("nightgrove_bush", 0) > 0);
        }), 4);
        add.add("ng bush view", () -> onServer(sp -> {
            if (jungle == null) {
                return;
            }
            ServerLevel level = (ServerLevel)sp.serverLevel();
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            BlockPos best = null;
            for (int x = jungle.getX() - 12; x <= jungle.getX() + 12 && best == null; x++) {
                for (int z = jungle.getZ() - 12; z <= jungle.getZ() + 12 && best == null; z++) {
                    for (int y = level.getMaxBuildHeight() - 2; y > level.getMinBuildHeight(); y--) {
                        var id = BuiltInRegistries.BLOCK.getKey(level.getBlockState(p.set(x, y, z)).getBlock()).getPath();
                        if (id.equals("nightgrove_bush")) {
                            best = p.immutable();
                            break;
                        }
                        if (!level.getBlockState(p).isAir()) {
                            break;
                        }
                    }
                }
            }
            if (best != null) {
                command.accept("execute in theabyss:the_abyss run tp @s " + (best.getX() + 0.5) + " " + (best.getY() + 2.5) + " " + (best.getZ() - 3.5) + " 0 30");
                command.accept("effect give @s minecraft:night_vision 60 0 true");
            }
        }), 60);
        add.add("ng shot plants", () -> shot.accept("117_nightgrove_plants"), 4);
        add.add("ng result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT nightgrove {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
    }

    private static float lightmapBrightness(Minecraft mc) {
        return net.yezon.theabyss.client.fx.AbyssBrightness.brightness(Math.max(0.0F, mc.options.gamma().get().floatValue()));
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] nightgrove check {}: {}", name, ok ? "ok" : "FAILED");
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
