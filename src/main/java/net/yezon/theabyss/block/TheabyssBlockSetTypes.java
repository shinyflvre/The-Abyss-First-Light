package net.yezon.theabyss.block;

import net.minecraft.world.level.block.state.properties.BlockSetType;

public final class TheabyssBlockSetTypes {
    public static final BlockSetType IRON_MOBS = BlockSetType.register(new BlockSetType(
        "theabyss:iron_mobs",
        BlockSetType.IRON.canOpenByHand(),
        BlockSetType.IRON.canOpenByWindCharge(),
        BlockSetType.IRON.canButtonBeActivatedByArrows(),
        BlockSetType.PressurePlateSensitivity.MOBS,
        BlockSetType.IRON.soundType(),
        BlockSetType.IRON.doorClose(),
        BlockSetType.IRON.doorOpen(),
        BlockSetType.IRON.trapdoorClose(),
        BlockSetType.IRON.trapdoorOpen(),
        BlockSetType.IRON.pressurePlateClickOff(),
        BlockSetType.IRON.pressurePlateClickOn(),
        BlockSetType.IRON.buttonClickOff(),
        BlockSetType.IRON.buttonClickOn()
    ));

    private TheabyssBlockSetTypes() {
    }
}
