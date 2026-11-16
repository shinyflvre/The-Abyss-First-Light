package net.yezon.theabyss.client.codex;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.yezon.theabyss.TheabyssMod;

public final class CodexBook {
    public record Category(String id, String name, String description, String icon, int order, List<Entry> entries) {
    }

    public record Entry(String id, String name, String icon, String category, int order, List<Page> pages, int chronicle) {
    }

    public record Page(String type, JsonObject json) {
        public String string(String key) {
            JsonElement e = this.json.get(key);
            return e == null || e.isJsonNull() ? null : e.getAsString();
        }

        public List<String> strings(String key) {
            List<String> out = new ArrayList<>();
            JsonElement e = this.json.get(key);
            if (e != null && e.isJsonArray()) {
                for (JsonElement x : e.getAsJsonArray()) {
                    out.add(x.getAsString());
                }
            }
            return out;
        }
    }

    public final String title;
    public final String landingText;
    public final Identifier texture;
    public final Identifier background;
    public final Identifier filler;
    public final int textColor;
    public final int headerColor;
    public final int linkColor;
    public final int nameplateColor;
    public final String edition;
    public final String font;
    public final List<Category> categories;

    private CodexBook(JsonObject book, List<Category> categories) {
        this.title = translated(book, "title", "The Abyss");
        this.landingText = translated(book, "landing_text", "");
        this.texture = Identifier.parse(text(book, "texture", "theabyss:textures/gui/guide_book.png"));
        this.background = Identifier.parse(text(book, "background", "theabyss:textures/gui/codex_book.png"));
        this.filler = Identifier.parse(text(book, "filler", "theabyss:textures/gui/page_filler.png"));
        this.textColor = 0xFF000000 | Integer.parseInt(text(book, "text_color", "000000"), 16);
        this.headerColor = 0xFF000000 | Integer.parseInt(text(book, "header_color", "333333"), 16);
        this.linkColor = 0xFF000000 | Integer.parseInt(text(book, "link_color", "00AAAA"), 16);
        this.nameplateColor = 0xFF000000 | Integer.parseInt(text(book, "nameplate_color", "FFAA00"), 16);
        this.edition = translated(book, "edition", "");
        this.font = text(book, "font", "default");
        this.categories = categories;
    }

    private static String text(JsonObject o, String key, String fallback) {
        return o.has(key) ? o.get(key).getAsString() : fallback;
    }

    private static String translated(JsonObject o, String key, String fallback) {
        return net.yezon.theabyss.util.AbyssTooltip.res(text(o, key, fallback));
    }

    private static final String CHRONICLES = "chronicles";

    public static CodexBook load() {
        ResourceManager resources = Minecraft.getInstance().getResourceManager();
        JsonObject book = read(resources, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "codex/book.json"));
        String lang = Minecraft.getInstance().getLanguageManager().getSelected();
        Map<String, JsonObject> categoryJson = files(resources, "codex/" + lang + "/categories");
        Map<String, JsonObject> entryJson = files(resources, "codex/" + lang + "/entries");
        if (categoryJson.isEmpty()) {
            categoryJson = files(resources, "codex/en_us/categories");
            entryJson = files(resources, "codex/en_us/entries");
        }
        boolean lores = net.yezon.theabyss.configuration.ConfigConfiguration.loresEnabled();
        Map<String, List<Entry>> byCategory = new LinkedHashMap<>();
        for (Map.Entry<String, JsonObject> e : entryJson.entrySet()) {
            JsonObject o = e.getValue();
            if (!lores && (o.has("chronicle") || CHRONICLES.equals(o.get("category").getAsString()))) {
                continue;
            }
            List<Page> pages = new ArrayList<>();
            JsonArray array = o.getAsJsonArray("pages");
            if (array != null) {
                for (JsonElement p : array) {
                    pages.add(new Page(p.getAsJsonObject().get("type").getAsString(), p.getAsJsonObject()));
                }
            }
            String category = o.get("category").getAsString();
            byCategory.computeIfAbsent(category, k -> new ArrayList<>())
                .add(new Entry(e.getKey(), o.get("name").getAsString(), text(o, "icon", "minecraft:book"), category,
                    o.has("order") ? o.get("order").getAsInt() : 0, pages, o.has("chronicle") ? o.get("chronicle").getAsInt() : 0));
        }
        List<Category> categories = new ArrayList<>();
        for (Map.Entry<String, JsonObject> c : categoryJson.entrySet()) {
            JsonObject o = c.getValue();
            if (!lores && CHRONICLES.equals(c.getKey())) {
                continue;
            }
            List<Entry> entries = byCategory.getOrDefault(c.getKey(), new ArrayList<>());
            entries.sort(Comparator.comparingInt(Entry::order).thenComparing(Entry::name));
            categories.add(new Category(c.getKey(), o.get("name").getAsString(), text(o, "description", ""), text(o, "icon", "minecraft:book"),
                o.has("order") ? o.get("order").getAsInt() : 0, entries));
        }
        categories.sort(Comparator.comparingInt(Category::order).thenComparing(Category::name));
        return new CodexBook(book, categories);
    }

    private static Map<String, JsonObject> files(ResourceManager resources, String folder) {
        Map<String, JsonObject> out = new LinkedHashMap<>();
        for (Map.Entry<Identifier, Resource> e : resources.listResources(folder, id -> id.getPath().endsWith(".json")).entrySet()) {
            if (!TheabyssMod.MODID.equals(e.getKey().getNamespace())) {
                continue;
            }
            String path = e.getKey().getPath();
            String id = path.substring(path.lastIndexOf('/') + 1, path.length() - ".json".length());
            try (Reader reader = e.getValue().openAsReader()) {
                out.put(id, JsonParser.parseReader(reader).getAsJsonObject());
            } catch (Exception ex) {
                TheabyssMod.LOGGER.warn("Abyss Codex: cannot read {}", e.getKey(), ex);
            }
        }
        return out;
    }

    private static JsonObject read(ResourceManager resources, Identifier id) {
        try (Reader reader = resources.getResourceOrThrow(id).openAsReader()) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception ex) {
            TheabyssMod.LOGGER.warn("Abyss Codex: cannot read {}", id, ex);
            return new JsonObject();
        }
    }
}
