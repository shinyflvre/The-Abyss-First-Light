package net.yezon.theabyss.fabric.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

public final class FabricRenderStateModifiers {
    private static final List<Entry> ENTITY = new ArrayList<>();
    private static final Map<Class<?>, List<BiConsumer<Object, Object>>> CACHE = new ConcurrentHashMap<>();
    private static volatile boolean empty = true;

    private FabricRenderStateModifiers() {
    }

    @SuppressWarnings("unchecked")
    static synchronized void register(Class<?> baseRenderer, BiConsumer<?, ?> modifier) {
        ENTITY.add(new Entry(baseRenderer, (BiConsumer<Object, Object>)modifier));
        CACHE.clear();
        empty = false;
    }

    public static void apply(EntityRenderer<?, ?> renderer, Entity entity, EntityRenderState state) {
        if (empty) {
            return;
        }
        Collection<BiConsumer<Object, Object>> modifiers = CACHE.computeIfAbsent(renderer.getClass(), FabricRenderStateModifiers::collect);
        for (BiConsumer<Object, Object> modifier : modifiers) {
            modifier.accept(entity, state);
        }
    }

    private static synchronized List<BiConsumer<Object, Object>> collect(Class<?> rendererClass) {
        List<BiConsumer<Object, Object>> list = new ArrayList<>();
        for (Entry entry : ENTITY) {
            if (entry.baseRenderer.isAssignableFrom(rendererClass)) {
                list.add(entry.modifier);
            }
        }
        return List.copyOf(list);
    }

    private record Entry(Class<?> baseRenderer, BiConsumer<Object, Object> modifier) {
    }
}
