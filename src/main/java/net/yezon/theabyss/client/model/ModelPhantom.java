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
import net.yezon.theabyss.client.renderer.InfectedPhantomRenderState;
import net.yezon.theabyss.entity.InfectedPhantomEntity;

public class ModelPhantom<S extends LivingEntityRenderState> extends EntityModel<S> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("theabyss", "model_phantom"), "main");
    public final ModelPart right_arm;
    public final ModelPart left_arm;
    public final ModelPart mob_head;
    public final ModelPart body;

    public ModelPhantom(ModelPart root) {
        super(root);
        this.right_arm = root.getChild("right_arm");
        this.left_arm = root.getChild("left_arm");
        this.mob_head = root.getChild("mob_head");
        this.body = root.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition right_arm = partdefinition.addOrReplaceChild(
            "right_arm",
            CubeListBuilder.create()
                .texOffs(16, 48)
                .addBox(-4.025F, -1.0F, -1.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 34)
                .addBox(-4.55F, -1.5F, -1.5F, 5.0F, 13.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        PartDefinition left_arm = partdefinition.addOrReplaceChild(
            "left_arm",
            CubeListBuilder.create()
                .texOffs(32, 49)
                .addBox(0.05F, -1.0F, -1.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(36, 0)
                .addBox(-0.45F, -1.5F, -1.5F, 5.0F, 13.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        PartDefinition mob_head = partdefinition.addOrReplaceChild(
            "mob_head",
            CubeListBuilder.create()
                .texOffs(0, 18)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0)
                .addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 1.0F, 1.0F)
        );
        PartDefinition body = partdefinition.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(28, 30)
                .addBox(-5.0F, -23.0F, -1.0F, 10.0F, 15.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(63, 0)
                .addBox(-4.5F, -22.975F, -0.25F, 9.0F, 15.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 24.0F, 0.0F)
        );
        PartDefinition cube_r1 = body.addOrReplaceChild(
            "cube_r1",
            CubeListBuilder.create()
                .texOffs(0, 6)
                .addBox(0.1546F, -0.475F, -2.8829F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0)
                .addBox(0.1546F, -6.475F, -2.8829F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3)
                .addBox(0.1546F, -3.475F, -2.8829F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-2.8467F, -14.5F, -4.0188F, 0.0F, -1.789F, 0.0F)
        );
        PartDefinition cube_r2 = body.addOrReplaceChild(
            "cube_r2",
            CubeListBuilder.create()
                .texOffs(0, 18)
                .addBox(-1.1546F, -0.475F, -2.8829F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 21)
                .addBox(-1.1546F, -6.475F, -2.8829F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(24, 18)
                .addBox(-1.1546F, -3.475F, -2.8829F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(2.8467F, -14.5F, -4.0188F, 0.0F, 1.789F, 0.0F)
        );
        PartDefinition cube_r3 = body.addOrReplaceChild(
            "cube_r3",
            CubeListBuilder.create()
                .texOffs(48, 49)
                .addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(27, 0)
                .addBox(-0.5F, -6.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(15, 34)
                .addBox(-0.5F, -3.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-4.5F, -14.5F, -0.5F, 0.0F, -0.7854F, 0.0F)
        );
        PartDefinition cube_r4 = body.addOrReplaceChild(
            "cube_r4",
            CubeListBuilder.create()
                .texOffs(51, 0)
                .addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 52)
                .addBox(-0.5F, -6.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(6, 53)
                .addBox(-0.5F, -3.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(4.5F, -14.5F, -0.5F, 0.0F, 0.7854F, 0.0F)
        );
        PartDefinition cube_r5 = body.addOrReplaceChild(
            "cube_r5",
            CubeListBuilder.create()
                .texOffs(58, 32)
                .addBox(-3.5F, 0.0F, -3.5F, 8.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(32, 18)
                .addBox(-4.0F, 0.0F, -4.0F, 9.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(-0.5F, -10.0F, 2.4641F, 0.5236F, 0.0F, 0.0F)
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
        float idle = state.ageInTicks;
        this.right_arm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount - 0.12F + Mth.sin(idle * 0.07F) * 0.08F;
        this.left_arm.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount - 0.12F + Mth.sin(idle * 0.07F + 1.3F) * 0.08F;
        this.right_arm.zRot = 0.1F + Mth.sin(idle * 0.05F) * 0.05F;
        this.left_arm.zRot = -0.1F - Mth.sin(idle * 0.05F + 0.8F) * 0.05F;
        this.mob_head.yRot = netHeadYaw / (180.0F / (float)Math.PI);
        this.mob_head.xRot = headPitch / (180.0F / (float)Math.PI);
        if (state instanceof InfectedPhantomRenderState phantom && phantom.attack != InfectedPhantomEntity.NONE) {
            this.attack(phantom.attack, phantom.attackAge);
        }
    }

    private static float smooth(float t) {
        t = Mth.clamp(t, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private void attack(byte attack, float t) {
        if (attack == InfectedPhantomEntity.VENOM) {
            float fire = InfectedPhantomEntity.VENOM_FIRE;
            float raise = smooth(t / 9.0F) * (1.0F - smooth((t - fire - 3.0F) / 7.0F));
            float kick = t < fire ? 0.0F : Math.max(0.0F, 1.0F - (t - fire) / 4.0F);
            float aim = -1.6F + this.mob_head.xRot * 0.8F - 0.4F * kick;
            this.right_arm.xRot = Mth.lerp(raise, this.right_arm.xRot, aim);
            this.right_arm.yRot = Mth.lerp(raise, this.right_arm.yRot, -0.15F);
            this.right_arm.zRot = Mth.lerp(raise, this.right_arm.zRot, 0.0F) + Mth.sin(t * 2.3F) * 0.03F * raise * (t < fire ? 1.0F : 0.0F);
            this.left_arm.xRot = Mth.lerp(raise, this.left_arm.xRot, 0.35F);
            this.left_arm.zRot = Mth.lerp(raise, this.left_arm.zRot, -0.3F);
        } else if (attack == InfectedPhantomEntity.MIASMA) {
            float burst = InfectedPhantomEntity.MIASMA_BURST;
            float open = smooth(t / 14.0F) * (1.0F - smooth((t - burst) / 2.0F));
            float snap = smooth((t - burst) / 2.0F) * (1.0F - smooth((t - burst - 4.0F) / 8.0F));
            float shiver = Mth.sin(t * 2.1F) * 0.05F * open;
            this.right_arm.zRot = Mth.lerp(open, this.right_arm.zRot, 1.35F) + shiver;
            this.left_arm.zRot = Mth.lerp(open, this.left_arm.zRot, -1.35F) - shiver;
            this.right_arm.xRot = Mth.lerp(open, this.right_arm.xRot, -0.35F);
            this.left_arm.xRot = Mth.lerp(open, this.left_arm.xRot, -0.35F);
            this.mob_head.xRot = Mth.lerp(open, this.mob_head.xRot, -0.5F);
            this.right_arm.xRot = Mth.lerp(snap, this.right_arm.xRot, -0.95F);
            this.left_arm.xRot = Mth.lerp(snap, this.left_arm.xRot, -0.95F);
            this.right_arm.zRot = Mth.lerp(snap, this.right_arm.zRot, 0.55F);
            this.left_arm.zRot = Mth.lerp(snap, this.left_arm.zRot, -0.55F);
            this.mob_head.xRot = Mth.lerp(snap, this.mob_head.xRot, 0.35F);
        } else if (attack == InfectedPhantomEntity.REND) {
            float up = smooth(t / 5.0F) * (1.0F - smooth((t - InfectedPhantomEntity.REND_JUMP) / 4.0F));
            float reach = smooth((t - InfectedPhantomEntity.REND_LUNGE + 6.0F) / 5.0F) * (1.0F - smooth((t - InfectedPhantomEntity.REND_END) / 6.0F));
            this.right_arm.xRot = Mth.lerp(up, this.right_arm.xRot, -2.7F);
            this.left_arm.xRot = Mth.lerp(up, this.left_arm.xRot, -2.7F);
            this.right_arm.xRot = Mth.lerp(reach, this.right_arm.xRot, -1.55F);
            this.left_arm.xRot = Mth.lerp(reach, this.left_arm.xRot, -1.55F);
            this.right_arm.zRot = Mth.lerp(reach, this.right_arm.zRot, 0.3F);
            this.left_arm.zRot = Mth.lerp(reach, this.left_arm.zRot, -0.3F);
            this.right_arm.yRot = Mth.lerp(reach, this.right_arm.yRot, -0.25F);
            this.left_arm.yRot = Mth.lerp(reach, this.left_arm.yRot, 0.25F);
            this.mob_head.xRot = Mth.lerp(reach, this.mob_head.xRot, 0.25F);
        }
    }
}
