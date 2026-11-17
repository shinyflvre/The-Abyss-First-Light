package net.yezon.theabyss.ring;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.TheabyssModVariables;

public final class RingWear {
    public static final String KEY = "theabyss_wear";
    public static final int MAX_LEVEL = 5;

    private RingWear() {
    }

    public static int level(double manaUpgrade) {
        return Mth.clamp(1 + (int)Math.round((1.0 - manaUpgrade) * 10.0), 1, MAX_LEVEL);
    }

    public static int level(Entity user) {
        return user == null ? 1 : level(TheabyssModVariables.getPlayerVariables(user).ManaUpgrade);
    }

    public static boolean isRing(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return TheabyssMod.MODID.equals(id.getNamespace()) && id.getPath().startsWith("ring_of_") && !id.getPath().endsWith("_attack");
    }

    public static int pending(ItemStack stack) {
        return net.yezon.theabyss.compat.NbtCompat.getIntOr(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(), KEY, 0);
    }

    public static void wear(ItemStack stack, ServerLevel level, Entity user, int uses) {
        if (stack.isEmpty() || !stack.isDamageableItem()) {
            return;
        }
        int perPoint = level(user);
        int total = pending(stack) + uses;
        int damage = total / perPoint;
        int left = total % perPoint;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(KEY, left));
        if (damage > 0) {
            stack.hurtAndBreak(damage, level, user instanceof ServerPlayer player ? player : null, broken -> {});
        }
    }
}
