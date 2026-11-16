package net.yezon.theabyss.client.fx.spell;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.client.fx.FxClock;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.ring.fx.FxOptions;

public abstract class Effect {
    private final long startNanos = FxClock.nanos();
    private boolean killed;

    public float age() {
        return (FxClock.nanos() - this.startNanos) / 5.0E7F;
    }

    public abstract float life();

    public boolean done() {
        return this.killed || this.age() > this.life();
    }

    public void kill() {
        this.killed = true;
    }

    public void tick(ClientLevel level) {
    }

    public abstract void draw(Frame frame);

    public int outline(Entity entity) {
        return 0;
    }

    protected float fade(float in, float out) {
        float age = this.age();
        float a = in <= 0.0F ? 1.0F : Math.min(1.0F, age / in);
        float b = out <= 0.0F ? 1.0F : Math.min(1.0F, Math.max(0.0F, (this.life() - age) / out));
        return Math.max(0.0F, Math.min(a, b));
    }

    protected static void emit(ClientLevel level, ParticleOptions options, Vec3 at, Vec3 velocity) {
        level.addParticle(options, true, true, at.x, at.y, at.z, velocity.x, velocity.y, velocity.z);
    }

    protected static FxOptions flame(int colour, float size, int life) {
        return TheabyssModParticleTypes.FX_FLAME.get().of(colour, size, life, FxOptions.ANIMATE);
    }

    protected static FxOptions wisp(int colour, float size, int life) {
        return TheabyssModParticleTypes.FX_WISP.get().of(colour, size, life, FxOptions.ANIMATE);
    }

    protected static FxOptions sparkle(int colour, float size, int life) {
        return TheabyssModParticleTypes.FX_SPARKLE.get().of(colour, size, life, FxOptions.FLICKER);
    }

    protected static FxOptions zap(int colour, float size, int life) {
        return TheabyssModParticleTypes.FX_ZAP.get().of(colour, size, life, FxOptions.FLICKER | FxOptions.HOT);
    }

    protected static FxOptions orb(int colour, float size, int life) {
        return TheabyssModParticleTypes.FX_ORB.get().of(colour, size, life, FxOptions.SHRINK);
    }

    protected static FxOptions smoke(int colour, float size, int life) {
        return TheabyssModParticleTypes.FX_SMOKE.get().of(colour, size, life, FxOptions.SMOKE | FxOptions.GROW);
    }

    protected static FxOptions groundRing(int colour, float radius, int life) {
        return TheabyssModParticleTypes.FX_RING.get().of(colour, radius, life, FxOptions.FLAT | FxOptions.GROW);
    }

    protected static double smooth(double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return t * t * (3.0 - 2.0 * t);
    }

    protected static double easeOut(double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return 1.0 - (1.0 - t) * (1.0 - t);
    }
}
