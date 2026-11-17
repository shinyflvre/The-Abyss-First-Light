package net.yezon.theabyss.init;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.model.ModelPhantom;
import net.yezon.theabyss.client.model.ModelRokaEntityModel;
import net.yezon.theabyss.client.model.Modelabysaurus_rex;
import net.yezon.theabyss.client.model.Modelabyss_lurker;
import net.yezon.theabyss.client.model.Modelabyss_raptor;
import net.yezon.theabyss.client.model.Modelabyss_wolf;
import net.yezon.theabyss.client.model.Modelabyssal_lion;
import net.yezon.theabyss.client.model.Modelancient_seeker;
import net.yezon.theabyss.client.model.Modelbrain;
import net.yezon.theabyss.client.model.Modelcrystal_golem_big;
import net.yezon.theabyss.client.model.Modeldeer;
import net.yezon.theabyss.client.model.Modeldragonfly;
import net.yezon.theabyss.client.model.Modelelder_entity;
import net.yezon.theabyss.client.model.Modelfish1;
import net.yezon.theabyss.client.model.Modelfish2;
import net.yezon.theabyss.client.model.Modelfish3;
import net.yezon.theabyss.client.model.Modelfrost_spider;
import net.yezon.theabyss.client.model.Modelglow_pug;
import net.yezon.theabyss.client.model.Modelhylia_fox;
import net.yezon.theabyss.client.model.Modelinfected_jellyfish;
import net.yezon.theabyss.client.model.Modelmagic;
import net.yezon.theabyss.client.model.Modelmagician;
import net.yezon.theabyss.client.model.Modelscorpion;
import net.yezon.theabyss.client.model.Modelskeleton_abyss;
import net.yezon.theabyss.client.model.Modelslice;
import net.yezon.theabyss.client.model.Modelslime_spider;
import net.yezon.theabyss.client.model.Modelsoul_lizard;
import net.yezon.theabyss.client.model.Modelwhale;
import net.yezon.theabyss.client.model.Modelzombie1;

@EventBusSubscriber(value = Dist.CLIENT)
public class TheabyssModModels {
    public static final ModelLayerLocation HUMANOID = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "humanoid"), "main");
    public static final ModelLayerLocation INFECTED_COW = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "infected_cow"), "main");

    @SubscribeEvent
    public static void registerLayerDefinitions(RegisterLayerDefinitions event) {
        event.registerLayerDefinition(Modelfish3.LAYER_LOCATION, Modelfish3::createBodyLayer);
        event.registerLayerDefinition(ModelRokaEntityModel.LAYER_LOCATION, ModelRokaEntityModel::createBodyLayer);
        event.registerLayerDefinition(Modelcrystal_golem_big.LAYER_LOCATION, Modelcrystal_golem_big::createBodyLayer);
        event.registerLayerDefinition(Modelsoul_lizard.LAYER_LOCATION, Modelsoul_lizard::createBodyLayer);
        event.registerLayerDefinition(Modelelder_entity.LAYER_LOCATION, Modelelder_entity::createBodyLayer);
        event.registerLayerDefinition(Modelbrain.LAYER_LOCATION, Modelbrain::createBodyLayer);
        event.registerLayerDefinition(Modelslice.LAYER_LOCATION, Modelslice::createBodyLayer);
        event.registerLayerDefinition(Modeldeer.LAYER_LOCATION, Modeldeer::createBodyLayer);
        event.registerLayerDefinition(Modelabyss_lurker.LAYER_LOCATION, Modelabyss_lurker::createBodyLayer);
        event.registerLayerDefinition(Modelwhale.LAYER_LOCATION, Modelwhale::createBodyLayer);
        event.registerLayerDefinition(Modelmagician.LAYER_LOCATION, Modelmagician::createBodyLayer);
        event.registerLayerDefinition(Modelskeleton_abyss.LAYER_LOCATION, Modelskeleton_abyss::createBodyLayer);
        event.registerLayerDefinition(Modelhylia_fox.LAYER_LOCATION, Modelhylia_fox::createBodyLayer);
        event.registerLayerDefinition(Modelabyss_raptor.LAYER_LOCATION, Modelabyss_raptor::createBodyLayer);
        event.registerLayerDefinition(Modeldragonfly.LAYER_LOCATION, Modeldragonfly::createBodyLayer);
        event.registerLayerDefinition(Modelancient_seeker.LAYER_LOCATION, Modelancient_seeker::createBodyLayer);
        event.registerLayerDefinition(Modelscorpion.LAYER_LOCATION, Modelscorpion::createBodyLayer);
        event.registerLayerDefinition(Modelabyss_wolf.LAYER_LOCATION, Modelabyss_wolf::createBodyLayer);
        event.registerLayerDefinition(Modelfish2.LAYER_LOCATION, Modelfish2::createBodyLayer);
        event.registerLayerDefinition(Modelmagic.LAYER_LOCATION, Modelmagic::createBodyLayer);
        event.registerLayerDefinition(Modelinfected_jellyfish.LAYER_LOCATION, Modelinfected_jellyfish::createBodyLayer);
        event.registerLayerDefinition(ModelPhantom.LAYER_LOCATION, ModelPhantom::createBodyLayer);
        event.registerLayerDefinition(Modelabyssal_lion.LAYER_LOCATION, Modelabyssal_lion::createBodyLayer);
        event.registerLayerDefinition(Modelfrost_spider.LAYER_LOCATION, Modelfrost_spider::createBodyLayer);
        event.registerLayerDefinition(Modelglow_pug.LAYER_LOCATION, Modelglow_pug::createBodyLayer);
        event.registerLayerDefinition(Modelzombie1.LAYER_LOCATION, Modelzombie1::createBodyLayer);
        event.registerLayerDefinition(Modelabysaurus_rex.LAYER_LOCATION, Modelabysaurus_rex::createBodyLayer);
        event.registerLayerDefinition(Modelslime_spider.LAYER_LOCATION, Modelslime_spider::createBodyLayer);
        event.registerLayerDefinition(Modelfish1.LAYER_LOCATION, Modelfish1::createBodyLayer);
        event.registerLayerDefinition(HUMANOID, TheabyssModModels::createHumanoidLayer);
        event.registerLayerDefinition(INFECTED_COW, TheabyssModModels::createInfectedCowLayer);
    }

    private static LayerDefinition createHumanoidLayer() {
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, false);
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static LayerDefinition createInfectedCowLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F)
                .texOffs(22, 0)
                .addBox("right_horn", -5.0F, -5.0F, -4.0F, 1.0F, 3.0F, 1.0F)
                .texOffs(22, 0)
                .addBox("left_horn", 4.0F, -5.0F, -4.0F, 1.0F, 3.0F, 1.0F),
            PartPose.offset(0.0F, 4.0F, -8.0F)
        );
        root.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(18, 4)
                .addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F)
                .texOffs(52, 0)
                .addBox(-2.0F, 2.0F, -8.0F, 4.0F, 6.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        CubeListBuilder leftLeg = CubeListBuilder.create().mirror().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F);
        CubeListBuilder rightLeg = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F);
        root.addOrReplaceChild("right_hind_leg", rightLeg, PartPose.offset(-4.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("left_hind_leg", leftLeg, PartPose.offset(4.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("right_front_leg", rightLeg, PartPose.offset(-4.0F, 12.0F, -5.0F));
        root.addOrReplaceChild("left_front_leg", leftLeg, PartPose.offset(4.0F, 12.0F, -5.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }
}
