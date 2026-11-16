package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.entity.RingOfBlackStrikeAttackProjectileEntity;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.ring.RingCast;

public final class ApplyRingOfBlackstrikeEvents {
    private static final double COST = 40.0;
    private static final int COOLDOWN = 40;
    private static final int KNOCKBACK = 2;
    private static final float SPEED = 1.5F;
    private static final double RANGE = 40.0;

    private ApplyRingOfBlackstrikeEvents() {
    }

    public static float damage(double upgrade) {
        return (float)net.yezon.theabyss.ring.RingDamage.BLACK_STRIKE.at(upgrade);
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        RingCast cast = RingCast.start(world, entity, itemstack, COST, new Vec3(x, y, z));
        if (cast == null) {
            return;
        }
        cast.durability(TheabyssModItems.RING_OF_BLACKSTRIKE.get(), 1).cooldown(COOLDOWN)
            .sound("theabyss:spell_of_blackstrike_shoot", new Vec3(x, y, z), 1.0F, net.yezon.theabyss.ring.RingVisuals.castPitch(entity.getRandom()));
        if (entity.level() instanceof ServerLevel level) {
            RingOfBlackStrikeAttackProjectileEntity bolt = new RingOfBlackStrikeAttackProjectileEntity(TheabyssModEntities.RING_OF_BLACK_STRIKE_ATTACK_PROJECTILE.get(), level);
            bolt.setKnockback(KNOCKBACK);
            bolt.setOwner(entity);
            bolt.setBaseDamage(damage(cast.vars().ManaDamage) / SPEED - 0.001);
            bolt.setNoGravity(true);
            bolt.setSilent(true);
            bolt.setPos(entity.getX(), entity.getEyeY() - 0.1, entity.getZ());
            Vec3 look = entity.getLookAngle();
            bolt.shoot(look.x, look.y, look.z, SPEED, 0.0F);
            bolt.limitRange(RANGE);
            level.addFreshEntity(bolt);
        }
        cast.charge(COST);
    }
}
