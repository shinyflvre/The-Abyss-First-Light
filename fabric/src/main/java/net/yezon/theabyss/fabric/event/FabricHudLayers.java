package net.yezon.theabyss.fabric.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.platform.client.gui.GuiLayer;

public final class FabricHudLayers {
    public static final Identifier CAMERA_OVERLAYS = id("camera_overlays");
    public static final Identifier CROSSHAIR = id("crosshair");
    public static final Identifier AFTER_CAMERA_DECORATIONS = id("after_camera_decorations");
    public static final Identifier HOTBAR = id("hotbar");
    public static final Identifier PLAYER_HEALTH = id("player_health");
    public static final Identifier ARMOR_LEVEL = id("armor_level");
    public static final Identifier FOOD_LEVEL = id("food_level");
    public static final Identifier VEHICLE_HEALTH = id("vehicle_health");
    public static final Identifier AIR_LEVEL = id("air_level");
    public static final Identifier CONTEXTUAL_INFO_BAR_BACKGROUND = id("contextual_info_bar_background");
    public static final Identifier EXPERIENCE_LEVEL = id("experience_level");
    public static final Identifier CONTEXTUAL_INFO_BAR = id("contextual_info_bar");
    public static final Identifier SELECTED_ITEM_NAME = id("selected_item_name");
    public static final Identifier SPECTATOR_TOOLTIP = id("spectator_tooltip");
    public static final Identifier EFFECTS = id("effects");
    public static final Identifier BOSS_OVERLAY = id("boss_overlay");
    public static final Identifier SLEEP_OVERLAY = id("sleep_overlay");
    public static final Identifier DEMO_OVERLAY = id("demo_overlay");
    public static final Identifier SCOREBOARD_SIDEBAR = id("scoreboard_sidebar");
    public static final Identifier OVERLAY_MESSAGE = id("overlay_message");
    public static final Identifier TITLE = id("title");
    public static final Identifier CHAT = id("chat");
    public static final Identifier TAB_LIST = id("tab_list");
    public static final Identifier SUBTITLE_OVERLAY = id("subtitle_overlay");

    private static final Identifier[] HIDDEN_FIRST = {
        CAMERA_OVERLAYS, CROSSHAIR, AFTER_CAMERA_DECORATIONS, HOTBAR, PLAYER_HEALTH, ARMOR_LEVEL, FOOD_LEVEL, VEHICLE_HEALTH, AIR_LEVEL,
        CONTEXTUAL_INFO_BAR_BACKGROUND, EXPERIENCE_LEVEL, CONTEXTUAL_INFO_BAR, SELECTED_ITEM_NAME, SPECTATOR_TOOLTIP, EFFECTS, BOSS_OVERLAY
    };
    private static final Identifier[] HIDDEN_SECOND = {DEMO_OVERLAY, SCOREBOARD_SIDEBAR, OVERLAY_MESSAGE, TITLE, CHAT, TAB_LIST};
    private static final Map<Identifier, List<Named>> BEFORE = new HashMap<>();
    private static final Map<Identifier, List<Named>> AFTER = new HashMap<>();

    private FabricHudLayers() {
    }

    private static Identifier id(String path) {
        return Identifier.withDefaultNamespace(path);
    }

    public static synchronized void below(Identifier anchor, Identifier id, GuiLayer layer) {
        BEFORE.computeIfAbsent(anchor, k -> new ArrayList<>()).add(new Named(id, layer));
    }

    public static synchronized void above(Identifier anchor, Identifier id, GuiLayer layer) {
        AFTER.computeIfAbsent(anchor, k -> new ArrayList<>()).add(0, new Named(id, layer));
    }

    public static void render(Identifier id, GuiLayer layer, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!ClientFabricEvents.guiLayerPre(graphics, id)) {
            layer.render(graphics, deltaTracker);
            ClientFabricEvents.guiLayerPost(graphics, id);
        }
    }

    public static void hiddenFirstBlock(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        for (Identifier vanilla : HIDDEN_FIRST) {
            renderAll(BEFORE.get(vanilla), graphics, deltaTracker);
            ClientFabricEvents.guiLayerSkipped(graphics, vanilla);
            renderAll(AFTER.get(vanilla), graphics, deltaTracker);
        }
    }

    public static void hiddenSecondBlock(GuiGraphicsExtractor graphics) {
        ClientFabricEvents.guiLayerSkipped(graphics, HIDDEN_SECOND);
    }

    private static void renderAll(List<Named> layers, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (layers == null) {
            return;
        }
        for (Named named : layers) {
            render(named.id, named.layer, graphics, deltaTracker);
        }
    }

    private record Named(Identifier id, GuiLayer layer) {
    }
}
