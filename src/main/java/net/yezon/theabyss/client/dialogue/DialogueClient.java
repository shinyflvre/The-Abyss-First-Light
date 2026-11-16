package net.yezon.theabyss.client.dialogue;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModKeyMappings;
import net.yezon.theabyss.init.TheabyssModSounds;
import net.yezon.theabyss.network.DialogueMessage;
import net.yezon.theabyss.network.DialogueReplyMessage;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class DialogueClient {
    private static final Identifier BOX = sprite("box");
    private static final Identifier ARROW = sprite("arrow");
    private static final Identifier ARROW_HIGHLIGHTED = sprite("arrow_highlighted");
    private static final Identifier OPTION = sprite("option");
    private static final Identifier OPTION_HIGHLIGHTED = sprite("option_highlighted");
    private static final Identifier POINTER_HIGHLIGHTED = sprite("pointer_highlighted");
    private static final int NAME_COLOUR = 0xFF7FE8FF;
    private static final int INFO_COLOUR = 0xFFFFA040;
    private static final String INFO_SPEAKER = "dialog.theabyss.speaker.info";
    private static final int TEXT_COLOUR = 0xFFE2E8EC;
    private static final int NUMBER_COLOUR = 0xFF66ABBB;
    private static final int OPTION_COLOUR = 0xFF96BAC4;
    private static final int OPTION_SELECTED_COLOUR = 0xFFECFCFF;
    private static final int PAD_X = 20;
    private static final int TOP = 17;
    private static final int LINE_H = 10;
    private static final int BOTTOM_PAD = 18;
    private static final int START_WIDTH = 280;
    private static final int MIN_WIDTH = 112;
    private static final int SCREEN_MARGIN = 24;
    private static final int HUD_CLEARANCE = 56;
    private static final int MAX_ROWS = 2;
    private static final int BAR_H = 18;
    private static final int GAP_X = 4;
    private static final int GAP_Y = 3;
    private static final int ABOVE_BOX = 4;
    private static final int OPTION_TEXT_X = 14;
    private static final int NUMBER_GAP = 3;
    private static final int OPTION_RIGHT_PAD = 8;
    private static final float CHARS_PER_SECOND = 45.0F;
    private static final float HOLD_SECONDS = 1.6F;
    private static final float HOLD_PER_CHAR = 0.045F;
    private static final float BLINK_SECONDS = 0.5F;
    private static final float SPEECH_VOLUME = 0.5F;
    private static final float SPEECH_MIN_PITCH = 0.9F;
    private static final float SPEECH_MAX_PITCH = 1.1F;
    private static final RandomSource SPEECH_RANDOM = RandomSource.create();
    private static SpeechLoop speech;
    private static final java.util.ArrayDeque<DialogueMessage> QUEUE = new java.util.ArrayDeque<>();

    private static boolean active;
    private static int id;
    private static String speaker = "";
    private static boolean info;
    private static final List<String> lines = new ArrayList<>();
    private static final List<String> choices = new ArrayList<>();
    private static int index;
    private static float typed;
    private static float hold;
    private static float clock;
    private static int highlight;
    private static long lastNanos;
    private static int layoutFor = -1;
    private static int boxWidth;
    private static int boxHeight;
    private static boolean holdPlayer;
    private static long waitUntil;
    private static final List<int[]> choiceRects = new ArrayList<>();
    private static final long WAIT_NANOS = 1_500_000_000L;

    private DialogueClient() {
    }

    private static Identifier sprite(String name) {
        return Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "dialog/" + name);
    }

    public static void show(DialogueMessage message) {
        boolean plain = message.choices().isEmpty() && !message.hold() && !message.lines().isEmpty();
        if (plain && active && choices.isEmpty() && !holdPlayer) {
            QUEUE.addLast(message);
            return;
        }
        if (!plain) {
            QUEUE.clear();
        }
        waitUntil = 0L;
        if (message.lines().isEmpty() && message.choices().isEmpty()) {
            active = false;
            return;
        }
        id = message.id();
        holdPlayer = message.hold();
        speaker = Component.translatable(message.speaker()).getString();
        info = INFO_SPEAKER.equals(message.speaker());
        lines.clear();
        for (String key : message.lines()) {
            lines.add(Component.translatable(key, Component.keybind(TheabyssModKeyMappings.RIFT_OF_SINS.getName())).getString());
        }
        if (lines.isEmpty()) {
            lines.add("");
        }
        choices.clear();
        for (String key : message.choices()) {
            choices.add(Component.translatable(key).getString());
        }
        index = 0;
        typed = 0.0F;
        hold = 0.0F;
        highlight = 0;
        layoutFor = -1;
        lastNanos = System.nanoTime();
        active = true;
    }

    private static String current() {
        return lines.get(index);
    }

    private static boolean lineTyped() {
        return typed >= current().length();
    }

    private static boolean choosing() {
        return active && !choices.isEmpty() && index == lines.size() - 1 && lineTyped();
    }

    private static void advance() {
        if (index < lines.size() - 1) {
            index++;
            typed = 0.0F;
            hold = 0.0F;
        } else if (choices.isEmpty()) {
            active = false;
            ClientPacketDistributor.sendToServer(new DialogueReplyMessage(id, DialogueReplyMessage.FINISHED));
            DialogueMessage queued = QUEUE.pollFirst();
            if (queued != null) {
                show(queued);
            }
        }
    }

    private static void pick(int choice) {
        active = false;
        waitUntil = System.nanoTime() + WAIT_NANOS;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.2F, 0.35F));
        ClientPacketDistributor.sendToServer(new DialogueReplyMessage(id, choice));
    }

    private static void onContinue() {
        if (!active) {
            return;
        }
        if (!lineTyped()) {
            typed = current().length();
        } else if (choosing()) {
            pick(highlight);
        } else {
            advance();
        }
    }

    static boolean holdsPlayer() {
        return !voiceFirst() && (active && holdPlayer || System.nanoTime() < waitUntil);
    }

    private static boolean voiceFirst() {
        return net.yezon.theabyss.client.lore.ChronicleVoice.speaking();
    }

    static void continueKey() {
        onContinue();
    }

    static void keyChoice(int choice) {
        if (choosing() && choice < choices.size()) {
            pick(choice);
        }
    }

    static void scroll(double delta) {
        if (choosing() && delta != 0.0) {
            highlight = Math.floorMod(highlight + (delta > 0.0 ? -1 : 1), choices.size());
        }
    }

    static void hover(double mouseX, double mouseY) {
        int hit = rectAt(mouseX, mouseY);
        if (choosing() && hit >= 0) {
            highlight = hit;
        }
    }

    static void click(double mouseX, double mouseY) {
        int hit = rectAt(mouseX, mouseY);
        if (choosing() && hit >= 0) {
            pick(hit);
        } else if (!choosing()) {
            onContinue();
        }
    }

    static void cancel() {
        if (active || System.nanoTime() < waitUntil) {
            active = false;
            waitUntil = 0L;
            ClientPacketDistributor.sendToServer(new DialogueReplyMessage(id, DialogueReplyMessage.CANCEL));
        }
    }

    private static int rectAt(double mouseX, double mouseY) {
        for (int i = 0; i < choiceRects.size(); i++) {
            int[] r = choiceRects.get(i);
            if (mouseX >= r[0] && mouseY >= r[1] && mouseX < r[0] + r[2] && mouseY < r[1] + r[3]) {
                return i;
            }
        }
        return -1;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (TheabyssModKeyMappings.DIALOG_CONTINUE.consumeClick()) {
            if (mc.gui.screen() == null && !voiceFirst()) {
                onContinue();
            }
        }
        if (mc.player == null) {
            return;
        }
        boolean hold = holdsPlayer() && mc.player.isAlive();
        if (hold && mc.gui.screen() == null) {
            mc.gui.setScreen(new DialogueScreen());
        } else if (!hold && mc.gui.screen() instanceof DialogueScreen) {
            mc.gui.setScreen(null);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        QUEUE.clear();
        active = false;
        waitUntil = 0L;
    }

    private static void update() {
        long now = System.nanoTime();
        float dt = Minecraft.getInstance().isPaused() ? 0.0F : Math.min(0.1F, (now - lastNanos) / 1.0E9F);
        lastNanos = now;
        clock += dt;
        if (!lineTyped()) {
            typed = Math.min(current().length(), typed + dt * CHARS_PER_SECOND);
            speak();
            return;
        }
        if (!choices.isEmpty()) {
            return;
        }
        hold += dt;
        if (hold >= HOLD_SECONDS + current().length() * HOLD_PER_CHAR) {
            advance();
        }
    }

    static boolean typing() {
        Minecraft mc = Minecraft.getInstance();
        return active && !lineTyped() && !voiceFirst() && !mc.isPaused() && mc.player != null && !mc.gui.hud.isHidden();
    }

    private static void speak() {
        if (speech == null || speech.isStopped()) {
            speech = new SpeechLoop(SPEECH_MIN_PITCH + SPEECH_RANDOM.nextFloat() * (SPEECH_MAX_PITCH - SPEECH_MIN_PITCH));
            Minecraft.getInstance().getSoundManager().play(speech);
        }
    }

    private static final class SpeechLoop extends AbstractTickableSoundInstance {
        SpeechLoop(float pitch) {
            super(TheabyssModSounds.PIXEL_SPEECH.get(), SoundSource.VOICE, SoundInstance.createUnseededRandom());
            this.looping = true;
            this.delay = 0;
            this.volume = SPEECH_VOLUME;
            this.pitch = pitch;
            this.relative = true;
            this.attenuation = SoundInstance.Attenuation.NONE;
        }

        @Override
        public void tick() {
            if (!typing()) {
                this.stop();
            }
        }
    }

    private static List<String> wrap(Font font, String text, int width) {
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String test = line.isEmpty() ? word : line + " " + word;
            if (font.width(test) > width && !line.isEmpty()) {
                out.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        out.add(line.toString());
        return out;
    }

    private static int textX(Font font, int choice) {
        return OPTION_TEXT_X + font.width((choice + 1) + ".") + NUMBER_GAP;
    }

    private static void layout(Font font, int screenWidth) {
        int max = Math.max(MIN_WIDTH, screenWidth - 2 * SCREEN_MARGIN);
        int width = Math.min(START_WIDTH, max);
        while (width < max && rowsNeeded(font, width) > MAX_ROWS) {
            width += 4;
        }
        if (!choices.isEmpty()) {
            int cell = 0;
            for (int i = 0; i < choices.size(); i++) {
                cell = Math.max(cell, textX(font, i) + font.width(choices.get(i)) + OPTION_RIGHT_PAD);
            }
            width = Math.max(width, Math.min(max, 2 * cell + GAP_X));
        }
        boxWidth = Math.min(width, max);
        int rows = Math.max(1, rowsNeeded(font, boxWidth));
        boxHeight = TOP + (1 + rows) * LINE_H + BOTTOM_PAD;
        layoutFor = screenWidth;
    }

    private static int rowsNeeded(Font font, int width) {
        int rows = 0;
        for (String line : lines) {
            rows = Math.max(rows, wrap(font, line, width - 2 * PAD_X).size());
        }
        return rows;
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (!active || mc.player == null || mc.gui.hud.isHidden()) {
            return;
        }
        if (voiceFirst()) {
            lastNanos = System.nanoTime();
            return;
        }
        update();
        if (!active) {
            return;
        }
        Font font = mc.font;
        int screenWidth = graphics.guiWidth();
        if (layoutFor != screenWidth) {
            layout(font, screenWidth);
        }
        int x = (screenWidth - boxWidth) / 2;
        int y = graphics.guiHeight() - HUD_CLEARANCE - boxHeight;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BOX, x, y, boxWidth, boxHeight);
        graphics.text(font, speaker, x + PAD_X, y + TOP, info ? INFO_COLOUR : NAME_COLOUR, true);
        int left = (int)typed;
        int ty = y + TOP + LINE_H + 1;
        for (String row : wrap(font, current(), boxWidth - 2 * PAD_X)) {
            if (left <= 0) {
                break;
            }
            graphics.text(font, row.substring(0, Math.min(row.length(), left)), x + PAD_X, ty, TEXT_COLOUR, true);
            left -= row.length() + 1;
            ty += LINE_H;
        }
        if (choosing()) {
            renderChoices(graphics, font, x, y);
        } else if (lineTyped()) {
            boolean lit = (int)(clock / BLINK_SECONDS) % 2 == 0;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, lit ? ARROW_HIGHLIGHTED : ARROW, x + boxWidth - PAD_X - 4, y + boxHeight - BOTTOM_PAD, 9, 6);
        }
    }

    private static void renderChoices(GuiGraphicsExtractor graphics, Font font, int boxX, int boxY) {
        choiceRects.clear();
        int gap = GAP_X + (boxWidth - GAP_X) % 2;
        int cell = (boxWidth - gap) / 2;
        List<int[]> rows = new ArrayList<>();
        int total = 0;
        for (int r = 0; r < choices.size(); r += 2) {
            int count = 1;
            for (int i = r; i < Math.min(r + 2, choices.size()); i++) {
                count = Math.max(count, wrap(font, choices.get(i), cell - textX(font, i) - OPTION_RIGHT_PAD).size());
            }
            int height = BAR_H + (count - 1) * LINE_H;
            rows.add(new int[] {r, height});
            total += height;
        }
        total += GAP_Y * (rows.size() - 1);
        int y = boxY - ABOVE_BOX - total;
        for (int[] row : rows) {
            for (int i = row[0]; i < Math.min(row[0] + 2, choices.size()); i++) {
                int cx = boxX + (i % 2) * (cell + gap);
                boolean selected = i == highlight;
                choiceRects.add(new int[] {cx, y, cell, row[1]});
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, selected ? OPTION_HIGHLIGHTED : OPTION, cx, y, cell, row[1]);
                List<String> wrapped = wrap(font, choices.get(i), cell - textX(font, i) - OPTION_RIGHT_PAD);
                int top = y + (row[1] - (wrapped.size() * LINE_H - 2)) / 2;
                if (selected) {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, POINTER_HIGHLIGHTED, cx + 5, y + (row[1] - 9) / 2, 6, 9);
                }
                graphics.text(font, (i + 1) + ".", cx + OPTION_TEXT_X, top, NUMBER_COLOUR, true);
                for (int n = 0; n < wrapped.size(); n++) {
                    graphics.text(font, wrapped.get(n), cx + textX(font, i), top + n * LINE_H, selected ? OPTION_SELECTED_COLOUR : OPTION_COLOUR, true);
                }
            }
            y += row[1] + GAP_Y;
        }
    }
}
