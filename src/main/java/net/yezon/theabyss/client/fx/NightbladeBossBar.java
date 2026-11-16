package net.yezon.theabyss.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModEntities;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class NightbladeBossBar {
    private static final int WIDTH = 182;
    private static final int HEIGHT = 5;
    private static final float[] PHASES = {0.66F, 0.33F};

    private NightbladeBossBar() {
    }

    @SubscribeEvent
    public static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        LerpingBossEvent boss = event.getBossEvent();
        if (!isNightblade(boss.getName())) {
            return;
        }
        event.setCanceled(true);
        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        int x = event.getX();
        int y = event.getY();
        bar(graphics, boss, x, y, WIDTH, "_background");
        int filled = Mth.lerpDiscrete(boss.getProgress(), 0, WIDTH);
        if (filled > 0) {
            bar(graphics, boss, x, y, filled, "_progress");
        }
        for (float phase : PHASES) {
            int at = x + Math.round(WIDTH * phase);
            graphics.fill(at - 1, y - 1, at + 1, y + HEIGHT + 1, 0xFF0E0B14);
            graphics.fill(at + 1, y, at + 2, y + HEIGHT, 0x66FFFFFF);
        }
        Component name = boss.getName();
        graphics.text(mc.font, name, graphics.guiWidth() / 2 - mc.font.width(name) / 2, y - 9, -1);
    }

    private static void bar(GuiGraphicsExtractor graphics, BossEvent boss, int x, int y, int width, String part) {
        Identifier sprite = Identifier.withDefaultNamespace("boss_bar/" + boss.getColor().getName() + part);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, WIDTH, HEIGHT, 0, 0, x, y, width, HEIGHT);
        if (boss.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
            Identifier overlay = Identifier.withDefaultNamespace("boss_bar/notched_" + boss.getOverlay().getName().replace("notched_", "") + part);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, overlay, WIDTH, HEIGHT, 0, 0, x, y, width, HEIGHT);
        }
    }

    private static boolean isNightblade(Component name) {
        return name.getContents() instanceof TranslatableContents translatable
            && translatable.getKey().equals(TheabyssModEntities.NIGHTBLADE_BOSS.get().getDescriptionId());
    }
}
