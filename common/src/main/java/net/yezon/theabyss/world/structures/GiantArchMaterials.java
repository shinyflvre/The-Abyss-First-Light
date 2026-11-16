package net.yezon.theabyss.world.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public record GiantArchMaterials(Block rock, Block band, Block seam, Optional<Block> vein, Block top, Block drip, Optional<Block> dripTip,
    Optional<Block> crown, Optional<Block> pillar, Optional<Block> pillarCap) {
    private static final Codec<Block> BLOCK = BuiltInRegistries.BLOCK.byNameCodec();
    public static final Codec<GiantArchMaterials> CODEC = RecordCodecBuilder.create(i -> i.group(
            BLOCK.fieldOf("rock").forGetter(GiantArchMaterials::rock),
            BLOCK.fieldOf("band").forGetter(GiantArchMaterials::band),
            BLOCK.fieldOf("seam").forGetter(GiantArchMaterials::seam),
            BLOCK.optionalFieldOf("vein").forGetter(GiantArchMaterials::vein),
            BLOCK.fieldOf("top").forGetter(GiantArchMaterials::top),
            BLOCK.fieldOf("drip").forGetter(GiantArchMaterials::drip),
            BLOCK.optionalFieldOf("drip_tip").forGetter(GiantArchMaterials::dripTip),
            BLOCK.optionalFieldOf("crown").forGetter(GiantArchMaterials::crown),
            BLOCK.optionalFieldOf("pillar").forGetter(GiantArchMaterials::pillar),
            BLOCK.optionalFieldOf("pillar_cap").forGetter(GiantArchMaterials::pillarCap))
        .apply(i, GiantArchMaterials::new));
}
