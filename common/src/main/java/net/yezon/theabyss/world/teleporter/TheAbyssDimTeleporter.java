package net.yezon.theabyss.world.teleporter;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.BlockUtil;
import net.minecraft.BlockUtil.FoundRectangle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.registries.RegisterEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModBlocks;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public class TheAbyssDimTeleporter {
    public static final ResourceKey<PoiType> POI = ResourceKey.create(
        Registries.POINT_OF_INTEREST_TYPE, ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "the_abyss_portal")
    );
    private final ServerLevel level;
    private final BlockPos entityEnterPos;

    @SubscribeEvent
    public static void registerPointOfInterest(RegisterEvent event) {
        event.register(
            Registries.POINT_OF_INTEREST_TYPE,
            POI.location(),
            () -> new PoiType(ImmutableSet.copyOf(TheabyssModBlocks.THE_ABYSS_PORTAL.get().getStateDefinition().getPossibleStates()), 0, 1)
        );
    }

    public TheAbyssDimTeleporter(ServerLevel level, BlockPos entityEnterPos) {
        this.level = level;
        this.entityEnterPos = entityEnterPos;
    }

    public Optional<FoundRectangle> findPortalAround(BlockPos pos, boolean smallRadius, WorldBorder worldBorder) {
        PoiManager poiManager = this.level.getPoiManager();
        int radius = smallRadius ? 16 : 128;
        poiManager.ensureLoadedAndValid(this.level, pos, radius);
        return poiManager.getInSquare(type -> type.is(POI), pos, radius, PoiManager.Occupancy.ANY)
            .map(PoiRecord::getPos)
            .filter(worldBorder::isWithinBounds)
            .filter(poiPos -> this.level.getBlockState(poiPos).hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
            .min(Comparator.<BlockPos>comparingDouble(poiPos -> poiPos.distSqr(pos)).thenComparingInt(Vec3i::getY))
            .map(poiPos -> {
                this.level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(poiPos), 3, poiPos);
                BlockState portalState = this.level.getBlockState(poiPos);
                return BlockUtil.getLargestRectangleAround(
                    poiPos,
                    portalState.getValue(BlockStateProperties.HORIZONTAL_AXIS),
                    PortalShape.MAX_WIDTH,
                    Axis.Y,
                    PortalShape.MAX_HEIGHT,
                    rectPos -> this.level.getBlockState(rectPos) == portalState
                );
            });
    }

    public Optional<FoundRectangle> createPortal(BlockPos origin, Axis portalAxis) {
        Direction direction = Direction.get(AxisDirection.POSITIVE, portalAxis);
        double closestFullDistanceSqr = -1.0;
        BlockPos closestFullPosition = null;
        double closestPartialDistanceSqr = -1.0;
        BlockPos closestPartialPosition = null;
        WorldBorder worldBorder = this.level.getWorldBorder();
        int maxPlaceableY = Math.min(this.level.getMaxBuildHeight() - 1, this.level.getMinBuildHeight() + this.level.getLogicalHeight() - 1);
        MutableBlockPos mutable = origin.mutable();

        for (MutableBlockPos columnPos : BlockPos.spiralAround(origin, 16, Direction.EAST, Direction.SOUTH)) {
            int height = Math.min(maxPlaceableY, this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, columnPos.getX(), columnPos.getZ()));
            if (worldBorder.isWithinBounds(columnPos) && worldBorder.isWithinBounds(columnPos.move(direction, 1))) {
                columnPos.move(direction.getOpposite(), 1);

                for (int y = height; y >= this.level.getMinBuildHeight(); y--) {
                    columnPos.setY(y);
                    if (this.canPortalReplaceBlock(columnPos)) {
                        int firstEmptyY = y;

                        while (y > this.level.getMinBuildHeight() && this.canPortalReplaceBlock(columnPos.move(Direction.DOWN))) {
                            y--;
                        }

                        if (y + 4 <= maxPlaceableY) {
                            int deltaY = firstEmptyY - y;
                            if (deltaY <= 0 || deltaY >= 3) {
                                columnPos.setY(y);
                                if (this.canHostFrame(columnPos, mutable, direction, 0)) {
                                    double distance = origin.distSqr(columnPos);
                                    if (this.canHostFrame(columnPos, mutable, direction, -1)
                                        && this.canHostFrame(columnPos, mutable, direction, 1)
                                        && (closestFullDistanceSqr == -1.0 || closestFullDistanceSqr > distance)) {
                                        closestFullDistanceSqr = distance;
                                        closestFullPosition = columnPos.immutable();
                                    }

                                    if (closestFullDistanceSqr == -1.0 && (closestPartialDistanceSqr == -1.0 || closestPartialDistanceSqr > distance)) {
                                        closestPartialDistanceSqr = distance;
                                        closestPartialPosition = columnPos.immutable();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (closestFullDistanceSqr == -1.0 && closestPartialDistanceSqr != -1.0) {
            closestFullPosition = closestPartialPosition;
            closestFullDistanceSqr = closestPartialDistanceSqr;
        }

        if (closestFullDistanceSqr == -1.0) {
            int minStartY = Math.max(this.level.getMinBuildHeight() + 1, 70);
            int maxStartY = maxPlaceableY - 9;
            if (maxStartY < minStartY) {
                return Optional.empty();
            }

            closestFullPosition = new BlockPos(origin.getX(), Mth.clamp(origin.getY(), minStartY, maxStartY), origin.getZ()).immutable();
            Direction clockWise = direction.getClockWise();
            if (!worldBorder.isWithinBounds(closestFullPosition)) {
                return Optional.empty();
            }

            for (int box = -1; box < 2; box++) {
                for (int width = 0; width < 2; width++) {
                    for (int height = -1; height < 3; height++) {
                        BlockState blockState = height < 0 ? TheabyssModBlocks.UNSTABLE_OBSIDIAN.get().defaultBlockState() : Blocks.AIR.defaultBlockState();
                        mutable.setWithOffset(
                            closestFullPosition,
                            width * direction.getStepX() + box * clockWise.getStepX(),
                            height,
                            width * direction.getStepZ() + box * clockWise.getStepZ()
                        );
                        this.level.setBlockAndUpdate(mutable, blockState);
                    }
                }
            }
        }

        for (int width = -1; width < 3; width++) {
            for (int height = -1; height < 4; height++) {
                if (width == -1 || width == 2 || height == -1 || height == 3) {
                    mutable.setWithOffset(closestFullPosition, width * direction.getStepX(), height, width * direction.getStepZ());
                    this.level.setBlock(mutable, TheabyssModBlocks.UNSTABLE_OBSIDIAN.get().defaultBlockState(), 3);
                }
            }
        }

        BlockState portalState = TheabyssModBlocks.THE_ABYSS_PORTAL.get().defaultBlockState().setValue(NetherPortalBlock.AXIS, portalAxis);

        for (int width = 0; width < 2; width++) {
            for (int height = 0; height < 3; height++) {
                mutable.setWithOffset(closestFullPosition, width * direction.getStepX(), height, width * direction.getStepZ());
                this.level.setBlock(mutable, portalState, 18);
            }
        }

        return Optional.of(new FoundRectangle(closestFullPosition.immutable(), 2, 3));
    }

    private boolean canHostFrame(BlockPos origin, MutableBlockPos mutable, Direction direction, int offset) {
        Direction clockWise = direction.getClockWise();

        for (int width = -1; width < 3; width++) {
            for (int height = -1; height < 4; height++) {
                mutable.setWithOffset(
                    origin, direction.getStepX() * width + clockWise.getStepX() * offset, height, direction.getStepZ() * width + clockWise.getStepZ() * offset
                );
                if (height < 0 && !this.level.getBlockState(mutable).isSolid()) {
                    return false;
                }

                if (height >= 0 && !this.canPortalReplaceBlock(mutable)) {
                    return false;
                }
            }
        }

        return true;
    }

    public DimensionTransition getPortalDestination(Entity entity) {
        WorldBorder worldBorder = this.level.getWorldBorder();
        double scale = DimensionType.getTeleportationScale(entity.level().dimensionType(), this.level.dimensionType());
        BlockPos approximateExitPos = worldBorder.clampToBounds(entity.getX() * scale, entity.getY(), entity.getZ() * scale);
        return this.getExitPortal(entity, approximateExitPos, worldBorder)
            .map(exitPortal -> this.createTransition(entity, exitPortal))
            .orElseGet(() -> new DimensionTransition(
                this.level, entity.position(), Vec3.ZERO, entity.getYRot(), entity.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND
            ));
    }

    protected Optional<FoundRectangle> getExitPortal(Entity entity, BlockPos pos, WorldBorder worldBorder) {
        Optional<FoundRectangle> existing = this.findPortalAround(pos, false, worldBorder);
        if (existing.isPresent() || !(entity instanceof ServerPlayer)) {
            return existing;
        }

        Axis entryAxis = entity.level().getBlockState(this.entityEnterPos).getOptionalValue(NetherPortalBlock.AXIS).orElse(Axis.X);
        return this.createPortal(pos, entryAxis);
    }

    private DimensionTransition createTransition(Entity entity, FoundRectangle exitPortal) {
        BlockState entryState = entity.level().getBlockState(this.entityEnterPos);
        Axis entryAxis;
        Vec3 offset;
        if (entryState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
            entryAxis = entryState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
            FoundRectangle entryArea = BlockUtil.getLargestRectangleAround(
                this.entityEnterPos, entryAxis, PortalShape.MAX_WIDTH, Axis.Y, PortalShape.MAX_HEIGHT, pos -> entity.level().getBlockState(pos) == entryState
            );
            offset = entity.getRelativePortalPosition(entryAxis, entryArea);
        } else {
            entryAxis = Axis.X;
            offset = new Vec3(0.5, 0.0, 0.0);
        }

        BlockPos bottomLeft = exitPortal.minCorner;
        Axis exitAxis = this.level.getBlockState(bottomLeft).getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS).orElse(Axis.X);
        EntityDimensions dimensions = entity.getDimensions(entity.getPose());
        int outputRotation = entryAxis == exitAxis ? 0 : 90;
        double offsetRight = dimensions.width() / 2.0 + (exitPortal.axis1Size - dimensions.width()) * offset.x();
        double offsetUp = (exitPortal.axis2Size - dimensions.height()) * offset.y();
        double offsetForward = 0.5 + offset.z();
        boolean xAligned = exitAxis == Axis.X;
        Vec3 targetPos = new Vec3(
            bottomLeft.getX() + (xAligned ? offsetRight : offsetForward),
            bottomLeft.getY() + offsetUp,
            bottomLeft.getZ() + (xAligned ? offsetForward : offsetRight)
        );
        Vec3 collisionFreePos = PortalShape.findCollisionFreePosition(targetPos, this.level, entity, dimensions);
        Vec3 speed = entryAxis == exitAxis ? entity.getDeltaMovement() : new Vec3(entity.getDeltaMovement().z, entity.getDeltaMovement().y, -entity.getDeltaMovement().x);
        return new DimensionTransition(
            this.level,
            collisionFreePos,
            speed,
            entity.getYRot() + outputRotation,
            entity.getXRot(),
            DimensionTransition.PLAY_PORTAL_SOUND
        );
    }

    private boolean canPortalReplaceBlock(MutableBlockPos pos) {
        BlockState blockState = this.level.getBlockState(pos);
        return blockState.canBeReplaced() && blockState.getFluidState().isEmpty();
    }
}
