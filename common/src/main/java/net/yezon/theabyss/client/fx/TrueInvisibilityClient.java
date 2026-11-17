package net.yezon.theabyss.client.fx;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.RenderPlayerEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class TrueInvisibilityClient {
    private record State(long until, float radius) {
    }

    private static final Map<Integer, State> ACTIVE = new HashMap<>();
    private static Entity revealed;

    private TrueInvisibilityClient() {
    }

    public static void set(int entityId, int ticks, float radius) {
        Minecraft mc = Minecraft.getInstance();
        if (ticks <= 0 || mc.level == null) {
            ACTIVE.remove(entityId);
        } else {
            ACTIVE.put(entityId, new State(mc.level.getGameTime() + ticks, radius));
        }
    }

    public static float localRadius() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return 0.0F;
        }
        State state = ACTIVE.get(mc.player.getId());
        return state != null && mc.level.getGameTime() <= state.until ? state.radius : 0.0F;
    }

    public static boolean revealed(Entity entity) {
        return revealed == entity;
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        revealed = null;
        Minecraft mc = Minecraft.getInstance();
        if (ACTIVE.isEmpty() || mc.level == null || mc.player == null) {
            return;
        }
        Entity other = event.getEntity();
        int id = other.getId();
        State invisible = ACTIVE.get(id);
        if (invisible == null) {
            return;
        }
        if (mc.level.getGameTime() > invisible.until) {
            ACTIVE.remove(id);
            return;
        }
        boolean noticed = other != mc.player && other.distanceTo(mc.player) <= invisible.radius;
        if (noticed) {
            revealed = other;
            return;
        }
        event.setCanceled(true);
        event.hideShadowAndFire();
    }
}
