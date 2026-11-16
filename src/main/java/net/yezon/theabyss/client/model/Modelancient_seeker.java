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

public class Modelancient_seeker<S extends LivingEntityRenderState> extends EntityModel<S> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("theabyss", "modelancient_seeker"), "main");
    public final ModelPart torso;
    public final ModelPart left_leg;
    public final ModelPart right_leg;
    public final ModelPart neck;
    public final ModelPart head;

    public Modelancient_seeker(ModelPart root) {
        super(root);
        this.torso = root.getChild("torso");
        this.left_leg = root.getChild("left_leg");
        this.right_leg = root.getChild("right_leg");
        this.neck = root.getChild("neck");
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition torso = partdefinition.addOrReplaceChild("torso", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition cube_r1 = torso.addOrReplaceChild(
            "cube_r1",
            CubeListBuilder.create().texOffs(44, 41).addBox(-6.0F, -28.0F, -24.0F, 12.0F, 14.0F, 14.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F)
        );
        PartDefinition cube_r2 = torso.addOrReplaceChild(
            "cube_r2",
            CubeListBuilder.create().texOffs(35, 9).addBox(-5.0F, -30.0F, -6.0F, 10.0F, 12.0F, 17.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1309F, 0.0F, 0.0F)
        );
        PartDefinition tail = torso.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -28.4913F, 6.5538F));
        PartDefinition cube_r3 = tail.addOrReplaceChild(
            "cube_r3",
            CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -30.0F, 3.0F, 12.0F, 0.0F, 26.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 28.4913F, -6.5538F, -0.0873F, 0.0F, 0.0F)
        );
        PartDefinition left_leg = partdefinition.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(2.0F, 25.75F, -8.0F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(35, 38)
                .addBox(2.0F, 25.0F, -5.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0)
                .addBox(6.0F, 25.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-7.0F, -2.0F, 0.0F)
        );
        PartDefinition cube_r4 = left_leg.addOrReplaceChild(
            "cube_r4",
            CubeListBuilder.create().texOffs(0, 19).addBox(-5.0F, -22.0F, -2.0F, 2.0F, 22.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(7.0F, 26.0F, 0.0F, 0.0873F, 0.0F, 0.0F)
        );
        PartDefinition cube_r5 = left_leg.addOrReplaceChild(
            "cube_r5",
            CubeListBuilder.create().texOffs(63, 69).addBox(-8.0F, -27.0F, -8.0F, 6.0F, 12.0F, 7.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(7.0F, 26.0F, 0.0F, -0.1745F, 0.0F, 0.0F)
        );
        PartDefinition right_leg = partdefinition.addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create()
                .texOffs(16, 19)
                .addBox(-4.0F, 25.0F, -5.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3)
                .addBox(-4.0F, 25.75F, -8.0F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(7.0F, -2.0F, 0.0F)
        );
        PartDefinition cube_r6 = right_leg.addOrReplaceChild(
            "cube_r6",
            CubeListBuilder.create().texOffs(0, 0).addBox(2.0F, -27.0F, -8.0F, 6.0F, 12.0F, 7.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-7.0F, 26.0F, 0.0F, -0.1745F, 0.0F, 0.0F)
        );
        PartDefinition cube_r7 = right_leg.addOrReplaceChild(
            "cube_r7",
            CubeListBuilder.create().texOffs(8, 19).addBox(3.0F, -22.0F, -2.0F, 2.0F, 22.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-7.0F, 26.0F, 0.0F, 0.0873F, 0.0F, 0.0F)
        );
        PartDefinition neck = partdefinition.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition cube_r8 = neck.addOrReplaceChild(
            "cube_r8",
            CubeListBuilder.create().texOffs(0, 26).addBox(-3.0F, 11.0F, -45.0F, 6.0F, 6.0F, 23.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.4399F, 0.0F, 0.0F)
        );
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -19.1538F, -21.0445F));
        PartDefinition cube_r9 = head.addOrReplaceChild(
            "cube_r9",
            CubeListBuilder.create().texOffs(72, 0).addBox(-7.0F, -8.0F, -55.0F, 14.0F, 14.0F, 0.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 43.1538F, 21.0445F, -1.309F, 0.0F, 0.0F)
        );
        PartDefinition cube_r10 = head.addOrReplaceChild(
            "cube_r10",
            CubeListBuilder.create().texOffs(0, 55).addBox(-5.0F, 11.0F, -54.0F, 10.0F, 11.0F, 9.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 43.1538F, 21.0445F, -1.4399F, 0.0F, 0.0F)
        );
        PartDefinition up_jaw = head.addOrReplaceChild("up_jaw", CubeListBuilder.create(), PartPose.offset(0.0F, 43.1538F, 21.0445F));
        PartDefinition cube_r11 = up_jaw.addOrReplaceChild(
            "cube_r11",
            CubeListBuilder.create().texOffs(31, 69).addBox(-4.5F, 22.0F, -54.0F, 9.0F, 14.0F, 7.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.4399F, 0.0F, 0.0F)
        );
        PartDefinition down_jaw = head.addOrReplaceChild("down_jaw", CubeListBuilder.create(), PartPose.offset(0.0F, 43.1538F, 21.0445F));
        PartDefinition cube_r12 = down_jaw.addOrReplaceChild(
            "cube_r12",
            CubeListBuilder.create().texOffs(0, 75).addBox(-4.0F, 22.0F, -49.0F, 8.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.4399F, 0.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        float limbSwing = state.walkAnimationPos;
        float limbSwingAmount = state.walkAnimationSpeed;
        float netHeadYaw = state.yRot;
        float headPitch = state.xRot;
        this.head.yRot = netHeadYaw / (180.0F / (float)Math.PI);
        this.head.xRot = headPitch / (180.0F / (float)Math.PI);
        this.left_leg.xRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;
        this.right_leg.xRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;
    }
}
