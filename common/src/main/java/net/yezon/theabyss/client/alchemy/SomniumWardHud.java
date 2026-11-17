package net.yezon.theabyss.client.alchemy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.RenderGuiLayerEvent;
import net.yezon.theabyss.platform.client.gui.VanillaGuiLayers;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.compat.GuiGraphicsCompat;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.network.TheabyssModVariables;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class SomniumWardHud {
    private static final ResourceLocation FULL = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "hud/ward_full");
    private static final ResourceLocation HALF = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "hud/ward_half");
    private static final int ROW = 10;
    private static int baseY = -1;

    private SomniumWardHud() {
    }

    @SubscribeEvent
    public static void onPre(RenderGuiLayerEvent.Pre event) {
        if (VanillaGuiLayers.PLAYER_HEALTH.equals(event.getName())) {
            baseY = event.getGuiGraphics().guiHeight() - net.yezon.theabyss.platform.client.gui.HudHeights.left();
        }
    }

    @SubscribeEvent
    public static void onPost(RenderGuiLayerEvent.Post event) {
        if (!VanillaGuiLayers.PLAYER_HEALTH.equals(event.getName()) || baseY < 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.getCameraEntity() instanceof Player player) || !player.hasEffect(TheabyssModMobEffects.SOMNIUM_WARD.getDelegate())) {
            return;
        }
        int wardHalves = Mth.ceil(TheabyssModVariables.getPlayerVariables(player).SomniumWard);
        if (wardHalves <= 0) {
            return;
        }
        Gui hud = mc.gui;
        GuiGraphics graphics = event.getGuiGraphics();
        int currentHealth = Mth.ceil(player.getHealth());
        float maxHealth = Math.max((float)player.getAttributeValue(Attributes.MAX_HEALTH), currentHealth);
        int absorption = Mth.ceil(player.getAbsorptionAmount());
        int healthContainers = Mth.ceil(maxHealth / 2.0F);
        int total = healthContainers + Mth.ceil(absorption / 2.0F);
        int tick = hud.getGuiTicks();
        int regenIndex = player.hasEffect(MobEffects.REGENERATION) ? tick % Mth.ceil(maxHealth + 5.0F) : -1;
        int seed = tick * 312871;
        RandomSource random = RandomSource.create(seed);
        int[] rowY = new int[ROW];
        for (int i = total - 1; i >= 0; i--) {
            int y = baseY;
            if (currentHealth + absorption <= 4) {
                y += random.nextInt(2);
            }
            if (i < healthContainers && i == regenIndex) {
                y -= 2;
            }
            if (i < ROW) {
                rowY[i] = y;
            }
        }
        int xLeft = graphics.guiWidth() / 2 - 91;
        int hearts = Math.min(ROW, Math.min(healthContainers, (wardHalves + 1) / 2));
        for (int i = 0; i < hearts; i++) {
            boolean half = i * 2 + 1 == wardHalves;
            GuiGraphicsCompat.blitSprite(graphics, half ? HALF : FULL, xLeft + i * 8, rowY[i], 9, 9);
        }
    }
}
