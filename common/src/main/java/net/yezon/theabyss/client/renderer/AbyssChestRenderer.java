package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.EntityRenderersEvent;
import net.yezon.theabyss.platform.client.event.RegisterItemRenderersEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.block.AbyssChestBlock;
import net.yezon.theabyss.block.entity.AbyssChestBlockEntity;
import net.yezon.theabyss.client.model.AbyssChestModel;
import net.yezon.theabyss.init.TheabyssModBlockEntities;
import net.yezon.theabyss.init.TheabyssModChests;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public class AbyssChestRenderer implements BlockEntityRenderer<AbyssChestBlockEntity> {
    static final int FULL_BRIGHT = 15728880;
    private static final Map<String, ResourceLocation> TEXTURES = new HashMap<>();
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
    public static void registerItems(RegisterItemRenderersEvent event) {
        TheabyssModChests.ITEMS.forEach((wood, item) -> event.register(new AbyssChestSpecialRenderer(wood), item.get()));
    }

    static ResourceLocation texture(String wood, ChestType type) {
        String suffix = switch (type) {
            case SINGLE -> "";
            case LEFT -> "_left";
            case RIGHT -> "_right";
        };
        return TEXTURES.computeIfAbsent(wood + suffix, key -> ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "textures/entity/abyss_chest/" + key + ".png"));
    }

    static RenderType glow(ChestType type) {
        return AbyssRenderTypes.eyes(texture("glow", type));
    }

    @Override
    public void render(AbyssChestBlockEntity chest, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        BlockState blockState = chest.getBlockState();
        Level level = chest.getLevel();
        ChestType type = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        Direction facing = blockState.hasProperty(ChestBlock.FACING) ? blockState.getValue(ChestBlock.FACING) : Direction.SOUTH;
        String wood = blockState.getBlock() instanceof AbyssChestBlock block ? block.wood() : "nightgrove";
        DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combined;
        if (level != null && blockState.getBlock() instanceof ChestBlock block) {
            combined = block.combine(blockState, level, chest.getBlockPos(), true);
        } else {
            combined = DoubleBlockCombiner.Combiner::acceptNone;
        }
        float lid = combined.apply(ChestBlock.opennessCombiner(chest)).get(partialTicks);
        int lightCoords = light;
        if (type != ChestType.SINGLE) {
            lightCoords = combined.apply(new BrightnessCombiner<>()).applyAsInt(lightCoords);
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        float closed = 1.0F - lid;
        float open = 1.0F - closed * closed * closed;
        AbyssChestModel model = switch (type) {
            case SINGLE -> this.single;
            case LEFT -> this.left;
            case RIGHT -> this.right;
        };
        model.setupAnim(open);
        model.renderToBuffer(poseStack, buffers.getBuffer(model.renderType(texture(wood, type))), lightCoords, OverlayTexture.NO_OVERLAY);
        model.renderToBuffer(poseStack, buffers.getBuffer(glow(type)), FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    public AABB getRenderBoundingBox(AbyssChestBlockEntity chest) {
        BlockPos pos = chest.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos.offset(-1, 0, -1), pos.offset(1, 1, 1));
    }
}
