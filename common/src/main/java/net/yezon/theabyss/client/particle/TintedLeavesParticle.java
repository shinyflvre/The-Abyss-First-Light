package net.yezon.theabyss.client.particle;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TintedLeavesParticle extends TextureSheetParticle {
    private static final float ACCELERATION_SCALE = 0.0025F;
    private static final int INITIAL_LIFETIME = 300;
    private static final int CURVE_ENDPOINT_TIME = 300;
    private static final double MAXIMUM_COLLISION_VELOCITY_SQUARED = Mth.square(100.0);
    private float rotSpeed = (float)Math.toRadians(this.random.nextBoolean() ? -30.0 : 30.0);
    private final float spinAcceleration = (float)Math.toRadians(this.random.nextBoolean() ? -5.0 : 5.0);
    private final float windBig;
    private final boolean swirl;
    private final boolean flowAway;
    private final double xaFlowScale;
    private final double zaFlowScale;
    private final double swirlPeriod;
    private boolean stoppedByCollision;

    protected TintedLeavesParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite, float fallAcceleration, float sideAcceleration,
        boolean swirl, boolean flowAway, float scale, float startVelocity) {
        super(level, x, y, z);
        this.setSprite(sprite);
        this.windBig = sideAcceleration;
        this.swirl = swirl;
        this.flowAway = flowAway;
        this.lifetime = INITIAL_LIFETIME;
        this.gravity = fallAcceleration * 1.2F * ACCELERATION_SCALE;
        float size = scale * (this.random.nextBoolean() ? 0.05F : 0.075F);
        this.quadSize = size;
        this.setSize(size, size);
        this.friction = 1.0F;
        this.yd = -startVelocity;
        float particleRandom = this.random.nextFloat();
        this.xaFlowScale = Math.cos(Math.toRadians(particleRandom * 60.0F)) * this.windBig;
        this.zaFlowScale = Math.sin(Math.toRadians(particleRandom * 60.0F)) * this.windBig;
        this.swirlPeriod = Math.toRadians(1000.0F + particleRandom * 3000.0F);
    }

    public static Provider provider(SpriteSet sprites) {
        return new Provider(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
        }
        if (!this.removed) {
            float aliveTicks = CURVE_ENDPOINT_TIME - this.lifetime;
            float relativeAge = Math.min(aliveTicks / CURVE_ENDPOINT_TIME, 1.0F);
            double xa = 0.0;
            double za = 0.0;
            if (this.flowAway) {
                xa += this.xaFlowScale * Math.pow(relativeAge, 1.25);
                za += this.zaFlowScale * Math.pow(relativeAge, 1.25);
            }
            if (this.swirl) {
                xa += relativeAge * Math.cos(relativeAge * this.swirlPeriod) * this.windBig;
                za += relativeAge * Math.sin(relativeAge * this.swirlPeriod) * this.windBig;
            }
            this.xd += xa * ACCELERATION_SCALE;
            this.zd += za * ACCELERATION_SCALE;
            this.yd = this.yd - this.gravity;
            this.rotSpeed = this.rotSpeed + this.spinAcceleration / 20.0F;
            this.oRoll = this.roll;
            this.roll = this.roll + this.rotSpeed / 20.0F;
            this.move(this.xd, this.yd, this.zd);
            if (this.onGround || this.lifetime < 299 && (this.xd == 0.0 || this.zd == 0.0)) {
                this.remove();
            }
            if (!this.removed) {
                this.xd = this.xd * this.friction;
                this.yd = this.yd * this.friction;
                this.zd = this.zd * this.friction;
            }
        }
    }

    @Override
    public void move(double xa, double ya, double za) {
        if (this.stoppedByCollision) {
            return;
        }
        double originalXa = xa;
        double originalYa = ya;
        double originalZa = za;
        if (this.hasPhysics && (xa != 0.0 || ya != 0.0 || za != 0.0) && xa * xa + ya * ya + za * za < MAXIMUM_COLLISION_VELOCITY_SQUARED) {
            Vec3 movement = collide(this.level, new PositionContext(this.y), new Vec3(xa, ya, za), this.getBoundingBox());
            xa = movement.x;
            ya = movement.y;
            za = movement.z;
        }
        if (xa != 0.0 || ya != 0.0 || za != 0.0) {
            this.setBoundingBox(this.getBoundingBox().move(xa, ya, za));
            this.setLocationFromBoundingbox();
        }
        if (Math.abs(originalYa) >= 1.0E-5F && Math.abs(ya) < 1.0E-5F) {
            this.stoppedByCollision = true;
        }
        this.onGround = originalYa != ya && originalYa < 0.0;
        if (originalXa != xa) {
            this.xd = 0.0;
        }
        if (originalZa != za) {
            this.zd = 0.0;
        }
    }

    @Override
    protected int getLightColor(float partialTick) {
        BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
        return this.level.hasChunkAt(pos) ? LevelRenderer.getLightColor(this.level, pos) : 15728640;
    }

    private static Vec3 collide(ClientLevel level, CollisionContext context, Vec3 movement, AABB box) {
        List<VoxelShape> shapes = blockShapes(level, context, box.expandTowards(movement));
        if (shapes.isEmpty()) {
            return movement;
        }
        double x = movement.x;
        double y = movement.y;
        double z = movement.z;
        AABB moved = box;
        if (y != 0.0) {
            y = Shapes.collide(Direction.Axis.Y, moved, shapes, y);
            if (y != 0.0) {
                moved = moved.move(0.0, y, 0.0);
            }
        }
        boolean zFirst = Math.abs(x) < Math.abs(z);
        if (zFirst && z != 0.0) {
            z = Shapes.collide(Direction.Axis.Z, moved, shapes, z);
            if (z != 0.0) {
                moved = moved.move(0.0, 0.0, z);
            }
        }
        if (x != 0.0) {
            x = Shapes.collide(Direction.Axis.X, moved, shapes, x);
            if (!zFirst && x != 0.0) {
                moved = moved.move(x, 0.0, 0.0);
            }
        }
        if (!zFirst && z != 0.0) {
            z = Shapes.collide(Direction.Axis.Z, moved, shapes, z);
        }
        return new Vec3(x, y, z);
    }

    private static List<VoxelShape> blockShapes(ClientLevel level, CollisionContext context, AABB box) {
        List<VoxelShape> shapes = new ArrayList<>();
        VoxelShape boxShape = Shapes.create(box);
        Cursor3D cursor = new Cursor3D(Mth.floor(box.minX - 1.0E-7) - 1, Mth.floor(box.minY - 1.0E-7) - 1, Mth.floor(box.minZ - 1.0E-7) - 1,
            Mth.floor(box.maxX + 1.0E-7) + 1, Mth.floor(box.maxY + 1.0E-7) + 1, Mth.floor(box.maxZ + 1.0E-7) + 1);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockGetter chunk = null;
        long chunkKey = Long.MIN_VALUE;
        while (cursor.advance()) {
            int x = cursor.nextX();
            int y = cursor.nextY();
            int z = cursor.nextZ();
            int type = cursor.getNextType();
            if (type == 3) {
                continue;
            }
            int chunkX = SectionPos.blockToSectionCoord(x);
            int chunkZ = SectionPos.blockToSectionCoord(z);
            long key = ChunkPos.asLong(chunkX, chunkZ);
            if (key != chunkKey) {
                chunk = level.getChunkForCollisions(chunkX, chunkZ);
                chunkKey = key;
            }
            if (chunk == null) {
                continue;
            }
            pos.set(x, y, z);
            BlockState state = chunk.getBlockState(pos);
            if (type == 1 && !state.hasLargeCollisionShape() || type == 2 && !state.is(Blocks.MOVING_PISTON)) {
                continue;
            }
            VoxelShape shape = state.getCollisionShape(level, pos, context);
            if (shape == Shapes.block()) {
                if (box.intersects(x, y, z, x + 1.0, y + 1.0, z + 1.0)) {
                    shapes.add(shape.move(x, y, z));
                }
            } else {
                VoxelShape placed = shape.move(x, y, z);
                if (!placed.isEmpty() && Shapes.joinIsNotEmpty(placed, boxShape, BooleanOp.AND)) {
                    shapes.add(placed);
                }
            }
        }
        return shapes;
    }

    private record PositionContext(double y) implements CollisionContext {
        @Override
        public boolean isDescending() {
            return false;
        }

        @Override
        public boolean isAbove(VoxelShape shape, BlockPos pos, boolean defaultValue) {
            return this.y > pos.getY() + shape.max(Direction.Axis.Y) - 1.0E-5F;
        }

        @Override
        public boolean isHoldingItem(Item item) {
            return false;
        }

        @Override
        public boolean canStandOnFluid(FluidState fluidStateAbove, FluidState fluid) {
            return false;
        }
    }

    public static class Provider implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(ColorParticleOption options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux) {
            TintedLeavesParticle particle = new TintedLeavesParticle(level, x, y, z, this.sprites.get(level.random), 0.07F, 10.0F, true, false, 2.0F, 0.021F);
            particle.setColor(options.getRed(), options.getGreen(), options.getBlue());
            return particle;
        }
    }
}
