package net.yezon.theabyss.client.fx;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.entity.FearShadeEntity;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.network.FearCaughtMessage;
import net.yezon.theabyss.ring.fx.GlowParticleOptions;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class FearClient {
    private static final int RAMP_TICKS = 100;
    private static final int FIRST_SHADE = 120;
    private static final int SHADE_TIMEOUT = 140;
    private static final int WINDUP_TICKS = 26;
    private static final int HUNT_TICKS = 150;
    private static final int JUMPSCARE_TICKS = 16;
    private static final int HUNT_RESERVE = 220;
    private static final double HUNT_SPEED = 0.33;
    private static final double CATCH_DISTANCE = 1.4;
    private static final double[] APPEAR_DISTANCE = {22.0, 16.0, 11.5, 8.5};
    private static final int SHADE_ID = -730_000_001;
    private static final Identifier WHISPER_SOUND = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "phantom_ambient");
    private static final Identifier JUMPSCARE_SOUND = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "fear_jumpscare");
    private static final SoundEvent AMBIENCE_SOUND = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "fear_ambience"));
    private static final float AMBIENCE_VOLUME = 1.0F;
    private static final RandomSource RANDOM = RandomSource.create();
    private static final Set<SoundInstance> OWN = Collections.newSetFromMap(new WeakHashMap<>());

    private enum Stage {
        NONE,
        CALM,
        SHADE,
        GAP,
        WINDUP,
        HUNT,
        JUMPSCARE,
        RESOLVED
    }

    private static Stage stage = Stage.NONE;
    private static int ticks;
    private static int duration;
    private static int stageTicks;
    private static int gapLength;
    private static int appearances;
    private static int lookTicks;
    private static int nextBeat;
    private static int nextWhisper;
    private static int stepsLeft;
    private static int stepTimer;
    private static int flickerTicks;
    private static float intensity;
    private static float tension;
    private static float beat;
    private static float glitch;
    private static float hunt;
    private static float flash;
    private static float jitter;
    private static Vec3 stepFrom = Vec3.ZERO;
    private static Vec3 shadePos = Vec3.ZERO;
    private static @Nullable FearShadeEntity shade;
    private static @Nullable Level shadeLevel;
    private static @Nullable Ambience ambience;

    static {
        FearShadeEntity.driver = FearClient::drive;
    }

    private FearClient() {
    }

    public static float intensity() {
        return intensity;
    }

    public static float beat() {
        return beat;
    }

    public static float glitch() {
        return glitch;
    }

    public static float hunt() {
        return hunt;
    }

    public static float flash() {
        return flash;
    }

    public static float shadeJitter() {
        return jitter;
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mc.level == null || player == null) {
            reset();
            return;
        }
        if (mc.isPaused()) {
            return;
        }
        if (shade != null && (shade.level() != mc.level || shade.isRemoved())) {
            removeShade(false);
        }
        MobEffectInstance fear = player.getEffect(TheabyssModMobEffects.FEAR);
        if (fear != null && stage == Stage.NONE && player.isAlive()) {
            begin(fear.getDuration());
        } else if (fear == null && stage != Stage.NONE && stage != Stage.RESOLVED && stage != Stage.JUMPSCARE) {
            resolve(false);
        }
        beat *= 0.8F;
        glitch *= 0.86F;
        flash *= 0.9F;
        jitter = Math.max(0.0F, jitter - 0.08F);
        if (stage == Stage.NONE) {
            intensity = Math.max(0.0F, intensity - 0.05F);
            hunt = Math.max(0.0F, hunt - 0.05F);
            return;
        }
        ticks++;
        stageTicks++;
        if (stage == Stage.RESOLVED) {
            intensity = Math.max(0.0F, intensity - 1.0F / 50.0F);
            hunt = Math.max(0.0F, hunt - 0.05F);
            if (intensity <= 0.0F && fear == null) {
                stage = Stage.NONE;
            }
            return;
        }
        intensity = Math.min(1.0F, intensity + 1.0F / RAMP_TICKS);
        hunt = stage == Stage.HUNT || stage == Stage.WINDUP || stage == Stage.JUMPSCARE ? Math.min(1.0F, hunt + 0.08F) : Math.max(0.0F, hunt - 0.04F);
        tension = tension();
        heartbeat(mc);
        flicker();
        if (stage != Stage.HUNT && stage != Stage.JUMPSCARE && stage != Stage.WINDUP) {
            whispers(mc, player);
        }
        footsteps(mc);
        switch (stage) {
            case CALM -> {
                if (ticks >= FIRST_SHADE) {
                    nextShade(mc, player);
                }
            }
            case SHADE -> watchShade(mc, player);
            case GAP -> {
                if (stageTicks >= gapLength) {
                    nextShade(mc, player);
                }
            }
            case WINDUP -> {
                jitter = Math.max(jitter, stageTicks % 9 < 2 ? 0.5F : 0.0F);
                if (stageTicks >= WINDUP_TICKS) {
                    setStage(Stage.HUNT);
                }
            }
            case HUNT -> huntStep(mc, player);
            case JUMPSCARE -> {
                if (stageTicks >= JUMPSCARE_TICKS) {
                    removeShade(true);
                    resolve(true);
                }
            }
            default -> {
            }
        }
    }

    private static void begin(int effectTicks) {
        Minecraft.getInstance().getMusicManager().stopPlaying();
        if (ambience != null) {
            ambience.end();
        }
        ambience = new Ambience();
        Minecraft.getInstance().getSoundManager().play(ambience);
        stage = Stage.CALM;
        ticks = 0;
        stageTicks = 0;
        duration = effectTicks;
        appearances = 0;
        nextBeat = 10;
        nextWhisper = 50 + RANDOM.nextInt(40);
        stepsLeft = 0;
    }

    private static void resolve(boolean withGlitch) {
        removeShade(withGlitch);
        if (withGlitch) {
            glitch = 1.0F;
        }
        stage = Stage.RESOLVED;
        stageTicks = 0;
    }

    private static void reset() {
        removeShade(false);
        if (ambience != null) {
            ambience.end();
            ambience = null;
        }
        stage = Stage.NONE;
        intensity = 0.0F;
        hunt = 0.0F;
        beat = 0.0F;
        glitch = 0.0F;
        flash = 0.0F;
        jitter = 0.0F;
    }

    private static void setStage(Stage next) {
        stage = next;
        stageTicks = 0;
        if (next == Stage.HUNT) {
            play(SoundEvents.ENDERMAN_STARE.location(), shadePos, 1.2F, 0.55F, false);
        }
    }

    private static float tension() {
        return switch (stage) {
            case CALM -> 0.2F + 0.25F * Math.min(1.0F, ticks / (float)FIRST_SHADE);
            case SHADE -> 0.55F + 0.35F * (float)Math.max(0.0, 1.0 - shadePos.distanceTo(Minecraft.getInstance().player.position()) / 24.0);
            case GAP -> Math.min(0.85F, 0.45F + appearances * 0.1F);
            default -> 1.0F;
        };
    }

    private static void heartbeat(Minecraft mc) {
        if (--nextBeat > 0) {
            return;
        }
        beat = 1.0F;
        nextBeat = Math.round(Mth.lerp(tension, 30.0F, 8.0F));
        playUi(SoundEvents.WARDEN_HEARTBEAT.location(), 0.55F + 0.45F * tension, 0.85F + 0.3F * tension);
        if (tension > 0.6F) {
            CameraShake.add(0.12F + 0.25F * tension, 0.15F);
        }
    }

    private static void flicker() {
        if (flickerTicks > 0) {
            flickerTicks--;
        } else if (RANDOM.nextFloat() < 0.012F + 0.05F * tension * hunt + 0.02F * tension) {
            flickerTicks = 2 + RANDOM.nextInt(5);
        }
    }

    private static void whispers(Minecraft mc, LocalPlayer player) {
        if (--nextWhisper > 0) {
            return;
        }
        nextWhisper = 70 + RANDOM.nextInt(90);
        Vec3 behind = around(player, 150.0F + RANDOM.nextFloat() * 60.0F, 4.0 + RANDOM.nextDouble() * 4.0);
        switch (RANDOM.nextInt(4)) {
            case 0 -> play(SoundEvents.AMBIENT_CAVE.value().location(), behind, 0.7F, 0.8F + RANDOM.nextFloat() * 0.3F, true);
            case 1 -> play(WHISPER_SOUND, behind, 0.35F, 0.5F + RANDOM.nextFloat() * 0.15F, true);
            case 2 -> play(SoundEvents.WARDEN_TENDRIL_CLICKS.location(), behind, 0.6F, 0.7F, true);
            default -> {
                stepsLeft = 5;
                stepTimer = 0;
                stepFrom = around(player, 165.0F + RANDOM.nextFloat() * 30.0F, 7.5);
            }
        }
    }

    private static void footsteps(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (stepsLeft <= 0 || --stepTimer > 0) {
            return;
        }
        stepTimer = 7;
        stepsLeft--;
        Vec3 to = player.position();
        Vec3 at = stepFrom.lerp(to, 0.13 * (5 - stepsLeft));
        step(mc, at, 0.55F);
    }

    private static void step(Minecraft mc, Vec3 at, float volume) {
        BlockState ground = mc.level.getBlockState(BlockPos.containing(at.x, at.y - 0.2, at.z));
        Identifier sound = ground.isAir() ? SoundEvents.GRASS_STEP.location() : ground.getSoundType().getStepSound().location();
        play(sound, at, volume, 0.75F + RANDOM.nextFloat() * 0.1F, true);
    }

    private static void nextShade(Minecraft mc, LocalPlayer player) {
        int left = duration - ticks;
        boolean huntTime = appearances >= APPEAR_DISTANCE.length || left <= HUNT_RESERVE;
        if (huntTime && left < WINDUP_TICKS + 90) {
            setStage(Stage.GAP);
            gapLength = Integer.MAX_VALUE;
            return;
        }
        if (huntTime) {
            if (spawnShade(mc, player, 35.0F, 15.0, true)) {
                setStage(Stage.WINDUP);
            }
            return;
        }
        double distance = APPEAR_DISTANCE[appearances];
        boolean behind = appearances % 2 == 1 && RANDOM.nextBoolean();
        if (spawnShade(mc, player, behind ? 180.0F : 42.0F, distance, !behind)) {
            setStage(Stage.SHADE);
            lookTicks = 0;
            if (behind) {
                stepsLeft = 3;
                stepTimer = 0;
                stepFrom = shadePos;
            }
        }
    }

    private static boolean spawnShade(Minecraft mc, LocalPlayer player, float angle, double distance, boolean needSight) {
        for (int attempt = 0; attempt < 14; attempt++) {
            float side = RANDOM.nextBoolean() ? 1.0F : -1.0F;
            float jitterAngle = (RANDOM.nextFloat() - 0.5F) * 30.0F;
            float yaw = angle >= 180.0F ? 180.0F + jitterAngle : side * (angle * (0.75F + RANDOM.nextFloat() * 0.35F)) + jitterAngle * 0.3F;
            double d = distance * (0.9 + RANDOM.nextDouble() * 0.2);
            Vec3 at = around(player, yaw, d);
            Vec3 ground = standable(mc.level, at.x, player.getY(), at.z);
            if (ground == null) {
                continue;
            }
            if (needSight && !visible(mc, player, ground)) {
                continue;
            }
            placeShade(mc, ground);
            return true;
        }
        return false;
    }

    private static void placeShade(Minecraft mc, Vec3 at) {
        if (shade == null || shade.level() != mc.level || shade.isRemoved()) {
            shade = TheabyssModEntities.FEAR_SHADE.get().create(mc.level, EntitySpawnReason.LOAD);
            if (shade == null) {
                return;
            }
            shade.setId(SHADE_ID);
            shadePos = at;
            face(shade, mc.player);
            shade.snapTo(at.x, at.y, at.z, shade.getYRot(), shade.getXRot());
            shade.setOldPosAndRot();
            mc.level.addEntity(shade);
            shadeLevel = mc.level;
        } else {
            shadePos = at;
            shade.snapTo(at.x, at.y, at.z, shade.getYRot(), shade.getXRot());
            shade.setOldPosAndRot();
        }
        smoke(mc, at, 6);
    }

    private static void removeShade(boolean withEffect) {
        if (shade != null) {
            Minecraft mc = Minecraft.getInstance();
            if (withEffect && mc.level != null && shade.level() == mc.level) {
                smoke(mc, shade.position(), 14);
                play(SoundEvents.ENDERMAN_TELEPORT.location(), shade.position(), 0.35F, 0.45F, false);
                glitch = 1.0F;
            }
            shade.discard();
        }
        shade = null;
        shadeLevel = null;
    }

    private static void drive(FearShadeEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (entity != shade || mc.player == null) {
            if (entity.getId() == SHADE_ID && entity != shade) {
                entity.discard();
            }
            return;
        }
        entity.setPos(shadePos.x, shadePos.y, shadePos.z);
        face(entity, mc.player);
        if (stage != Stage.JUMPSCARE) {
            eyes(mc, entity);
        }
    }

    private static void eyes(Minecraft mc, FearShadeEntity entity) {
        float yaw = entity.getYRot() * Mth.DEG_TO_RAD;
        double fx = -Mth.sin(yaw);
        double fz = Mth.cos(yaw);
        double eyeY = shadePos.y + 1.56;
        float size = (float)Math.min(0.16, 0.03 + 0.0055 * mc.player.getEyePosition().distanceTo(shadePos));
        for (int side = -1; side <= 1; side += 2) {
            double x = shadePos.x + fx * 0.27 + fz * 0.125 * side;
            double z = shadePos.z + fz * 0.27 - fx * 0.125 * side;
            mc.level.addParticle(GlowParticleOptions.of(0x9FFAFF, size, 2).drag(0.0F).flags(0), x, eyeY, z, 0.0, 0.0, 0.0);
        }
    }

    private static void face(FearShadeEntity entity, LocalPlayer player) {
        Vec3 eye = player.getEyePosition();
        double dx = eye.x - entity.getX();
        double dz = eye.z - entity.getZ();
        double dy = eye.y - (entity.getY() + 1.62);
        float yaw = (float)(Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        float pitch = (float)(-(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * Mth.RAD_TO_DEG));
        entity.setYRot(yaw);
        entity.yBodyRot = yaw;
        entity.yHeadRot = yaw;
        entity.setXRot(pitch);
    }

    private static void watchShade(Minecraft mc, LocalPlayer player) {
        if (shade == null) {
            setStage(Stage.GAP);
            gapLength = 40;
            return;
        }
        Vec3 head = shadePos.add(0.0, 1.6, 0.0);
        Vec3 eye = player.getEyePosition();
        Vec3 to = head.subtract(eye).normalize();
        double dot = player.getViewVector(1.0F).dot(to);
        boolean looked = dot > Math.cos(Math.toRadians(10.0)) && visible(mc, player, shadePos);
        lookTicks = looked ? lookTicks + 1 : 0;
        boolean tooClose = eye.distanceTo(head) < 3.5;
        if (lookTicks >= 5 || tooClose || stageTicks > SHADE_TIMEOUT) {
            removeShade(lookTicks >= 5 || tooClose);
            appearances++;
            setStage(Stage.GAP);
            gapLength = 50 + RANDOM.nextInt(60);
        }
    }

    private static void huntStep(Minecraft mc, LocalPlayer player) {
        if (shade == null) {
            resolve(true);
            return;
        }
        Vec3 target = player.position();
        Vec3 flat = new Vec3(target.x - shadePos.x, 0.0, target.z - shadePos.z);
        double distance = flat.length();
        if (distance < CATCH_DISTANCE && Math.abs(target.y - shadePos.y) < 2.5) {
            jumpscare(mc, player);
            return;
        }
        if (stageTicks > HUNT_TICKS || distance > 30.0) {
            removeShade(true);
            resolve(true);
            return;
        }
        Vec3 next = shadePos.add(flat.normalize().scale(Math.min(HUNT_SPEED, distance)));
        Vec3 ground = standable(mc.level, next.x, shadePos.y, next.z);
        shadePos = ground != null ? ground : new Vec3(next.x, shadePos.y + (target.y - shadePos.y) * 0.2, next.z);
        if (stageTicks % 4 == 0) {
            step(mc, shadePos, 0.8F);
        }
        if (stageTicks % 3 == 0) {
            mc.level.addParticle(ParticleTypes.LARGE_SMOKE, shadePos.x, shadePos.y + 0.3, shadePos.z, 0.0, 0.02, 0.0);
        }
    }

    private static void jumpscare(Minecraft mc, LocalPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 flatLook = new Vec3(look.x, 0.0, look.z);
        flatLook = flatLook.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : flatLook.normalize();
        shadePos = new Vec3(eye.x + flatLook.x * 1.3, eye.y - 1.5, eye.z + flatLook.z * 1.3);
        if (shade != null) {
            shade.snapTo(shadePos.x, shadePos.y, shadePos.z, shade.getYRot(), shade.getXRot());
            shade.setOldPosAndRot();
        }
        setStage(Stage.JUMPSCARE);
        flash = 1.0F;
        glitch = 0.3F;
        jitter = 0.35F;
        playUi(JUMPSCARE_SOUND, 1.0F, 1.0F);
        CameraShake.add(3.5F, 0.6F);
        ClientPacketDistributor.sendToServer(new FearCaughtMessage());
    }

    private static Vec3 around(LocalPlayer player, float yawOffset, double distance) {
        float yaw = (player.getYRot() + yawOffset) * Mth.DEG_TO_RAD;
        return new Vec3(player.getX() - Mth.sin(yaw) * distance, player.getY(), player.getZ() + Mth.cos(yaw) * distance);
    }

    private static @Nullable Vec3 standable(Level level, double x, double y, double z) {
        int bx = Mth.floor(x);
        int bz = Mth.floor(z);
        int start = Mth.floor(y);
        for (int dy = 0; dy <= 8; dy++) {
            for (int sign = -1; sign <= 1; sign += 2) {
                int by = start + dy * sign;
                if (stand(level, bx, by, bz)) {
                    return new Vec3(x, by, z);
                }
                if (dy == 0) {
                    break;
                }
            }
        }
        return null;
    }

    private static boolean stand(Level level, int x, int y, int z) {
        BlockPos feet = new BlockPos(x, y, z);
        BlockState below = level.getBlockState(feet.below());
        return below.isFaceSturdy(level, feet.below(), net.minecraft.core.Direction.UP) && below.getFluidState().isEmpty()
            && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty() && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
            && level.getFluidState(feet).isEmpty();
    }

    private static boolean visible(Minecraft mc, LocalPlayer player, Vec3 feet) {
        Vec3 eye = player.getEyePosition();
        Vec3 head = feet.add(0.0, 1.6, 0.0);
        return mc.level.clip(new ClipContext(eye, head, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS;
    }

    private static void smoke(Minecraft mc, Vec3 at, int count) {
        for (int i = 0; i < count; i++) {
            mc.level.addParticle(ParticleTypes.LARGE_SMOKE, at.x + (RANDOM.nextDouble() - 0.5) * 0.7, at.y + RANDOM.nextDouble() * 1.9,
                at.z + (RANDOM.nextDouble() - 0.5) * 0.7, 0.0, 0.01, 0.0);
        }
    }

    private static void play(Identifier sound, Vec3 at, float volume, float pitch, boolean linear) {
        SoundInstance instance = new SimpleSoundInstance(sound, SoundSource.AMBIENT, volume, pitch, RANDOM, false, 0,
            linear ? SoundInstance.Attenuation.LINEAR : SoundInstance.Attenuation.NONE, at.x, at.y, at.z, false);
        OWN.add(instance);
        Minecraft.getInstance().getSoundManager().play(instance);
    }

    private static void playUi(Identifier sound, float volume, float pitch) {
        SoundInstance instance = new SimpleSoundInstance(sound, SoundSource.MASTER, volume, pitch, RANDOM, false, 0, SoundInstance.Attenuation.NONE,
            0.0, 0.0, 0.0, true);
        OWN.add(instance);
        Minecraft.getInstance().getSoundManager().play(instance);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (intensity <= 0.0F || sound == null || OWN.contains(sound) || OWN.contains(event.getOriginalSound()) || sound instanceof TickableSoundInstance
            || sound.isLooping() || sound.getSource() == SoundSource.MUSIC || sound.getSource() == SoundSource.RECORDS || sound.getSource() == SoundSource.VOICE
            || sound.getSource() == SoundSource.MASTER) {
            return;
        }
        event.setSound(new Muffled(sound, 1.0F - 0.55F * intensity));
    }

    @SubscribeEvent
    public static void onSelectMusic(SelectMusicEvent event) {
        if (stage != Stage.NONE) {
            event.setMusic(null);
        }
    }

    @SubscribeEvent
    public static void onExtract(ExtractLevelRenderStateEvent event) {
        if (intensity <= 0.0F || flickerTicks <= 0) {
            return;
        }
        LightmapRenderState lightmap = Minecraft.getInstance().gameRenderer.gameRenderState().lightmapRenderState;
        lightmap.blockFactor *= 1.0F - 0.7F * intensity;
        lightmap.needsUpdate = true;
    }

    @SubscribeEvent
    public static void onFov(ViewportEvent.ComputeFov event) {
        if (intensity <= 0.0F) {
            return;
        }
        event.setFOV(event.getFOV() * (1.0F - 0.12F * hunt * intensity - 0.025F * beat * intensity));
    }

    private static final class Ambience extends AbstractTickableSoundInstance {
        private boolean ending;

        Ambience() {
            super(AMBIENCE_SOUND, SoundSource.MASTER, RANDOM);
            this.looping = false;
            this.delay = 0;
            this.relative = true;
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.volume = 0.0F;
            OWN.add(this);
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
            if (this.ending || stage == Stage.NONE && intensity <= 0.0F || ambience != this) {
                this.stop();
                return;
            }
            this.volume = AMBIENCE_VOLUME * Math.min(1.0F, intensity * 1.4F);
        }
    }

    private record Muffled(SoundInstance inner, float factor) implements SoundInstance {
        @Override
        public Identifier getIdentifier() {
            return this.inner.getIdentifier();
        }

        @Override
        public @Nullable WeighedSoundEvents getOrResolve(SoundManager soundManager) {
            return this.inner.getOrResolve(soundManager);
        }

        @Override
        public net.minecraft.client.resources.sounds.@Nullable Sound getSound() {
            return this.inner.getSound();
        }

        @Override
        public @Nullable WeighedSoundEvents getSoundEvent() {
            return this.inner.getSoundEvent();
        }

        @Override
        public SoundSource getSource() {
            return this.inner.getSource();
        }

        @Override
        public boolean isLooping() {
            return this.inner.isLooping();
        }

        @Override
        public boolean isRelative() {
            return this.inner.isRelative();
        }

        @Override
        public int getDelay() {
            return this.inner.getDelay();
        }

        @Override
        public float getVolume() {
            return this.inner.getVolume() * this.factor;
        }

        @Override
        public float getPitch() {
            return this.inner.getPitch() * (1.0F - 0.06F * (1.0F - this.factor));
        }

        @Override
        public double getX() {
            return this.inner.getX();
        }

        @Override
        public double getY() {
            return this.inner.getY();
        }

        @Override
        public double getZ() {
            return this.inner.getZ();
        }

        @Override
        public Attenuation getAttenuation() {
            return this.inner.getAttenuation();
        }

        @Override
        public boolean canStartSilent() {
            return this.inner.canStartSilent();
        }

        @Override
        public boolean canPlaySound() {
            return this.inner.canPlaySound();
        }
    }
}
