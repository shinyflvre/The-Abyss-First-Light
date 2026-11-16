package net.yezon.theabyss.client.skill;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.gui.SkillTreeScreen;
import net.yezon.theabyss.init.TheabyssModKeyMappings;
import net.yezon.theabyss.network.SkillActionMessage;
import net.yezon.theabyss.skill.Skill;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class SkillClient {
    private static final int DOUBLE_TAP_TICKS = 7;
    private static final Set<Skill> LEARNED = EnumSet.of(Skill.REFLECTION);
    private static boolean wasDown;
    private static long lastPress = -100L;
    private static int spent;
    private static long ticks;

    private SkillClient() {
    }

    public static void sync(List<String> learned, int spentLevels, boolean open) {
        LEARNED.clear();
        LEARNED.add(Skill.REFLECTION);
        for (String id : learned) {
            Skill skill = Skill.byId(id);
            if (skill != null) {
                LEARNED.add(skill);
            }
        }
        spent = spentLevels;
        if (open) {
            Minecraft.getInstance().gui.setScreen(new SkillTreeScreen());
        }
    }

    public static boolean has(Skill skill) {
        return LEARNED.contains(skill);
    }

    public static boolean available(Skill skill) {
        return !has(skill) && skill.parents().stream().anyMatch(LEARNED::contains);
    }

    public static int spent() {
        return spent;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ticks++;
        if (player == null || mc.gui.screen() != null) {
            wasDown = false;
            return;
        }
        while (TheabyssModKeyMappings.WRAITH_FORM.consumeClick()) {
            if (has(Skill.WRAITH_FORM) && !player.isSpectator()) {
                ClientPacketDistributor.sendToServer(new SkillActionMessage(SkillActionMessage.WRAITH, 0));
            }
        }
        boolean down = sprintHeld(mc);
        if (down && !wasDown) {
            if (ticks - lastPress <= DOUBLE_TAP_TICKS) {
                lastPress = -100L;
                if (has(Skill.SHADOW_DASH) && !player.isSpectator()) {
                    ClientPacketDistributor.sendToServer(new SkillActionMessage(SkillActionMessage.DASH, 0));
                }
            } else {
                lastPress = ticks;
            }
        }
        wasDown = down;
    }

    private static boolean sprintHeld(Minecraft mc) {
        InputConstants.Key key = mc.options.keySprint.getKey();
        if (key.getType() == InputConstants.Type.KEYBOARD && !mc.options.keySprint.isUnbound()) {
            return InputConstants.isKeyDown(key.getValue());
        }
        return mc.options.keySprint.isDown();
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        LEARNED.clear();
        LEARNED.add(Skill.REFLECTION);
        spent = 0;
    }
}
