package net.yezon.theabyss.platform.fml.loading;

import java.nio.file.Path;
import net.yezon.theabyss.platform.Platform;

public enum FMLPaths {
    CONFIGDIR;

    public Path get() {
        return Platform.get().configDir();
    }
}
