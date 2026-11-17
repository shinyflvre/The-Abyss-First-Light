package net.yezon.theabyss.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.client.ParticleStatus;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.particle.AmbientSmokeParticle;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.init.TheabyssModParticleTypes;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class AmbientSmoke {
    private static final int TARGET = 400;
    private static final int PER_TICK = 10;
    private static final double RADIUS = 24.0;

    private AmbientSmoke() {
    }

    private static boolean enabled() {
        try {
            return ConfigConfiguration.AMBIENT_SMOKE.get();
        } catch (IllegalStateException notLoadedYet) {
            return false;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null || mc.isPaused() || !enabled()) {
            return;
        }
        String dimension = level.dimension().location().toString();
        if (!dimension.equals("theabyss:the_abyss") && !dimension.equals("theabyss:frost_world")) {
            return;
        }
        ParticleStatus particles = mc.options.particles().get();
        if (particles == ParticleStatus.MINIMAL) {
            return;
        }
        int target = particles == ParticleStatus.DECREASED ? TARGET / 2 : TARGET;
        RandomSource random = level.getRandom();
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 motion = mc.player.getDeltaMovement();
        Vec3 ahead = new Vec3(motion.x, 0.0, motion.z).scale(30.0);
        if (ahead.length() > RADIUS * 0.6) {
            ahead = ahead.normalize().scale(RADIUS * 0.6);
        }
        int alive = AmbientSmokeParticle.alive(level);
        int perTick = alive < target / 2 ? PER_TICK * 4 : PER_TICK;
        for (int i = 0; i < perTick && alive < target; i++, alive++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = 3.0 + Math.sqrt(random.nextDouble()) * (RADIUS - 3.0);
            double x = camera.x + ahead.x + Math.cos(angle) * distance;
            double z = camera.z + ahead.z + Math.sin(angle) * distance;
            int blockX = (int)Math.floor(x);
            int blockZ = (int)Math.floor(z);
            if (!level.hasChunk(SectionPos.blockToSectionCoord(blockX), SectionPos.blockToSectionCoord(blockZ))) {
                continue;
            }
            int ground = ClientGroundHeight.noLeaves(level, blockX, blockZ);
            if (camera.y < ground - 12.0) {
                continue;
            }
            double y = ground + 0.1 + random.nextDouble() * random.nextDouble() * 1.4;
            double vx = 0.006 + (random.nextDouble() - 0.5) * 0.006;
            double vz = 0.003 + (random.nextDouble() - 0.5) * 0.006;
            double vy = (random.nextDouble() - 0.5) * 0.002;
            level.addAlwaysVisibleParticle(TheabyssModParticleTypes.AMBIENT_SMOKE.get(), true, x, y, z, vx, vy, vz);
        }
    }
}
