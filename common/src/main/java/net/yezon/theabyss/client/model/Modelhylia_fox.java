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

public class Modelhylia_fox<S extends LivingEntityRenderState> extends EntityModel<S> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("theabyss", "modelhylia_fox"), "main");
    public final ModelPart mob_head;
    public final ModelPart right_leg2;
    public final ModelPart left_leg2;
    public final ModelPart right_leg1;
    public final ModelPart left_leg1;
    public final ModelPart mob_tail;
    public final ModelPart body;

    public Modelhylia_fox(ModelPart root) {
        super(root);
        this.mob_head = root.getChild("mob_head");
        this.right_leg2 = root.getChild("right_leg2");
        this.left_leg2 = root.getChild("left_leg2");
        this.right_leg1 = root.getChild("right_leg1");
        this.left_leg1 = root.getChild("left_leg1");
        this.mob_tail = root.getChild("mob_tail");
        this.body = root.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition mob_head = partdefinition.addOrReplaceChild(
            "mob_head",
            CubeListBuilder.create().texOffs(0, 17).addBox(-4.0F, -2.0F, -6.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 16.0F, -3.0F)
        );
        PartDefinition cube_r1 = mob_head.addOrReplaceChild(
            "cube_r1",
            CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -3.0F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-3.0F, -1.0F, -2.5F, 0.0F, 0.0F, -0.3491F)
        );
        PartDefinition cube_r2 = mob_head.addOrReplaceChild(
            "cube_r2",
            CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -3.0F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(3.0F, -1.0F, -2.5F, 0.0F, 0.0F, 0.3491F)
        );
        PartDefinition cube_r3 = mob_head.addOrReplaceChild(
            "cube_r3",
            CubeListBuilder.create().texOffs(22, 15).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 1.65F, -6.0F, 0.1745F, 0.0F, 0.0F)
        );
        PartDefinition right_leg2 = partdefinition.addOrReplaceChild(
            "right_leg2",
            CubeListBuilder.create().texOffs(16, 29).addBox(-2.005F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-1.0F, 18.0F, 6.0F)
        );
        PartDefinition left_leg2 = partdefinition.addOrReplaceChild(
            "left_leg2",
            CubeListBuilder.create().texOffs(8, 29).addBox(0.005F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(1.0F, 18.0F, 6.0F)
        );
        PartDefinition right_leg1 = partdefinition.addOrReplaceChild(
            "right_leg1",
            CubeListBuilder.create().texOffs(0, 29).addBox(-2.005F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-1.0F, 18.0F, -1.0F)
        );
        PartDefinition left_leg1 = partdefinition.addOrReplaceChild(
            "left_leg1",
            CubeListBuilder.create().texOffs(26, 27).addBox(0.005F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(1.0F, 18.0F, -1.0F)
        );
        PartDefinition mob_tail = partdefinition.addOrReplaceChild(
            "mob_tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 16.0F, 7.0F, 1.5708F, 0.0F, 0.0F)
        );
        PartDefinition cube_r4 = mob_tail.addOrReplaceChild(
            "cube_r4",
            CubeListBuilder.create().texOffs(24, 0).addBox(-2.0F, 0.0F, -2.25F, 4.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F)
        );
        PartDefinition body = partdefinition.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 11.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 1.5708F, 0.0F, 0.0F)
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
        this.right_leg1.xRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;
        this.mob_tail.yRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;
        this.right_leg2.xRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;
        this.mob_head.yRot = netHeadYaw / (180.0F / (float)Math.PI);
        this.mob_head.xRot = headPitch / (180.0F / (float)Math.PI);
        this.left_leg1.xRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;
        this.left_leg2.xRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;
    }
}
