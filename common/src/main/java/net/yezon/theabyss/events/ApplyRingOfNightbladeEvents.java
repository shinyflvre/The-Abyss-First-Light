package net.yezon.theabyss.events;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingCast;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.util.RingMode;

public class ApplyRingOfNightbladeEvents {
    private static final int HOLD = 100;

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        Vec3 centre = new Vec3(x, y, z);
        RingCast cast = RingCast.start(world, entity, itemstack, 50.0, centre);
        if (cast == null) {
            return;
        }
        cast.durability(TheabyssModItems.RING_OF_NIGHTBLADE.get(), 1).cooldown(300).sound("theabyss:spell_of_teleport").charge(50.0);
        ServerLevel level = cast.server();
        if (level == null) {
            return;
        }
        RingVisuals.nightbladeRift(level, centre);
        DamageSource magic = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.MAGIC));
        DamageSource generic = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.GENERIC));
        float damage = (float)net.yezon.theabyss.ring.RingDamage.NIGHTBLADE_RIFT.at(cast.vars().ManaDamage);
        for (Entity target : level.getEntitiesOfClass(Entity.class, new AABB(centre, centre).inflate(20.0), e -> e != entity)) {
            if (RingMode.spares(entity, target)) {
                continue;
            }
            if (target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 1, false, false));
            }
            Vec3 from = target.position();
            teleport(target, target.getX(), y + 10.0, target.getZ());
            target.setNoGravity(true);
            target.hurt(magic, damage);
            RingVisuals.nightbladeLift(level, target, from, HOLD);
            TheabyssMod.queueServerWork(HOLD, () -> {
                teleport(target, target.getX(), target.getY(), target.getZ());
                target.hurt(generic, damage);
                target.setNoGravity(false);
            });
        }
        double nbX = entity.getX();
        double nbY = entity.getY();
        double nbZ = entity.getZ();
        TheabyssModVariables.updatePlayerVariables(entity, capability -> {
            capability.NightbladeX = nbX;
            capability.NightbladeY = nbY;
            capability.NightbladeZ = nbZ;
            capability.syncPlayerVariables(entity);
        });
        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 0, false, false));
        }
        TheabyssMod.queueServerWork(3, () -> {
            entity.setNoGravity(true);
            Vec3 from = entity.position();
            teleport(entity, x, y + 10.0, z);
            RingVisuals.nightbladeLift(level, entity, from, HOLD - 3);
        });
        TheabyssMod.queueServerWork(HOLD, () -> {
            entity.setNoGravity(false);
            TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(entity);
            Vec3 from = entity.position();
            teleport(entity, vars.NightbladeX, vars.NightbladeY, vars.NightbladeZ);
            RingVisuals.teleportDepart(level, from, entity.getBbHeight());
            RingVisuals.teleportArrive(level, entity.position());
            cast.sound("theabyss:spell_of_teleport");
            if (entity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 0, false, false));
            }
        });
    }

    private static void teleport(Entity entity, double x, double y, double z) {
        net.yezon.theabyss.util.AbyssTeleport.to(entity, x, y, z);
        if (entity instanceof ServerPlayer player) {
            net.yezon.theabyss.util.AbyssTeleport.connection(player, x, y, z, entity.getYRot(), entity.getXRot());
        }
    }
}
