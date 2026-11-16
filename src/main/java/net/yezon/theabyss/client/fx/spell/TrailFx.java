package net.yezon.theabyss.client.fx.spell;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;

final class TrailFx extends Effect {
    private static final int LENGTH = 7;
    private static final float FADE = 6.0F;

    private final int glow;
    private final int core;
    private final int ticks;
    private final List<Track> tracks = new ArrayList<>();

    private static final class Track {
        final int id;
        final Deque<Vec3> points = new ArrayDeque<>();
        float goneAt = -1.0F;

        Track(int id) {
            this.id = id;
        }
    }

    TrailFx(SpellFxMessage m) {
        this.glow = m.data().get(0).intValue();
        this.core = m.data().size() > 1 ? m.data().get(1).intValue() : 0xFFFFFF;
        this.ticks = m.ticks();
        for (int id : m.ids()) {
            this.tracks.add(new Track(id));
        }
    }

    @Override
    public float life() {
        return this.ticks + FADE;
    }

    @Override
    public void tick(ClientLevel level) {
        float age = this.age();
        for (Track t : this.tracks) {
            Entity e = level.getEntity(t.id);
            if (e == null || e.isRemoved()) {
                if (t.goneAt < 0.0F) {
                    t.goneAt = age;
                }
                continue;
            }
            t.points.addFirst(e.position());
            while (t.points.size() > LENGTH) {
                t.points.removeLast();
            }
        }
    }

    @Override
    public void draw(Frame f) {
        float age = this.age();
        for (Track t : this.tracks) {
            float fade = t.goneAt < 0.0F ? 1.0F : 1.0F - (age - t.goneAt) / FADE;
            if (fade <= 0.0F || t.points.size() < 2) {
                continue;
            }
            List<Vec3> list = new ArrayList<>();
            Vec3 head = t.goneAt < 0.0F ? f.pos(t.id) : null;
            if (head != null) {
                list.add(head);
            }
            list.addAll(t.points);
            int n = list.size();
            Vec3[] p = list.toArray(new Vec3[0]);
            float[] w = new float[n];
            float[] wc = new float[n];
            float[] l = new float[n];
            for (int i = 0; i < n; i++) {
                float s = i / (float)(n - 1);
                w[i] = 0.55F * (1.0F - s) + 0.04F;
                wc[i] = 0.18F * (1.0F - s) + 0.02F;
                l[i] = (1.0F - s) * fade;
            }
            f.ribbon(p, w, l, this.glow);
            f.ribbon(p, wc, l, this.core);
            if (head != null) {
                f.flare(head, 1.3F, this.glow, 0.9F);
                f.flare(head, 0.5F, this.core, 1.0F);
            }
        }
    }
}
