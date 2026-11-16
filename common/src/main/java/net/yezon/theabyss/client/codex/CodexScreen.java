package net.yezon.theabyss.client.codex;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.util.AbyssTooltip;

public class CodexScreen extends Screen {
    static final int W = 328;
    static final int H = 216;
    static final int PAGE_W = 144;
    static final int PAGE_H = 188;
    static final int LEFT = 15;
    static final int RIGHT = 169;
    static final int TOP = 18;
    static final int LINE = 9;
    static final int CX = PAGE_W / 2;
    static final int PAD = (PAGE_W - 116) / 2;
    private static final int PREV_X = -4;
    private static final int NEXT_X = W - 14;
    private static final int BACK_X = W / 2 - 9;
    private static final int ARROW_Y = H - 6;
    private static final int FIRST_ROWS = (TOP + PAGE_H - 38) / 11;
    private static final int ROWS = PAGE_H / 11;
    static final Identifier PARTS = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "textures/gui/codex_parts.png");
    static final FontDescription UNIFORM_FONT = new FontDescription.Resource(Identifier.withDefaultNamespace("uniform"));
    private FontDescription UNIFORM = UNIFORM_FONT;

    private enum Kind {
        LANDING,
        CATEGORY,
        INDEX,
        ENTRY
    }

    private record Place(Kind kind, CodexBook.Category category, CodexBook.Entry entry, int spread) {
    }

    private static Place lastPlace;
    private static final Deque<Place> lastHistory = new ArrayDeque<>();

    private final CodexBook book;
    private Place place;
    private final Deque<Place> history = new ArrayDeque<>();
    private int left;
    private int top;
    private int ticks;
    private ItemStack hovered = ItemStack.EMPTY;
    private final List<Target> targets = new ArrayList<>();

    private record Target(int x, int y, int w, int h, Place place) {
    }

    private final java.util.Map<CodexBook.Entry, List<CodexBook.Page>> laidOut = new java.util.IdentityHashMap<>();

    public CodexScreen() {
        super(Component.translatable("codex.theabyss.screen"));
        this.book = CodexBook.load();
        this.UNIFORM = "uniform".equals(this.book.font) ? UNIFORM_FONT : FontDescription.DEFAULT;
        this.place = lastPlace != null && lastPlace.kind() != Kind.LANDING ? this.resolve(lastPlace) : new Place(Kind.LANDING, null, null, 0);
        if (this.place.kind() != Kind.LANDING) {
            for (Place p : lastHistory) {
                Place r = this.resolve(p);
                if (r != null) {
                    this.history.add(r);
                }
            }
        }
        if (this.place == null) {
            this.place = new Place(Kind.LANDING, null, null, 0);
        }
    }

    public static void open() {
        Minecraft.getInstance().gui.setScreen(new CodexScreen());
    }

    public static void openCategory(String categoryId) {
        CodexScreen screen = new CodexScreen();
        for (CodexBook.Category c : screen.book.categories) {
            if (c.id().equals(categoryId)) {
                screen.history.clear();
                screen.history.push(new Place(Kind.LANDING, null, null, 0));
                screen.place = new Place(Kind.CATEGORY, c, null, 0);
            }
        }
        Minecraft.getInstance().gui.setScreen(screen);
    }

    public static void openEntry(String entryId, int spread) {
        CodexScreen screen = new CodexScreen();
        for (CodexBook.Category c : screen.book.categories) {
            for (CodexBook.Entry e : c.entries()) {
                if (e.id().equals(entryId) && !locked(e)) {
                    screen.history.clear();
                    screen.history.push(new Place(Kind.CATEGORY, c, null, 0));
                    screen.place = new Place(Kind.ENTRY, c, e, spread);
                }
            }
        }
        Minecraft.getInstance().gui.setScreen(screen);
    }

    public static boolean clickBook(int x, int y, boolean right) {
        if (Minecraft.getInstance().gui.screen() instanceof CodexScreen screen) {
            return screen.mouseClicked(new MouseButtonEvent(screen.left + x + 0.5, screen.top + y + 0.5,
                new net.minecraft.client.input.MouseButtonInfo(right ? InputConstants.MOUSE_BUTTON_RIGHT : InputConstants.MOUSE_BUTTON_LEFT, 0)), false);
        }
        return false;
    }

    private Place resolve(Place p) {
        CodexBook.Category category = null;
        CodexBook.Entry entry = null;
        for (CodexBook.Category c : this.book.categories) {
            if (p.category() != null && c.id().equals(p.category().id())) {
                category = c;
            }
            for (CodexBook.Entry e : c.entries()) {
                if (p.entry() != null && e.id().equals(p.entry().id()) && e.category().equals(p.entry().category())) {
                    entry = e;
                }
            }
        }
        if (p.kind() == Kind.CATEGORY && category == null || p.kind() == Kind.ENTRY && entry == null) {
            return null;
        }
        return new Place(p.kind(), category, entry, p.spread());
    }

    @Override
    protected void init() {
        this.left = this.width / 2 - W / 2;
        this.top = this.height / 2 - H / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        this.ticks++;
    }

    @Override
    public void removed() {
        lastPlace = this.place;
        lastHistory.clear();
        lastHistory.addAll(this.history);
    }

    private int spreads() {
        return switch (this.place.kind()) {
            case LANDING -> 1;
            case CATEGORY, INDEX -> 1 + (int)Math.ceil(Math.max(0, this.entries().size() - FIRST_ROWS) / (double)(ROWS * 2));
            case ENTRY -> Math.max(1, (this.pages(this.place.entry()).size() + 1) / 2);
        };
    }

    private List<CodexBook.Entry> entries() {
        if (this.place.kind() == Kind.CATEGORY) {
            return this.place.category().entries();
        }
        List<CodexBook.Entry> all = new ArrayList<>();
        for (CodexBook.Category c : this.book.categories) {
            for (CodexBook.Entry e : c.entries()) {
                if (!locked(e)) {
                    all.add(e);
                }
            }
        }
        all.sort((a, b) -> a.name().compareTo(b.name()));
        return all;
    }

    private void go(Place next) {
        this.history.push(this.place);
        this.place = next;
        this.flipSound();
    }

    private void back(boolean toLanding) {
        if (this.history.isEmpty()) {
            return;
        }
        if (toLanding) {
            this.history.clear();
            this.place = new Place(Kind.LANDING, null, null, 0);
        } else {
            this.place = this.history.pop();
        }
        this.flipSound();
    }

    private void turn(int delta) {
        int spread = this.place.spread() + delta;
        if (spread >= 0 && spread < this.spreads()) {
            this.place = new Place(this.place.kind(), this.place.category(), this.place.entry(), spread);
            this.flipSound();
        }
    }

    private void flipSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 0.7F + (float)Math.random() * 0.3F));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int mx = (int)event.x() - this.left;
        int my = (int)event.y() - this.top;
        switch (event.button()) {
            case InputConstants.MOUSE_BUTTON_LEFT -> {
                if (this.place.spread() > 0 && in(mx, my, PREV_X, ARROW_Y, 18, 10)) {
                    this.turn(-1);
                    return true;
                }
                if (this.place.spread() + 1 < this.spreads() && in(mx, my, NEXT_X, ARROW_Y, 18, 10)) {
                    this.turn(1);
                    return true;
                }
                if (!this.history.isEmpty() && in(mx, my, BACK_X, ARROW_Y + 1, 18, 9)) {
                    this.back(event.hasShiftDown());
                    return true;
                }
                for (Target t : this.targets) {
                    if (in(mx, my, t.x(), t.y(), t.w(), t.h())) {
                        this.go(t.place());
                        return true;
                    }
                }
            }
            case InputConstants.MOUSE_BUTTON_RIGHT -> {
                this.back(event.hasShiftDown());
                return true;
            }
            case InputConstants.MOUSE_BUTTON_4 -> {
                this.turn(-1);
                return true;
            }
            case InputConstants.MOUSE_BUTTON_5 -> {
                this.turn(1);
                return true;
            }
            default -> {
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (scrollY != 0) {
            this.turn(scrollY > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (key == InputConstants.KEY_BACKSPACE) {
            this.back(event.hasShiftDown());
            return true;
        }
        if (key == InputConstants.KEY_LEFT || key == InputConstants.KEY_PAGEUP) {
            this.turn(-1);
            return true;
        }
        if (key == InputConstants.KEY_RIGHT || key == InputConstants.KEY_PAGEDOWN) {
            this.turn(1);
            return true;
        }
        if (Minecraft.getInstance().options.keyInventory.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    private static boolean in(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        this.hovered = ItemStack.EMPTY;
        this.targets.clear();
        int mx = mouseX - this.left;
        int my = mouseY - this.top;
        this.blit(g, this.book.background, 0, 0, 0, 0, W, H, 512, 256);
        switch (this.place.kind()) {
            case LANDING -> this.drawLanding(g, mx, my);
            case CATEGORY, INDEX -> this.drawList(g, mx, my);
            case ENTRY -> this.drawEntry(g, mx, my);
        }
        if (this.place.spread() > 0) {
            this.blit(g, this.book.texture, PREV_X, ARROW_Y, 272 + (in(mx, my, PREV_X, ARROW_Y, 18, 10) ? 18 : 0), 10, 18, 10, 512, 256);
        }
        if (this.place.spread() + 1 < this.spreads()) {
            this.blit(g, this.book.texture, NEXT_X, ARROW_Y, 272 + (in(mx, my, NEXT_X, ARROW_Y, 18, 10) ? 18 : 0), 0, 18, 10, 512, 256);
        }
        if (!this.history.isEmpty()) {
            this.blit(g, this.book.texture, BACK_X, ARROW_Y + 1, 308 + (in(mx, my, BACK_X, ARROW_Y + 1, 18, 9) ? 18 : 0), 0, 18, 9, 512, 256);
        }
        if (!this.hovered.isEmpty()) {
            g.setTooltipForNextFrame(this.font, this.hovered, mouseX, mouseY);
        }
    }

    private void drawLanding(GuiGraphicsExtractor g, int mx, int my) {
        this.blit(g, this.book.texture, -8, 12, 0, 180, 140, 31, 512, 256);
        g.text(this.font, this.book.title, this.left + 13, this.top + 16, this.book.nameplateColor, false);
        g.text(this.font, Component.literal(this.book.edition).withStyle(s -> s.withFont(UNIFORM)), this.left + 24, this.top + 24, this.book.nameplateColor, false);
        this.textBlock(g, this.book.landingText, LEFT, 43, PAGE_W, this.book.textColor);
        this.header(g, AbyssTooltip.tr("codex.theabyss.categories"), RIGHT + CX, TOP);
        this.separator(g, RIGHT, TOP + 12);
        int i = 0;
        for (CodexBook.Category c : this.book.categories) {
            this.categoryButton(g, mx, my, RIGHT + CX - 46 + (i % 4) * 24, 43 + (i / 4) * 24, item(c.icon()), c.name(), new Place(Kind.CATEGORY, c, null, 0));
            i++;
        }
        this.categoryButton(g, mx, my, RIGHT + CX - 46 + (i % 4) * 24, 43 + (i / 4) * 24, item("theabyss:the_abyss_guide"), AbyssTooltip.tr("codex.theabyss.index"), new Place(Kind.INDEX, null, null, 0));
        int rows = i / 4 + 1;
        this.separator(g, RIGHT, 30 + 25 + 24 * rows);
    }

    private void categoryButton(GuiGraphicsExtractor g, int mx, int my, int x, int y, ItemStack icon, String name, Place target) {
        boolean hover = in(mx, my, x, y, 20, 20);
        g.item(icon, this.left + x + 2, this.top + y + 2);
        if (!hover) {
            this.blit(g, this.book.background, x, y, x, y, 20, 20, 512, 256, ARGB.color(128, 255, 255, 255));
        } else {
            g.setTooltipForNextFrame(Component.literal(name), this.left + mx, this.top + my);
        }
        this.targets.add(new Target(x, y, 20, 20, target));
    }

    private void drawList(GuiGraphicsExtractor g, int mx, int my) {
        List<CodexBook.Entry> entries = this.entries();
        int spread = this.place.spread();
        if (spread == 0) {
            String title = this.place.kind() == Kind.INDEX ? AbyssTooltip.tr("codex.theabyss.index") : this.place.category().name();
            String description = this.place.kind() == Kind.INDEX ? AbyssTooltip.tr("codex.theabyss.index.description") : this.place.category().description();
            if (this.place.kind() == Kind.CATEGORY && "chronicles".equals(this.place.category().id()) && Minecraft.getInstance().player != null) {
                description = description + " " + AbyssTooltip.tr("codex.theabyss.pages_written", net.yezon.theabyss.lore.ChronicleData.of(Minecraft.getInstance().player).count(),
                    net.yezon.theabyss.lore.ChronicleData.COUNT);
            }
            this.header(g, title, LEFT + CX, TOP);
            this.separator(g, LEFT, TOP + 12);
            this.textBlock(g, description, LEFT, 40, PAGE_W, this.book.textColor);
            this.header(g, AbyssTooltip.tr("codex.theabyss.chapters"), RIGHT + CX, TOP);
            this.separator(g, RIGHT, TOP + 12);
            for (int i = 0; i < FIRST_ROWS && i < entries.size(); i++) {
                this.entryButton(g, mx, my, RIGHT, 38 + i * 11, entries.get(i));
            }
            if (entries.isEmpty()) {
                g.centeredText(this.font, AbyssTooltip.tr("codex.theabyss.no_entries"), this.left + RIGHT + CX, this.top + 60, 0xFF333333);
            }
        } else {
            int start = FIRST_ROWS + (spread - 1) * ROWS * 2;
            for (int i = 0; i < ROWS * 2 && start + i < entries.size(); i++) {
                int x = i < ROWS ? LEFT : RIGHT;
                this.entryButton(g, mx, my, x, TOP + (i % ROWS) * 11, entries.get(start + i));
            }
        }
    }

    static boolean locked(CodexBook.Entry entry) {
        Minecraft mc = Minecraft.getInstance();
        return entry.chronicle() > 0 && (mc.player == null || !net.yezon.theabyss.lore.ChronicleData.of(mc.player).has(entry.chronicle()));
    }

    private void entryButton(GuiGraphicsExtractor g, int mx, int my, int x, int y, CodexBook.Entry entry) {
        if (locked(entry)) {
            g.text(this.font, Component.literal(entry.chronicle() + ". ???").withStyle(s -> s.withFont(UNIFORM)), this.left + x + 12, this.top + y + 1,
                0xFF8C8270, false);
            return;
        }
        if (in(mx, my, x, y, PAGE_W, 10)) {
            g.fill(this.left + x, this.top + y, this.left + x + PAGE_W, this.top + y + 10, 0x22000000);
        }
        g.pose().pushMatrix();
        g.pose().translate(this.left + x + 1, this.top + y + 1);
        g.pose().scale(0.5F, 0.5F);
        g.item(item(entry.icon()), 0, 0);
        g.pose().popMatrix();
        g.text(this.font, Component.literal(entry.name()).withStyle(s -> s.withFont(UNIFORM)), this.left + x + 12, this.top + y + 1, this.book.textColor, false);
        this.targets.add(new Target(x, y, PAGE_W, 10, new Place(Kind.ENTRY, null, entry, 0)));
    }

    private void drawEntry(GuiGraphicsExtractor g, int mx, int my) {
        CodexBook.Entry entry = this.place.entry();
        List<CodexBook.Page> pages = this.pages(entry);
        int first = this.place.spread() * 2;
        for (int side = 0; side < 2; side++) {
            int index = first + side;
            int px = side == 0 ? LEFT : RIGHT;
            if (index < pages.size()) {
                CodexPages.draw(this, g, entry, pages.get(index), index, px, TOP, mx, my);
            } else if (side == 1) {
                this.blit(g, this.book.filler, px + CX - 64, TOP + PAGE_H / 2 - 64, 0, 0, 128, 128, 128, 128);
            }
        }
    }

    List<CodexBook.Page> pages(CodexBook.Entry entry) {
        return this.laidOut.computeIfAbsent(entry, this::layout);
    }

    private List<CodexBook.Page> layout(CodexBook.Entry entry) {
        List<CodexBook.Page> out = new ArrayList<>();
        for (CodexBook.Page page : entry.pages()) {
            String text = page.string("text");
            int y = textStart(page, out.size());
            if (text == null || y == Integer.MIN_VALUE) {
                out.add(page);
                continue;
            }
            int fit = Math.max(1, (PAGE_H - y) / LINE);
            List<int[]> lines = this.lineRanges(text);
            if (lines.size() <= fit) {
                out.add(page);
                continue;
            }
            int cut = lines.get(fit - 1)[1];
            out.add(withText(page, text.substring(0, cut).trim()));
            String rest = text.substring(cut).trim();
            int full = PAGE_H / LINE;
            while (!rest.isEmpty()) {
                lines = this.lineRanges(rest);
                if (lines.size() <= full) {
                    out.add(continuation(rest));
                    break;
                }
                cut = lines.get(full - 1)[1];
                out.add(continuation(rest.substring(0, cut).trim()));
                rest = rest.substring(cut).trim();
            }
        }
        return out;
    }

    private static int textStart(CodexBook.Page page, int pageNum) {
        return switch (page.type()) {
            case "text" -> pageNum == 0 ? 22 : page.string("title") != null ? 12 : 0;
            case "item" -> 40;
            case "image" -> 120;
            case "crafting" -> 69;
            case "block" -> 115;
            default -> Integer.MIN_VALUE;
        };
    }

    private List<int[]> lineRanges(String text) {
        return this.lineRanges(text, PAGE_W);
    }

    private List<int[]> lineRanges(String text, int width) {
        List<int[]> lines = new ArrayList<>();
        this.font.getSplitter().splitLines(text, width, net.minecraft.network.chat.Style.EMPTY.withFont(UNIFORM), true,
            (style, start, end) -> lines.add(new int[] {start, end}));
        return lines;
    }

    private static CodexBook.Page withText(CodexBook.Page page, String text) {
        com.google.gson.JsonObject json = page.json().deepCopy();
        json.addProperty("text", text);
        return new CodexBook.Page(page.type(), json);
    }

    private static CodexBook.Page continuation(String text) {
        com.google.gson.JsonObject json = new com.google.gson.JsonObject();
        json.addProperty("type", "text");
        json.addProperty("text", text);
        return new CodexBook.Page("text", json);
    }

    CodexBook book() {
        return this.book;
    }

    int ticks() {
        return this.ticks;
    }

    void blit(GuiGraphicsExtractor g, Identifier texture, int x, int y, int u, int v, int w, int h, int texW, int texH) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, this.left + x, this.top + y, u, v, w, h, texW, texH);
    }

    void blit(GuiGraphicsExtractor g, Identifier texture, int x, int y, int u, int v, int w, int h, int texW, int texH, int color) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, this.left + x, this.top + y, u, v, w, h, texW, texH, color);
    }

    void header(GuiGraphicsExtractor g, String text, int x, int y) {
        g.text(this.font, text, this.left + x - this.font.width(text) / 2, this.top + y, this.book.headerColor, false);
    }

    void separator(GuiGraphicsExtractor g, int pageX, int y) {
        this.blit(g, this.book.texture, pageX + CX - 55, y, 140, 180, 110, 3, 512, 256, ARGB.color(204, 255, 255, 255));
    }

    void textBlock(GuiGraphicsExtractor g, String text, int x, int y, int width, int color) {
        if (text == null || text.isEmpty()) {
            return;
        }
        int room = TOP + PAGE_H - y;
        int virtualWidth = width;
        List<int[]> lines = this.lineRanges(text, virtualWidth);
        while (lines.size() * LINE * width / (float)virtualWidth > room + 2 && virtualWidth < width * 4) {
            virtualWidth += 4;
            lines = this.lineRanges(text, virtualWidth);
        }
        float scale = width / (float)virtualWidth;
        g.pose().pushMatrix();
        g.pose().translate(this.left + x, this.top + y);
        g.pose().scale(scale, scale);
        for (int i = 0; i < lines.size(); i++) {
            String part = text.substring(lines.get(i)[0], lines.get(i)[1]);
            g.text(this.font, Component.literal(part).withStyle(st -> st.withFont(UNIFORM)), 0, i * LINE, color, false);
        }
        g.pose().popMatrix();
    }

    void itemAt(GuiGraphicsExtractor g, ItemStack stack, int x, int y, boolean framed, int mx, int my) {
        if (framed) {
            this.blit(g, PARTS, x - 5, y - 5, 0, 64, 26, 26, 128, 128);
        }
        if (stack.isEmpty()) {
            return;
        }
        g.item(stack, this.left + x, this.top + y);
        if (in(mx, my, x, y, 16, 16)) {
            this.hovered = stack;
        }
    }

    void bigItem(GuiGraphicsExtractor g, ItemStack stack, int centreX, int centreY, float scale) {
        g.pose().pushMatrix();
        g.pose().translate(this.left + centreX - 8 * scale, this.top + centreY - 8 * scale);
        g.pose().scale(scale, scale);
        g.item(stack, 0, 0);
        g.pose().popMatrix();
    }

    ItemStack stack(String id) {
        if (id == null || id.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (id.startsWith("#")) {
            List<Item> items = new ArrayList<>();
            BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM, Identifier.parse(id.substring(1)))).forEach(h -> items.add(h.value()));
            return items.isEmpty() ? ItemStack.EMPTY : new ItemStack(items.get(this.ticks / 20 % items.size()));
        }
        return item(id);
    }

    static ItemStack item(String id) {
        Identifier key = Identifier.tryParse(id);
        if (key == null) {
            return ItemStack.EMPTY;
        }
        return BuiltInRegistries.ITEM.getOptional(key).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    net.minecraft.client.gui.Font font() {
        return this.font;
    }
}
