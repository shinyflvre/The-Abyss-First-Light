package net.yezon.theabyss.platform.event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class AbyssEventBus {
    private static final Map<Class<?>, List<Listener>> OWN = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Listener[]> RESOLVED = new ConcurrentHashMap<>();
    private static final Listener[] NONE = new Listener[0];

    private AbyssEventBus() {
    }

    public static void register(Class<?> type) {
        for (Method method : type.getDeclaredMethods()) {
            SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
            if (annotation == null) {
                continue;
            }
            Class<?>[] params = method.getParameterTypes();
            if (!Modifier.isStatic(method.getModifiers()) || params.length != 1 || !Event.class.isAssignableFrom(params[0])) {
                throw new IllegalArgumentException("Invalid @SubscribeEvent method " + type.getName() + "#" + method.getName());
            }
            MethodHandle handle;
            try {
                method.setAccessible(true);
                handle = MethodHandles.lookup().unreflect(method);
            } catch (ReflectiveOperationException | RuntimeException e) {
                throw new IllegalStateException("Cannot access " + type.getName() + "#" + method.getName(), e);
            }
            String name = type.getName() + "#" + method.getName();
            add(params[0], new Listener(annotation.priority(), annotation.receiveCanceled(), event -> {
                try {
                    handle.invoke(event);
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Throwable e) {
                    throw new IllegalStateException("Event listener " + name + " failed", e);
                }
            }));
        }
    }

    public static <E extends Event> void addListener(Class<E> type, Consumer<E> listener) {
        addListener(type, EventPriority.NORMAL, false, listener);
    }

    @SuppressWarnings("unchecked")
    public static <E extends Event> void addListener(Class<E> type, EventPriority priority, boolean receiveCanceled, Consumer<E> listener) {
        add(type, new Listener(priority, receiveCanceled, event -> listener.accept((E)event)));
    }

    private static synchronized void add(Class<?> type, Listener listener) {
        OWN.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
        RESOLVED.clear();
    }

    public static <E extends Event> E post(E event) {
        for (Listener listener : resolve(event.getClass())) {
            listener.call(event);
        }
        return event;
    }

    public static <E extends Event> E post(EventPriority priority, E event) {
        for (Listener listener : resolve(event.getClass())) {
            if (listener.priority == priority) {
                listener.call(event);
            }
        }
        return event;
    }

    public static boolean hasListeners(Class<? extends Event> type) {
        return resolve(type).length > 0;
    }

    public static Set<EventPriority> priorities(Class<? extends Event> type) {
        Set<EventPriority> set = EnumSet.noneOf(EventPriority.class);
        for (Listener listener : resolve(type)) {
            set.add(listener.priority);
        }
        return set;
    }

    private static Listener[] resolve(Class<?> type) {
        Listener[] cached = RESOLVED.get(type);
        if (cached != null) {
            return cached;
        }
        List<Listener> all = new ArrayList<>();
        synchronized (AbyssEventBus.class) {
            for (EventPriority priority : EventPriority.values()) {
                for (Class<?> c = type; c != null && Event.class.isAssignableFrom(c); c = c.getSuperclass()) {
                    for (Listener listener : OWN.getOrDefault(c, List.of())) {
                        if (listener.priority == priority) {
                            all.add(listener);
                        }
                    }
                }
            }
        }
        Listener[] result = all.isEmpty() ? NONE : all.toArray(new Listener[0]);
        RESOLVED.put(type, result);
        return result;
    }

    private record Listener(EventPriority priority, boolean receiveCanceled, Consumer<Event> handler) {
        void call(Event event) {
            if (!this.receiveCanceled && event.isCanceled()) {
                return;
            }
            this.handler.accept(event);
        }
    }
}
