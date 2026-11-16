package net.yezon.theabyss.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.FriendleyFireAMessage;
import net.yezon.theabyss.network.FriendleyFireBMessage;
import net.yezon.theabyss.network.GhostAttackMessage;
import net.yezon.theabyss.network.GhostDeactivateMessage;
import net.yezon.theabyss.network.GhostFireAttackMessage;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public class TheabyssModKeyMappings {
    public static final KeyMapping.Category ABYSS_CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "abyss"));

    public static final KeyMapping GHOST_ATTACK = new PressReleaseKeyMapping(
        "key.theabyss.ghost_attack", InputConstants.KEY_X, ABYSS_CATEGORY, GhostAttackMessage::new, GhostAttackMessage::pressAction
    );
    public static final KeyMapping GHOST_FIRE_ATTACK = new PressReleaseKeyMapping(
        "key.theabyss.ghost_fire_attack", InputConstants.KEY_C, ABYSS_CATEGORY, GhostFireAttackMessage::new, GhostFireAttackMessage::pressAction
    );
    public static final KeyMapping GHOST_DEACTIVATE = new PressReleaseKeyMapping(
        "key.theabyss.ghost_deactivate", InputConstants.KEY_V, ABYSS_CATEGORY, GhostDeactivateMessage::new, GhostDeactivateMessage::pressAction
    );
    public static final KeyMapping FRIENDLEY_FIRE_A = new PressKeyMapping(
        "key.theabyss.friendley_fire_a", InputConstants.KEY_RIGHT, ABYSS_CATEGORY, FriendleyFireAMessage::new, FriendleyFireAMessage::pressAction
    );
    public static final KeyMapping FRIENDLEY_FIRE_B = new PressKeyMapping(
        "key.theabyss.friendley_fire_b", InputConstants.KEY_LEFT, ABYSS_CATEGORY, FriendleyFireBMessage::new, FriendleyFireBMessage::pressAction
    );
    public static final KeyMapping ABIL_DEBUG = new KeyMapping("key.theabyss.abil_debug", InputConstants.KEY_DOWN, KeyMapping.Category.MISC);
    public static final KeyMapping ABYSS_ECHOES = new KeyMapping(
        "key.theabyss.abyss_echoes", InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_MIDDLE, ABYSS_CATEGORY
    );
    public static final KeyMapping MAIN_MAGIC = new KeyMapping(
        "key.theabyss.main_magic", InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_RIGHT, ABYSS_CATEGORY
    );
    public static final KeyMapping SPELL_WHEEL = new KeyMapping(
        "key.theabyss.spell_wheel", net.neoforged.neoforge.client.settings.KeyConflictContext.IN_GAME,
        net.neoforged.neoforge.client.settings.KeyModifier.NONE, InputConstants.Type.KEYBOARD, InputConstants.KEY_R, ABYSS_CATEGORY
    );
    public static final KeyMapping RIFT_OF_SINS = new KeyMapping("key.theabyss.rift_of_sins", InputConstants.KEY_B, ABYSS_CATEGORY);
    public static final KeyMapping DIALOG_CONTINUE = new KeyMapping("key.theabyss.dialog_continue", InputConstants.KEY_G, ABYSS_CATEGORY);
    public static final KeyMapping WRAITH_FORM = new KeyMapping("key.theabyss.wraith_form", InputConstants.KEY_LALT, ABYSS_CATEGORY);

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(ABYSS_CATEGORY);
        event.register(GHOST_ATTACK);
        event.register(GHOST_FIRE_ATTACK);
        event.register(GHOST_DEACTIVATE);
        event.register(FRIENDLEY_FIRE_A);
        event.register(FRIENDLEY_FIRE_B);
        event.register(ABIL_DEBUG);
        event.register(ABYSS_ECHOES);
        event.register(MAIN_MAGIC);
        event.register(SPELL_WHEEL);
        event.register(RIFT_OF_SINS);
        event.register(DIALOG_CONTINUE);
        event.register(WRAITH_FORM);
    }

    private static boolean mainMagicOwnKey() {
        return !MAIN_MAGIC.same(Minecraft.getInstance().options.keyUse);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().gui.screen() == null) {
            GHOST_ATTACK.consumeClick();
            GHOST_FIRE_ATTACK.consumeClick();
            GHOST_DEACTIVATE.consumeClick();
            FRIENDLEY_FIRE_A.consumeClick();
            FRIENDLEY_FIRE_B.consumeClick();
            var player = Minecraft.getInstance().player;
            while (ABYSS_ECHOES.consumeClick()) {
                if (player != null && player.getMainHandItem().is(net.yezon.theabyss.init.TheabyssModItems.ABYSS_SWORD.get())) {
                    net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new net.yezon.theabyss.network.AbyssEchoesMessage());
                } else if (player != null && net.yezon.theabyss.ring.RingMagic.magicHand(player) != null) {
                    ClientPacketDistributor.sendToServer(new net.yezon.theabyss.network.GauntletMessage(net.yezon.theabyss.network.GauntletMessage.SECOND, 0));
                }
            }
            while (MAIN_MAGIC.consumeClick()) {
                if (mainMagicOwnKey() && player != null && net.yezon.theabyss.ring.RingMagic.magicHand(player) != null
                    && !net.yezon.theabyss.client.fx.SpellWheelClient.isOpen()) {
                    ClientPacketDistributor.sendToServer(new net.yezon.theabyss.network.GauntletMessage(net.yezon.theabyss.network.GauntletMessage.MAIN, 0));
                }
            }
            while (RIFT_OF_SINS.consumeClick()) {
                if (player != null) {
                    ClientPacketDistributor.sendToServer(new net.yezon.theabyss.network.RiftOfSinsMessage());
                }
            }
            SPELL_WHEEL.consumeClick();
        }
    }

    @SubscribeEvent
    public static void onInteractionKey(net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered event) {
        var player = Minecraft.getInstance().player;
        boolean magic = player != null && net.yezon.theabyss.ring.RingMagic.magicHand(player) != null;
        if (event.isPickBlock() && player != null && (player.getMainHandItem().is(net.yezon.theabyss.init.TheabyssModItems.ABYSS_SWORD.get()) || magic)
            && ABYSS_ECHOES.same(Minecraft.getInstance().options.keyPickItem)) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
        if (event.isUseItem() && player != null && mainMagicOwnKey()) {
            var held = player.getItemInHand(event.getHand());
            if (net.yezon.theabyss.ring.Gauntlet.isRing(held) || net.yezon.theabyss.ring.Gauntlet.is(held)) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        }
    }

    @FunctionalInterface
    private interface MessageFactory {
        CustomPacketPayload create(int eventType, int pressedms);
    }

    @FunctionalInterface
    private interface PressAction {
        void run(Player player, int eventType, int pressedms);
    }

    private static class PressKeyMapping extends KeyMapping {
        private final MessageFactory message;
        private final PressAction action;
        private boolean isDownOld;

        PressKeyMapping(String name, int key, KeyMapping.Category category, MessageFactory message, PressAction action) {
            super(name, key, category);
            this.message = message;
            this.action = action;
        }

        @Override
        public void setDown(boolean isDown) {
            super.setDown(isDown);
            if (isDown != this.isDownOld) {
                if (isDown) {
                    this.onPress();
                } else {
                    this.onRelease();
                }
            }

            this.isDownOld = isDown;
        }

        protected void onPress() {
            this.send(0, 0);
        }

        protected void onRelease() {
        }

        protected final void send(int eventType, int pressedms) {
            ClientPacketDistributor.sendToServer(this.message.create(eventType, pressedms));
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                this.action.run(player, eventType, pressedms);
            }
        }
    }

    private static class PressReleaseKeyMapping extends PressKeyMapping {
        private long lastPress;

        PressReleaseKeyMapping(String name, int key, KeyMapping.Category category, MessageFactory message, PressAction action) {
            super(name, key, category, message, action);
        }

        @Override
        protected void onPress() {
            super.onPress();
            this.lastPress = System.currentTimeMillis();
        }

        @Override
        protected void onRelease() {
            this.send(1, (int)(System.currentTimeMillis() - this.lastPress));
        }
    }
}
