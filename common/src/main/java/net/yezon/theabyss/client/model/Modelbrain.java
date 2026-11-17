package net.yezon.theabyss.client.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class Modelbrain<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("theabyss", "modelbrain"), "main");
    public final ModelPart right_leg;
    public final ModelPart left_leg;
    public final ModelPart left_back_leg;
    public final ModelPart right_back_leg;
    public final ModelPart head;
    public final ModelPart bb_main;

    private final ModelPart root;

    public Modelbrain(ModelPart root) {
        this.root = root;
        this.right_leg = root.getChild("right_leg");
        this.left_leg = root.getChild("left_leg");
        this.left_back_leg = root.getChild("left_back_leg");
        this.right_back_leg = root.getChild("right_back_leg");
        this.head = root.getChild("head");
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition right_leg = partdefinition.addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create().texOffs(29, 52).addBox(-1.5F, 0.5F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(3.5F, 14.5F, -10.5F)
        );
        PartDefinition left_leg = partdefinition.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(17, 52).addBox(-1.5F, 0.5F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-3.5F, 14.5F, -10.5F)
        );
        PartDefinition left_back_leg = partdefinition.addOrReplaceChild(
            "left_back_leg",
            CubeListBuilder.create().texOffs(45, 7).addBox(-1.5F, 0.5F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-3.5F, 14.5F, 3.5F)
        );
        PartDefinition right_back_leg = partdefinition.addOrReplaceChild(
            "right_back_leg",
            CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 0.5F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(3.5F, 14.5F, 3.5F)
        );
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 9.6991F, 0.0F));
        PartDefinition head_r1 = head.addOrReplaceChild(
            "head_r1",
            CubeListBuilder.create().texOffs(0, 35).addBox(-4.0F, -23.0F, -13.0F, 8.0F, 8.0F, 9.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 14.3009F, 0.0F, 0.3927F, 0.0F, 0.0F)
        );
        PartDefinition bb_main = partdefinition.addOrReplaceChild(
            "bb_main",
            CubeListBuilder.create()
                .texOffs(34, 41)
                .addBox(-3.0F, -17.0F, -5.0F, 6.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(45, 0)
                .addBox(-2.5F, -13.0F, -3.0F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(25, 23)
                .addBox(0.0F, -13.0F, -1.0F, 0.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 42)
                .addBox(0.0F, -16.5F, -14.0F, 0.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0)
                .addBox(-7.0F, -25.0F, -13.0F, 13.0F, 16.0F, 19.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 24.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        this.head.yRot = netHeadYaw / (180.0F / (float)Math.PI);
        this.head.xRot = headPitch / (180.0F / (float)Math.PI);
        this.left_leg.xRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;
        this.right_leg.xRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;
        this.left_back_leg.xRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;
        this.right_back_leg.xRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
