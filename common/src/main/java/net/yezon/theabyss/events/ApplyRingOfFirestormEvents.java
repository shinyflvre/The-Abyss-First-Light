package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.ring.RingCast;
import net.yezon.theabyss.ring.RingVisuals;

public class ApplyRingOfFirestormEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        RingCast cast = RingCast.start(world, entity, itemstack, 80.0, new Vec3(x, y, z));
        if (cast == null) {
            return;
        }
        ServerLevel level = cast.server();
        if (level != null && entity instanceof LivingEntity caster) {
            caster.addEffect(new MobEffectInstance(TheabyssModMobEffects.FIRESTORM.getDelegate(), 200, 0, false, false));
            RingVisuals.firestorm(level, caster);
        }
        cast.durability(TheabyssModItems.RING_OF_FIRESTORM.get(), 1).cooldown(1000).sound("theabyss:spell_of_fire").charge(80.0);
    }
}
