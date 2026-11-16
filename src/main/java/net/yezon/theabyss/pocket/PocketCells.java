package net.yezon.theabyss.pocket;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;

public final class PocketCells extends SavedData {
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, Identifier.parse("theabyss:pocket_dimension"));
    public static final int BOX = 32;
    public static final int RING = 16;
    public static final int CELL = RING + BOX + RING;
    public static final int PITCH = 512;
    public static final int ROW = 64;
    public static final int FIRST_ROW = 4;
    public static final int FLOOR = 64;
    public static final int TOP_BLOCK = FLOOR + 32;
    public static final double CEILING = TOP_BLOCK + 3.0;
    public static final int BOTTOM = 40;

    private static final Codec<PocketCells> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.optionalFieldOf("next", 0).forGetter(v -> v.next),
        Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT).optionalFieldOf("owners", Map.of()).forGetter(v -> v.owners)
    ).apply(i, PocketCells::new));
    public static final SavedDataType<PocketCells> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "pocket_cells"), PocketCells::new, CODEC
    );

    private int next;
    private final Map<UUID, Integer> owners;

    public PocketCells() {
        this(0, Map.of());
    }

    private PocketCells(int next, Map<UUID, Integer> owners) {
        this.next = next;
        this.owners = new HashMap<>(owners);
    }

    public static PocketCells get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public int find(UUID player) {
        Integer cell = this.owners.get(player);
        return cell == null ? -1 : cell;
    }

    public synchronized int cellOf(UUID player) {
        Integer cell = this.owners.get(player);
        if (cell != null) {
            return cell;
        }
        int created = this.next++;
        this.owners.put(player, created);
        this.setDirty();
        TheabyssMod.LOGGER.info("Pocket box: cell {} for {}", created, player);
        return created;
    }

    public static int originX(int cell) {
        return (cell % ROW) * PITCH;
    }

    public static int originZ(int cell) {
        return (FIRST_ROW + cell / ROW) * PITCH;
    }

    public static int boxX(int cell) {
        return originX(cell) + RING;
    }

    public static int boxZ(int cell) {
        return originZ(cell) + RING;
    }

    public static double centreX(int cell) {
        return boxX(cell) + BOX / 2.0;
    }

    public static double centreZ(int cell) {
        return boxZ(cell) + BOX / 2.0;
    }

    public static int at(double x, double z) {
        int gx = Math.floorDiv(Mth.floor(x), PITCH);
        int gz = Math.floorDiv(Mth.floor(z), PITCH) - FIRST_ROW;
        if (gx < 0 || gx >= ROW || gz < 0) {
            return -1;
        }
        int cell = gz * ROW + gx;
        double u = x - originX(cell);
        double v = z - originZ(cell);
        return u < CELL && v < CELL ? cell : -1;
    }

    public static boolean inBox(int cell, double x, double z, double margin) {
        double u = x - boxX(cell);
        double v = z - boxZ(cell);
        return u >= -margin && u <= BOX + margin && v >= -margin && v <= BOX + margin;
    }

    public static Vec3 arrival(int cell) {
        return new Vec3(centreX(cell), FLOOR + 1, boxZ(cell) + 2.5);
    }

    public static final float ARRIVAL_YAW = 0.0F;
}
