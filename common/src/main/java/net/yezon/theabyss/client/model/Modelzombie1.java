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
import net.minecraft.world.entity.Mob;

public class Modelzombie1<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("theabyss", "modelzombie_1"), "main");
    public final ModelPart mob_head;
    public final ModelPart left_arm;
    public final ModelPart left_leg;
    public final ModelPart right_arm;
    public final ModelPart right_leg;
    public final ModelPart body;

    private final ModelPart root;

    public Modelzombie1(ModelPart root) {
        this.root = root;
        this.mob_head = root.getChild("mob_head");
        this.left_arm = root.getChild("left_arm");
        this.left_leg = root.getChild("left_leg");
        this.right_arm = root.getChild("right_arm");
        this.right_leg = root.getChild("right_leg");
        this.body = root.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition mob_head = partdefinition.addOrReplaceChild(
            "mob_head",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 16)
                .addBox(-3.5F, -7.5F, -3.5F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.3054F, 0.0F, -0.3054F)
        );
        PartDefinition cube_r1 = mob_head.addOrReplaceChild(
            "cube_r1",
            CubeListBuilder.create().texOffs(56, 1).addBox(-4.0F, -1.0F, -0.025F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, -1.0F, -4.0F, -0.215F, 0.0376F, 0.1705F)
        );
        PartDefinition left_arm = partdefinition.addOrReplaceChild(
            "left_arm",
            CubeListBuilder.create()
                .texOffs(44, 16)
                .addBox(0.5F, -1.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(48, 0)
                .addBox(1.0F, -1.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 30)
                .addBox(0.001F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(4.0F, 2.5F, 0.0F)
        );
        PartDefinition left_leg = partdefinition.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create()
                .texOffs(12, 42)
                .addBox(-1.999F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 61)
                .addBox(-1.499F, 1.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(16, 30)
                .addBox(-1.0F, 1.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(2.0F, 12.0F, 2.0F)
        );
        PartDefinition right_arm = partdefinition.addOrReplaceChild(
            "right_arm",
            CubeListBuilder.create()
                .texOffs(32, 0)
                .addBox(-4.001F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(44, 42)
                .addBox(-3.5F, -1.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(48, 29)
                .addBox(-3.0F, -1.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-4.0F, 2.5F, 0.0F)
        );
        PartDefinition right_leg = partdefinition.addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create()
                .texOffs(28, 42)
                .addBox(-2.001F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 46)
                .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(17, 61)
                .addBox(-1.499F, 0.025F, -1.5F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-2.0F, 12.0F, 2.0F)
        );
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 2.0F));
        PartDefinition cube_r2 = body.addOrReplaceChild(
            "cube_r2",
            CubeListBuilder.create()
                .texOffs(31, 60)
                .addBox(-3.5F, -11.0F, -1.5F, 7.0F, 10.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(24, 26)
                .addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, -11.6527F, 0.0304F, 0.1745F, 0.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        this.left_leg.xRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;
        this.right_arm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
        this.mob_head.yRot = netHeadYaw / (180.0F / (float)Math.PI);
        this.mob_head.xRot = headPitch / (180.0F / (float)Math.PI);
        this.left_arm.xRot = Mth.cos(limbSwing * 1.0F) * -1.0F * limbSwingAmount;
        this.right_leg.xRot = Mth.cos(limbSwing * 1.0F) * 1.0F * limbSwingAmount;
        if (entity instanceof Mob mob) {
            boolean raiseArms = !mob.isBaby() || mob.getMainHandItem().isEmpty();
            float armDrop = raiseArms ? (float) -Math.PI / (mob.isAggressive() ? 1.5F : 2.25F) : 0.0F;
            float swing = this.attackTime;
            float attackYRotModifier = (raiseArms ? 1.0F : -1.0F) * Mth.sin(swing * (float) Math.PI);
            float attackXRotModifier = Mth.sin((1.0F - (1.0F - swing) * (1.0F - swing)) * (float) Math.PI);
            float xRot = armDrop + attackYRotModifier * 1.2F - attackXRotModifier * 0.4F;
            float yRot = 0.1F - attackYRotModifier * 0.6F;
            this.right_arm.xRot = xRot;
            this.right_arm.yRot = raiseArms ? -yRot : yRot;
            this.right_arm.zRot = 0.0F;
            this.left_arm.xRot = xRot;
            this.left_arm.yRot = raiseArms ? yRot : -yRot;
            this.left_arm.zRot = 0.0F;
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
