package net.yezon.theabyss.events;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.ring.RingCast;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.util.RingMode;

public class ApplyRingOfFireEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        Vec3 centre = new Vec3(x, y, z);
        RingCast cast = RingCast.start(world, entity, itemstack, 35.0, centre);
        if (cast == null) {
            return;
        }
        ServerLevel level = cast.server();
        if (level != null) {
            List<Entity> hit = new ArrayList<>();
            for (Entity target : level.getEntitiesOfClass(Entity.class, new AABB(centre, centre).inflate(20.0), e -> e != entity)) {
                if (!RingMode.spares(entity, target)) {
                    target.igniteForSeconds((int)(10.0 + cast.vars().ManaDamage));
                    hit.add(target);
                }
            }
            if (entity instanceof LivingEntity caster) {
                RingVisuals.fire(level, centre, caster, hit);
            }
        }
        cast.durability(TheabyssModItems.RING_OF_FIRE.get(), 1).cooldown(250).sound("theabyss:spell_of_fire").charge(35.0);
    }
}
