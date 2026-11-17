package net.yezon.theabyss.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.BoatModel;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.EntityRenderersEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.renderer.AbyssBoatRenderer;
import net.yezon.theabyss.client.renderer.AbyssChestBoatRenderer;
import net.yezon.theabyss.init.TheabyssModWoodSets;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class WoodSetClient {
    private WoodSetClient() {
    }

    static ModelLayerLocation boatLayer(String wood) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "boat/" + wood), "main");
    }

    @SubscribeEvent
    public static void onLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (TheabyssModWoodSets.WoodSet set : TheabyssModWoodSets.ALL) {
            event.registerLayerDefinition(boatLayer(set.name()), BoatModel::createBodyModel);
        }
    }

    @SubscribeEvent
    public static void onRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (TheabyssModWoodSets.WoodSet set : TheabyssModWoodSets.ALL) {
            ModelLayerLocation boat = boatLayer(set.name());
            event.registerEntityRenderer(set.boat().get(), context -> new AbyssBoatRenderer(context, boat));
            event.registerEntityRenderer(set.chestBoat().get(), context -> new AbyssChestBoatRenderer(context, boat, set.name()));
        }
    }
}
