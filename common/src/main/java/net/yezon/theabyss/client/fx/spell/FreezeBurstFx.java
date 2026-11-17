package net.yezon.theabyss.client.fx.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.RegisterShadersEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.SpellFxMessage;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class FreezeBurstFx extends Effect {
    static final float FLASH_TINT = 2.3773587F;
    static final float GLOW_TINT = 1.0F;
    static final float SPARK_TINT = 6.3479204F;
    private static final float CRATER_TINT = 4.0000005F;
    private static final RenderType FLASH = type(FLASH_TINT, "flash");
    private static final RenderType GLOW = type(GLOW_TINT, "glow");
    static final RenderType SPARK = type(SPARK_TINT, "spark");
    static final RenderType CRATER = type(CRATER_TINT, "crater");

    private static final float STEP = 0.025F;
    private static final float BURST_TIME = 0.15F;
    private static final float CRATER_TIME = 0.29F;
    private static final float FLASH_LIFE = 0.1F;
    private static final float FLASH_SIZE = 3.0F;
    private static final float GLOW_LIFE = 1.2F;
    private static final float GLOW_SIZE = 8.0F;
    private static final float GLOW_ALPHA = 0.29803923F;
    private static final float CRATER_LIFE = 1.2F;
    private static final double CRATER_SIZE = 8.0;
    private static final double CRATER_REACH = 4.0;
    private static final float PIVOT = 0.25F;
    private static final int GLOW_CELLS = 6;
    private static final int SPARKS = 20;
    private static final int LATE_SPARKS = 25;
    private static final float LATE_INTERVAL = 0.04F;
    private static final double LATE_RADIUS = 3.0;
    private static final float SPARK_LENGTH = 2.0F;
    private static final float SPARK_STRETCH = 0.1F;
    private static final float LATE_LENGTH = 3.0F;
    private static final double SPARK_DAMPING = Math.pow(1.0 - 0.17, STEP * 30.0);
    private static final double LATE_DAMPING = Math.pow(1.0 - 0.05, STEP * 30.0);
    private static final float TOTAL_LIFE = 88.0F;
    private static final double DECAL_LIFT = 0.02;
    private static final int SCAN_DEPTH = 8;
    private static final double MAX_STEP = 1.25;
    private static final double LOW_COVER = 0.5;
    private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);

    private static final int FLASH_RGB = rgb(0.07843137F, 0.2867299F, 1.0F);
    private static final int GLOW_RGB = rgb(0.062745094F, 0.36876726F, 1.0F);
    private static final int SPARK_RGB = rgb(0.02128871F, 0.03799522F, 0.122641504F);
    private static final int CRATER_RGB = rgb(0.11210393F, 0.40748855F, 0.8867924F);
    private static final float LATE_R = 0.09170522F;
    private static final float LATE_G = 0.21585593F;
    private static final float LATE_B = 0.5283019F;
    private static final float LATE_FADE_START = 14842.0F / 65535.0F;

    private static final float[][] FLASH_SIZE_CURVE = {{0.0F, 0.0F, 2.0F, 2.0F}, {1.0F, 1.0F, 0.0F, 0.0F}};
    private static final float[][] GLOW_SIZE_CURVE = {{0.0F, 0.0F, 9.1414F, 9.1414F}, {0.2023F, 0.8719F, 0.6952F, 0.6952F}, {1.0F, 1.0F, 0.0F, 0.0F}};
    private static final float[][] SPARK_SIZE_CURVE = {{0.0F, 0.0F, 0.0F, 0.0F}, {0.1758F, 1.0F, 0.0F, 0.0F}, {0.5F, 1.0F, 0.0F, 0.0F}, {1.0F, 0.0F, -3.8708F, -3.8708F}};
    private static final float[][] LATE_SIZE_CURVE = {{0.0F, 1.0F, 0.0F, 0.0F}, {1.0F, 0.0F, -2.0F, -2.0F}};
    private static final float[][] SPEED_LIMIT_CURVE = {{0.0F, 1.0F, -2.0F, -2.0F}, {1.0F, 0.0F, 0.0F, 0.0F}};
    private static final float[][] FLASH_ALPHA = {{0.0F, 0.0F}, {13107.0F / 65535.0F, 1.0F}, {59367.0F / 65535.0F, 1.0F}, {1.0F, 0.0F}};
    private static final float[][] GLOW_FADE = {{0.0F, 0.0F}, {6554.0F / 65535.0F, 1.0F}, {20817.0F / 65535.0F, 0.902F}, {36044.0F / 65535.0F, 0.573F}, {50115.0F / 65535.0F, 0.255F}, {1.0F, 0.0F}};
    private static final int LIGHT_RGB = 0x005CFF;
    private static final float LIGHT_RANGE = 10.0F;
    private static final float LIGHT_LIFE = 1.0F;
    private static final float[][] LIGHT_FADE = {{0.0F, 0.0F}, {0.136F, 1.0F}, {1.0F, 0.0F}};
    private static final float[][] CRATER_ALPHA = {{0.0F, 0.0F}, {14264.0F / 65535.0F, 1.0F}, {1.0F, 0.0F}};

    private final Vec3 origin;
    private final float flashAngle;
    private final float craterAngle;
    private final Spark[] sparks = new Spark[SPARKS + LATE_SPARKS];
    private int steps;
    private double groundY = Double.NaN;
    private List<Cell> cells;

    public FreezeBurstFx(SpellFxMessage m) {
        this.origin = SpellFx.vec(m, 0);
        RandomSource random = RandomSource.create();
        this.flashAngle = random.nextFloat() * Mth.TWO_PI;
        this.craterAngle = random.nextFloat() * Mth.TWO_PI;
        for (int i = 0; i < SPARKS; i++) {
            Vec3 dir = sphere(random);
            dir = new Vec3(dir.x, Math.abs(dir.y), dir.z);
            this.sparks[i] = new Spark(false, BURST_TIME, range(random, 0.4F, 1.0F), range(random, 0.05F, 0.15F), range(random, -4.0F, -2.0F),
                this.origin, dir.scale(range(random, 10.0F, 30.0F)));
        }
        for (int i = 0; i < LATE_SPARKS; i++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double r = LATE_RADIUS * Math.sqrt(random.nextDouble());
            this.sparks[SPARKS + i] = new Spark(true, BURST_TIME + i * LATE_INTERVAL, range(random, 0.4F, 1.0F), range(random, 0.05F, 0.1F), 0.0F,
                this.origin.add(Math.cos(a) * r, 0.0, Math.sin(a) * r), sphere(random).scale(range(random, 0.5F, 2.0F)));
        }
    }

    private static RenderType type(float tint, String texture) {
        return SpellRenderTypes.unlit("theabyss_freeze_burst_" + texture,
            ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "textures/fx/freeze/" + texture + ".png"), tint);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        SpellRenderTypes.registerShaders(event);
    }

    @Override
    public float life() {
        return TOTAL_LIFE;
    }

    @Override
    public void draw(Frame frame) {
        float age = this.age();
        float sim = age * STEP;
        int whole = (int)age;
        while (this.steps <= whole) {
            this.step();
        }
        float blend = age - whole;
        if (sim < BURST_TIME) {
            return;
        }
        if (this.cells == null) {
            this.prepare(frame.level);
        }
        net.yezon.theabyss.client.fx.MagicLights.add(this.origin.x, this.groundY + 1.1, this.origin.z, LIGHT_RGB, LIGHT_RANGE, ramp(LIGHT_FADE, (sim - BURST_TIME) / LIGHT_LIFE));
        Vec3 centre = frame.rel(this.origin);
        this.drawCrater(frame, sim);
        this.drawGlow(frame, centre, sim);
        this.drawSparks(frame, sim, blend);
        this.drawFlash(frame, centre, sim);
    }

    private void step() {
        float now = this.steps * STEP;
        this.steps++;
        for (Spark s : this.sparks) {
            float t = (now - s.spawn) / s.life;
            if (t >= 0.0F && t <= 1.0F) {
                s.advance(t);
            }
        }
    }

    private void drawCrater(Frame frame, float sim) {
        float t = (sim - CRATER_TIME) / CRATER_LIFE;
        if (t < 0.0F || t > 1.0F || this.cells.isEmpty()) {
            return;
        }
        int colour = pack(CRATER_RGB, ramp(CRATER_ALPHA, t));
        double cos = Math.cos(this.craterAngle);
        double sin = Math.sin(this.craterAngle);
        Mesh mesh = new Mesh(this.cells.size());
        for (Cell c : this.cells) {
            double y = c.top + DECAL_LIFT;
            this.decal(mesh, frame.camera, c.x, y, c.z, cos, sin, colour);
            this.decal(mesh, frame.camera, c.x + 1.0, y, c.z, cos, sin, colour);
            this.decal(mesh, frame.camera, c.x + 1.0, y, c.z + 1.0, cos, sin, colour);
            this.decal(mesh, frame.camera, c.x, y, c.z + 1.0, cos, sin, colour);
        }
        mesh.submit(frame, CRATER);
    }

    private void decal(Mesh mesh, Vec3 camera, double x, double y, double z, double cos, double sin, int colour) {
        double dx = x - this.origin.x;
        double dz = z - this.origin.z;
        double lx = dx * cos + dz * sin;
        double lz = dz * cos - dx * sin;
        mesh.vertex(x - camera.x, y - camera.y, z - camera.z, (float)(lx / CRATER_SIZE + 0.5), (float)(0.5 - lz / CRATER_SIZE), colour);
    }

    private void drawGlow(Frame frame, Vec3 centre, float sim) {
        float t = (sim - BURST_TIME) / GLOW_LIFE;
        if (t < 0.0F || t > 1.0F) {
            return;
        }
        Mesh mesh = new Mesh(GLOW_CELLS * GLOW_CELLS);
        facing(mesh, centre, GLOW_SIZE * curve(GLOW_SIZE_CURVE, t), 0.0F, GLOW_RGB, GLOW_ALPHA * ramp(GLOW_FADE, t), GLOW_CELLS, this.groundY - frame.camera.y);
        mesh.submit(frame, GLOW);
    }

    private void drawFlash(Frame frame, Vec3 centre, float sim) {
        float t = (sim - BURST_TIME) / FLASH_LIFE;
        if (t < 0.0F || t > 1.0F) {
            return;
        }
        Mesh mesh = new Mesh(1);
        facing(mesh, centre, FLASH_SIZE * curve(FLASH_SIZE_CURVE, t), this.flashAngle, FLASH_RGB, ramp(FLASH_ALPHA, t), 1, Double.NaN);
        mesh.submit(frame, FLASH);
    }

    private void drawSparks(Frame frame, float sim, float blend) {
        Mesh mesh = new Mesh(this.sparks.length);
        for (Spark s : this.sparks) {
            float t = (sim - s.spawn) / s.life;
            if (t < 0.0F || t > 1.0F) {
                continue;
            }
            double vx = Mth.lerp(blend, s.oldVx, s.vx);
            double vy = Mth.lerp(blend, s.oldVy, s.vy);
            double vz = Mth.lerp(blend, s.oldVz, s.vz);
            double speed = Math.sqrt(vx * vx + vy * vy + vz * vz);
            if (speed < 1.0E-5) {
                continue;
            }
            float size = s.size * curve(s.late ? LATE_SIZE_CURVE : SPARK_SIZE_CURVE, t);
            int rgb = SPARK_RGB;
            if (s.late) {
                float fade = Math.max(0.0F, (t - LATE_FADE_START) / (1.0F - LATE_FADE_START));
                rgb = rgb(LATE_R, LATE_G * Mth.lerp(fade, 1.0F, 0.27F), LATE_B * (1.0F - fade));
            } else {
                size *= (float)Math.min(speed, 1.0);
            }
            if (size <= 0.0F) {
                continue;
            }
            Vec3 axis = new Vec3(vx / speed, vy / speed, vz / speed);
            double length = s.late ? size * LATE_LENGTH : size * SPARK_LENGTH + speed * SPARK_STRETCH;
            Vec3 head = new Vec3(Mth.lerp(blend, s.oldX, s.x) - frame.camera.x, Mth.lerp(blend, s.oldY, s.y) - frame.camera.y, Mth.lerp(blend, s.oldZ, s.z) - frame.camera.z);
            Vec3 side = axis.cross(head);
            if (side.lengthSqr() < 1.0E-8) {
                continue;
            }
            side = side.normalize().scale(size * 0.5);
            Vec3 tail = head.subtract(axis.scale(length));
            int colour = pack(rgb, 1.0F);
            mesh.vertex(head.add(side), 1.0F, 0.0F, colour);
            mesh.vertex(head.subtract(side), 0.0F, 0.0F, colour);
            mesh.vertex(tail.subtract(side), 0.0F, 1.0F, colour);
            mesh.vertex(tail.add(side), 1.0F, 1.0F, colour);
        }
        mesh.submit(frame, SPARK);
    }

    private static void facing(Mesh mesh, Vec3 centre, float size, float angle, int rgb, float alpha, int cells, double ground) {
        double distance = centre.length();
        if (distance < 1.0E-3 || size <= 0.0F || alpha <= 0.0F) {
            return;
        }
        Vec3 toCamera = centre.scale(-1.0 / distance);
        Vec3 middle = centre.add(toCamera.scale(Math.min(size * PIVOT, distance * 0.5)));
        Vec3 right = toCamera.cross(UP);
        right = right.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : right.normalize();
        Vec3 up = right.cross(toCamera);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        Vec3 axisU = right.scale(cos * size).add(up.scale(sin * size));
        Vec3 axisV = up.scale(cos * size).subtract(right.scale(sin * size));
        float cell = 1.0F / cells;
        for (int i = 0; i < cells; i++) {
            for (int j = 0; j < cells; j++) {
                float u0 = i * cell;
                float v0 = j * cell;
                float u1 = u0 + cell;
                float v1 = v0 + cell;
                corner(mesh, middle, axisU, axisV, u0, v0, rgb, alpha, ground);
                corner(mesh, middle, axisU, axisV, u1, v0, rgb, alpha, ground);
                corner(mesh, middle, axisU, axisV, u1, v1, rgb, alpha, ground);
                corner(mesh, middle, axisU, axisV, u0, v1, rgb, alpha, ground);
            }
        }
    }

    private static void corner(Mesh mesh, Vec3 middle, Vec3 axisU, Vec3 axisV, float u, float v, int rgb, float alpha, double ground) {
        Vec3 p = middle.add(axisU.scale(u - 0.5)).add(axisV.scale(0.5 - v));
        mesh.vertex(p, u, v, pack(rgb, alpha * soften(p, ground)));
    }

    private static float soften(Vec3 p, double ground) {
        if (Double.isNaN(ground) || ground >= 0.0 || p.y >= 0.0) {
            return 1.0F;
        }
        if (p.y <= ground) {
            return 0.0F;
        }
        return (float)Math.min(1.0, p.length() * (ground / p.y - 1.0));
    }

    private void prepare(ClientLevel level) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int cx = Mth.floor(this.origin.x);
        int cz = Mth.floor(this.origin.z);
        double top = surface(level, pos, cx, cz, this.origin.y);
        this.groundY = Double.isNaN(top) ? this.origin.y : top;
        this.cells = new ArrayList<>();
        int reach = Mth.ceil(CRATER_REACH);
        for (int x = cx - reach; x <= cx + reach; x++) {
            for (int z = cz - reach; z <= cz + reach; z++) {
                double nx = Mth.clamp(this.origin.x, x, x + 1.0) - this.origin.x;
                double nz = Mth.clamp(this.origin.z, z, z + 1.0) - this.origin.z;
                if (nx * nx + nz * nz > CRATER_REACH * CRATER_REACH) {
                    continue;
                }
                double y = surface(level, pos, x, z, this.origin.y);
                if (!Double.isNaN(y)) {
                    this.cells.add(new Cell(x, z, y));
                }
            }
        }
    }

    static double surface(ClientLevel level, BlockPos.MutableBlockPos pos, int x, int z, double feet) {
        int start = Mth.floor(feet + 1.0);
        for (int y = start; y >= start - SCAN_DEPTH; y--) {
            pos.set(x, y, z);
            BlockState state = level.getBlockState(pos);
            FluidState fluid = state.getFluidState();
            if (!fluid.isEmpty()) {
                return y + fluid.getHeight(level, pos);
            }
            VoxelShape shape = state.getCollisionShape(level, pos);
            if (shape.isEmpty()) {
                shape = state.getShape(level, pos);
                if (shape.isEmpty() || shape.max(Direction.Axis.Y) > LOW_COVER) {
                    continue;
                }
            }
            double top = y + shape.max(Direction.Axis.Y);
            return top <= feet + MAX_STEP ? top : Double.NaN;
        }
        return Double.NaN;
    }

    private static Vec3 sphere(RandomSource random) {
        double y = random.nextDouble() * 2.0 - 1.0;
        double a = random.nextDouble() * Math.PI * 2.0;
        double r = Math.sqrt(Math.max(0.0, 1.0 - y * y));
        return new Vec3(Math.cos(a) * r, y, Math.sin(a) * r);
    }

    private static float range(RandomSource random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private static int rgb(float r, float g, float b) {
        return Math.round(r * 255.0F) << 16 | Math.round(g * 255.0F) << 8 | Math.round(b * 255.0F);
    }

    static int pack(int rgb, float alpha) {
        int a = Math.round((float)Math.sqrt(Mth.clamp(alpha, 0.0F, 1.0F)) * 255.0F);
        return a << 24 | rgb;
    }

    private static float curve(float[][] keys, float t) {
        for (int i = 0; i + 1 < keys.length; i++) {
            float[] a = keys[i];
            float[] b = keys[i + 1];
            if (t <= b[0]) {
                float span = b[0] - a[0];
                float k = Math.max(0.0F, t - a[0]) / span;
                float k2 = k * k;
                float k3 = k2 * k;
                return (2.0F * k3 - 3.0F * k2 + 1.0F) * a[1] + (k3 - 2.0F * k2 + k) * span * a[3]
                    + (3.0F * k2 - 2.0F * k3) * b[1] + (k3 - k2) * span * b[2];
            }
        }
        return keys[keys.length - 1][1];
    }

    private static float ramp(float[][] keys, float t) {
        for (int i = 0; i + 1 < keys.length; i++) {
            float[] a = keys[i];
            float[] b = keys[i + 1];
            if (t <= b[0]) {
                return Mth.lerp(Math.max(0.0F, t - a[0]) / (b[0] - a[0]), a[1], b[1]);
            }
        }
        return keys[keys.length - 1][1];
    }

    private record Cell(int x, int z, double top) {
    }

    private static final class Spark {
        final boolean late;
        final float spawn;
        final float life;
        final float size;
        final float gravity;
        double x;
        double y;
        double z;
        double oldX;
        double oldY;
        double oldZ;
        double vx;
        double vy;
        double vz;
        double oldVx;
        double oldVy;
        double oldVz;

        Spark(boolean late, float spawn, float life, float size, float gravity, Vec3 at, Vec3 velocity) {
            this.late = late;
            this.spawn = spawn;
            this.life = life;
            this.size = size;
            this.gravity = gravity;
            this.x = this.oldX = at.x;
            this.y = this.oldY = at.y;
            this.z = this.oldZ = at.z;
            this.vx = this.oldVx = velocity.x;
            this.vy = this.oldVy = velocity.y;
            this.vz = this.oldVz = velocity.z;
        }

        void advance(float t) {
            this.oldX = this.x;
            this.oldY = this.y;
            this.oldZ = this.z;
            this.oldVx = this.vx;
            this.oldVy = this.vy;
            this.oldVz = this.vz;
            this.vy += this.gravity * STEP;
            double speed = Math.sqrt(this.vx * this.vx + this.vy * this.vy + this.vz * this.vz);
            float limit = curve(SPEED_LIMIT_CURVE, t);
            if (speed > limit && speed > 0.0) {
                double scale = (limit + (speed - limit) * (this.late ? LATE_DAMPING : SPARK_DAMPING)) / speed;
                this.vx *= scale;
                this.vy *= scale;
                this.vz *= scale;
            }
            this.x += this.vx * STEP;
            this.y += this.vy * STEP;
            this.z += this.vz * STEP;
        }
    }

    static final class Mesh {
        private float[] floats;
        private int[] colours;
        private int vertices;

        Mesh(int quads) {
            this.floats = new float[Math.max(1, quads) * 20];
            this.colours = new int[Math.max(1, quads) * 4];
        }

        void vertex(Vec3 p, float u, float v, int colour) {
            this.vertex(p.x, p.y, p.z, u, v, colour);
        }

        void vertex(double x, double y, double z, float u, float v, int colour) {
            if (this.vertices == this.colours.length) {
                this.floats = Arrays.copyOf(this.floats, this.floats.length * 2);
                this.colours = Arrays.copyOf(this.colours, this.colours.length * 2);
            }
            int i = this.vertices * 5;
            this.floats[i] = (float)x;
            this.floats[i + 1] = (float)y;
            this.floats[i + 2] = (float)z;
            this.floats[i + 3] = u;
            this.floats[i + 4] = v;
            this.colours[this.vertices++] = colour;
        }

        void submit(Frame frame, RenderType type) {
            if (this.vertices > 0) {
                frame.add(type, this::emit);
            }
        }

        void emit(PoseStack.Pose pose, VertexConsumer buffer) {
            for (int n = 0; n < this.vertices; n++) {
                int i = n * 5;
                int c = this.colours[n];
                buffer.addVertex(pose, this.floats[i], this.floats[i + 1], this.floats[i + 2])
                    .setUv(this.floats[i + 3], this.floats[i + 4])
                    .setColor(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, c >>> 24);
            }
        }
    }
}
