package net.yezon.theabyss.devtest;

import com.mojang.datafixers.util.Pair;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.init.TheabyssModItems;

public final class MeteoroidAutotest {
    private static final long FRAME = 50_000_000L;
    private static final ResourceKey<Level> ABYSS = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("theabyss", "the_abyss"));
    private static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("theabyss", "plains"));
    private static volatile int baseX = 0;
    private static volatile int baseZ = 400;
    private static boolean lores = true;
    private static final Vec3[] targets = {Vec3.ZERO, Vec3.ZERO, Vec3.ZERO};
    private static volatile Vec3 dir = new Vec3(0.0, 0.0, 1.0);
    private static boolean recording;
    private static long next;
    private static int index;
    private static int frames;
    private static String prefix = "meteo";

    private MeteoroidAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, String take) {
        prefix = "meteo_" + take;
        add.add("meteo lores off", () -> {
            lores = ConfigConfiguration.ENABLE_LORES.get();
            ConfigConfiguration.ENABLE_LORES.set(false);
        }, 2);
        add.add("meteo find", () -> onServer(sp -> {
            ServerLevel abyss = sp.level().getServer().getLevel(ABYSS);
            if (abyss == null) {
                return;
            }
            Pair<BlockPos, Holder<Biome>> found = abyss.findClosestBiome3d(h -> h.is(BIOME), new BlockPos(0, 80, 0), 3000, 32, 64);
            if (found != null) {
                baseX = found.getFirst().getX();
                baseZ = found.getFirst().getZ();
            }
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] meteo spot {} {} found {}", baseX, baseZ, found != null);
        }), 10);
        add.add("meteo arena", () -> {
            command.accept(String.format(Locale.ROOT, "execute in theabyss:the_abyss run forceload add %d %d %d %d", baseX - 64, baseZ - 64, baseX + 64, baseZ + 64));
            command.accept(String.format(Locale.ROOT, "execute in theabyss:the_abyss run tp @s %d 200 %d", baseX, baseZ));
            command.accept("gamemode creative @s");
            command.accept("weather clear");
            command.accept("time set " + (take.equals("day") ? "6000" : "14500"));
            command.accept("gamerule spawn_mobs false");
            command.accept("gamerule advance_time false");
            command.accept("ta2 somnium set infinite");
        }, 220);
        add.add("meteo place", () -> onServer(sp -> {
            ServerLevel level = sp.level();
            run(sp, "kill @e[type=!minecraft:player,distance=..80]");
            Vec3 base = surface(level, baseX, baseZ);
            dir = new Vec3(0.0, 0.0, 1.0);
            for (int k = 0; k < 16; k++) {
                double a = k * Math.PI / 8.0;
                Vec3 d = new Vec3(Math.sin(a), 0.0, Math.cos(a));
                Vec3 cam = surface(level, base.subtract(d.scale(20.0))).add(0.0, 3.0, 0.0);
                Vec3 caster = surface(level, base.subtract(d.scale(22.0))).add(0.0, 1.6, 0.0);
                if (clear(level, cam, base.add(0.0, 1.5, 0.0)) && clear(level, cam, base.add(0.0, 24.0, 0.0)) && clear(level, cam, base.add(0.0, 12.0, 0.0))
                    && clear(level, caster, base.add(0.0, 1.0, 0.0))) {
                    dir = d;
                    TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] meteo direction {}", k);
                    break;
                }
            }
            Vec3 caster = surface(level, base.subtract(dir.scale(22.0)));
            sp.getAbilities().flying = true;
            sp.onUpdateAbilities();
            run(sp, String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f facing %.2f %.2f %.2f", caster.x, caster.y, caster.z, base.x, base.y, base.z));
            sp.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TheabyssModItems.RING_OF_METEOROIDS.get()));
            for (int i = 0; i < 3; i++) {
                double a = i * Math.PI * 2.0 / 3.0 + 0.4;
                Vec3 h = surface(level, base.add(Math.cos(a) * 5.0, 0.0, Math.sin(a) * 5.0));
                run(sp, String.format(Locale.ROOT, "summon minecraft:husk %.2f %.2f %.2f {NoAI:1b,PersistenceRequired:1b,Silent:1b}", h.x, h.y, h.z));
            }
        }), 30);
        add.add("meteo hud", () -> {
            Minecraft mc = Minecraft.getInstance();
            if (!mc.gui.hud.isHidden()) {
                mc.gui.hud.toggle();
            }
            mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(0.0);
        }, 20);
        add.add("meteo cast", () -> {
            start(150, "meteo_" + take);
            onServer(sp -> {
                ServerLevel level = sp.level();
                sp.getMainHandItem().getItem().use(level, sp, InteractionHand.MAIN_HAND);
                Vec3 base = surface(level, baseX, baseZ);
                Vec3 cam = surface(level, base.subtract(dir.scale(20.0))).add(0.0, 2.0, 0.0);
                Vec3 look = cam.add(dir.scale(10.0)).add(0.0, 10.0 * Math.tan(Math.toRadians(28.0)), 0.0);
                run(sp, String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f facing %.2f %.2f %.2f", cam.x, cam.y, cam.z, look.x, look.y, look.z));
            });
        }, 170);
        add.add("strike place", () -> onServer(sp -> {
            ServerLevel level = sp.level();
            run(sp, "kill @e[type=!minecraft:player,distance=..80]");
            run(sp, "ta2 somnium level damage 2");
            Vec3 base = surface(level, baseX, baseZ);
            Vec3 sdir = dir;
            for (int k = 0; k < 16; k++) {
                double ang = k * Math.PI / 8.0;
                Vec3 d = new Vec3(Math.sin(ang), 0.0, Math.cos(ang));
                Vec3 sd = new Vec3(-d.z, 0.0, d.x);
                Vec3 c = surface(level, base.subtract(d.scale(14.0)));
                Vec3 eye = c.add(0.0, 4.1, 0.0);
                boolean ok = Math.abs(c.y - base.y) <= 3.0 && level.getBiome(BlockPos.containing(c)).is(BIOME);
                for (int i = 0; i < 3 && ok; i++) {
                    ok = clear(level, eye, surface(level, base.add(sd.scale(-4.0 + i * 4.0))).add(0.0, 1.2, 0.0));
                }
                if (ok) {
                    sdir = d;
                    TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] strike direction {}", k);
                    break;
                }
            }
            Vec3 side = new Vec3(-sdir.z, 0.0, sdir.x);
            Vec3 caster = surface(level, base.subtract(sdir.scale(14.0)));
            for (int i = 0; i < 3; i++) {
                Vec3 h = surface(level, base.add(side.scale(-4.0 + i * 4.0)));
                run(sp, String.format(Locale.ROOT, "summon minecraft:husk %.2f %.2f %.2f {NoAI:1b,PersistenceRequired:1b,Silent:1b,Health:200f,attributes:[{id:\"minecraft:max_health\",base:200}]}", h.x, h.y, h.z));
                targets[i] = h.add(0.0, 1.2, 0.0);
            }
            sp.getAbilities().flying = true;
            sp.onUpdateAbilities();
            run(sp, String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f facing %.2f %.2f %.2f", caster.x, caster.y + 2.5, caster.z, targets[0].x, targets[0].y, targets[0].z));
            net.yezon.theabyss.ring.secondary.Secondary.resetCooldowns(sp);
        }), 30);
        add.add("strike cast", () -> {
            start(70, "meteo_strike");
            onServer(sp -> net.yezon.theabyss.ring.secondary.Secondary.cast(sp, sp.getMainHandItem()));
        }, 8);
        for (int k = 1; k < 3; k++) {
            int t = k;
            add.add("strike aim " + k, () -> onServer(sp -> run(sp, String.format(Locale.ROOT, "tp @s ~ ~ ~ facing %.2f %.2f %.2f",
                targets[t].x, targets[t].y, targets[t].z))), 8);
        }
        add.add("strike wait", () -> {
        }, 60);
        add.add("meteo end", () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.setCameraEntity(mc.player);
            }
            recording = false;
            if (mc.gui.hud.isHidden()) {
                mc.gui.hud.toggle();
            }
            ConfigConfiguration.ENABLE_LORES.set(lores);
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT meteo frames {}", index);
        }, 20);
    }

    static void addSkySteps(PossessionAutotest.StepAdder add, Consumer<String> command) {
        add.add("sky lores off", () -> {
            lores = ConfigConfiguration.ENABLE_LORES.get();
            ConfigConfiguration.ENABLE_LORES.set(false);
        }, 2);
        add.add("sky find", () -> onServer(sp -> {
            ServerLevel abyss = sp.level().getServer().getLevel(ABYSS);
            if (abyss == null) {
                return;
            }
            Pair<BlockPos, Holder<Biome>> found = abyss.findClosestBiome3d(h -> h.is(BIOME), new BlockPos(0, 80, 0), 3000, 32, 64);
            if (found != null) {
                baseX = found.getFirst().getX();
                baseZ = found.getFirst().getZ();
            }
        }), 10);
        add.add("sky arena", () -> {
            command.accept(String.format(Locale.ROOT, "execute in theabyss:the_abyss run forceload add %d %d %d %d", baseX - 64, baseZ - 64, baseX + 64, baseZ + 64));
            command.accept(String.format(Locale.ROOT, "execute in theabyss:the_abyss run tp @s %d 200 %d", baseX, baseZ));
            command.accept("gamemode creative @s");
            command.accept("weather clear");
            command.accept("time set 14500");
            command.accept("gamerule spawn_mobs false");
            command.accept("gamerule advance_time false");
            command.accept("ta2 somnium set infinite");
            command.accept("ta2 somnium level damage 2");
        }, 220);
        add.add("sky place", () -> onServer(sp -> {
            ServerLevel level = sp.level();
            run(sp, "kill @e[type=!minecraft:player,distance=..80]");
            dir = new Vec3(0.0, 0.0, 1.0);
            int bx = baseX;
            int bz = baseZ;
            search:
            for (int ring = 0; ring <= 5; ring++) {
                for (int ox = -ring; ox <= ring; ox++) {
                    for (int oz = -ring; oz <= ring; oz++) {
                        if (Math.max(Math.abs(ox), Math.abs(oz)) != ring) {
                            continue;
                        }
                        int cx = bx + ox * 8;
                        int cz = bz + oz * 8;
                        Vec3 c = surface(level, cx, cz);
                        boolean flat = true;
                        for (int i = 0; i < 16 && flat; i++) {
                            double a = i * Math.PI / 8.0;
                            for (double rr : new double[] {3.0, 6.5}) {
                                int sx = (int)Math.floor(c.x + Math.cos(a) * rr);
                                int sz = (int)Math.floor(c.z + Math.sin(a) * rr);
                                int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sx, sz);
                                int any = level.getHeight(Heightmap.Types.MOTION_BLOCKING, sx, sz);
                                if (Math.abs(top - c.y) > 1.0 || any - top > 0) {
                                    flat = false;
                                }
                            }
                        }
                        if (!flat) {
                            continue;
                        }
                        for (int k = 0; k < 16; k++) {
                            double a = k * Math.PI / 8.0;
                            Vec3 d = new Vec3(Math.sin(a), 0.0, Math.cos(a));
                            Vec3 cam = c.subtract(d.scale(11.0)).add(0.0, 9.0, 0.0);
                            if (clear(level, cam, c.add(0.0, 0.5, 0.0)) && clear(level, cam, c.add(0.0, 6.0, 0.0))) {
                                dir = d;
                                baseX = cx;
                                baseZ = cz;
                                TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] sky flat spot {} {} direction {}", cx, cz, k);
                                break search;
                            }
                        }
                    }
                }
            }
            Vec3 base = surface(level, baseX, baseZ);
            int gy = (int)base.y - 1;
            net.minecraft.world.level.block.state.BlockState top = level.getBlockState(new BlockPos(baseX, gy, baseZ));
            net.minecraft.world.level.block.state.BlockState under = level.getBlockState(new BlockPos(baseX, gy - 1, baseZ));
            for (int x = -14; x <= 14; x++) {
                for (int z = -14; z <= 14; z++) {
                    if (x * x + z * z > 14 * 14) {
                        continue;
                    }
                    for (int y = gy - 3; y <= gy + 12; y++) {
                        BlockPos pos = new BlockPos(baseX + x, y, baseZ + z);
                        net.minecraft.world.level.block.state.BlockState state = y < gy ? under : y == gy ? top : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
                        level.setBlock(pos, state, 2);
                    }
                }
            }
            Vec3 caster = surface(level, base.subtract(dir.scale(16.0)));
            sp.getAbilities().flying = true;
            sp.onUpdateAbilities();
            run(sp, String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f facing %.2f %.2f %.2f", caster.x, caster.y + 1.0, caster.z, base.x, base.y, base.z));
            sp.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TheabyssModItems.RING_OF_INVERTED_SKY.get()));
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI * 2.0 / 6.0 + 0.3;
                double r = 1.5 + (i % 3) * 1.4;
                Vec3 h = surface(level, base.add(Math.cos(a) * r, 0.0, Math.sin(a) * r));
                run(sp, String.format(Locale.ROOT, "summon minecraft:husk %.2f %.2f %.2f {PersistenceRequired:1b,Silent:1b,Health:200f,attributes:[{id:\"minecraft:max_health\",base:200}]}", h.x, h.y, h.z));
            }
        }), 30);
        add.add("sky hud", () -> {
            Minecraft mc = Minecraft.getInstance();
            if (!mc.gui.hud.isHidden()) {
                mc.gui.hud.toggle();
            }
            mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(0.0);
        }, 20);
        add.add("sky cast", () -> {
            start(130, "sky_main");
            onServer(sp -> {
                ServerLevel level = sp.level();
                sp.getMainHandItem().getItem().use(level, sp, InteractionHand.MAIN_HAND);
                Vec3 base = surface(level, baseX, baseZ);
                Vec3 cam = base.subtract(dir.scale(12.0)).add(0.0, 8.0, 0.0);
                Vec3 look = base.subtract(dir.scale(1.0)).add(0.0, 1.0, 0.0);
                run(sp, String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f facing %.2f %.2f %.2f", cam.x, cam.y, cam.z, look.x, look.y, look.z));
            });
        }, 140);
        add.add("cage place", () -> onServer(sp -> {
            ServerLevel level = sp.level();
            run(sp, "kill @e[type=minecraft:husk,distance=..80]");
            Vec3 base = surface(level, baseX, baseZ);
            for (int i = 0; i < 4; i++) {
                double a = i * Math.PI / 2.0 + 0.5;
                Vec3 h = surface(level, base.add(Math.cos(a) * 3.0, 0.0, Math.sin(a) * 3.0));
                run(sp, String.format(Locale.ROOT, "summon minecraft:husk %.2f %.2f %.2f {PersistenceRequired:1b,Silent:1b,Health:200f,attributes:[{id:\"minecraft:max_health\",base:200}]}", h.x, h.y, h.z));
            }
            Vec3 cam = base.subtract(dir.scale(12.0)).add(0.0, 6.0, 0.0);
            run(sp, String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f facing %.2f %.2f %.2f", cam.x, cam.y, cam.z, base.x, base.y, base.z));
            net.yezon.theabyss.ring.secondary.Secondary.resetCooldowns(sp);
        }), 20);
        add.add("cage cast", () -> {
            start(110, "sky_cage");
            onServer(sp -> {
                net.yezon.theabyss.ring.secondary.Secondary.cast(sp, sp.getMainHandItem());
                Vec3 base = surface(sp.level(), baseX, baseZ);
                run(sp, String.format(Locale.ROOT, "tp @s ~ ~ ~ facing %.2f %.2f %.2f", base.x, base.y + 2.0, base.z));
            });
        }, 120);
        add.add("sky end", () -> {
            recording = false;
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui.hud.isHidden()) {
                mc.gui.hud.toggle();
            }
            ConfigConfiguration.ENABLE_LORES.set(lores);
            TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT sky frames {}", index);
        }, 20);
    }

    private static Vec3 surface(ServerLevel level, int x, int z) {
        return new Vec3(x + 0.5, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z + 0.5);
    }

    private static Vec3 surface(ServerLevel level, Vec3 at) {
        int x = (int)Math.floor(at.x);
        int z = (int)Math.floor(at.z);
        return new Vec3(at.x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), at.z);
    }

    private static boolean clear(ServerLevel level, Vec3 a, Vec3 b) {
        return level.clip(new net.minecraft.world.level.ClipContext(a, b, net.minecraft.world.level.ClipContext.Block.VISUAL,
            net.minecraft.world.level.ClipContext.Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty())).getType()
            == net.minecraft.world.phys.HitResult.Type.MISS;
    }

    private static void start(int count, String name) {
        prefix = name;
        index = 0;
        frames = count;
        next = System.nanoTime();
        recording = true;
    }

    @SubscribeEvent
    public static void onFrame(RenderFrameEvent.Post event) {
        if (!recording) {
            return;
        }
        long now = System.nanoTime();
        if (now < next) {
            return;
        }
        next += FRAME;
        if (next < now) {
            next = now + FRAME;
        }
        Minecraft mc = Minecraft.getInstance();
        Screenshot.grab(mc.gameDirectory, String.format(Locale.ROOT, "%s_%03d.png", prefix, index), mc.gameRenderer.mainRenderTarget(), 1, msg -> {});
        if (++index >= frames) {
            recording = false;
        }
    }

    private static void run(ServerPlayer sp, String cmd) {
        sp.level().getServer().getCommands().performPrefixedCommand(sp.createCommandSourceStack().withPermission(LevelBasedPermissionSet.OWNER), cmd);
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
                try {
                    action.accept(sp);
                } catch (RuntimeException e) {
                    TheabyssDevTest.LOGGER.error("[ABYSS-AUTOTEST] meteo server step failed", e);
                }
            }
        });
    }
}
