package net.yezon.theabyss.fabric.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.platform.client.model.DynamicBlockStateModel;

final class FabricDynamicBlockStateModel implements BlockStateModel {
    private final DynamicBlockStateModel model;

    FabricDynamicBlockStateModel(DynamicBlockStateModel model) {
        this.model = model;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        this.model.collectParts(random, parts);
    }

    @Override
    public Material.Baked particleMaterial() {
        return this.model.particleMaterial();
    }

    @Override
    public int materialFlags() {
        return this.model.materialFlags();
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<Direction> cullTest) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        this.model.collectParts(level, pos, state, random, parts);
        for (int i = 0; i < parts.size(); i++) {
            parts.get(i).emitQuads(emitter, cullTest);
        }
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return this.model.particleMaterial(level, pos, state);
    }

    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return this.model.materialFlags(level, pos, state);
    }
}
