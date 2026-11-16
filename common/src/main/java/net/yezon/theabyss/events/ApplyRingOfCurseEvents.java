package net.yezon.theabyss.events;

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

public class ApplyRingOfCurseEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        Vec3 centre = new Vec3(x, y, z);
        RingCast cast = RingCast.start(world, entity, itemstack, 50.0, centre);
        if (cast == null) {
            return;
        }
        ServerLevel level = cast.server();
        if (level != null) {
            if (entity instanceof LivingEntity caster) {
                caster.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 400, 1, false, false));
                caster.addEffect(new MobEffectInstance(TheabyssModMobEffects.CURSED_AURA.getDelegate(), 400, 1, false, false));
                RingVisuals.curse(level, centre, caster);
            }
            int amplifier = (int)cast.vars().ManaDamage;
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(200.0), e -> e != entity)) {
                if (!RingMode.spares(entity, target)) {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 400, 1, false, false));
                    target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 400, amplifier, false, false));
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, amplifier, false, false));
                    target.addEffect(new MobEffectInstance(TheabyssModMobEffects.CURSE.getDelegate(), 400, amplifier, false, false));
                }
            }
        }
        cast.durability(TheabyssModItems.RING_OF_CURSE.get(), 4).cooldown(2000).sound("theabyss:spell_of_curse", centre, 1.0F, RingVisuals.castPitch(entity.getRandom())).charge(50.0);
    }
}
