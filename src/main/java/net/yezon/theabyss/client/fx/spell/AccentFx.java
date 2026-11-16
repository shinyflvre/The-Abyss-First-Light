package net.yezon.theabyss.client.fx.spell;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.FxOptions;
import net.yezon.theabyss.ring.fx.RingAccent;

public final class AccentFx extends Effect {
    private static final int GOLD = 0xFFD36A;
    private static final int PEARL = 0xE8E0FF;

    private final int code;
    private final Vec3 at;
    private final Vec3 second;
    private final List<Integer> ids;
    private final float life;
    private final int param;
    private final int colour;
    private final Vec3[] path;
    private final RandomSource random = RandomSource.create();
    private final List<Vec3> history = new ArrayList<>();
    private final List<Streak> streaks = new ArrayList<>();
    private ClientLevel level;
    private Vec3 lastCamera;
    private int t;

    private AccentFx(int code, Vec3 at, Vec3 second, List<Integer> ids, float life, int param, int colour, Vec3[] path) {
        this.code = code;
        this.at = at;
        this.second = second;
        this.ids = ids;
        this.life = life;
        this.param = param;
        this.colour = colour;
        this.path = path;
    }

    public static Effect ofMessage(SpellFxMessage m) {
        int code = m.ticks();
        return new AccentFx(code, SpellFx.vec(m, 0), null, m.ids(), lifeOf(code), 0, 0, null);
    }

    public static Effect forSpell(SpellFxMessage m) {
        try {
            return switch (m.kind()) {
                case SpellFxMessage.METEOR -> new AccentFx(RingAccent.METEOR, SpellFx.vec(m, 0), SpellFx.vec(m, 3), m.ids(), m.ticks() + 80, m.ticks(), 0, null);
                case SpellFxMessage.VOID -> new AccentFx(RingAccent.VOID, SpellFx.vec(m, 0), null, m.ids(), m.ticks() + 14, m.ticks(), 0, null);
                case SpellFxMessage.TRAIL -> {
                    int colour = m.data().get(0).intValue();
                    yield colour == RingVisuals.FIRE || colour == RingVisuals.NIGHTBLADE ? new AccentFx(RingAccent.TRAIL, Vec3.ZERO, null, m.ids(), m.ticks(), 0, colour, null) : null;
                }
                case SpellFxMessage.SWAP -> new AccentFx(RingAccent.SWAP, SpellFx.vec(m, 0), SpellFx.vec(m, 3), m.ids(), m.ticks() + 6, m.ticks(), 0, null);
                case SpellFxMessage.REWIND -> new AccentFx(RingAccent.REWIND, Vec3.ZERO, null, m.ids(), m.ticks(), 0, 0, points(m, 2));
                case SpellFxMessage.TETHER -> new AccentFx(RingAccent.TETHER, Vec3.ZERO, null, m.ids(), m.ticks(), 0, 0, null);
                case SpellFxMessage.TIDAL -> new AccentFx(RingAccent.TIDAL, SpellFx.vec(m, 0), new Vec3(m.data().get(3), 0.0, m.data().get(4)).normalize(), m.ids(), m.ticks(), 0, 0, null);
                case SpellFxMessage.HEAL -> new AccentFx(RingAccent.HEAL, Vec3.ZERO, null, m.ids(), Math.max(40, m.ticks()), 0, 0, null);
                case SpellFxMessage.WALL -> new AccentFx(RingAccent.WALL, SpellFx.vec(m, 0), new Vec3(m.data().get(3), 0.0, m.data().get(4)).normalize(), m.ids(), m.ticks(), 0, 0, null);
                case SpellFxMessage.HEX -> new AccentFx(RingAccent.HEX, Vec3.ZERO, null, m.ids(), m.ticks(), 0, 0, null);
                case SpellFxMessage.HEX_END -> new AccentFx(RingAccent.HEX_END, SpellFx.vec(m, 0), null, m.ids(), 34, 0, 0, null);
                case SpellFxMessage.OVERCHARGE -> m.data().get(0).intValue() > 0 ? new AccentFx(RingAccent.OVERCHARGE, Vec3.ZERO, null, m.ids(), 30, 0, 0, null) : null;
                case SpellFxMessage.OVERCHARGE_FIRE -> new AccentFx(RingAccent.OVERCHARGE_FIRE, Vec3.ZERO, null, m.ids(), 12, 0, 0, null);
                case SpellFxMessage.DASH -> new AccentFx(RingAccent.DASH, Vec3.ZERO, null, m.ids(), m.ticks() + 10, 0, m.data().get(0).intValue(), null);
                case SpellFxMessage.FLAME_TRAIL -> new AccentFx(RingAccent.FLAME_TRAIL, Vec3.ZERO, null, m.ids(), m.ticks(), 0, 0, points(m, 0));
                case SpellFxMessage.ICE -> new AccentFx(RingAccent.ICE, SpellFx.vec(m, 0), null, m.ids(), m.ticks(), 0, 0, null);
                case SpellFxMessage.ICE_BURST -> new AccentFx(RingAccent.ICE_BURST, SpellFx.vec(m, 0), null, m.ids(), 34, 0, 0, null);
                case SpellFxMessage.RECALL_MARK -> m.data().isEmpty() ? null : new AccentFx(RingAccent.RECALL_MARK, SpellFx.vec(m, 0), null, m.ids(), 36, 0, 0, null);
                case SpellFxMessage.RECALL -> new AccentFx(RingAccent.RECALL, SpellFx.vec(m, 0), SpellFx.vec(m, 3), m.ids(), 40, 0, 0, null);
                case SpellFxMessage.SOUL -> new AccentFx(RingAccent.SOUL, SpellFx.vec(m, 0), SpellFx.vec(m, 3), m.ids(), 30, 0, 0, null);
                case SpellFxMessage.ECHO_SUMMON -> new AccentFx(RingAccent.ECHO_SUMMON, SpellFx.vec(m, 3), null, m.ids(), 50, 0, 0, null);
                case SpellFxMessage.ECHO_DISSOLVE -> new AccentFx(RingAccent.ECHO_DISSOLVE, SpellFx.vec(m, 0), null, m.ids(), 50, 0, 0, null);
                default -> null;
            };
        } catch (RuntimeException e) {
            TheabyssMod.LOGGER.warn("Spell accent {} skipped", m.kind(), e);
            return null;
        }
    }

    private static Vec3[] points(SpellFxMessage m, int start) {
        int n = (m.data().size() - start) / 3;
        Vec3[] out = new Vec3[Math.max(0, n)];
        for (int i = 0; i < out.length; i++) {
            out[i] = SpellFx.vec(m, start + i * 3);
        }
        return out;
    }

    private static float lifeOf(int code) {
        return switch (code) {
            case RingAccent.FIRESTORM -> 200.0F;
            case RingAccent.SPEED -> 400.0F;
            case RingAccent.JUGGER -> 160.0F;
            case RingAccent.REGENERATION -> 110.0F;
            case RingAccent.CURSE, RingAccent.SLIDE -> 80.0F;
            case RingAccent.FREEZE, RingAccent.OCEAN, RingAccent.EAGLE, RingAccent.TIME, RingAccent.SEEKER -> 70.0F;
            case RingAccent.FIRE_TRAIL, RingAccent.BLACK_TRAIL, RingAccent.FLIGHT -> 14.0F;
            default -> 44.0F;
        };
    }

    @Override
    public float life() {
        return this.life;
    }

    @Override
    public void tick(ClientLevel level) {
        this.level = level;
        try {
            int now = this.t++;
            this.step(now);
            this.stepField(now);
        } catch (RuntimeException e) {
            TheabyssMod.LOGGER.warn("Spell accent {} failed", this.code, e);
            this.kill();
        }
    }

    private void step(int t) {
        switch (this.code) {
            case RingAccent.FIRE -> this.fire(t);
            case RingAccent.FIRESTORM -> this.firestorm(t);
            case RingAccent.FIRE_TRAIL -> this.fireTrail(t);
            case RingAccent.FIRE_HIT -> this.fireHit(t);
            case RingAccent.BLACK_TRAIL -> this.blackTrail(t);
            case RingAccent.BLACK_HIT -> this.blackHit(t);
            case RingAccent.THUNDER -> this.thunder(t);
            case RingAccent.ELECTRO -> this.electro(t);
            case RingAccent.FREEZE -> this.freeze(t);
            case RingAccent.OCEAN -> this.ocean(t);
            case RingAccent.REGENERATION -> this.regeneration(t);
            case RingAccent.CURSE -> this.curse(t);
            case RingAccent.STEP_DEPART -> this.stepDepart(t);
            case RingAccent.STEP_ARRIVE -> this.stepArrive(t);
            case RingAccent.SPEED -> this.speed(t);
            case RingAccent.JUGGER -> this.jugger(t);
            case RingAccent.EAGLE -> this.eagle(t);
            case RingAccent.TELEKINETIC -> this.telekinetic(t);
            case RingAccent.NIGHTBLADE -> this.nightblade(t);
            case RingAccent.NATURE -> this.nature(t);
            case RingAccent.FANGS -> this.fangs(t);
            case RingAccent.TIME -> this.time(t);
            case RingAccent.HOME -> this.home(t);
            case RingAccent.POCKET -> this.pocket(t);
            case RingAccent.SEEKER -> this.seeker(t);
            case RingAccent.SPECTRAL -> this.spectral(t);
            case RingAccent.INVISIBILITY -> this.invisibility(t);
            case RingAccent.FLIGHT -> this.flight(t);
            case RingAccent.SLIDE -> this.slide(t);
            case RingAccent.ENDER -> this.ender(t);
            case RingAccent.METEOR -> this.meteor(t);
            case RingAccent.VOID -> this.voidRift(t);
            case RingAccent.TRAIL -> this.trail(t);
            case RingAccent.SWAP -> this.swap(t);
            case RingAccent.REWIND -> this.rewind(t);
            case RingAccent.TETHER -> this.tether(t);
            case RingAccent.TIDAL -> this.tidal(t);
            case RingAccent.HEAL -> this.heal(t);
            case RingAccent.WALL -> this.wall(t);
            case RingAccent.HEX -> this.hex(t);
            case RingAccent.HEX_END -> this.hexEnd(t);
            case RingAccent.OVERCHARGE -> this.overcharge(t);
            case RingAccent.OVERCHARGE_FIRE -> this.overchargeFire(t);
            case RingAccent.DASH -> this.dash(t);
            case RingAccent.FLAME_TRAIL -> this.flameTrail(t);
            case RingAccent.ICE -> this.ice(t);
            case RingAccent.ICE_BURST -> this.iceBurst(t);
            case RingAccent.RECALL_MARK -> this.recallMark(t);
            case RingAccent.RECALL -> this.recall(t);
            case RingAccent.SOUL -> this.soul(t);
            case RingAccent.ECHO_SUMMON -> this.echoSummon(t);
            case RingAccent.ECHO_DISSOLVE -> this.echoDissolve(t);
            default -> this.kill();
        }
    }

    private void fire(int t) {
        if (t == 0) {
            this.burstMotes(this.at.add(0.0, 0.3, 0.0), 30, 0.3, 0.42, 0.13F, 26, RingVisuals.EMBER, 0.012F);
            this.puffRing(this.at, 12, 2.5, 0xFFB868, 0.55F);
        }
        if (t < 16 && t % 2 == 0) {
            double r = 1.0 + t * 0.6;
            for (int i = 0; i < 6; i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                this.sprite(this.loop(TheabyssModParticleTypes.SP_FLAME_ORB, 0.42F, 16), this.at.add(Math.cos(a) * r, 0.25, Math.sin(a) * r), new Vec3(Math.cos(a) * 0.08, 0.05, Math.sin(a) * 0.08));
            }
        }
        if (t == 6) {
            this.eachTarget(p -> {
                for (int i = 0; i < 5; i++) {
                    this.sprite(this.loop(TheabyssModParticleTypes.SP_FLAME_ORB, 0.34F, 18), p.add(this.spread(0.4)), new Vec3(0.0, 0.06, 0.0));
                }
                this.burstMotes(p, 6, 0.2, 0.2, 0.1F, 18, RingVisuals.FIRE, 0.01F);
            });
        }
    }

    private void firestorm(int t) {
        Vec3 c = this.centre();
        if (t % 2 == 0) {
            for (int k = 0; k < 2; k++) {
                double h = this.random.nextDouble() * 6.0;
                double a = t * 0.42 + k * Math.PI + h * 0.7;
                double r = 1.4 + h * 0.6;
                this.mote(c.add(Math.cos(a) * r, h, Math.sin(a) * r), new Vec3(-Math.sin(a) * 0.28, 0.07, Math.cos(a) * 0.28), 0.1F + this.random.nextFloat() * 0.07F, 24, k == 0 ? RingVisuals.EMBER : RingVisuals.FIRE, -0.002F);
            }
        }
        if (t % 3 == 0) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = 3.0 + this.random.nextDouble() * 13.0;
            Vec3 p = c.add(Math.cos(a) * r, 0.4 + this.random.nextDouble() * 1.8, Math.sin(a) * r);
            this.sprite(this.tint(TheabyssModParticleTypes.SP_FX_PIXEL_PUFF, 0xFFB060, 0.8F, 42), p, new Vec3(0.0, 0.06, 0.0));
            this.sprite(this.loop(TheabyssModParticleTypes.SP_METEOR_EMBER, 0.3F, 30), p, new Vec3(0.0, 0.1, 0.0));
        }
        if (t % 4 == 0) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = this.random.nextDouble() * 8.0;
            this.sprite(this.tint(TheabyssModParticleTypes.SP_FX_PIXEL_PUFF, 0x4A3A34, 1.3F, 60), c.add(Math.cos(a) * r, 5.0 + this.random.nextDouble() * 3.0, Math.sin(a) * r), new Vec3(0.0, 0.03, 0.0));
        }
    }

    private void fireTrail(int t) {
        if (t == 0) {
            this.sprite(this.loop(TheabyssModParticleTypes.SP_FLAME_ORB, 0.3F, 12), this.at, Vec3.ZERO);
            this.mote(this.at.add(this.spread(0.2)), this.spread(0.08), 0.07F, 12, RingVisuals.EMBER, 0.006F);
        }
    }

    private void fireHit(int t) {
        if (t == 0) {
            this.burstMotes(this.at, 20, 0.2, 0.34, 0.12F, 24, RingVisuals.EMBER, 0.018F);
            this.puffRing(this.at, 10, 1.3, 0xFFB868, 0.5F);
            for (int i = 0; i < 8; i++) {
                this.sprite(this.loop(TheabyssModParticleTypes.SP_FLAME_ORB, 0.4F, 16), this.at.add(this.spread(0.6)), this.spread(0.06));
            }
            this.sparks(this.at, 10, RingVisuals.EMBER, 0.25);
        }
    }

    private void blackTrail(int t) {
        if (t == 0) {
            this.sprite(this.loop(TheabyssModParticleTypes.SP_RIFT_WISP, 0.32F, 12), this.at, Vec3.ZERO);
            this.mote(this.at.add(this.spread(0.2)), this.spread(0.07), 0.07F, 12, RingVisuals.BLACKSTRIKE, 0.0F);
        }
    }

    private void blackHit(int t) {
        if (t == 0) {
            this.sprite(this.loop(TheabyssModParticleTypes.SP_VOID_CORE, 0.9F, 26), this.at, Vec3.ZERO);
            for (int i = 0; i < 8; i++) {
                this.sprite(this.loop(TheabyssModParticleTypes.SP_RIFT_WISP, 0.42F, 22), this.at.add(this.spread(0.7)), this.spread(0.05));
            }
            this.burstMotes(this.at, 18, 0.2, 0.3, 0.11F, 22, RingVisuals.BLACKSTRIKE, 0.0F);
        }
    }

    private void thunder(int t) {
        if (t == 0) {
            this.burstMotes(this.at.add(0.0, 0.3, 0.0), 14, 0.2, 0.34, 0.09F, 18, RingVisuals.THUNDER, 0.02F);
            this.sparks(this.at.add(0.0, 0.4, 0.0), 10, RingVisuals.THUNDER, 0.3);
            for (int i = 0; i < 3; i++) {
                this.sprite(this.loop(TheabyssModParticleTypes.SP_THUNDER_ORB, 0.5F, 14), this.at.add(this.spread(0.6)).add(0.0, 0.7, 0.0), Vec3.ZERO);
            }
        }
    }

    private void electro(int t) {
        if (t == 0) {
            this.burstMotes(this.centre().add(0.0, 1.0, 0.0), 6, 0.3, 0.14, 0.07F, 16, RingVisuals.ELECTRO, 0.0F);
        }
        if (t < 12 && t % 3 == 0) {
            this.eachTarget(p -> this.burstMotes(p, 3, 0.35, 0.1, 0.07F, 14, RingVisuals.ELECTRO, 0.0F));
        }
    }

    private void freeze(int t) {
        Vec3 c = this.centre();
        if (t < 30 && t % 3 == 0) {
            for (int i = 0; i < 2; i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double r = 1.5 + this.random.nextDouble() * 4.0;
                this.sprite(this.loop(TheabyssModParticleTypes.SP_FROST_FLAKE, 0.3F, 36), c.add(Math.cos(a) * r, 0.3 + this.random.nextDouble() * 2.2, Math.sin(a) * r), new Vec3(0.0, 0.01, 0.0));
            }
        }
        if (t == 0) {
            this.burstMotes(c.add(0.0, 1.0, 0.0), 22, 0.5, 0.26, 0.1F, 32, RingVisuals.ICE, 0.012F);
        }
        if (t == 10) {
            this.eachTarget(p -> {
                for (int i = 0; i < 2; i++) {
                    this.sprite(this.loop(TheabyssModParticleTypes.SP_ICE_SHARD, 0.34F, 40), p.add(this.spread(0.8)), new Vec3(0.0, 0.01, 0.0));
                }
                this.sprite(this.loop(TheabyssModParticleTypes.SP_FROST_FLAKE, 0.4F, 40), p, Vec3.ZERO);
                this.burstMotes(p, 5, 0.3, 0.12, 0.08F, 24, RingVisuals.ICE, 0.012F);
            });
        }
    }

    private void ocean(int t) {
        Vec3 c = this.centre();
        if (t < 34 && t % 2 == 0) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = 0.8 + this.random.nextDouble() * 1.8;
            Vec3 p = c.add(Math.cos(a) * r, 0.2 + this.random.nextDouble() * 0.6, Math.sin(a) * r);
            this.sprite(this.loop(TheabyssModParticleTypes.SP_WATER_GLOB, 0.26F, 30), p, new Vec3(0.0, 0.05, 0.0));
            this.sprite(this.tint(TheabyssModParticleTypes.SP_FX_RING_BUBBLE, RingVisuals.OCEAN, 0.1F, 28), p.add(this.spread(0.5)), new Vec3(0.0, 0.08, 0.0));
        }
    }

    private void regeneration(int t) {
        Vec3 c = this.centre();
        if (t % 8 == 0 && t < 90) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            this.sprite(this.loop(TheabyssModParticleTypes.SP_HEAL_MOTE, 0.24F, 34), c.add(Math.cos(a) * 1.2, 0.2, Math.sin(a) * 1.2), new Vec3(0.0, 0.05, 0.0));
        }
    }

    private void curse(int t) {
        Vec3 c = this.centre();
        if (t == 0) {
            this.sprite(this.loop(TheabyssModParticleTypes.SP_CURSE_SEAL, 1.2F, 60), c.add(0.0, 2.8, 0.0), Vec3.ZERO);
        }
        if (t < 50 && t % 3 == 0) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            this.sprite(this.loop(TheabyssModParticleTypes.SP_HEX_FLAME, 0.3F, 28), c.add(Math.cos(a) * 3.2, 0.2, Math.sin(a) * 3.2), new Vec3(0.0, 0.04, 0.0));
        }
    }

    private void stepDepart(int t) {
        if (t == 0) {
            for (int i = 0; i < 10; i++) {
                this.sprite(this.loop(TheabyssModParticleTypes.SP_STEP_SMOKE, 0.5F, 28), this.at.add(this.spread(0.6).multiply(1.0, 0.5, 1.0)).add(0.0, 0.5, 0.0), new Vec3(0.0, 0.015, 0.0));
            }
            this.burstMotes(this.at.add(0.0, 1.0, 0.0), 12, 0.35, 0.14, 0.07F, 22, RingVisuals.TELEPORT, 0.0F);
        }
    }

    private void stepArrive(int t) {
        if (t == 0) {
            this.burstMotes(this.at.add(0.0, 0.3, 0.0), 14, 0.5, 0.18, 0.08F, 24, RingVisuals.TELEPORT_TIP, -0.002F);
            this.sparks(this.at.add(0.0, 0.5, 0.0), 8, RingVisuals.TELEPORT, 0.2);
            for (int i = 0; i < 4; i++) {
                double a = i * Math.PI / 2.0;
                this.sprite(this.loop(TheabyssModParticleTypes.SP_SWAP_SPARK, 0.3F, 20), this.at.add(Math.cos(a) * 1.3, 0.4, Math.sin(a) * 1.3), Vec3.ZERO);
            }
        }
    }

    private void speed(int t) {
        Entity e = this.entity(0);
        if (e == null) {
            return;
        }
        this.remember(e.position());
        if (e.getDeltaMovement().horizontalDistanceSqr() > 0.01) {
            if (t % 3 == 0) {
                this.mote(e.position().add(this.spread(0.6).multiply(1.0, 0.0, 1.0)).add(0.0, 0.1 + this.random.nextDouble() * 0.9, 0.0), Vec3.ZERO, 0.07F, 14, RingVisuals.SPEED, 0.0F);
            }
            if (t % 5 == 0) {
                this.sprite(this.loop(TheabyssModParticleTypes.SP_SPEED_ZAP, 0.3F, 8), e.position().add(this.spread(0.5).multiply(1.0, 0.0, 1.0)).add(0.0, 0.4, 0.0), Vec3.ZERO);
            }
        }
    }

    private void jugger(int t) {
        if (t == 0) {
            this.puffRing(this.centre(), 16, 2.6, RingVisuals.JUGGER, 0.6F);
        }
    }

    private void eagle(int t) {
        if (t == 0) {
            this.burstMotes(this.centre().add(0.0, 1.2, 0.0), 12, 0.4, 0.14, 0.07F, 26, RingVisuals.EAGLE, -0.002F);
        }
        if (t < 50 && t % 7 == 0) {
            this.eachTargetTop(p -> this.sprite(this.tint(TheabyssModParticleTypes.SP_FX_CROSS_MOTE, RingVisuals.EAGLE, 0.15F, 22), p.add(this.spread(0.4)), new Vec3(0.0, 0.03, 0.0)));
        }
    }

    private void telekinetic(int t) {
        if (t % 5 == 0 && t < 30) {
            this.eachTarget(p -> this.burstMotes(p, 2, 0.5, 0.05, 0.06F, 18, RingVisuals.TELEKINETIC, 0.0F));
        }
    }

    private void nightblade(int t) {
        if (t == 0) {
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3.0;
                this.sprite(this.loop(TheabyssModParticleTypes.SP_NIGHT_CRESCENT, 0.7F, 24), this.at.add(Math.cos(a) * 3.0, 0.8, Math.sin(a) * 3.0), new Vec3(Math.cos(a) * 0.05, 0.02, Math.sin(a) * 0.05));
            }
            this.burstMotes(this.at.add(0.0, 1.0, 0.0), 24, 1.0, 0.24, 0.09F, 28, RingVisuals.NIGHTBLADE, 0.0F);
        }
    }

    private void nature(int t) {
        if (t == 0) {
            Vec3 c = this.at.add(0.5, 1.0, 0.5);
            for (int i = 0; i < 7; i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double r = this.random.nextDouble() * 1.8;
                this.sprite(this.loop(TheabyssModParticleTypes.SP_NATURE_BLOOM, 0.3F, 34), c.add(Math.cos(a) * r, 0.2 + this.random.nextDouble() * 0.5, Math.sin(a) * r), new Vec3(0.0, 0.02, 0.0));
            }
            this.burstMotes(c.add(0.0, 0.3, 0.0), 14, 0.8, 0.12, 0.07F, 28, RingVisuals.NATURE, 0.006F);
        }
    }

    private void fangs(int t) {
        if (t == 0) {
            for (int i = 0; i < 10; i++) {
                double a = i * Math.PI / 5.0;
                this.sprite(this.loop(TheabyssModParticleTypes.SP_ICE_SHARD, 0.4F, 26), this.at.add(Math.cos(a) * 3.5, 0.6, Math.sin(a) * 3.5), new Vec3(Math.cos(a) * 0.05, 0.03, Math.sin(a) * 0.05));
            }
            this.burstMotes(this.at.add(0.0, 0.4, 0.0), 22, 1.5, 0.22, 0.09F, 26, RingVisuals.ICE, 0.012F);
        }
    }

    private void time(int t) {
        Vec3 c = this.centre();
        if (t == 0) {
            for (int i = 0; i < 3; i++) {
                double a = i * Math.PI * 2.0 / 3.0;
                this.sprite(this.loop(TheabyssModParticleTypes.SP_CLOCK_MARK, 0.75F, 60), c.add(Math.cos(a) * 2.4, 1.3, Math.sin(a) * 2.4), Vec3.ZERO);
            }
            this.burstMotes(c.add(0.0, 1.0, 0.0), 22, 1.2, 0.05, 0.08F, 60, GOLD, -0.001F);
            this.eachTarget(p -> this.sprite(this.loop(TheabyssModParticleTypes.SP_CLOCK_MARK, 0.5F, 50), p.add(0.0, 1.0, 0.0), Vec3.ZERO));
        }
    }

    private void home(int t) {
        if (t == 0) {
            this.sprite(this.loop(TheabyssModParticleTypes.SP_RECALL_RUNE, 0.9F, 36), this.at.add(0.0, 1.0, 0.0), Vec3.ZERO);
            this.burstMotes(this.at.add(0.0, 0.2, 0.0), 18, 0.6, 0.12, 0.08F, 34, RingVisuals.ABYSS, -0.004F);
        }
    }

    private void pocket(int t) {
        if (t == 0) {
            this.burstMotes(this.at.add(0.0, 1.0, 0.0), 22, 1.3, 0.05, 0.08F, 28, RingVisuals.POCKET, 0.0F);
        }
        if (t == 8) {
            this.sparks(this.at.add(0.0, 1.0, 0.0), 10, RingVisuals.POCKET, 0.22);
        }
    }

    private void seeker(int t) {
        if (t == 0) {
            this.burstMotes(this.at.add(0.0, 0.2, 0.0), 14, 0.8, 0.1, 0.07F, 28, RingVisuals.ABYSS, -0.003F);
        }
    }

    private void spectral(int t) {
        Vec3 c = this.centre();
        if (t == 0) {
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3.0;
                this.sprite(this.loop(TheabyssModParticleTypes.SP_SOUL_PEARL, 0.24F, 34), c.add(Math.cos(a) * 1.6, 0.5 + (i % 3) * 0.6, Math.sin(a) * 1.6), new Vec3(0.0, 0.02, 0.0));
            }
            this.burstMotes(c.add(0.0, 1.0, 0.0), 16, 0.6, 0.1, 0.07F, 32, PEARL, -0.002F);
        }
    }

    private void invisibility(int t) {
        Entity e = this.entity(0);
        if (t == 0 && e != null) {
            double w = e.getBbWidth();
            double h = e.getBbHeight();
            for (int i = 0; i < 44; i++) {
                Vec3 p = e.position().add((this.random.nextDouble() - 0.5) * w * 1.1, this.random.nextDouble() * h, (this.random.nextDouble() - 0.5) * w * 1.1);
                this.mote(p, new Vec3((this.random.nextDouble() - 0.5) * 0.04, 0.02 + this.random.nextDouble() * 0.05, (this.random.nextDouble() - 0.5) * 0.04), 0.05F + this.random.nextFloat() * 0.05F, 18 + this.random.nextInt(14), RingVisuals.INVIS, -0.001F);
            }
        }
    }

    private void flight(int t) {
        if (t == 0) {
            Vec3 c = this.centre();
            for (int i = 0; i < 3; i++) {
                this.mote(c.add(this.spread(0.7).multiply(1.0, 0.0, 1.0)), new Vec3(0.0, -0.06, 0.0), 0.08F, 13, RingVisuals.LILAC, 0.0F);
            }
            this.sprite(this.tint(TheabyssModParticleTypes.SP_FX_SQUARE_MOTE, RingVisuals.LILAC, 0.12F, 14), c.add(this.spread(0.6).multiply(1.0, 0.0, 1.0)), new Vec3(0.0, -0.04, 0.0));
        }
    }

    private void slide(int t) {
        Entity e = this.entity(0);
        if (e != null) {
            this.remember(e.position());
            if (t % 3 == 0) {
                this.mote(e.position().add(this.spread(0.8)).add(0.0, 0.9, 0.0), e.getDeltaMovement().scale(-0.2), 0.06F, 14, RingVisuals.LILAC, 0.0F);
            }
        }
    }

    private void ender(int t) {
        if (t == 0) {
            Entity e = this.entity(0);
            Vec3 c = e == null ? this.at : e.position().add(0.0, 0.9, 0.0).add(e.getLookAngle().scale(1.4));
            this.sprite(this.loop(TheabyssModParticleTypes.SP_VOID_CORE, 0.3F, 18), c, Vec3.ZERO);
            for (int i = 0; i < 12; i++) {
                Vec3 off = this.spread(1.2);
                this.mote(c.add(off), off.scale(-0.08), 0.05F, 14, 0x9A5FE0, 0.0F);
            }
        }
    }

    private void meteor(int t) {
        int impact = Math.max(1, this.param);
        if (t < impact) {
            Vec3 p = this.at.lerp(this.second, t / (double)impact);
            this.sprite(this.loop(TheabyssModParticleTypes.SP_FLAME_ORB, 0.8F, 12), p.add(this.spread(0.8)), Vec3.ZERO);
            this.sprite(this.tint(TheabyssModParticleTypes.SP_FX_PIXEL_PUFF, 0xFFB060, 0.7F, 26), p.add(this.spread(1.0)), new Vec3(0.0, 0.03, 0.0));
            this.mote(p.add(this.spread(0.8)), this.spread(0.12), 0.12F, 22, RingVisuals.EMBER, 0.008F);
        } else if (t == impact) {
            Vec3 c = this.second;
            this.burstMotes(c.add(0.0, 0.4, 0.0), 54, 0.8, 0.5, 0.13F, 36, RingVisuals.EMBER, 0.022F);
            for (int i = 0; i < 6; i++) {
                this.sprite(this.loop(TheabyssModParticleTypes.SP_METEOR_EMBER, 0.34F, 38).gravity(0.03F), c.add(0.0, 0.5, 0.0), this.spread(0.5).add(0.0, 0.42, 0.0));
            }
            this.puffRing(c, 26, 4.0, 0xFFB868, 0.95F);
            this.sparks(c.add(0.0, 0.5, 0.0), 22, RingVisuals.EMBER, 0.42);
        } else if (t < impact + 36 && t % 2 == 0) {
            for (int i = 0; i < 2; i++) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                double r = this.random.nextDouble() * 3.0;
                this.sprite(this.tint(TheabyssModParticleTypes.SP_FX_PIXEL_PUFF, i == 0 ? 0xFFB868 : 0x5A4640, 1.0F + this.random.nextFloat() * 0.5F, 50), this.second.add(Math.cos(a) * r, 0.6 + (t - impact) * 0.16, Math.sin(a) * r), new Vec3(Math.cos(a) * 0.02, 0.05, Math.sin(a) * 0.02));
            }
        }
    }

    private void voidRift(int t) {
        if (t == Math.max(1, this.param)) {
            this.burstMotes(this.at, 40, 0.8, 0.5, 0.12F, 26, RingVisuals.BLACKSTRIKE, 0.0F);
        }
    }

    private void trail(int t) {
        for (int id : this.ids) {
            Entity e = this.level.getEntity(id);
            if (e == null) {
                continue;
            }
            Vec3 p = e.position().add(0.0, e.getBbHeight() * 0.5, 0.0);
            if (p.distanceToSqr(this.camera()) < 25.0) {
                continue;
            }
            if (this.colour == RingVisuals.NIGHTBLADE && t % 2 == 0) {
                this.sprite(this.loop(TheabyssModParticleTypes.SP_NIGHT_CRESCENT, 0.42F, 10), p, Vec3.ZERO);
            } else if (t % 2 == 0) {
                this.sprite(this.loop(TheabyssModParticleTypes.SP_FLAME_ORB, 0.24F, 10), p, Vec3.ZERO);
            }
            this.mote(p.add(this.spread(0.25)), this.spread(0.05), 0.06F, 12, this.colour, 0.0F);
        }
    }

    private void swap(int t) {
        if (t == 0) {
            for (Vec3 p : new Vec3[] {this.at, this.second}) {
                this.sprite(this.loop(TheabyssModParticleTypes.SP_SWAP_SPARK, 0.48F, 22), p.add(0.0, 1.0, 0.0), Vec3.ZERO);
                this.burstMotes(p.add(0.0, 1.0, 0.0), 14, 0.4, 0.16, 0.08F, 20, RingVisuals.TELEPORT, 0.0F);
            }
        }
    }

    private void rewind(int t) {
        if (this.path.length < 2) {
            return;
        }
        if (t % 2 == 0) {
            Vec3 p = this.path[this.random.nextInt(this.path.length)].add(0.0, 0.8, 0.0);
            this.mote(p.add(this.spread(0.5)), new Vec3(0.0, 0.02, 0.0), 0.08F, 26, t % 4 == 0 ? GOLD : RingVisuals.TELEPORT_TIP, 0.0F);
        }
        if (t % 10 == 0) {
            Vec3 p = this.path[this.random.nextInt(this.path.length)].add(0.0, 1.0, 0.0);
            if (p.distanceToSqr(this.camera()) > 9.0) {
                this.sprite(this.loop(TheabyssModParticleTypes.SP_CLOCK_MARK, 0.6F, 24), p, Vec3.ZERO);
            }
        }
    }

    private void tether(int t) {
        Entity a = this.entity(0);
        Entity b = this.entity(1);
        if (a != null && b != null && t % 2 == 0) {
            Vec3 p = a.position().lerp(b.position(), this.random.nextDouble()).add(0.0, 1.0, 0.0);
            this.mote(p.add(this.spread(0.3)), b.position().subtract(a.position()).normalize().scale(-0.1), 0.08F, 16, RingVisuals.POCKET, 0.0F);
        }
    }

    private void tidal(int t) {
        Vec3 front = this.at.add(this.second.scale(t * 0.6));
        Vec3 side = new Vec3(-this.second.z, 0.0, this.second.x);
        double half = 1.5 + t * 0.09;
        for (int i = 0; i < 3; i++) {
            Vec3 p = front.add(side.scale((this.random.nextDouble() - 0.5) * 2.0 * half)).add(0.0, 0.6 + this.random.nextDouble() * 2.4, 0.0);
            if (i == 0) {
                this.sprite(this.loop(TheabyssModParticleTypes.SP_WATER_GLOB, 0.3F, 18), p, this.second.scale(0.2).add(0.0, 0.08, 0.0));
            } else if (i == 1) {
                this.sprite(this.tint(TheabyssModParticleTypes.SP_FX_RING_BUBBLE, RingVisuals.OCEAN, 0.12F, 22), p, new Vec3(0.0, 0.08, 0.0));
            } else {
                this.mote(p, this.second.scale(0.28).add(0.0, 0.18, 0.0), 0.07F, 18, 0xD8F8FF, 0.025F);
            }
        }
    }

    private void heal(int t) {
        Vec3 c = this.centre();
        if (t < 30 && t % 2 == 0) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = this.random.nextDouble() * 7.0;
            this.mote(c.add(Math.cos(a) * r, 0.2, Math.sin(a) * r), new Vec3(0.0, 0.06, 0.0), 0.08F, 24, RingVisuals.REG_GOLD, 0.0F);
        }
        if (t == 6) {
            for (int i = 1; i < this.ids.size(); i++) {
                Entity e = this.entity(i);
                if (e != null) {
                    this.sprite(this.loop(TheabyssModParticleTypes.SP_HEAL_MOTE, 0.4F, 30), e.position().add(0.0, e.getBbHeight() + 0.3, 0.0), Vec3.ZERO);
                }
            }
        }
    }

    private void wall(int t) {
        if (t % 2 == 0) {
            double u = (this.random.nextDouble() - 0.5) * 11.0;
            Vec3 p = this.at.add(this.second.scale(u)).add(0.0, this.random.nextDouble() * 3.2, 0.0);
            this.mote(p, new Vec3(0.0, 0.05, 0.0), 0.05F + this.random.nextFloat() * 0.04F, 24, RingVisuals.ICE, 0.0F);
        }
        if (t % 6 == 0) {
            double u = (this.random.nextDouble() - 0.5) * 10.0;
            this.sprite(this.loop(TheabyssModParticleTypes.SP_ICE_SHARD, 0.3F, 26), this.at.add(this.second.scale(u)).add(0.0, 0.5 + this.random.nextDouble() * 2.4, 0.0), Vec3.ZERO);
        }
    }

    private void hex(int t) {
        Entity e = this.entity(0);
        if (e != null && t % 8 == 0) {
            this.sprite(this.loop(TheabyssModParticleTypes.SP_HEX_FLAME, 0.26F, 22), e.position().add(this.spread(e.getBbWidth() + 0.5).multiply(1.0, 0.0, 1.0)).add(0.0, 0.2 + this.random.nextDouble() * e.getBbHeight(), 0.0), new Vec3(0.0, 0.03, 0.0));
        }
    }

    private void hexEnd(int t) {
        if (t == 0) {
            this.burstMotes(this.at, 24, 0.4, 0.34, 0.1F, 24, RingVisuals.CURSE, 0.0F);
            this.sprite(this.loop(TheabyssModParticleTypes.SP_HEX_FLAME, 0.8F, 18), this.at, Vec3.ZERO);
        }
    }

    private void overcharge(int t) {
        Entity e = this.entity(0);
        if (e != null && t == 0) {
            Vec3 look = e.getLookAngle().multiply(1.0, 0.0, 1.0);
            look = look.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : look.normalize();
            Vec3 side = new Vec3(-look.z, 0.0, look.x);
            for (int i = -1; i <= 1; i += 2) {
                this.sprite(this.loop(TheabyssModParticleTypes.SP_THUNDER_ORB, 0.26F, 24), e.position().add(side.scale(i * 1.5)).add(0.0, 1.1, 0.0), Vec3.ZERO);
            }
            this.burstMotes(e.position().add(0.0, 1.0, 0.0), 10, 0.7, 0.1, 0.06F, 18, RingVisuals.THUNDER, 0.0F);
        }
    }

    private void overchargeFire(int t) {
        Entity target = this.entity(1);
        if (t == 0 && target != null) {
            Vec3 p = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
            this.sprite(this.loop(TheabyssModParticleTypes.SP_THUNDER_ORB, 0.6F, 12), p, Vec3.ZERO);
            this.burstMotes(p, 14, 0.3, 0.3, 0.08F, 16, RingVisuals.THUNDER, 0.015F);
        }
    }

    private void dash(int t) {
        Entity e = this.entity(0);
        if (e == null) {
            return;
        }
        this.remember(e.position());
        if (t < this.life - 10) {
            this.mote(e.position().add(this.spread(0.7).multiply(1.0, 0.0, 1.0)).add(0.0, this.random.nextDouble() * 1.5, 0.0), e.getDeltaMovement().scale(-0.1), 0.07F, 16, this.colour, 0.0F);
            if (t % 2 == 0) {
                this.sprite(this.tint(TheabyssModParticleTypes.SP_FX_RECT_SPARK, this.colour, 0.14F, 10), e.position().add(this.spread(0.8)).add(0.0, 0.8, 0.0), Vec3.ZERO);
            }
        }
    }

    private void flameTrail(int t) {
        if (this.path.length == 0) {
            return;
        }
        if (t % 2 == 0) {
            Vec3 p = this.path[this.random.nextInt(this.path.length)];
            this.sprite(this.loop(TheabyssModParticleTypes.SP_FLAME_ORB, 0.32F, 14), p.add(this.spread(0.5).multiply(1.0, 0.0, 1.0)).add(0.0, 0.2, 0.0), new Vec3(0.0, 0.05, 0.0));
        }
        if (t % 8 == 0) {
            Vec3 p = this.path[this.random.nextInt(this.path.length)];
            this.sprite(this.tint(TheabyssModParticleTypes.SP_FX_PIXEL_PUFF, 0xFFB060, 0.45F, 32), p.add(0.0, 0.4, 0.0), new Vec3(0.0, 0.05, 0.0));
            this.mote(p.add(0.0, 0.2, 0.0), this.spread(0.1).add(0.0, 0.12, 0.0), 0.08F, 22, RingVisuals.EMBER, 0.01F);
        }
    }

    private void ice(int t) {
        Entity e = this.entity(0);
        if (e != null && t % 12 == 0) {
            this.sprite(this.loop(TheabyssModParticleTypes.SP_FROST_FLAKE, 0.3F, 30), e.position().add(this.spread(e.getBbWidth() + 0.8).multiply(1.0, 0.0, 1.0)).add(0.0, this.random.nextDouble() * e.getBbHeight(), 0.0), Vec3.ZERO);
        }
    }

    private void iceBurst(int t) {
        if (t == 0) {
            this.burstMotes(this.at.add(0.0, 1.0, 0.0), 32, 0.5, 0.36, 0.12F, 30, RingVisuals.ICE, 0.025F);
            for (int i = 0; i < 8; i++) {
                this.sprite(this.loop(TheabyssModParticleTypes.SP_ICE_SHARD, 0.34F, 22), this.at.add(this.spread(1.5)).add(0.0, 1.0, 0.0), this.spread(0.07));
            }
            this.sprite(this.loop(TheabyssModParticleTypes.SP_FROST_FLAKE, 0.9F, 20), this.at.add(0.0, 1.0, 0.0), Vec3.ZERO);
        }
    }

    private void recallMark(int t) {
        if (t == 0) {
            this.sprite(this.loop(TheabyssModParticleTypes.SP_RECALL_RUNE, 0.8F, 32), this.at.add(0.0, 1.0, 0.0), Vec3.ZERO);
            this.burstMotes(this.at.add(0.0, 0.2, 0.0), 14, 0.5, 0.1, 0.07F, 30, RingVisuals.ABYSS, -0.004F);
        }
    }

    private void recall(int t) {
        if (t == 0) {
            for (Vec3 p : new Vec3[] {this.at, this.second}) {
                this.sprite(this.loop(TheabyssModParticleTypes.SP_RECALL_RUNE, 0.9F, 34), p.add(0.0, 1.0, 0.0), Vec3.ZERO);
                this.burstMotes(p.add(0.0, 0.3, 0.0), 18, 0.5, 0.14, 0.08F, 30, RingVisuals.ABYSS, -0.005F);
            }
        }
    }

    private void soul(int t) {
        if (t == 0) {
            this.sprite(this.loop(TheabyssModParticleTypes.SP_SOUL_PEARL, 0.4F, 26), this.at.add(0.0, 1.0, 0.0), Vec3.ZERO);
            this.burstMotes(this.second.add(0.0, 1.0, 0.0), 16, 0.4, 0.12, 0.07F, 24, PEARL, 0.0F);
        }
    }

    private void echoSummon(int t) {
        if (t == 28) {
            this.burstMotes(this.at.add(0.0, 1.0, 0.0), 18, 0.3, 0.2, 0.08F, 20, PEARL, 0.0F);
        }
    }

    private void echoDissolve(int t) {
        if (t == 0) {
            this.burstMotes(this.at.add(0.0, 1.0, 0.0), 20, 0.5, 0.1, 0.07F, 30, PEARL, -0.003F);
        }
    }

    @Override
    public void draw(Frame f) {
        this.lastCamera = f.camera;
        this.drawField(f);
        float age = this.age();
        switch (this.code) {
            case RingAccent.FIRESTORM -> this.tornado(f, this.centre(f), age, RingVisuals.FIRE, RingVisuals.EMBER, this.fade(10.0F, 20.0F));
            case RingAccent.OCEAN -> this.helixUp(f, this.centre(f), age, 1.4, 2.8, RingVisuals.OCEAN, RingVisuals.OCEAN_DEEP, this.fade(6.0F, 24.0F));
            case RingAccent.SPEED, RingAccent.SLIDE, RingAccent.DASH -> this.streaks(f, this.code == RingAccent.SPEED ? RingVisuals.SPEED : this.code == RingAccent.SLIDE ? RingVisuals.LILAC : this.colour);
            case RingAccent.EAGLE -> this.eagleShafts(f);
            case RingAccent.HOME, RingAccent.RECALL_MARK -> this.shaft(f, this.at, 6.0F, RingVisuals.ABYSS, this.fade(3.0F, 22.0F) * 0.8F);
            case RingAccent.SWAP -> this.helix(f, this.at.add(0.0, 1.0, 0.0), this.second.add(0.0, 1.0, 0.0), age, 0.22F, this.fade(1.0F, 10.0F) * 0.9F, RingVisuals.TELEPORT, RingVisuals.TELEPORT_TIP);
            case RingAccent.TETHER -> {
                Entity a = this.entity(0);
                Entity b = this.entity(1);
                if (a != null && b != null) {
                    this.helix(f, a.getPosition(f.pt).add(0.0, 1.0, 0.0), b.getPosition(f.pt).add(0.0, 1.0, 0.0), age, 0.14F, this.fade(2.0F, 6.0F) * 0.7F, RingVisuals.POCKET, 0xFFD36A);
                }
            }
            case RingAccent.WALL -> this.curtain(f, age);
            case RingAccent.OVERCHARGE_FIRE -> {
                Entity a = this.entity(0);
                Entity b = this.entity(1);
                if (a != null && b != null) {
                    this.helix(f, a.getPosition(f.pt).add(0.0, 1.1, 0.0), b.getPosition(f.pt).add(0.0, b.getBbHeight() * 0.5, 0.0), age, 0.2F, this.fade(1.0F, 8.0F), RingVisuals.THUNDER, 0xFFFFFF);
                }
            }
            case RingAccent.RECALL -> {
                this.shaft(f, this.at, 7.0F, RingVisuals.ABYSS, this.fade(2.0F, 26.0F) * 0.8F);
                this.shaft(f, this.second, 7.0F, RingVisuals.ABYSS, this.fade(2.0F, 26.0F) * 0.8F);
            }
            default -> {
            }
        }
    }

    private double fieldRadius() {
        return switch (this.code) {
            case RingAccent.SEEKER, RingAccent.ECHO_SUMMON -> 2.4;
            case RingAccent.HEAL -> 7.0;
            case RingAccent.REGENERATION -> 1.9;
            case RingAccent.FREEZE -> 4.5;
            case RingAccent.NIGHTBLADE, RingAccent.FANGS -> 5.0;
            case RingAccent.CURSE -> 4.0;
            case RingAccent.TIME, RingAccent.FIRE, RingAccent.METEOR -> 6.0;
            case RingAccent.HOME, RingAccent.RECALL, RingAccent.RECALL_MARK -> 1.6;
            case RingAccent.SPECTRAL, RingAccent.THUNDER, RingAccent.NATURE -> 2.0;
            case RingAccent.JUGGER -> 2.8;
            case RingAccent.EAGLE -> 2.5;
            default -> 0.0;
        };
    }

    private int fieldColour() {
        return switch (this.code) {
            case RingAccent.SEEKER, RingAccent.HOME, RingAccent.RECALL, RingAccent.RECALL_MARK -> RingVisuals.ABYSS;
            case RingAccent.ECHO_SUMMON -> RingVisuals.LILAC;
            case RingAccent.HEAL, RingAccent.REGENERATION -> RingVisuals.REG_GREEN;
            case RingAccent.FREEZE, RingAccent.FANGS -> RingVisuals.ICE;
            case RingAccent.NIGHTBLADE -> RingVisuals.NIGHTBLADE;
            case RingAccent.CURSE -> RingVisuals.CURSE;
            case RingAccent.TIME -> GOLD;
            case RingAccent.FIRE, RingAccent.METEOR -> RingVisuals.EMBER;
            case RingAccent.SPECTRAL -> PEARL;
            case RingAccent.THUNDER -> RingVisuals.THUNDER;
            case RingAccent.NATURE -> RingVisuals.NATURE;
            case RingAccent.JUGGER -> RingVisuals.JUGGER;
            case RingAccent.EAGLE -> RingVisuals.EAGLE;
            default -> 0xFFFFFF;
        };
    }

    private int fieldUntil() {
        return switch (this.code) {
            case RingAccent.SEEKER, RingAccent.CURSE -> 50;
            case RingAccent.HEAL, RingAccent.ECHO_SUMMON -> 30;
            case RingAccent.REGENERATION -> 80;
            case RingAccent.FREEZE, RingAccent.TIME -> 44;
            case RingAccent.NIGHTBLADE, RingAccent.FANGS, RingAccent.SPECTRAL -> 26;
            case RingAccent.METEOR -> Math.max(1, this.param) + 40;
            case RingAccent.THUNDER -> 10;
            case RingAccent.HOME, RingAccent.RECALL, RingAccent.RECALL_MARK -> 22;
            default -> 20;
        };
    }

    private Vec3 fieldCentre(Vec3 entityPos) {
        return switch (this.code) {
            case RingAccent.METEOR -> this.second;
            case RingAccent.NATURE -> this.at.add(0.5, 1.0, 0.5);
            case RingAccent.FIRE, RingAccent.NIGHTBLADE, RingAccent.FANGS, RingAccent.THUNDER, RingAccent.HOME, RingAccent.RECALL, RingAccent.RECALL_MARK, RingAccent.SEEKER, RingAccent.ECHO_SUMMON -> this.at;
            default -> entityPos;
        };
    }

    private void stepField(int t) {
        if (this.code == RingAccent.WALL && this.streaks.size() < 160) {
            for (int i = 0; i < 2; i++) {
                Vec3 base = this.at.add(this.second.scale((this.random.nextDouble() - 0.5) * 11.0));
                this.addStreak(base, RingVisuals.ICE, 2.0F + this.random.nextFloat() * 2.0F);
            }
        }
        Iterator<Streak> it = this.streaks.iterator();
        while (it.hasNext()) {
            Streak s = it.next();
            if (++s.age >= s.life) {
                it.remove();
            }
        }
        double radius = this.fieldRadius();
        int start = this.code == RingAccent.METEOR ? Math.max(1, this.param) : 0;
        if (radius <= 0.0 || t < start || t >= this.fieldUntil() || this.streaks.size() >= 160) {
            return;
        }
        Vec3 c = this.fieldCentre(this.centre());
        int rate = radius >= 4.0 ? 3 : 1;
        for (int i = 0; i < rate; i++) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = radius * Math.sqrt(this.random.nextDouble());
            this.addStreak(c.add(Math.cos(a) * r, 0.05, Math.sin(a) * r), this.fieldColour(), 1.4F + this.random.nextFloat() * 2.6F);
        }
    }

    private void addStreak(Vec3 base, int rgb, float height) {
        Streak s = new Streak();
        s.base = base;
        s.rgb = rgb;
        s.height = height;
        s.life = 12 + this.random.nextInt(14);
        this.streaks.add(s);
    }

    private void drawField(Frame f) {
        for (Streak s : this.streaks) {
            float u = Math.min(1.0F, (s.age + f.pt) / s.life);
            float head = (1.0F - (1.0F - u) * (1.0F - u)) * s.height;
            float tail = Math.max(0.0F, head - s.height * 0.5F);
            float a = Math.min(1.0F, u * 6.0F) * Math.min(1.0F, (1.0F - u) * 3.0F);
            Vec3 top = s.base.add(0.0, head, 0.0);
            Vec3 low = s.base.add(0.0, tail, 0.0);
            float[] w = {0.012F, 0.05F, 0.07F};
            float[] l = {0.0F, a * 0.55F, a};
            f.ribbon(new Vec3[] {low, low.lerp(top, 0.6), top}, w, l, s.rgb);
            f.flare(top, 0.2F, s.rgb, a * 0.85F);
        }
    }

    private void eagleShafts(Frame f) {
        float a = this.fade(5.0F, 24.0F) * 0.75F;
        for (int i = 1; i < this.ids.size(); i++) {
            Entity e = this.entity(i);
            if (e != null) {
                Vec3 p = e.getPosition(f.pt);
                f.line(p, p.add(0.0, e.getBbHeight() + 5.0, 0.0), 0.12F, a, RingVisuals.EAGLE);
            }
        }
    }

    private void curtain(Frame f, float age) {
        float a = this.fade(6.0F, 14.0F) * 0.5F;
        for (int i = -6; i <= 6; i++) {
            Vec3 base = this.at.add(this.second.scale(i * 0.92));
            float h = 2.4F + (float)Math.sin(age * 0.18 + i * 1.7) * 0.8F;
            float[] w = {0.42F, 0.42F, 0.3F};
            float[] l = {a, a * 0.55F, 0.0F};
            f.ribbon(new Vec3[] {base, base.add(0.0, h * 0.55, 0.0), base.add(0.0, h, 0.0)}, w, l, RingVisuals.ICE);
        }
    }

    private void shaft(Frame f, Vec3 c, float height, int rgb, float light) {
        if (light <= 0.01F) {
            return;
        }
        Vec3 top = c.add(0.0, height, 0.0);
        float[] w = {0.5F, 0.42F, 0.22F};
        float[] l = {light, light * 0.5F, 0.0F};
        f.ribbon(new Vec3[] {c, c.lerp(top, 0.5), top}, w, l, rgb);
        float[] w2 = {0.14F, 0.12F, 0.06F};
        f.ribbon(new Vec3[] {c, c.lerp(top, 0.5), top}, w2, l, 0xFFFFFF);
    }

    private void helix(Frame f, Vec3 a, Vec3 b, float age, float width, float light, int rgb, int strand) {
        if (light <= 0.01F) {
            return;
        }
        Vec3 axis = b.subtract(a);
        double length = axis.length();
        if (length < 0.2) {
            return;
        }
        Vec3 dir = axis.normalize();
        Vec3 s1 = dir.cross(new Vec3(0.0, 1.0, 0.0));
        s1 = s1.lengthSqr() < 1.0E-5 ? new Vec3(1.0, 0.0, 0.0) : s1.normalize();
        Vec3 s2 = dir.cross(s1).normalize();
        f.line(a, b, width, light, rgb);
        f.line(a, b, width * 0.35F, light, 0xFFFFFF);
        int n = Math.max(8, (int)(length * 5.0));
        for (int k = 0; k < 2; k++) {
            Vec3[] p = new Vec3[n + 1];
            float[] w = new float[n + 1];
            float[] l = new float[n + 1];
            for (int i = 0; i <= n; i++) {
                double u = i / (double)n;
                double env = Math.sin(u * Math.PI);
                double phase = u * length * 2.4 - age * 0.45 + k * Math.PI;
                double r = (0.18 + width) * env;
                p[i] = a.add(dir.scale(u * length)).add(s1.scale(Math.cos(phase) * r)).add(s2.scale(Math.sin(phase) * r));
                w[i] = width * 0.5F;
                l[i] = light * 0.85F;
            }
            f.ribbon(p, w, l, strand);
        }
        f.flare(a, width * 6.0F, rgb, light * 0.9F);
        f.flare(b, width * 7.0F, rgb, light);
    }

    private void helixUp(Frame f, Vec3 c, float age, double radius, double height, int rgb, int rgb2, float light) {
        int n = 34;
        for (int k = 0; k < 2; k++) {
            Vec3[] p = new Vec3[n + 1];
            float[] w = new float[n + 1];
            float[] l = new float[n + 1];
            for (int i = 0; i <= n; i++) {
                double u = i / (double)n;
                double a = u * 9.0 + age * 0.22 + k * Math.PI;
                p[i] = c.add(Math.cos(a) * radius * (1.0 - u * 0.35), u * height, Math.sin(a) * radius * (1.0 - u * 0.35));
                w[i] = 0.2F * (float)(1.0 - u * 0.7);
                l[i] = light * (float)Math.sin(u * Math.PI);
            }
            f.ribbon(p, w, l, k == 0 ? rgb : rgb2);
        }
    }

    private void tornado(Frame f, Vec3 c, float age, int rgb, int rgb2, float light) {
        int n = 44;
        for (int k = 0; k < 3; k++) {
            Vec3[] p = new Vec3[n + 1];
            float[] w = new float[n + 1];
            float[] l = new float[n + 1];
            for (int i = 0; i <= n; i++) {
                double u = i / (double)n;
                double h = u * 8.0;
                double a = age * 0.28 + k * Math.PI * 2.0 / 3.0 + h * 0.75;
                double r = 1.2 + h * 0.55;
                p[i] = c.add(Math.cos(a) * r, h, Math.sin(a) * r);
                w[i] = 0.5F * (float)(1.0 - u * 0.6);
                l[i] = light * 0.75F * (float)Math.sin(u * Math.PI);
            }
            f.ribbon(p, w, l, k == 0 ? rgb2 : rgb);
        }
    }

    private void streaks(Frame f, int rgb) {
        int n = this.history.size();
        if (n < 3) {
            return;
        }
        float a = Math.min(1.0F, (this.life - this.age()) / 10.0F);
        for (double h : new double[] {0.45, 1.25}) {
            Vec3[] p = new Vec3[n];
            float[] w = new float[n];
            float[] l = new float[n];
            for (int i = 0; i < n; i++) {
                float u = i / (float)(n - 1);
                p[i] = this.history.get(i).add(0.0, h, 0.0);
                w[i] = 0.22F * u;
                l[i] = 0.8F * u * a;
            }
            if (p[0].distanceToSqr(p[n - 1]) > 0.04) {
                f.ribbon(p, w, l, rgb);
            }
        }
    }

    private static Vec3[] circle(Vec3 c, double radius) {
        int n = Math.max(16, (int)(radius * 9.0));
        Vec3[] p = new Vec3[n + 1];
        for (int i = 0; i <= n; i++) {
            double a = i * Math.PI * 2.0 / n;
            p[i] = c.add(Math.cos(a) * radius, 0.0, Math.sin(a) * radius);
        }
        return p;
    }

    private void mote(Vec3 pos, Vec3 vel, float size, int life, int rgb, float gravity) {
        this.sprite(this.tint(TheabyssModParticleTypes.SP_FX_CROSS_MOTE, rgb, Math.max(0.07F, size), life).gravity(gravity), pos, vel);
    }

    private void burstMotes(Vec3 c, int count, double spread, double speed, float size, int life, int rgb, float gravity) {
        for (int i = 0; i < count; i++) {
            Vec3 dir = this.spread(1.0);
            dir = dir.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 1.0, 0.0) : dir.normalize();
            this.mote(c.add(dir.scale(spread * this.random.nextDouble())), dir.scale(speed * (0.4 + this.random.nextDouble() * 0.6)), size * (0.7F + this.random.nextFloat() * 0.6F), life + this.random.nextInt(8), rgb, gravity);
        }
    }

    private void puffRing(Vec3 c, int count, double radius, int rgb, float size) {
        for (int i = 0; i < count; i++) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = radius * Math.sqrt(this.random.nextDouble());
            int tone = i % 4 == 3 ? shade(rgb, 0.45F) : rgb;
            this.sprite(this.tint(TheabyssModParticleTypes.SP_FX_PIXEL_PUFF, tone, size * (0.7F + this.random.nextFloat() * 0.7F), 30 + this.random.nextInt(16)), c.add(Math.cos(a) * r, 0.2 + this.random.nextDouble() * 1.2, Math.sin(a) * r), new Vec3(Math.cos(a) * 0.03, 0.03 + this.random.nextDouble() * 0.05, Math.sin(a) * 0.03));
        }
    }

    private void sparks(Vec3 c, int count, int rgb, double speed) {
        for (int i = 0; i < count; i++) {
            Vec3 v = this.spread(1.0);
            this.sprite(this.tint(TheabyssModParticleTypes.SP_FX_RECT_SPARK, rgb, 0.1F + this.random.nextFloat() * 0.06F, 12 + this.random.nextInt(8)).gravity(0.012F), c, v.scale(speed));
        }
    }

    private static int shade(int rgb, float k) {
        return (int)(((rgb >> 16) & 255) * k) << 16 | (int)(((rgb >> 8) & 255) * k) << 8 | (int)((rgb & 255) * k);
    }

    private FxOptions loop(Supplier<? extends ParticleType<FxOptions>> holder, float size, int life) {
        return this.tint(holder, 0xFFFFFF, size, life);
    }

    private FxOptions tint(Supplier<? extends ParticleType<FxOptions>> holder, int rgb, float size, int life) {
        return ((FxOptions.Type)holder.get()).of(rgb, size, life, 0);
    }

    private void sprite(FxOptions options, Vec3 p, Vec3 v) {
        if (this.level != null && p.distanceToSqr(this.camera()) > Math.max(1.2, options.size() * options.size() * 9.0)) {
            emit(this.level, options, p, v);
        }
    }

    private Vec3 camera() {
        if (this.lastCamera != null) {
            return this.lastCamera;
        }
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null ? Vec3.ZERO : mc.player.getEyePosition();
    }

    private Vec3 spread(double r) {
        return new Vec3((this.random.nextDouble() - 0.5) * 2.0 * r, (this.random.nextDouble() - 0.5) * 2.0 * r, (this.random.nextDouble() - 0.5) * 2.0 * r);
    }

    private int firstId() {
        return this.ids.isEmpty() ? -1 : this.ids.get(0);
    }

    private Entity entity(int index) {
        if (this.level == null && Minecraft.getInstance().level != null) {
            this.level = Minecraft.getInstance().level;
        }
        return index < this.ids.size() && this.level != null ? this.level.getEntity(this.ids.get(index)) : null;
    }

    private Vec3 centre() {
        Entity e = this.entity(0);
        return e != null ? e.position() : this.at;
    }

    private Vec3 centre(Frame f) {
        Vec3 p = this.firstId() >= 0 ? f.pos(this.firstId()) : null;
        return p != null ? p : this.at;
    }

    private void eachTarget(java.util.function.Consumer<Vec3> action) {
        for (int i = 1; i < this.ids.size(); i++) {
            Entity e = this.entity(i);
            if (e != null) {
                action.accept(e.position().add(0.0, e.getBbHeight() * 0.5, 0.0));
            }
        }
    }

    private void eachTargetTop(java.util.function.Consumer<Vec3> action) {
        for (int i = 1; i < this.ids.size(); i++) {
            Entity e = this.entity(i);
            if (e != null) {
                action.accept(e.position().add(0.0, e.getBbHeight() + 0.4, 0.0));
            }
        }
    }

    private void remember(Vec3 p) {
        this.history.add(p);
        if (this.history.size() > 14) {
            this.history.remove(0);
        }
    }

    private static final class Streak {
        Vec3 base;
        int rgb;
        float height;
        int life;
        int age;
    }
}
