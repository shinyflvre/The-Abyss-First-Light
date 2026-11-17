package net.yezon.theabyss.client.gravity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.model.data.EntityModelData;

public final class GravityGeoHead {
    private GravityGeoHead() {
    }

    public static Object adjust(DataTicket<?> ticket, Object value, Entity entity) {
        if (ticket != DataTickets.ENTITY_MODEL_DATA || !(value instanceof EntityModelData data) || !(entity instanceof LivingEntity living)) {
            return value;
        }
        float yaw = -GravityClient.headYaw(living, -data.netHeadYaw());
        float pitch = -GravityClient.headPitch(living, -data.headPitch());
        if (Float.compare(yaw, data.netHeadYaw()) == 0 && Float.compare(pitch, data.headPitch()) == 0) {
            return value;
        }
        return new EntityModelData(data.isSitting(), data.isChild(), yaw, pitch);
    }
}
