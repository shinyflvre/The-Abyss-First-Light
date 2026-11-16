package net.yezon.theabyss.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.SomniumUpgrades;

public class ApplyRingOfSeekerEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        if (entity == null) {
            return;
        }

        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(entity);
        if (vars.Mana < 50.0 * SomniumUpgrades.costFactor(vars)) {
            if (entity instanceof Player _player && !_player.level().isClientSide()) {
                _player.sendOverlayMessage(Component.translatable("ring.theabyss.low_energy"));
            }
            return;
        }

        if (itemstack.getItem() == TheabyssModItems.RING_OF_SEEKER.get() && world instanceof ServerLevel _serverLevel) {
            net.yezon.theabyss.ring.RingWear.wear(itemstack, _serverLevel, entity, 1);
        }

        if (world instanceof ServerLevel _level) {
            net.yezon.theabyss.ring.fx.SpellCircle.at(_level, net.yezon.theabyss.ring.fx.SpellCircle.TECH, new net.minecraft.world.phys.Vec3(x, y, z), net.yezon.theabyss.ring.RingVisuals.ABYSS, 2.0F, 30);
            net.yezon.theabyss.ring.fx.RingAccent.send(_level, net.yezon.theabyss.ring.fx.RingAccent.SEEKER, new net.minecraft.world.phys.Vec3(x, y, z));
            double attack = net.yezon.theabyss.ring.RingDamage.HOLLOW_SEEKERS.at(vars.ManaDamage);
            for (int i = 0; i < 8; i++) {
                var seeker = TheabyssModEntities.SUMMONED_SEEKER.get().spawn(_level, BlockPos.containing(x, y, z), EntitySpawnReason.MOB_SUMMONED);
                var damage = seeker == null ? null : seeker.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                if (damage != null) {
                    damage.setBaseValue(attack);
                }
            }
        }

        double _setval = vars.Mana - 50.0 * SomniumUpgrades.costFactor(vars);
        TheabyssModVariables.updatePlayerVariables(entity, capability -> {
            capability.Mana = _setval;
            capability.syncPlayerVariables(entity);
        });
    }
}
