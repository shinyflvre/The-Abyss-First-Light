package net.yezon.theabyss.client.fx;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.BlendFactor;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.ring.fx.GlowParticleOptions;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class RootCracks {
    private static final ResourceKey<Level> THE_ABYSS = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "the_abyss"));
    private static final ResourceKey<Biome> NIGHTGROVE = ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "nightgrove"));
    private static final int MAX_RADIUS = 16;
    private static final int MAX_JOBS = 3;
    private static final int UPLOADS_PER_TICK = 4;
    private static final int CHECKS_PER_TICK = 12;
    private static final int WATCH_RADIUS = 2;
    private static final float LIFT = 0.003F;
    private static final int SPARK_COLOR = 0x52F5FF;
    private static final RandomSource RANDOM = RandomSource.create();

    private static final RenderPipeline.Snippet SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
        .withVertexShader(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "core/root_cracks"))
        .withFragmentShader(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "core/root_cracks"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
        .withCull(false)
        .buildSnippet();
    private static final RenderPipeline RIM_PIPELINE = pipeline("rim", 0, BlendFunction.TRANSLUCENT);
    private static final RenderPipeline HALO_LIGHT_PIPELINE = pipeline("halo_light", 1,
        new BlendFunction(BlendFactor.DST_COLOR, BlendFactor.ONE, BlendFactor.ZERO, BlendFactor.ONE));
    private static final RenderPipeline HALO_GLOW_PIPELINE = pipeline("halo_glow", 2,
        new BlendFunction(BlendFactor.ONE, BlendFactor.ONE, BlendFactor.ZERO, BlendFactor.ONE));
    private static final RenderPipeline CORE_PIPELINE = pipeline("core", 3, BlendFunction.TRANSLUCENT);

    private static final Map<Long, ChunkCracks> CHUNKS = new HashMap<>();
    private static final Set<Long> PENDING = new HashSet<>();
    private static final ConcurrentLinkedQueue<Built> DONE = new ConcurrentLinkedQueue<>();
    private static final List<Long> CHECK_ORDER = new ArrayList<>();
    private static ClientLevel lastLevel;
    private static int checkIndex;
    private static int generation;

    private RootCracks() {
    }

    private static RenderPipeline pipeline(String name, int pass, BlendFunction blend) {
        return RenderPipeline.builder(SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "pipeline/root_cracks_" + name))
            .withShaderDefine("PASS", pass)
            .withColorTargetState(new ColorTargetState(blend))
            .build();
    }

    @SubscribeEvent
    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(RIM_PIPELINE);
        event.registerPipeline(HALO_LIGHT_PIPELINE);
        event.registerPipeline(HALO_GLOW_PIPELINE);
        event.registerPipeline(CORE_PIPELINE);
    }

    private static boolean enabled() {
        try {
            return ConfigConfiguration.ROOT_CRACKS.get();
        } catch (IllegalStateException notLoadedYet) {
            return false;
        }
    }

    private record Column(boolean valid, float top, int height, BlockState state) {
    }

    private record Built(long key, int generation, long signature, int originX, int originZ, float[][] layers, float[] sparks, float minY, float maxY,
        int[] heights, BlockState[] states) {
    }

    private static final class ChunkCracks {
        final long signature;
        final int originX;
        final int originZ;
        final GpuBuffer[] buffers = new GpuBuffer[3];
        final int[] quads = new int[3];
        final float[] sparks;
        final float minY;
        final float maxY;
        final int[] heights;
        final BlockState[] states;

        ChunkCracks(long signature, int originX, int originZ, float[] sparks, float minY, float maxY, int[] heights, BlockState[] states) {
            this.signature = signature;
            this.originX = originX;
            this.originZ = originZ;
            this.sparks = sparks;
            this.minY = minY;
            this.maxY = maxY;
            this.heights = heights;
            this.states = states;
        }

        boolean empty() {
            return this.quads[0] == 0 && this.quads[1] == 0 && this.quads[2] == 0;
        }

        void close() {
            for (int i = 0; i < 3; i++) {
                if (this.buffers[i] != null) {
                    this.buffers[i].close();
                    this.buffers[i] = null;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null || !enabled() || level.dimension() != THE_ABYSS) {
            clear();
            return;
        }
        if (level != lastLevel) {
            clear();
            lastLevel = level;
        }
        upload();
        BlockPos camera = mc.gameRenderer.mainCamera().blockPosition();
        int radius = Math.min(MAX_RADIUS, mc.options.getEffectiveRenderDistance());
        int ccx = camera.getX() >> 4;
        int ccz = camera.getZ() >> 4;
        forgetFar(ccx, ccz, radius + 2, level);
        watchNear(level, ccx, ccz);
        schedule(level, ccx, ccz, radius);
        validate(level);
        if (!mc.isPaused()) {
            sparks(mc, camera);
        }
    }

    private static void clear() {
        for (ChunkCracks chunk : CHUNKS.values()) {
            chunk.close();
        }
        CHUNKS.clear();
        PENDING.clear();
        DONE.clear();
        CHECK_ORDER.clear();
        lastLevel = null;
        generation++;
    }

    private static void forgetFar(int ccx, int ccz, int keep, ClientLevel level) {
        Iterator<Map.Entry<Long, ChunkCracks>> it = CHUNKS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, ChunkCracks> entry = it.next();
            int cx = ChunkPos.getX(entry.getKey());
            int cz = ChunkPos.getZ(entry.getKey());
            if (Math.abs(cx - ccx) > keep || Math.abs(cz - ccz) > keep || !level.hasChunk(cx, cz)) {
                entry.getValue().close();
                it.remove();
                CHECK_ORDER.remove(entry.getKey());
            }
        }
    }

    private static void schedule(ClientLevel level, int ccx, int ccz, int radius) {
        if (PENDING.size() >= MAX_JOBS) {
            return;
        }
        for (int ring = 0; ring <= radius; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) {
                        continue;
                    }
                    int cx = ccx + dx;
                    int cz = ccz + dz;
                    long key = ChunkPos.pack(cx, cz);
                    if (CHUNKS.containsKey(key) || PENDING.contains(key) || !level.hasChunk(cx, cz)) {
                        continue;
                    }
                    build(level, cx, cz, key);
                    if (PENDING.size() >= MAX_JOBS) {
                        return;
                    }
                }
            }
        }
    }

    private static void watchNear(ClientLevel level, int ccx, int ccz) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -WATCH_RADIUS; dx <= WATCH_RADIUS; dx++) {
            for (int dz = -WATCH_RADIUS; dz <= WATCH_RADIUS; dz++) {
                int cx = ccx + dx;
                int cz = ccz + dz;
                long key = ChunkPos.pack(cx, cz);
                ChunkCracks chunk = CHUNKS.get(key);
                if (chunk == null || PENDING.contains(key) || !level.hasChunk(cx, cz)) {
                    continue;
                }
                if (changed(level, pos, chunk, cx, cz)) {
                    build(level, cx, cz, key);
                }
            }
        }
    }

    private static boolean changed(ClientLevel level, BlockPos.MutableBlockPos pos, ChunkCracks chunk, int cx, int cz) {
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int i = z * 16 + x;
                int wx = (cx << 4) + x;
                int wz = (cz << 4) + z;
                int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, wx, wz);
                if (height != chunk.heights[i] || level.getBlockState(pos.set(wx, height - 1, wz)) != chunk.states[i]) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void validate(ClientLevel level) {
        if (CHECK_ORDER.isEmpty()) {
            return;
        }
        for (int i = 0; i < CHECKS_PER_TICK && !CHECK_ORDER.isEmpty(); i++) {
            checkIndex = (checkIndex + 1) % CHECK_ORDER.size();
            long key = CHECK_ORDER.get(checkIndex);
            ChunkCracks chunk = CHUNKS.get(key);
            if (chunk == null || PENDING.contains(key) || PENDING.size() >= MAX_JOBS) {
                continue;
            }
            Column[] columns = snapshot(level, ChunkPos.getX(key), ChunkPos.getZ(key));
            if (signature(columns) != chunk.signature) {
                submit(ChunkPos.getX(key), ChunkPos.getZ(key), key, columns);
            }
        }
    }

    private static void build(ClientLevel level, int cx, int cz, long key) {
        submit(cx, cz, key, snapshot(level, cx, cz));
    }

    private static void submit(int cx, int cz, long key, Column[] columns) {
        PENDING.add(key);
        int gen = generation;
        long signature = signature(columns);
        CompletableFuture.runAsync(() -> DONE.add(mesh(key, gen, signature, cx, cz, columns)), Util.backgroundExecutor());
    }

    private static Column[] snapshot(ClientLevel level, int cx, int cz) {
        Column[] columns = new Column[256];
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int wx = (cx << 4) + x;
                int wz = (cz << 4) + z;
                columns[z * 16 + x] = column(level, pos, wx, wz);
            }
        }
        return columns;
    }

    private static Column column(ClientLevel level, BlockPos.MutableBlockPos pos, int x, int z) {
        int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        pos.set(x, height - 1, z);
        BlockState top = level.getBlockState(pos);
        if (height <= level.getMinY() || top.is(BlockTags.LOGS) || !top.getFluidState().isEmpty() || !level.getBiome(pos).is(NIGHTGROVE)) {
            return new Column(false, 0.0F, height, top);
        }
        VoxelShape shape = top.getShape(level, pos);
        if (shape.isEmpty()) {
            return new Column(false, 0.0F, height, top);
        }
        return new Column(true, (float)(height - 1 + shape.max(Direction.Axis.Y)), height, top);
    }

    private static long signature(Column[] columns) {
        long hash = 1125899906842597L;
        for (Column column : columns) {
            hash = 31 * hash + (column.valid() ? Float.floatToIntBits(column.top()) : -1);
        }
        return hash;
    }

    private static Built mesh(long key, int gen, long signature, int cx, int cz, Column[] columns) {
        int originX = cx << 4;
        int originZ = cz << 4;
        FloatList[] layers = {new FloatList(), new FloatList(), new FloatList()};
        FloatList sparks = new FloatList();
        float[] range = {Float.MAX_VALUE, -Float.MAX_VALUE};
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                Column column = columns[z * 16 + x];
                if (!column.valid() || !RootCrackPattern.blockMayCrack(originX + x, originZ + z)) {
                    continue;
                }
                float y = column.top() + LIFT;
                int bx = x;
                int bz = z;
                float[][] rowValue = new float[3][256];
                float[][] rowKind = new float[3][256];
                boolean[][] present = new boolean[3][256];
                RootCrackPattern.block(originX + x, originZ + z, (layer, tx, tz, value, kind) -> {
                    present[layer][tz * 16 + tx] = true;
                    rowValue[layer][tz * 16 + tx] = value;
                    rowKind[layer][tz * 16 + tx] = kind;
                });
                boolean any = false;
                for (int layer = 0; layer < 3; layer++) {
                    for (int tz = 0; tz < 16; tz++) {
                        int tx = 0;
                        while (tx < 16) {
                            int index = tz * 16 + tx;
                            if (!present[layer][index]) {
                                tx++;
                                continue;
                            }
                            int value = Math.round(rowValue[layer][index] * 255.0F);
                            int kind = Math.round(rowKind[layer][index] * 255.0F);
                            int end = tx + 1;
                            while (end < 16 && present[layer][tz * 16 + end] && Math.round(rowValue[layer][tz * 16 + end] * 255.0F) == value
                                && Math.round(rowKind[layer][tz * 16 + end] * 255.0F) == kind) {
                                end++;
                            }
                            layers[layer].add(bx + tx / 16.0F, bz + tz / 16.0F, bx + end / 16.0F, bz + (tz + 1) / 16.0F, y, value, kind);
                            if (layer == RootCrackPattern.CORE && kind == 0 && sparks.size() < 7 * 48) {
                                sparks.add(originX + bx + (tx + end) / 32.0F, y, originZ + bz + (tz + 0.5F) / 16.0F, value, 0, 0, 0);
                            }
                            any = true;
                            tx = end;
                        }
                    }
                }
                if (any) {
                    range[0] = Math.min(range[0], y);
                    range[1] = Math.max(range[1], y);
                }
            }
        }
        float[][] data = {layers[0].toArray(), layers[1].toArray(), layers[2].toArray()};
        int[] heights = new int[256];
        BlockState[] states = new BlockState[256];
        for (int i = 0; i < 256; i++) {
            heights[i] = columns[i].height();
            states[i] = columns[i].state();
        }
        return new Built(key, gen, signature, originX, originZ, data, sparks.toArray(), range[0], range[1], heights, states);
    }

    private static final class FloatList {
        private float[] data = new float[64];
        private int size;

        void add(float... values) {
            if (this.size + values.length > this.data.length) {
                this.data = java.util.Arrays.copyOf(this.data, Math.max(this.data.length * 2, this.size + values.length));
            }
            System.arraycopy(values, 0, this.data, this.size, values.length);
            this.size += values.length;
        }

        int size() {
            return this.size;
        }

        float[] toArray() {
            return java.util.Arrays.copyOf(this.data, this.size);
        }
    }

    private static void upload() {
        for (int i = 0; i < UPLOADS_PER_TICK; i++) {
            Built built = DONE.poll();
            if (built == null) {
                return;
            }
            if (built.generation() != generation) {
                continue;
            }
            PENDING.remove(built.key());
            ChunkCracks old = CHUNKS.remove(built.key());
            if (old != null) {
                old.close();
            } else {
                CHECK_ORDER.add(built.key());
            }
            ChunkCracks chunk = new ChunkCracks(built.signature(), built.originX(), built.originZ(), built.sparks(), built.minY(), built.maxY(), built.heights(),
                built.states());
            for (int layer = 0; layer < 3; layer++) {
                float[] quads = built.layers()[layer];
                int count = quads.length / 7;
                if (count == 0) {
                    continue;
                }
                try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(count * 4 * DefaultVertexFormat.POSITION_COLOR.getVertexSize())) {
                    BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
                    for (int q = 0; q < count; q++) {
                        int o = q * 7;
                        float x0 = quads[o];
                        float z0 = quads[o + 1];
                        float x1 = quads[o + 2];
                        float z1 = quads[o + 3];
                        float y = quads[o + 4];
                        int r = (int)quads[o + 5];
                        int b = (int)quads[o + 6];
                        builder.addVertex(x0, y, z0).setColor(r, 0, b, 255);
                        builder.addVertex(x0, y, z1).setColor(r, 0, b, 255);
                        builder.addVertex(x1, y, z1).setColor(r, 0, b, 255);
                        builder.addVertex(x1, y, z0).setColor(r, 0, b, 255);
                    }
                    try (MeshData mesh = builder.buildOrThrow()) {
                        chunk.buffers[layer] = RenderSystem.getDevice().createBuffer(() -> "Abyss root cracks", GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
                        chunk.quads[layer] = count;
                    }
                }
            }
            CHUNKS.put(built.key(), chunk);
        }
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        if (CHUNKS.isEmpty() || !enabled()) {
            return;
        }
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        Frustum frustum = event.getLevelRenderState().cameraRenderState.cullFrustum;
        List<ChunkCracks> visible = new ArrayList<>();
        List<GpuBufferSlice> transforms = new ArrayList<>();
        int maxQuads = 0;
        for (ChunkCracks chunk : CHUNKS.values()) {
            if (chunk.empty() || !frustum.isVisible(new AABB(chunk.originX, chunk.minY - 0.5, chunk.originZ, chunk.originX + 16, chunk.maxY + 0.5, chunk.originZ + 16))) {
                continue;
            }
            float dx = (float)(chunk.originX - camera.x);
            float dy = (float)(-camera.y);
            float dz = (float)(chunk.originZ - camera.z);
            Matrix4f relative = new Matrix4f().translation(dx, dy, dz);
            Matrix4f modelView = new Matrix4f(event.getModelViewMatrix()).mul(relative);
            Vector3f offset = new Vector3f(Math.floorMod(chunk.originX, 65536), 0.0F, Math.floorMod(chunk.originZ, 65536));
            transforms.add(RenderSystem.getDynamicUniforms().writeTransform(modelView, new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), offset, relative));
            visible.add(chunk);
            for (int quads : chunk.quads) {
                maxQuads = Math.max(maxQuads, quads);
            }
        }
        if (visible.isEmpty()) {
            return;
        }
        RenderPass pass = event.getRenderPass();
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
        GpuBuffer indexBuffer = indices.getBuffer(maxQuads * 6);
        draw(pass, RIM_PIPELINE, 0, visible, transforms, indexBuffer, indices);
        draw(pass, HALO_LIGHT_PIPELINE, 1, visible, transforms, indexBuffer, indices);
        draw(pass, HALO_GLOW_PIPELINE, 1, visible, transforms, indexBuffer, indices);
        draw(pass, CORE_PIPELINE, 2, visible, transforms, indexBuffer, indices);
    }

    private static void draw(RenderPass pass, RenderPipeline pipeline, int layer, List<ChunkCracks> visible, List<GpuBufferSlice> transforms, GpuBuffer indexBuffer,
        RenderSystem.AutoStorageIndexBuffer indices) {
        pass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
        pass.setIndexBuffer(indexBuffer, indices.type());
        for (int i = 0; i < visible.size(); i++) {
            ChunkCracks chunk = visible.get(i);
            if (chunk.quads[layer] == 0) {
                continue;
            }
            pass.setUniform("DynamicTransforms", transforms.get(i));
            pass.setVertexBuffer(0, chunk.buffers[layer].slice());
            pass.drawIndexed(chunk.quads[layer] * 6, 1, 0, 0, 0);
        }
    }

    private static void sparks(Minecraft mc, BlockPos camera) {
        List<ChunkCracks> near = new ArrayList<>();
        for (ChunkCracks chunk : CHUNKS.values()) {
            if (chunk.sparks.length > 0 && Math.abs(chunk.originX + 8 - camera.getX()) < 30 && Math.abs(chunk.originZ + 8 - camera.getZ()) < 30) {
                near.add(chunk);
            }
        }
        if (near.isEmpty()) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            ChunkCracks chunk = near.get(RANDOM.nextInt(near.size()));
            int spark = RANDOM.nextInt(chunk.sparks.length / 7) * 7;
            double x = chunk.sparks[spark];
            double y = chunk.sparks[spark + 1];
            double z = chunk.sparks[spark + 2];
            if (Mth.square(x - camera.getX()) + Mth.square(z - camera.getZ()) > 22 * 22 || RANDOM.nextFloat() > 0.6F) {
                continue;
            }
            mc.level.addParticle(GlowParticleOptions.of(SPARK_COLOR, 0.035F + RANDOM.nextFloat() * 0.03F, 50 + RANDOM.nextInt(40))
                    .gravity(-0.0012F).drag(0.965F).flags(GlowParticleOptions.FADE_IN | GlowParticleOptions.SHRINK | GlowParticleOptions.FLICKER),
                x, y + 0.02, z, (RANDOM.nextDouble() - 0.5) * 0.01, 0.015 + RANDOM.nextDouble() * 0.02, (RANDOM.nextDouble() - 0.5) * 0.01);
        }
    }
}
