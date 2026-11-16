package net.yezon.theabyss.ring;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class RingAura {
    @FunctionalInterface
    public interface Tick {
        boolean tick(ServerLevel level, Entity entity, int age);
    }

    private static final class Aura {
        final Entity entity;
        final int duration;
        final Tick tick;
        final String key;
        int age;

        Aura(Entity entity, int duration, Tick tick, String key) {
            this.entity = entity;
            this.duration = duration;
            this.tick = tick;
            this.key = key;
        }
    }

    private static final List<Aura> AURAS = new ArrayList<>();

    private RingAura() {
    }

    public static void start(Entity entity, String key, int duration, Tick tick) {
        AURAS.removeIf(a -> a.entity == entity && a.key.equals(key));
        AURAS.add(new Aura(entity, duration, tick, key));
    }

    public static void stop(Entity entity, String key) {
        AURAS.removeIf(a -> a.entity == entity && a.key.equals(key));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (AURAS.isEmpty()) {
            return;
        }
        List<Aura> running = new ArrayList<>(AURAS);
        for (Aura aura : running) {
            boolean keep = aura.age < aura.duration && aura.entity.isAlive() && aura.entity.level() instanceof ServerLevel;
            if (keep) {
                keep = aura.tick.tick((ServerLevel)aura.entity.level(), aura.entity, aura.age++);
            }
            if (!keep) {
                AURAS.remove(aura);
            }
        }
    }
}
