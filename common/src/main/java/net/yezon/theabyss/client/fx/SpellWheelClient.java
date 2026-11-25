package net.yezon.theabyss.client.fx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.client.event.InputEvent;
import net.yezon.theabyss.platform.client.network.ClientPacketDistributor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModKeyMappings;
import net.yezon.theabyss.network.GauntletMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.Gauntlet;
import net.yezon.theabyss.ring.SomniumUpgrades;
import net.yezon.theabyss.util.AbyssTooltip;
import net.yezon.theabyss.client.compat.ObscureSkillsClientCompat;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class SpellWheelClient {
    public static final com.mojang.renderpearl.api.pipeline.RenderPipeline PIPELINE = com.mojang.renderpearl.api.pipeline.RenderPipeline
        .builder(net.yezon.theabyss.mixin.accessor.client.RenderPipelinesAccessor.theabyss$guiTexturedSnippet())
        .withLocation(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "pipeline/spell_wheel"))
        .withFragmentShader(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "core/spell_wheel"))
        .build();
    private static final Identifier FRAMES = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "textures/gui/spell_wheel_frames.png");
    private static final int RADIUS = 56;
    private static final float ICON_SCALE = 1.14F;
    private static final double DEAD_ZONE = 20.0;
    private static final float OPEN_SECONDS = 0.20F;
    private static final float CLOSE_SECONDS = 0.20F;
    private static final float VOLUME = 0.15F;
    private static final Map<Identifier, Boolean> ICONS = new HashMap<>();

    private static boolean open;
    private static boolean closing;
    private static long openNanos;
    private static long closeNanos;
    private static int hovered = -1;
    private static int gaugeHovered = -1;
    private static double aimX;
    private static double aimY;
    private static int lastHovered = -1;
    private static double[] testAim;

    private SpellWheelClient() {
    }

    public static boolean isOpen() {
        return open;
    }

    public static void aimForTest(double[] aim) {
        testAim = aim;
    }

    private record Entry(int ring, int obscure, ItemStack stack, String spell) {
        boolean empty() {
            return this.ring >= 0 ? this.stack.isEmpty() : this.spell == null;
        }
    }

    private static List<Entry> entries(LocalPlayer player) {
        List<Entry> out = new ArrayList<>();
        ItemStack gauntlet = Gauntlet.held(player);
        if (!gauntlet.isEmpty()) {
            NonNullList<ItemStack> rings = Gauntlet.rings(gauntlet);
            for (int k = 0; k < Gauntlet.SLOTS; k++) {
                out.add(new Entry(k, -1, rings.get(k), null));
            }
        }
        if (ObscureSkillsClientCompat.present() && (!out.isEmpty() || ObscureSkillsClientCompat.anyAssigned())) {
            for (int s = 0; s < net.yezon.theabyss.compat.ObscureSkillsCompat.SLOTS; s++) {
                out.add(new Entry(-1, s, ItemStack.EMPTY, ObscureSkillsClientCompat.spell(s)));
            }
        }
        return out;
    }

    private static int selectedIndex(LocalPlayer player, List<Entry> entries) {
        int obscure = ObscureSkillsClientCompat.selected();
        ItemStack gauntlet = Gauntlet.held(player);
        int ring = obscure < 0 && !gauntlet.isEmpty() ? Gauntlet.selected(gauntlet) : -1;
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            if (obscure >= 0 && entry.obscure() == obscure || ring >= 0 && entry.ring() == ring) {
                return i;
            }
        }
        return -1;
    }

    private static float cooldown(LocalPlayer player, Entry entry, float pt) {
        if (entry.empty()) {
            return 0.0F;
        }
        return entry.ring() >= 0 ? player.getCooldowns().getCooldownPercent(entry.stack(), pt) : ObscureSkillsClientCompat.cooldown(entry.spell());
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        boolean holding = player != null && !player.isSpectator() && (Gauntlet.hand(player) != null || ObscureSkillsClientCompat.anyAssigned());
        if (open && (mc.gui.screen() != null || !holding)) {
            close(false);
            return;
        }
        boolean wanted = holding && mc.gui.screen() == null && TheabyssModKeyMappings.SPELL_WHEEL.isDown() && !mc.hasAltDown();
        if (!open && wanted) {
            openWheel();
        } else if (open && !wanted) {
            close(true);
            return;
        }
        if (open) {
            int count = Math.min(entries(player).size(), mc.options.keyHotbarSlots.length);
            for (int i = 0; i < count; i++) {
                while (mc.options.keyHotbarSlots[i].consumeClick()) {
                    hovered = i;
                    close(true);
                    return;
                }
            }
        }
    }

    private static void openWheel() {
        Minecraft mc = Minecraft.getInstance();
        open = true;
        closing = false;
        openNanos = FxClock.nanos();
        hovered = -1;
        mc.mouseHandler.releaseMouse();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(net.yezon.theabyss.init.TheabyssModSounds.SPELL_WHEEL_OPEN.get(), 1.0F, VOLUME));
    }

    private static void close(boolean pick) {
        Minecraft mc = Minecraft.getInstance();
        open = false;
        closing = true;
        closeNanos = FxClock.nanos();
        lastHovered = pick ? hovered : -1;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(net.yezon.theabyss.init.TheabyssModSounds.SPELL_WHEEL_CLOSE.get(), 1.0F, VOLUME));
        if (pick && hovered >= 0 && mc.player != null) {
            List<Entry> entries = entries(mc.player);
            Entry entry = hovered < entries.size() ? entries.get(hovered) : null;
            ItemStack gauntlet = Gauntlet.held(mc.player);
            if (entry != null && !entry.empty() && entry.ring() >= 0 && !gauntlet.isEmpty()) {
                Gauntlet.select(gauntlet, entry.ring());
                ObscureSkillsClientCompat.forget();
                ClientPacketDistributor.sendToServer(new GauntletMessage(GauntletMessage.SELECT, entry.ring()));
            } else if (entry != null && !entry.empty() && entry.obscure() >= 0) {
                ObscureSkillsClientCompat.select(entry.obscure());
            }
        }
        hovered = -1;
        if (mc.gui.screen() == null) {
            mc.mouseHandler.grabMouse();
        }
    }

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (!open) {
            return;
        }
        event.setCanceled(true);
        event.setSwingHand(false);
        if (event.isUseItem()) {
            close(false);
        }
    }

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        if (!open || Minecraft.getInstance().player == null) {
            return;
        }
        event.setCanceled(true);
        List<Entry> entries = entries(Minecraft.getInstance().player);
        int count = entries.size();
        if (count == 0) {
            return;
        }
        int step = event.getScrollDeltaY() > 0 ? -1 : 1;
        int start = hovered < 0 ? (step > 0 ? -1 : 0) : hovered;
        for (int n = 1; n <= count; n++) {
            int i = Math.floorMod(start + step * n, count);
            if (!entries.get(i).empty()) {
                setHovered(i);
                return;
            }
        }
    }

    private static void setHovered(int slot) {
        if (slot != hovered) {
            hovered = slot;
            if (slot >= 0) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 1.8F, 0.25F));
            }
        }
    }

    public static void render(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.gui.hud.isHidden()) {
            return;
        }
        List<Entry> entries = entries(player);
        if (entries.isEmpty()) {
            return;
        }
        int count = entries.size();
        double sliceDeg = 360.0 / count;
        float pt = deltaTracker.getGameTimeDeltaPartialTick(false);
        int selected = selectedIndex(player, entries);
        float progress;
        if (open) {
            progress = Mth.clamp((FxClock.nanos() - openNanos) / 1.0E9F / OPEN_SECONDS, 0.0F, 1.0F);
        } else if (closing) {
            progress = 1.0F - (FxClock.nanos() - closeNanos) / 1.0E9F / CLOSE_SECONDS;
            if (progress <= 0.0F) {
                closing = false;
            }
        } else {
            progress = 0.0F;
        }
        if (!open && !closing) {
            return;
        }
        int cx = g.guiWidth() / 2;
        int cy = g.guiHeight() / 2;
        float fit = Math.min(1.0F, Math.min((g.guiHeight() / 2.0F - 50.0F) / 96.0F, (g.guiHeight() / 2.0F - 4.0F) / 136.0F));
        int lit = hovered;
        if (open) {
            double mx = testAim != null ? testAim[0] : mc.mouseHandler.getScaledXPos(mc.getWindow()) - cx;
            double my = testAim != null ? testAim[1] : mc.mouseHandler.getScaledYPos(mc.getWindow()) - cy;
            aimX = mx / fit;
            aimY = my / fit;
            double dist = Math.sqrt(aimX * aimX + aimY * aimY);
            double deg = Math.toDegrees(Math.atan2(mx, -my));
            gaugeHovered = -1;
            if (dist > 86.0 && dist < 140.0) {
                double rel = Math.floorMod((long)Math.round(deg - 45.0), 360L);
                int corner = (int)Math.round(rel / 90.0) % 4;
                double off = Math.abs(rel - corner * 90.0);
                if (Math.min(off, 360.0 - off) < 30.0) {
                    gaugeHovered = new int[] {1, 2, 3, 0}[corner];
                }
            }
            if (gaugeHovered >= 0) {
                setHovered(-1);
            } else if (dist >= DEAD_ZONE) {
                setHovered(Math.floorMod((int)Math.round(deg / sliceDeg), count));
            } else {
                setHovered(-1);
            }
            lit = hovered;
        } else {
            lit = lastHovered;
        }
        float ease = progress * progress * (3.0F - 2.0F * progress);
        float spread = open ? 0.55F + 0.45F * ease : 1.35F - 0.35F * ease;
        int alpha = Math.round(255 * (float)Math.pow(progress, 1.5));
        int fade = Math.round(255 * progress);
        g.blit(PIPELINE, FRAMES, 0, 0, 0.0F, 0.0F, g.guiWidth(), g.guiHeight(), 64, 32, 64, 32, ARGB.color(0, 0, 0, fade));
        g.pose().pushMatrix();
        g.pose().translate(cx, cy);
        g.pose().scale(fit, fit);
        int params = ARGB.color(open ? 255 : 180, Math.max(0, Math.min(lit + 1, 7)) * 32 + count, selected < 0 ? 7 * 32 : selected * 32, fade);
        g.blit(PIPELINE, FRAMES, -120, -120, 0.0F, 0.0F, 240, 240, 64, 32, 64, 32, params);
        for (int k = 0; k < count; k++) {
            double a = Math.toRadians(sliceDeg * k);
            int sx = (int)Math.round(Math.sin(a) * RADIUS * spread);
            int sy = (int)Math.round(-Math.cos(a) * RADIUS * spread);
            Entry entry = entries.get(k);
            g.pose().pushMatrix();
            g.pose().translate(sx, sy);
            g.pose().scale(ICON_SCALE, ICON_SCALE);
            if (!entry.empty()) {
                if (entry.ring() >= 0) {
                    icon(g, entry.stack(), -8, -8, alpha);
                } else {
                    obscureIcon(g, entry.spell(), -8, -8, alpha);
                }
                float cd = cooldown(player, entry, pt);
                if (cd > 0.0F) {
                    int h = (int)(16 * cd + 1);
                    g.fill(-8, 8 - h, 8, 8, ARGB.color(Math.min(alpha, 128), 255, 255, 255));
                }
            }
            boolean gold = k == selected && !entry.empty();
            int frameAlpha = entry.empty() ? alpha / 3 : alpha;
            g.blit(RenderPipelines.GUI_TEXTURED, FRAMES, -16, -16, gold ? 32.0F : 0.0F, 0.0F, 32, 32, 64, 32, ARGB.color(frameAlpha, 255, 255, 255));
            g.pose().popMatrix();
        }
        Stat[] stats = stats(player);
        g.pose().pushMatrix();
        g.pose().scale(spread, spread);
        for (int k = 0; k < stats.length; k++) {
            Stat stat = stats[k];
            int gaugeParams = ARGB.color(Math.round((0.25F + 0.35F * progress) * 255), Math.round(k * 255 / 8.0F),
                Math.round(stat.level() * 255 / 8.0F), Math.round(stat.left() * 255));
            g.blit(PIPELINE, FRAMES, -120, -120, 0.0F, 0.0F, 240, 240, 64, 32, 64, 32, gaugeParams);
        }
        g.pose().popMatrix();
        if (open && gaugeHovered >= 0) {
            info(g, mc, stats[gaugeHovered], (int)aimX, (int)aimY, alpha, g.guiHeight() / 2.0F / fit);
        }
        Entry shown = lit >= 0 && lit < count ? entries.get(lit) : null;
        if (shown != null && shown.obscure() >= 0) {
            obscureText(g, mc, shown, 0, 0, alpha);
        } else {
            text(g, mc, player, shown != null ? shown.stack() : ItemStack.EMPTY, 0, 0, alpha, pt);
        }
        g.pose().popMatrix();
    }

    private record Stat(String key, int colour, int level, float left, int potions, int needed) {
    }

    private static final int[] COLOURS = {0xFFB437, 0xFF6BE6, 0x5FEFFF, 0xB77BFF};

    private static Stat[] stats(LocalPlayer player) {
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        SomniumUpgrades.Kind[] kinds = {SomniumUpgrades.Kind.DAMAGE, SomniumUpgrades.Kind.COOLDOWN, SomniumUpgrades.Kind.USAGE, SomniumUpgrades.Kind.REGEN};
        Stat[] out = new Stat[kinds.length];
        for (int i = 0; i < kinds.length; i++) {
            int level = SomniumUpgrades.level(vars, kinds[i]);
            out[i] = new Stat("gauntlet.theabyss.stat." + kinds[i].id, COLOURS[i], level, SomniumUpgrades.progress(vars, kinds[i]),
                SomniumUpgrades.potions(vars, kinds[i]), SomniumUpgrades.needed(level));
        }
        return out;
    }
    private static void info(GuiGraphicsExtractor g, Minecraft mc, Stat stat, int x, int y, int alpha, float halfHeight) {
        List<net.minecraft.util.FormattedCharSequence> lines = new ArrayList<>();
        lines.add(Component.translatable(stat.key()).withStyle(style -> style.withColor(stat.colour())).getVisualOrderText());
        Component level = stat.level() >= SomniumUpgrades.MAX
            ? Component.translatable("gauntlet.theabyss.stat.level_max", stat.level())
            : Component.translatable("gauntlet.theabyss.stat.level", stat.level(), stat.potions(), stat.needed(), stat.level() + 1);
        lines.add(level.copy().withStyle(ChatFormatting.WHITE).getVisualOrderText());
        lines.addAll(mc.font.split(Component.translatable(stat.key() + ".info").withStyle(ChatFormatting.GRAY), 150));
        int w = 0;
        for (var line : lines) {
            w = Math.max(w, mc.font.width(line));
        }
        int h = lines.size() * 10 + 6;
        int bx = x >= 0 ? x + 10 : x - 10 - w - 8;
        int by = Mth.clamp(y - h / 2, (int)-halfHeight + 2, (int)halfHeight - h - 2);
        g.fill(bx, by, bx + w + 8, by + h, ARGB.color(Math.round(alpha * 0.85F), 6, 14, 20));
        g.outline(bx, by, w + 8, h, ARGB.color(alpha / 2, stat.colour()));
        int ty = by + 4;
        for (var line : lines) {
            g.text(mc.font, line, bx + 4, ty, ARGB.color(alpha, 255, 255, 255), true);
            ty += 10;
        }
    }

    private static void text(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer player, ItemStack ring, int cx, int cy, int alpha, float pt) {
        if (ring.isEmpty() || mc.level == null) {
            return;
        }
        List<AbyssTooltip.Spell> spells = AbyssTooltip.spells(ring, Item.TooltipContext.of(mc.level));
        List<Component> parts = new ArrayList<>();
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        for (AbyssTooltip.Spell spell : spells) {
            boolean locked = spell.requiredKind != null && spell.requiredLevel > SomniumUpgrades.level(vars, spell.requiredKind);
            MutableComponent part = Component.empty();
            if (spell.key != null) {
                part.append(key(spell.key)).append(" ");
            }
            part.append(Component.literal(spell.name).withStyle(locked ? ChatFormatting.DARK_GRAY : ChatFormatting.AQUA));
            if (locked) {
                int colour = COLOURS[spell.requiredKind.ordinal()];
                part.append("  ").append(Component.translatable("tooltip.theabyss.kind_level", AbyssTooltip.kindName(spell.requiredKind), spell.requiredLevel)
                    .withStyle(style -> style.withColor(colour)));
            } else {
                for (MutableComponent value : new MutableComponent[] {spell.cost, spell.damage, spell.cooldown}) {
                    if (value != null) {
                        part.append("  ").append(value);
                    }
                }
            }
            parts.add(part);
        }
        Component joined = null;
        for (Component part : parts) {
            joined = joined == null ? part.copy() : ((MutableComponent)joined).append(Component.literal("   |   ").withStyle(ChatFormatting.DARK_GRAY)).append(part);
        }
        boolean oneLine = joined != null && mc.font.width(joined) <= 300;
        List<Component> rows = oneLine ? List.of(joined) : parts;
        float cd = player.getCooldowns().getCooldownPercent(ring, pt);
        int top = cy - 112 - rows.size() * 10 - (cd > 0.0F ? 10 : 0);
        g.centeredText(mc.font, ring.getHoverName(), cx, top, ARGB.color(alpha, 255, 255, 255));
        int y = top + 12;
        for (Component row : rows) {
            g.centeredText(mc.font, row, cx, y, ARGB.color(alpha, 255, 255, 255));
            y += 10;
        }
        if (cd > 0.0F) {
            g.centeredText(mc.font, Component.translatable("gauntlet.theabyss.cooldown"), cx, y, ARGB.color(alpha, 255, 255, 85));
        }
    }

    private static void obscureText(GuiGraphicsExtractor g, Minecraft mc, Entry entry, int cx, int cy, int alpha) {
        if (entry.empty()) {
            return;
        }
        String id = entry.spell();
        int colour = ObscureSkillsClientCompat.colour(id);
        MutableComponent row = Component.empty().append(key(AbyssTooltip.RIGHT_CLICK)).append(" ")
            .append(ObscureSkillsClientCompat.name(id).copy().withStyle(style -> style.withColor(colour)));
        double mana = ObscureSkillsClientCompat.mana(id);
        if (!Double.isNaN(mana)) {
            row.append("  ").append(Component.translatable("obscureskills.magic.cost", number(mana)).withStyle(style -> style.withColor(0x6FB6FF)));
        }
        double damage = ObscureSkillsClientCompat.damage(id);
        if (!Double.isNaN(damage) && damage > 0.0) {
            row.append("  ").append(AbyssTooltip.icon(AbyssTooltip.DAMAGE)).append(" ")
                .append(Component.literal(number(Math.round(damage * 5.0) / 10.0)).withStyle(ChatFormatting.WHITE));
        }
        double seconds = ObscureSkillsClientCompat.cooldownSeconds(id);
        if (!Double.isNaN(seconds) && seconds > 0.0) {
            row.append("  ").append(AbyssTooltip.icon(AbyssTooltip.COOLDOWN)).append(" ")
                .append(Component.literal(AbyssTooltip.seconds((int)Math.round(seconds * 20.0))).withStyle(ChatFormatting.YELLOW));
        }
        float cd = ObscureSkillsClientCompat.cooldown(id);
        int top = cy - 112 - 10 - (cd > 0.0F ? 10 : 0);
        g.centeredText(mc.font, ObscureSkillsClientCompat.name(id), cx, top, ARGB.color(alpha, 255, 255, 255));
        g.centeredText(mc.font, row, cx, top + 12, ARGB.color(alpha, 255, 255, 255));
        if (cd > 0.0F) {
            g.centeredText(mc.font, Component.translatable("gauntlet.theabyss.cooldown"), cx, top + 22, ARGB.color(alpha, 255, 255, 85));
        }
    }

    private static String number(double value) {
        double rounded = Math.round(value * 10.0) / 10.0;
        return rounded == Math.rint(rounded) ? String.valueOf((long)rounded) : String.valueOf(rounded);
    }

    private static void obscureIcon(GuiGraphicsExtractor g, String id, int x, int y, int alpha) {
        Identifier sprite = ObscureSkillsClientCompat.icon(id);
        if (sprite != null) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 16, 16, ARGB.color(alpha, 255, 255, 255));
        }
    }

    private static Component key(String glyph) {
        if (glyph.equals(AbyssTooltip.RIGHT_CLICK) && !TheabyssModKeyMappings.MAIN_MAGIC.isDefault()) {
            return Component.literal("[").append(TheabyssModKeyMappings.MAIN_MAGIC.getTranslatedKeyMessage()).append("]").withStyle(ChatFormatting.WHITE);
        }
        if (glyph.equals(AbyssTooltip.MIDDLE_CLICK) && !TheabyssModKeyMappings.ABYSS_ECHOES.isDefault()) {
            return Component.literal("[").append(TheabyssModKeyMappings.ABYSS_ECHOES.getTranslatedKeyMessage()).append("]").withStyle(ChatFormatting.WHITE);
        }
        return glyph.startsWith("[") ? Component.literal(glyph).withStyle(ChatFormatting.WHITE) : AbyssTooltip.icon(glyph);
    }

    private static void icon(GuiGraphicsExtractor g, ItemStack ring, int x, int y, int alpha) {
        Identifier id = BuiltInRegistries.ITEM.getKey(ring.getItem());
        Identifier tex = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "textures/gui/spell_icons/" + id.getPath() + ".png");
        boolean has = ICONS.computeIfAbsent(tex, t -> Minecraft.getInstance().getResourceManager().getResource(t).isPresent());
        if (has) {
            g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0.0F, 0.0F, 16, 16, 16, 16, ARGB.color(alpha, 255, 255, 255));
        } else {
            g.item(ring, x, y);
        }
    }
}
