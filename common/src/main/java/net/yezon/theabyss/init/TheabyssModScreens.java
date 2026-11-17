package net.yezon.theabyss.init;

import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.RegisterMenuScreensEvent;
import net.yezon.theabyss.platform.client.event.RegisterShadersEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.gui.AbyssChestScreen;
import net.yezon.theabyss.client.gui.ArcaneScreen;
import net.yezon.theabyss.client.gui.CutterScreen;
import net.yezon.theabyss.client.gui.ExtractorScreen;
import net.yezon.theabyss.client.gui.InfuserScreen;
import net.yezon.theabyss.client.gui.MortarScreen;
import net.yezon.theabyss.client.gui.ResearchScreen;
import net.yezon.theabyss.client.gui.SkillTreeScreen;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public class TheabyssModScreens {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(TheabyssModMenus.INFUSER.get(), InfuserScreen::new);
        event.register(TheabyssModMenus.CUTTER.get(), CutterScreen::new);
        event.register(TheabyssModMenus.ARCANE.get(), ArcaneScreen::new);
        event.register(TheabyssModMenus.RESEARCH.get(), ResearchScreen::new);
        event.register(TheabyssModMenus.EXTRACTOR.get(), ExtractorScreen::new);
        event.register(TheabyssModMenus.MORTAR.get(), MortarScreen::new);
        event.register(TheabyssModMenus.GAUNTLET_STATION.get(), net.yezon.theabyss.client.gui.GauntletStationScreen::new);
        event.register(TheabyssModMenus.ABYSS_CHEST.get(), AbyssChestScreen::new);
        event.register(TheabyssModMenus.ABYSS_CHEST_DOUBLE.get(), AbyssChestScreen::new);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        SkillTreeScreen.registerShader(event);
    }
}
