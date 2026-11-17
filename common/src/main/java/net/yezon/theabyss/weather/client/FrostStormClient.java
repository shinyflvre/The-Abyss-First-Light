package net.yezon.theabyss.weather.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.yezon.theabyss.compat.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.client.event.RegisterGuiLayersEvent;
import net.yezon.theabyss.platform.client.event.RenderFrameEvent;
import net.yezon.theabyss.platform.client.event.ViewportEvent;
import net.yezon.theabyss.platform.client.gui.VanillaGuiLayers;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.compat.GuiGraphicsCompat;
import net.yezon.theabyss.client.fx.CameraShake;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.ring.fx.GlowParticleOptions;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class FrostStormClient {
    private static final ResourceKey<Level> FROST_WORLD = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "frost_world"));
    private static final ResourceLocation FROST_OVERLAY = ResourceLocation.withDefaultNamespace("textures/misc/powder_snow_outline.png");
    private static final float FOG_CLEAR = 128.0F;
    private static final float FOG_GUST = 46.0F;
    private static final float FROST_MAX = 0.8F;
    private static final float FROST_IN = 1.0F / 1800.0F;
    private static final float FROST_OUT = 1.0F / 400.0F;
    private static final float FROST_WARM = 1.0F / 80.0F;
    private static final int FLAKES_PER_TICK = 70;
    private static final RandomSource RANDOM = RandomSource.create();

    private static boolean inStorm;
    private static float gust = 0.5F;
    private static float gustTarget = 0.5F;
    private static int gustTimer;
    private static float windAngle = RANDOM.nextFloat() * Mth.TWO_PI;
    private static float exposure;
    private static float frost;
    private static boolean warm;
    private static int warmCheck;
    private static int nextBreath = 60;
    private static int nextShiver = 100;
    private static WindSound wind;

    private FrostStormClient() {
    }

    public static float frost() {
        return frost;
    }

    public static float gust() {
        return gust;
    }

    private static boolean storming(Minecraft mc) {
        return mc.level != null && mc.player != null && mc.level.dimension() == FROST_WORLD;
    }

    @SubscribeEvent
    public static void onTickPre(ClientTickEvent.Pre event) {
        forceSnow(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void onFrame(RenderFrameEvent.Pre event) {
        forceSnow(Minecraft.getInstance());
    }

    private static void forceSnow(Minecraft mc) {
        if (storming(mc)) {
            mc.level.setRainLevel(1.0F);
            mc.level.setThunderLevel(0.0F);
        }
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        forceSnow(mc);
        inStorm = storming(mc);
        if (!inStorm) {
            exposure = Math.max(0.0F, exposure - 0.05F);
            frost = Math.max(0.0F, frost - 1.0F / 40.0F);
            if (wind != null && exposure <= 0.0F) {
                wind.end();
                wind = null;
            }
            return;
        }
        if (mc.isPaused()) {
            return;
        }
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        Vec3 eye = mc.gameRenderer.getMainCamera().getPosition();
        BlockPos head = BlockPos.containing(eye);
        boolean open = level.canSeeSky(head);
        exposure = Mth.clamp(exposure + (open ? 0.03F : -0.03F), 0.0F, 1.0F);
        updateGust();
        if (--warmCheck <= 0) {
            warmCheck = 20;
            warm = nearHeat(level, player.blockPosition());
        }
        float rate = warm ? -FROST_WARM : open && !player.isCreative() && !player.isSpectator() ? FROST_IN * (0.7F + 0.6F * gust) : -FROST_OUT;
        frost = Mth.clamp(frost + rate, 0.0F, FROST_MAX);
        flakes(mc, level, eye);
        breath(mc, player);
        if (frost > 0.45F && --nextShiver <= 0) {
            nextShiver = 80 + RANDOM.nextInt(80);
            CameraShake.add(0.25F + 0.35F * frost, 0.35F);
        }
        if (wind == null || wind.isStopped()) {
            wind = new WindSound();
            mc.getSoundManager().play(wind);
        }
    }

    private static void updateGust() {
        if (--gustTimer <= 0) {
            gustTimer = 60 + RANDOM.nextInt(160);
            gustTarget = RANDOM.nextFloat() < 0.22F ? 0.85F + RANDOM.nextFloat() * 0.15F : 0.25F + RANDOM.nextFloat() * 0.45F;
        }
        gust += (gustTarget - gust) * 0.02F;
        windAngle += (RANDOM.nextFloat() - 0.5F) * 0.012F;
    }

    private static boolean nearHeat(ClientLevel level, BlockPos center) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockState state = level.getBlockState(pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz));
                    if (state.is(BlockTags.FIRE) || state.is(Blocks.LAVA) || state.is(Blocks.MAGMA_BLOCK)
                        || state.is(BlockTags.CAMPFIRES) && state.getValue(net.minecraft.world.level.block.CampfireBlock.LIT)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void flakes(Minecraft mc, ClientLevel level, Vec3 eye) {
        ParticleStatus status = mc.options.particles().get();
        float budget = status == ParticleStatus.ALL ? 1.0F : status == ParticleStatus.DECREASED ? 0.5F : 0.2F;
        int count = Math.round(FLAKES_PER_TICK * budget * (0.5F + 0.7F * gust));
        double speed = 0.3 + 0.55 * gust;
        double wx = Mth.cos(windAngle) * speed;
        double wz = Mth.sin(windAngle) * speed;
        for (int i = 0; i < count; i++) {
            boolean drift = RANDOM.nextFloat() < 0.25F;
            double x = eye.x + (RANDOM.nextDouble() - 0.5) * 44.0 - wx * 22.0;
            double z = eye.z + (RANDOM.nextDouble() - 0.5) * 44.0 - wz * 22.0;
            int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
            double y = drift ? ground + 0.1 + RANDOM.nextDouble() * 0.7 : eye.y - 4.0 + RANDOM.nextDouble() * 18.0;
            if (y < ground || Math.abs(y - eye.y) > 24.0) {
                continue;
            }
            double boost = drift ? 1.6 : 0.85 + RANDOM.nextDouble() * 0.3;
            double fall = drift ? -0.005 - RANDOM.nextDouble() * 0.01 : -0.06 - RANDOM.nextDouble() * 0.08;
            level.addParticle(TheabyssModParticleTypes.BLIZZARD_FLAKE.get(), x, y, z, wx * boost, fall, wz * boost);
        }
    }

    private static void breath(Minecraft mc, LocalPlayer player) {
        if (--nextBreath > 0 || player.isSpectator()) {
            return;
        }
        nextBreath = 55 + RANDOM.nextInt(40);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 mouth = player.getEyePosition().add(look.scale(0.7)).add(0.0, -0.2, 0.0);
        for (int i = 0; i < 4; i++) {
            mc.level.addParticle(GlowParticleOptions.of(0x343A44, 0.025F + RANDOM.nextFloat() * 0.02F, 16 + RANDOM.nextInt(8)).drag(0.9F)
                    .flags(GlowParticleOptions.FADE_IN | GlowParticleOptions.GROW), mouth.x, mouth.y, mouth.z,
                look.x * 0.035 + Mth.cos(windAngle) * 0.015 + (RANDOM.nextDouble() - 0.5) * 0.01, 0.006 + RANDOM.nextDouble() * 0.006,
                look.z * 0.035 + Mth.sin(windAngle) * 0.015 + (RANDOM.nextDouble() - 0.5) * 0.01);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (!storming(mc) || event.getMode() != FogRenderer.FogMode.FOG_TERRAIN || !event.isAtmospheric()) {
            return;
        }
        float storm = gust * (0.35F + 0.65F * exposure);
        float far = Mth.lerp(storm, FOG_CLEAR, FOG_GUST);
        net.yezon.theabyss.client.fx.FogRange.linear(event, far * 0.08F, far);
    }

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiLayersEvent event) {
        event.registerBelow(VanillaGuiLayers.CAMERA_OVERLAYS, ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "frost_storm"), (graphics, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            if (frost <= 0.0F || mc.player == null || mc.options.hideGui) {
                return;
            }
            int w = graphics.guiWidth();
            int h = graphics.guiHeight();
            GuiGraphicsCompat.blit(graphics, FROST_OVERLAY, 0, 0, 0.0F, 0.0F, w, h, w, h, ARGB.white(frost));
        });
    }

    private static final class WindSound extends AbstractTickableSoundInstance {
        private boolean ending;

        WindSound() {
            super(SoundEvents.ELYTRA_FLYING, SoundSource.WEATHER, RANDOM);
            this.looping = true;
            this.delay = 0;
            this.relative = true;
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.volume = 0.0F;
        }

        void end() {
            this.ending = true;
            this.stop();
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }

        @Override
        public void tick() {
            if (this.ending || wind != this) {
                this.stop();
                return;
            }
            float target = inStorm ? (0.1F + 0.32F * gust) * (0.3F + 0.7F * exposure) : 0.0F;
            this.volume += (target - this.volume) * 0.05F;
            this.pitch = 0.5F + 0.25F * gust;
        }
    }
}
