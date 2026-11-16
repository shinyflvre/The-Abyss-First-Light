package net.yezon.theabyss.events;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
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

public class ApplyRingOfFreezeEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        Vec3 centre = new Vec3(x, y, z);
        RingCast cast = RingCast.start(world, entity, itemstack, 35.0, centre);
        if (cast == null) {
            return;
        }
        ServerLevel level = cast.server();
        if (level != null) {
            List<LivingEntity> hit = new ArrayList<>();
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(50.0), e -> e != entity)) {
                if (!RingMode.spares(entity, target)) {
                    target.addEffect(new MobEffectInstance(TheabyssModMobEffects.FROZEN, 140, 0, false, false, true));
                    target.addEffect(new MobEffectInstance(TheabyssModMobEffects.FROST_POISON, 140, 20, false, false));
                    hit.add(target);
                }
            }
            if (entity instanceof LivingEntity caster) {
                RingVisuals.freeze(level, centre, caster, hit);
            }
        }
        cast.durability(TheabyssModItems.RING_OF_FREEZE.get(), 1).cooldown(280).sound("theabyss:spell_of_freeze").charge(35.0);
    }
}
