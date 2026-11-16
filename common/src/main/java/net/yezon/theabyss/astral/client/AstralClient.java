package net.yezon.theabyss.astral.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.client.event.RenderPlayerEvent;
import net.yezon.theabyss.platform.client.event.ViewportEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.astral.AstralData;
import net.yezon.theabyss.client.fx.ScreenFx;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class AstralClient {
    private static final RandomSource RANDOM = RandomSource.create();
    private static int nextChime = 100;

    private AstralClient() {
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        AvatarRenderState state = event.getRenderState();
        if (mc.level.getEntity(state.id) instanceof Player player && player != mc.player && AstralData.isAstral(player)
            && !AstralData.isAstral(mc.player)) {
            event.setCanceled(true);
            state.shadowPieces.clear();
            state.displayFireAnimation = false;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        float strength = ScreenFx.astralStrength();
        if (strength <= 0.0F) {
            return;
        }
        float t = (Util.getMillis() % 600_000L) / 1000.0F;
        float red = 0.94F + 0.04F * Mth.sin(t * 0.21F);
        float green = 0.92F + 0.04F * Mth.sin(t * 0.17F + 2.0F);
        float blue = 0.98F + 0.02F * Mth.sin(t * 0.13F + 4.0F);
        event.setRed(Mth.lerp(strength, event.getRed(), red));
        event.setGreen(Mth.lerp(strength, event.getGreen(), green));
        event.setBlue(Mth.lerp(strength, event.getBlue(), blue));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.isPaused() || !AstralData.isAstral(player)) {
            return;
        }
        if (player.tickCount % 3 == 0) {
            double x = player.getX() + (RANDOM.nextDouble() - 0.5) * 6.0;
            double y = player.getY() + RANDOM.nextDouble() * 3.0;
            double z = player.getZ() + (RANDOM.nextDouble() - 0.5) * 6.0;
            mc.level.addParticle(ParticleTypes.WHITE_ASH, x, y, z, 0.0, 0.01, 0.0);
        }
        if (--nextChime <= 0) {
            nextChime = 120 + RANDOM.nextInt(160);
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 0.5F + RANDOM.nextFloat() * 0.4F, 0.35F));
        }
    }
}
