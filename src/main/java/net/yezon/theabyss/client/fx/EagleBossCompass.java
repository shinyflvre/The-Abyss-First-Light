package net.yezon.theabyss.client.fx;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.EagleBossesMessage;

public final class EagleBossCompass {
    private static final int HALF = 110;
    private static final int TOP = 32;
    private static final int PINK = 0xFF6FE0;
    private static final int NIGHTBLADE = 0x4FF0C8;

    private static List<EagleBossesMessage.Marker> markers = List.of();
    private static long untilNanos;

    private EagleBossCompass() {
    }

    public static void set(List<EagleBossesMessage.Marker> found, int ticks) {
        markers = new ArrayList<>(found);
        untilNanos = FxClock.nanos() + ticks * 50_000_000L;
    }

    public static int shown() {
        return FxClock.nanos() < untilNanos ? markers.size() : 0;
    }

    public static void render(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        long left = untilNanos - FxClock.nanos();
        if (player == null || markers.isEmpty() || left <= 0 || mc.gui.hud.isHidden()) {
            return;
        }
        float fade = Mth.clamp(left / 1_500_000_000.0F, 0.0F, 1.0F);
        int alpha = (int)(255 * fade);
        if (alpha < 8) {
            return;
        }
        Font font = mc.font;
        int cx = g.guiWidth() / 2;
        float partial = deltaTracker.getGameTimeDeltaPartialTick(false);
        Vec3 eye = player.getEyePosition(partial);
        float yaw = player.getViewYRot(partial);
        g.fill(cx - HALF, TOP, cx + HALF, TOP + 1, ARGB.color(alpha / 2, 255, 255, 255));
        for (int i = -2; i <= 2; i++) {
            int x = cx + i * HALF / 2;
            int h = i == 0 ? 4 : 2;
            g.fill(x, TOP - h, x + 1, TOP + h + 1, ARGB.color(i == 0 ? alpha : alpha / 2, 255, 255, 255));
        }
        List<EagleBossesMessage.Marker> sorted = new ArrayList<>(markers);
        sorted.sort(Comparator.comparingDouble(m -> eye.distanceToSqr(m.x(), m.y(), m.z())));
        boolean leftArrow = false;
        boolean rightArrow = false;
        List<int[]> labels = new ArrayList<>();
        String[] texts = new String[sorted.size()];
        int[] xs = new int[sorted.size()];
        for (int i = sorted.size() - 1; i >= 0; i--) {
            EagleBossesMessage.Marker m = sorted.get(i);
            double dx = m.x() - eye.x;
            double dz = m.z() - eye.z;
            double dy = m.y() - eye.y;
            float direction = (float)Math.toDegrees(Math.atan2(-dx, dz));
            float diff = Mth.wrapDegrees(direction - yaw);
            int distance = (int)Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz));
            int rgb = isNightblade(m) ? NIGHTBLADE : PINK;
            int color = ARGB.color(alpha, rgb);
            String height = dy > 6.0 ? "▲" : dy < -6.0 ? "▼" : "";
            if (Math.abs(diff) > 90.0F) {
                boolean onLeft = diff < 0.0F;
                if (onLeft ? leftArrow : rightArrow) {
                    continue;
                }
                int ax = onLeft ? cx - HALF - 10 : cx + HALF + 4;
                g.text(font, onLeft ? "◀" : "▶", ax, TOP - 4, color, true);
                String label = distance + "m" + height;
                g.text(font, label, onLeft ? ax - font.width(label) - 2 : ax + 9, TOP - 4, color, true);
                if (onLeft) {
                    leftArrow = true;
                } else {
                    rightArrow = true;
                }
                continue;
            }
            int x = cx + Math.round(diff / 90.0F * HALF);
            g.centeredText(font, "◆", x, TOP - 4, color);
            xs[i] = x;
            texts[i] = i == 0 || Math.abs(diff) < 12.0F ? name(m) + " " + distance + "m" + (height.isEmpty() ? "" : " " + height) : height;
        }
        for (int i = 0; i < sorted.size(); i++) {
            String label = texts[i];
            if (label == null || label.isEmpty()) {
                continue;
            }
            int w = font.width(label);
            int x0 = xs[i] - w / 2 - 2;
            int x1 = xs[i] + w / 2 + 2;
            boolean free = labels.stream().noneMatch(r -> x0 < r[1] && x1 > r[0]);
            if (!free) {
                continue;
            }
            labels.add(new int[] {x0, x1});
            int rgb = isNightblade(sorted.get(i)) ? NIGHTBLADE : PINK;
            g.centeredText(font, label, xs[i], TOP + 6, ARGB.color(alpha, rgb));
        }
    }

    private static boolean isNightblade(EagleBossesMessage.Marker m) {
        return m.type().endsWith("night_blade_boss") || m.type().endsWith("nightblade_boss");
    }

    private static String name(EagleBossesMessage.Marker m) {
        Identifier id = Identifier.tryParse(m.type());
        EntityType<?> type = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getValue(id);
        return type == null ? m.type() : type.getDescription().getString();
    }
}
