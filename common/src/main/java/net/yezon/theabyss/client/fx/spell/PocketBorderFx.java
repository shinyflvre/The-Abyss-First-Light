package net.yezon.theabyss.client.fx.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.event.tick.PlayerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.renderer.LightTrail;
import net.yezon.theabyss.pocket.PocketCells;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class PocketBorderFx extends Effect {
    private static final RenderType FIELD = LightTrail.type(Identifier.fromNamespaceAndPath("theabyss", "textures/entity/pocket_field.png"));
    private static final int COLOUR = 0x3AB4FF;
    private static final double SHOW = 4.5;
    private static final double PATCH = 5.0;
    private static final int FULL_BRIGHT = 15728880;

    @Override
    public float life() {
        return Float.MAX_VALUE;
    }

    @Override
    public boolean done() {
        Minecraft mc = Minecraft.getInstance();
        return super.done() || mc.level == null || mc.level.dimension() != PocketCells.DIMENSION;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.dimension() != PocketCells.DIMENSION) {
            return;
        }
        for (Effect effect : SpellFx.effects()) {
            if (effect instanceof PocketBorderFx) {
                return;
            }
        }
        SpellFx.add(new PocketBorderFx());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof LocalPlayer player) || player.level().dimension() != PocketCells.DIMENSION || player.isSpectator()) {
            return;
        }
        int cell = PocketCells.at(player.getX(), player.getZ());
        if (cell < 0) {
            return;
        }
        double half = player.getBbWidth() / 2.0;
        double minX = PocketCells.boxX(cell) + half;
        double maxX = PocketCells.boxX(cell) + PocketCells.BOX - half;
        double minZ = PocketCells.boxZ(cell) + half;
        double maxZ = PocketCells.boxZ(cell) + PocketCells.BOX - half;
        double x = Mth.clamp(player.getX(), minX, maxX);
        double z = Mth.clamp(player.getZ(), minZ, maxZ);
        boolean hitX = x != player.getX();
        boolean hitZ = z != player.getZ();
        double maxY = PocketCells.CEILING - player.getBbHeight();
        double y = Math.min(player.getY(), maxY);
        boolean hitY = y != player.getY();
        if (hitX || hitZ || hitY) {
            Vec3 v = player.getDeltaMovement();
            player.setPos(x, y, z);
            player.setDeltaMovement(hitX ? 0.0 : v.x, hitY ? Math.min(0.0, v.y) : v.y, hitZ ? 0.0 : v.z);
        }
    }

    @Override
    public void draw(Frame frame) {
        LocalPlayer player = frame.mc.player;
        if (player == null) {
            return;
        }
        Vec3 eye = player.getEyePosition(frame.pt);
        int cell = PocketCells.at(eye.x, eye.z);
        if (cell < 0) {
            return;
        }
        double x0 = PocketCells.boxX(cell);
        double z0 = PocketCells.boxZ(cell);
        double x1 = x0 + PocketCells.BOX;
        double z1 = z0 + PocketCells.BOX;
        float pulse = 0.85F + 0.15F * Mth.sin(this.age() * 0.15F);
        this.wall(frame, eye, true, x0, z0, z1, pulse);
        this.wall(frame, eye, true, x1, z0, z1, pulse);
        this.wall(frame, eye, false, z0, x0, x1, pulse);
        this.wall(frame, eye, false, z1, x0, x1, pulse);
        this.ceiling(frame, eye, x0, z0, x1, z1, pulse);
    }

    private void ceiling(Frame frame, Vec3 eye, double x0, double z0, double x1, double z1, float pulse) {
        double at = PocketCells.CEILING;
        double distance = at - eye.y;
        if (distance >= SHOW || distance < 0.0) {
            return;
        }
        float strength = (float)Math.pow(1.0 - distance / SHOW, 1.5) * pulse;
        int i0 = Mth.floor(Math.max(x0, eye.x - PATCH) * 2.0);
        int i1 = Mth.ceil(Math.min(x1, eye.x + PATCH) * 2.0);
        int j0 = Mth.floor(Math.max(z0, eye.z - PATCH) * 2.0);
        int j1 = Mth.ceil(Math.min(z1, eye.z + PATCH) * 2.0);
        frame.mesh(FIELD, (pose, buffer, camera) -> {
            for (int i = i0; i < i1; i++) {
                for (int j = j0; j < j1; j++) {
                    double x = i * 0.5;
                    double z = j * 0.5;
                    float la = light(x, z, eye.x, eye.z, strength);
                    float lb = light(x + 0.5, z, eye.x, eye.z, strength);
                    float lc = light(x + 0.5, z + 0.5, eye.x, eye.z, strength);
                    float ld = light(x, z + 0.5, eye.x, eye.z, strength);
                    if (la + lb + lc + ld <= 0.0F) {
                        continue;
                    }
                    float u0 = (i & 1) * 0.5F;
                    float u1 = u0 + 0.5F;
                    float v0 = (j & 1) * 0.5F;
                    float v1 = v0 + 0.5F;
                    vertex(buffer, pose, new Vec3(x, at, z).subtract(camera), u0, v0, la);
                    vertex(buffer, pose, new Vec3(x + 0.5, at, z).subtract(camera), u1, v0, lb);
                    vertex(buffer, pose, new Vec3(x + 0.5, at, z + 0.5).subtract(camera), u1, v1, lc);
                    vertex(buffer, pose, new Vec3(x, at, z + 0.5).subtract(camera), u0, v1, ld);
                }
            }
        });
    }

    private void wall(Frame frame, Vec3 eye, boolean alongZ, double at, double from, double to, float pulse) {
        double distance = Math.abs((alongZ ? eye.x : eye.z) - at);
        if (distance >= SHOW) {
            return;
        }
        float strength = (float)Math.pow(1.0 - distance / SHOW, 1.5) * pulse;
        double centre = alongZ ? eye.z : eye.x;
        int a0 = Mth.floor(Math.max(from, centre - PATCH) * 2.0);
        int a1 = Mth.ceil(Math.min(to, centre + PATCH) * 2.0);
        int y0 = Mth.floor((eye.y - PATCH) * 2.0);
        int y1 = Mth.ceil((eye.y + PATCH) * 2.0);
        if (a1 <= a0) {
            return;
        }
        double side = (alongZ ? eye.x : eye.z) - at;
        boolean flip = alongZ ? side > 0.0 : side < 0.0;
        frame.mesh(FIELD, (pose, buffer, camera) -> {
            for (int i = a0; i < a1; i++) {
                for (int j = y0; j < y1; j++) {
                    double a = i * 0.5;
                    double y = j * 0.5;
                    float la = light(a, y, centre, eye.y, strength);
                    float lb = light(a + 0.5, y, centre, eye.y, strength);
                    float lc = light(a + 0.5, y + 0.5, centre, eye.y, strength);
                    float ld = light(a, y + 0.5, centre, eye.y, strength);
                    if (la + lb + lc + ld <= 0.0F) {
                        continue;
                    }
                    Vec3 pa = point(alongZ, at, a, y, camera);
                    Vec3 pb = point(alongZ, at, a + 0.5, y, camera);
                    Vec3 pc = point(alongZ, at, a + 0.5, y + 0.5, camera);
                    Vec3 pd = point(alongZ, at, a, y + 0.5, camera);
                    float u0 = (i & 1) * 0.5F;
                    float u1 = u0 + 0.5F;
                    float v1 = 1.0F - (j & 1) * 0.5F;
                    float v0 = v1 - 0.5F;
                    if (flip) {
                        vertex(buffer, pose, pa, u0, v1, la);
                        vertex(buffer, pose, pd, u0, v0, ld);
                        vertex(buffer, pose, pc, u1, v0, lc);
                        vertex(buffer, pose, pb, u1, v1, lb);
                    } else {
                        vertex(buffer, pose, pa, u0, v1, la);
                        vertex(buffer, pose, pb, u1, v1, lb);
                        vertex(buffer, pose, pc, u1, v0, lc);
                        vertex(buffer, pose, pd, u0, v0, ld);
                    }
                }
            }
        });
    }

    private static float light(double a, double y, double centre, double eyeY, float strength) {
        double d = Math.sqrt((a - centre) * (a - centre) + (y - eyeY) * (y - eyeY)) / PATCH;
        if (d >= 1.0) {
            return 0.0F;
        }
        double fade = 1.0 - d;
        return (float)(fade * fade) * strength;
    }

    private static Vec3 point(boolean alongZ, double at, double a, double y, Vec3 camera) {
        return (alongZ ? new Vec3(at, y, a) : new Vec3(a, y, at)).subtract(camera);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, Vec3 p, float u, float v, float light) {
        float k = Mth.clamp(light, 0.0F, 1.0F);
        buffer.addVertex(pose, (float)p.x, (float)p.y, (float)p.z)
            .setColor(Math.round((COLOUR >> 16 & 0xFF) * k), Math.round((COLOUR >> 8 & 0xFF) * k), Math.round((COLOUR & 0xFF) * k), 255)
            .setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
