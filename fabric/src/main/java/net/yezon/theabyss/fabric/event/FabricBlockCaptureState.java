package net.yezon.theabyss.fabric.event;

import java.util.List;

public interface FabricBlockCaptureState {
    boolean theabyss$capturing();

    void theabyss$setCapturing(boolean capturing);

    boolean theabyss$restoring();

    void theabyss$setRestoring(boolean restoring);

    List<FabricBlockSnapshot> theabyss$snapshots();
}
