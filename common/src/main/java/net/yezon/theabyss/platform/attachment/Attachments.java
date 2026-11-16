package net.yezon.theabyss.platform.attachment;

import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.platform.Platform;
import org.jspecify.annotations.Nullable;

public final class Attachments {
    private Attachments() {
    }

    public static <T> T getData(Entity holder, Supplier<AttachmentType<T>> type) {
        return Platform.get().getData(holder, type.get());
    }

    public static <T> @Nullable T setData(Entity holder, Supplier<AttachmentType<T>> type, T value) {
        return Platform.get().setData(holder, type.get(), value);
    }

    public static boolean hasData(Entity holder, Supplier<? extends AttachmentType<?>> type) {
        return Platform.get().hasData(holder, type.get());
    }

    public static void syncData(Entity holder, Supplier<? extends AttachmentType<?>> type) {
        Platform.get().syncData(holder, type.get());
    }
}
