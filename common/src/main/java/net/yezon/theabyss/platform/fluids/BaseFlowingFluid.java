package net.yezon.theabyss.platform.fluids;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.yezon.theabyss.platform.common.SoundActions;
import org.jspecify.annotations.Nullable;

public abstract class BaseFlowingFluid extends FlowingFluid {
    private final Properties properties;

    protected BaseFlowingFluid(Properties properties) {
        this.properties = properties;
    }

    public FluidType abyssType() {
        return this.properties.fluidType.get();
    }

    public Supplier<? extends FluidType> abyssTypeSupplier() {
        return this.properties.fluidType;
    }

    @Override
    public Fluid getFlowing() {
        return this.properties.flowing.get();
    }

    @Override
    public Fluid getSource() {
        return this.properties.still.get();
    }

    @Override
    protected boolean canConvertToSource(ServerLevel level) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        Block.dropResources(state, level, pos, blockEntity);
    }

    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return this.properties.slopeFindDistance;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return this.properties.levelDecreasePerBlock;
    }

    @Override
    public Item getBucket() {
        Supplier<? extends Item> bucket = this.properties.bucket;
        return bucket == null ? Items.AIR : bucket.get();
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid other, Direction direction) {
        return direction == Direction.DOWN && !this.isSame(other);
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return this.properties.tickRate;
    }

    @Override
    protected float getExplosionResistance() {
        return this.properties.explosionResistance;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        Supplier<? extends LiquidBlock> block = this.properties.block;
        if (block == null) {
            return Blocks.AIR.defaultBlockState();
        }
        return block.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public boolean isSame(Fluid other) {
        return other == this.getSource() || other == this.getFlowing();
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.ofNullable(this.abyssType().getSound(SoundActions.BUCKET_FILL));
    }

    public static class Properties {
        private final Supplier<? extends FluidType> fluidType;
        private final Supplier<? extends Fluid> still;
        private final Supplier<? extends Fluid> flowing;
        private @Nullable Supplier<? extends Item> bucket;
        private @Nullable Supplier<? extends LiquidBlock> block;
        private final int slopeFindDistance = 4;
        private final int levelDecreasePerBlock = 1;
        private float explosionResistance = 1.0F;
        private final int tickRate = 5;

        public Properties(Supplier<? extends FluidType> fluidType, Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {
            this.fluidType = fluidType;
            this.still = still;
            this.flowing = flowing;
        }

        public Properties bucket(Supplier<? extends Item> bucket) {
            this.bucket = bucket;
            return this;
        }

        public Properties block(Supplier<? extends LiquidBlock> block) {
            this.block = block;
            return this;
        }

        public Properties explosionResistance(float explosionResistance) {
            this.explosionResistance = explosionResistance;
            return this;
        }
    }
}
