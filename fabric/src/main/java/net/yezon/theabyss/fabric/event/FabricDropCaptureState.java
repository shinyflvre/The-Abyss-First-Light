package net.yezon.theabyss.fabric.event;

import java.util.Collection;
import net.minecraft.world.entity.item.ItemEntity;
import org.jspecify.annotations.Nullable;

public interface FabricDropCaptureState {
    @Nullable Collection<ItemEntity> theabyss$capturedDrops();

    @Nullable Collection<ItemEntity> theabyss$captureDrops(@Nullable Collection<ItemEntity> drops);
}
