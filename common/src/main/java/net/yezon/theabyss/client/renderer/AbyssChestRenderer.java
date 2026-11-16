package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.EntityRenderersEvent;
import net.yezon.theabyss.platform.client.event.RegisterSpecialModelRendererEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.block.AbyssChestBlock;
import net.yezon.theabyss.block.entity.AbyssChestBlockEntity;
import net.yezon.theabyss.client.model.AbyssChestModel;
import net.yezon.theabyss.init.TheabyssModBlockEntities;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public class AbyssChestRenderer implements BlockEntityRenderer<AbyssChestBlockEntity, AbyssChestRenderer.State> {
    static final int FULL_BRIGHT = 15728880;
    private static final Map<String, Identifier> TEXTURES = new HashMap<>();
    private final AbyssChestModel single;
    private final AbyssChestModel left;
    private final AbyssChestModel right;

    public AbyssChestRenderer(BlockEntityRendererProvider.Context context) {
        this.single = new AbyssChestModel(context.bakeLayer(AbyssChestModel.SINGLE));
        this.left = new AbyssChestModel(context.bakeLayer(AbyssChestModel.LEFT));
        this.right = new AbyssChestModel(context.bakeLayer(AbyssChestModel.RIGHT));
    }

    @SubscribeEvent
    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TheabyssModBlockEntities.ABYSS_CHEST.get(), AbyssChestRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AbyssChestModel.SINGLE, AbyssChestModel::createSingleLayer);
        event.registerLayerDefinition(AbyssChestModel.LEFT, AbyssChestModel::createLeftLayer);
        event.registerLayerDefinition(AbyssChestModel.RIGHT, AbyssChestModel::createRightLayer);
    }

    @SubscribeEvent
    public static void registerSpecial(RegisterSpecialModelRendererEvent event) {
        event.register(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "abyss_chest"), AbyssChestSpecialRenderer.Unbaked.MAP_CODEC);
    }

    static Identifier texture(String wood, ChestType type) {
        String suffix = switch (type) {
            case SINGLE -> "";
            case LEFT -> "_left";
            case RIGHT -> "_right";
        };
        return TEXTURES.computeIfAbsent(wood + suffix, key -> Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "textures/entity/abyss_chest/" + key + ".png"));
    }

    static RenderType glow(ChestType type) {
        return RenderTypes.eyes(texture("glow", type));
    }

    public static class State extends BlockEntityRenderState {
        ChestType type = ChestType.SINGLE;
        Direction facing = Direction.SOUTH;
        String wood = "nightgrove";
        float open;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AbyssChestBlockEntity chest, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(chest, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = chest.getBlockState();
        Level level = chest.getLevel();
        state.type = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        state.facing = blockState.hasProperty(ChestBlock.FACING) ? blockState.getValue(ChestBlock.FACING) : Direction.SOUTH;
        state.wood = blockState.getBlock() instanceof AbyssChestBlock block ? block.wood() : "nightgrove";
        DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combined;
        if (level != null && blockState.getBlock() instanceof ChestBlock block) {
            combined = block.combine(blockState, level, chest.getBlockPos(), true);
        } else {
            combined = DoubleBlockCombiner.Combiner::acceptNone;
        }
        state.open = combined.apply(ChestBlock.opennessCombiner(chest)).get(partialTicks);
        if (state.type != ChestType.SINGLE) {
            state.lightCoords = combined.apply(new BrightnessCombiner<>()).applyAsInt(state.lightCoords);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(ChestRenderer.modelTransformation(state.facing));
        float closed = 1.0F - state.open;
        float open = 1.0F - closed * closed * closed;
        AbyssChestModel model = switch (state.type) {
            case SINGLE -> this.single;
            case LEFT -> this.left;
            case RIGHT -> this.right;
        };
        RenderType base = model.renderType(texture(state.wood, state.type));
        collector.submitModel(model, open, poseStack, base, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        collector.order(1).submitModel(model, open, poseStack, glow(state.type), FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        if (state.breakProgress != null) {
            collector.order(2).submitCrumblingOverlay(model, open, poseStack, base, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, state.breakProgress);
        }
        poseStack.popPose();
    }

    public AABB getRenderBoundingBox(AbyssChestBlockEntity chest) {
        BlockPos pos = chest.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos.offset(-1, 0, -1), pos.offset(1, 1, 1));
    }
}
