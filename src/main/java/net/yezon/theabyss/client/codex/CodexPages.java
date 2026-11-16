package net.yezon.theabyss.client.codex;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

final class CodexPages {
    private CodexPages() {
    }

    static void draw(CodexScreen s, GuiGraphicsExtractor g, CodexBook.Entry entry, CodexBook.Page page, int pageNum, int px, int py, int mx, int my) {
        int color = entry.chronicle() <= 0 ? s.book().textColor
            : net.yezon.theabyss.lore.Chronicles.blackInk(entry.chronicle()) ? 0xFF0B0B0D : 0xFF23395A;
        switch (page.type()) {
            case "text" -> {
                if (pageNum == 0) {
                    s.header(g, entry.name(), px + CodexScreen.CX, py);
                    s.separator(g, px, py + 12);
                    s.textBlock(g, page.string("text"), px, py + 22, CodexScreen.PAGE_W, color);
                } else if (page.string("title") != null) {
                    s.header(g, page.string("title"), px + CodexScreen.CX, py);
                    s.textBlock(g, page.string("text"), px, py + 12, CodexScreen.PAGE_W, color);
                } else {
                    s.textBlock(g, page.string("text"), px, py, CodexScreen.PAGE_W, color);
                }
            }
            case "item" -> {
                ItemStack stack = s.stack(page.string("item"));
                s.blit(g, CodexScreen.PARTS, px + CodexScreen.PAD + 25, py + 10, 0, 96, 66, 26, 128, 128);
                s.itemAt(g, stack, px + CodexScreen.PAD + 50, py + 15, false, mx, my);
                String title = page.string("title") != null ? page.string("title") : stack.getHoverName().getString();
                s.header(g, title, px + CodexScreen.CX, py);
                s.textBlock(g, page.string("text"), px, py + 40, CodexScreen.PAGE_W, color);
            }
            case "image" -> {
                if (Boolean.TRUE.equals(page.json().has("border") && page.json().get("border").getAsBoolean())) {
                    s.blit(g, s.book().texture, px + CodexScreen.PAD + 5, py + 7, 405, 149, 106, 106, 512, 256);
                }
                Identifier image = Identifier.parse(page.string("image"));
                g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, image, screenX(s, px + CodexScreen.PAD + 8), screenY(s, py + 10), 0, 0, 100, 100, 200, 200, 256, 256);
                if (page.string("title") != null) {
                    s.header(g, page.string("title"), px + CodexScreen.CX, py - 3);
                }
                s.textBlock(g, page.string("text"), px, py + 120, CodexScreen.PAGE_W, color);
            }
            case "crafting" -> {
                s.blit(g, CodexScreen.PARTS, px + CodexScreen.PAD + 7, py + 2, 0, 0, 102, 62, 128, 128);
                List<String> grid = page.strings("grid");
                for (int i = 0; i < 9 && i < grid.size(); i++) {
                    s.itemAt(g, s.stack(grid.get(i)), px + CodexScreen.PAD + 12 + (i % 3) * 19, py + 7 + (i / 3) * 19, false, mx, my);
                }
                ItemStack result = s.stack(page.string("result"));
                if (page.json().has("count")) {
                    result.setCount(page.json().get("count").getAsInt());
                }
                s.itemAt(g, result, px + CodexScreen.PAD + 88, py + 26, false, mx, my);
                if (result.getCount() > 1) {
                    g.itemDecorations(s.font(), result, screenX(s, px + CodexScreen.PAD + 88), screenY(s, py + 26));
                }
                String title = page.string("title") != null ? page.string("title") : result.getHoverName().getString();
                s.header(g, title, px + CodexScreen.CX, py - 6);
                s.textBlock(g, page.string("text"), px, py + 69, CodexScreen.PAGE_W, color);
            }
            case "block" -> {
                s.blit(g, s.book().texture, px + CodexScreen.PAD + 5, py + 7, 405, 149, 106, 106, 512, 256);
                ItemStack block = s.stack(page.string("block"));
                s.bigItem(g, block, px + CodexScreen.CX, py + 60, 4.0F);
                String title = page.string("title") != null ? page.string("title") : block.getHoverName().getString();
                s.header(g, title, px + CodexScreen.CX, py);
                s.textBlock(g, page.string("text"), px, py + 115, CodexScreen.PAGE_W, color);
            }
            case "multiblock" -> {
                s.blit(g, s.book().texture, px + CodexScreen.PAD + 5, py + 7, 405, 149, 106, 106, 512, 256);
                int x0 = screenX(s, px + CodexScreen.PAD + 8);
                int y0 = screenY(s, py + 10);
                CodexMultiblock.submit(g, CodexMultiblock.structure(page), x0, y0, x0 + 100, y0 + 100);
                if (page.string("title") != null) {
                    s.header(g, page.string("title"), px + CodexScreen.CX, py);
                }
                s.textBlock(g, page.string("text"), px, py + 115, CodexScreen.PAGE_W, color);
            }
            case "arcane" -> {
                int[][] around = {{10, 60}, {20, 34}, {48, 20}, {75, 34}, {85, 60}, {75, 86}, {48, 100}, {20, 86}};
                List<String> inputs = page.strings("inputs");
                for (int i = 0; i < around.length && i < inputs.size(); i++) {
                    s.itemAt(g, s.stack(inputs.get(i)), px + CodexScreen.PAD + around[i][0], py + around[i][1], true, mx, my);
                }
                s.itemAt(g, s.stack(page.string("result")), px + CodexScreen.PAD + 48, py + 60, true, mx, my);
                if (page.string("title") != null) {
                    s.header(g, page.string("title"), px + CodexScreen.CX, py);
                }
                notes(s, g, page, px, py, color);
            }
            case "infuser" -> {
                s.blit(g, Identifier.fromNamespaceAndPath("theabyss", "textures/guide/infuser/infuser_overlay.png"), px + CodexScreen.PAD + 5, py + 72, 0, 0, 101, 26, 256, 256);
                s.itemAt(g, s.stack(page.string("somnium")), px + CodexScreen.PAD + 10, py + 17, true, mx, my);
                s.itemAt(g, s.stack(page.string("energy")), px + CodexScreen.PAD + 60, py + 17, true, mx, my);
                List<String> inputs = page.strings("inputs");
                for (int i = 0; i < 4 && i < inputs.size(); i++) {
                    s.itemAt(g, s.stack(inputs.get(i)), px + CodexScreen.PAD + 10 + i * 25, py + 77, false, mx, my);
                }
                if (page.string("title") != null) {
                    s.header(g, page.string("title"), px + CodexScreen.CX, py);
                }
                notes(s, g, page, px, py, color);
            }
            default -> s.textBlock(g, "Unknown page type: " + page.type(), px, py, CodexScreen.PAGE_W, 0xFFAA0000);
        }
    }

    private static void notes(CodexScreen s, GuiGraphicsExtractor g, CodexBook.Page page, int px, int py, int color) {
        List<String> notes = page.strings("notes");
        if (!notes.isEmpty()) {
            s.textBlock(g, notes.get(0), px + 1, py + 40, CodexScreen.PAGE_W - 1, color);
        }
        if (notes.size() > 1) {
            s.textBlock(g, notes.get(1), px + 1, py + 100, CodexScreen.PAGE_W - 1, color);
        }
    }

    private static int screenX(CodexScreen s, int x) {
        return (s.width - CodexScreen.W) / 2 + x;
    }

    private static int screenY(CodexScreen s, int y) {
        return (s.height - CodexScreen.H) / 2 + y;
    }
}
