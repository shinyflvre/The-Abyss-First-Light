package net.yezon.theabyss.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.world.inventory.ArcaneMenu;
import net.yezon.theabyss.world.inventory.CutterMenu;
import net.yezon.theabyss.world.inventory.ExtractorMenu;
import net.yezon.theabyss.world.inventory.InfuserMenu;
import net.yezon.theabyss.world.inventory.MortarMenu;
import net.yezon.theabyss.world.inventory.ResearchMenu;

public class TheabyssModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, TheabyssMod.MODID);
    public static final DeferredHolder<MenuType<?>, MenuType<InfuserMenu>> INFUSER = REGISTRY.register("infuser", () -> IMenuTypeExtension.create(InfuserMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<CutterMenu>> CUTTER = REGISTRY.register("cutter", () -> IMenuTypeExtension.create(CutterMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ArcaneMenu>> ARCANE = REGISTRY.register("arcane", () -> IMenuTypeExtension.create(ArcaneMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ResearchMenu>> RESEARCH = REGISTRY.register("research", () -> IMenuTypeExtension.create(ResearchMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtractorMenu>> EXTRACTOR = REGISTRY.register("extractor", () -> IMenuTypeExtension.create(ExtractorMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<MortarMenu>> MORTAR = REGISTRY.register("mortar", () -> IMenuTypeExtension.create(MortarMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ChestMenu>> ABYSS_CHEST = REGISTRY.register("abyss_chest",
        () -> IMenuTypeExtension.create((id, inventory, buf) -> new ChestMenu(TheabyssModMenus.ABYSS_CHEST.get(), id, inventory, new SimpleContainer(27), 3)));
    public static final DeferredHolder<MenuType<?>, MenuType<ChestMenu>> ABYSS_CHEST_DOUBLE = REGISTRY.register("abyss_chest_double",
        () -> IMenuTypeExtension.create((id, inventory, buf) -> new ChestMenu(TheabyssModMenus.ABYSS_CHEST_DOUBLE.get(), id, inventory, new SimpleContainer(54), 6)));
    public static final DeferredHolder<MenuType<?>, MenuType<net.yezon.theabyss.world.inventory.GauntletStationMenu>> GAUNTLET_STATION = REGISTRY.register("gauntlet_station", () -> IMenuTypeExtension.create(net.yezon.theabyss.world.inventory.GauntletStationMenu::new));
}
