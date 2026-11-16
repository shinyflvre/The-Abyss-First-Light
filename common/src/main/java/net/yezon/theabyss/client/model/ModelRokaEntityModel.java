package net.yezon.theabyss.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class ModelRokaEntityModel<S extends LivingEntityRenderState> extends EntityModel<S> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("theabyss", "model_roka_entity_model"), "main");
    public final ModelPart Head;
    public final ModelPart RightArm;
    public final ModelPart LeftArm;
    public final ModelPart RightLeg;
    public final ModelPart LeftLeg;
    public final ModelPart bb_main;

    public ModelRokaEntityModel(ModelPart root) {
        super(root);
        this.Head = root.getChild("Head");
        this.RightArm = root.getChild("RightArm");
        this.LeftArm = root.getChild("LeftArm");
        this.RightLeg = root.getChild("RightLeg");
        this.LeftLeg = root.getChild("LeftLeg");
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition Head = partdefinition.addOrReplaceChild(
            "Head",
            CubeListBuilder.create()
                .texOffs(0, 27)
                .addBox(-9.0F, -16.0833F, -7.9167F, 18.0F, 16.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(51, 27)
                .addBox(4.0F, -7.0833F, -9.9167F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(26, 58)
                .addBox(-4.0F, -8.0833F, -9.9167F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(51, 34)
                .addBox(-6.0F, -7.0833F, -9.9167F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0)
                .addBox(4.0F, -26.0833F, -5.9167F, 2.0F, 10.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 27)
                .addBox(-6.0F, -26.0833F, -5.9167F, 2.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -41.9167F, -0.0833F)
        );
        PartDefinition LeftEar = Head.addOrReplaceChild(
            "LeftEar",
            CubeListBuilder.create().texOffs(21, 97).addBox(0.0F, 0.0F, -5.5F, 2.0F, 10.0F, 11.0F, new CubeDeformation(0.0F)),
            PartPose.offset(9.0F, -12.0833F, -0.4167F)
        );
        PartDefinition RightEar = Head.addOrReplaceChild(
            "RightEar",
            CubeListBuilder.create().texOffs(84, 94).addBox(-2.0F, 0.0F, -5.5F, 2.0F, 10.0F, 11.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-9.0F, -12.0833F, -0.4167F)
        );
        PartDefinition RightArm = partdefinition.addOrReplaceChild(
            "RightArm",
            CubeListBuilder.create()
                .texOffs(36, 70)
                .addBox(-6.0F, -3.5F, -3.0F, 6.0F, 32.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(84, 76)
                .addBox(-7.0F, 14.5F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-13.0F, -38.5F, 0.0F)
        );
        PartDefinition LeftArm = partdefinition.addOrReplaceChild(
            "LeftArm",
            CubeListBuilder.create()
                .texOffs(60, 76)
                .addBox(0.1667F, -4.0F, -3.0F, 6.0F, 32.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 85)
                .addBox(-0.8333F, 14.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(78, 0)
                .addBox(0.1667F, -5.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(12.8333F, -38.0F, 0.0F)
        );
        PartDefinition RightLeg = partdefinition.addOrReplaceChild(
            "RightLeg",
            CubeListBuilder.create().texOffs(0, 58).addBox(-4.0F, -0.25F, -5.0F, 8.0F, 17.0F, 10.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-5.0F, -9.75F, 2.0F)
        );
        PartDefinition RightLeg2 = RightLeg.addOrReplaceChild(
            "RightLeg2",
            CubeListBuilder.create().texOffs(104, 16).addBox(-3.0F, 0.0F, -2.5F, 6.0F, 17.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 16.75F, 2.5F)
        );
        PartDefinition LeftLeg = partdefinition.addOrReplaceChild(
            "LeftLeg",
            CubeListBuilder.create().texOffs(68, 17).addBox(-4.0F, 0.0F, -5.0F, 8.0F, 17.0F, 10.0F, new CubeDeformation(0.0F)),
            PartPose.offset(5.0F, -10.0F, 2.0F)
        );
        PartDefinition LeftLeg2 = LeftLeg.addOrReplaceChild(
            "LeftLeg2",
            CubeListBuilder.create().texOffs(108, 38).addBox(-3.0F, 0.0F, -2.5F, 6.0F, 17.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 17.0F, 2.5F)
        );
        PartDefinition bb_main = partdefinition.addOrReplaceChild(
            "bb_main",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-13.0F, -66.0F, -6.0F, 26.0F, 14.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(56, 48)
                .addBox(-8.0F, -52.0F, -4.0F, 16.0F, 18.0F, 10.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 24.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float limbSwing = state.walkAnimationPos;
        float limbSwingAmount = state.walkAnimationSpeed;
        float netHeadYaw = state.yRot;
        float headPitch = state.xRot;
        this.RightArm.xRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;
        this.LeftLeg.xRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;
        this.Head.yRot = netHeadYaw / (180.0F / (float)Math.PI);
        this.Head.xRot = headPitch / (180.0F / (float)Math.PI);
        this.LeftArm.xRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;
        this.RightLeg.xRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;
    }
}
