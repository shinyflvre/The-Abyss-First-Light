package net.yezon.theabyss.client.fx;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
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

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (ACTIVE.isEmpty() || mc.level == null || mc.player == null) {
            return;
        }
        AvatarRenderState state = event.getRenderState();
        State invisible = ACTIVE.get(state.id);
        if (invisible == null) {
            return;
        }
        if (mc.level.getGameTime() > invisible.until) {
            ACTIVE.remove(state.id);
            return;
        }
        Entity other = mc.level.getEntity(state.id);
        boolean noticed = other != null && other != mc.player && other.distanceTo(mc.player) <= invisible.radius;
        if (noticed) {
            state.isInvisible = false;
            state.isInvisibleToPlayer = false;
            return;
        }
        event.setCanceled(true);
        state.shadowPieces.clear();
        state.displayFireAnimation = false;
    }
}
