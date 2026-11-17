package net.yezon.theabyss.client.fx;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.entity.RingOfBlackStrikeAttackProjectileEntity;
import net.yezon.theabyss.entity.RingOfFireStrikeAttackProjectileEntity;
import net.yezon.theabyss.ring.RingVisuals;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class MagicLights {
    public static final ResourceLocation EFFECT = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "magic_light");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "textures/effect/magic_lights.png");
    private static final int MAX_LIGHTS = 16;
    private static final int PIXELS_PER_LIGHT = 3;
    private static final int WIDTH = 1 + MAX_LIGHTS * PIXELS_PER_LIGHT;
    private static final double MERGE_DISTANCE_SQR = 2.25;
    private static final float REACH_SCALE = 8.0F;
    private static final float REACH_BOOST = 1.4F;
    private static final float BOLT_REACH = 6.0F;
    private static final float BOLT_STRENGTH = 0.8F;

    private static final class Light {
        final double x;
        final double y;
        final double z;
        final int rgb;
        final float reach;
        float strength;
        double score;

        Light(double x, double y, double z, int rgb, float reach, float strength) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.rgb = rgb;
            this.reach = reach;
            this.strength = strength;
        }
    }

    private static final List<Light> LIGHTS = new ArrayList<>();
    private static final List<Projectile> BOLTS = new ArrayList<>();
    private static DynamicTexture texture;
    private static Vec3 camera = Vec3.ZERO;
    private static boolean live;
    private static boolean uploaded;

    private MagicLights() {
    }

    public static boolean enabled() {
        try {
            return ConfigConfiguration.IMMERSIVE_MAGIC.get();
        } catch (IllegalStateException notLoadedYet) {
            return false;
        }
    }

    public static boolean active() {
        return live;
    }

    public static void begin(Vec3 cameraPosition) {
        camera = cameraPosition;
        LIGHTS.clear();
    }

    public static void add(Vec3 at, int rgb, float reach, float strength) {
        add(at.x, at.y, at.z, rgb, reach, strength);
    }

    public static void add(double x, double y, double z, int rgb, float reach, float strength) {
        if (strength <= 0.01F || !enabled()) {
            return;
        }
        int r = rgb >> 16 & 255;
        int g = rgb >> 8 & 255;
        int b = rgb & 255;
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        if (max < 160 && max - min < 48) {
            return;
        }
        for (Light other : LIGHTS) {
            double dx = other.x - x;
            double dy = other.y - y;
            double dz = other.z - z;
            if (dx * dx + dy * dy + dz * dz < MERGE_DISTANCE_SQR) {
                other.strength = Math.max(other.strength, strength);
                return;
            }
        }
        float k = 255.0F / Math.max(max, 1);
        LIGHTS.add(new Light(x, y, z, Math.round(r * k) << 16 | Math.round(g * k) << 8 | Math.round(b * k), reach, Math.min(strength, 1.0F)));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        BOLTS.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !enabled()) {
            return;
        }
        for (Entity e : mc.level.entitiesForRendering()) {
            if ((e instanceof RingOfBlackStrikeAttackProjectileEntity || e instanceof RingOfFireStrikeAttackProjectileEntity)
                && ((Projectile)e).getOwner() instanceof Player) {
                BOLTS.add((Projectile)e);
            }
        }
    }

    public static void finish(Minecraft mc, float partialTick) {
        if (mc.level == null || !enabled()) {
            LIGHTS.clear();
            live = false;
            return;
        }
        for (Projectile bolt : BOLTS) {
            if (!bolt.isRemoved()) {
                Vec3 at = bolt.getPosition(partialTick);
                add(at, bolt instanceof RingOfBlackStrikeAttackProjectileEntity ? RingVisuals.BLACKSTRIKE : RingVisuals.FIRE, BOLT_REACH, BOLT_STRENGTH);
            }
        }
        if (LIGHTS.isEmpty()) {
            live = false;
            if (uploaded) {
                texture.getPixels().setPixelRGBA(0, 0, 0);
                texture.upload();
                uploaded = false;
            }
            return;
        }
        for (Light l : LIGHTS) {
            double dx = l.x - camera.x;
            double dy = l.y - camera.y;
            double dz = l.z - camera.z;
            l.score = l.strength * l.reach / Math.max(1.0, Math.sqrt(dx * dx + dy * dy + dz * dz));
        }
        if (LIGHTS.size() > MAX_LIGHTS) {
            LIGHTS.sort(Comparator.comparingDouble((Light l) -> -l.score));
        }
        prepareTexture(mc);
        int count = Math.min(LIGHTS.size(), MAX_LIGHTS);
        texture.getPixels().setPixelRGBA(0, 0, pixel(count, 0, 0, 255));
        for (int i = 0; i < count; i++) {
            Light l = LIGHTS.get(i);
            int p = 1 + i * PIXELS_PER_LIGHT;
            int x = packed(l.x - camera.x);
            int y = packed(l.y - camera.y);
            int z = packed(l.z - camera.z);
            texture.getPixels().setPixelRGBA(p, 0, pixel(x >> 8, x & 255, y >> 8, y & 255));
            texture.getPixels().setPixelRGBA(p + 1, 0, pixel(z >> 8, z & 255, Mth.clamp(Math.round(l.reach * REACH_BOOST * REACH_SCALE), 1, 255), Math.round(l.strength * 255.0F)));
            texture.getPixels().setPixelRGBA(p + 2, 0, pixel(l.rgb >> 16 & 255, l.rgb >> 8 & 255, l.rgb & 255, 255));
        }
        texture.upload();
        uploaded = true;
        live = true;
    }

    private static int packed(double relative) {
        return Mth.clamp((int)Math.round((relative + 128.0) / 256.0 * 65535.0), 0, 65535);
    }

    private static int pixel(int r, int g, int b, int a) {
        return a << 24 | b << 16 | g << 8 | r;
    }

    public static void prepareTexture(Minecraft mc) {
        if (texture == null) {
            texture = new DynamicTexture(WIDTH, 1, true);
            mc.getTextureManager().register(TEXTURE, texture);
            texture.upload();
        }
    }
}
