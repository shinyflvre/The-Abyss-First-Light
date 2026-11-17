package net.yezon.theabyss.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class RenderTicks {
    private RenderTicks() {
    }

    public static float partialTick(Entity entity) {
        boolean frozen = entity.level().tickRateManager().isEntityFrozen(entity);
        return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(!frozen);
    }

    public static Vec3 position(Entity entity, float partialTick) {
        return new Vec3(net.minecraft.util.Mth.lerp(partialTick, entity.xOld, entity.getX()), net.minecraft.util.Mth.lerp(partialTick, entity.yOld, entity.getY()),
            net.minecraft.util.Mth.lerp(partialTick, entity.zOld, entity.getZ()));
    }
}
