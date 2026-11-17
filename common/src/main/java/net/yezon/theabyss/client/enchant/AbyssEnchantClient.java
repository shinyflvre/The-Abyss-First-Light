package net.yezon.theabyss.client.enchant;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.client.event.RegisterItemPropertiesEvent;
import net.yezon.theabyss.platform.event.entity.player.ItemTooltipEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.enchant.AbyssEnchantments;
import net.yezon.theabyss.enchant.BowEnchantments;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.SomniumUpgrades;
import net.yezon.theabyss.client.fx.spell.EnchantFx;
import net.yezon.theabyss.client.fx.spell.SpellFx;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class AbyssEnchantClient {
    private static final int DRAW_FULL = 20;

    private AbyssEnchantClient() {
    }

    private static TagKey<Enchantment> kind(String name) {
        return TagKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "book/" + name));
    }

    private static final TagKey<Enchantment> ARMOR = kind("armor");
    private static final TagKey<Enchantment> WEAPON = kind("weapon");
    private static final TagKey<Enchantment> BOW = kind("bow");

    private static float bookKind(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed) {
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            if (holder.is(BOW)) {
                return 3.0F;
            }
            if (holder.is(WEAPON)) {
                return 2.0F;
            }
            if (holder.is(ARMOR)) {
                return 1.0F;
            }
        }
        return 0.0F;
    }

    @EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
    public static final class ModBus {
        private ModBus() {
        }

        @SubscribeEvent
        public static void registerProperties(RegisterItemPropertiesEvent event) {
            event.register(TheabyssModItems.ABYSS_ENCHANTED_BOOK.get(), ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "abyss_book_kind"),
                AbyssEnchantClient::bookKind);
        }
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(TheabyssModItems.ABYSS_ENCHANTED_BOOK.get())) {
            return;
        }
        List<Component> lines = event.getToolTip();
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            entry.getKey().unwrapKey().filter(k -> TheabyssMod.MODID.equals(k.location().getNamespace())).ifPresent(k -> {
                lines.add(Component.empty());
                lines.add(Component.translatable("enchantment.theabyss." + k.location().getPath()).withStyle(ChatFormatting.DARK_AQUA));
                lines.add(Component.translatable("enchantment.theabyss." + k.location().getPath() + ".desc").withStyle(ChatFormatting.GRAY));
            });
        }
    }

    private static EnchantFx.DrawCharge charge;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        boolean on = drawing(mc);
        if (on && (charge == null || charge.done() || !SpellFx.effects().contains(charge))) {
            charge = EnchantFx.drawCharge();
            SpellFx.add(charge);
        }
        if (charge != null) {
            charge.set(on);
        }
    }

    private static boolean drawing(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.isPaused() || !player.isUsingItem() || player.getTicksUsingItem() < DRAW_FULL) {
            return false;
        }
        ItemStack bow = player.getUseItem();
        if (!(bow.getItem() instanceof BowItem)) {
            return false;
        }
        int power = AbyssEnchantments.level(mc.level, AbyssEnchantments.SOMNIUM_DRAW, bow);
        if (power <= 0) {
            return false;
        }
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        return vars.Mana >= BowEnchantments.drawCost(power) * SomniumUpgrades.costFactor(vars);
    }
}
