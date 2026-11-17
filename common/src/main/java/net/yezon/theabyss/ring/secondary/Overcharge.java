package net.yezon.theabyss.ring.secondary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.living.LivingDamageEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModSounds;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.util.RingMode;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class Overcharge {
    public static final int CHARGES = 3;
    public static final int DURATION = 300;
    public static final int FLIGHT = 3;

    private record Charge(int left, long until) {
    }

    private static final Map<UUID, Charge> CHARGED = new HashMap<>();

    private Overcharge() {
    }

    public static int charges(ServerPlayer player) {
        Charge charge = CHARGED.get(player.getUUID());
        return charge == null || player.level().getGameTime() > charge.until() ? 0 : charge.left();
    }

    public static double damage(double upgrade) {
        return net.yezon.theabyss.ring.RingDamage.OVERCHARGE.at(upgrade);
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.serverLevel();
        CHARGED.put(player.getUUID(), new Charge(CHARGES, level.getGameTime() + DURATION));
        RingVisuals.thunderStrike(level, player.position());
        RingVisuals.sound(level, player.position(), TheabyssModSounds.NIGHTBLADE_STORM_STRIKE.get(), 1.6F, 1.3F);
        RingVisuals.sound(level, player.position(), SoundEvents.BEACON_POWER_SELECT, 1.0F, 1.8F);
        update(level, player, CHARGES, DURATION);
        return true;
    }

    private static void update(ServerLevel level, ServerPlayer player, int charges, int ticks) {
        SpellFxMessage.around(level, player.position(), SpellFxMessage.OVERCHARGE, List.of(player.getId()), List.of((double)charges), ticks);
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player) || source.getDirectEntity() != player) {
            return;
        }
        Charge charge = CHARGED.get(player.getUUID());
        ServerLevel level = player.serverLevel();
        if (charge == null || level.getGameTime() > charge.until()) {
            CHARGED.remove(player.getUUID());
            return;
        }
        LivingEntity target = event.getEntity();
        if (RingMode.summon(target)) {
            return;
        }
        int left = charge.left() - 1;
        if (left <= 0) {
            CHARGED.remove(player.getUUID());
        } else {
            CHARGED.put(player.getUUID(), new Charge(left, charge.until()));
        }
        SpellFxMessage.around(level, target.position(), SpellFxMessage.OVERCHARGE_FIRE, List.of(player.getId(), target.getId()), List.of((double)left), FLIGHT + 8);
        update(level, player, left, (int)(charge.until() - level.getGameTime()));
        float dmg = (float)damage(TheabyssModVariables.getPlayerVariables(player).ManaDamage);
        TheabyssMod.queueServerWork(FLIGHT, () -> strike(level, player, target, dmg));
    }

    private static void strike(ServerLevel level, ServerPlayer player, LivingEntity target, float damage) {
        if (!target.isAlive() || target.level() != level || RingMode.summon(target)) {
            return;
        }
        Vec3 at = target.position();
        RingVisuals.thunderStrike(level, at);
        RingVisuals.sound(level, at, TheabyssModSounds.NIGHTBLADE_STORM_STRIKE.get(), 2.0F, 1.1F);
        DamageSource lightning = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.LIGHTNING_BOLT), null, player);
        target.invulnerableTime = 0;
        target.hurt(lightning, damage);
        target.igniteForSeconds(3.0F);
        for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(2.5),
            e -> e != target && e != player && e.isAlive() && !RingMode.spares(player, e) && !RingMode.summon(e))) {
            near.hurt(lightning, damage * 0.5F);
        }
    }
}
