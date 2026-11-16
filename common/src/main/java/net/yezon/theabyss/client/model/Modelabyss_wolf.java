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

public class Modelabyss_wolf<S extends LivingEntityRenderState> extends EntityModel<S> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("theabyss", "modelabyss_wolf"), "main");
    public final ModelPart right_leg1;
    public final ModelPart left_leg1;
    public final ModelPart right_leg;
    public final ModelPart left_leg;
    public final ModelPart tail;
    public final ModelPart mob_head;
    public final ModelPart body;

    public Modelabyss_wolf(ModelPart root) {
        super(root);
        this.right_leg1 = root.getChild("right_leg1");
        this.left_leg1 = root.getChild("left_leg1");
        this.right_leg = root.getChild("right_leg");
        this.left_leg = root.getChild("left_leg");
        this.tail = root.getChild("tail");
        this.mob_head = root.getChild("mob_head");
        this.body = root.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition right_leg1 = partdefinition.addOrReplaceChild(
            "right_leg1",
            CubeListBuilder.create().texOffs(0, 35).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-2.5F, 17.0F, 8.5F)
        );
        PartDefinition left_leg1 = partdefinition.addOrReplaceChild(
            "left_leg1",
            CubeListBuilder.create().texOffs(44, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(2.5F, 17.0F, 8.5F)
        );
        PartDefinition right_leg = partdefinition.addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create().texOffs(12, 35).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-2.5F, 17.0F, -6.5F)
        );
        PartDefinition left_leg = partdefinition.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(44, 25).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(2.5F, 17.0F, -6.5F)
        );
        PartDefinition tail = partdefinition.addOrReplaceChild(
            "tail",
            CubeListBuilder.create()
                .texOffs(25, 25)
                .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(44, 40)
                .addBox(-2.0F, -2.0F, 11.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 14.0F, 10.0F)
        );
        PartDefinition mob_head = partdefinition.addOrReplaceChild(
            "mob_head",
            CubeListBuilder.create()
                .texOffs(28, 10)
                .addBox(-4.0F, -3.0F, -7.0F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(26, 0)
                .addBox(-2.0F, 0.0F, -12.0F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 12.0F, -8.0F, 0.1309F, 0.0F, 0.0F)
        );
        PartDefinition cube_r1 = mob_head.addOrReplaceChild(
            "cube_r1",
            CubeListBuilder.create().texOffs(24, 35).addBox(0.55F, -4.0F, 2.0F, 0.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-4.0F, 1.0F, -6.0F, 0.0F, -0.2618F, 0.0F)
        );
        PartDefinition cube_r2 = mob_head.addOrReplaceChild(
            "cube_r2",
            CubeListBuilder.create().texOffs(34, 35).addBox(-0.55F, -4.0F, 2.0F, 0.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(4.0F, 1.0F, -6.0F, 0.0F, 0.2618F, 0.0F)
        );
        PartDefinition cube_r3 = mob_head.addOrReplaceChild(
            "cube_r3",
            CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -3.0F, 0.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-2.0F, -3.0F, -2.0F, -0.2182F, 0.0F, -0.48F)
        );
        PartDefinition cube_r4 = mob_head.addOrReplaceChild(
            "cube_r4",
            CubeListBuilder.create().texOffs(0, 4).addBox(0.0F, -3.0F, 0.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(2.0F, -3.0F, -2.0F, -0.2182F, 0.0F, 0.48F)
        );
        PartDefinition body = partdefinition.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(0, 18)
                .addBox(-5.0F, -16.0F, -8.0F, 10.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0)
                .addBox(-4.0F, -15.0F, 0.0F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 24.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float limbSwing = state.walkAnimationPos;
        float limbSwingAmount = state.walkAnimationSpeed;
        float netHeadYaw = state.yRot;
        float headPitch = state.xRot;
        this.right_leg1.xRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;
        this.left_leg.xRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;
        this.right_leg.xRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;
        this.mob_head.yRot = netHeadYaw / (180.0F / (float)Math.PI);
        this.mob_head.xRot = headPitch / (180.0F / (float)Math.PI);
        this.tail.yRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;
        this.left_leg1.xRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;
    }
}
