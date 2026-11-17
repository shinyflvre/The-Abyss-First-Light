package net.yezon.theabyss.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelResource;
import net.yezon.theabyss.TheabyssMod;
import org.jspecify.annotations.Nullable;

public final class AbyssSavedData {
    private static final String SUFFIX = ".dat";

    private AbyssSavedData() {
    }

    public static <T extends SavedData> T server(MinecraftServer server, String id, SavedData.Factory<T> factory, String legacyName) {
        Path target = root(server).resolve("data").resolve(TheabyssMod.MODID).resolve(id + SUFFIX);
        return get(server.overworld(), target, factory, legacyName);
    }

    public static <T extends SavedData> T level(ServerLevel level, String id, SavedData.Factory<T> factory, String legacyName) {
        ResourceLocation dimension = level.dimension().location();
        Path target = root(level.getServer()).resolve("dimensions").resolve(dimension.getNamespace()).resolve(dimension.getPath()).resolve("data")
            .resolve(TheabyssMod.MODID).resolve(id + SUFFIX);
        return get(level, target, factory, legacyName);
    }

    private static <T extends SavedData> T get(ServerLevel host, Path target, SavedData.Factory<T> factory, String legacyName) {
        DimensionDataStorage storage = host.getDataStorage();
        Path folder = DimensionType.getStorageFolder(host.dimension(), root(host.getServer())).resolve("data");
        String name = name(folder, target);
        T data = storage.get(factory, name);
        if (data != null) {
            return data;
        }
        T created = Files.exists(target) ? null : legacy(host, storage, folder, factory, legacyName);
        if (created == null) {
            created = factory.constructor().get();
        }
        try {
            Files.createDirectories(target.getParent());
        } catch (IOException e) {
            TheabyssMod.LOGGER.error("Could not create saved data folder {}", target.getParent(), e);
        }
        storage.set(name, created);
        created.setDirty();
        return created;
    }

    private static <T extends SavedData> @Nullable T legacy(ServerLevel host, DimensionDataStorage storage, Path folder, SavedData.Factory<T> factory,
        String legacyName) {
        if (!Files.exists(folder.resolve(legacyName + SUFFIX))) {
            return null;
        }
        try {
            CompoundTag tag = storage.readTagFromDisk(legacyName, factory.type(), SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            return factory.deserializer().apply(tag.getCompound("data"), host.registryAccess());
        } catch (Exception e) {
            TheabyssMod.LOGGER.error("Error loading saved data: {}", legacyName, e);
            return null;
        }
    }

    private static Path root(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
    }

    private static String name(Path folder, Path target) {
        Path relative = folder.toAbsolutePath().normalize().relativize(target.toAbsolutePath().normalize());
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < relative.getNameCount(); i++) {
            if (i > 0) {
                name.append('/');
            }
            name.append(relative.getName(i));
        }
        return name.substring(0, name.length() - SUFFIX.length());
    }
}
