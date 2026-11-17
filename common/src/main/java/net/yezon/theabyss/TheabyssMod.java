package net.yezon.theabyss;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.yezon.theabyss.astral.AstralData;
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
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.tick.ServerTickEvent;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.possession.PossessionData;
import net.yezon.theabyss.world.features.StructureFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class TheabyssMod {
    public static final Logger LOGGER = LogManager.getLogger(TheabyssMod.class);
    public static final String MODID = "theabyss";
    private static final Collection<SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

    private TheabyssMod() {
    }

    public static List<DeferredRegister<?>> init() {
        net.yezon.theabyss.init.NightgroveAliases.register();
        net.yezon.theabyss.init.TheabyssModWoodSets.init();
        net.yezon.theabyss.init.TheabyssModTreeSets.init();
        net.yezon.theabyss.init.TheabyssModFlora.init();
        net.yezon.theabyss.init.TheabyssModChests.init();
        List<DeferredRegister<?>> registers = List.of(
            TheabyssModSounds.REGISTRY,
            TheabyssModBlocks.REGISTRY,
            TheabyssModBlockEntities.REGISTRY,
            TheabyssModItems.REGISTRY,
            net.yezon.theabyss.item.TheabyssModArmorMaterials.REGISTRY,
            TheabyssModEntities.REGISTRY,
            TheabyssModTabs.REGISTRY,
            TheabyssModFeatures.REGISTRY,
            StructureFeature.REGISTRY,
            net.yezon.theabyss.world.features.HangingDecoratedFeature.REGISTRY,
            net.yezon.theabyss.world.features.TriplePlantFeature.REGISTRY,
            net.yezon.theabyss.world.features.HangingPlantFeature.REGISTRY,
            net.yezon.theabyss.world.features.PlantPatchFeature.REGISTRY,
            net.yezon.theabyss.world.features.PlantClusterFeature.REGISTRY,
            net.yezon.theabyss.world.features.LeafBushFeature.REGISTRY,
            net.yezon.theabyss.world.features.ShroomTreeFeature.REGISTRY,
            net.yezon.theabyss.world.features.VigilantTreeFeature.REGISTRY,
            net.yezon.theabyss.world.features.BogTreeFeature.REGISTRY,
            net.yezon.theabyss.world.features.BiomePathProcessor.REGISTRY,
            net.yezon.theabyss.world.structures.GiantArchStructure.TYPES,
            net.yezon.theabyss.world.structures.GiantArchStructure.PIECES,
            net.yezon.theabyss.pocket.PocketChunkGenerator.REGISTRY,
            net.yezon.theabyss.world.biome.RingBiomeSource.REGISTRY,
            TheabyssModMobEffects.REGISTRY,
            TheabyssModParticleTypes.REGISTRY,
            TheabyssModMenus.REGISTRY,
            TheabyssModFluids.REGISTRY,
            TheabyssModFluidTypes.REGISTRY,
            TheabyssModRecipeTypes.SERIALIZERS,
            TheabyssModRecipeTypes.TYPES,
            TheabyssModVariables.ATTACHMENT_TYPES,
            PossessionData.ATTACHMENTS,
            AstralData.ATTACHMENTS,
            net.yezon.theabyss.lore.ChronicleData.ATTACHMENTS,
            net.yezon.theabyss.item.KeptGuide.ATTACHMENTS,
            net.yezon.theabyss.gravity.Gravity.ATTACHMENTS
        );
        for (Class<?> subscriber : TheabyssSubscribers.ALL) {
            AbyssEventBus.register(subscriber);
        }
        return registers;
    }

    public static void queueServerWork(int tick, Runnable action) {
        workQueue.add(new SimpleEntry<>(action, tick));
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
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
