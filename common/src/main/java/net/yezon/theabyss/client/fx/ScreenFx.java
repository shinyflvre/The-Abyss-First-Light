package net.yezon.theabyss.client.fx;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.RenderFrameEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.astral.AstralData;
import net.yezon.theabyss.configuration.ConfigConfiguration;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class ScreenFx {
    public static final Identifier STATE_TEXTURE = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "textures/effect/fx_state.png");
    public static final Identifier ASTRAL_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "astral");
    public static final Identifier LIGHT_ENGINE_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "colored_light");
    public static final Identifier PRESENCE_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "nightblade_presence");
    public static final Identifier STORM_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "nightblade_storm");
    public static final Identifier RING_FX_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "ring_fx");
    public static final Identifier IMPACT_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "impact_frame");
    public static final Identifier ABYSS_GRADE_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "abyss_grade");
    public static final Identifier TIME_STOP_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "time_stop");
    public static final Identifier FOG_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "volumetric_fog");
    public static final Identifier DREAMSHIFT_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "dreamshift");
    public static final Identifier POCKET_FOLD_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "pocket_fold");
    public static final Identifier FEAR_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "fear");
    public static final Identifier AFTERLIFE_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "afterlife");
    public static final Identifier RIFT_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "rift_atmosphere");
    public static final Identifier RIFT_SHIFT_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "rift_shift");
    public static final Identifier WRAITH_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "wraith");
    public static final Identifier ECHO_VISION_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "echo_vision");
    public static final Identifier OVERGROWTH_EFFECT = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "overgrowth");

    private static final float ASTRAL_FADE_SECONDS = 0.7F;
    private static final float ASTRAL_FLASH_SECONDS = 0.9F;
    private static final float DEPART_SECONDS = 1.25F;
    private static final float ARRIVE_SECONDS = 2.8F;
    private static final float ABORT_SECONDS = 4.0F;
    private static final float FOLD_SECONDS = 0.8F;
    private static final float UNFOLD_SECONDS = 1.4F;
    private static final float FOLD_ABORT_SECONDS = 3.0F;
    private static final float RIFT_DEPART_SECONDS = 1.25F;
    private static final float RIFT_ARRIVE_SECONDS = 3.2F;
    private static final float RIFT_ABORT_SECONDS = 4.0F;

    private static DynamicTexture state;
    private static final int STATE_WIDTH = 176;
    private static final int[] uploaded = new int[STATE_WIDTH];
    private static final float[] cameraMatrix = {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1};
    private static int fogColour = 0xFF406060;
    private static final Identifier[] ORDER = {FOG_EFFECT, LIGHT_ENGINE_EFFECT, MagicLights.EFFECT, RIFT_EFFECT, ABYSS_GRADE_EFFECT, TIME_STOP_EFFECT, PRESENCE_EFFECT, STORM_EFFECT, RING_FX_EFFECT, ASTRAL_EFFECT, DREAMSHIFT_EFFECT, POCKET_FOLD_EFFECT, RIFT_SHIFT_EFFECT, FEAR_EFFECT, WRAITH_EFFECT, ECHO_VISION_EFFECT, OVERGROWTH_EFFECT, AFTERLIFE_EFFECT, IMPACT_EFFECT};
    private static float cameraYaw;
    private static float cameraPitch;
    private static float cameraFov = 70.0F;
    private static net.minecraft.world.phys.Vec3 lightVolume = net.minecraft.world.phys.Vec3.ZERO;
    private static net.minecraft.world.phys.Vec3 lightVolumeB = net.minecraft.world.phys.Vec3.ZERO;
    private static net.minecraft.world.phys.Vec3 timeStopCentre = net.minecraft.world.phys.Vec3.ZERO;
    private static net.minecraft.world.phys.Vec3 stormBoss = net.minecraft.world.phys.Vec3.ZERO;
    private static final float[] stormRings = new float[NightbladeStormClient.SLOTS * 4];
    private static final float[] arena = new float[5];
    private static final float[] ringFx = new float[RingFxClient.SLOTS * 8];
    private static final float[] afterLife = new float[AfterLifeClient.VALUES];
    private static float daylight;
    private static float fogGround = Float.NaN;
    private static long lastNanos;

    private static float astral;
    private static float astralFlash;
    private static boolean wasAstral;

    private static float shiftClock = -1.0F;
    private static float shift;
    private static boolean arriving;
    private static ResourceKey<Level> shiftFrom;
    private static float foldClock = -1.0F;
    private static float fold;
    private static boolean unfolding;
    private static ResourceKey<Level> foldFrom;
    private static float riftClock = -1.0F;
    private static float riftShift;
    private static boolean riftArriving;
    private static ResourceKey<Level> riftFrom;

    private ScreenFx() {
    }

    public static void startDreamShift() {
        Minecraft mc = Minecraft.getInstance();
        shiftClock = 0.0F;
        arriving = false;
        shiftFrom = mc.level != null ? mc.level.dimension() : null;
    }

    public static void startPocketFold() {
        Minecraft mc = Minecraft.getInstance();
        foldClock = 0.0F;
        unfolding = false;
        foldFrom = mc.level != null ? mc.level.dimension() : null;
    }

    public static void startRiftShift() {
        Minecraft mc = Minecraft.getInstance();
        riftClock = 0.0F;
        riftArriving = false;
        riftFrom = mc.level != null ? mc.level.dimension() : null;
    }

    public static float astralStrength() {
        return astral;
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        long now = Util.getNanos();
        float dt = lastNanos == 0L || mc.isPaused() ? 0.0F : Math.min(0.1F, (now - lastNanos) / 1.0E9F);
        lastNanos = now;
        if (mc.player == null || mc.level == null) {
            return;
        }

        boolean isAstral = AstralData.isAstral(mc.player);
        if (isAstral != wasAstral) {
            wasAstral = isAstral;
            astralFlash = 1.0F;
            if (isAstral) {
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BEACON_POWER_SELECT, 0.55F, 0.6F));
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_RESONATE, 0.7F, 0.8F));
            } else {
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BEACON_DEACTIVATE, 1.3F, 0.6F));
            }
        }
        astral = Mth.approach(astral, isAstral ? 1.0F : 0.0F, dt / ASTRAL_FADE_SECONDS);
        astralFlash = Math.max(0.0F, astralFlash - dt / ASTRAL_FLASH_SECONDS);

        if (shiftClock >= 0.0F) {
            shiftClock += dt;
            if (!arriving && (shiftFrom != null && mc.level.dimension() != shiftFrom || shiftClock > ABORT_SECONDS)) {
                arriving = true;
                shiftClock = 0.0F;
            }
            if (arriving) {
                float k = Mth.clamp(shiftClock / ARRIVE_SECONDS, 0.0F, 1.0F);
                shift = 1.0F - k * k * (3.0F - 2.0F * k);
                if (k >= 1.0F) {
                    shiftClock = -1.0F;
                    shift = 0.0F;
                    arriving = false;
                }
            } else {
                float k = Mth.clamp(shiftClock / DEPART_SECONDS, 0.0F, 1.0F);
                shift = k * k * (3.0F - 2.0F * k);
            }
        }

        if (foldClock >= 0.0F) {
            foldClock += dt;
            boolean loading = mc.gui.screen() instanceof net.minecraft.client.gui.screens.LevelLoadingScreen;
            if (!unfolding && !loading && (foldFrom != null && mc.level.dimension() != foldFrom || foldClock > FOLD_ABORT_SECONDS)) {
                unfolding = true;
                foldClock = 0.0F;
            }
            if (unfolding && loading) {
                foldClock = 0.0F;
            }
            if (unfolding) {
                float k = Mth.clamp(foldClock / UNFOLD_SECONDS, 0.0F, 1.0F);
                fold = 1.0F - k * k * (3.0F - 2.0F * k);
                if (k >= 1.0F) {
                    foldClock = -1.0F;
                    fold = 0.0F;
                    unfolding = false;
                }
            } else {
                fold = Math.min(1.0F, foldClock / FOLD_SECONDS);
            }
        }

        if (riftClock >= 0.0F) {
            riftClock += dt;
            boolean loading = mc.gui.screen() instanceof net.minecraft.client.gui.screens.LevelLoadingScreen;
            if (!riftArriving && !loading && (riftFrom != null && mc.level.dimension() != riftFrom || riftClock > RIFT_ABORT_SECONDS)) {
                riftArriving = true;
                riftClock = 0.0F;
            }
            if (riftArriving && loading) {
                riftClock = 0.0F;
            }
            if (riftArriving) {
                float k = Mth.clamp(riftClock / RIFT_ARRIVE_SECONDS, 0.0F, 1.0F);
                riftShift = 1.0F - k * k * (3.0F - 2.0F * k);
                if (k >= 1.0F) {
                    riftClock = -1.0F;
                    riftShift = 0.0F;
                    riftArriving = false;
                }
            } else {
                float k = Mth.clamp(riftClock / RIFT_DEPART_SECONDS, 0.0F, 1.0F);
                riftShift = k * k * (3.0F - 2.0F * k);
            }
        }

        updateFogGround(mc, dt);
        upload(mc);
        List<Identifier> effects = mc.player.getActivePostEffects();
        toggle(effects, FOG_EFFECT, volumetricFog(mc) && !net.yezon.theabyss.client.debug.OreHighlighter.active());
        toggle(effects, LIGHT_ENGINE_EFFECT, ColoredLight.active());
        toggle(effects, MagicLights.EFFECT, MagicLights.active());
        toggle(effects, ABYSS_GRADE_EFFECT, abyssGrading(mc));
        toggle(effects, TIME_STOP_EFFECT, net.yezon.theabyss.timestop.client.TimeStopClient.active());
        toggle(effects, ASTRAL_EFFECT, astral > 0.0F || astralFlash > 0.0F);
        toggle(effects, DREAMSHIFT_EFFECT, shift > 0.0F);
        toggle(effects, POCKET_FOLD_EFFECT, fold > 0.0F);
        toggle(effects, RIFT_EFFECT, net.yezon.theabyss.rift.RiftOfSins.isRift(mc.level));
        toggle(effects, RIFT_SHIFT_EFFECT, riftShift > 0.0F);
        toggle(effects, FEAR_EFFECT, FearClient.intensity() > 0.001F || FearClient.flash() > 0.01F || ShadowVeilClient.strength() > 0.001F);
        toggle(effects, WRAITH_EFFECT, net.yezon.theabyss.client.skill.WraithClient.intensity() > 0.001F || net.yezon.theabyss.client.skill.WraithClient.pulse() > 0.001F);
        toggle(effects, ECHO_VISION_EFFECT, EchoVisionClient.shaderOn());
        toggle(effects, OVERGROWTH_EFFECT, OvergrowthClient.stage() > 0.001F || OvergrowthClient.collapse() > 0.0F);
        toggle(effects, AFTERLIFE_EFFECT, AfterLifeClient.active());
        toggle(effects, IMPACT_EFFECT, ImpactFrame.strength() > 0.0F);
        toggle(effects, STORM_EFFECT, NightbladeStormClient.active() || NightbladeArenaClient.active());
        toggle(effects, RING_FX_EFFECT, RingFxClient.active());
        toggle(effects, PRESENCE_EFFECT, NightbladeAtmosphere.presence() > 0.005F || NightbladeAtmosphere.ripple() > 0.0F);
        int index = 0;
        for (Identifier ours : ORDER) {
            if (effects.remove(ours)) {
                effects.add(Math.min(index, effects.size()), ours);
                index++;
            }
        }
    }

    private static void updateFogGround(Minecraft mc, float dt) {
        net.minecraft.core.BlockPos centre = mc.gameRenderer.mainCamera().blockPosition();
        float sum = 0.0F;
        int count = 0;
        for (int dx = -20; dx <= 20; dx += 20) {
            for (int dz = -20; dz <= 20; dz += 20) {
                int x = centre.getX() + dx;
                int z = centre.getZ() + dz;
                if (mc.level.hasChunk(net.minecraft.core.SectionPos.blockToSectionCoord(x), net.minecraft.core.SectionPos.blockToSectionCoord(z))) {
                    sum += mc.level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    count++;
                }
            }
        }
        if (count == 0) {
            return;
        }
        float target = sum / count;
        fogGround = Float.isNaN(fogGround) || Math.abs(target - fogGround) > 64.0F ? target : fogGround + (target - fogGround) * Math.min(1.0F, dt * 0.4F);
    }

    public static int fogColour() {
        return fogColour;
    }

    private static boolean volumetricFog(Minecraft mc) {
        boolean enabled;
        try {
            enabled = net.yezon.theabyss.configuration.ConfigConfiguration.VOLUMETRIC_FOG.get();
        } catch (IllegalStateException notLoadedYet) {
            enabled = true;
        }
        if (!enabled || mc.level == null) {
            return false;
        }
        String dimension = mc.level.dimension().identifier().toString();
        return dimension.equals("theabyss:the_abyss") || dimension.equals("theabyss:frost_world");
    }

    @SubscribeEvent(priority = net.yezon.theabyss.platform.event.EventPriority.LOWEST)
    public static void onFogColour(net.yezon.theabyss.platform.client.event.ViewportEvent.ComputeFogColor event) {
        fogColour = ARGB.color(255, unit(event.getRed()), unit(event.getGreen()), unit(event.getBlue()));
    }

    private static void toggle(List<Identifier> effects, Identifier effect, boolean on) {
        if (on && !effects.contains(effect)) {
            effects.add(effect);
            boolean loaded = Minecraft.getInstance().getShaderManager()
                .getPostChain(effect, net.minecraft.client.renderer.LevelTargetBundle.MAIN_TARGETS) != null;
            TheabyssMod.LOGGER.info("[ScreenFx] {} on, post chain loaded: {}, state {}", effect, loaded, java.util.Arrays.toString(uploaded));
        } else if (!on) {
            effects.remove(effect);
        }
    }

    private static void upload(Minecraft mc) {
        if (state == null) {
            state = new DynamicTexture(() -> "theabyss fx state", STATE_WIDTH, 1, true);
            java.util.Arrays.fill(uploaded, -1);
            mc.getTextureManager().register(STATE_TEXTURE, state);
        }
        int[] pixels = {
            ARGB.color(unit(NightbladeAtmosphere.ripple()), unit(astral), unit(astralFlash), unit(NightbladeAtmosphere.presence())),
            ARGB.color(unit(NightbladeAtmosphere.heartbeat()), unit(shift), arriving ? 255 : 0, unit(ImpactFrame.strength())),
            packed16(Mth.wrapDegrees(cameraYaw) / 360.0F + 0.5F, (cameraPitch + 90.0F) / 180.0F),
            packed16(cameraFov / 180.0F, 0.0F),
            fogColour,
            packed16((float)(lightVolume.x + 128.0) / 256.0F, (float)(lightVolume.y + 128.0) / 256.0F),
            packed16((float)(lightVolume.z + 128.0) / 256.0F, daylight),
            packed16(((Float.isNaN(fogGround) ? 64.0F : fogGround) + 1024.0F) / 2048.0F, 0.0F),
        };
        boolean changed = false;
        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] != uploaded[i]) {
                state.getPixels().setPixel(i, 0, pixels[i]);
                uploaded[i] = pixels[i];
                changed = true;
            }
        }
        int slotB = packed16((float)(lightVolumeB.x + 128.0) / 256.0F, (float)(lightVolumeB.y + 128.0) / 256.0F);
        int slotBz = packed16((float)(lightVolumeB.z + 128.0) / 256.0F, ColoredLight.weightOfSlot1());
        for (int i = 0; i < 2; i++) {
            int pixel = i == 0 ? slotB : slotBz;
            if (pixel != uploaded[24 + i]) {
                state.getPixels().setPixel(24 + i, 0, pixel);
                uploaded[24 + i] = pixel;
                changed = true;
            }
        }
        put(26, packed16(net.yezon.theabyss.timestop.client.TimeStopClient.radius() / 512.0F, net.yezon.theabyss.timestop.client.TimeStopClient.strength()));
        put(27, packed16(net.yezon.theabyss.timestop.client.TimeStopClient.front(), net.yezon.theabyss.timestop.client.TimeStopClient.shock()));
        put(28, floatPixel((float)timeStopCentre.x));
        put(29, floatPixel((float)timeStopCentre.y));
        put(30, floatPixel((float)timeStopCentre.z));
        changed |= putChanged;
        putChanged = false;
        put(23, packed16((configInt(ConfigConfiguration.ABYSS_HUE) + 50) / 100.0F, (configInt(ConfigConfiguration.ABYSS_SATURATION) + 50) / 100.0F));
        put(31, packed16((configInt(ConfigConfiguration.ABYSS_LIGHTNESS) + 50) / 100.0F, 0.0F));
        changed |= putChanged;
        putChanged = false;
        put(32, floatPixel((float)stormBoss.x));
        put(33, floatPixel((float)stormBoss.y));
        put(34, floatPixel((float)stormBoss.z));
        put(35, floatPixel(NightbladeStormClient.darkness()));
        for (int i = 0; i < stormRings.length; i++) {
            put(36 + i, floatPixel(stormRings[i]));
        }
        for (int i = 0; i < arena.length; i++) {
            put(88 + i, floatPixel(arena[i]));
        }
        for (int i = 0; i < ringFx.length; i++) {
            put(96 + i, floatPixel(ringFx[i]));
        }
        for (int i = 0; i < afterLife.length; i++) {
            put(160 + i, floatPixel(afterLife[i]));
        }
        put(84, ARGB.color(255, unit(ImpactFrame.tint()), 0, 0));
        put(85, ARGB.color(255, unit(fold), unfolding ? 255 : 0, 0));
        put(86, ARGB.color(255, unit(riftShift), riftArriving ? 255 : 0, 0));
        put(87, ARGB.color(255, unit(net.yezon.theabyss.client.skill.WraithClient.intensity()), unit(net.yezon.theabyss.client.skill.WraithClient.pulse()), 0));
        put(93, ARGB.color(255, unit(OvergrowthClient.stage()), unit(OvergrowthClient.pulse()), unit(OvergrowthClient.collapse())));
        put(94, ARGB.color(unit(FearClient.hunt()), unit(FearClient.intensity()), unit(FearClient.beat()), unit(FearClient.glitch())));
        put(95, ARGB.color(255, unit(FearClient.flash()), unit(ShadowVeilClient.strength()), 0));
        put(175, ARGB.color(255, unit(EchoVisionClient.intensity()), unit(EchoVisionClient.edge()), unit(EchoVisionClient.flash())));
        changed |= putChanged;
        putChanged = false;
        int lightLook = packed16(configPercent(ConfigConfiguration.LIGHT_SATURATION) / 4.0F, configPercent(ConfigConfiguration.LIGHT_BRIGHTNESS) / 4.0F);
        if (lightLook != uploaded[22]) {
            state.getPixels().setPixel(22, 0, lightLook);
            uploaded[22] = lightLook;
            changed = true;
        }
        for (int i = 0; i < cameraMatrix.length; i++) {
            int pixel = floatPixel(cameraMatrix[i]);
            if (pixel != uploaded[8 + i]) {
                state.getPixels().setPixel(8 + i, 0, pixel);
                uploaded[8 + i] = pixel;
                changed = true;
            }
        }
        if (changed) {
            state.upload();
        }
    }

    private static boolean putChanged;

    private static void put(int index, int pixel) {
        if (pixel != uploaded[index]) {
            state.getPixels().setPixel(index, 0, pixel);
            uploaded[index] = pixel;
            putChanged = true;
        }
    }

    private static int floatPixel(float value) {
        int bits = Float.floatToRawIntBits(value);
        return ARGB.color(bits & 255, bits >>> 24, bits >> 16 & 255, bits >> 8 & 255);
    }

    private static int configInt(net.neoforged.neoforge.common.ModConfigSpec.IntValue value) {
        try {
            return value.get();
        } catch (IllegalStateException notLoadedYet) {
            return 0;
        }
    }

    private static boolean abyssGrading(Minecraft mc) {
        return mc.level != null && mc.level.dimension().identifier().toString().equals("theabyss:the_abyss")
            && (configInt(ConfigConfiguration.ABYSS_HUE) != 0 || configInt(ConfigConfiguration.ABYSS_SATURATION) != 0
                || configInt(ConfigConfiguration.ABYSS_LIGHTNESS) != 0);
    }

    private static float configPercent(net.neoforged.neoforge.common.ModConfigSpec.IntValue value) {
        try {
            return value.get() / 100.0F;
        } catch (IllegalStateException notLoadedYet) {
            return 1.0F;
        }
    }

    private static int packed16(float first, float second) {
        int a = Mth.clamp(Math.round(first * 65535.0F), 0, 65535);
        int b = Mth.clamp(Math.round(second * 65535.0F), 0, 65535);
        return ARGB.color(b & 255, a >> 8, a & 255, b >> 8);
    }

    @SubscribeEvent(priority = net.yezon.theabyss.platform.event.EventPriority.LOWEST)
    public static void onExtractLevel(net.yezon.theabyss.platform.client.event.ExtractLevelRenderStateEvent event) {
        net.minecraft.client.renderer.state.level.CameraRenderState camera = event.getRenderState().cameraRenderState;
        net.minecraft.client.renderer.state.OptionsRenderState options = event.getOptionsRenderState();
        net.minecraft.client.renderer.state.level.CameraEntityRenderState entity = camera.entityRenderState;
        com.mojang.blaze3d.vertex.PoseStack bob = new com.mojang.blaze3d.vertex.PoseStack();
        if (entity.isLiving) {
            if (entity.isDeadOrDying) {
                float duration = Math.min(entity.deathTime, 20.0F);
                bob.rotateDegrees(com.mojang.math.Axis.ZP, 40.0F - 8000.0F / (duration + 200.0F));
            }
            if (entity.hurtTime >= 0.0F && !net.yezon.theabyss.platform.services.ClientPlatformHooks.get().preventFlinch(entity)) {
                float hurt = entity.hurtTime / entity.hurtDuration;
                hurt = Mth.sin(hurt * hurt * hurt * hurt * (float)Math.PI);
                float direction = entity.hurtDir;
                bob.rotateDegrees(com.mojang.math.Axis.YP, -direction);
                bob.rotateDegrees(com.mojang.math.Axis.ZP, (float)(-hurt * 14.0 * options.damageTiltStrength));
                bob.rotateDegrees(com.mojang.math.Axis.YP, direction);
            }
        }
        if (options.bobView && entity.isPlayer) {
            float walk = entity.backwardsInterpolatedWalkDistance;
            float amount = entity.bob;
            bob.translate(Mth.sin(walk * (float)Math.PI) * amount * 0.5F, -Math.abs(Mth.cos(walk * (float)Math.PI) * amount), 0.0F);
            bob.rotateDegrees(com.mojang.math.Axis.ZP, Mth.sin(walk * (float)Math.PI) * amount * 3.0F);
            bob.rotateDegrees(com.mojang.math.Axis.XP, Math.abs(Mth.cos(walk * (float)Math.PI - 0.2F) * amount) * 5.0F);
        }
        org.joml.Matrix4f inverse = new org.joml.Matrix4f(bob.last().pose()).mul(camera.viewRotationMatrix).invert();
        float[] m = cameraMatrix;
        m[0] = inverse.m00();
        m[1] = inverse.m01();
        m[2] = inverse.m02();
        m[3] = inverse.m10();
        m[4] = inverse.m11();
        m[5] = inverse.m12();
        m[6] = inverse.m20();
        m[7] = inverse.m21();
        m[8] = inverse.m22();
        m[9] = inverse.m30();
        m[10] = inverse.m31();
        m[11] = inverse.m32();
        m[12] = camera.projectionMatrix.m00();
        m[13] = camera.projectionMatrix.m11();

        cameraYaw = camera.yRot;
        cameraPitch = camera.xRot;
        cameraFov = (float)Math.toDegrees(2.0 * Math.atan(1.0 / Math.max(1.0E-4F, camera.projectionMatrix.m11())));
        lightVolume = ColoredLight.originRelativeTo(0, camera.pos);
        lightVolumeB = ColoredLight.originRelativeTo(1, camera.pos);
        timeStopCentre = net.yezon.theabyss.timestop.client.TimeStopClient.centreRelativeTo(camera.pos);
        stormBoss = NightbladeStormClient.bossRelativeTo(camera.pos);
        NightbladeStormClient.fillSlots(camera.pos, stormRings);
        NightbladeArenaClient.fill(camera.pos, arena);
        RingFxClient.fill(camera.pos, ringFx);
        AfterLifeClient.fill(camera.pos, afterLife);
        daylight = ColoredLight.daylight(Minecraft.getInstance());
        if (state != null) {
            upload(Minecraft.getInstance());
        }
    }

    private static int unit(float value) {
        return Mth.clamp(Math.round(value * 255.0F), 0, 255);
    }
}
