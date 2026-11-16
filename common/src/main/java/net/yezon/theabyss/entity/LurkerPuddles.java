package net.yezon.theabyss.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.tick.ServerTickEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class LurkerPuddles {
    private static final int MAX = 64;

    private record Puddle(ResourceKey<Level> dimension, Vec3 at, double radius, long until, LivingEntity owner) {
    }

    private static final List<Puddle> PUDDLES = new ArrayList<>();
    private static final Map<LivingEntity, Long> STUCK = new WeakHashMap<>();

    private LurkerPuddles() {
    }

    static void add(ServerLevel level, Vec3 at, double radius, int ticks, LivingEntity owner) {
        if (PUDDLES.size() >= MAX) {
            PUDDLES.remove(0);
        }
        PUDDLES.add(new Puddle(level.dimension(), at, radius, level.getGameTime() + ticks, owner));
    }

    static void stick(LivingEntity victim, int ticks) {
        STUCK.put(victim, victim.level().getGameTime() + ticks);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (PUDDLES.isEmpty() && STUCK.isEmpty()) {
            return;
        }
        Iterator<Puddle> it = PUDDLES.iterator();
        while (it.hasNext()) {
            Puddle p = it.next();
            ServerLevel level = event.getServer().getLevel(p.dimension());
            if (level == null || level.getGameTime() > p.until()) {
                it.remove();
                continue;
            }
            int tick = (int)(level.getGameTime() % 1200);
            LurkerFx.puddle(level, p.at(), p.radius(), tick);
            if (tick % 10 != 0) {
                continue;
            }
            AABB box = new AABB(p.at(), p.at()).inflate(p.radius(), 0.8, p.radius());
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && !(e instanceof JungleLurkerEntity)
                && !(e instanceof Player pl && (pl.isCreative() || pl.isSpectator())) && e.onGround())) {
                if (e.position().subtract(p.at()).horizontalDistance() <= p.radius()) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 2, false, true), p.owner());
                    STUCK.put(e, level.getGameTime() + 30);
                }
            }
        }
        Iterator<Map.Entry<LivingEntity, Long>> stuck = STUCK.entrySet().iterator();
        while (stuck.hasNext()) {
            Map.Entry<LivingEntity, Long> entry = stuck.next();
            LivingEntity e = entry.getKey();
            if (!e.isAlive() || e.isRemoved() || !(e.level() instanceof ServerLevel level) || level.getGameTime() > entry.getValue()) {
                stuck.remove();
                continue;
            }
            LurkerFx.stuck(level, e);
        }
    }
}
