package net.yezon.theabyss.gravity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.yezon.theabyss.init.TheabyssModParticleTypes;

public record GravityMoteOptions(int pull) implements ParticleOptions {
    public static final MapCodec<GravityMoteOptions> CODEC = Codec.INT.fieldOf("pull").xmap(GravityMoteOptions::new, GravityMoteOptions::pull);
    public static final StreamCodec<ByteBuf, GravityMoteOptions> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(GravityMoteOptions::new, GravityMoteOptions::pull);

    @Override
    public ParticleType<GravityMoteOptions> getType() {
        return TheabyssModParticleTypes.GRAVITY_MOTE.get();
    }

    public static final class Type extends ParticleType<GravityMoteOptions> {
        public Type() {
            super(false);
        }

        @Override
        public MapCodec<GravityMoteOptions> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<ByteBuf, GravityMoteOptions> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
