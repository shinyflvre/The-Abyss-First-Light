package net.yezon.theabyss.client.compat;

import java.lang.reflect.Method;
import java.util.function.IntFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.fx.SpellWheelClient;
import net.yezon.theabyss.compat.ObscureSkillsCompat;
import net.yezon.theabyss.init.TheabyssModKeyMappings;
import net.yezon.theabyss.network.GauntletMessage;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.Platform;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.client.network.ClientPacketDistributor;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.fml.event.lifecycle.FMLClientSetupEvent;
import net.yezon.theabyss.ring.RingMagic;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class ObscureSkillsClientCompat {
    private static final String API = "com.shinyflvres.obscureskills.api.client.ObscureSkillsClientApi";
    private static @Nullable Boolean present;
    private static @Nullable Method castSpell;
    private static @Nullable Method name;
    private static @Nullable Method colour;
    private static @Nullable Method icon;
    private static @Nullable Method cooldown;
    private static @Nullable Method value;
    private static @Nullable Method mana;
    private static @Nullable Method damage;
    private static @Nullable Method setCastLabel;
    private static int selected = -1;

    private ObscureSkillsClientCompat() {
    }

    public static boolean present() {
        if (present == null) {
            boolean ok = false;
            try {
                if (Platform.get().isModLoaded(ObscureSkillsCompat.MODID)) {
                    Class<?> api = Class.forName(API);
                    castSpell = api.getMethod("castSpell", int.class);
                    name = api.getMethod("name", String.class);
                    colour = api.getMethod("colour", String.class);
                    icon = api.getMethod("icon", String.class);
                    cooldown = api.getMethod("cooldown", String.class);
                    value = api.getMethod("value", String.class, String.class);
                    mana = api.getMethod("mana", String.class);
                    damage = api.getMethod("damage", String.class);
                    setCastLabel = api.getMethod("setCastLabel", IntFunction.class);
                    ok = true;
                }
            } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
                TheabyssMod.LOGGER.warn("Obscure Skills is installed, but its client magic interface was not found", e);
            }
            present = ok;
        }
        return present;
    }

    private static @Nullable Object call(@Nullable Method method, Object... args) {
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(null, args);
        } catch (ReflectiveOperationException | RuntimeException e) {
            TheabyssMod.LOGGER.debug("Obscure Skills call {} failed", method.getName(), e);
            return null;
        }
    }

    public static @Nullable String spell(int slot) {
        if (!present() || slot < 0 || slot >= ObscureSkillsCompat.SLOTS) {
            return null;
        }
        return call(castSpell, slot) instanceof String id && !id.isBlank() ? id : null;
    }

    public static boolean anyAssigned() {
        for (int slot = 0; slot < ObscureSkillsCompat.SLOTS; slot++) {
            if (spell(slot) != null) {
                return true;
            }
        }
        return false;
    }

    public static Component name(String id) {
        return call(name, id) instanceof Component text ? text : Component.literal(id);
    }

    public static int colour(String id) {
        return call(colour, id) instanceof Integer rgb ? rgb : 0xFFFFFF;
    }

    public static @Nullable Identifier icon(String id) {
        return call(icon, id) instanceof Identifier sprite ? sprite : null;
    }

    public static float cooldown(String id) {
        return call(cooldown, id) instanceof Float left ? left : 0.0F;
    }

    public static double cooldownSeconds(String id) {
        return call(value, id, "cooldown") instanceof Double seconds ? seconds : Double.NaN;
    }

    public static double mana(String id) {
        return call(mana, id) instanceof Double amount ? amount : Double.NaN;
    }

    public static double damage(String id) {
        return call(damage, id) instanceof Double amount ? amount : Double.NaN;
    }

    public static int selected() {
        return selected >= 0 && spell(selected) != null ? selected : -1;
    }

    public static void select(int slot) {
        selected = slot >= 0 && spell(slot) != null ? slot : -1;
        ClientPacketDistributor.sendToServer(new GauntletMessage(GauntletMessage.OBSCURE, selected));
    }

    public static void forget() {
        selected = -1;
    }

    public static void onIdleUse(Minecraft mc) {
        if (selected() < 0 || mc.player == null || mc.gameMode == null || mc.gui.screen() != null || mc.player.isSpectator()) {
            return;
        }
        if (mc.gameMode.isDestroying() || mc.player.isHandsBusy() || SpellWheelClient.isOpen() || RingMagic.magicHand(mc.player) != null) {
            return;
        }
        if (!TheabyssModKeyMappings.MAIN_MAGIC.same(mc.options.keyUse)) {
            return;
        }
        ClientPacketDistributor.sendToServer(new GauntletMessage(GauntletMessage.MAIN, 0));
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        if (!present()) {
            return;
        }
        IntFunction<Component> label = slot -> Component.translatable("compat.theabyss.obscure_slot", TheabyssModKeyMappings.SPELL_WHEEL.getTranslatedKeyMessage());
        call(setCastLabel, label);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (selected >= 0 && Minecraft.getInstance().player == null) {
            selected = -1;
        }
    }
}
