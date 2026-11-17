package net.yezon.theabyss.ring.secondary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.tick.ServerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingVisuals;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class Puppeteer {
    public static final double RANGE = 25.0;
    public static final int DURATION = 160;
    public static final double HUNT_RANGE = 16.0;
    private static final float RAM = 3.0F;

    private static final class Puppet {
        final ServerLevel level;
        final ServerPlayer caster;
        final Mob mob;
        int age;
        LivingEntity victim;
        long lastRam;

        Puppet(ServerLevel level, ServerPlayer caster, Mob mob) {
            this.level = level;
            this.caster = caster;
            this.mob = mob;
        }
    }

    private static final List<Puppet> PUPPETS = new ArrayList<>();

    private Puppeteer() {
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.serverLevel();
        if (!(Aim.creature(level, player, RANGE) instanceof Mob mob)) {
            player.displayClientMessage(Component.translatable("ring.theabyss.secondary.no_target", Component.translatable("tooltip.theabyss.ring_of_possession.secondary.ability")), true);
            return false;
        }
        PUPPETS.removeIf(p -> p.mob == mob);
        PUPPETS.add(new Puppet(level, player, mob));
        SpellFxMessage.around(level, mob.position(), SpellFxMessage.PUPPET, List.of(player.getId(), mob.getId()), List.of(), DURATION);
        RingVisuals.possessionEnter(level, player, mob);
        RingVisuals.sound(level, mob.position(), SoundEvents.ILLUSIONER_CAST_SPELL, 1.4F, 0.8F);
        RingVisuals.sound(level, mob.position(), SoundEvents.CHAIN_PLACE, 1.0F, 1.6F);
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (Iterator<Puppet> it = PUPPETS.iterator(); it.hasNext(); ) {
            Puppet p = it.next();
            p.age++;
            if (p.age > DURATION || !p.mob.isAlive() || p.mob.level() != p.level) {
                release(p);
                it.remove();
                continue;
            }
            control(p);
        }
    }

    private static void control(Puppet p) {
        Mob mob = p.mob;
        if (p.victim == null || !p.victim.isAlive() || p.victim.level() != p.level || p.age % 20 == 0) {
            LivingEntity next = p.level.getEntitiesOfClass(LivingEntity.class, new AABB(mob.position(), mob.position()).inflate(HUNT_RANGE),
                    e -> e != mob && e != p.caster && e.isAlive() && !(e instanceof Player) && !e.isSpectator())
                .stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(mob))).orElse(null);
            if (next != p.victim) {
                p.victim = next;
                SpellFxMessage.around(p.level, mob.position(), SpellFxMessage.PUPPET_TARGET, List.of(mob.getId(), next == null ? -1 : next.getId()), List.of(), 0);
            }
        }
        LivingEntity victim = p.victim;
        if (victim == null) {
            return;
        }
        mob.setTarget(victim);
        if (mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) || mob.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET,
            net.minecraft.world.entity.ai.memory.MemoryStatus.REGISTERED)) {
            mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, victim);
        }
        if (mob.getAttribute(Attributes.ATTACK_DAMAGE) == null) {
            mob.getNavigation().moveTo(victim, 1.4);
            long now = p.level.getGameTime();
            if (mob.distanceToSqr(victim) < 2.6 && now - p.lastRam >= 20) {
                p.lastRam = now;
                victim.hurt(p.level.damageSources().mobAttack(mob), RAM);
                net.minecraft.world.phys.Vec3 away = victim.position().subtract(mob.position()).multiply(1.0, 0.0, 1.0);
                away = away.lengthSqr() < 1.0E-4 ? net.minecraft.world.phys.Vec3.ZERO : away.normalize().scale(0.5);
                victim.push(away.x, 0.25, away.z);
                RingVisuals.sound(p.level, victim.position(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F, 0.8F);
            }
        }
    }

    private static void release(Puppet p) {
        if (p.mob.isAlive()) {
            p.mob.setTarget(null);
            if (p.mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                p.mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            }
            RingVisuals.possessionRelease(p.level, p.mob);
            RingVisuals.sound(p.level, p.mob.position(), SoundEvents.CHAIN_BREAK, 1.0F, 1.4F);
        }
    }

    public static boolean isPuppet(LivingEntity e) {
        for (Puppet p : PUPPETS) {
            if (p.mob == e) {
                return true;
            }
        }
        return false;
    }
}
