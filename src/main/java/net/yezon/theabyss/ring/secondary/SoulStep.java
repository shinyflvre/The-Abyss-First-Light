package net.yezon.theabyss.ring.secondary;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TeleportDashMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.util.FallGuard;

public final class SoulStep {
    public static final double REACH = 3.0;
    private static final double[] TRY = {3.0, 3.5, 2.5, 4.0, 2.0, 4.5};
    private static final double[] RISE = {0.0, 1.0, -1.0};
    public static final int PEARL = 0xE8E0FF;

    private SoulStep() {
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.level();
        Vec3 dir = Dash.ahead(player);
        Vec3 from = player.position();
        Vec3 dest = null;
        search:
        for (int pass = 0; pass < 2; pass++) {
            for (double d : TRY) {
                for (double dy : RISE) {
                    Vec3 at = from.add(dir.scale(d)).add(0.0, dy, 0.0);
                    var box = player.getBoundingBox().move(at.subtract(from));
                    if (level.noCollision(player, box) && (pass == 1 || !level.noCollision(player, box.move(0.0, -0.6, 0.0).inflate(-0.05, 0.0, -0.05)))) {
                        dest = at;
                        break search;
                    }
                }
            }
        }
        if (dest == null) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.spectral.no_room"));
            return false;
        }
        Vec3 fromEye = player.getEyePosition();
        List<Double> ghost = new ArrayList<>();
        ghost.add((double)PEARL);
        ghost.add(from.x);
        ghost.add(from.y);
        ghost.add(from.z);
        ghost.add((double)player.getYRot());
        SpellFxMessage.around(level, from, SpellFxMessage.GHOSTS, List.of(player.getId()), ghost, 24);
        SpellFxMessage.around(level, from, SpellFxMessage.SOUL, List.of(player.getId()), SpellFxMessage.points(from, dest), 20);
        PacketDistributor.sendToPlayer(player, new TeleportDashMessage(fromEye.x, fromEye.y, fromEye.z, player.getYRot(), player.getXRot()));
        player.teleportTo(dest.x, dest.y, dest.z);
        player.connection.teleport(dest.x, dest.y, dest.z, player.getYRot(), player.getXRot());
        player.resetFallDistance();
        FallGuard.protect(player, 40);
        net.yezon.theabyss.ring.RingVisuals.sound(level, from, SoundEvents.SOUL_ESCAPE.value(), 2.0F, 0.8F);
        net.yezon.theabyss.ring.RingVisuals.sound(level, dest, SoundEvents.ALLAY_ITEM_TAKEN, 1.2F, 0.6F);
        net.yezon.theabyss.ring.RingVisuals.sound(level, dest, SoundEvents.AMETHYST_BLOCK_CHIME, 1.4F, 1.6F);
        return true;
    }
}
