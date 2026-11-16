package net.yezon.theabyss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.astral.AstralData;
import net.yezon.theabyss.possession.PossessionData;
import net.yezon.theabyss.init.TheabyssModBlockEntities;
import net.yezon.theabyss.init.TheabyssModBlocks;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModFeatures;
import net.yezon.theabyss.init.TheabyssModFluidTypes;
import net.yezon.theabyss.init.TheabyssModFluids;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModMenus;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.init.TheabyssModRecipeTypes;
import net.yezon.theabyss.init.TheabyssModSounds;
import net.yezon.theabyss.init.TheabyssModTabs;
import net.yezon.theabyss.network.TheabyssModNetwork;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.world.features.StructureFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TheabyssMod.MODID)
public class TheabyssMod {
    public static final Logger LOGGER = LogManager.getLogger(TheabyssMod.class);
    public static final String MODID = "theabyss";
    private static final Collection<SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

    public TheabyssMod(IEventBus bus, ModContainer container) {
        NeoForge.EVENT_BUS.register(this);
        net.yezon.theabyss.init.NightgroveAliases.register();
        net.yezon.theabyss.init.TheabyssModWoodSets.init();
        net.yezon.theabyss.init.TheabyssModTreeSets.init();
        net.yezon.theabyss.init.TheabyssModFlora.init();
        net.yezon.theabyss.init.TheabyssModChests.init();
        TheabyssModSounds.REGISTRY.register(bus);
        TheabyssModBlocks.REGISTRY.register(bus);
        TheabyssModBlockEntities.REGISTRY.register(bus);
        TheabyssModItems.REGISTRY.register(bus);
        net.yezon.theabyss.item.AbyssTotemEffect.REGISTRY.register(bus);
        TheabyssModEntities.REGISTRY.register(bus);
        TheabyssModTabs.REGISTRY.register(bus);
        TheabyssModFeatures.REGISTRY.register(bus);
        StructureFeature.REGISTRY.register(bus);
        net.yezon.theabyss.world.features.HangingDecoratedFeature.REGISTRY.register(bus);
        net.yezon.theabyss.world.features.TriplePlantFeature.REGISTRY.register(bus);
        net.yezon.theabyss.world.features.HangingPlantFeature.REGISTRY.register(bus);
        net.yezon.theabyss.world.features.PlantPatchFeature.REGISTRY.register(bus);
        net.yezon.theabyss.world.features.PlantClusterFeature.REGISTRY.register(bus);
        net.yezon.theabyss.world.features.LeafBushFeature.REGISTRY.register(bus);
        net.yezon.theabyss.world.features.ShroomTreeFeature.REGISTRY.register(bus);
        net.yezon.theabyss.world.features.VigilantTreeFeature.REGISTRY.register(bus);
        net.yezon.theabyss.world.features.BogTreeFeature.REGISTRY.register(bus);
        net.yezon.theabyss.world.features.BiomePathProcessor.REGISTRY.register(bus);
        net.yezon.theabyss.world.structures.GiantArchStructure.TYPES.register(bus);
        net.yezon.theabyss.world.structures.GiantArchStructure.PIECES.register(bus);
        net.yezon.theabyss.pocket.PocketChunkGenerator.REGISTRY.register(bus);
        net.yezon.theabyss.world.biome.RingBiomeSource.REGISTRY.register(bus);
        TheabyssModMobEffects.REGISTRY.register(bus);
        TheabyssModParticleTypes.REGISTRY.register(bus);
        TheabyssModMenus.REGISTRY.register(bus);
        TheabyssModFluids.REGISTRY.register(bus);
        TheabyssModFluidTypes.REGISTRY.register(bus);
        TheabyssModRecipeTypes.SERIALIZERS.register(bus);
        TheabyssModRecipeTypes.TYPES.register(bus);
        TheabyssModVariables.ATTACHMENT_TYPES.register(bus);
        PossessionData.ATTACHMENTS.register(bus);
        AstralData.ATTACHMENTS.register(bus);
        net.yezon.theabyss.lore.ChronicleData.ATTACHMENTS.register(bus);
        net.yezon.theabyss.item.KeptGuide.ATTACHMENTS.register(bus);
        net.yezon.theabyss.gravity.Gravity.ATTACHMENTS.register(bus);
        bus.addListener(TheabyssModNetwork::register);
        container.registerConfig(ModConfig.Type.LOCAL, ConfigConfiguration.SPEC, "ta_theotherside.toml");
    }

    public static void queueServerWork(int tick, Runnable action) {
        workQueue.add(new SimpleEntry<>(action, tick));
    }

    @SubscribeEvent
    public void tick(ServerTickEvent.Post event) {
        if (workQueue.isEmpty()) {
            return;
        }
        List<Runnable> due = new ArrayList<>();
        Iterator<SimpleEntry<Runnable, Integer>> iterator = workQueue.iterator();
        while (iterator.hasNext()) {
            SimpleEntry<Runnable, Integer> work = iterator.next();
            int left = work.getValue() - 1;
            if (left <= 0) {
                iterator.remove();
                due.add(work.getKey());
            } else {
                work.setValue(left);
            }
        }
        for (Runnable action : due) {
            try {
                action.run();
            } catch (RuntimeException e) {
                LOGGER.error("Scheduled server work failed", e);
            }
        }
    }
}
