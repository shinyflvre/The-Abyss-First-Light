package net.yezon.theabyss.world.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.block.flora.HangingPlantBodyBlock;
import net.yezon.theabyss.block.flora.HangingPlantHeadBlock;

public record HangingPlants(Block plant, float chance, IntProvider length, Optional<HolderSet<Block>> attachTo) {
    public static final Codec<HangingPlants> CODEC = RecordCodecBuilder.create(i -> i.group(
        BuiltInRegistries.BLOCK.byNameCodec().fieldOf("plant").forGetter(HangingPlants::plant),
        Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(HangingPlants::chance),
        IntProviders.codec(1, 32).fieldOf("length").forGetter(HangingPlants::length),
        RegistryCodecs.holderSet(Registries.BLOCK).optionalFieldOf("attach_to").forGetter(HangingPlants::attachTo)
    ).apply(i, HangingPlants::new));

    public void decorate(WorldGenLevel level, RandomSource random, Iterable<BlockPos> anchors) {
        if (!(this.plant instanceof HangingPlantHeadBlock head)) {
            return;
        }
        BlockState tip = head.getMaxAgeState(head.defaultBlockState());
        BlockState body = head.body().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (BlockPos anchor : anchors) {
            pos.set(anchor);
            if (this.attachTo.isPresent() && !level.getBlockState(pos).is(this.attachTo.get())) {
                continue;
            }
            pos.move(Direction.DOWN);
            if (!level.getBlockState(pos).isAir() || random.nextFloat() >= this.chance) {
                continue;
            }
            int wanted = this.length.sample(random);
            int free = 0;
            while (free < wanted && level.isInsideBuildHeight(pos) && level.getBlockState(pos).isAir()) {
                free++;
                pos.move(Direction.DOWN);
            }
            for (int i = 1; i <= free; i++) {
                pos.set(anchor.getX(), anchor.getY() - i, anchor.getZ());
                level.setBlock(pos, i == free ? tip : body.setValue(HangingPlantBodyBlock.TOP, i == 1), Block.UPDATE_CLIENTS);
            }
        }
    }
}
