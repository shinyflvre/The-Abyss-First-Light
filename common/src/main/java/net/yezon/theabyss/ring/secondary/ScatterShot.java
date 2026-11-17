package net.yezon.theabyss.ring.secondary;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.entity.RingOfFireStrikeAttackProjectileEntity;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.init.TheabyssModSounds;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.RingFx;

public final class ScatterShot {
    public static final int BOLTS = 5;
    public static final float SPREAD = 9.0F;
    public static final float SPEED = 2.4F;

    private ScatterShot() {
    }

    public static double damage(double upgrade) {
        return net.yezon.theabyss.ring.RingDamage.SCATTER_SHOT.at(upgrade);
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle();
        Vec3 eye = player.getEyePosition().subtract(0.0, 0.1, 0.0);
        double base = damage(vars.ManaDamage) / SPEED - 0.01;
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < BOLTS; i++) {
            float yaw = (i - (BOLTS - 1) / 2.0F) * SPREAD;
            Vec3 dir = look.yRot(yaw * net.minecraft.util.Mth.DEG_TO_RAD).add(0.0, (i % 2 == 0 ? 0.0 : 0.035), 0.0).normalize();
            RingOfFireStrikeAttackProjectileEntity bolt = new RingOfFireStrikeAttackProjectileEntity(TheabyssModEntities.RING_OF_FIRE_STRIKE_ATTACK_PROJECTILE.get(), level);
            bolt.setOwner(player);
            bolt.setBaseDamage(base);
            bolt.setSilent(true);
            bolt.igniteForSeconds(100.0F);
            bolt.addTag(net.yezon.theabyss.events.RingOfFireStrikeParticleEvents.SCATTER);
            bolt.setPos(eye.add(dir.scale(0.6)));
            bolt.shoot(dir.x, dir.y, dir.z, SPEED, 0.0F);
            level.addFreshEntity(bolt);
            ids.add(bolt.getId());
        }
        net.yezon.theabyss.ring.fx.SpellCircle.at(level, net.yezon.theabyss.ring.fx.SpellCircle.SHOCK, player.position(), RingVisuals.FIRE, 1.4F, 12);
        SpellFxMessage.around(level, eye, SpellFxMessage.TRAIL, ids, List.of((double)RingVisuals.FIRE, (double)RingVisuals.EMBER), 60);
        Vec3 hand = RingVisuals.hand(player);
        Vec3 muzzle = hand.add(look.scale(2.2));
        RingFx.burst(level, RingVisuals.sparkle(RingVisuals.EMBER, 0.1F, 16).gravity(0.04F).flags(net.yezon.theabyss.ring.fx.FxOptions.FLICKER | net.yezon.theabyss.ring.fx.FxOptions.HOT),
            muzzle, 12, 0.15, 0.2);
        RingFx.burst(level, RingVisuals.flame(RingVisuals.FIRE, 0.22F, 8).drag(0.8F), muzzle, 5, 0.1, 0.08);
        RingVisuals.sound(level, eye, TheabyssModSounds.SPELL_OF_FIRE_SHOOT.get(), 1.0F, RingVisuals.castPitch(player.getRandom()));
        return true;
    }
}
