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

public class ApplyRingOfElectroEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        Vec3 centre = new Vec3(x, y, z);
        RingCast cast = RingCast.start(world, entity, itemstack, 40.0, centre);
        if (cast == null) {
            return;
        }
        ServerLevel level = cast.server();
        if (level != null) {
            List<LivingEntity> charged = new ArrayList<>();
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(30.0), e -> e != entity)) {
                if (!RingMode.spares(entity, target)) {
                    target.addEffect(new MobEffectInstance(TheabyssModMobEffects.ELECTRICITY, 100, (int)cast.vars().ManaDamage, false, false));
                    charged.add(target);
                }
            }
            if (entity instanceof LivingEntity caster) {
                RingVisuals.electro(level, centre, caster, charged);
            }
        }
        cast.durability(TheabyssModItems.RING_OF_ELECTRO.get(), 4).cooldown(360).sound("theabyss:spell_of_teleport").charge(40.0);
    }
}
