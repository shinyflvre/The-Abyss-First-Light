package net.yezon.theabyss.world.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;

public class GiantArchStructure extends Structure {
    public static final MapCodec<GiantArchStructure> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            settingsCodec(i),
            IntProvider.codec(40, 190).fieldOf("span").forGetter(s -> s.span),
            IntProvider.codec(20, 150).fieldOf("height").forGetter(s -> s.height),
            Codec.floatRange(0.3F, 2.0F).optionalFieldOf("thickness", 1.0F).forGetter(s -> s.thickness),
            GiantArchMaterials.CODEC.fieldOf("materials").forGetter(s -> s.materials))
        .apply(i, GiantArchStructure::new));
    public static final DeferredRegister<StructureType<?>> TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, TheabyssMod.MODID);
    public static final DeferredHolder<StructureType<?>, StructureType<GiantArchStructure>> GIANT_ARCH = TYPES.register("giant_arch", () -> () -> CODEC);
    public static final DeferredRegister<StructurePieceType> PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, TheabyssMod.MODID);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> GIANT_ARCH_PIECE = PIECES.register("giant_arch",
        () -> (context, tag) -> new GiantArchPiece(tag));
    private static final int REFERENCE_RANGE = 8 * 16;

    private final IntProvider span;
    private final IntProvider height;
    private final float thickness;
    private final GiantArchMaterials materials;

    public GiantArchStructure(Structure.StructureSettings settings, IntProvider span, IntProvider height, float thickness, GiantArchMaterials materials) {
        super(settings);
        this.span = span;
        this.height = height;
        this.thickness = thickness;
        this.materials = materials;
    }

    @Override
    public StructureType<?> type() {
        return GIANT_ARCH.get();
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        WorldgenRandom random = context.random();
        ChunkPos chunk = context.chunkPos();
        ChunkGenerator generator = context.chunkGenerator();
        LevelHeightAccessor heights = context.heightAccessor();
        RandomState state = context.randomState();
        int cx = chunk.getMiddleBlockX() + random.nextInt(9) - 4;
        int cz = chunk.getMiddleBlockZ() + random.nextInt(9) - 4;
        int span = this.span.sample(random);
        int height = this.height.sample(random);
        float scale = Math.min(1.0F, 0.55F + span / 300.0F) * this.thickness;
        float rTop = (5.0F + random.nextFloat() * 4.0F) * scale;
        float rLeg = (12.0F + random.nextFloat() * 8.0F) * scale;
        float widthRatio = 1.25F + random.nextFloat() * 0.45F;
        float bow = (random.nextFloat() - 0.5F) * 0.16F * span;
        float gamma = 0.85F + random.nextFloat() * 0.3F;
        float power = 1.6F + random.nextFloat();
        float legA = 0.85F + random.nextFloat() * 0.3F;
        float legB = 0.85F + random.nextFloat() * 0.3F;
        float twist = (random.nextFloat() - 0.5F);
        float period = 6.0F + random.nextFloat() * 3.0F;
        long seed = random.nextLong();
        int minX = chunk.getMinBlockX() - REFERENCE_RANGE;
        int maxX = chunk.getMaxBlockX() + REFERENCE_RANGE;
        int minZ = chunk.getMinBlockZ() - REFERENCE_RANGE;
        int maxZ = chunk.getMaxBlockZ() + REFERENCE_RANGE;
        GiantArchShape best = null;
        BoundingBox bestBox = null;
        double bestClearance = 0.0;
        double startAngle = random.nextDouble() * Math.PI;
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = startAngle + attempt * Math.PI / 10.0;
            double half = span / 2.0;
            int ax = (int)Math.round(cx - Math.cos(angle) * half);
            int az = (int)Math.round(cz - Math.sin(angle) * half);
            int bx = (int)Math.round(cx + Math.cos(angle) * half);
            int bz = (int)Math.round(cz + Math.sin(angle) * half);
            int ga = generator.getBaseHeight(ax, az, Heightmap.Types.WORLD_SURFACE_WG, heights, state);
            int gb = generator.getBaseHeight(bx, bz, Heightmap.Types.WORLD_SURFACE_WG, heights, state);
            if (Math.min(ga, gb) <= heights.getMinBuildHeight() + 10 || Math.abs(ga - gb) > height * 0.6) {
                continue;
            }
            float rise = height + Math.abs(ga - gb) / 2.0F;
            GiantArchShape shape = new GiantArchShape(seed, ax, ga - 2, az, bx, gb - 2, bz, rise, rTop, rLeg, widthRatio, bow, gamma, power, legA, legB,
                twist, period);
            if (shape.curveY(0.5) + rTop + 16 >= heights.getMaxBuildHeight() - 5) {
                continue;
            }
            double lowest = Double.MAX_VALUE;
            double total = 0.0;
            for (int k = 3; k <= 13; k++) {
                double t = k / 16.0;
                double y = shape.curveY(t);
                int ground = generator.getBaseHeight((int)Math.floor(shape.curveX(t)), (int)Math.floor(shape.curveZ(t)), Heightmap.Types.WORLD_SURFACE_WG,
                    heights, state);
                double free = y - shape.radius(t, y) - ground;
                lowest = Math.min(lowest, free);
                total += Math.min(free, rise);
            }
            double clearance = total / 11.0;
            if (lowest < 10.0 || clearance < rise * 0.4 || clearance <= bestClearance) {
                continue;
            }
            BoundingBox box = shape.bounds(heights.getMinBuildHeight());
            if (box.minX() < minX || box.maxX() > maxX || box.minZ() < minZ || box.maxZ() > maxZ) {
                continue;
            }
            best = shape;
            bestBox = box;
            bestClearance = clearance;
        }
        if (best == null) {
            return Optional.empty();
        }
        GiantArchShape chosen = best;
        BoundingBox chosenBox = bestBox;
        int groundY = generator.getBaseHeight(cx, cz, Heightmap.Types.WORLD_SURFACE_WG, heights, state);
        return Optional.of(new Structure.GenerationStub(new BlockPos(cx, groundY, cz), builder -> builder.addPiece(new GiantArchPiece(chosen, this.materials, chosenBox))));
    }
}
