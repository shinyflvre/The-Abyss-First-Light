package net.yezon.theabyss.ring.secondary;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.entity.NightbladeBladeEntity;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingVisuals;

public final class BladeThrow {
    public static final double RANGE = 16.0;
    public static final int PASS = 12;

    private BladeThrow() {
    }

    public static double damage(double upgrade) {
        return 6.0 + 2.0 * upgrade;
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.serverLevel();
        LivingEntity target = Aim.creature(level, player, RANGE);
        Vec3 through = target != null ? target.getBoundingBox().getCenter() : Aim.spot(level, player, RANGE).add(0.0, 1.0, 0.0);
        Vec3 start = RingVisuals.hand(player);
        NightbladeBladeEntity blade = TheabyssModEntities.NIGHTBLADE_BLADE.get().create(level);
        if (blade == null) {
            return false;
        }
        blade.launchRing(player, start, through, (float)damage(vars.ManaDamage), PASS);
        level.addFreshEntity(blade);
        net.yezon.theabyss.ring.fx.SpellCircle.on(level, net.yezon.theabyss.ring.fx.SpellCircle.SLASH, player, RingVisuals.NIGHTBLADE, 1.4F, 12);
        SpellFxMessage.around(level, start, SpellFxMessage.TRAIL, List.of(blade.getId()), List.of((double)RingVisuals.NIGHTBLADE, (double)0xDFFFF6), PASS * 2 + 6);
        RingVisuals.sound(level, start, SoundEvents.TRIDENT_THROW.value(), 1.4F, 0.8F);
        RingVisuals.sound(level, start, SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, 0.6F);
        return true;
    }
}
