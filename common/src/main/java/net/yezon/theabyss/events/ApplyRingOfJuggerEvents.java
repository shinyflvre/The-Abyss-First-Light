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

public class ApplyRingOfJuggerEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        RingCast cast = RingCast.start(world, entity, itemstack, 65.0, new Vec3(x, y, z));
        if (cast == null) {
            return;
        }
        ServerLevel level = cast.server();
        if (level != null && entity instanceof LivingEntity living) {
            int amplifier = (int)cast.vars().ManaDamage;
            living.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2000, 5));
            living.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 2000, amplifier));
            living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 2000, amplifier));
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 2000, 0));
            RingVisuals.jugger(level, living);
        }
        cast.durability(TheabyssModItems.RING_OF_JUGGER.get(), 16).cooldown(4000).sound("theabyss:spell_of_jugger").charge(65.0);
    }
}
