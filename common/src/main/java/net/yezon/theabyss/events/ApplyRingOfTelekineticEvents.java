package net.yezon.theabyss.events;

import java.util.Comparator;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.ring.RingCast;
import net.yezon.theabyss.ring.Telekinesis;
import net.yezon.theabyss.util.RingMode;

public class ApplyRingOfTelekineticEvents {
    public static float damage(double upgrade) {
        return (float)net.yezon.theabyss.ring.RingDamage.TELEKINESIS.at(upgrade);
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        if (!(entity instanceof LivingEntity caster) || !(world instanceof ServerLevel level)) {
            return;
        }
        Vec3 centre = caster.position();
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(Telekinesis.RADIUS),
                e -> e != caster && e.isAlive() && !e.isSpectator() && !RingMode.spares(caster, e)
                    && e.distanceToSqr(caster) <= Telekinesis.RADIUS * Telekinesis.RADIUS)
            .stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(caster))).limit(Telekinesis.MAX_TARGETS).toList();
        if (targets.isEmpty()) {
            if (caster instanceof ServerPlayer player) {
                player.displayClientMessage(Component.translatable("ring.theabyss.telekinetic.nothing_in_reach"), true);
            }
            return;
        }
        RingCast cast = RingCast.start(world, entity, itemstack, 35.0, centre);
        if (cast == null) {
            return;
        }
        float damage = damage(cast.vars().ManaDamage);
        Telekinesis.start(level, caster, targets, damage);
        cast.durability(TheabyssModItems.RING_OF_TELEKINETIC.get(), 1).cooldown(800).sound("theabyss:spell_of_telekinetic").charge(35.0);
    }
}
