package net.yezon.theabyss.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.SomniumUpgrades;

public class FlyingOnPotionActiveEvents {
    public static void execute(Entity entity) {
        if (entity == null) {
            return;
        }

        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(entity);
        double _setval = vars.Mana - 0.5 * SomniumUpgrades.costFactor(vars);
        TheabyssModVariables.updatePlayerVariables(entity, capability -> {
            capability.Mana = _setval;
            capability.syncPlayerVariables(entity);
        });
        entity.setDeltaMovement(
            new Vec3(
                Math.sin(Math.toRadians(entity.getYRot() + 180.0F)) * 1.5,
                Math.sin(Math.toRadians(0.0F - entity.getXRot())),
                Math.cos(Math.toRadians(entity.getYRot())) * 1.5
            )
        );
        if (!entity.level().isClientSide()) {
            entity.syncVelocity = true;
        }

        entity.fallDistance = 0.0F;
        if (entity.level() instanceof net.minecraft.server.level.ServerLevel _level) {
            net.yezon.theabyss.ring.RingVisuals.slideTick(_level, entity);
        }
        if (TheabyssModVariables.getPlayerVariables(entity).Mana < 1.5 || entity.onGround()) {
            if (entity instanceof LivingEntity _entity) {
                _entity.removeEffect(TheabyssModMobEffects.FLYING);
            }

            entity.fallDistance = 0.0F;
        }
    }
}
