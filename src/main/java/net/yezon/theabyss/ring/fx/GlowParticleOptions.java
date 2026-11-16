package net.yezon.theabyss.ring.fx;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.yezon.theabyss.init.TheabyssModParticleTypes;

public record GlowParticleOptions(int color, float size, int lifetime, float gravity, float drag, int flags) implements ParticleOptions {
    public static final int SHRINK = 1;
    public static final int FLICKER = 2;
    public static final int FADE_IN = 4;
    public static final int GROW = 8;

    public static final MapCodec<GlowParticleOptions> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.INT.fieldOf("color").forGetter(GlowParticleOptions::color),
        Codec.FLOAT.fieldOf("size").forGetter(GlowParticleOptions::size),
        Codec.INT.fieldOf("lifetime").forGetter(GlowParticleOptions::lifetime),
        Codec.FLOAT.optionalFieldOf("gravity", 0.0F).forGetter(GlowParticleOptions::gravity),
        Codec.FLOAT.optionalFieldOf("drag", 0.92F).forGetter(GlowParticleOptions::drag),
        Codec.INT.optionalFieldOf("flags", SHRINK).forGetter(GlowParticleOptions::flags)
    ).apply(i, GlowParticleOptions::new));

    public static final StreamCodec<ByteBuf, GlowParticleOptions> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, GlowParticleOptions::color,
        ByteBufCodecs.FLOAT, GlowParticleOptions::size,
        ByteBufCodecs.VAR_INT, GlowParticleOptions::lifetime,
        ByteBufCodecs.FLOAT, GlowParticleOptions::gravity,
        ByteBufCodecs.FLOAT, GlowParticleOptions::drag,
        ByteBufCodecs.VAR_INT, GlowParticleOptions::flags,
        GlowParticleOptions::new
    );

    public static GlowParticleOptions of(int color, float size, int lifetime) {
        return new GlowParticleOptions(color, size, lifetime, 0.0F, 0.92F, SHRINK);
    }

    public GlowParticleOptions gravity(float value) {
        return new GlowParticleOptions(this.color, this.size, this.lifetime, value, this.drag, this.flags);
    }

    public GlowParticleOptions drag(float value) {
        return new GlowParticleOptions(this.color, this.size, this.lifetime, this.gravity, value, this.flags);
    }

    public GlowParticleOptions flags(int value) {
        return new GlowParticleOptions(this.color, this.size, this.lifetime, this.gravity, this.drag, value);
    }

    @Override
    public ParticleType<GlowParticleOptions> getType() {
        return TheabyssModParticleTypes.GLOW.get();
    }

    public static final class Type extends ParticleType<GlowParticleOptions> {
        public Type() {
            super(true);
        }

        @Override
        public MapCodec<GlowParticleOptions> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<ByteBuf, GlowParticleOptions> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
