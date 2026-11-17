package net.yezon.theabyss.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public final class TintedBuffers implements MultiBufferSource {
    private final MultiBufferSource delegate;
    private final RenderType target;
    private final int tint;

    public TintedBuffers(MultiBufferSource delegate, RenderType target, int tint) {
        this.delegate = delegate;
        this.target = target;
        this.tint = tint;
    }

    public static MultiBufferSource of(MultiBufferSource delegate, RenderType target, int tint) {
        return tint == -1 ? delegate : new TintedBuffers(delegate, target, tint);
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        VertexConsumer buffer = this.delegate.getBuffer(type);
        return type == this.target ? new Tinted(buffer, this.tint) : buffer;
    }

    public static int multiply(int a, int b) {
        if (b == -1) {
            return a;
        }
        if (a == -1) {
            return b;
        }
        return channel(a >>> 24, b >>> 24) << 24 | channel(a >> 16 & 0xFF, b >> 16 & 0xFF) << 16 | channel(a >> 8 & 0xFF, b >> 8 & 0xFF) << 8
            | channel(a & 0xFF, b & 0xFF);
    }

    private static int channel(int a, int b) {
        return a * b / 255;
    }

    private record Tinted(VertexConsumer inner, int tint) implements VertexConsumer {
        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.inner.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            this.inner.setColor(channel(red, this.tint >> 16 & 0xFF), channel(green, this.tint >> 8 & 0xFF), channel(blue, this.tint & 0xFF),
                channel(alpha, this.tint >>> 24));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.inner.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            this.inner.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            this.inner.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            this.inner.setNormal(x, y, z);
            return this;
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float nx, float ny, float nz) {
            this.inner.addVertex(x, y, z, multiply(color, this.tint), u, v, overlay, light, nx, ny, nz);
        }
    }
}
