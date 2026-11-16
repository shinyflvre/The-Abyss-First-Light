package net.yezon.theabyss.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.renderer.AbyssChestBoatRenderer;
import net.yezon.theabyss.init.TheabyssModWoodSets;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class WoodSetClient {
    private WoodSetClient() {
    }

    static ModelLayerLocation boatLayer(String wood) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "boat/" + wood), "main");
    }

    @SubscribeEvent
    public static void onLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (TheabyssModWoodSets.WoodSet set : TheabyssModWoodSets.ALL) {
            event.registerLayerDefinition(boatLayer(set.name()), BoatModel::createBoatModel);
        }
    }

    @SubscribeEvent
    public static void onRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (TheabyssModWoodSets.WoodSet set : TheabyssModWoodSets.ALL) {
            ModelLayerLocation boat = boatLayer(set.name());
            event.registerEntityRenderer(set.boat().get(), context -> new BoatRenderer(context, boat));
            event.registerEntityRenderer(set.chestBoat().get(), context -> new AbyssChestBoatRenderer(context, boat, set.name()));
        }
    }
}
