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

public class ApplyRingOfInvEvents {
    public static float radius(int level) {
        return 12.0F - 2.0F * (level - 1);
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        RingCast cast = RingCast.start(world, entity, itemstack, 20.0, new Vec3(x, y, z));
        if (cast == null) {
            return;
        }
        ServerLevel level = cast.server();
        if (level != null && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 400, 5));
            if (living instanceof net.minecraft.server.level.ServerPlayer player) {
                net.yezon.theabyss.ring.TrueInvisibility.start(player, 400, radius(ApplyRingOfTeleportEvents.level(cast.vars().ManaUpgrade)));
            }
            RingVisuals.invisibility(level, living);
        }
        cast.durability(TheabyssModItems.RING_OF_INVISIBILITY.get(), 1).cooldown(800).sound("theabyss:spell_of_invisibility").charge(20.0);
    }
}
