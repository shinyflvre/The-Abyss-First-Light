package net.yezon.theabyss.compat;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class ARGB {
    private ARGB() {
    }

    public static int alpha(int color) {
        return color >>> 24;
    }

    public static int red(int color) {
        return color >> 16 & 0xFF;
    }

    public static int green(int color) {
        return color >> 8 & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    public static int color(int alpha, int red, int green, int blue) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }

    public static int color(int red, int green, int blue) {
        return color(255, red, green, blue);
    }

    public static int color(Vec3 vec) {
        return color(as8BitChannel((float)vec.x()), as8BitChannel((float)vec.y()), as8BitChannel((float)vec.z()));
    }

    public static int color(int alpha, int rgb) {
        return alpha << 24 | rgb & 0xFFFFFF;
    }

    public static int color(float alpha, int rgb) {
        return as8BitChannel(alpha) << 24 | rgb & 0xFFFFFF;
    }

    public static int white(float alpha) {
        return as8BitChannel(alpha) << 24 | 0xFFFFFF;
    }

    public static int white(int alpha) {
        return alpha << 24 | 0xFFFFFF;
    }

    public static int opaque(int color) {
        return color | 0xFF000000;
    }

    public static int colorFromFloat(float alpha, float red, float green, float blue) {
        return color(as8BitChannel(alpha), as8BitChannel(red), as8BitChannel(green), as8BitChannel(blue));
    }

    public static int srgbLerp(float alpha, int p0, int p1) {
        return color(Mth.lerpInt(alpha, alpha(p0), alpha(p1)), Mth.lerpInt(alpha, red(p0), red(p1)),
            Mth.lerpInt(alpha, green(p0), green(p1)), Mth.lerpInt(alpha, blue(p0), blue(p1)));
    }

    public static int as8BitChannel(float value) {
        return Mth.floor(value * 255.0F);
    }
}
