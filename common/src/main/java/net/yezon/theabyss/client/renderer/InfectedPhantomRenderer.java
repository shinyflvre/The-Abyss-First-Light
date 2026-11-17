package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.yezon.theabyss.compat.ARGB;
import net.minecraft.util.Mth;
import net.yezon.theabyss.client.model.ModelPhantom;
import net.yezon.theabyss.entity.InfectedPhantomEntity;

public class InfectedPhantomRenderer extends MobRenderer<InfectedPhantomEntity, ModelPhantom<InfectedPhantomEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("theabyss:textures/entity/phantom.png");
    private static final RenderType GLOW = AbyssRenderTypes.eyes(ResourceLocation.parse("theabyss:textures/entity/phantom_glow.png"));
    private final InfectedPhantomRenderState state = new InfectedPhantomRenderState();

    public InfectedPhantomRenderer(Context context) {
        super(context, new ModelPhantom<>(context.bakeLayer(ModelPhantom.LAYER_LOCATION)), 0.5F);
        this.model.phantom = this.state;
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }

            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffers, int light, InfectedPhantomEntity entity, float limbSwing, float limbSwingAmount,
                               float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                if (InfectedPhantomRenderer.this.state.fade > 0.6F) {
                    super.render(poseStack, buffers, light, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
                }
            }
        });
    }

    private static void extract(InfectedPhantomEntity entity, InfectedPhantomRenderState state, float partialTicks) {
        state.attack = entity.attack();
        state.attackAge = state.attack == InfectedPhantomEntity.NONE ? 0.0F : entity.attackAge(partialTicks);
        state.fade = fade(state.attack, state.attackAge);
        state.lean = lean(state.attack, state.attackAge);
    }

    @Override
    public void render(InfectedPhantomEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        extract(entity, this.state, partialTicks);
        MultiBufferSource target = this.state.fade < 0.999F ? TintedBuffers.of(buffers, RenderType.entityTranslucent(TEXTURE), ARGB.white(this.state.fade)) : buffers;
        super.render(entity, entityYaw, partialTicks, poseStack, target, light);
    }

    private static float smooth(float t) {
        t = Mth.clamp(t, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    static float fade(byte attack, float age) {
        if (attack != InfectedPhantomEntity.REND) {
            return 1.0F;
        }
        if (age < 6.0F) {
            return 1.0F - age / 6.0F;
        }
        float back = InfectedPhantomEntity.REND_LUNGE - 4.0F;
        return age < back ? 0.0F : Math.min(1.0F, (age - back) / 4.0F);
    }

    static float lean(byte attack, float age) {
        if (attack == InfectedPhantomEntity.REND) {
            float lunge = smooth((age - InfectedPhantomEntity.REND_LUNGE + 3.0F) / 3.0F) * (1.0F - smooth((age - InfectedPhantomEntity.REND_END) / 5.0F));
            return 0.55F * lunge;
        }
        if (attack == InfectedPhantomEntity.MIASMA) {
            float burst = InfectedPhantomEntity.MIASMA_BURST;
            float back = smooth(age / burst) * (1.0F - smooth((age - burst) / 2.0F));
            float snap = smooth((age - burst) / 2.0F) * (1.0F - smooth((age - burst - 4.0F) / 7.0F));
            return -0.22F * back + 0.3F * snap;
        }
        if (attack == InfectedPhantomEntity.VENOM) {
            float fire = InfectedPhantomEntity.VENOM_FIRE;
            float kick = age < fire ? 0.0F : Math.max(0.0F, 1.0F - (age - fire) / 5.0F);
            return 0.08F * smooth(age / 8.0F) * (1.0F - smooth((age - fire) / 6.0F)) - 0.12F * kick;
        }
        return 0.0F;
    }

    @Override
    protected void setupRotations(InfectedPhantomEntity entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
        poseStack.translate(0.0F, Mth.sin(bob * 0.09F) * 0.06F, 0.0F);
        if (this.state.lean != 0.0F) {
            poseStack.translate(0.0F, 1.0F, 0.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-this.state.lean * Mth.RAD_TO_DEG));
            poseStack.translate(0.0F, -1.0F, 0.0F);
        }
    }

    @Override
    protected RenderType getRenderType(InfectedPhantomEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (this.state.fade < 0.02F) {
            return null;
        }
        if (this.state.fade < 0.999F && bodyVisible) {
            return RenderType.entityTranslucent(TEXTURE);
        }
        return super.getRenderType(entity, bodyVisible, translucent, glowing);
    }

    @Override
    protected float getShadowRadius(InfectedPhantomEntity entity) {
        byte attack = entity.attack();
        float fade = attack == InfectedPhantomEntity.NONE ? 1.0F : fade(attack, entity.attackAge(RenderTicks.partialTick(entity)));
        return super.getShadowRadius(entity) * fade;
    }

    @Override
    public ResourceLocation getTextureLocation(InfectedPhantomEntity entity) {
        return TEXTURE;
    }
}
