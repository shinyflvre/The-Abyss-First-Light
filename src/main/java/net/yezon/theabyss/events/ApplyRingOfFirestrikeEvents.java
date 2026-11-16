package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.entity.RingOfFireStrikeAttackProjectileEntity;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.ring.RingCast;
import net.yezon.theabyss.ring.RingVisuals;

public final class ApplyRingOfFirestrikeEvents {
    private static final double COST = 25.0;
    private static final int COOLDOWN = 20;
    private static final int KNOCKBACK = 2;
    private static final int PIERCE = 2;
    private static final float SPEED = 3.0F;

    private ApplyRingOfFirestrikeEvents() {
    }

    public static float damage(double upgrade) {
        return (float)net.yezon.theabyss.ring.RingDamage.FIRE_STRIKE.at(upgrade);
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        RingCast cast = RingCast.start(world, entity, itemstack, COST, new Vec3(x, y, z));
        if (cast == null) {
            return;
        }
        cast.durability(TheabyssModItems.RING_OF_FIRESTRIKE.get(), 1).cooldown(COOLDOWN).sound("theabyss:spell_of_fire_shoot", new Vec3(x, y, z), 1.0F, RingVisuals.castPitch(entity.getRandom()));
        if (entity.level() instanceof ServerLevel level) {
            RingOfFireStrikeAttackProjectileEntity bolt = new RingOfFireStrikeAttackProjectileEntity(TheabyssModEntities.RING_OF_FIRE_STRIKE_ATTACK_PROJECTILE.get(), level);
            bolt.setKnockback(KNOCKBACK);
            bolt.setPierce(PIERCE);
            bolt.setOwner(entity);
            bolt.setBaseDamage(damage(cast.vars().ManaDamage) / SPEED - 0.001);
            bolt.setSilent(true);
            bolt.setPos(entity.getX(), entity.getEyeY() - 0.1, entity.getZ());
            Vec3 look = entity.getLookAngle();
            bolt.shoot(look.x, look.y, look.z, SPEED, 0.0F);
            level.addFreshEntity(bolt);
        }
        cast.charge(COST);
    }
}
