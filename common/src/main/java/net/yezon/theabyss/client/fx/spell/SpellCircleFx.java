package net.yezon.theabyss.client.fx.spell;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.client.fx.MagicLights;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.SpellCircle;
import net.yezon.theabyss.ring.secondary.HealingPulse;
import net.yezon.theabyss.ring.secondary.Meteor;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class SpellCircleFx extends Effect {
    private static final float BRIGHT_TINT = 4.754717F;

    private static final int T_CRATER = 0;
    private static final int T_CRACK = 1;
    private static final int T_TECH2 = 2;
    private static final int T_DISC = 3;
    private static final int T_RING = 4;
    private static final int T_ORNATE = 5;
    private static final int T_RUNES = 6;
    private static final int T_TECH = 7;
    private static final int T_SLASH = 8;
    private static final int T_WALL = 9;
    private static final int T_SPARK = 10;
    private static final int T_FLARE = 11;
    private static final int T_SNOW = 12;
    private static final int T_ELECTRO = 13;
    private static final RenderType[] TYPES = {
        FreezeBurstFx.CRATER,
        type(FreezeBurstFx.GLOW_TINT, "crack"),
        type(BRIGHT_TINT, "tech2"),
        type(FreezeBurstFx.FLASH_TINT, "disc"),
        type(FreezeBurstFx.FLASH_TINT, "ring"),
        type(FreezeBurstFx.FLASH_TINT, "ornate"),
        type(FreezeBurstFx.FLASH_TINT, "runes"),
        type(FreezeBurstFx.FLASH_TINT, "tech"),
        type(BRIGHT_TINT, "slash"),
        type(FreezeBurstFx.FLASH_TINT, "wall"),
        FreezeBurstFx.SPARK,
        type(FreezeBurstFx.SPARK_TINT, "flare"),
        type(BRIGHT_TINT, "snow"),
        type(FreezeBurstFx.SPARK_TINT, "electro"),
    };

    private static final double DECAL_LIFT = 0.02;
    private static final float LOOP_LIFE = 60.0F;
    private static final float MOTE_CYCLE = 30.0F;
    private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);

    private final int follow;
    private final int lightColour;
    private final float lightReach;
    private final float lightStrength;
    private final float span;
    private final float delay;
    private final boolean air;
    private final double reach;
    private final RandomSource random = RandomSource.create();
    private final List<Layer> layers = new ArrayList<>();
    private final List<Mote> motes = new ArrayList<>();
    private Vec3 origin;
    private List<Cell> cells;
    private double groundY;
    private int cellX;
    private int cellY;
    private int cellZ;

    public SpellCircleFx(int style, Vec3 at, int follow, int rgb, float radius, float life, float delay) {
        this.origin = at;
        this.follow = follow;
        this.span = life;
        this.delay = delay;
        this.air = style == SpellCircle.HOVER;
        this.lightColour = rgb;
        this.lightReach = Mth.clamp(radius * 2.5F + 2.0F, 4.0F, 14.0F);
        this.lightStrength = switch (style) {
            case SpellCircle.HOVER, SpellCircle.CLOSING -> 0.0F;
            case SpellCircle.SHOCK, SpellCircle.CRATER, SpellCircle.CRACK, SpellCircle.FROST, SpellCircle.SLASH -> 1.0F;
            case SpellCircle.RIPPLE -> 0.7F;
            default -> life > LOOP_LIFE ? 0.5F : 0.8F;
        };
        this.recipe(style, rgb, radius);
        double max = 0.0;
        for (Layer l : this.layers) {
            max = Math.max(max, Math.max(l.from, l.to));
        }
        this.reach = max;
    }

    public SpellCircleFx(SpellFxMessage m) {
        this(m.ticks(), SpellFx.vec(m, 0), m.ids().isEmpty() ? -1 : m.ids().get(0), m.data().get(3).intValue(), m.data().get(4).floatValue(), m.data().get(5).floatValue(), 0.0F);
    }

    private static RenderType type(float tint, String texture) {
        return SpellRenderTypes.unlit("theabyss_spell_circle_" + texture,
            ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "textures/fx/circle/" + texture + ".png"), tint);
    }

    public static List<Effect> forSpell(SpellFxMessage m) {
        List<Effect> out = new ArrayList<>(3);
        try {
            switch (m.kind()) {
                case SpellFxMessage.METEOR -> {
                    Vec3 spot = SpellFx.vec(m, 3);
                    out.add(new SpellCircleFx(SpellCircle.RUNES, spot, -1, RingVisuals.FIRE, 3.0F, m.ticks() + 3.0F, 0.0F));
                    out.add(new SpellCircleFx(SpellCircle.CLOSING, spot, -1, RingVisuals.EMBER, (float)Meteor.RADIUS, m.ticks() + 3.0F, 0.0F));
                    out.add(new SpellCircleFx(SpellCircle.CRATER, spot, -1, RingVisuals.FIRE, 3.5F, 34.0F, m.ticks()));
                }
                case SpellFxMessage.VOID -> {
                    Vec3 at = SpellFx.vec(m, 0);
                    out.add(new SpellCircleFx(SpellCircle.ORNATE, at, -1, RingVisuals.BLACKSTRIKE, 2.8F, m.ticks(), 0.0F));
                    out.add(new SpellCircleFx(SpellCircle.SHOCK, at, -1, RingVisuals.BLACKSTRIKE, 3.0F, 16.0F, m.ticks()));
                }
                case SpellFxMessage.SWAP -> {
                    out.add(new SpellCircleFx(SpellCircle.RIPPLE, SpellFx.vec(m, 0), -1, RingVisuals.TELEPORT, 1.6F, 18.0F, 0.0F));
                    out.add(new SpellCircleFx(SpellCircle.RIPPLE, SpellFx.vec(m, 3), -1, RingVisuals.TELEPORT_TIP, 1.6F, 18.0F, 0.0F));
                }
                case SpellFxMessage.REWIND -> {
                    int points = (m.data().size() - 2) / 3;
                    if (points > 0) {
                        out.add(new SpellCircleFx(SpellCircle.TECH, SpellFx.vec(m, 2), -1, 0xFFD36A, 1.6F, 18.0F, 0.0F));
                        out.add(new SpellCircleFx(SpellCircle.TECH, SpellFx.vec(m, 2 + (points - 1) * 3), -1, RingVisuals.TELEPORT_TIP, 1.6F, 30.0F, 0.0F));
                    }
                }
                case SpellFxMessage.TIDAL -> out.add(new SpellCircleFx(SpellCircle.RIPPLE, SpellFx.vec(m, 0), -1, RingVisuals.OCEAN_DEEP, 2.4F, 24.0F, 0.0F));
                case SpellFxMessage.HEAL -> {
                    for (int i = 0; i < m.ids().size(); i++) {
                        int id = m.ids().get(i);
                        if (i == 0) {
                            out.add(new SpellCircleFx(SpellCircle.DISC, Vec3.ZERO, id, RingVisuals.REG_GOLD, 2.0F, 30.0F, 0.0F));
                        } else {
                            out.add(new SpellCircleFx(SpellCircle.DISC, Vec3.ZERO, id, RingVisuals.REG_GREEN, 1.4F, 26.0F, HealingPulse.TRAVEL));
                        }
                    }
                }
                case SpellFxMessage.WALL -> out.add(new SpellCircleFx(SpellCircle.FROST, SpellFx.vec(m, 0), -1, RingVisuals.ICE, 2.6F, Math.min(m.ticks(), 40), 0.0F));
                case SpellFxMessage.HEX -> out.add(new SpellCircleFx(SpellCircle.ORNATE, Vec3.ZERO, m.ids().get(0), RingVisuals.CURSE, 1.8F, m.ticks(), 0.0F));
                case SpellFxMessage.HEX_END -> out.add(new SpellCircleFx(SpellCircle.SHOCK, SpellFx.vec(m, 0), -1, RingVisuals.CURSE, 2.2F, 14.0F, 0.0F));
                case SpellFxMessage.OVERCHARGE -> {
                    if (m.data().get(0).intValue() > 0) {
                        out.add(new SpellCircleFx(SpellCircle.ARCS, Vec3.ZERO, m.ids().get(0), RingVisuals.THUNDER, 1.5F, 24.0F, 0.0F));
                    }
                }
                case SpellFxMessage.DASH -> out.add(new SpellCircleFx(SpellCircle.SLASH, Vec3.ZERO, m.ids().get(0), m.data().get(0).intValue(), 1.8F, 12.0F, 0.0F));
                case SpellFxMessage.ICE_BURST -> out.add(new SpellCircleFx(SpellCircle.SHOCK, SpellFx.vec(m, 0), -1, RingVisuals.ICE, 2.0F, 12.0F, 0.0F));
                case SpellFxMessage.RECALL -> {
                    out.add(new SpellCircleFx(SpellCircle.RIPPLE, SpellFx.vec(m, 0), -1, RingVisuals.ABYSS, 2.0F, 22.0F, 0.0F));
                    out.add(new SpellCircleFx(SpellCircle.RIPPLE, SpellFx.vec(m, 3), -1, RingVisuals.ABYSS, 2.0F, 22.0F, 0.0F));
                }
                case SpellFxMessage.SOUL -> {
                    out.add(new SpellCircleFx(SpellCircle.RIPPLE, SpellFx.vec(m, 0), -1, 0xE8E0FF, 1.4F, 18.0F, 0.0F));
                    out.add(new SpellCircleFx(SpellCircle.RIPPLE, SpellFx.vec(m, 3), -1, 0xE8E0FF, 1.4F, 18.0F, 0.0F));
                }
                case SpellFxMessage.ECHO_SUMMON -> out.add(new SpellCircleFx(SpellCircle.ORNATE, SpellFx.vec(m, 3), -1, RingVisuals.LILAC, 1.8F, 34.0F, 0.0F));
                case SpellFxMessage.ECHO_DISSOLVE -> out.add(new SpellCircleFx(SpellCircle.RIPPLE, SpellFx.vec(m, 0), -1, RingVisuals.LILAC, 1.4F, 18.0F, 0.0F));
                default -> {
                }
            }
        } catch (RuntimeException e) {
            TheabyssMod.LOGGER.warn("Spell circle for {} skipped", m.kind(), e);
        }
        return out;
    }

    private void recipe(int style, int rgb, float r) {
        float life = this.span;
        float open = Math.min(life * 0.18F, 10.0F);
        float out = Math.max(life * 0.72F, life - 14.0F);
        int deep = scale(rgb, 0.45F);
        switch (style) {
            case SpellCircle.RUNES -> {
                this.flat(T_DISC, deep, r * 0.98F).open(open).alpha(0.3F).fade(open, out);
                this.flat(T_RUNES, rgb, r).open(open).spin(90.0F).pulse(0.12F).fade(2.0F, out);
                this.wall(rgb, r * 0.72F, Math.min(1.5F, r * 0.7F)).open(open).alpha(0.55F).spin(18.0F).fade(open, out);
                this.risers(T_SPARK, rgb, r * 0.72F, Math.round(r * 7.0F), 0.05F, 0.12F, 0.05, 0.14, true);
            }
            case SpellCircle.ORNATE -> {
                this.flat(T_ORNATE, rgb, r).open(open).spin(24.0F).pulse(0.2F).fade(2.0F, out);
                float charge = Math.min(life * 0.5F, 18.0F);
                Layer first = this.flat(T_RING, rgb, r * 1.2F).grow(r * 0.4F, charge).lasting(charge).alpha(0.8F).fade(charge * 0.5F, charge * 0.8F);
                Layer second = this.flat(T_RING, deep, r * 1.2F).grow(r * 0.4F, charge).lasting(charge).after(charge * 0.6F).alpha(0.8F).fade(charge * 0.5F, charge * 0.8F);
                if (life > LOOP_LIFE) {
                    first.repeat(MOTE_CYCLE);
                    second.repeat(MOTE_CYCLE);
                }
                this.wall(deep, r * 0.8F, Math.min(1.2F, r * 0.55F)).open(open).alpha(0.5F).spin(-14.0F).fade(open, out);
                this.risers(T_SPARK, rgb, r * 0.8F, Math.round(r * 6.0F), 0.05F, 0.1F, 0.05, 0.13, true);
                this.risers(T_SPARK, scale(rgb, 0.12F), r * 0.8F, Math.round(r * 4.0F), 0.05F, 0.1F, 0.05, 0.13, true);
            }
            case SpellCircle.TECH -> {
                this.flat(T_TECH2, scale(rgb, 0.16F), r * 0.89F).open(open).alpha(0.55F).fade(open, out);
                this.flat(T_TECH, rgb, r).open(open).spin(-90.0F).fade(2.0F, out);
                this.flat(T_TECH, rgb, r * 0.78F).open(open).spin(45.0F).turn(40.0F).fade(2.0F, out);
                float pulse = Math.min(20.0F, life);
                this.wall(rgb, r * 0.55F, Math.min(1.4F, r * 0.6F)).lasting(pulse).repeat(pulse).alpha(0.5F).fade(0.0F, 0.0F);
                this.risers(T_SPARK, rgb, r * 0.55F, Math.round(r * 4.0F), 0.04F, 0.09F, 0.05, 0.12, true);
            }
            case SpellCircle.DISC -> {
                float wide = Math.min(life * 0.11F, 8.0F);
                this.flat(T_DISC, rgb, r).open(wide).alpha(0.55F).fade(wide, out);
                this.wall(rgb, r * 0.97F, Math.min(1.3F, r * 0.55F)).open(wide).alpha(0.6F).spin(16.0F).fade(wide, out);
                this.risers(T_FLARE, rgb, r, Math.round(r * 8.0F), 0.1F, 0.3F, 0.02, 0.06, false);
                this.risers(T_SPARK, rgb, r, Math.round(r * 5.0F), 0.03F, 0.06F, 0.06, 0.15, true);
            }
            case SpellCircle.RIPPLE -> {
                float wave = life * 0.6F;
                for (int i = 0; i < 3; i++) {
                    this.flat(T_RING, i == 1 ? deep : rgb, r).grow(r * 0.15F, wave).lasting(wave).after(life * 0.2F * i).fade(2.0F, wave * 0.35F);
                }
                this.flat(T_DISC, deep, r * 0.7F).open(life * 0.3F).alpha(0.2F).fade(life * 0.2F, life * 0.5F);
            }
            case SpellCircle.SHOCK -> {
                this.flat(T_DISC, rgb, r).grow(r * 0.1F, life * 0.6F).alpha(0.8F).fade(0.0F, life * 0.2F);
                this.flat(T_RING, rgb, r * 1.15F).grow(r * 0.2F, life * 0.9F).fade(0.0F, life * 0.3F);
                this.radial(rgb, Math.round(r * 9.0F));
            }
            case SpellCircle.CRATER -> {
                this.flat(T_CRATER, rgb, r * 1.1F).open(life * 0.1F).fade(life * 0.22F, life * 0.25F);
                this.flat(T_DISC, rgb, r).grow(0.0F, life * 0.3F).lasting(life * 0.35F).alpha(0.8F).fade(0.0F, life * 0.12F);
                this.radial(rgb, Math.round(r * 8.0F));
            }
            case SpellCircle.CRACK, SpellCircle.FROST -> {
                this.flat(T_CRACK, rgb, r).open(life * 0.1F).fade(1.0F, life * 0.45F);
                this.flat(T_DISC, deep, r * 1.1F).grow(0.0F, life * 0.35F).lasting(life * 0.45F).alpha(0.6F).fade(0.0F, life * 0.15F);
                this.flat(T_RING, rgb, r * 1.1F).grow(r * 0.3F, life * 0.45F).lasting(life * 0.5F).fade(0.0F, life * 0.2F);
                this.radial(rgb, Math.round(r * 6.0F));
                if (style == SpellCircle.FROST) {
                    this.risers(T_SNOW, rgb, r, Math.round(r * 6.0F), 0.12F, 0.32F, 0.02, 0.05, false);
                }
            }
            case SpellCircle.SLASH -> {
                for (int i = 0; i < 3; i++) {
                    this.flat(T_SLASH, rgb, r).open(2.0F).spin(900.0F).turn(i * 120.0F).fade(1.0F, life * 0.4F);
                }
                this.flat(T_RING, rgb, r * 1.1F).grow(r * 0.2F, life * 0.7F).lasting(life * 0.7F).alpha(0.8F).fade(life * 0.5F, life * 0.6F);
                this.flat(T_DISC, deep, r * 0.8F).open(2.0F).alpha(0.2F).fade(1.0F, life * 0.4F);
            }
            case SpellCircle.ARCS -> {
                this.flat(T_DISC, deep, r).open(open).alpha(0.25F).fade(open, out);
                this.flat(T_RING, rgb, r * 0.9F).open(open).spin(60.0F).pulse(0.3F).fade(2.0F, out);
                int count = Math.max(4, Math.round(life * 0.7F));
                for (int i = 0; i < count; i++) {
                    double a = this.random.nextDouble() * Math.PI * 2.0;
                    double d = r * (0.6 + this.random.nextDouble() * 0.4);
                    Mote mote = new Mote(T_ELECTRO, rgb, this.random.nextFloat() * Math.max(1.0F, life - 3.0F), 3.0F, 0.5F + this.random.nextFloat() * 0.5F, false);
                    mote.x = Math.cos(a) * d;
                    mote.y = 0.3 + this.random.nextDouble() * 1.0;
                    mote.z = Math.sin(a) * d;
                    mote.angle = this.random.nextFloat() * Mth.TWO_PI;
                    mote.frame = this.random.nextBoolean() ? 0 : 1;
                    this.motes.add(mote);
                }
            }
            case SpellCircle.HOVER -> this.flat(T_RING, rgb, r).grow(r * 0.3F, life).alpha(0.8F).fade(1.0F, life * 0.4F);
            case SpellCircle.CLOSING -> {
                this.flat(T_RING, rgb, r * 1.25F).shrink(r, life).fade(4.0F, life - 3.0F);
                this.flat(T_RING, deep, r).open(open).alpha(0.7F).fade(open, life - 3.0F);
            }
            default -> this.kill();
        }
    }

    private Layer flat(int tex, int rgb, float radius) {
        Layer l = new Layer(tex, false, rgb, radius, this.span);
        this.layers.add(l);
        return l;
    }

    private Layer wall(int rgb, float radius, float height) {
        Layer l = new Layer(T_WALL, true, rgb, radius, this.span);
        l.height = height;
        this.layers.add(l);
        return l;
    }

    private void risers(int tex, int rgb, float radius, int count, float minSize, float maxSize, double minSpeed, double maxSpeed, boolean stretched) {
        float limit = Math.min(this.span, this.span > LOOP_LIFE ? MOTE_CYCLE : this.span);
        for (int i = 0; i < count; i++) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double d = radius * (stretched ? 1.0 : Math.sqrt(this.random.nextDouble()));
            float moteLife = 10.0F + this.random.nextFloat() * 12.0F;
            Mote m = new Mote(tex, rgb, this.random.nextFloat() * Math.max(1.0F, limit * 0.7F), moteLife, minSize + this.random.nextFloat() * (maxSize - minSize), stretched);
            m.x = Math.cos(a) * d;
            m.z = Math.sin(a) * d;
            m.vy = minSpeed + this.random.nextDouble() * (maxSpeed - minSpeed);
            m.angle = this.random.nextFloat() * Mth.TWO_PI;
            this.motes.add(m);
        }
    }

    private void radial(int rgb, int count) {
        for (int i = 0; i < count; i++) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double speed = 0.12 + this.random.nextDouble() * 0.2;
            Mote m = new Mote(T_SPARK, rgb, this.random.nextFloat() * 2.0F, 7.0F + this.random.nextFloat() * 7.0F, 0.04F + this.random.nextFloat() * 0.07F, true);
            m.vx = Math.cos(a) * speed;
            m.vz = Math.sin(a) * speed;
            m.vy = 0.05 + this.random.nextDouble() * 0.12;
            m.gravity = 0.012;
            m.y = 0.05;
            this.motes.add(m);
        }
    }

    @Override
    public float life() {
        return this.delay + this.span;
    }

    @Override
    public void draw(Frame f) {
        float age = this.age() - this.delay;
        if (age < 0.0F) {
            return;
        }
        if (this.follow >= 0) {
            Entity e = f.level.getEntity(this.follow);
            if (e != null && e.isAlive()) {
                this.origin = e.getPosition(f.pt);
            } else if (this.span > LOOP_LIFE || this.origin == Vec3.ZERO) {
                this.kill();
                return;
            }
        }
        this.surface(f.level);
        float lightIn = Math.min(1.0F, age / 2.0F);
        float lightOut = Math.min(1.0F, (this.span - age) / Math.min(this.span * 0.6F, 20.0F));
        MagicLights.add(this.origin.x, Math.max(this.groundY + 1.1, this.origin.y + 0.5), this.origin.z, this.lightColour, this.lightReach, this.lightStrength * lightIn * lightOut);
        FreezeBurstFx.Mesh[] meshes = new FreezeBurstFx.Mesh[TYPES.length];
        for (Layer l : this.layers) {
            this.drawLayer(meshes, l, age, f.camera);
        }
        float tail = Math.min(1.0F, Math.max(0.0F, (this.span - age) / 10.0F));
        if (tail > 0.0F) {
            for (Mote m : this.motes) {
                this.drawMote(meshes, m, age, tail, f.camera);
            }
        }
        for (int i = 0; i < meshes.length; i++) {
            if (meshes[i] != null) {
                meshes[i].submit(f, TYPES[i]);
            }
        }
    }

    private static FreezeBurstFx.Mesh mesh(FreezeBurstFx.Mesh[] meshes, int tex, int quads) {
        if (meshes[tex] == null) {
            meshes[tex] = new FreezeBurstFx.Mesh(quads);
        }
        return meshes[tex];
    }

    private void drawLayer(FreezeBurstFx.Mesh[] meshes, Layer l, float age, Vec3 camera) {
        float t = age - l.after;
        float lasting = l.lasting;
        if (t < 0.0F) {
            return;
        }
        if (l.repeat > 0.0F) {
            if (age > this.span - 1.0F) {
                return;
            }
            t %= l.repeat;
        } else if (t > lasting) {
            return;
        }
        float alpha = l.alpha;
        if (l.fadeIn > 0.0F) {
            alpha *= Math.min(1.0F, t / l.fadeIn);
        }
        if (t > l.fadeOut && lasting > l.fadeOut) {
            alpha *= Math.max(0.0F, 1.0F - (t - l.fadeOut) / (lasting - l.fadeOut));
        }
        if (l.pulse > 0.0F) {
            alpha *= 1.0F - l.pulse + l.pulse * Mth.sin(age * 0.35F + l.turn);
        }
        if (l.repeat > 0.0F) {
            float tail = Math.min(1.0F, (this.span - age) / 10.0F);
            alpha *= Math.max(0.0F, tail);
        }
        if (alpha <= 0.003F) {
            return;
        }
        float k = l.grow <= 0.0F ? 1.0F : Math.min(1.0F, t / l.grow);
        float eased = l.linear ? k : 1.0F - (1.0F - k) * (1.0F - k);
        float radius = Mth.lerp(eased, l.from, l.to);
        if (radius <= 0.01F) {
            return;
        }
        float angle = l.turn + l.spin * (age * 0.05F);
        int colour = FreezeBurstFx.pack(l.rgb, alpha);
        if (l.isWall) {
            this.drawWall(mesh(meshes, l.tex, 32), radius, l.height, angle, colour, camera);
        } else {
            this.drawFlat(mesh(meshes, l.tex, 32), radius, angle, colour, camera);
        }
    }

    private void drawFlat(FreezeBurstFx.Mesh mesh, float radius, float angle, int colour, Vec3 camera) {
        double a = Math.toRadians(angle);
        double cos = Math.cos(a);
        double sin = Math.sin(a);
        double size = radius * 2.0;
        if (this.air || this.cells.isEmpty()) {
            double y = this.origin.y + 0.05;
            this.decal(mesh, camera, this.origin.x - radius, y, this.origin.z - radius, cos, sin, size, colour);
            this.decal(mesh, camera, this.origin.x + radius, y, this.origin.z - radius, cos, sin, size, colour);
            this.decal(mesh, camera, this.origin.x + radius, y, this.origin.z + radius, cos, sin, size, colour);
            this.decal(mesh, camera, this.origin.x - radius, y, this.origin.z + radius, cos, sin, size, colour);
            return;
        }
        for (Cell c : this.cells) {
            if (c.dist > radius * 1.02) {
                continue;
            }
            double y = c.top + DECAL_LIFT;
            this.decal(mesh, camera, c.x, y, c.z, cos, sin, size, colour);
            this.decal(mesh, camera, c.x + 1.0, y, c.z, cos, sin, size, colour);
            this.decal(mesh, camera, c.x + 1.0, y, c.z + 1.0, cos, sin, size, colour);
            this.decal(mesh, camera, c.x, y, c.z + 1.0, cos, sin, size, colour);
        }
    }

    private void decal(FreezeBurstFx.Mesh mesh, Vec3 camera, double x, double y, double z, double cos, double sin, double size, int colour) {
        double dx = x - this.origin.x;
        double dz = z - this.origin.z;
        double lx = dx * cos + dz * sin;
        double lz = dz * cos - dx * sin;
        mesh.vertex(x - camera.x, y - camera.y, z - camera.z, (float)(lx / size + 0.5), (float)(0.5 - lz / size), colour);
    }

    private void drawWall(FreezeBurstFx.Mesh mesh, float radius, float height, float angle, int colour, Vec3 camera) {
        int segments = Math.max(16, Math.round(radius * 10.0F));
        double base = this.groundY + DECAL_LIFT;
        double turn = Math.toRadians(angle);
        for (int i = 0; i < segments; i++) {
            double a0 = turn + i * Math.PI * 2.0 / segments;
            double a1 = turn + (i + 1) * Math.PI * 2.0 / segments;
            double x0 = this.origin.x + Math.cos(a0) * radius - camera.x;
            double z0 = this.origin.z + Math.sin(a0) * radius - camera.z;
            double x1 = this.origin.x + Math.cos(a1) * radius - camera.x;
            double z1 = this.origin.z + Math.sin(a1) * radius - camera.z;
            double y0 = base - camera.y;
            double y1 = y0 + height;
            float u0 = i / (float)segments;
            float u1 = (i + 1) / (float)segments;
            mesh.vertex(x0, y0, z0, u0, 1.0F, colour);
            mesh.vertex(x1, y0, z1, u1, 1.0F, colour);
            mesh.vertex(x1, y1, z1, u1, 0.0F, colour);
            mesh.vertex(x0, y1, z0, u0, 0.0F, colour);
        }
    }

    private void drawMote(FreezeBurstFx.Mesh[] meshes, Mote m, float age, float tail, Vec3 camera) {
        float t = age - m.spawn;
        if (this.span > LOOP_LIFE) {
            t = (age % MOTE_CYCLE) - m.spawn;
            if (t < 0.0F) {
                t += MOTE_CYCLE;
            }
        }
        if (t < 0.0F || t > m.life) {
            return;
        }
        float k = t / m.life;
        float size = m.size * (k < 0.2F ? k / 0.2F : (1.0F - k) / 0.8F);
        if (size <= 0.005F) {
            return;
        }
        double vy = m.vy - m.gravity * t;
        Vec3 p = new Vec3(this.origin.x + m.x + m.vx * t - camera.x, this.groundY + m.y + m.vy * t - 0.5 * m.gravity * t * t - camera.y, this.origin.z + m.z + m.vz * t - camera.z);
        int colour = FreezeBurstFx.pack(m.rgb, tail);
        FreezeBurstFx.Mesh mesh = mesh(meshes, m.tex, 16);
        if (m.stretched) {
            double speed = Math.sqrt(m.vx * m.vx + vy * vy + m.vz * m.vz);
            Vec3 axis = speed < 1.0E-4 ? UP : new Vec3(m.vx / speed, vy / speed, m.vz / speed);
            Vec3 side = axis.cross(p);
            if (side.lengthSqr() < 1.0E-8) {
                return;
            }
            side = side.normalize().scale(size * 0.5);
            Vec3 tailEnd = p.subtract(axis.scale(size * 3.0 + speed * 1.2));
            mesh.vertex(p.add(side), 1.0F, 0.0F, colour);
            mesh.vertex(p.subtract(side), 0.0F, 0.0F, colour);
            mesh.vertex(tailEnd.subtract(side), 0.0F, 1.0F, colour);
            mesh.vertex(tailEnd.add(side), 1.0F, 1.0F, colour);
            return;
        }
        double distance = p.length();
        if (distance < 1.0E-3) {
            return;
        }
        Vec3 toCamera = p.scale(-1.0 / distance);
        Vec3 right = toCamera.cross(UP);
        right = right.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : right.normalize();
        Vec3 up = right.cross(toCamera);
        double cos = Math.cos(m.angle + t * 0.05);
        double sin = Math.sin(m.angle + t * 0.05);
        double half = size * 0.5;
        Vec3 u = right.scale(cos * half).add(up.scale(sin * half));
        Vec3 v = up.scale(cos * half).subtract(right.scale(sin * half));
        float v0 = m.tex == T_ELECTRO ? m.frame * 0.5F : 0.0F;
        float v1 = m.tex == T_ELECTRO ? v0 + 0.5F : 1.0F;
        mesh.vertex(p.subtract(u).add(v), 0.0F, v0, colour);
        mesh.vertex(p.add(u).add(v), 1.0F, v0, colour);
        mesh.vertex(p.add(u).subtract(v), 1.0F, v1, colour);
        mesh.vertex(p.subtract(u).subtract(v), 0.0F, v1, colour);
    }

    private void surface(ClientLevel level) {
        int x = Mth.floor(this.origin.x);
        int y = Mth.floor(this.origin.y * 4.0);
        int z = Mth.floor(this.origin.z);
        if (this.cells != null && x == this.cellX && y == this.cellY && z == this.cellZ) {
            return;
        }
        this.cellX = x;
        this.cellY = y;
        this.cellZ = z;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        double top = FreezeBurstFx.surface(level, pos, x, z, this.origin.y);
        this.groundY = Double.isNaN(top) ? this.origin.y : top;
        this.cells = new ArrayList<>();
        int range = Mth.ceil(this.reach) + 1;
        for (int cx = x - range; cx <= x + range; cx++) {
            for (int cz = z - range; cz <= z + range; cz++) {
                double nx = Mth.clamp(this.origin.x, cx, cx + 1.0) - this.origin.x;
                double nz = Mth.clamp(this.origin.z, cz, cz + 1.0) - this.origin.z;
                double dist = Math.sqrt(nx * nx + nz * nz);
                if (dist > this.reach * 1.02) {
                    continue;
                }
                double h = FreezeBurstFx.surface(level, pos, cx, cz, this.origin.y);
                if (!Double.isNaN(h)) {
                    this.cells.add(new Cell(cx, cz, h, dist));
                }
            }
        }
    }

    private static int scale(int rgb, float k) {
        return Math.round((rgb >> 16 & 255) * k) << 16 | Math.round((rgb >> 8 & 255) * k) << 8 | Math.round((rgb & 255) * k);
    }

    private record Cell(int x, int z, double top, double dist) {
    }

    private static final class Layer {
        final int tex;
        final boolean isWall;
        final int rgb;
        float from;
        float to;
        float grow;
        float after;
        float lasting;
        float repeat;
        float spin;
        float turn;
        float alpha = 1.0F;
        float fadeIn;
        float fadeOut;
        float pulse;
        float height;
        boolean linear;

        Layer(int tex, boolean isWall, int rgb, float radius, float life) {
            this.tex = tex;
            this.isWall = isWall;
            this.rgb = rgb;
            this.from = radius;
            this.to = radius;
            this.lasting = life;
            this.fadeOut = life;
        }

        Layer open(float ticks) {
            this.from = 0.0F;
            this.grow = ticks;
            return this;
        }

        Layer grow(float start, float ticks) {
            this.from = start;
            this.grow = ticks;
            return this;
        }

        Layer shrink(float end, float ticks) {
            this.to = end;
            this.grow = ticks;
            this.linear = true;
            return this;
        }

        Layer lasting(float ticks) {
            this.lasting = ticks;
            this.fadeOut = Math.min(this.fadeOut, ticks);
            return this;
        }

        Layer after(float ticks) {
            this.after = ticks;
            return this;
        }

        Layer repeat(float ticks) {
            this.repeat = ticks;
            return this;
        }

        Layer spin(float degreesPerSecond) {
            this.spin = degreesPerSecond;
            return this;
        }

        Layer turn(float degrees) {
            this.turn = degrees;
            return this;
        }

        Layer alpha(float value) {
            this.alpha = value;
            return this;
        }

        Layer fade(float in, float outAt) {
            this.fadeIn = in;
            this.fadeOut = outAt;
            return this;
        }

        Layer pulse(float amount) {
            this.pulse = amount;
            return this;
        }
    }

    private static final class Mote {
        final int tex;
        final int rgb;
        final float spawn;
        final float life;
        final float size;
        final boolean stretched;
        double x;
        double y;
        double z;
        double vx;
        double vy;
        double vz;
        double gravity;
        float angle;
        int frame;

        Mote(int tex, int rgb, float spawn, float life, float size, boolean stretched) {
            this.tex = tex;
            this.rgb = rgb;
            this.spawn = spawn;
            this.life = life;
            this.size = size;
            this.stretched = stretched;
        }
    }
}
