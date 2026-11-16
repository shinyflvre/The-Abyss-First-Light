package net.yezon.theabyss.ring.secondary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.init.TheabyssModSounds;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.RingFx;
import net.yezon.theabyss.util.RingMode;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class HexMark {
    private static final double CASTER_SOUND_DISTANCE = 8.0;
    public static final double RANGE = 30.0;
    public static final int DURATION = 200;
    public static final int JUMP_DURATION = 120;
    public static final float MORE = 1.5F;
    public static final int JUMPS = 3;
    public static final double JUMP_RANGE = 8.0;
    public static final int GENERATIONS = 2;

    private record Mark(ServerLevel level, UUID caster, long until, int generation) {
    }

    private static final Map<Integer, Mark> MARKS = new HashMap<>();

    private HexMark() {
    }

    public static boolean marked(LivingEntity e) {
        Mark mark = MARKS.get(e.getId());
        return mark != null && e.level().getGameTime() <= mark.until();
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.level();
        LivingEntity target = Aim.creature(level, player, RANGE);
        if (target == null) {
            player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("ring.theabyss.secondary.no_target", net.minecraft.network.chat.Component.translatable("tooltip.theabyss.ring_of_curse.secondary.ability")));
            return false;
        }
        Vec3 hand = RingVisuals.hand(player);
        mark(level, player.getUUID(), target, hand, DURATION, 0);
        float pitch = RingVisuals.castPitch(player.getRandom());
        RingVisuals.sound(level, target.position(), TheabyssModSounds.SPELL_OF_HEX_MARK.get(), 1.0F, pitch);
        if (target.distanceToSqr(player) > CASTER_SOUND_DISTANCE * CASTER_SOUND_DISTANCE) {
            RingVisuals.sound(level, player.position(), TheabyssModSounds.SPELL_OF_HEX_MARK.get(), 1.0F, pitch);
        }
        return true;
    }

    private static void mark(ServerLevel level, UUID caster, LivingEntity target, Vec3 from, int ticks, int generation) {
        MARKS.put(target.getId(), new Mark(level, caster, level.getGameTime() + ticks, generation));
        SpellFxMessage.around(level, target.position(), SpellFxMessage.HEX, List.of(target.getId()), SpellFxMessage.points(from), ticks);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        Mark mark = MARKS.get(victim.getId());
        if (mark == null || !(victim.level() instanceof ServerLevel level) || level.getGameTime() > mark.until()) {
            return;
        }
        event.setAmount(event.getAmount() * MORE);
        Vec3 at = victim.position().add(0.0, victim.getBbHeight() * 0.6, 0.0);
        RingFx.burst(level, RingVisuals.wisp(RingVisuals.CURSE, 0.28F, 14).gravity(-0.004F), at, 6, 0.25, 0.08);
        RingFx.burst(level, RingVisuals.sparkle(0xFFC8F4, 0.14F, 16).flags(net.yezon.theabyss.ring.fx.FxOptions.FLICKER | net.yezon.theabyss.ring.fx.FxOptions.HOT),
            at, 8, 0.2, 0.18);
        RingVisuals.sound(level, at, SoundEvents.AMETHYST_CLUSTER_HIT, 1.0F, 0.6F);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        Mark mark = MARKS.remove(dead.getId());
        if (mark == null || !(dead.level() instanceof ServerLevel level) || level.getGameTime() > mark.until()) {
            return;
        }
        Vec3 from = dead.position().add(0.0, dead.getBbHeight() * 0.6, 0.0);
        SpellFxMessage.around(level, from, SpellFxMessage.HEX_END, List.of(dead.getId()), SpellFxMessage.points(from), 20);
        RingFx.implode(level, from, 3.0F, 8, RingVisuals.CURSE);
        RingVisuals.sound(level, from, TheabyssModSounds.SPELL_OF_BLACKSTRIKE_HIT.get(), 1.0F, RingVisuals.castPitch(dead.getRandom()));
        if (mark.generation() >= GENERATIONS) {
            return;
        }
        Entity caster = level.getEntity(mark.caster());
        List<LivingEntity> next = level.getEntitiesOfClass(LivingEntity.class, new AABB(from, from).inflate(JUMP_RANGE),
                e -> e != dead && e.isAlive() && !e.isSpectator() && e != caster && !MARKS.containsKey(e.getId())
                    && (caster == null || !RingMode.spares(caster, e)) && e.position().distanceToSqr(from) <= JUMP_RANGE * JUMP_RANGE)
            .stream().sorted(Comparator.comparingDouble(e -> e.position().distanceToSqr(from))).limit(JUMPS).toList();
        for (int i = 0; i < next.size(); i++) {
            LivingEntity target = next.get(i);
            TheabyssMod.queueServerWork(3 + i * 3, () -> {
                if (target.isAlive()) {
                    mark(level, mark.caster(), target, from, JUMP_DURATION, mark.generation() + 1);
                    RingVisuals.sound(level, target.position(), TheabyssModSounds.SPELL_OF_HEX_MARK.get(), 0.6F, RingVisuals.castPitch(target.getRandom()));
                }
            });
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (MARKS.isEmpty()) {
            return;
        }
        List<Integer> gone = new ArrayList<>();
        for (Iterator<Map.Entry<Integer, Mark>> it = MARKS.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, Mark> entry = it.next();
            Mark mark = entry.getValue();
            Entity e = mark.level().getEntity(entry.getKey());
            if (e == null || !e.isAlive() || mark.level().getGameTime() > mark.until()) {
                gone.add(entry.getKey());
                continue;
            }
            RingVisuals.cursedTick(mark.level(), e);
        }
        gone.forEach(MARKS::remove);
    }
}
