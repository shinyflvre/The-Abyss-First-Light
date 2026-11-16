package net.yezon.theabyss.client.fx;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.entity.NightbladeBladeEntity;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class ParryCue {
    private static final Identifier RING = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "textures/gui/parry_ring.png");
    private static final float LEAD = 1.0F;
    private static final int TARGET_SIZE = 22;
    private static final int START_SIZE = 110;
    private static final double BLADE_RANGE = 40.0;

    private record Cue(long impactNanos, float windowSeconds, boolean forced) {
    }

    private static final Map<Integer, Cue> CUES = new HashMap<>();

    private ParryCue() {
    }

    public static boolean enabled() {
        return ConfigConfiguration.BOSS_HELP.get();
    }

    public static void set(int sourceId, int impactIn, int window) {
        set(sourceId, impactIn, window, false);
    }

    public static void set(int sourceId, int impactIn, int window, boolean forced) {
        CUES.put(sourceId, new Cue(FxClock.nanos() + impactIn * 50_000_000L, window / 20.0F, forced));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        long now = FxClock.nanos();
        CUES.values().removeIf(cue -> now - cue.impactNanos > 250_000_000L);
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || !enabled()) {
            return;
        }
        Vec3 me = player.getBoundingBox().getCenter();
        for (NightbladeBladeEntity blade : mc.level.getEntitiesOfClass(NightbladeBladeEntity.class, player.getBoundingBox().inflate(BLADE_RANGE))) {
            Vec3 velocity = blade.clientVelocity();
            double speed2 = velocity.lengthSqr();
            if (speed2 < 1.0E-4) {
                continue;
            }
            Vec3 toMe = me.subtract(blade.position());
            double ticks = toMe.dot(velocity) / speed2;
            double miss = toMe.subtract(velocity.scale(ticks)).length();
            if (ticks > 0.0 && ticks < 40.0 && miss < 1.8) {
                set(-1 - blade.getId(), (int)Math.round(ticks), NightbladeBladeEntity.PARRY_WINDOW);
            }
        }
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (CUES.isEmpty() || mc.player == null || mc.gui.hud.isHidden()) {
            return;
        }
        long now = FxClock.nanos();
        boolean help = enabled();
        Cue next = null;
        for (Cue cue : CUES.values()) {
            if ((help || cue.forced) && cue.impactNanos - now > -150_000_000L && (next == null || cue.impactNanos < next.impactNanos)) {
                next = cue;
            }
        }
        if (next == null) {
            return;
        }
        float remaining = (next.impactNanos - now) / 1.0E9F;
        if (remaining > LEAD) {
            return;
        }
        int cx = graphics.guiWidth() / 2;
        int cy = graphics.guiHeight() / 2;
        boolean now_ = remaining <= next.windowSeconds;
        float fadeIn = Mth.clamp((LEAD - remaining) / 0.2F, 0.0F, 1.0F);
        float fadeOut = remaining < 0.0F ? Mth.clamp(1.0F + remaining / 0.15F, 0.0F, 1.0F) : 1.0F;
        float alpha = fadeIn * fadeOut;
        int targetColor = now_ ? ARGB.colorFromFloat(alpha, 0.35F, 0.9F, 1.0F) : ARGB.colorFromFloat(alpha * 0.55F, 1.0F, 1.0F, 1.0F);
        ring(graphics, cx, cy, TARGET_SIZE, targetColor);
        float t = Mth.clamp(Math.max(remaining, 0.0F) / LEAD, 0.0F, 1.0F);
        int size = Math.round(TARGET_SIZE + (START_SIZE - TARGET_SIZE) * t);
        int ringColor = now_ ? ARGB.colorFromFloat(alpha, 0.35F, 0.9F, 1.0F) : ARGB.colorFromFloat(alpha * 0.85F, 1.0F, 1.0F, 1.0F);
        ring(graphics, cx, cy, size, ringColor);
        if (now_) {
            ring(graphics, cx, cy, size + 4, ARGB.colorFromFloat(alpha * 0.4F, 0.35F, 0.9F, 1.0F));
        }
    }

    private static void ring(GuiGraphicsExtractor graphics, int cx, int cy, int size, int color) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, RING, cx - size / 2, cy - size / 2, 0.0F, 0.0F, size, size, size, size, color);
    }
}
