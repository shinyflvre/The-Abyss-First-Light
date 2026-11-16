package net.yezon.theabyss.client.fx.spell;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ViewportEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.fx.FxClock;
import net.yezon.theabyss.network.SpellFxMessage;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class DashFx extends Effect {
    private static final float KICK = 0.22F;
    private static long kickStart;
    private static float kickTicks;

    private final int player;
    private final int colour;
    private final int ticks;

    DashFx(SpellFxMessage m) {
        this.player = m.ids().get(0);
        this.colour = m.data().get(0).intValue();
        this.ticks = m.ticks();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getId() == this.player) {
            kickStart = FxClock.nanos();
            kickTicks = this.ticks + 8.0F;
        }
    }

    @Override
    public float life() {
        return this.ticks + 6.0F;
    }

    @Override
    public void draw(Frame f) {
        Entity e = f.level.getEntity(this.player);
        if (e == null) {
            return;
        }
        float age = this.age();
        float fade = this.fade(0.0F, 6.0F);
        Vec3 at = e.getPosition(f.pt);
        Vec3 motion = at.subtract(e.getPosition(Math.max(0.0F, f.pt - 1.0F)));
        Vec3 back = motion.lengthSqr() < 1.0E-4 ? e.getLookAngle().multiply(-1.0, 0.0, -1.0) : motion.normalize().scale(-1.0);
        Vec3 side = new Vec3(-back.z, 0.0, back.x);
        RandomSource random = RandomSource.create((long)(age * 3.0F) + this.player);
        for (int k = 0; k < 6; k++) {
            Vec3 start = at.add(side.scale((random.nextDouble() - 0.5) * 1.6)).add(0.0, 0.2 + random.nextDouble() * 1.6, 0.0);
            f.line(start, start.add(back.scale(2.0 + random.nextDouble() * 2.5)), 0.06F, 0.7F * fade, this.colour);
        }
    }

    @Override
    public void tick(ClientLevel level) {
        Entity e = level.getEntity(this.player);
        if (e == null || this.age() > this.ticks) {
            return;
        }
        RandomSource random = level.getRandom();
        for (int k = 0; k < 3; k++) {
            emit(level, sparkle(this.colour, 0.12F, 12), e.position().add(random.nextGaussian() * 0.3, random.nextDouble() * 1.8, random.nextGaussian() * 0.3),
                Vec3.ZERO);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onFov(ViewportEvent.ComputeFov event) {
        if (kickTicks <= 0.0F) {
            return;
        }
        float t = (FxClock.nanos() - kickStart) / 5.0E7F / kickTicks;
        if (t >= 1.0F) {
            kickTicks = 0.0F;
            return;
        }
        float kick = Mth.sin(Math.min(t * 4.0F, 1.0F) * Mth.HALF_PI) * (1.0F - t);
        event.setFOV(event.getFOV() * (1.0F + KICK * kick));
    }
}
