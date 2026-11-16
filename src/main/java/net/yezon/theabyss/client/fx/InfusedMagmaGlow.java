package net.yezon.theabyss.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModFluids;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class InfusedMagmaGlow {
    private static final ContextKey<float[]> QUADS = new ContextKey<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "infused_magma_glow"));
    private static final Identifier MAGMA_SPRITE = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "block/infused_magma_rim");
    private static final Identifier ARENO_SPRITE = Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "block/areno_rim");
    private static final Map<Level, Long2ObjectOpenHashMap<LongOpenHashSet>> INDEX = new WeakHashMap<>();
    private static final float[] NONE = new float[0];
    private static final double INSET = 0.02;
    private static final double HEIGHT = 0.75;
    private static final int MAX_CHUNKS = 8;
    private static final int STRIDE = 19;
    private static RenderType type;

    private InfusedMagmaGlow() {
    }

    private static boolean magma(FluidState fluid) {
        return !fluid.isEmpty() && fluid.getType().isSame(TheabyssModFluids.INFUSED_LAVA.get());
    }

    private static boolean areno(FluidState fluid) {
        return !fluid.isEmpty() && fluid.getType().isSame(TheabyssModFluids.ARENO.get());
    }

    private static boolean rimmed(BlockState state) {
        FluidState fluid = state.getFluidState();
        return magma(fluid) || areno(fluid);
    }

    @SubscribeEvent
    public static void onSection(AddSectionGeometryEvent event) {
        Level level = event.getLevel();
        if (level != null && level.isClientSide()) {
            scan(level, event.getSectionOrigin());
        }
    }

    private static void scan(Level level, BlockPos origin) {
        long key = SectionPos.asLong(origin);
        Long2ObjectOpenHashMap<LongOpenHashSet> sections = INDEX.computeIfAbsent(level, l -> new Long2ObjectOpenHashMap<>());
        ChunkAccess access = level.getChunkSource().getChunk(origin.getX() >> 4, origin.getZ() >> 4, false);
        int index = level.getSectionIndex(origin.getY());
        if (!(access instanceof LevelChunk chunk) || index < 0 || index >= chunk.getSections().length) {
            sections.remove(key);
            return;
        }
        LevelChunkSection section = chunk.getSection(index);
        if (section.hasOnlyAir() || !section.maybeHas(InfusedMagmaGlow::rimmed)) {
            sections.remove(key);
            return;
        }
        LongOpenHashSet found = new LongOpenHashSet();
        int x0 = origin.getX() & ~15;
        int y0 = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(origin.getY()));
        int z0 = origin.getZ() & ~15;
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    if (rimmed(section.getBlockState(x, y, z))) {
                        found.add(BlockPos.asLong(x0 + x, y0 + y, z0 + z));
                    }
                }
            }
        }
        if (found.isEmpty()) {
            sections.remove(key);
        } else {
            sections.put(key, found);
        }
    }

    @SubscribeEvent
    public static void onUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof Level level) || !level.isClientSide()) {
            return;
        }
        Long2ObjectOpenHashMap<LongOpenHashSet> sections = INDEX.get(level);
        if (sections == null) {
            return;
        }
        int cx = event.getChunk().getPos().x();
        int cz = event.getChunk().getPos().z();
        for (int sy = level.getMinSectionY(); sy <= level.getMaxSectionY(); sy++) {
            sections.remove(SectionPos.asLong(cx, sy, cz));
        }
    }

    private static final class Quads {
        float[] data = new float[STRIDE * 32];
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
        if (level == null) {
            event.getRenderState().setRenderData(QUADS, NONE);
            return;
        }
        Long2ObjectOpenHashMap<LongOpenHashSet> sections = INDEX.get(level);
        if (sections == null || sections.isEmpty()) {
            event.getRenderState().setRenderData(QUADS, NONE);
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        TextureAtlasSprite magmaSprite = mc.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(MAGMA_SPRITE);
        TextureAtlasSprite arenoSprite = mc.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(ARENO_SPRITE);
        Vec3 camera = event.getCamera().position();
        Frustum frustum = event.getFrustum();
        int range = Math.min(MAX_CHUNKS, mc.options.getEffectiveRenderDistance());
        int csx = SectionPos.blockToSectionCoord(camera.x);
        int csz = SectionPos.blockToSectionCoord(camera.z);
        Quads quads = new Quads();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos near = new BlockPos.MutableBlockPos();
        for (Long2ObjectMap.Entry<LongOpenHashSet> entry : sections.long2ObjectEntrySet()) {
            long key = entry.getLongKey();
            int sx = SectionPos.x(key);
            int sz = SectionPos.z(key);
            if (Math.abs(sx - csx) > range || Math.abs(sz - csz) > range) {
                continue;
            }
            int x0 = SectionPos.sectionToBlockCoord(sx);
            int y0 = SectionPos.sectionToBlockCoord(SectionPos.y(key));
            int z0 = SectionPos.sectionToBlockCoord(sz);
            if (!frustum.isVisible(new AABB(x0 - 1, y0 - 1, z0 - 1, x0 + 17, y0 + 18, z0 + 17))) {
                continue;
            }
            for (LongIterator it = entry.getValue().iterator(); it.hasNext(); ) {
                pos.set(it.nextLong());
                FluidState fluid = level.getFluidState(pos);
                boolean magma = magma(fluid);
                if (!(magma || areno(fluid)) || !fluid.isSource() || !level.getBlockState(near.setWithOffset(pos, Direction.UP)).isAir()) {
                    continue;
                }
                double surface = pos.getY() + fluid.getHeight(level, pos);
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    FluidState next = level.getFluidState(near.setWithOffset(pos, side));
                    if (magma ? !magma(next) : !areno(next)) {
                        add(quads, pos, side, surface, camera, magma ? magmaSprite : arenoSprite);
                    }
                }
            }
        }
        event.getRenderState().setRenderData(QUADS, quads.size == 0 ? NONE : Arrays.copyOf(quads.data, quads.size));
    }

    private static void add(Quads quads, BlockPos pos, Direction side, double surface, Vec3 camera, TextureAtlasSprite sprite) {
        Direction.Axis axis = side.getAxis();
        double plane = side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? pos.get(axis) + 1.0 - INSET : pos.get(axis) + INSET;
        boolean alongX = axis == Direction.Axis.Z;
        double t0 = alongX ? pos.getX() : pos.getZ();
        double t1 = t0 + 1.0;
        double top = surface + HEIGHT;
        quads.ensure();
        float[] d = quads.data;
        int o = quads.size;
        corner(d, o, alongX, plane, t0, surface, camera);
        corner(d, o + 3, alongX, plane, t1, surface, camera);
        corner(d, o + 6, alongX, plane, t1, top, camera);
        corner(d, o + 9, alongX, plane, t0, top, camera);
        d[o + 12] = sprite.getU0();
        d[o + 13] = sprite.getU1();
        d[o + 14] = sprite.getV0();
        d[o + 15] = sprite.getV1();
        d[o + 16] = side.getStepX();
        d[o + 17] = side.getStepY();
        d[o + 18] = side.getStepZ();
        quads.size += STRIDE;
    }

    private static void corner(float[] d, int o, boolean alongX, double plane, double t, double y, Vec3 camera) {
        double x = alongX ? t : plane;
        double z = alongX ? plane : t;
        d[o] = (float)(x - camera.x);
        d[o + 1] = (float)(y - camera.y);
        d[o + 2] = (float)(z - camera.z);
    }

    @SubscribeEvent
    public static void onSubmit(SubmitCustomGeometryEvent event) {
        float[] data = event.getLevelRenderState().getRenderData(QUADS);
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
