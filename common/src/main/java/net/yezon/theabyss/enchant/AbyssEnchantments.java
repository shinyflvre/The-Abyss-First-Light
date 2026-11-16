package net.yezon.theabyss.enchant;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModItems;

public final class AbyssEnchantments {
    public static final ResourceKey<Enchantment> MAGMA_SMELT = key("magma_smelt");
    public static final ResourceKey<Enchantment> PHANTOM_REACH = key("phantom_reach");
    public static final ResourceKey<Enchantment> PHANTOM_VENOM = key("phantom_venom");
    public static final ResourceKey<Enchantment> NIGHTBLADE_ECHO = key("nightblade_echo");
    public static final ResourceKey<Enchantment> CRYSTAL_SHATTER = key("crystal_shatter");
    public static final ResourceKey<Enchantment> SEEKER_SHOT = key("seeker_shot");
    public static final ResourceKey<Enchantment> SOMNIUM_DRAW = key("somnium_draw");
    public static final ResourceKey<Enchantment> PIERCING_LIGHT = key("piercing_light");
    public static final ResourceKey<Enchantment> HOLLOW_BORE = key("hollow_bore");
    public static final List<ResourceKey<Enchantment>> ALL = List.of(MAGMA_SMELT, PHANTOM_REACH, PHANTOM_VENOM, NIGHTBLADE_ECHO, CRYSTAL_SHATTER,
        SEEKER_SHOT, SOMNIUM_DRAW, PIERCING_LIGHT, HOLLOW_BORE);

    private AbyssEnchantments() {
    }

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, name));
    }

    public static Optional<Holder.Reference<Enchantment>> holder(Level level, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key);
    }

    public static int level(Level level, ResourceKey<Enchantment> key, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        return holder(level, key).map(h -> EnchantmentHelper.getItemEnchantmentLevel(h, stack)).orElse(0);
    }

    public static boolean ours(Holder<Enchantment> enchantment) {
        return enchantment.unwrapKey().map(k -> TheabyssMod.MODID.equals(k.identifier().getNamespace())).orElse(false);
    }

    public static ItemStack book(HolderLookup.Provider registries, ResourceKey<Enchantment> key, int enchantLevel) {
        ItemStack stack = new ItemStack(TheabyssModItems.ABYSS_ENCHANTED_BOOK.get());
        registries.lookupOrThrow(Registries.ENCHANTMENT).get(key).ifPresent(h -> {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            mutable.set(h, enchantLevel);
            stack.set(net.minecraft.core.component.DataComponents.ENCHANTMENTS, mutable.toImmutable());
        });
        return stack;
    }
}
