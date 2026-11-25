package net.yezon.theabyss.compat;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.platform.Platform;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.util.RingMode;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class ObscureSkillsCompat {
    public static final String MODID = "obscureskills";
    public static final int SLOTS = 2;
    private static final String API = "com.shinyflvres.obscureskills.api.ObscureSkillsApi";
    private static final Map<UUID, Integer> SELECTED = new HashMap<>();
    private static @Nullable Boolean present;
    private static @Nullable Method castSpell;
    private static @Nullable Method cast;
    private static @Nullable Method addTargetFilter;
    private static boolean filtered;

    private ObscureSkillsCompat() {
    }

    public static boolean present() {
        if (present == null) {
            boolean ok = false;
            try {
                if (Platform.get().isModLoaded(MODID)) {
                    Class<?> api = Class.forName(API);
                    castSpell = api.getMethod("castSpell", ServerPlayer.class, int.class);
                    cast = api.getMethod("cast", ServerPlayer.class, int.class);
                    addTargetFilter = api.getMethod("addTargetFilter", BiPredicate.class);
                    ok = true;
                }
            } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
                TheabyssMod.LOGGER.warn("Obscure Skills is installed, but its magic interface was not found", e);
            }
            present = ok;
        }
        return present;
    }

    private static void install() {
        if (filtered || !present()) {
            return;
        }
        filtered = true;
        BiPredicate<ServerPlayer, Entity> filter = (caster, target) -> !RingMode.spares(caster, target);
        try {
            addTargetFilter.invoke(null, filter);
        } catch (ReflectiveOperationException | RuntimeException e) {
            TheabyssMod.LOGGER.warn("Could not hand the ring mode to Obscure Skills", e);
        }
    }

    public static @Nullable String spell(ServerPlayer player, int slot) {
        if (!present() || slot < 0 || slot >= SLOTS) {
            return null;
        }
        try {
            Object id = castSpell.invoke(null, player, slot);
            return id instanceof String text && !text.isBlank() ? text : null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            TheabyssMod.LOGGER.warn("Could not read the Obscure Skills spell slot", e);
            return null;
        }
    }

    public static void select(ServerPlayer player, int slot) {
        if (slot < 0 || slot >= SLOTS || !present()) {
            SELECTED.remove(player.getUUID());
        } else {
            SELECTED.put(player.getUUID(), slot);
        }
    }

    public static int selected(ServerPlayer player) {
        if (!present()) {
            return -1;
        }
        Integer slot = SELECTED.get(player.getUUID());
        return slot == null || spell(player, slot) == null ? -1 : slot;
    }

    public static boolean castSelected(Player player) {
        if (!(player instanceof ServerPlayer caster)) {
            return false;
        }
        int slot = selected(caster);
        if (slot < 0) {
            return false;
        }
        install();
        try {
            cast.invoke(null, caster, slot);
        } catch (ReflectiveOperationException | RuntimeException e) {
            TheabyssMod.LOGGER.warn("Casting the Obscure Skills spell failed", e);
        }
        return true;
    }

    public static boolean secondaryBlocked(ServerPlayer player) {
        if (selected(player) < 0) {
            return false;
        }
        player.sendOverlayMessage(Component.translatable("gauntlet.theabyss.no_second_spell"));
        return true;
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        install();
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SELECTED.remove(event.getEntity().getUUID());
    }
}
