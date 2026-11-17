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
import net.yezon.theabyss.client.particle.GroundFogParticle;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.init.TheabyssModParticleTypes;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class GroundFog {
    private static final int TARGET = 320;
    private static final int PER_TICK = 4;
    private static final double RANGE = 48.0;

    private GroundFog() {
    }

    private static boolean enabled() {
        try {
            return ConfigConfiguration.GROUND_FOG.get();
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
        int alive = GroundFogParticle.alive(level);
        int perTick = alive < target / 2 ? PER_TICK * 4 : PER_TICK;
        for (int i = 0; i < perTick && alive < target; i++, alive++) {
            double x = camera.x + (random.nextDouble() - random.nextDouble()) * RANGE;
            double z = camera.z + (random.nextDouble() - random.nextDouble()) * RANGE;
            int blockX = (int)Math.floor(x);
            int blockZ = (int)Math.floor(z);
            if (!level.hasChunk(SectionPos.blockToSectionCoord(blockX), SectionPos.blockToSectionCoord(blockZ))) {
                continue;
            }
            int ground = ClientGroundHeight.noLeaves(level, blockX, blockZ);
            if (Math.abs(ground - camera.y) > 40.0) {
                continue;
            }
            double y = ground + 0.05 + random.nextDouble() * 0.35;
            double vx = (random.nextDouble() - 0.5) * 0.004;
            double vz = (random.nextDouble() - 0.5) * 0.004;
            level.addAlwaysVisibleParticle(TheabyssModParticleTypes.GROUND_FOG.get(), true, x, y, z, vx, 0.0, vz);
        }
    }
}
