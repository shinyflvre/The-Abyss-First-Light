package net.yezon.theabyss.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public final class AbyssStreamCodecs {
    public static final StreamCodec<ByteBuf, Vec3> VEC3 = new StreamCodec<>() {
        @Override
        public Vec3 decode(ByteBuf input) {
            return new Vec3(input.readDouble(), input.readDouble(), input.readDouble());
        }

        @Override
        public void encode(ByteBuf output, Vec3 value) {
            output.writeDouble(value.x);
            output.writeDouble(value.y);
            output.writeDouble(value.z);
        }
    };

    private AbyssStreamCodecs() {
    }
}
