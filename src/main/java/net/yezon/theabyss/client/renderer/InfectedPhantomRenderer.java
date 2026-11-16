package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.yezon.theabyss.client.model.ModelPhantom;
import net.yezon.theabyss.entity.InfectedPhantomEntity;

public class InfectedPhantomRenderer extends MobRenderer<InfectedPhantomEntity, InfectedPhantomRenderState, ModelPhantom<InfectedPhantomRenderState>> {
    private static final Identifier TEXTURE = Identifier.parse("theabyss:textures/entity/phantom.png");
    private static final RenderType GLOW = RenderTypes.eyes(Identifier.parse("theabyss:textures/entity/phantom_glow.png"));

    public InfectedPhantomRenderer(Context context) {
        super(context, new ModelPhantom<>(context.bakeLayer(ModelPhantom.LAYER_LOCATION)), 0.5F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }

            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, InfectedPhantomRenderState state, float yRot, float xRot) {
                if (state.fade > 0.6F) {
                    super.submit(poseStack, collector, light, state, yRot, xRot);
                }
            }
        });
    }

    @Override
    public InfectedPhantomRenderState createRenderState() {
        return new InfectedPhantomRenderState();
    }

    @Override
    public void extractRenderState(InfectedPhantomEntity entity, InfectedPhantomRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack = entity.attack();
        state.attackAge = state.attack == InfectedPhantomEntity.NONE ? 0.0F : entity.attackAge(partialTicks);
        state.fade = fade(state.attack, state.attackAge);
        state.lean = lean(state.attack, state.attackAge);
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
    protected void setupRotations(InfectedPhantomRenderState state, PoseStack poseStack, float bodyRot, float scale) {
        super.setupRotations(state, poseStack, bodyRot, scale);
        poseStack.translate(0.0F, Mth.sin(state.ageInTicks * 0.09F) * 0.06F, 0.0F);
        if (state.lean != 0.0F) {
            poseStack.translate(0.0F, 1.0F, 0.0F);
            poseStack.rotateDegrees(Axis.XP, -state.lean * Mth.RAD_TO_DEG);
            poseStack.translate(0.0F, -1.0F, 0.0F);
        }
    }

    @Override
    protected RenderType getRenderType(InfectedPhantomRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        if (state.fade < 0.02F) {
            return null;
        }
        if (state.fade < 0.999F && isBodyVisible) {
            return RenderTypes.entityTranslucent(TEXTURE);
        }
        return super.getRenderType(state, isBodyVisible, forceTransparent, appearGlowing);
    }

    @Override
    protected int getModelTint(InfectedPhantomRenderState state) {
        return state.fade < 0.999F ? ARGB.white(state.fade) : -1;
    }

    @Override
    protected float getShadowRadius(InfectedPhantomRenderState state) {
        return super.getShadowRadius(state) * state.fade;
    }

    @Override
    public Identifier getTextureLocation(InfectedPhantomRenderState state) {
        return TEXTURE;
    }
}
