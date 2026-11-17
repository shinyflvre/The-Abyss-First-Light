package net.yezon.theabyss.fabric.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.platform.client.event.RenderGuiEvent;
import net.yezon.theabyss.platform.client.event.RenderGuiLayerEvent;
import net.yezon.theabyss.platform.client.event.ScreenEvent;
import net.yezon.theabyss.platform.client.gui.GuiLayer;
import net.yezon.theabyss.platform.event.AbyssEventBus;

public final class FabricHudLayers {
    public static final float Z_SEPARATION = 200.0F;
    public static final ResourceLocation CAMERA_OVERLAYS = id("camera_overlays");
    public static final ResourceLocation CROSSHAIR = id("crosshair");
    public static final ResourceLocation HOTBAR = id("hotbar");
    public static final ResourceLocation JUMP_METER = id("jump_meter");
    public static final ResourceLocation EXPERIENCE_BAR = id("experience_bar");
    public static final ResourceLocation PLAYER_HEALTH = id("player_health");
    public static final ResourceLocation ARMOR_LEVEL = id("armor_level");
    public static final ResourceLocation FOOD_LEVEL = id("food_level");
    public static final ResourceLocation VEHICLE_HEALTH = id("vehicle_health");
    public static final ResourceLocation AIR_LEVEL = id("air_level");
    public static final ResourceLocation SELECTED_ITEM_NAME = id("selected_item_name");
    public static final ResourceLocation SPECTATOR_TOOLTIP = id("spectator_tooltip");
    public static final ResourceLocation EXPERIENCE_LEVEL = id("experience_level");
    public static final ResourceLocation EFFECTS = id("effects");
    public static final ResourceLocation BOSS_OVERLAY = id("boss_overlay");
    public static final ResourceLocation SLEEP_OVERLAY = id("sleep_overlay");
    public static final ResourceLocation DEMO_OVERLAY = id("demo_overlay");
    public static final ResourceLocation DEBUG_OVERLAY = id("debug_overlay");
    public static final ResourceLocation SCOREBOARD_SIDEBAR = id("scoreboard_sidebar");
    public static final ResourceLocation OVERLAY_MESSAGE = id("overlay_message");
    public static final ResourceLocation TITLE = id("title");
    public static final ResourceLocation CHAT = id("chat");
    public static final ResourceLocation TAB_LIST = id("tab_list");
    public static final ResourceLocation SUBTITLE_OVERLAY = id("subtitle_overlay");
    public static final ResourceLocation SAVING_INDICATOR = id("saving_indicator");

    private static final ResourceLocation[] HIDDEN_FIRST = {
        CAMERA_OVERLAYS, CROSSHAIR, HOTBAR, JUMP_METER, EXPERIENCE_BAR, PLAYER_HEALTH, ARMOR_LEVEL, FOOD_LEVEL, VEHICLE_HEALTH, AIR_LEVEL,
        SELECTED_ITEM_NAME, SPECTATOR_TOOLTIP, EXPERIENCE_LEVEL, EFFECTS, BOSS_OVERLAY
    };
    private static final ResourceLocation[] HIDDEN_SECOND = {
        DEMO_OVERLAY, DEBUG_OVERLAY, SCOREBOARD_SIDEBAR, OVERLAY_MESSAGE, TITLE, CHAT, TAB_LIST, SUBTITLE_OVERLAY, SAVING_INDICATOR
    };
    private static final Map<ResourceLocation, List<Named>> BEFORE = new HashMap<>();
    private static final Map<ResourceLocation, List<Named>> AFTER = new HashMap<>();

    private FabricHudLayers() {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    public static synchronized void below(ResourceLocation anchor, ResourceLocation id, GuiLayer layer) {
        BEFORE.computeIfAbsent(anchor, k -> new ArrayList<>()).add(new Named(id, layer));
    }

    public static synchronized void above(ResourceLocation anchor, ResourceLocation id, GuiLayer layer) {
        AFTER.computeIfAbsent(anchor, k -> new ArrayList<>()).add(0, new Named(id, layer));
    }

    public static DeltaTracker delta() {
        return Minecraft.getInstance().getTimer();
    }

    public static void vanilla(ResourceLocation layer, GuiGraphics graphics, DeltaTracker deltaTracker, Runnable draw) {
        before(layer, graphics, deltaTracker);
        if (!layerPre(graphics, deltaTracker, layer)) {
            draw.run();
            layerPost(graphics, deltaTracker, layer);
        }
        after(layer, graphics, deltaTracker);
    }

    public static void skipped(ResourceLocation layer, GuiGraphics graphics, DeltaTracker deltaTracker) {
        before(layer, graphics, deltaTracker);
        events(layer, graphics, deltaTracker);
        after(layer, graphics, deltaTracker);
    }

    public static void events(ResourceLocation layer, GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!layerPre(graphics, deltaTracker, layer)) {
            layerPost(graphics, deltaTracker, layer);
        }
    }

    public static void before(ResourceLocation layer, GuiGraphics graphics, DeltaTracker deltaTracker) {
        renderAll(BEFORE.get(layer), graphics, deltaTracker);
    }

    public static void after(ResourceLocation layer, GuiGraphics graphics, DeltaTracker deltaTracker) {
        renderAll(AFTER.get(layer), graphics, deltaTracker);
    }

    public static void hiddenFirstBlock(GuiGraphics graphics, DeltaTracker deltaTracker) {
        for (ResourceLocation layer : HIDDEN_FIRST) {
            skipped(layer, graphics, deltaTracker);
        }
    }

    public static void hiddenSecondBlock(GuiGraphics graphics, DeltaTracker deltaTracker) {
        for (ResourceLocation layer : HIDDEN_SECOND) {
            skipped(layer, graphics, deltaTracker);
        }
    }

    private static void renderAll(List<Named> layers, GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (layers == null) {
            return;
        }
        for (Named named : layers) {
            if (!layerPre(graphics, deltaTracker, named.id)) {
                named.layer.render(graphics, deltaTracker);
                layerPost(graphics, deltaTracker, named.id);
            }
            graphics.pose().translate(0.0F, 0.0F, Z_SEPARATION);
        }
    }

    public static boolean layerPre(GuiGraphics graphics, DeltaTracker deltaTracker, ResourceLocation layer) {
        return AbyssEventBus.hasListeners(RenderGuiLayerEvent.Pre.class)
            && AbyssEventBus.post(new RenderGuiLayerEvent.Pre(graphics, deltaTracker, layer)).isCanceled();
    }

    public static void layerPost(GuiGraphics graphics, DeltaTracker deltaTracker, ResourceLocation layer) {
        if (AbyssEventBus.hasListeners(RenderGuiLayerEvent.Post.class)) {
            AbyssEventBus.post(new RenderGuiLayerEvent.Post(graphics, deltaTracker, layer));
        }
    }

    public static boolean guiPre(GuiGraphics graphics, DeltaTracker deltaTracker) {
        return AbyssEventBus.hasListeners(RenderGuiEvent.Pre.class) && AbyssEventBus.post(new RenderGuiEvent.Pre(graphics, deltaTracker)).isCanceled();
    }

    public static void guiPost(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (AbyssEventBus.hasListeners(RenderGuiEvent.Post.class)) {
            AbyssEventBus.post(new RenderGuiEvent.Post(graphics, deltaTracker));
        }
    }

    public static void screenPost(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (AbyssEventBus.hasListeners(ScreenEvent.Render.Post.class)) {
            AbyssEventBus.post(new ScreenEvent.Render.Post(screen, graphics, mouseX, mouseY, partialTick));
        }
    }

    private record Named(ResourceLocation id, GuiLayer layer) {
    }
}
