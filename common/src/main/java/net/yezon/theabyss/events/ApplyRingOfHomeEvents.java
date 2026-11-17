package net.yezon.theabyss.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.ring.RingCast;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.network.TheabyssModVariables;

public class ApplyRingOfHomeEvents {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        if (entity == null || !TheabyssModVariables.getPlayerVariables(entity).RingOfHome) {
            return;
        }
        if (entity.level().dimension() != Level.OVERWORLD) {
            if (entity instanceof Player player && !player.level().isClientSide()) {
                player.displayClientMessage(Component.translatable("ring.theabyss.overworld"), true);
            }
            return;
        }
        RingCast cast = RingCast.start(world, entity, itemstack, 50.0, new Vec3(x, y, z));
        if (cast == null) {
            return;
        }
        cast.durability(TheabyssModItems.RING_OF_HOME.get(), 1).cooldown(1500).sound("theabyss:spell_of_teleport");
        TheabyssModVariables.PlayerVariables vars = cast.vars();
        ServerLevel level = cast.server();
        if (level != null) {
            RingVisuals.teleportDepart(level, entity.position(), entity.getBbHeight());
            RingFxHome.depart(level, entity.position());
        }
        net.yezon.theabyss.util.AbyssTeleport.to(entity, vars.HomeX, vars.HomeY, vars.HomeZ);
        if (entity instanceof ServerPlayer player) {
            net.yezon.theabyss.util.AbyssTeleport.connection(player, vars.HomeX, vars.HomeY, vars.HomeZ, entity.getYRot(), entity.getXRot());
        }
        if (level != null) {
            RingVisuals.teleportArrive(level, new Vec3(vars.HomeX, vars.HomeY, vars.HomeZ));
            net.yezon.theabyss.ring.fx.SpellCircle.at(level, net.yezon.theabyss.ring.fx.SpellCircle.TECH, new Vec3(vars.HomeX, vars.HomeY, vars.HomeZ), RingVisuals.ABYSS, 1.8F, 30);
            net.yezon.theabyss.ring.fx.RingAccent.send(level, net.yezon.theabyss.ring.fx.RingAccent.HOME, new Vec3(vars.HomeX, vars.HomeY, vars.HomeZ));
            cast.sound("theabyss:spell_of_teleport", new Vec3(vars.HomeX, vars.HomeY, vars.HomeZ), 1.0F, 1.2F);
        }
        cast.charge(50.0);
    }

    private static final class RingFxHome {
        static void depart(ServerLevel level, Vec3 feet) {
            net.yezon.theabyss.ring.fx.RingFx.pillar(level, feet, 4.0F, 14, RingVisuals.ABYSS, 0.9F);
        }
    }
}
