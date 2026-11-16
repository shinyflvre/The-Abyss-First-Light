package net.yezon.theabyss.client.fx;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.configuration.ConfigConfiguration;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class ColoredLight {
    public static final Identifier[] VOLUME_TEXTURES = {
        Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "textures/effect/light_volume.png"),
        Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "textures/effect/light_volume_b.png")
    };
    private static final long FADE_MILLIS = 750L;
    static final int SIZE_XZ = 96;
    static final int SIZE_Y = 48;
    private static final int TILES_X = 8;
    private static final int WIDTH = SIZE_XZ * TILES_X;
    private static final int HEIGHT = SIZE_XZ * (SIZE_Y / TILES_X);
    private static final int CELLS = SIZE_XZ * SIZE_Y * SIZE_XZ;
    private static final int REBUILD_TICKS = 15;

    private record Snapshot(Level level, BlockPos origin, PalettedContainer<BlockState>[] sections, int minSectionX, int minSectionY,
        int minSectionZ, int sectionsY, int sectionsZ, int[] heights, Map<Block, Integer> colours, float spread, float minReach,
        float maxReach) {
    }

    private static final int UNITS_PER_BLOCK = 4;

    private record Volume(Level level, BlockPos origin, NativeImage image) {
    }

    private static final DynamicTexture[] textures = new DynamicTexture[2];
    private static final BlockPos[] origins = {BlockPos.ZERO, BlockPos.ZERO};
    private static int newest;
    private static long arrivedAt;
    private static volatile Volume finished;
    private static CompletableFuture<Void> running;
    private static Level builtFor;
    private static boolean ready;
    private static Map<Block, Integer> colours;
    private static Map<Block, Integer> textureColours = new IdentityHashMap<>();
    private static boolean failed;

    private static final byte[] DAMPENING = new byte[CELLS];
    private static final byte[] SOURCE = new byte[CELLS];
    private static final byte[] LEVEL = new byte[CELLS];
    private static final byte[] RED = new byte[CELLS];
    private static final byte[] GREEN = new byte[CELLS];
    private static final byte[] BLUE = new byte[CELLS];
    private static final IntArrayList[] BUCKETS = new IntArrayList[256];

    static {
        for (int i = 0; i < BUCKETS.length; i++) {
            BUCKETS[i] = new IntArrayList();
        }
    }

    private ColoredLight() {
    }

    public static boolean enabled() {
        try {
            return ConfigConfiguration.COLORED_LIGHT_ENGINE.get();
        } catch (IllegalStateException notLoadedYet) {
            return false;
        }
    }

    public static boolean active() {
        return ready && enabled();
    }

    @SubscribeEvent
    public static void onAtlasStitched(TextureAtlasStitchedEvent event) {
        if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            colours = null;
            textureColours = new IdentityHashMap<>();
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!enabled() || mc.level == null || mc.player == null || failed) {
            ready = false;
            builtFor = null;
            discardFinished();
            return;
        }
        Volume done = finished;
        if (done != null) {
            finished = null;
            if (done.level == mc.level) {
                ensureTextures(mc);
                int slot = 1 - newest;
                if (!ready) {
                    clear(newest);
                    origins[newest] = done.origin;
                }
                textures[slot].setPixels(done.image);
                textures[slot].upload();
                origins[slot] = done.origin;
                newest = slot;
                arrivedAt = Util.getMillis();
                builtFor = done.level;
                ready = true;
            } else {
                done.image.close();
            }
        }
        if (builtFor != mc.level) {
            ready = false;
        }
        if (mc.player.tickCount % REBUILD_TICKS == 0 && (running == null || running.isDone())) {
            Snapshot snapshot = snapshot(mc);
            running = CompletableFuture.runAsync(() -> {
                try {
                    finished = build(snapshot);
                } catch (RuntimeException e) {
                    failed = true;
                    TheabyssMod.LOGGER.error("[ColoredLight] light volume build failed, coloured light engine disabled for this session", e);
                }
            }, Util.backgroundExecutor());
        }
    }

    private static void ensureTextures(Minecraft mc) {
        for (int i = 0; i < textures.length; i++) {
            if (textures[i] == null) {
                textures[i] = new DynamicTexture(() -> "theabyss light volume", WIDTH, HEIGHT, true);
                mc.getTextureManager().register(VOLUME_TEXTURES[i], textures[i]);
            }
        }
    }

    private static void clear(int slot) {
        textures[slot].setPixels(new NativeImage(WIDTH, HEIGHT, true));
        textures[slot].upload();
    }

    private static void discardFinished() {
        Volume stale = finished;
        if (stale != null) {
            finished = null;
            stale.image.close();
        }
    }

    @SuppressWarnings("unchecked")
    private static Snapshot snapshot(Minecraft mc) {
        Level level = mc.level;
        BlockPos camera = mc.gameRenderer.mainCamera().blockPosition().immutable();
        BlockPos min = camera.offset(-SIZE_XZ / 2, -SIZE_Y / 2, -SIZE_XZ / 2);
        int minSectionX = SectionPos.blockToSectionCoord(min.getX());
        int minSectionY = SectionPos.blockToSectionCoord(min.getY());
        int minSectionZ = SectionPos.blockToSectionCoord(min.getZ());
        int sectionsX = SectionPos.blockToSectionCoord(min.getX() + SIZE_XZ - 1) - minSectionX + 1;
        int sectionsY = SectionPos.blockToSectionCoord(min.getY() + SIZE_Y - 1) - minSectionY + 1;
        int sectionsZ = SectionPos.blockToSectionCoord(min.getZ() + SIZE_XZ - 1) - minSectionZ + 1;
        PalettedContainer<BlockState>[] sections = new PalettedContainer[sectionsX * sectionsY * sectionsZ];
        int[] heights = new int[SIZE_XZ * SIZE_XZ];
        java.util.Arrays.fill(heights, Integer.MIN_VALUE);
        for (int sx = 0; sx < sectionsX; sx++) {
            for (int sz = 0; sz < sectionsZ; sz++) {
                LevelChunk chunk = level.getChunkSource().getChunk(minSectionX + sx, minSectionZ + sz, false);
                if (chunk == null) {
                    continue;
                }
                for (int sy = 0; sy < sectionsY; sy++) {
                    int sectionY = minSectionY + sy;
                    if (sectionY < level.getMinSectionY() || sectionY > level.getMaxSectionY()) {
                        continue;
                    }
                    LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(sectionY));
                    if (!section.hasOnlyAir()) {
                        sections[(sx * sectionsY + sy) * sectionsZ + sz] = section.getStates().copy();
                    }
                }
                for (int lx = 0; lx < 16; lx++) {
                    int x = (minSectionX + sx) * 16 + lx - min.getX();
                    if (x < 0 || x >= SIZE_XZ) {
                        continue;
                    }
                    for (int lz = 0; lz < 16; lz++) {
                        int z = (minSectionZ + sz) * 16 + lz - min.getZ();
                        if (z >= 0 && z < SIZE_XZ) {
                            heights[z * SIZE_XZ + x] = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, lx, lz);
                        }
                    }
                }
            }
        }
        int minReach = ConfigConfiguration.LIGHT_SPREAD_MIN.get();
        int maxReach = Math.max(minReach, ConfigConfiguration.LIGHT_SPREAD_MAX.get());
        return new Snapshot(level, min, sections, minSectionX, minSectionY, minSectionZ, sectionsY, sectionsZ, heights, colourTable(),
            ConfigConfiguration.LIGHT_SPREAD.get() / 100.0F, minReach, maxReach);
    }

    public static final net.minecraft.tags.TagKey<Block> NO_COLORED_LIGHT = net.minecraft.tags.TagKey.create(
        net.minecraft.core.registries.Registries.BLOCK, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "no_colored_light"));

    @SubscribeEvent
    public static void onTagsUpdated(net.neoforged.neoforge.event.TagsUpdatedEvent event) {
        colours = null;
    }

    private static final float FLUID_REACH = 2.5F;
    private static final float FLUID_BRIGHTNESS = 0.55F;
    private static final int FLUID_FLAG = 1 << 28;
    private static final int SELF_LIT_FLAG = 1 << 29;

    private static Map<Block, Integer> colourTable() {
        if (colours == null) {
            Map<Block, Integer> table = new IdentityHashMap<>();
            for (Block block : BuiltInRegistries.BLOCK) {
                if (!TheabyssMod.MODID.equals(BuiltInRegistries.BLOCK.getKey(block).getNamespace()) || block.defaultBlockState().is(NO_COLORED_LIGHT)) {
                    continue;
                }
                int flag = (block instanceof LiquidBlock ? FLUID_FLAG : 0)
                    | (FIXED_COLOURS.containsKey(BuiltInRegistries.BLOCK.getKey(block).getPath()) ? SELF_LIT_FLAG : 0);
                block.getStateDefinition().getPossibleStates().stream().filter(s -> s.getLightEmission() > 0).findFirst()
                    .ifPresent(lit -> table.put(block, colour(lit) | flag));
            }
            colours = table;
        }
        return colours;
    }

    private static final Map<String, Integer> FIXED_COLOURS = Map.of("abyssal_energetica", 0x5FF0E6, "arcane_workbench", 0x5FF0E6);

    static int colour(BlockState state) {
        Integer fixed = FIXED_COLOURS.get(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath());
        if (fixed != null) {
            return fixed;
        }
        return textureColours.computeIfAbsent(state.getBlock(), block -> {
            TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();
            NativeImage image = sprite.contents().getOriginalImage();
            double r = 0;
            double g = 0;
            double b = 0;
            double weight = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getPixel(x, y);
                    if (ARGB.alpha(argb) < 128) {
                        continue;
                    }
                    float pr = ARGB.red(argb) / 255.0F;
                    float pg = ARGB.green(argb) / 255.0F;
                    float pb = ARGB.blue(argb) / 255.0F;
                    float max = Math.max(pr, Math.max(pg, pb));
                    float saturation = max - Math.min(pr, Math.min(pg, pb));
                    double w = Math.pow(saturation * max, 2.0) + 0.02 * max * max;
                    r += pr * w;
                    g += pg * w;
                    b += pb * w;
                    weight += w;
                }
            }
            if (weight <= 0.0) {
                return 0xFFFFFF;
            }
            r /= weight;
            g /= weight;
            b /= weight;
            double max = Math.max(r, Math.max(g, b));
            if (max <= 0.001) {
                return 0xFFFFFF;
            }
            r /= max;
            g /= max;
            b /= max;
            double grey = (r + g + b) / 3.0;
            r = Mth.clamp(grey + (r - grey) * 1.25, 0.0, 1.0);
            g = Mth.clamp(grey + (g - grey) * 1.25, 0.0, 1.0);
            b = Mth.clamp(grey + (b - grey) * 1.25, 0.0, 1.0);
            return ARGB.color(0, (int)Math.round(r * 255), (int)Math.round(g * 255), (int)Math.round(b * 255)) & 0xFFFFFF;
        });
    }

    private static Volume build(Snapshot s) {
        BlockPos min = s.origin;
        BlockState air = Blocks.AIR.defaultBlockState();
        Int2ObjectMap<IntArrayList> seeds = new Int2ObjectOpenHashMap<>();
        for (int y = 0; y < SIZE_Y; y++) {
            int wy = min.getY() + y;
            int sy = (wy >> 4) - s.minSectionY;
            for (int z = 0; z < SIZE_XZ; z++) {
                int wz = min.getZ() + z;
                int sz = (wz >> 4) - s.minSectionZ;
                for (int x = 0; x < SIZE_XZ; x++) {
                    int wx = min.getX() + x;
                    int sx = (wx >> 4) - s.minSectionX;
                    PalettedContainer<BlockState> section = s.sections[(sx * s.sectionsY + sy) * s.sectionsZ + sz];
                    BlockState state = section == null ? air : section.get(wx & 15, wy & 15, wz & 15);
                    int cell = (y * SIZE_XZ + z) * SIZE_XZ + x;
                    DAMPENING[cell] = (byte)Math.min(15, state.getLightDampening());
                    SOURCE[cell] = 0;
                    int emission = state.getLightEmission();
                    if (emission > 0) {
                        Integer colour = s.colours.get(state.getBlock());
                        if (colour != null) {
                            SOURCE[cell] = (byte)((colour & SELF_LIT_FLAG) != 0 ? 0 : 1);
                            int key = colour & 0xFFFFFF | Math.min(emission, 15) << 24 | colour & FLUID_FLAG;
                            seeds.computeIfAbsent(key, c -> new IntArrayList()).add(cell);
                        }
                    }
                }
            }
        }
        java.util.Arrays.fill(RED, (byte)0);
        java.util.Arrays.fill(GREEN, (byte)0);
        java.util.Arrays.fill(BLUE, (byte)0);
        IntArrayList touched = new IntArrayList();
        for (Int2ObjectMap.Entry<IntArrayList> entry : seeds.int2ObjectEntrySet()) {
            int key = entry.getIntKey();
            int emission = key >>> 24 & 15;
            boolean fluid = (key & FLUID_FLAG) != 0;
            int red = key >> 16 & 255;
            int green = key >> 8 & 255;
            int blue = key & 255;
            float reachBlocks = Mth.clamp(emission * s.spread, s.minReach, s.maxReach);
            if (fluid) {
                reachBlocks = Math.min(reachBlocks, FLUID_REACH);
            }
            int reach = Mth.clamp(Math.round(reachBlocks * UNITS_PER_BLOCK), UNITS_PER_BLOCK, BUCKETS.length - 1);
            float scale = emission / 15.0F / reach * (fluid ? FLUID_BRIGHTNESS : 1.0F);
            IntArrayList list = entry.getValue();
            for (int i = 0; i < list.size(); i++) {
                int cell = list.getInt(i);
                if (reach > (LEVEL[cell] & 255)) {
                    if (LEVEL[cell] == 0) {
                        touched.add(cell);
                    }
                    LEVEL[cell] = (byte)reach;
                    BUCKETS[reach].add(cell);
                }
            }
            for (int level = reach; level >= 1; level--) {
                IntArrayList bucket = BUCKETS[level];
                for (int i = 0; i < bucket.size(); i++) {
                    int cell = bucket.getInt(i);
                    if ((LEVEL[cell] & 255) != level) {
                        continue;
                    }
                    float brightness = level * scale;
                    RED[cell] = (byte)Math.max(RED[cell] & 255, Math.round(red * brightness));
                    GREEN[cell] = (byte)Math.max(GREEN[cell] & 255, Math.round(green * brightness));
                    BLUE[cell] = (byte)Math.max(BLUE[cell] & 255, Math.round(blue * brightness));
                    if (level <= UNITS_PER_BLOCK) {
                        continue;
                    }
                    int x = cell % SIZE_XZ;
                    int z = cell / SIZE_XZ % SIZE_XZ;
                    int y = cell / (SIZE_XZ * SIZE_XZ);
                    if (x > 0) spread(cell - 1, level, touched);
                    if (x < SIZE_XZ - 1) spread(cell + 1, level, touched);
                    if (z > 0) spread(cell - SIZE_XZ, level, touched);
                    if (z < SIZE_XZ - 1) spread(cell + SIZE_XZ, level, touched);
                    if (y > 0) spread(cell - SIZE_XZ * SIZE_XZ, level, touched);
                    if (y < SIZE_Y - 1) spread(cell + SIZE_XZ * SIZE_XZ, level, touched);
                }
                bucket.clear();
            }
            for (int i = 0; i < touched.size(); i++) {
                LEVEL[touched.getInt(i)] = 0;
            }
            touched.clear();
        }
        NativeImage image = new NativeImage(WIDTH, HEIGHT, false);
        for (int y = 0; y < SIZE_Y; y++) {
            int tileX = y % TILES_X * SIZE_XZ;
            int tileY = y / TILES_X * SIZE_XZ;
            int wy = min.getY() + y;
            for (int z = 0; z < SIZE_XZ; z++) {
                for (int x = 0; x < SIZE_XZ; x++) {
                    int cell = (y * SIZE_XZ + z) * SIZE_XZ + x;
                    int alpha = (wy > s.heights[z * SIZE_XZ + x] ? 254 : 0) | SOURCE[cell];
                    image.setPixelABGR(tileX + x, tileY + z, alpha << 24 | (BLUE[cell] & 255) << 16 | (GREEN[cell] & 255) << 8 | RED[cell] & 255);
                }
            }
        }
        return new Volume(s.level, min, image);
    }

    private static void spread(int cell, int level, IntArrayList touched) {
        int dampening = DAMPENING[cell];
        if (dampening >= 15) {
            return;
        }
        int next = level - Math.max(1, dampening) * UNITS_PER_BLOCK;
        if (next > (LEVEL[cell] & 255)) {
            if (LEVEL[cell] == 0) {
                touched.add(cell);
            }
            LEVEL[cell] = (byte)next;
            BUCKETS[next].add(cell);
        }
    }

    static Vec3 originRelativeTo(int slot, Vec3 position) {
        BlockPos origin = origins[slot];
        return new Vec3(origin.getX() - position.x, origin.getY() - position.y, origin.getZ() - position.z);
    }

    static float weightOfSlot1() {
        float k = Mth.clamp((Util.getMillis() - arrivedAt) / (float)FADE_MILLIS, 0.0F, 1.0F);
        k = k * k * (3.0F - 2.0F * k);
        return newest == 1 ? k : 1.0F - k;
    }

    static float daylight(Minecraft mc) {
        if (mc.level == null || !mc.level.dimensionType().hasSkyLight()) {
            return 0.0F;
        }
        return Mth.clamp((11.0F - mc.level.getSkyDarken()) / 11.0F, 0.0F, 1.0F);
    }
}
