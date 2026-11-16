package net.yezon.theabyss.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModBlocks;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.ring.fx.FxOptions;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class MossGlow {
    private static final int[] COLOURS = {0x5FF5F0, 0x3AA8FF, 0x46D9F2, 0xBFFFF6};
    private static final double RANGE = 40.0;
    private static final int MAX_WALKERS = 24;

    private MossGlow() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.isPaused() || mc.getCameraEntity() == null) {
            return;
        }
        Vec3 eye = mc.getCameraEntity().position();
        int walkers = 0;
        for (Entity entity : level.getEntities((Entity)null, new AABB(eye, eye).inflate(RANGE), e -> e instanceof LivingEntity && e.onGround())) {
            if (walkers >= MAX_WALKERS) {
                break;
            }
            double dx = entity.getX() - entity.xo;
            double dz = entity.getZ() - entity.zo;
            double speed = Math.sqrt(dx * dx + dz * dz);
            if (speed < 0.02) {
                continue;
            }
            BlockPos below = BlockPos.containing(entity.getX(), entity.getY() - 0.2, entity.getZ());
            BlockState ground = level.getBlockState(below);
            if (!ground.is(TheabyssModBlocks.BLARU_MOSS.get()) && !ground.is(TheabyssModBlocks.GLOWING_BLARU_MOSS.get())) {
                continue;
            }
            walkers++;
            glow(level, entity, below, speed);
        }
    }

    private static void glow(ClientLevel level, Entity entity, BlockPos below, double speed) {
        RandomSource random = level.getRandom();
        double top = below.getY() + ground(level, below) + 0.015;
        double reach = entity.getBbWidth() * 0.5 + 0.35;
        int dots = (int)Math.min(4, 1 + speed * 9) + (random.nextFloat() < 0.3F ? 1 : 0);
        for (int i = 0; i < dots; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double r = reach * Math.sqrt(random.nextDouble());
            double x = entity.getX() + Math.cos(angle) * r;
            double z = entity.getZ() + Math.sin(angle) * r;
            int colour = COLOURS[random.nextInt(COLOURS.length)];
            int flags = FxOptions.FLAT | FxOptions.SHRINK;
            float size = 0.025F + random.nextFloat() * 0.02F;
            int life = 30 + random.nextInt(30);
            level.addParticle(TheabyssModParticleTypes.FX_DOT.get().of(colour, size, life, flags), true, false, x, top, z, 0.0, 0.0, 0.0);
        }
    }

    private static double ground(ClientLevel level, BlockPos pos) {
        var shape = level.getBlockState(pos).getCollisionShape(level, pos);
        return shape.isEmpty() ? 1.0 : shape.max(net.minecraft.core.Direction.Axis.Y);
    }
}
