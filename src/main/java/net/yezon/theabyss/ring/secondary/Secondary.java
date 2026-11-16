package net.yezon.theabyss.ring.secondary;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.Gauntlet;
import net.yezon.theabyss.ring.RingCast;
import net.yezon.theabyss.ring.SomniumUpgrades;
import net.yezon.theabyss.ring.SomniumUpgrades.Kind;
import net.yezon.theabyss.util.AbyssTooltip;

public final class Secondary {
    @FunctionalInterface
    public interface Handler {
        boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars);
    }

    public record Spec(Supplier<Item> ring, String name, String text, String action, Kind kind, int level, double cost, int cooldown,
                       DoubleUnaryOperator damage, String damageSuffix, boolean raw, Handler handler) {
    }

    private static Map<Item, Spec> specs;
    private static final Map<UUID, Map<Item, Long>> READY = new HashMap<>();

    private Secondary() {
    }

    private static Map<Item, Spec> specs() {
        if (specs == null) {
            Map<Item, Spec> map = new LinkedHashMap<>();
            for (Spec spec : SecondarySpells.all()) {
                map.put(spec.ring().get(), spec);
            }
            specs = map;
        }
        return specs;
    }

    public static Spec spec(ItemStack ring) {
        return ring.isEmpty() ? null : specs().get(ring.getItem());
    }

    public static boolean has(ItemStack ring) {
        return spec(ring) != null;
    }

    public static void cast(ServerPlayer player, ItemStack ring) {
        Spec spec = spec(ring);
        if (spec == null || player.isSpectator()) {
            return;
        }
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        if (SomniumUpgrades.level(vars, spec.kind()) < spec.level()) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.secondary.locked", Component.translatable(spec.name()),
                Component.translatable("gauntlet.theabyss.stat." + spec.kind().id), spec.level()));
            return;
        }
        long now = player.level().getGameTime();
        Long ready = READY.getOrDefault(player.getUUID(), Map.of()).get(ring.getItem());
        if (ready != null && now < ready) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.secondary.cooldown", Component.translatable(spec.name()), (int)Math.ceil((ready - now) / 20.0)));
            return;
        }
        if (spec.raw()) {
            if (spec.handler().cast(player, ring, vars)) {
                swing(player, ring);
            }
            return;
        }
        RingCast cast = RingCast.start(player.level(), player, ring, spec.cost(), player.getEyePosition());
        if (cast == null) {
            return;
        }
        if (!spec.handler().cast(player, ring, vars)) {
            return;
        }
        READY.computeIfAbsent(player.getUUID(), k -> new HashMap<>()).put(ring.getItem(), now + (long)(spec.cooldown() * vars.ManaCooldown));
        swing(player, ring);
        cast.durability(ring.getItem(), 1).charge(spec.cost());
    }

    public static void resetCooldowns(ServerPlayer player) {
        READY.remove(player.getUUID());
    }

    private static void swing(ServerPlayer player, ItemStack ring) {
        InteractionHand hand = player.getOffhandItem() == ring
            || Gauntlet.is(player.getOffhandItem()) && !Gauntlet.is(player.getMainHandItem()) && player.getMainHandItem().getItem() != ring.getItem()
            ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        player.swing(hand, SwingAnimation.DEFAULT, true);
    }

    public static void tooltip(Item ring, Consumer<Component> tooltip) {
        Spec spec = specs().get(ring);
        if (spec == null) {
            return;
        }
        Consumer<Component> lines = AbyssTooltip.ability(tooltip, spec.name(), spec.text(), spec.kind(), spec.level());
        lines.accept(AbyssTooltip.middleClick(spec.action()));
        if (!spec.raw() || spec.cost() > 0.0) {
            lines.accept(AbyssTooltip.somnium(spec.cost()));
        }
        if (spec.damage() != null) {
            lines.accept(AbyssTooltip.damage(spec.damage().applyAsDouble(AbyssTooltip.damageUpgrade()), spec.damage().applyAsDouble(0.0), spec.damageSuffix()));
        }
        if (spec.cooldown() > 0) {
            lines.accept(AbyssTooltip.cooldown(spec.cooldown()));
        }
    }

    static Supplier<Item> ring(Supplier<? extends Item> holder) {
        return holder::get;
    }
}
