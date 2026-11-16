package net.yezon.theabyss.client.fx.spell;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;

public final class WraithFx extends Effect {
    private static final RenderType POOL = RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "textures/entity/wraith_pool.png"));
    private static final int FRAMES = 8;
    private static final float FRAME_TICKS = 3.0F;
    private static final float GROW = 6.0F;
    private static final float FADE = 8.0F;
    private static final int SHADOW = 0x0B0910;
    private static final int SHADOW_SOFT = 0x261F31;
    private static final int MOTE = 0x9C8BD0;
    private static final RandomSource RANDOM = RandomSource.create();

    private final int entity;
    private float until;
    private float released = -1.0F;
    private int missing;

    public WraithFx(int entity, int ticks) {
        this.entity = entity;
        this.until = ticks;
    }

    public int entity() {
        return this.entity;
    }

    public void extend(int ticks) {
        this.released = -1.0F;
        this.until = this.age() + ticks;
    }

    public void release() {
        if (this.released < 0.0F) {
            this.released = Math.min(this.age(), this.until);
        }
    }

    private float end() {
        return this.released >= 0.0F ? this.released : this.until;
    }

    @Override
    public float life() {
        return this.end() + FADE;
    }

    private float strength() {
        float age = this.age();
        float in = (float)easeOut(age / GROW);
        float out = 1.0F - Mth.clamp((age - this.end()) / FADE, 0.0F, 1.0F);
        return Math.min(in, out);
    }

    @Override
    public void tick(ClientLevel level) {
        Entity e = level.getEntity(this.entity);
        if (e == null) {
            if (++this.missing > 40) {
                this.kill();
            }
            return;
        }
        this.missing = 0;
        if (this.age() > this.end()) {
            return;
        }
        Vec3 feet = e.position();
        for (int i = 0; i < 2; i++) {
            double a = RANDOM.nextDouble() * Math.PI * 2.0;
            double r = 0.55 + RANDOM.nextDouble() * 0.4;
            Vec3 at = feet.add(Math.cos(a) * r, 0.05, Math.sin(a) * r);
            emit(level, smoke(RANDOM.nextInt(3) == 0 ? SHADOW_SOFT : SHADOW, 0.22F + RANDOM.nextFloat() * 0.18F, 18 + RANDOM.nextInt(10)), at,
                new Vec3(-Math.cos(a) * 0.006, 0.02 + RANDOM.nextDouble() * 0.025, -Math.sin(a) * 0.006));
        }
        if (e.tickCount % 3 == 0) {
            double a = RANDOM.nextDouble() * Math.PI * 2.0;
            double r = RANDOM.nextDouble() * 0.7;
            emit(level, sparkle(MOTE, 0.06F + RANDOM.nextFloat() * 0.03F, 14 + RANDOM.nextInt(8)), feet.add(Math.cos(a) * r, 0.08, Math.sin(a) * r),
                new Vec3(0.0, 0.03 + RANDOM.nextDouble() * 0.02, 0.0));
        }
    }

    @Override
    public void draw(Frame f) {
        Entity e = f.level.getEntity(this.entity);
        if (e == null) {
            return;
        }
        float k = this.strength();
        if (k <= 0.0F) {
            return;
        }
        Vec3 feet = e.getPosition(f.pt);
        BlockHitResult hit = f.level.clip(new ClipContext(feet.add(0.0, 0.2, 0.0), feet.add(0.0, -3.0, 0.0), ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, e));
        double y = hit.getType() == HitResult.Type.BLOCK ? hit.getLocation().y : feet.y;
        Vec3 centre = new Vec3(feet.x, y, feet.z);
        float age = this.age();
        int frame = (int)(age / FRAME_TICKS) % FRAMES;
        float breathe = 1.0F + 0.04F * Mth.sin(age * 0.25F);
        this.pool(f, centre.add(0.0, 0.012, 0.0), 1.55F * k * breathe, -age * 0.012F, (frame + FRAMES / 2) % FRAMES, 0.35F * k);
        this.pool(f, centre.add(0.0, 0.02, 0.0), 1.0F * k * breathe, age * 0.018F, frame, k);
    }

    private void pool(Frame f, Vec3 at, float half, float spin, int frame, float alpha) {
        Vec3 rel = f.rel(at);
        float cos = Mth.cos(spin) * half;
        float sin = Mth.sin(spin) * half;
        Vec3 a = rel.add(-cos + sin, 0.0, -sin - cos);
        Vec3 b = rel.add(cos + sin, 0.0, sin - cos);
        Vec3 c = rel.add(cos - sin, 0.0, sin + cos);
        Vec3 d = rel.add(-cos - sin, 0.0, -sin + cos);
        float v0 = frame / (float)FRAMES;
        float v1 = (frame + 1) / (float)FRAMES;
        int argb = ARGB.color(Mth.clamp((int)(alpha * 255.0F), 0, 255), 255, 255, 255);
        f.mesh(POOL, (pose, buffer, camera) -> Frame.quad(buffer, pose, a, b, c, d, 0.0F, v0, 1.0F, v1, argb));
    }
}
