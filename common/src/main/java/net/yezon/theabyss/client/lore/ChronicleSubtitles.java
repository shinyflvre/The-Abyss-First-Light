package net.yezon.theabyss.client.lore;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.compat.GuiGraphicsCompat;
import net.yezon.theabyss.lore.Chronicles;

public final class ChronicleSubtitles {
    private static final String FALLBACK = "en_us";
    private static final int JOURNAL = 0xEAF6F4;
    private static final int BLACK_INK = 0xC4CBD6;

    private record Cue(double start, double end, String text) {
    }

    private static List<Cue> cues = List.of();
    private static boolean blackInk;
    private static double played;
    private static long last;

    private ChronicleSubtitles() {
    }

    static void start(int page) {
        cues = load(String.format("page_%02d", page));
        blackInk = Chronicles.blackInk(page);
        played = 0.0;
        last = System.nanoTime();
    }

    static void stop() {
        cues = List.of();
    }

    private static List<Cue> load(String key) {
        List<Cue> out = new ArrayList<>();
        String lang = Minecraft.getInstance().getLanguageManager().getSelected();
        ResourceLocation file = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "chronicle/" + lang + "/subtitles.json");
        if (Minecraft.getInstance().getResourceManager().getResource(file).isEmpty()) {
            file = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "chronicle/" + FALLBACK + "/subtitles.json");
        }
        try (Reader reader = Minecraft.getInstance().getResourceManager().openAsReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (root.has(key)) {
                for (JsonElement e : root.getAsJsonArray(key)) {
                    JsonObject o = e.getAsJsonObject();
                    out.add(new Cue(o.get("start").getAsDouble(), o.get("end").getAsDouble(), o.get("text").getAsString()));
                }
            }
        } catch (Exception e) {
            TheabyssMod.LOGGER.warn("Could not read chronicle subtitles", e);
        }
        return out;
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (cues.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        long now = System.nanoTime();
        if (!mc.isPaused()) {
            played += (now - last) / 1.0E9;
        }
        last = now;
        Cue cue = null;
        for (Cue c : cues) {
            if (played >= c.start() && played <= c.end()) {
                cue = c;
                break;
            }
        }
        if (cue == null) {
            return;
        }
        double fade = Math.min(1.0, Math.min((played - cue.start()) / 0.2, (cue.end() - played) / 0.25));
        int alpha = (int)(Math.max(0.0, fade) * 255.0);
        if (alpha < 8) {
            return;
        }
        Font font = mc.font;
        MutableComponent text = Component.literal(cue.text());
        if (blackInk) {
            text = text.withStyle(ChatFormatting.ITALIC);
        }
        List<FormattedCharSequence> lines = font.split(text, Math.min(graphics.guiWidth() - 40, 340));
        int width = 0;
        for (FormattedCharSequence line : lines) {
            width = Math.max(width, font.width(line));
        }
        int lineHeight = 10;
        int cx = graphics.guiWidth() / 2;
        int top = graphics.guiHeight() - 92 - lines.size() * lineHeight;
        graphics.fill(cx - width / 2 - 4, top - 3, cx + width / 2 + 4, top + lines.size() * lineHeight + 1, (alpha * 150 / 255) << 24);
        int colour = (alpha << 24) | (blackInk ? BLACK_INK : JOURNAL);
        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            GuiGraphicsCompat.text(graphics, font, line, cx - font.width(line) / 2, top + i * lineHeight, colour, true);
        }
    }
}
