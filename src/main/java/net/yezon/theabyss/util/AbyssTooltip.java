package net.yezon.theabyss.util;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class AbyssTooltip {
    public static final String SOMNIUM = "\uEF01";
    public static final String SOMNIUM_FLOW = "\uEF02";
    public static final String RIGHT_CLICK = "\uEF03";
    public static final String DURABILITY = "\uEF04";
    public static final String LEFT_CLICK = "\uEF06";
    public static final String MIDDLE_CLICK = "\uEF07";
    public static final String DAMAGE = "\uEF08";
    public static final String COOLDOWN = "\uEF09";

    private static final ChatFormatting NAME = ChatFormatting.DARK_AQUA;
    private static final ChatFormatting TEXT = ChatFormatting.GRAY;
    private static final ChatFormatting COST = ChatFormatting.LIGHT_PURPLE;
    private static final ChatFormatting BASE = ChatFormatting.DARK_GRAY;
    private static final ChatFormatting LOCKED = ChatFormatting.DARK_GRAY;

    public static java.util.function.Supplier<net.yezon.theabyss.network.TheabyssModVariables.@org.jspecify.annotations.Nullable PlayerVariables> viewer = () -> null;

    public static double usageFactor() {
        var vars = viewer.get();
        return vars == null ? 1.0 : vars.ManaUpgrade;
    }

    public static double cooldownFactor() {
        var vars = viewer.get();
        return vars == null ? 1.0 : vars.ManaCooldown;
    }

    public static double damageUpgrade() {
        var vars = viewer.get();
        return vars == null ? 0.0 : vars.ManaDamage;
    }

    public static int ringLevel() {
        return net.yezon.theabyss.ring.RingWear.level(usageFactor());
    }

    public static String roman(int n) {
        String[] tens = {"", "X", "XX", "XXX"};
        String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
        return n <= 0 ? "0" : tens[Math.min(3, n / 10)] + ones[n % 10];
    }

    private AbyssTooltip() {
    }

    public static String tr(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }

    public static String res(String text) {
        return Language.getInstance().has(text) ? Component.translatable(text).getString() : text;
    }

    public static final class Spell {
        public String name;
        public @org.jspecify.annotations.Nullable String key;
        public @org.jspecify.annotations.Nullable MutableComponent cost;
        public @org.jspecify.annotations.Nullable MutableComponent damage;
        public @org.jspecify.annotations.Nullable MutableComponent cooldown;
        public net.yezon.theabyss.ring.SomniumUpgrades.@org.jspecify.annotations.Nullable Kind requiredKind;
        public int requiredLevel;
    }

    private static final ThreadLocal<List<Spell>> CAPTURE = new ThreadLocal<>();

    public static List<Spell> spells(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context) {
        List<Spell> out = new java.util.ArrayList<>();
        CAPTURE.set(out);
        try {
            stack.getItem().appendHoverText(stack, context, net.minecraft.world.item.component.TooltipDisplay.DEFAULT, line -> {
            }, net.minecraft.world.item.TooltipFlag.NORMAL);
        } finally {
            CAPTURE.remove();
        }
        return out;
    }

    private static void captureAbility(String name) {
        List<Spell> out = CAPTURE.get();
        if (out != null) {
            Spell spell = new Spell();
            spell.name = name;
            out.add(spell);
        }
    }

    private static void captureKey(String key, String action) {
        List<Spell> out = CAPTURE.get();
        if (out == null) {
            return;
        }
        if (out.isEmpty() || out.get(out.size() - 1).key != null) {
            Spell spell = new Spell();
            spell.name = action;
            out.add(spell);
        }
        out.get(out.size() - 1).key = key;
    }

    private static void captureCost(MutableComponent cost) {
        List<Spell> out = CAPTURE.get();
        if (out != null && !out.isEmpty() && out.get(out.size() - 1).cost == null) {
            out.get(out.size() - 1).cost = cost;
        }
    }

    private static @org.jspecify.annotations.Nullable Spell lastSpell() {
        List<Spell> out = CAPTURE.get();
        return out == null || out.isEmpty() ? null : out.get(out.size() - 1);
    }

    public static Consumer<Component> ability(Consumer<Component> tooltip, String name, String text, int damageLevel) {
        return ability(tooltip, name, text, net.yezon.theabyss.ring.SomniumUpgrades.Kind.DAMAGE, damageLevel);
    }

    public static Consumer<Component> ability(Consumer<Component> tooltip, String name, String text, net.yezon.theabyss.ring.SomniumUpgrades.Kind kind,
                                              int level) {
        name = res(name);
        text = res(text);
        if (level(kind) >= level) {
            ability(tooltip, name, text);
            return tooltip;
        }
        captureAbility(name);
        List<Spell> out = CAPTURE.get();
        if (out != null && !out.isEmpty()) {
            out.get(out.size() - 1).requiredKind = kind;
            out.get(out.size() - 1).requiredLevel = level;
        }
        Consumer<Component> grey = line -> tooltip.accept(Component.literal(line.getString()).withStyle(LOCKED));
        List<String> rows = wrap(name + " " + requires(kind, level) + ": " + text, WIDTH);
        rows.forEach(row -> tooltip.accept(Component.literal(row).withStyle(LOCKED)));
        return grey;
    }

    public static void text(Consumer<Component> tooltip, String text, int damageLevel) {
        text(tooltip, text, net.yezon.theabyss.ring.SomniumUpgrades.Kind.DAMAGE, damageLevel);
    }

    public static void text(Consumer<Component> tooltip, String text, net.yezon.theabyss.ring.SomniumUpgrades.Kind kind, int level) {
        text = res(text);
        if (level(kind) >= level) {
            wrap(text, WIDTH).forEach(row -> tooltip.accept(text(row)));
        } else {
            wrap(text + " " + requires(kind, level), WIDTH).forEach(row -> tooltip.accept(Component.literal(row).withStyle(LOCKED)));
        }
    }

    public static int level(net.yezon.theabyss.ring.SomniumUpgrades.Kind kind) {
        var vars = viewer.get();
        return vars == null ? 0 : net.yezon.theabyss.ring.SomniumUpgrades.level(vars, kind);
    }

    public static String kindName(net.yezon.theabyss.ring.SomniumUpgrades.Kind kind) {
        return tr("gauntlet.theabyss.stat." + kind.id);
    }

    private static String requires(net.yezon.theabyss.ring.SomniumUpgrades.Kind kind, int level) {
        return tr("tooltip.theabyss.requires", kindName(kind), level);
    }

    public static MutableComponent icon(String glyph) {
        return Component.literal(glyph).withStyle(ChatFormatting.WHITE);
    }

    public static Component ability(String name, String text) {
        name = res(name);
        text = res(text);
        captureAbility(name);
        return Component.literal(name).withStyle(NAME).append(Component.literal(": " + text).withStyle(TEXT));
    }

    private static final int WIDTH = 42;

    public static void ability(Consumer<Component> tooltip, String name, String text) {
        name = res(name);
        text = res(text);
        captureAbility(name);
        List<String> rows = wrap(name + ": " + text, WIDTH);
        for (int i = 0; i < rows.size(); i++) {
            String row = rows.get(i);
            if (i == 0) {
                tooltip.accept(Component.literal(name).withStyle(NAME).append(Component.literal(row.substring(name.length())).withStyle(TEXT)));
            } else {
                tooltip.accept(Component.literal(row).withStyle(TEXT));
            }
        }
    }

    private static List<String> wrap(String text, int width) {
        List<String> rows = new java.util.ArrayList<>();
        StringBuilder row = new StringBuilder();
        for (String word : text.split(" ")) {
            if (row.length() > 0 && row.length() + 1 + word.length() > width) {
                rows.add(row.toString());
                row.setLength(0);
            }
            if (row.length() > 0) {
                row.append(' ');
            }
            row.append(word);
        }
        if (row.length() > 0) {
            rows.add(row.toString());
        }
        return rows;
    }

    public static Component text(String text) {
        return Component.literal(res(text)).withStyle(TEXT);
    }

    public static Component rightClick(String action) {
        action = res(action);
        captureKey(RIGHT_CLICK, action);
        return line(RIGHT_CLICK, action);
    }

    public static Component leftClick(String action) {
        action = res(action);
        captureKey(LEFT_CLICK, action);
        return line(LEFT_CLICK, action);
    }

    public static Component middleClick(String action) {
        action = res(action);
        captureKey(MIDDLE_CLICK, action);
        return line(MIDDLE_CLICK, action);
    }

    public static Component key(String key, String action) {
        action = res(action);
        captureKey("[" + key + "]", action);
        return Component.literal("[" + key + "] ").withStyle(ChatFormatting.WHITE).append(Component.literal(action).withStyle(TEXT));
    }

    public static Component somnium(double amount) {
        MutableComponent line = icon(SOMNIUM).append(" ").append(Component.literal(number(real(amount))).withStyle(COST));
        captureCost(line.copy());
        return line.append(base(real(amount), amount));
    }

    public static Component somniumFlow(double amount, String per) {
        per = res(per);
        MutableComponent line = icon(SOMNIUM_FLOW).append(" ").append(Component.literal(number(real(amount)) + " / " + per).withStyle(COST));
        captureCost(line.copy());
        return line.append(base(real(amount), amount));
    }

    public static Component somnium(double amount, double flow, String per) {
        per = res(per);
        MutableComponent wheel = icon(SOMNIUM).append(" ").append(Component.literal(number(real(amount))).withStyle(COST)).append("  ").append(icon(SOMNIUM_FLOW)).append(" ")
            .append(Component.literal(number(real(flow)) + " / " + per).withStyle(COST));
        captureCost(wheel);
        return icon(SOMNIUM).append(" ").append(Component.literal(number(real(amount))).withStyle(COST)).append(base(real(amount), amount)).append("  ")
            .append(icon(SOMNIUM_FLOW)).append(" ").append(Component.literal(number(real(flow)) + " / " + per).withStyle(COST)).append(base(real(flow), flow));
    }

    private static double real(double amount) {
        return Math.round(amount * net.yezon.theabyss.ring.SomniumUpgrades.costFactor(usageFactor()) * 100.0) / 100.0;
    }

    private static MutableComponent base(double real, double base) {
        return Math.abs(real - base) < 1.0E-6 ? Component.empty() : Component.literal(" (" + number(base) + ")").withStyle(BASE);
    }

    private static MutableComponent base(String real, String base) {
        return real.equals(base) ? Component.empty() : Component.literal(" (" + base + ")").withStyle(BASE);
    }

    public static Component damage(double hp, double baseHp, String suffix) {
        suffix = res(suffix);
        String real = number(Math.round(hp * 5.0) / 10.0);
        String base = number(Math.round(baseHp * 5.0) / 10.0);
        MutableComponent value = Component.literal(real).withStyle(ChatFormatting.WHITE).append(Component.literal(suffix).withStyle(TEXT));
        Spell spell = lastSpell();
        if (spell != null && spell.damage == null) {
            spell.damage = icon(DAMAGE).append(" ").append(value.copy());
        }
        return icon(DAMAGE).append(" ").append(value).append(base(real, base));
    }

    public static Component cooldown(int ticks) {
        return cooldownLine((int)(ticks * cooldownFactor()), ticks);
    }

    public static Component cooldownFixed(int ticks) {
        return cooldownLine(ticks, ticks);
    }

    private static Component cooldownLine(int ticks, int baseTicks) {
        String real = seconds(ticks);
        MutableComponent value = Component.literal(real).withStyle(ChatFormatting.YELLOW);
        Spell spell = lastSpell();
        if (spell != null && spell.cooldown == null) {
            spell.cooldown = icon(COOLDOWN).append(" ").append(value.copy());
        }
        return icon(COOLDOWN).append(" ").append(value).append(base(real, seconds(baseTicks)));
    }

    public static String seconds(int ticks) {
        double s = ticks / 20.0;
        if (s < 60.0) {
            return tr("tooltip.theabyss.seconds", number(Math.round(s * 10.0) / 10.0));
        }
        int whole = (int)Math.round(s);
        return whole % 60 == 0 ? tr("tooltip.theabyss.minutes", whole / 60) : tr("tooltip.theabyss.minutes_seconds", whole / 60, whole % 60);
    }

    public static Component durability(ItemStack stack) {
        return durability(stack, 1);
    }

    public static Component durability(ItemStack stack, int level) {
        int max = stack.getMaxDamage() * level;
        int left = (stack.getMaxDamage() - stack.getDamageValue()) * level - (level > 1 ? net.yezon.theabyss.ring.RingWear.pending(stack) : 0);
        return icon(DURABILITY).append(" ").append(Component.literal(left + " / " + max).withStyle(TEXT));
    }

    public static void ring(Consumer<Component> tooltip, String name, String text, String action, double somnium) {
        ability(tooltip, name, text);
        tooltip.accept(rightClick(action));
        tooltip.accept(somnium(somnium));
    }

    private static Component line(String glyph, String action) {
        return icon(glyph).append(" ").append(Component.literal(action).withStyle(TEXT));
    }

    private static String number(double value) {
        return value == Math.rint(value) ? Long.toString((long)value) : Double.toString(value);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.isDamageableItem() || !TheabyssMod.MODID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace())) {
            return;
        }
        List<Component> lines = event.getToolTip();
        int at = lines.size();
        if (event.getFlags().isAdvanced()) {
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            for (int i = 1; i < lines.size(); i++) {
                Component line = lines.get(i);
                if (line.getContents() instanceof TranslatableContents t && t.getKey().equals("item.durability") || line.getString().equals(id)) {
                    at = i;
                    break;
                }
            }
        }
        int level = event.getEntity() != null && net.yezon.theabyss.ring.RingWear.isRing(stack) ? net.yezon.theabyss.ring.RingWear.level(event.getEntity()) : 1;
        lines.add(at, durability(stack, level));
    }
}
