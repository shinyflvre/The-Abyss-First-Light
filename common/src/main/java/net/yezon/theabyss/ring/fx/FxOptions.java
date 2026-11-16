package net.yezon.theabyss.ring.fx;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class FxOptions implements ParticleOptions {
    public static final int ANIMATE = 1;
    public static final int FLICKER = 2;
    public static final int SHRINK = 4;
    public static final int GROW = 8;
    public static final int HOT = 16;
    public static final int BOUNCE = 32;
    public static final int FLAT = 64;
    public static final int SMOKE = 128;

    private final ParticleType<FxOptions> type;
    private final int color;
    private final float size;
    private final int lifetime;
    private final float gravity;
    private final float drag;
    private final int flags;

    public FxOptions(ParticleType<FxOptions> type, int color, float size, int lifetime, float gravity, float drag, int flags) {
        this.type = type;
        this.color = color;
        this.size = size;
        this.lifetime = lifetime;
        this.gravity = gravity;
        this.drag = drag;
        this.flags = flags;
    }

    public static MapCodec<FxOptions> codec(ParticleType<FxOptions> type) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.INT.fieldOf("color").forGetter(FxOptions::color),
            Codec.FLOAT.fieldOf("size").forGetter(FxOptions::size),
            Codec.INT.fieldOf("lifetime").forGetter(FxOptions::lifetime),
            Codec.FLOAT.optionalFieldOf("gravity", 0.0F).forGetter(FxOptions::gravity),
            Codec.FLOAT.optionalFieldOf("drag", 0.9F).forGetter(FxOptions::drag),
            Codec.INT.optionalFieldOf("flags", 0).forGetter(FxOptions::flags)
        ).apply(i, (c, s, l, g, d, f) -> new FxOptions(type, c, s, l, g, d, f)));
    }

    public static StreamCodec<ByteBuf, FxOptions> streamCodec(ParticleType<FxOptions> type) {
        return StreamCodec.composite(
            ByteBufCodecs.INT, FxOptions::color,
            ByteBufCodecs.FLOAT, FxOptions::size,
            ByteBufCodecs.VAR_INT, FxOptions::lifetime,
            ByteBufCodecs.FLOAT, FxOptions::gravity,
            ByteBufCodecs.FLOAT, FxOptions::drag,
            ByteBufCodecs.VAR_INT, FxOptions::flags,
            (c, s, l, g, d, f) -> new FxOptions(type, c, s, l, g, d, f)
        );
    }

    public int color() {
        return this.color;
    }

    public float size() {
        return this.size;
    }

    public int lifetime() {
        return this.lifetime;
    }

    public float gravity() {
        return this.gravity;
    }

    public float drag() {
        return this.drag;
    }

    public int flags() {
        return this.flags;
    }

    public FxOptions gravity(float value) {
        return new FxOptions(this.type, this.color, this.size, this.lifetime, value, this.drag, this.flags);
    }

    public FxOptions drag(float value) {
        return new FxOptions(this.type, this.color, this.size, this.lifetime, this.gravity, value, this.flags);
    }

    public FxOptions flags(int value) {
        return new FxOptions(this.type, this.color, this.size, this.lifetime, this.gravity, this.drag, value);
    }

    public FxOptions size(float value) {
        return new FxOptions(this.type, this.color, value, this.lifetime, this.gravity, this.drag, this.flags);
    }

    public FxOptions life(int value) {
        return new FxOptions(this.type, this.color, this.size, value, this.gravity, this.drag, this.flags);
    }

    @Override
    public ParticleType<FxOptions> getType() {
        return this.type;
    }

    public static final class Type extends ParticleType<FxOptions> {
        private final MapCodec<FxOptions> codec = FxOptions.codec(this);
        private final StreamCodec<ByteBuf, FxOptions> streamCodec = FxOptions.streamCodec(this);

        public Type() {
            super(true);
        }

        @Override
        public MapCodec<FxOptions> codec() {
            return this.codec;
        }

        @Override
        public StreamCodec<ByteBuf, FxOptions> streamCodec() {
            return this.streamCodec;
        }

        public FxOptions of(int color, float size, int lifetime, int flags) {
            return new FxOptions(this, color, size, lifetime, 0.0F, 0.9F, flags);
        }
    }
}
