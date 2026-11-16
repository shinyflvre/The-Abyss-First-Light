package net.yezon.theabyss.ring.secondary;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModSounds;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.RingFx;

public final class HealingPulse {
    public static final double RADIUS = 8.0;
    public static final float HEAL = 16.0F;
    public static final int TRAVEL = 8;
    private static final float ECHO_VOLUME = 0.45F;
    private static final float ECHO_VOLUME_TOTAL = 1.2F;

    private HealingPulse() {
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.level();
        List<LivingEntity> friends = level.getEntitiesOfClass(LivingEntity.class, new AABB(player.position(), player.position()).inflate(RADIUS),
            e -> e != player && e.isAlive() && !e.isSpectator() && e.distanceToSqr(player) <= RADIUS * RADIUS && friend(player, e));
        if (friends.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.reg.nobody"));
            return false;
        }
        List<Integer> ids = new ArrayList<>();
        ids.add(player.getId());
        friends.forEach(e -> ids.add(e.getId()));
        SpellFxMessage.around(level, player.position(), SpellFxMessage.HEAL, ids, List.of(), 34);
        RingFx.wave(level, player.position().add(0.0, 0.1, 0.0), (float)RADIUS, 18, RingVisuals.REG_GREEN);
        RingVisuals.sound(level, player.position(), TheabyssModSounds.SPELL_OF_HEALING_PULSE.get(), 1.0F, RingVisuals.castPitch(player.getRandom()));
        TheabyssMod.queueServerWork(TRAVEL, () -> {
            float volume = Math.min(ECHO_VOLUME, ECHO_VOLUME_TOTAL / friends.size());
            for (int i = 0; i < friends.size(); i++) {
                LivingEntity e = friends.get(i);
                if (!e.isAlive()) {
                    continue;
                }
                e.heal(HEAL);
                e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 2, false, true));
                float pitch = 0.85F + 0.15F * (i + level.getRandom().nextFloat()) / friends.size();
                RingVisuals.sound(level, e.position(), TheabyssModSounds.SPELL_OF_REGEN.get(), volume, pitch);
            }
        });
        return true;
    }

    private static boolean friend(Player caster, LivingEntity e) {
        if (e instanceof Player) {
            return true;
        }
        if (e instanceof OwnableEntity owned && owned.getOwnerReference() != null) {
            return true;
        }
        return e instanceof AbstractHorse horse && horse.isTamed();
    }
}
