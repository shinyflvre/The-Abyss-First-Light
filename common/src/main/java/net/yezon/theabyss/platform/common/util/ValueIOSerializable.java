package net.yezon.theabyss.platform.common.util;

import net.yezon.theabyss.compat.ValueInput;
import net.yezon.theabyss.compat.ValueOutput;

public interface ValueIOSerializable {
    void serialize(ValueOutput output);

    void deserialize(ValueInput input);
}
