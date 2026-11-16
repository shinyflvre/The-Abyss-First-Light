package net.yezon.theabyss.ring;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModParticleTypes;
import net.yezon.theabyss.network.NightbladeShakeMessage;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.ring.fx.FxOptions;
import net.yezon.theabyss.ring.fx.RingAccent;
import net.yezon.theabyss.ring.fx.RingFx;
import net.yezon.theabyss.ring.fx.SpellCircle;

public final class RingVisuals {
    public static final int REG_GREEN = 0x7CFF6A;
    public static final int REG_GOLD = 0xFFE45C;
    public static final int INVIS = 0x8FE8FF;
    public static final int OCEAN = 0x8FEAFF;
    public static final int OCEAN_DEEP = 0x3FA9FF;
    public static final int SPEED = 0xFF4FD8;
    public static final int JUGGER = 0x9AAEC4;
    public static final int EAGLE = 0xD8FF6A;
    public static final int ICE = 0xB5F7FF;
    public static final int TELEKINETIC = 0xB45CFF;
    public static final int FIRE = 0xFF5A3C;
    public static final int EMBER = 0xFFB13B;
    public static final int THUNDER = 0x6FF3FF;
    public static final int ELECTRO = 0x5FF2E8;
    public static final int CURSE = 0xFF3DD8;
    public static final int TELEPORT = 0xC890FF;
    public static final int TELEPORT_TIP = 0x7FE8FF;
    public static final int NIGHTBLADE = 0x4FF0C8;
    public static final int POCKET = 0xFF9A3C;
    public static final int LILAC = 0xB49CFF;
    public static final int NATURE = 0x7CE86A;
    public static final int NATURE_FLOWER = 0xC9A0FF;
    public static final int BLACKSTRIKE = 0xE040FF;
    public static final int DARK_SMOKE = 0x2A1630;
    public static final int ABYSS = 0x5AD7FF;

    private RingVisuals() {
    }

    public static FxOptions flame(int color, float size, int life) {
        return TheabyssModParticleTypes.FX_FLAME.get().of(color, size, life, FxOptions.ANIMATE);
    }

    public static FxOptions wisp(int color, float size, int life) {
        return TheabyssModParticleTypes.FX_WISP.get().of(color, size, life, FxOptions.ANIMATE);
    }

    public static FxOptions sparkle(int color, float size, int life) {
        return TheabyssModParticleTypes.FX_SPARKLE.get().of(color, size, life, FxOptions.FLICKER);
    }

    public static FxOptions zap(int color, float size, int life) {
        return TheabyssModParticleTypes.FX_ZAP.get().of(color, size, life, FxOptions.FLICKER | FxOptions.HOT);
    }

    public static FxOptions orb(int color, float size, int life) {
        return TheabyssModParticleTypes.FX_ORB.get().of(color, size, life, FxOptions.SHRINK);
    }

    public static FxOptions smoke(int color, float size, int life) {
        return TheabyssModParticleTypes.FX_SMOKE.get().of(color, size, life, FxOptions.SMOKE | FxOptions.GROW);
    }

    public static Vec3 body(Entity e) {
        return e.position().add(0.0, e.getBbHeight() * 0.5, 0.0);
    }

    public static Vec3 aroundBody(Entity e, RandomSource random, double spread) {
        return e.position().add((random.nextDouble() - 0.5) * e.getBbWidth() * spread, random.nextDouble() * e.getBbHeight(),
            (random.nextDouble() - 0.5) * e.getBbWidth() * spread);
    }

    public static float castPitch(RandomSource random) {
        return 0.85F + random.nextFloat() * 0.15F;
    }

    public static void sound(ServerLevel level, Vec3 at, net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        level.playSound(null, at.x, at.y, at.z, sound, SoundSource.PLAYERS, volume, pitch);
    }

    public static Vec3 hand(Entity e) {
        Vec3 look = e.getLookAngle();
        Vec3 side = new Vec3(-look.z, 0.0, look.x).normalize().scale(0.35);
        return e.position().add(0.0, e.getBbHeight() * 0.65, 0.0).add(look.scale(0.4)).add(side);
    }

    public static void regeneration(ServerLevel level, LivingEntity caster) {
        Vec3 feet = caster.position();
        RingAccent.send(level, RingAccent.REGENERATION, feet, caster);
        SpellCircle.on(level, SpellCircle.DISC, caster, REG_GREEN, 2.2F, 60);
        SpellCircle.on(level, SpellCircle.RUNES, caster, REG_GOLD, 1.3F, 40);
        RingFx.spiral(level, sparkle(REG_GREEN, 0.16F, 26).gravity(-0.002F), feet, 0.85, 2.3, 2.0, 24, 0.03);
        RingFx.spiral(level, sparkle(REG_GOLD, 0.13F, 22).gravity(-0.002F), feet.add(0.0, 0.2, 0.0), 0.6, 2.0, -1.5, 16, 0.03);
        RingFx.burst(level, orb(REG_GOLD, 0.12F, 24).gravity(-0.004F), feet.add(0.0, caster.getBbHeight() + 0.3, 0.0), 8, 0.35, 0.02);
        FxOptions mote = sparkle(REG_GOLD, 0.13F, 22).gravity(-0.003F);
        FxOptions leaf = sparkle(REG_GREEN, 0.12F, 22).gravity(-0.003F);
        RingAura.start(caster, "regeneration", 100, (lvl, e, age) -> {
            if (age % 4 == 0) {
                RingFx.emit(lvl, age % 8 == 0 ? mote : leaf, aroundBody(e, e.getRandom(), 1.2), new Vec3(0.0, 0.03, 0.0));
            }
            return true;
        });
    }

    public static void potionCircle(ServerLevel level, LivingEntity drinker, int colour) {
        int light = lighten(colour, 0.45F);
        Vec3 feet = drinker.position();
        SpellCircle.on(level, SpellCircle.DISC, drinker, colour, 1.4F, 26);
        RingFx.spiral(level, sparkle(colour, 0.15F, 24).gravity(-0.002F), feet, 0.8, 2.1, 1.75, 20, 0.03);
        RingFx.spiral(level, sparkle(light, 0.12F, 20).gravity(-0.002F), feet.add(0.0, 0.2, 0.0), 0.55, 1.8, -1.25, 12, 0.03);
        RingFx.burst(level, orb(light, 0.1F, 22).gravity(-0.004F), feet.add(0.0, drinker.getBbHeight() * 0.6, 0.0), 6, 0.3, 0.02);
    }

    public static int lighten(int colour, float amount) {
        int r = (colour >> 16) & 255;
        int g = (colour >> 8) & 255;
        int b = colour & 255;
        r += (int)((255 - r) * amount);
        g += (int)((255 - g) * amount);
        b += (int)((255 - b) * amount);
        return r << 16 | g << 8 | b;
    }

    public static void invisibility(ServerLevel level, LivingEntity caster) {
        Vec3 centre = body(caster);
        RingAccent.send(level, RingAccent.INVISIBILITY, centre, caster);
        SpellCircle.on(level, SpellCircle.RIPPLE, caster, INVIS, 1.8F, 22);
        RingFx.burst(level, smoke(0xDDF6FF, 0.5F, 26), centre, 6, 0.3, 0.01);
        RingFx.burst(level, sparkle(INVIS, 0.14F, 26).gravity(0.012F).flags(FxOptions.FLICKER | FxOptions.BOUNCE), centre, 18, 0.35, 0.08);
        sound(level, centre, SoundEvents.GLASS_BREAK, 0.7F, 1.5F);
    }

    public static void ocean(ServerLevel level, LivingEntity caster) {
        Vec3 feet = caster.position();
        RingAccent.send(level, RingAccent.OCEAN, feet, caster);
        SpellCircle.at(level, SpellCircle.RIPPLE, feet, OCEAN, 2.6F, 30);
        RingFx.ring(level, ParticleTypes.SPLASH, feet.add(0.0, 0.2, 0.0), 1.0, 28, 0.25, 0.25);
        FxOptions droplet = orb(OCEAN, 0.11F, 34).gravity(0.035F).flags(FxOptions.SHRINK | FxOptions.BOUNCE);
        RandomSource random = caster.getRandom();
        for (int i = 0; i < 26; i++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double speed = 0.15 + random.nextDouble() * 0.2;
            RingFx.emit(level, droplet, body(caster), new Vec3(Math.cos(a) * speed, 0.3 + random.nextDouble() * 0.25, Math.sin(a) * speed));
        }
        level.sendParticles(ParticleTypes.BUBBLE_POP, feet.x, feet.y + 1.0, feet.z, 24, 0.5, 0.6, 0.5, 0.02);
        sound(level, feet, SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 0.6F, 1.2F);
        RingAura.start(caster, "ocean", 600, (lvl, e, age) -> {
            if (age % 9 == 0) {
                Vec3 at = aroundBody(e, e.getRandom(), 1.1);
                lvl.sendParticles(ParticleTypes.BUBBLE_POP, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
                RingFx.emit(lvl, droplet, at, new Vec3(0.0, 0.05, 0.0));
            }
            return true;
        });
    }

    public static void speed(ServerLevel level, LivingEntity caster) {
        Vec3 feet = caster.position();
        RingAccent.send(level, RingAccent.SPEED, feet, caster);
        SpellCircle.on(level, SpellCircle.SLASH, caster, SPEED, 2.0F, 14);
        RingFx.burst(level, zap(SPEED, 0.3F, 8), feet.add(0.0, 0.4, 0.0), 14, 0.3, 0.12);
        Vec3 back = caster.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale(-0.3);
        for (int i = 0; i < 8; i++) {
            level.sendParticles(ParticleTypes.CLOUD, feet.x, feet.y + 0.1, feet.z, 0, back.x, 0.02, back.z, 1.0);
        }
        FxOptions spark = zap(SPEED, 0.22F, 5);
        FxOptions streak = wisp(SPEED, 0.2F, 10).drag(0.8F);
        RingAura.start(caster, "speed", 400, (lvl, e, age) -> {
            Vec3 motion = e.getDeltaMovement();
            if (motion.horizontalDistanceSqr() > 0.01) {
                RandomSource random = e.getRandom();
                RingFx.emit(lvl, spark, e.position().add((random.nextDouble() - 0.5) * 0.6, random.nextDouble() * 0.9, (random.nextDouble() - 0.5) * 0.6), Vec3.ZERO);
                if (age % 3 == 0) {
                    RingFx.emit(lvl, streak, e.position().add(0.0, 0.4 + random.nextDouble() * 0.8, 0.0), motion.multiply(-0.5, 0.0, -0.5));
                }
            }
            return true;
        });
    }

    public static void jugger(ServerLevel level, LivingEntity caster) {
        Vec3 feet = caster.position();
        RingAccent.send(level, RingAccent.JUGGER, feet, caster);
        NightbladeShakeMessage.around(level, feet, 14.0, 1.4F, 0.45F);
        BlockState ground = level.getBlockState(caster.blockPosition().below());
        if (!ground.isAir()) {
            RingFx.ring(level, new BlockParticleOption(ParticleTypes.BLOCK, ground), feet.add(0.0, 0.1, 0.0), 0.8, 32, 0.3, 0.25);
        }
        SpellCircle.at(level, SpellCircle.CRACK, feet, JUGGER, 2.6F, 40);
        RingFx.dome(level, body(caster), 1.8F, 30, JUGGER);
        sound(level, feet, SoundEvents.ANVIL_LAND, 0.6F, 0.5F);
        FxOptions stone = orb(JUGGER, 0.14F, 12);
        RingAura.start(caster, "jugger", 2000, (lvl, e, age) -> {
            if (age % 5 == 0) {
                for (int k = 0; k < 2; k++) {
                    double a = age * 0.12 + k * Math.PI;
                    RingFx.emit(lvl, stone, body(e).add(Math.cos(a) * 0.9, Math.sin(age * 0.05 + k) * 0.5, Math.sin(a) * 0.9), Vec3.ZERO);
                }
            }
            return true;
        });
    }

    public static void eagle(ServerLevel level, Vec3 origin, LivingEntity caster, List<? extends Entity> revealed) {
        int ticks = 50;
        RingAccent.send(level, RingAccent.EAGLE, caster.position(), caster, revealed);
        RingFx.wave(level, origin.add(0.0, 1.0, 0.0), 50.0F, ticks, EAGLE);
        SpellCircle.on(level, SpellCircle.TECH, caster, EAGLE, 2.2F, 40);
        RingFx.spiral(level, wisp(EAGLE, 0.2F, 22).gravity(-0.004F), caster.position(), 0.7, 2.2, 1.5, 18, 0.05);
        FxOptions mark = sparkle(EAGLE, 0.2F, 30).gravity(-0.002F);
        for (Entity target : revealed) {
            int delay = (int)Math.max(1, Math.sqrt(target.distanceToSqr(origin)) / 50.0 * ticks);
            TheabyssMod.queueServerWork(delay, () -> {
                if (target.isAlive() && target.level() == level) {
                    Vec3 top = target.position().add(0.0, target.getBbHeight() + 0.4, 0.0);
                    RingFx.burst(level, mark, top, 5, 0.12, 0.02);
                    level.sendParticles(ParticleTypes.END_ROD, top.x, top.y, top.z, 1, 0.0, 0.0, 0.0, 0.0);
                }
            });
        }
    }

    public static void freezeBurst(ServerLevel level, Vec3 at) {
        SpellFxMessage.around(level, at, SpellFxMessage.FREEZE_BURST, List.of(), SpellFxMessage.points(at), 0);
    }

    public static void freeze(ServerLevel level, Vec3 origin, LivingEntity caster, List<? extends Entity> victims) {
        RingAccent.send(level, RingAccent.FREEZE, caster.position(), caster, victims);
        freezeBurst(level, caster.position());
        RingFx.wave(level, origin.add(0.0, 0.5, 0.0), 50.0F, 50, ICE);
        level.sendParticles(ParticleTypes.SNOWFLAKE, caster.getX(), caster.getY() + 1.0, caster.getZ(), 40, 0.8, 0.8, 0.8, 0.06);
        List<Entity> nearestFirst = new ArrayList<>(victims);
        nearestFirst.sort(java.util.Comparator.comparingDouble(e -> e.distanceToSqr(origin)));
        int crystals = 0;
        for (Entity victim : nearestFirst) {
            int delay = (int)Math.max(1, Math.sqrt(victim.distanceToSqr(origin)) / 50.0 * 50);
            boolean withCrystals = crystals++ < 64 && caster != null;
            TheabyssMod.queueServerWork(delay, () -> {
                if (!victim.isAlive() || victim.level() != level) {
                    return;
                }
                freezeBurst(level, victim.position());
                level.sendParticles(ParticleTypes.SNOWFLAKE, victim.getX(), victim.getY() + victim.getBbHeight() * 0.6, victim.getZ(), 14, 0.3, 0.4, 0.3, 0.03);
                level.sendParticles(ParticleTypes.ITEM_SNOWBALL, victim.getX(), victim.getY() + 0.3, victim.getZ(), 8, 0.3, 0.1, 0.3, 0.05);
                if (withCrystals) {
                    double r = victim.getBbWidth() * 0.5 + 0.35;
                    for (int k = 0; k < 3; k++) {
                        double a = k * Math.PI * 2.0 / 3.0 + victim.getRandom().nextDouble();
                        var crystal = TheabyssModEntities.NIGHTBLADE_SPIKE.get().create(level, EntitySpawnReason.TRIGGERED);
                        if (crystal != null) {
                            crystal.setPos(victim.getX() + Math.cos(a) * r, victim.getY(), victim.getZ() + Math.sin(a) * r);
                            crystal.setupFrost(caster, 0, Math.max(20, 140 - delay), (float)Math.toDegrees(a));
                            level.addFreshEntity(crystal);
                        }
                    }
                }
            });
        }
    }

    public static void telekinetic(ServerLevel level, Vec3 origin, LivingEntity caster, List<? extends LivingEntity> lifted, int levitation) {
        RingAccent.send(level, RingAccent.TELEKINETIC, caster.position(), caster, lifted);
        SpellCircle.on(level, SpellCircle.RUNES, caster, TELEKINETIC, 2.2F, 30);
        sound(level, caster.position(), SoundEvents.CHAIN_PLACE, 0.8F, 0.6F);
        FxOptions chain = sparkle(TELEKINETIC, 0.12F, 14);
        FxOptions trickle = wisp(TELEKINETIC, 0.16F, 20).gravity(0.004F);
        for (LivingEntity target : lifted) {
            int delay = (int)Math.max(1, Math.sqrt(target.distanceToSqr(origin)) / 50.0 * 20);
            TheabyssMod.queueServerWork(delay, () -> {
                if (target.isAlive() && target.level() == level) {
                    RingFx.line(level, chain, hand(caster), body(target), 0.35);
                    SpellCircle.on(level, SpellCircle.RIPPLE, target, TELEKINETIC, target.getBbWidth() + 0.7F, 20);
                }
            });
            RingAura.start(target, "telekinetic", levitation, (lvl, e, age) -> {
                if (age % 4 == 0) {
                    RingFx.emit(lvl, trickle, e.position().add((e.getRandom().nextDouble() - 0.5) * e.getBbWidth(), 0.0,
                        (e.getRandom().nextDouble() - 0.5) * e.getBbWidth()), new Vec3(0.0, -0.02, 0.0));
                }
                return true;
            });
        }
    }

    public static void fire(ServerLevel level, Vec3 origin, LivingEntity caster, List<? extends Entity> burning) {
        Vec3 feet = origin.add(0.0, 0.15, 0.0);
        RingAccent.send(level, RingAccent.FIRE, origin, caster, burning);
        SpellCircle.at(level, SpellCircle.CRATER, origin, FIRE, 3.2F, 26);
        FxOptions big = flame(FIRE, 0.42F, 18).drag(0.93F);
        FxOptions small = flame(EMBER, 0.26F, 14).drag(0.93F);
        for (int i = 0; i < 14; i++) {
            double a = i * Math.PI * 2.0 / 14.0;
            Vec3 out = new Vec3(Math.cos(a), 0.0, Math.sin(a));
            RingFx.emit(level, big, feet, out.scale(0.55));
            RingFx.emit(level, small, feet, out.scale(0.4).add(0.0, 0.03, 0.0));
            RingFx.emit(level, small, feet, out.scale(0.7));
        }
        FxOptions flare = flame(FIRE, 0.32F, 16).gravity(-0.01F);
        for (Entity target : burning) {
            int delay = (int)Math.max(1, Math.sqrt(target.distanceToSqr(origin)) / 20.0 * 18);
            TheabyssMod.queueServerWork(delay, () -> {
                if (target.isAlive() && target.level() == level) {
                    RingFx.burst(level, flare, body(target), 9, 0.3, 0.04);
                    level.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY() + 0.5, target.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
                }
            });
        }
    }

    public static void thunderStrike(ServerLevel level, Vec3 at) {
        RingAccent.send(level, RingAccent.THUNDER, at);
        RingFx.bolt(level, at, 12, THUNDER);
        SpellCircle.at(level, SpellCircle.CRATER, at, THUNDER, 2.2F, 18);
        RingFx.burst(level, zap(THUNDER, 0.34F, 8), at.add(0.0, 0.5, 0.0), 10, 0.4, 0.1);
        RingFx.burst(level, sparkle(THUNDER, 0.12F, 30).gravity(0.05F).flags(FxOptions.FLICKER | FxOptions.HOT | FxOptions.BOUNCE),
            at.add(0.0, 0.3, 0.0), 16, 0.2, 0.3);
        NightbladeShakeMessage.around(level, at, 16.0, 0.8F, 0.25F);
    }

    public static void electro(ServerLevel level, Vec3 origin, LivingEntity caster, List<? extends Entity> charged) {
        RingAccent.send(level, RingAccent.ELECTRO, caster.position(), caster, charged);
        SpellCircle.on(level, SpellCircle.ARCS, caster, ELECTRO, 2.2F, 22);
        List<Entity> left = new ArrayList<>(charged);
        Vec3 from = body(caster);
        int step = 0;
        while (!left.isEmpty() && step < 30) {
            Vec3 a = from;
            Entity next = left.get(0);
            for (Entity e : left) {
                if (body(e).distanceToSqr(a) < body(next).distanceToSqr(a)) {
                    next = e;
                }
            }
            left.remove(next);
            Entity target = next;
            TheabyssMod.queueServerWork(1 + step * 2, () -> {
                if (target.isAlive() && target.level() == level) {
                    RingFx.arc(level, zap(ELECTRO, 0.2F, 6), a, body(target), Math.min(1.4, a.distanceTo(body(target)) * 0.12), target.getRandom());
                    RingFx.burst(level, sparkle(ELECTRO, 0.13F, 16).flags(FxOptions.FLICKER | FxOptions.HOT), body(target), 8, 0.3, 0.08);
                }
            });
            from = body(next);
            step++;
        }
    }

    public static void electricityTick(ServerLevel level, Entity victim) {
    }

    public static void curse(ServerLevel level, Vec3 origin, LivingEntity caster) {
        Vec3 feet = caster.position();
        RingAccent.send(level, RingAccent.CURSE, feet, caster);
        SpellCircle.on(level, SpellCircle.ORNATE, caster, CURSE, 2.8F, 50);
        RingFx.vortex(level, origin, 9.0F, 60, CURSE, 5);
        FxOptions seal = wisp(CURSE, 0.3F, 26).gravity(-0.004F);
        for (int i = 0; i < 20; i++) {
            double a = i * Math.PI * 2.0 / 20.0;
            RingFx.emit(level, seal, feet.add(Math.cos(a) * 4.1, 0.1, Math.sin(a) * 4.1), new Vec3(0.0, 0.02, 0.0));
        }
        level.sendParticles(ParticleTypes.WITCH, feet.x, feet.y + 1.0, feet.z, 24, 0.6, 0.6, 0.6, 0.05);
    }

    public static void cursedAuraTick(ServerLevel level, Entity caster) {
        if (caster.tickCount % 3 != 0) {
            return;
        }
        double a = caster.tickCount * 0.21;
        RingFx.emit(level, wisp(CURSE, 0.22F, 18).gravity(-0.003F), caster.position().add(Math.cos(a) * 1.1, 0.2 + (caster.tickCount % 18) * 0.1, Math.sin(a) * 1.1),
            Vec3.ZERO);
    }

    public static void cursedTick(ServerLevel level, Entity victim) {
        if (victim.tickCount % 24 % 5 == 0 && victim.tickCount % 24 < 21) {
            RingFx.emit(level, wisp(CURSE, 0.2F, 18).gravity(-0.004F), aroundBody(victim, victim.getRandom(), 1.0), Vec3.ZERO);
        }
        cursedAura(level, victim);
    }

    public static void possessionEnter(ServerLevel level, Entity player, Entity mob) {
        RingFx.line(level, wisp(CURSE, 0.2F, 14).gravity(-0.002F), body(player), body(mob), 0.4);
        SpellCircle.on(level, SpellCircle.ORNATE, mob, CURSE_AURA_VIOLET, mob.getBbWidth() * 0.5F + 1.4F, 26);
        Vec3 centre = body(mob);
        RandomSource random = mob.getRandom();
        for (int i = 0; i < 16; i++) {
            double a = i * Math.PI * 2.0 / 16.0;
            double r = mob.getBbWidth() * 0.5 + 1.2;
            Vec3 at = centre.add(Math.cos(a) * r, (random.nextDouble() - 0.5) * mob.getBbHeight(), Math.sin(a) * r);
            RingFx.emit(level, smoke(i % 2 == 0 ? CURSE_AURA_BLACK : CURSE_AURA_VIOLET, 0.42F, 22).drag(0.86F), at, centre.subtract(at).scale(0.09));
        }
        RingFx.burst(level, wisp(CURSE, 0.24F, 18).gravity(-0.004F), centre, 10, 0.3, 0.03);
    }

    public static void possessionRelease(ServerLevel level, Entity mob) {
        Vec3 centre = body(mob);
        RingFx.burst(level, smoke(CURSE_AURA_BLACK, 0.45F, 26).drag(0.88F), centre, 10, 0.3, 0.08);
        RingFx.burst(level, smoke(CURSE_AURA_VIOLET, 0.38F, 22).drag(0.88F), centre, 8, 0.3, 0.06);
        RingFx.burst(level, wisp(CURSE, 0.2F, 16).gravity(-0.004F), centre, 8, 0.3, 0.04);
    }

    public static final int CURSE_AURA_BLACK = 0x150A1E;
    public static final int CURSE_AURA_VIOLET = 0x4A1670;
    public static final int CURSE_AURA_SEAL = 0x7A2BB0;

    static void cursedAura(ServerLevel level, Entity victim) {
        int t = victim.tickCount;
        RandomSource random = victim.getRandom();
        double r = victim.getBbWidth() * 0.5 + 0.35;
        if (t % 3 == 0) {
            double a = t * 0.18 + random.nextDouble() * 0.6;
            Vec3 at = victim.position().add(Math.cos(a) * r, random.nextDouble() * victim.getBbHeight() * 0.9, Math.sin(a) * r);
            Vec3 swirl = new Vec3(-Math.sin(a), 0.4, Math.cos(a)).scale(0.025);
            int colour = t % 6 == 0 ? CURSE_AURA_BLACK : CURSE_AURA_VIOLET;
            RingFx.emit(level, smoke(colour, 0.38F, 30).gravity(-0.002F).drag(0.92F), at, swirl);
        }
        if (t % 20 == 0) {
            SpellCircle.on(level, SpellCircle.ORNATE, victim, CURSE_AURA_VIOLET, victim.getBbWidth() * 0.5F + 0.8F, 22);
        }
        if (t % 9 == 0) {
            RingFx.emit(level, sparkle(CURSE_AURA_BLACK, 0.09F, 24).gravity(0.004F).flags(FxOptions.FLICKER), aroundBody(victim, random, 1.3), Vec3.ZERO);
        }
    }

    public static void firestorm(ServerLevel level, LivingEntity caster) {
        Vec3 c = caster.position();
        RingAccent.send(level, RingAccent.FIRESTORM, c, caster);
        RingFx.vortex(level, c, 20.0F, 200, FIRE, 4);
        SpellCircle.on(level, SpellCircle.RUNES, caster, FIRE, 3.5F, 70);
        SpellCircle.at(level, SpellCircle.CRATER, c, EMBER, 3.0F, 30);
        RingFx.ring(level, flame(FIRE, 0.5F, 22).drag(0.95F), c.add(0.0, 0.2, 0.0), 1.0, 40, 0.9, 0.02);
        RingFx.ring(level, flame(EMBER, 0.35F, 18).drag(0.95F), c.add(0.0, 0.5, 0.0), 0.8, 30, 0.7, 0.05);
        level.sendParticles(ParticleTypes.LAVA, c.x, c.y + 0.5, c.z, 14, 1.5, 0.3, 1.5, 0.0);
        NightbladeShakeMessage.around(level, c, 20.0, 1.0F, 0.4F);
    }

    public static void firestormTick(ServerLevel level, Entity caster) {
        RandomSource random = caster.getRandom();
        int t = caster.tickCount;
        Vec3 c = caster.position();
        FxOptions tornado = flame(FIRE, 0.45F, 18).gravity(-0.025F).drag(0.9F);
        FxOptions tornadoEmber = flame(EMBER, 0.3F, 16).gravity(-0.03F).drag(0.9F);
        for (int k = 0; k < 9; k++) {
            double h = random.nextDouble() * 6.0;
            double a = t * 0.5 + (k % 3) * Math.PI * 2.0 / 3.0 + h * 0.7;
            double r = 1.2 + h * 0.55;
            Vec3 at = c.add(Math.cos(a) * r, h, Math.sin(a) * r);
            Vec3 swirl = new Vec3(-Math.sin(a), 0.15, Math.cos(a)).scale(0.35);
            RingFx.emit(level, k % 2 == 0 ? tornado : tornadoEmber, at, swirl);
        }
        double wall = 3.0 + (t * 0.5) % 17.0;
        int wallFlames = (int)(wall * 1.3);
        FxOptions wallFlame = flame(FIRE, 0.55F, 14).gravity(-0.02F);
        FxOptions wallEmber = flame(EMBER, 0.38F, 12).gravity(-0.025F);
        for (int k = 0; k < wallFlames; k++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            Vec3 at = ground(level, c.add(Math.cos(a) * wall, 0.0, Math.sin(a) * wall));
            RingFx.emit(level, k % 3 == 0 ? wallEmber : wallFlame, at.add(0.0, 0.1, 0.0), new Vec3(Math.cos(a) * 0.08, 0.05, Math.sin(a) * 0.08));
        }
        if (t % 2 == 0) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double r = 3.0 + Math.sqrt(random.nextDouble()) * 17.0;
            Vec3 at = ground(level, c.add(Math.cos(a) * r, 0.0, Math.sin(a) * r));
            FxOptions column = flame(FIRE, 0.5F, 16).gravity(-0.03F).drag(0.88F);
            for (int k = 0; k < 6; k++) {
                RingFx.emit(level, k % 2 == 0 ? column : flame(EMBER, 0.36F, 14).gravity(-0.03F), at.add((random.nextDouble() - 0.5) * 0.6, k * 0.25,
                    (random.nextDouble() - 0.5) * 0.6), new Vec3(0.0, 0.18 + random.nextDouble() * 0.12, 0.0));
            }
            level.sendParticles(ParticleTypes.LAVA, at.x, at.y + 0.3, at.z, 1, 0.1, 0.0, 0.1, 0.0);
            RingFx.emit(level, smoke(0x3A2A24, 0.9F, 40).gravity(-0.01F), at.add(0.0, 1.8, 0.0), new Vec3(0.0, 0.04, 0.0));
        }
        FxOptions rain = sparkle(EMBER, 0.12F, 40).gravity(0.03F).flags(FxOptions.FLICKER | FxOptions.HOT | FxOptions.BOUNCE);
        for (int k = 0; k < 3; k++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double r = Math.sqrt(random.nextDouble()) * 18.0;
            RingFx.emit(level, rain, c.add(Math.cos(a) * r, 6.0 + random.nextDouble() * 3.0, Math.sin(a) * r), new Vec3(0.0, -0.1, 0.0));
        }
        if (t % 3 == 0) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double r = random.nextDouble() * 8.0;
            RingFx.emit(level, smoke(0x2E2220, 1.4F, 60).gravity(-0.006F), c.add(Math.cos(a) * r, 5.0 + random.nextDouble() * 2.0, Math.sin(a) * r),
                new Vec3(Math.cos(a) * 0.03, 0.03, Math.sin(a) * 0.03));
        }
        if (t % 4 == 0) {
            FxOptions burn = flame(FIRE, 0.34F, 14).gravity(-0.02F);
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(20.0), e -> e != caster && e.isOnFire())) {
                RingFx.burst(level, burn, body(victim), 3, 0.25, 0.03);
            }
        }
        if (t % 12 == 0) {
            level.playSound(null, c.x, c.y, c.z, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.6F, 0.6F + random.nextFloat() * 0.3F);
        }
    }

    public static Vec3 ground(ServerLevel level, Vec3 at) {
        net.minecraft.core.BlockPos.MutableBlockPos pos = new net.minecraft.core.BlockPos.MutableBlockPos();
        int x = net.minecraft.util.Mth.floor(at.x);
        int z = net.minecraft.util.Mth.floor(at.z);
        int y0 = net.minecraft.util.Mth.floor(at.y);
        for (int dy = 4; dy >= -6; dy--) {
            pos.set(x, y0 + dy, z);
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) {
                return new Vec3(at.x, y0 + dy + 1.0, at.z);
            }
        }
        return at;
    }

    public static void teleportTrail(ServerLevel level, Vec3 from, Vec3 to) {
        RingFx.line(level, sparkle(TELEPORT, 0.14F, 12), from, to, 0.5);
        RingFx.burst(level, zap(TELEPORT_TIP, 0.2F, 6), to, 2, 0.1, 0.02);
    }

    public static void teleportDepart(ServerLevel level, Vec3 feet, float height) {
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, feet.x, feet.y + height * 0.5, feet.z, 40, 0.3, height * 0.35, 0.3, 0.06);
        RingFx.burst(level, smoke(TELEPORT, 0.45F, 22), feet.add(0.0, height * 0.5, 0.0), 5, 0.25, 0.01);
    }

    public static void teleportArrive(ServerLevel level, Vec3 feet) {
        level.sendParticles(ParticleTypes.PORTAL, feet.x, feet.y + 1.0, feet.z, 50, 0.35, 0.7, 0.35, 0.4);
        SpellCircle.at(level, SpellCircle.RIPPLE, feet, TELEPORT, 1.8F, 18);
        RingFx.burst(level, sparkle(TELEPORT, 0.15F, 20).gravity(-0.003F), feet.add(0.0, 1.0, 0.0), 12, 0.4, 0.04);
    }

    public static final int STEP_SMOKE_DARK = 0x2C2A30;
    public static final int STEP_SMOKE_GREY = 0x77747C;

    public static void teleportStepDepart(ServerLevel level, LivingEntity caster) {
        RingAccent.send(level, RingAccent.STEP_DEPART, caster.position());
        Vec3 forward = caster.getForward().normalize();
        RandomSource random = caster.getRandom();
        FxOptions dark = smoke(STEP_SMOKE_DARK, 0.55F, 34).gravity(-0.002F).drag(0.88F);
        FxOptions grey = smoke(STEP_SMOKE_GREY, 0.4F, 28).gravity(-0.004F).drag(0.88F);
        for (int i = 0; i < 26; i++) {
            Vec3 at = new Vec3(caster.getRandomX(0.45), caster.getRandomY(), caster.getRandomZ(0.45));
            Vec3 motion = forward.scale(0.05 + random.nextDouble() * 0.2).add(0.0, 0.01, 0.0);
            RingFx.emit(level, i % 3 == 0 ? grey : dark, at, motion);
        }
        for (int i = 0; i < 12; i++) {
            Vec3 at = new Vec3(caster.getRandomX(0.4), caster.getRandomY(), caster.getRandomZ(0.4));
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, at.x, at.y, at.z, 0, forward.x * 0.05, 0.03, forward.z * 0.05, 1.0);
        }
        level.sendParticles(ParticleTypes.WHITE_ASH, caster.getX(), caster.getY() + 1.0, caster.getZ(), 20, 0.3, 0.5, 0.3, 0.02);
    }

    public static void teleportStepTrail(ServerLevel level, Vec3 a, Vec3 b) {
        FxOptions streak = smoke(STEP_SMOKE_DARK, 0.32F, 22).gravity(-0.002F).drag(0.85F);
        FxOptions wisp = smoke(STEP_SMOKE_GREY, 0.24F, 18).gravity(-0.003F).drag(0.85F);
        double length = a.distanceTo(b);
        int n = Math.max(2, (int)(length * 2.0));
        RandomSource random = level.getRandom();
        for (int i = 0; i < n; i++) {
            Vec3 p = a.lerp(b, (i + random.nextDouble()) / n).add((random.nextDouble() - 0.5) * 0.4, 0.3 + random.nextDouble() * 1.3,
                (random.nextDouble() - 0.5) * 0.4);
            RingFx.emit(level, i % 2 == 0 ? streak : wisp, p, new Vec3((random.nextDouble() - 0.5) * 0.04, 0.01, (random.nextDouble() - 0.5) * 0.04));
        }
    }

    public static void teleportStepArrive(ServerLevel level, Vec3 feet) {
        RingAccent.send(level, RingAccent.STEP_ARRIVE, feet);
        RingFx.ring(level, smoke(STEP_SMOKE_DARK, 0.45F, 30).drag(0.86F), feet.add(0.0, 0.15, 0.0), 0.4, 16, 0.18, 0.0);
        RingFx.ring(level, smoke(STEP_SMOKE_GREY, 0.35F, 24).drag(0.86F), feet.add(0.0, 0.25, 0.0), 0.3, 10, 0.12, 0.01);
        RingFx.burst(level, smoke(STEP_SMOKE_DARK, 0.4F, 26).gravity(-0.003F), feet.add(0.0, 1.0, 0.0), 8, 0.3, 0.02);
        level.sendParticles(ParticleTypes.WHITE_ASH, feet.x, feet.y + 1.0, feet.z, 12, 0.4, 0.5, 0.4, 0.02);
    }

    public static void nightbladeRift(ServerLevel level, Vec3 centre) {
        RingAccent.send(level, RingAccent.NIGHTBLADE, centre);
        RingFx.implode(level, centre.add(0.0, 1.0, 0.0), 20.0F, 24, NIGHTBLADE);
        SpellCircle.at(level, SpellCircle.SLASH, centre, NIGHTBLADE, 3.0F, 18);
        SpellCircle.at(level, SpellCircle.CRATER, centre, NIGHTBLADE, 2.8F, 26);
        level.sendParticles(TheabyssModParticleTypes.NIGHTBLADE_TRAIL.get(), centre.x, centre.y + 1.0, centre.z, 30, 0.6, 1.0, 0.6, 0.05);
    }

    public static void nightbladeLift(ServerLevel level, Entity target, Vec3 from, int hold) {
        RingFx.line(level, TheabyssModParticleTypes.NIGHTBLADE_TRAIL.get(), from.add(0.0, 0.5, 0.0), target.position().add(0.0, 0.5, 0.0), 0.3);
        RingAura.start(target, "nightblade_lift", hold, (lvl, e, age) -> {
            if (age % 3 == 0) {
                lvl.sendParticles(TheabyssModParticleTypes.NIGHTBLADE_TRAIL.get(), e.getX(), e.getY() - 0.1, e.getZ(), 1, e.getBbWidth() * 0.3, 0.0, e.getBbWidth() * 0.3, 0.0);
            }
            return true;
        });
    }

    public static void pocketPortal(ServerLevel level, Vec3 feet) {
        RingAccent.send(level, RingAccent.POCKET, feet);
        Vec3 c = feet.add(0.0, 1.0, 0.0);
        FxOptions edge = sparkle(POCKET, 0.13F, 16);
        double s = 1.2;
        int[][] corners = {{-1, -1, -1}, {1, -1, -1}, {1, -1, 1}, {-1, -1, 1}, {-1, 1, -1}, {1, 1, -1}, {1, 1, 1}, {-1, 1, 1}};
        int[][] edges = {{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
        for (int[] e : edges) {
            Vec3 a = c.add(corners[e[0]][0] * s, corners[e[0]][1] * s, corners[e[0]][2] * s);
            Vec3 b = c.add(corners[e[1]][0] * s, corners[e[1]][1] * s, corners[e[1]][2] * s);
            for (int i = 0; i <= 5; i++) {
                Vec3 p = a.lerp(b, i / 5.0);
                RingFx.emit(level, edge, p, c.subtract(p).scale(0.06));
            }
        }
        SpellCircle.at(level, SpellCircle.TECH, feet, POCKET, 1.8F, 26);
    }

    public static void slideStart(ServerLevel level, Entity player) {
        RingAccent.send(level, RingAccent.SLIDE, player.position(), player);
        SpellCircle.on(level, SpellCircle.SLASH, player, LILAC, 1.8F, 12);
        RingFx.ring(level, ParticleTypes.CLOUD, body(player), 0.4, 14, 0.3, 0.0);
    }

    public static void slideTick(ServerLevel level, Entity player) {
        Vec3 motion = player.getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-3) {
            return;
        }
        Vec3 back = motion.normalize().scale(-0.2);
        Vec3 side = new Vec3(-motion.z, 0.0, motion.x).normalize().scale(0.5);
        FxOptions feather = wisp(LILAC, 0.22F, 12).drag(0.85F);
        RingFx.emit(level, feather, body(player).add(side), back);
        RingFx.emit(level, feather, body(player).subtract(side), back);
    }

    public static void flightTick(ServerLevel level, Entity player) {
        if (player.tickCount % 6 == 0) {
            RingAccent.send(level, RingAccent.FLIGHT, player.position(), player);
        }
        if (player.tickCount % 16 == 0) {
            SpellCircle.at(level, SpellCircle.HOVER, player.position(), LILAC, 1.0F, 14);
        }
        if (player.tickCount % 4 == 0) {
            RingFx.emit(level, sparkle(LILAC, 0.13F, 24).gravity(0.003F), player.position().add((player.getRandom().nextDouble() - 0.5) * 0.6, -0.05,
                (player.getRandom().nextDouble() - 0.5) * 0.6), Vec3.ZERO);
        }
    }

    public static void nature(ServerLevel level, Vec3 centre, List<Vec3> grown) {
        RingAccent.send(level, RingAccent.NATURE, centre);
        SpellCircle.at(level, SpellCircle.DISC, centre.add(0.5, 1.0, 0.5), NATURE, 2.0F, 30);
        for (Vec3 at : grown) {
            Vec3 top = at.add(0.5, 0.7, 0.5);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, top.x, top.y, top.z, 4, 0.3, 0.3, 0.3, 0.0);
            RingFx.burst(level, sparkle(NATURE, 0.13F, 22).gravity(-0.003F), top, 3, 0.25, 0.02);
            RingFx.burst(level, sparkle(NATURE_FLOWER, 0.12F, 22).gravity(-0.003F), top, 2, 0.25, 0.02);
        }
    }

    public static void fangs(ServerLevel level, Vec3 centre) {
        RingAccent.send(level, RingAccent.FANGS, centre);
        SpellCircle.at(level, SpellCircle.FROST, centre, ICE, 2.8F, 30);
        RingFx.burst(level, zap(ICE, 0.3F, 8), centre.add(0.0, 0.3, 0.0), 10, 0.5, 0.06);
    }

    public static void enderChest(ServerLevel level, Entity player) {
        RingAccent.send(level, RingAccent.ENDER, player.position(), player);
        SpellCircle.on(level, SpellCircle.TECH, player, 0x3FE0B0, 1.2F, 20);
        Vec3 at = body(player).add(player.getLookAngle().scale(0.6));
        level.sendParticles(ParticleTypes.PORTAL, at.x, at.y, at.z, 20, 0.2, 0.3, 0.2, 0.3);
    }

    public static void fireStrikeTrail(ServerLevel level, Vec3 from, Vec3 to) {
        RingAccent.send(level, RingAccent.FIRE_TRAIL, to);
        RingFx.line(level, flame(FIRE, 0.3F, 10), from, to, 0.5);
        RingFx.burst(level, flame(EMBER, 0.2F, 8), to, 1, 0.08, 0.02);
    }

    public static void fireStrikeHit(ServerLevel level, Vec3 at) {
        RingAccent.send(level, RingAccent.FIRE_HIT, at);
        SpellCircle.at(level, SpellCircle.SHOCK, at.add(0.0, -0.5, 0.0), FIRE, 1.4F, 12);
        RingFx.burst(level, flame(FIRE, 0.34F, 14).gravity(-0.01F), at, 10, 0.25, 0.06);
        RingFx.burst(level, sparkle(EMBER, 0.11F, 26).gravity(0.05F).flags(FxOptions.FLICKER | FxOptions.HOT | FxOptions.BOUNCE), at, 12, 0.15, 0.25);
        RingFx.burst(level, smoke(0x555555, 0.5F, 26), at, 4, 0.2, 0.01);
    }

    public static void blackStrikeTrail(ServerLevel level, Vec3 from, Vec3 to) {
        RingAccent.send(level, RingAccent.BLACK_TRAIL, to);
        RingFx.line(level, wisp(BLACKSTRIKE, 0.28F, 12), from, to, 0.5);
        RingFx.burst(level, smoke(DARK_SMOKE, 0.4F, 18), to, 1, 0.08, 0.01);
    }

    public static void blackStrikeFade(ServerLevel level, Vec3 at) {
        RingFx.burst(level, wisp(BLACKSTRIKE, 0.22F, 12).gravity(-0.004F), at, 6, 0.15, 0.03);
        RingFx.burst(level, smoke(DARK_SMOKE, 0.4F, 16), at, 3, 0.12, 0.01);
    }

    public static void blackStrikeHit(ServerLevel level, Vec3 at) {
        RingAccent.send(level, RingAccent.BLACK_HIT, at);
        SpellCircle.at(level, SpellCircle.SHOCK, at.add(0.0, -0.5, 0.0), BLACKSTRIKE, 1.4F, 12);
        RingFx.burst(level, wisp(BLACKSTRIKE, 0.3F, 16).gravity(-0.006F), at, 10, 0.25, 0.05);
        RingFx.burst(level, smoke(DARK_SMOKE, 0.55F, 24), at, 5, 0.2, 0.01);
    }

    public static void ghostCurse(ServerLevel level, LivingEntity ghost, List<? extends Entity> hit) {
        SpellCircle.at(level, SpellCircle.ORNATE, ghost.position(), CURSE, 2.6F, 18);
        for (Entity target : hit) {
            RingFx.arc(level, zap(CURSE, 0.18F, 6), body(ghost), body(target), 0.6, ghost.getRandom());
        }
    }
}
