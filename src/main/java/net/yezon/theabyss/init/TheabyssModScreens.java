package net.yezon.theabyss.init;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.gui.AbyssChestScreen;
import net.yezon.theabyss.client.gui.ArcaneScreen;
import net.yezon.theabyss.client.gui.CutterScreen;
import net.yezon.theabyss.client.gui.ExtractorScreen;
import net.yezon.theabyss.client.gui.InfuserScreen;
import net.yezon.theabyss.client.gui.MortarScreen;
import net.yezon.theabyss.client.gui.ResearchScreen;

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
}
