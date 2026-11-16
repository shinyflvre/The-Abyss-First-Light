package net.yezon.theabyss.client.debug;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.client.event.ViewportEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class OreHighlighter {
    private static final int RADIUS_CHUNKS = 5;
    private static final int RESCAN_TICKS = 30;
    private static final int MAX_OUTLINES = 6000;
    private static final Map<Block, Integer> TARGETS = new LinkedHashMap<>();
    private static final List<Found> FOUND = new ArrayList<>();
    private static int rescan;
    private static ClientLevel scannedLevel;

    record Found(BlockPos pos, int color, Block block) {
    }

    private OreHighlighter() {
    }

    public static boolean active() {
        return !TARGETS.isEmpty();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFog(ViewportEvent.RenderFog event) {
        if (active()) {
            event.setNearPlaneDistance(9000.0F);
            event.setFarPlaneDistance(10000.0F);
        }
    }

    public static List<Block> ores(boolean crystals) {
        List<Block> ores = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            if (id.getNamespace().equals(TheabyssMod.MODID) && id.getPath().endsWith("_ore") && id.getPath().endsWith("_crystal_ore") == crystals) {
                ores.add(block);
            }
        }
        ores.sort(Comparator.comparing(b -> BuiltInRegistries.BLOCK.getKey(b).getPath()));
        return ores;
    }

    public static List<String> suggestions(boolean crystals) {
        List<String> names = new ArrayList<>(List.of("all", "reset"));
        for (Block ore : ores(crystals)) {
            names.add(BuiltInRegistries.BLOCK.getKey(ore).getPath());
        }
        return names;
    }

    public static int command(boolean crystals, String argument) {
        Minecraft mc = Minecraft.getInstance();
        String kind = crystals ? "crystal" : "ore";
        List<Block> ores = ores(crystals);
        TARGETS.keySet().removeIf(ores::contains);
        if (argument.equals("reset")) {
            FOUND.clear();
            rescan = 0;
            say(mc, Component.literal("Highlight off for " + kind + "s"));
            return 1;
        }
        if (argument.equals("all")) {
            for (int i = 0; i < ores.size(); i++) {
                float hue = (i * 0.618034F + (crystals ? 0.31F : 0.0F)) % 1.0F;
                TARGETS.put(ores.get(i), 0xFF000000 | Mth.hsvToRgb(hue, 0.85F, 1.0F));
            }
        } else {
            Block match = null;
            for (Block ore : ores) {
                if (BuiltInRegistries.BLOCK.getKey(ore).getPath().equals(argument)) {
                    match = ore;
                }
            }
            if (match == null) {
                List<String> names = suggestions(crystals);
                say(mc, Component.literal("Unknown " + kind + ": " + argument + " (use all, reset or one of " + names.subList(2, names.size()) + ")"));
                return 0;
            }
            TARGETS.put(match, crystals ? 0xFFFFD23D : 0xFFFF40E0);
        }
        rescan = 0;
        mc.schedule(() -> {
            scan(mc);
            MutableComponent legend = Component.literal("Highlighting within " + RADIUS_CHUNKS * 16 + " blocks: ");
            boolean first = true;
            for (Map.Entry<Block, Integer> entry : TARGETS.entrySet()) {
                if (!ores.contains(entry.getKey())) {
                    continue;
                }
                long count = FOUND.stream().filter(f -> f.block() == entry.getKey()).count();
                if (!first) {
                    legend.append(Component.literal(", "));
                }
                first = false;
                legend.append(Component.literal(BuiltInRegistries.BLOCK.getKey(entry.getKey()).getPath() + " " + count)
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(entry.getValue() & 0xFFFFFF))));
            }
            say(mc, legend);
        });
        return 1;
    }

    private static void say(Minecraft mc, Component message) {
        mc.gui.hud.getChat().addClientSystemMessage(message);
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (TARGETS.isEmpty() || mc.level == null || mc.player == null) {
            return;
        }
        if (mc.level != scannedLevel || --rescan <= 0) {
            scan(mc);
        }
        for (Found found : FOUND) {
            Gizmos.cuboid(new AABB(found.pos()).inflate(0.003), GizmoStyle.strokeAndFill(found.color(), 3.0F, (found.color() & 0xFFFFFF) | 0x55000000))
                .setAlwaysOnTop();
        }
    }

    private static void scan(Minecraft mc) {
        rescan = RESCAN_TICKS;
        scannedLevel = mc.level;
        FOUND.clear();
        if (mc.level == null || mc.player == null || TARGETS.isEmpty()) {
            return;
        }
        ClientLevel level = mc.level;
        BlockPos center = mc.player.blockPosition();
        int ccx = center.getX() >> 4;
        int ccz = center.getZ() >> 4;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int cx = ccx - RADIUS_CHUNKS; cx <= ccx + RADIUS_CHUNKS; cx++) {
            for (int cz = ccz - RADIUS_CHUNKS; cz <= ccz + RADIUS_CHUNKS; cz++) {
                if (!level.hasChunk(cx, cz)) {
                    continue;
                }
                LevelChunk chunk = level.getChunk(cx, cz);
                LevelChunkSection[] sections = chunk.getSections();
                for (int i = 0; i < sections.length; i++) {
                    LevelChunkSection section = sections[i];
                    if (section.hasOnlyAir() || !section.getStates().maybeHas(state -> TARGETS.containsKey(state.getBlock()))) {
                        continue;
                    }
                    int baseY = level.getSectionYFromSectionIndex(i) << 4;
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            for (int x = 0; x < 16; x++) {
                                Block block = section.getBlockState(x, y, z).getBlock();
                                Integer color = TARGETS.get(block);
                                if (color != null) {
                                    FOUND.add(new Found(pos.set((cx << 4) + x, baseY + y, (cz << 4) + z).immutable(), color, block));
                                }
                            }
                        }
                    }
                }
            }
        }
        if (FOUND.size() > MAX_OUTLINES) {
            FOUND.sort(Comparator.comparingDouble(f -> f.pos().distSqr(center)));
            FOUND.subList(MAX_OUTLINES, FOUND.size()).clear();
        }
    }
}
