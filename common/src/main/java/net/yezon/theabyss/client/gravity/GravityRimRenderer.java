package net.yezon.theabyss.client.gravity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ExtractLevelRenderStateEvent;
import net.yezon.theabyss.platform.client.event.SubmitCustomGeometryEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.block.ChiseledGravityStoneBlock;
import net.yezon.theabyss.gravity.GravityIndex;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class GravityRimRenderer {
    private static final ContextKey<float[]> RIMS = new ContextKey<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "gravity_rims"));
    private static final Identifier SPRITE = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "block/chiseled_gravity_stone_rim");
    private static final float[] NONE = new float[0];
    private static final double INSET = 0.02;
    private static final int STRIDE = 19;
    private static RenderType type;

    private GravityRimRenderer() {
    }

    private static final class Quads {
        float[] data = new float[STRIDE * 16];
        int size;

        void ensure() {
            if (this.size + STRIDE > this.data.length) {
                this.data = Arrays.copyOf(this.data, this.data.length * 2);
            }
        }
    }

    @SubscribeEvent
    public static void onExtract(ExtractLevelRenderStateEvent event) {
        ClientLevel level = event.getLevel();
        if (level == null || !GravityStoneLook.installed() || !GravityIndex.any(level)) {
            net.yezon.theabyss.platform.client.renderstate.RenderData.set(event.getRenderState(), RIMS, NONE);
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        TextureAtlasSprite sprite = mc.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(SPRITE);
        Vec3 camera = event.getCamera().position();
        Frustum frustum = event.getFrustum();
        int range = mc.options.getEffectiveRenderDistance();
        int csx = SectionPos.blockToSectionCoord(camera.x);
        int csz = SectionPos.blockToSectionCoord(camera.z);
        Quads quads = new Quads();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        GravityIndex.sections(level, key -> {
            int sx = SectionPos.x(key);
            int sy = SectionPos.y(key);
            int sz = SectionPos.z(key);
            if (Math.abs(sx - csx) > range || Math.abs(sz - csz) > range) {
                return;
            }
            int x0 = SectionPos.sectionToBlockCoord(sx);
            int y0 = SectionPos.sectionToBlockCoord(sy);
            int z0 = SectionPos.sectionToBlockCoord(sz);
            if (!frustum.isVisible(new AABB(x0 - 1, y0 - 1, z0 - 1, x0 + 17, y0 + 17, z0 + 17))) {
                return;
            }
            GravityIndex.positions(level, key, packed -> {
                pos.set(packed);
                BlockState state = level.getBlockState(pos);
                if (!(state.getBlock() instanceof ChiseledGravityStoneBlock)) {
                    return;
                }
                BlockState shown = ChiseledGravityStoneBlock.shownRims(level, pos, state);
                Direction facing = shown.getValue(ChiseledGravityStoneBlock.FACING);
                for (Direction side : Direction.values()) {
                    if (shown.getValue(ChiseledGravityStoneBlock.rim(side))) {
                        add(quads, pos, facing, side, camera, sprite);
                    }
                }
            });
        });
        net.yezon.theabyss.platform.client.renderstate.RenderData.set(event.getRenderState(), RIMS, quads.size == 0 ? NONE : Arrays.copyOf(quads.data, quads.size));
    }

    private static void add(Quads quads, BlockPos pos, Direction facing, Direction side, Vec3 camera, TextureAtlasSprite sprite) {
        Direction.Axis fAxis = facing.getAxis();
        Direction.Axis sAxis = side.getAxis();
        Direction.Axis tAxis = third(fAxis, sAxis);
        double near = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? pos.get(fAxis) + 1.0 : pos.get(fAxis);
        double far = near + facing.getAxisDirection().getStep();
        double plane = side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? pos.get(sAxis) + 1.0 - INSET : pos.get(sAxis) + INSET;
        double t0 = pos.get(tAxis);
        double t1 = t0 + 1.0;
        quads.ensure();
        float[] d = quads.data;
        int o = quads.size;
        corner(d, o, fAxis, near, sAxis, plane, tAxis, t0, camera);
        corner(d, o + 3, fAxis, near, sAxis, plane, tAxis, t1, camera);
        corner(d, o + 6, fAxis, far, sAxis, plane, tAxis, t1, camera);
        corner(d, o + 9, fAxis, far, sAxis, plane, tAxis, t0, camera);
        d[o + 12] = sprite.getU0();
        d[o + 13] = sprite.getU1();
        d[o + 14] = sprite.getV0();
        d[o + 15] = sprite.getV1();
        d[o + 16] = side.getStepX();
        d[o + 17] = side.getStepY();
        d[o + 18] = side.getStepZ();
        quads.size += STRIDE;
    }

    private static void corner(float[] d, int o, Direction.Axis fAxis, double f, Direction.Axis sAxis, double s, Direction.Axis tAxis, double t, Vec3 camera) {
        double[] xyz = new double[3];
        xyz[fAxis.ordinal()] = f;
        xyz[sAxis.ordinal()] = s;
        xyz[tAxis.ordinal()] = t;
        d[o] = (float)(xyz[0] - camera.x);
        d[o + 1] = (float)(xyz[1] - camera.y);
        d[o + 2] = (float)(xyz[2] - camera.z);
    }

    private static Direction.Axis third(Direction.Axis a, Direction.Axis b) {
        for (Direction.Axis axis : Direction.Axis.values()) {
            if (axis != a && axis != b) {
                return axis;
            }
        }
        return a;
    }

    @SubscribeEvent
    public static void onSubmit(SubmitCustomGeometryEvent event) {
        float[] data = net.yezon.theabyss.platform.client.renderstate.RenderData.get(event.getLevelRenderState(), RIMS);
        if (data == null || data.length == 0) {
            return;
        }
        if (type == null) {
            type = RenderTypes.beaconBeam(Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).location(), true);
        }
        PoseStack poseStack = event.getPoseStack();
        event.getSubmitNodeCollector().submitCustomGeometry(poseStack, type, (pose, buffer) -> {
            for (int o = 0; o < data.length; o += STRIDE) {
                float u0 = data[o + 12];
                float u1 = data[o + 13];
                float v0 = data[o + 14];
                float v1 = data[o + 15];
                float nx = data[o + 16];
                float ny = data[o + 17];
                float nz = data[o + 18];
                vertex(pose, buffer, data, o, u0, v1, nx, ny, nz);
                vertex(pose, buffer, data, o + 3, u1, v1, nx, ny, nz);
                vertex(pose, buffer, data, o + 6, u1, v0, nx, ny, nz);
                vertex(pose, buffer, data, o + 9, u0, v0, nx, ny, nz);
                vertex(pose, buffer, data, o + 9, u0, v0, -nx, -ny, -nz);
                vertex(pose, buffer, data, o + 6, u1, v0, -nx, -ny, -nz);
                vertex(pose, buffer, data, o + 3, u1, v1, -nx, -ny, -nz);
                vertex(pose, buffer, data, o, u0, v1, -nx, -ny, -nz);
            }
        });
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float[] d, int o, float u, float v, float nx, float ny, float nz) {
        buffer.addVertex(pose, d[o], d[o + 1], d[o + 2]).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880)
            .setNormal(pose, nx, ny, nz);
    }
}
