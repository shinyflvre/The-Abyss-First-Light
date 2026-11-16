package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.EntityRenderersEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.block.entity.AbyssalEnergeticaBlockEntity;
import net.yezon.theabyss.client.fx.spell.Frame;
import net.yezon.theabyss.init.TheabyssModBlockEntities;
import net.yezon.theabyss.station.ArcanePower;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public class AbyssalEnergeticaRenderer implements BlockEntityRenderer<AbyssalEnergeticaBlockEntity, AbyssalEnergeticaRenderer.State> {
    private static final int CYAN = 0x5FF0E6;
    private static final int CORE = 0xE6FFFB;
    private static final int FULL_BRIGHT = 15728880;
    private static final double GEM_Y = 1.22;
    private static final int POINTS = 26;

    private final ItemModelResolver itemModelResolver;

    public AbyssalEnergeticaRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TheabyssModBlockEntities.ABYSSAL_ENERGETICA.get(), AbyssalEnergeticaRenderer::new);
    }

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState gem = new ItemStackRenderState();
        float time;
        boolean beam;
        Vec3 target = Vec3.ZERO;
        Vec3 camera = Vec3.ZERO;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AbyssalEnergeticaBlockEntity pillar, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(pillar, state, partialTicks, cameraPosition, breakProgress);
        Level level = pillar.getLevel();
        BlockPos pos = pillar.getBlockPos();
        this.itemModelResolver.updateForTopItem(state.gem, pillar.energy(), ItemDisplayContext.GROUND, level, null, (int)pos.asLong());
        state.time = level == null ? 0.0F : (level.getGameTime() % 24000L) + partialTicks;
        state.camera = cameraPosition.subtract(pos.getX(), pos.getY(), pos.getZ());
        state.beam = false;
        if (level != null && pillar.charged()) {
            BlockPos arcane = ArcanePower.arcaneOf(level, pos);
            if (arcane != null && ArcanePower.powered(level, arcane)) {
                state.beam = true;
                state.target = new Vec3(arcane.getX() - pos.getX() + 0.5, arcane.getY() - pos.getY() + 0.8, arcane.getZ() - pos.getZ() + 0.5);
            }
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.gem.isEmpty()) {
            return;
        }
        float t = state.time;
        double bob = 0.06 * Math.sin(t * 0.09);
        Vec3 gem = new Vec3(0.5, GEM_Y + bob, 0.5);
        poseStack.pushPose();
        poseStack.translate(gem.x, gem.y - 0.12, gem.z);
        poseStack.rotateDegrees(Axis.YP, (t * 3.0F) % 360.0F);
        poseStack.scale(0.75F, 0.75F, 0.75F);
        state.gem.submit(poseStack, collector, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();

        Vec3 eye = state.camera;
        float breathe = 0.5F + 0.5F * Mth.sin(t * 0.12F);
        float halo = state.beam ? 0.6F + 0.25F * breathe : 0.42F + 0.14F * breathe;
        float radius = state.beam ? 1.15F : 1.05F;
        collector.submitCustomGeometry(poseStack, LightTrail.FLARE, (pose, buffer) -> LightTrail.flare(pose, buffer, gem, radius, CYAN, halo, eye));
        if (!state.beam) {
            return;
        }
        Vec3 target = state.target;
        Vec3 control = gem.add(target).scale(0.5).add(0.0, 1.1, 0.0);
        Vec3[] points = Frame.curve(gem, control, target, POINTS);
        int n = points.length;
        float[] outerWidth = new float[n];
        float[] coreWidth = new float[n];
        float[] outerLight = new float[n];
        float[] coreLight = new float[n];
        for (int i = 0; i < n; i++) {
            float s = i / (float)(n - 1);
            float wave = Mth.sin((s * 3.0F - t * 0.06F) * Mth.TWO_PI);
            float pulse = (float)Math.pow(Math.max(0.0F, wave), 6.0);
            float ends = Mth.clamp(s * 6.0F, 0.0F, 1.0F) * Mth.clamp((1.0F - s) * 8.0F, 0.35F, 1.0F);
            outerWidth[i] = 0.16F + 0.1F * pulse;
            coreWidth[i] = 0.05F + 0.04F * pulse;
            outerLight[i] = (0.32F + 0.68F * pulse) * ends;
            coreLight[i] = (0.25F + 0.75F * pulse) * ends;
        }
        collector.submitCustomGeometry(poseStack, LightTrail.STREAK, (pose, buffer) -> {
            LightTrail.ribbon(pose, buffer, points, outerWidth, outerLight, CYAN, eye);
            LightTrail.ribbon(pose, buffer, points, coreWidth, coreLight, CORE, eye);
        });
        float sink = 0.45F + 0.3F * breathe;
        collector.submitCustomGeometry(poseStack, LightTrail.FLARE, (pose, buffer) -> LightTrail.flare(pose, buffer, target, 0.7F, CYAN, sink, eye));
    }

    public AABB getRenderBoundingBox(AbyssalEnergeticaBlockEntity pillar) {
        return new AABB(pillar.getBlockPos()).inflate(3.0, 3.0, 3.0);
    }
}
