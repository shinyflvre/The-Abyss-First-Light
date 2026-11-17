package net.yezon.theabyss.neoforge.client;

import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.common.util.TriState;
import net.yezon.theabyss.platform.client.model.DynamicBlockStateModel;
import org.jspecify.annotations.Nullable;

final class NeoForgeDynamicBlockStateModel extends BakedModelWrapper<BakedModel> {
    private static final ModelProperty<DynamicBlockStateModel.Delegate> DELEGATE = new ModelProperty<>();
    private static final ModelProperty<ModelData> INNER = new ModelProperty<>();

    private final DynamicBlockStateModel model;

    NeoForgeDynamicBlockStateModel(DynamicBlockStateModel model) {
        super(model.fallback().model());
        this.model = model;
    }

    private DynamicBlockStateModel.Delegate delegate(ModelData data) {
        DynamicBlockStateModel.Delegate delegate = data.get(DELEGATE);
        return delegate != null ? delegate : this.model.fallback();
    }

    private static ModelData inner(ModelData data) {
        ModelData inner = data.get(INNER);
        return inner != null ? inner : ModelData.EMPTY;
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        DynamicBlockStateModel.Delegate delegate = this.model.select(level, pos, state);
        ModelData inner = delegate.model().getModelData(level, pos, delegate.state(), modelData);
        return ModelData.builder().with(DELEGATE, delegate).with(INNER, inner).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        DynamicBlockStateModel.Delegate delegate = this.delegate(extraData);
        return delegate.model().getQuads(delegate.state(), side, rand, inner(extraData), renderType);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        DynamicBlockStateModel.Delegate delegate = this.delegate(data);
        return delegate.model().getRenderTypes(delegate.state(), rand, inner(data));
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        DynamicBlockStateModel.Delegate delegate = this.delegate(data);
        return delegate.model().getParticleIcon(inner(data));
    }

    @Override
    public TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) {
        DynamicBlockStateModel.Delegate delegate = this.delegate(data);
        return delegate.model().useAmbientOcclusion(delegate.state(), inner(data), renderType);
    }
}
