package net.yezon.theabyss.fabric.mixin.blocks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.yezon.theabyss.item.hooks.GameplayEnchantmentsItem;
import net.yezon.theabyss.platform.Platform;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @ModifyReturnValue(method = "getItemEnchantmentLevel", at = @At("RETURN"))
    private static int theabyss$gameplayLevel(int original, @Local(argsOnly = true) Holder<Enchantment> enchantment, @Local(argsOnly = true) ItemStack piece) {
        if (piece.getItem() instanceof GameplayEnchantmentsItem item) {
            return item.getEnchantmentLevel(piece, enchantment);
        }
        return original;
    }

    @ModifyExpressionValue(method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentVisitor;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object theabyss$gameplayIteration(Object original, @Local(argsOnly = true) ItemStack piece) {
        return theabyss$gameplay(piece, original, theabyss$serverLookup());
    }

    @ModifyExpressionValue(method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private static Object theabyss$gameplaySlotIteration(Object original, @Local(argsOnly = true) ItemStack piece, @Local(argsOnly = true) LivingEntity owner) {
        return theabyss$gameplay(piece, original, owner.registryAccess().lookupOrThrow(Registries.ENCHANTMENT));
    }

    @ModifyExpressionValue(method = "hasTag", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object theabyss$gameplayTag(Object original, @Local(argsOnly = true) ItemStack item) {
        return theabyss$gameplay(item, original, theabyss$serverLookup());
    }

    @Unique
    private static Object theabyss$gameplay(ItemStack stack, Object original, HolderLookup.@Nullable RegistryLookup<Enchantment> lookup) {
        if (lookup != null && stack.getItem() instanceof GameplayEnchantmentsItem item) {
            ItemEnchantments enchantments = item.getAllEnchantments(stack, lookup);
            return enchantments;
        }
        return original;
    }

    @Unique
    private static HolderLookup.@Nullable RegistryLookup<Enchantment> theabyss$serverLookup() {
        MinecraftServer server = Platform.get().currentServer();
        return server == null ? null : server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
    }
}
