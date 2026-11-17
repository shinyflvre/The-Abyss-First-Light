package net.yezon.theabyss.client.fx;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.yezon.theabyss.compat.ARGB;
import net.yezon.theabyss.compat.GuiGraphicsCompat;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.fx.spell.NightMarkFx;
import net.yezon.theabyss.client.fx.spell.SpellFx;
import net.yezon.theabyss.init.TheabyssModSounds;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class NightGraceClient {
    private static final int SLOW_BEAT = 24;
    private static final int FAST_BEAT = 6;
    private static final int STRIKE_LEAD = 16;
    private static final int STRIKE_FADE = 12;
    private static final int FADE_IN = 5;
    private static final ResourceLocation EDGE_GLOW = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "textures/gui/night/edge_glow.png");
    private static final int GLOW_ALONG = 110;
    private static final int GLOW_ACROSS = 44;
    private static final int GLOW_DARK = 0x8A45FF;
    private static final int GLOW_LIGHT = 0xC9A2FF;
    private static final RandomSource RANDOM = RandomSource.create();
    private static final Map<Integer, Grace> GRACES = new ConcurrentHashMap<>();
    private static float pulse;

    private static final class Grace {
        final long start;
        final long until;
        long nextBeat;
        boolean striking;
        double x;
        double z;

        Grace(long start, long until) {
            this.start = start;
            this.until = until;
            this.nextBeat = start + SLOW_BEAT / 2;
        }

        float progress(float now) {
            return Mth.clamp((now - this.start) / Math.max(1L, this.until - this.start), 0.0F, 1.0F);
        }
    }

    private NightGraceClient() {
    }

    public static void start(int entityId, int ticks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        long now = mc.level.getGameTime();
        Grace grace = new Grace(now, now + ticks);
        Entity mob = mc.level.getEntity(entityId);
        if (mob != null) {
            grace.x = mob.getX();
            grace.z = mob.getZ();
            play(mob, TheabyssModSounds.NIGHT_NOTICE.get(), 1.0F, 0.92F + RANDOM.nextFloat() * 0.12F);
        }
        GRACES.put(entityId, grace);
        SpellFx.add(new NightMarkFx(entityId, ticks));
    }

    public static float pulse() {
        return pulse;
    }

    private static void play(Entity mob, SoundEvent sound, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(sound, SoundSource.HOSTILE, volume, pitch, RANDOM, mob.getX(), mob.getEyeY(), mob.getZ()));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        pulse *= 0.8F;
        if (mc.level == null || mc.player == null) {
            GRACES.clear();
            return;
        }
        if (mc.isPaused()) {
            return;
        }
        long now = mc.level.getGameTime();
        Iterator<Map.Entry<Integer, Grace>> it = GRACES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Grace> entry = it.next();
            Grace grace = entry.getValue();
            Entity mob = mc.level.getEntity(entry.getKey());
            if (now >= grace.until + STRIKE_FADE || (mob == null || !mob.isAlive()) && now < grace.until) {
                it.remove();
                continue;
            }
            if (mob == null || now >= grace.until) {
                continue;
            }
            grace.x = mob.getX();
            grace.z = mob.getZ();
            if (!grace.striking && now >= grace.until - STRIKE_LEAD) {
                grace.striking = true;
                play(mob, TheabyssModSounds.NIGHT_STRIKE.get(), 1.0F, 0.95F + RANDOM.nextFloat() * 0.1F);
            }
            float progress = grace.progress(now);
            if (now >= grace.nextBeat) {
                play(mob, SoundEvents.WARDEN_HEARTBEAT, 0.55F + 0.45F * progress, 0.8F + 0.2F * progress);
                grace.nextBeat = now + Math.round(Mth.lerp(progress, SLOW_BEAT, FAST_BEAT));
                pulse = 1.0F;
            }
        }
    }

    public static void render(GuiGraphics g, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || GRACES.isEmpty() || mc.options.hideGui) {
            return;
        }
        float partial = deltaTracker.getGameTimeDeltaPartialTick(false);
        float now = mc.level.getGameTime() + partial;
        Vec3 eye = player.getEyePosition(partial);
        float yaw = player.getViewYRot(partial);
        float halfW = g.guiWidth() * 0.5F;
        float halfH = g.guiHeight() * 0.5F;
        for (Map.Entry<Integer, Grace> entry : GRACES.entrySet()) {
            Grace grace = entry.getValue();
            Entity mob = mc.level.getEntity(entry.getKey());
            double mx = mob != null ? Mth.lerp(partial, mob.xo, mob.getX()) : grace.x;
            double mz = mob != null ? Mth.lerp(partial, mob.zo, mob.getZ()) : grace.z;
            float direction = (float)Math.toDegrees(Math.atan2(-(mx - eye.x), mz - eye.z));
            float relative = Mth.wrapDegrees(direction - yaw) * Mth.DEG_TO_RAD;
            float alpha;
            float size;
            float heat;
            if (now < grace.until) {
                float progress = grace.progress(now);
                float in = Mth.clamp((now - grace.start) / FADE_IN, 0.0F, 1.0F);
                alpha = in * (0.45F + 0.3F * progress + 0.25F * pulse);
                size = 0.75F + 0.15F * in + 0.15F * progress + 0.08F * pulse;
                heat = Mth.clamp(0.3F * progress + 0.7F * pulse, 0.0F, 1.0F);
            } else {
                float k = Mth.clamp((now - grace.until) / STRIKE_FADE, 0.0F, 1.0F);
                alpha = 1.0F - k * k;
                size = 1.05F + 0.2F * k;
                heat = 1.0F - k;
            }
            if (alpha <= 0.01F) {
                continue;
            }
            float dx = Mth.sin(relative);
            float dy = -Mth.cos(relative);
            float reachX = Math.abs(dx) > 1.0E-4F ? halfW / Math.abs(dx) : Float.MAX_VALUE;
            float reachY = Math.abs(dy) > 1.0E-4F ? halfH / Math.abs(dy) : Float.MAX_VALUE;
            float reach = Math.min(reachX, reachY);
            float ex = halfW + dx * reach;
            float ey = halfH + dy * reach;
            int along = Math.round(GLOW_ALONG * size);
            int across = Math.round(GLOW_ACROSS * size);
            int w = reachY <= reachX ? along : across;
            int h = reachY <= reachX ? across : along;
            int colour = ARGB.color(Math.round(170 * alpha), ARGB.srgbLerp(heat, GLOW_DARK, GLOW_LIGHT));
            GuiGraphicsCompat.blit(g, EDGE_GLOW, Math.round(ex - w * 0.5F), Math.round(ey - h * 0.5F), 0.0F, 0.0F, w, h, w, h, colour);
        }
    }
}
