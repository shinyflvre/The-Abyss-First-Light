package net.yezon.theabyss.gravity;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.LongConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class GravityIndex {
    private static final Map<Level, Long2ObjectOpenHashMap<LongOpenHashSet>> SERVER = new WeakHashMap<>();
    private static final Map<Level, Long2ObjectOpenHashMap<LongOpenHashSet>> CLIENT = new WeakHashMap<>();

    private GravityIndex() {
    }

    private static Map<Level, Long2ObjectOpenHashMap<LongOpenHashSet>> side(Level level) {
        return level.isClientSide() ? CLIENT : SERVER;
    }

    private static boolean offThread(Level level, Runnable retry) {
        MinecraftServer server = level.getServer();
        if (level.isClientSide() || server == null || server.isSameThread()) {
            return false;
        }
        server.execute(retry);
        return true;
    }

    public static void add(Level level, BlockPos pos) {
        BlockPos at = pos.immutable();
        if (offThread(level, () -> add(level, at))) {
            return;
        }
        side(level).computeIfAbsent(level, l -> new Long2ObjectOpenHashMap<>())
            .computeIfAbsent(SectionPos.asLong(pos), k -> new LongOpenHashSet())
            .add(pos.asLong());
    }

    public static void remove(Level level, BlockPos pos) {
        BlockPos at = pos.immutable();
        if (offThread(level, () -> remove(level, at))) {
            return;
        }
        Long2ObjectOpenHashMap<LongOpenHashSet> sections = side(level).get(level);
        if (sections == null) {
            return;
        }
        long key = SectionPos.asLong(pos);
        LongOpenHashSet set = sections.get(key);
        if (set != null && set.remove(pos.asLong()) && set.isEmpty()) {
            sections.remove(key);
        }
    }

    public static boolean any(Level level) {
        Long2ObjectOpenHashMap<LongOpenHashSet> sections = side(level).get(level);
        return sections != null && !sections.isEmpty();
    }

    public static void sections(Level level, LongConsumer out) {
        Long2ObjectOpenHashMap<LongOpenHashSet> sections = side(level).get(level);
        if (sections == null) {
            return;
        }
        for (LongIterator it = sections.keySet().iterator(); it.hasNext(); ) {
            out.accept(it.nextLong());
        }
    }

    public static void positions(Level level, long section, LongConsumer out) {
        Long2ObjectOpenHashMap<LongOpenHashSet> sections = side(level).get(level);
        LongOpenHashSet set = sections != null ? sections.get(section) : null;
        if (set == null) {
            return;
        }
        for (LongIterator it = set.iterator(); it.hasNext(); ) {
            out.accept(it.nextLong());
        }
    }

    public static void collect(Level level, AABB box, LongArrayList out) {
        Long2ObjectOpenHashMap<LongOpenHashSet> sections = side(level).get(level);
        if (sections == null || sections.isEmpty()) {
            return;
        }
        int minX = Mth.floor(box.minX), minY = Mth.floor(box.minY), minZ = Mth.floor(box.minZ);
        int maxX = Mth.floor(box.maxX), maxY = Mth.floor(box.maxY), maxZ = Mth.floor(box.maxZ);
        for (int sx = SectionPos.blockToSectionCoord(minX); sx <= SectionPos.blockToSectionCoord(maxX); sx++) {
            for (int sy = SectionPos.blockToSectionCoord(minY); sy <= SectionPos.blockToSectionCoord(maxY); sy++) {
                for (int sz = SectionPos.blockToSectionCoord(minZ); sz <= SectionPos.blockToSectionCoord(maxZ); sz++) {
                    LongOpenHashSet set = sections.get(SectionPos.asLong(sx, sy, sz));
                    if (set == null) {
                        continue;
                    }
                    for (LongIterator it = set.iterator(); it.hasNext(); ) {
                        long packed = it.nextLong();
                        int x = BlockPos.getX(packed), y = BlockPos.getY(packed), z = BlockPos.getZ(packed);
                        if (x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ) {
                            out.add(packed);
                        }
                    }
                }
            }
        }
    }
}
