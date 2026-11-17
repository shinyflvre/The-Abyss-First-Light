package net.yezon.theabyss.events;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModSounds;
import net.yezon.theabyss.network.NightbladeStormMessage;
import net.yezon.theabyss.ring.RingCast;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.util.RingMode;

public class ApplyRingOfThunderEvents {
    private static final int GAP = 2;
    private static final int CHARGE = 14;
    private static final float STRENGTH = 1.25F;
    private static final float AREA = 5.0F * STRENGTH;

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        Vec3 centre = new Vec3(x, y, z);
        RingCast cast = RingCast.start(world, entity, itemstack, 60.0, centre);
        if (cast == null) {
            return;
        }
        ServerLevel level = cast.server();
        if (level != null) {
            net.yezon.theabyss.ring.fx.SpellCircle.at(level, net.yezon.theabyss.ring.fx.SpellCircle.RUNES, entity.position(), net.yezon.theabyss.ring.RingVisuals.THUNDER, 2.2F, 26);
            float damage = targetDamage(cast.vars().ManaDamage);
            float area = damage * AREA / oldTarget(cast.vars().ManaDamage);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(20.0),
                e -> e != entity && !RingMode.spares(entity, e) && !RingMode.summon(e)).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(centre))).toList();
            for (int i = 0; i < targets.size(); i++) {
                LivingEntity target = targets.get(i);
                TheabyssMod.queueServerWork(1 + i * GAP, () -> announce(level, entity, target, damage, area));
            }
        }
        cast.durability(TheabyssModItems.RING_OF_THUNDER.get(), 1).cooldown(140).charge(60.0);
    }

    private static void announce(ServerLevel level, Entity caster, LivingEntity target, float damage, float area) {
        if (!target.isAlive() || target.level() != level) {
            return;
        }
        Vec3 at = target.position();
        NightbladeStormMessage.around(level, caster.getId(), at, CHARGE, false, true);
        level.playSound(null, at.x, at.y, at.z, TheabyssModSounds.NIGHTBLADE_STORM_CIRCLE.get(), SoundSource.PLAYERS, 1.2F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        TheabyssMod.queueServerWork(CHARGE, () -> strike(level, caster, target, damage, area));
    }

    public static float targetDamage(double upgrade) {
        return (float)net.yezon.theabyss.ring.RingDamage.THUNDER.at(upgrade);
    }

    private static float oldTarget(double upgrade) {
        return Math.max(AREA, (float)(3.0 * upgrade) * STRENGTH);
    }

    private static void strike(ServerLevel level, Entity caster, Entity target, float damage, float area) {
        if (!target.isAlive() || target.level() != level) {
            return;
        }
        Vec3 at = Vec3.atBottomCenterOf(BlockPos.containing(target.getX(), target.getY(), target.getZ()));
        RingVisuals.thunderStrike(level, at);
        level.playSound(null, at.x, at.y, at.z, TheabyssModSounds.NIGHTBLADE_STORM_STRIKE.get(), SoundSource.PLAYERS, 3.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        DamageSource lightning = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.LIGHTNING_BOLT));
        AABB box = new AABB(at.x - 3.0, at.y - 3.0, at.z - 3.0, at.x + 3.0, at.y + 9.0, at.z + 3.0);
        for (LivingEntity hit : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster && !RingMode.spares(caster, e) && !RingMode.summon(e))) {
            hit.igniteForSeconds(8.0F);
            hit.hurt(lightning, area);
        }
        target.hurt(lightning, damage);
    }
}
