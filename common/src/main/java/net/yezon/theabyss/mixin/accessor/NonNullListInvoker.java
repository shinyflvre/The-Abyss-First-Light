package net.yezon.theabyss.mixin.accessor;

import java.util.List;
import net.minecraft.core.NonNullList;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NonNullList.class)
public interface NonNullListInvoker {
    @Invoker("<init>")
    static <E> NonNullList<E> theabyss$create(List<E> list, @Nullable E defaultValue) {
        throw new AssertionError();
    }
}
