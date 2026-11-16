package net.yezon.theabyss.client.fx;

import net.minecraft.client.Minecraft;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.InputEvent;
import net.yezon.theabyss.platform.client.network.ClientPacketDistributor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.entity.NightbladeBossEntity;
import net.yezon.theabyss.network.NightbladeParryMessage;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class NightbladeParryClient {
    private static final double RANGE = 24.0;

    private NightbladeParryClient() {
    }

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack()) {
            tryParry();
        }
    }

    public static boolean tryParry() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return false;
        }
        boolean pull = TelekinesisClient.pullParry();
        if (mc.player.getMainHandItem().isEmpty() && !pull) {
            return false;
        }
        boolean bossNear = pull || !mc.level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, mc.player.getBoundingBox().inflate(RANGE),
            net.yezon.theabyss.ring.EagleBossTracker::isBoss).isEmpty();
        if (bossNear) {
            ClientPacketDistributor.sendToServer(new NightbladeParryMessage());
        }
        return bossNear;
    }
}
