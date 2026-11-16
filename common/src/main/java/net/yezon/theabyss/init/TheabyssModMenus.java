package net.yezon.theabyss.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.platform.services.ServerHooks;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.world.inventory.ArcaneMenu;
import net.yezon.theabyss.world.inventory.CutterMenu;
import net.yezon.theabyss.world.inventory.ExtractorMenu;
import net.yezon.theabyss.world.inventory.InfuserMenu;
import net.yezon.theabyss.world.inventory.MortarMenu;
import net.yezon.theabyss.world.inventory.ResearchMenu;

public class TheabyssModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, TheabyssMod.MODID);
    public static final DeferredHolder<MenuType<?>, MenuType<InfuserMenu>> INFUSER = REGISTRY.register("infuser", () -> ServerHooks.get().createMenuType(InfuserMenu::new, true));
    public static final DeferredHolder<MenuType<?>, MenuType<CutterMenu>> CUTTER = REGISTRY.register("cutter", () -> ServerHooks.get().createMenuType(CutterMenu::new, true));
    public static final DeferredHolder<MenuType<?>, MenuType<ArcaneMenu>> ARCANE = REGISTRY.register("arcane", () -> ServerHooks.get().createMenuType(ArcaneMenu::new, true));
    public static final DeferredHolder<MenuType<?>, MenuType<ResearchMenu>> RESEARCH = REGISTRY.register("research", () -> ServerHooks.get().createMenuType(ResearchMenu::new, true));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtractorMenu>> EXTRACTOR = REGISTRY.register("extractor", () -> ServerHooks.get().createMenuType(ExtractorMenu::new, true));
    public static final DeferredHolder<MenuType<?>, MenuType<MortarMenu>> MORTAR = REGISTRY.register("mortar", () -> ServerHooks.get().createMenuType(MortarMenu::new, true));
    public static final DeferredHolder<MenuType<?>, MenuType<ChestMenu>> ABYSS_CHEST = REGISTRY.register("abyss_chest",
        () -> ServerHooks.get().createMenuType((id, inventory, buf) -> new ChestMenu(TheabyssModMenus.ABYSS_CHEST.get(), id, inventory, new SimpleContainer(27), 3), false));
    public static final DeferredHolder<MenuType<?>, MenuType<ChestMenu>> ABYSS_CHEST_DOUBLE = REGISTRY.register("abyss_chest_double",
        () -> ServerHooks.get().createMenuType((id, inventory, buf) -> new ChestMenu(TheabyssModMenus.ABYSS_CHEST_DOUBLE.get(), id, inventory, new SimpleContainer(54), 6), false));
    public static final DeferredHolder<MenuType<?>, MenuType<net.yezon.theabyss.world.inventory.GauntletStationMenu>> GAUNTLET_STATION = REGISTRY.register("gauntlet_station", () -> ServerHooks.get().createMenuType(net.yezon.theabyss.world.inventory.GauntletStationMenu::new, false));
}
