package net.yezon.theabyss.ring.secondary;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.util.FallGuard;

final class Dash {
    private Dash() {
    }

    static Vec3 ahead(ServerPlayer player) {
        Vec3 dir = player.getLookAngle().multiply(1.0, 0.0, 1.0);
        return dir.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : dir.normalize();
    }

    static void run(ServerPlayer player, Vec3 dir, double speed, int ticks, Consumer<Vec3> each, Consumer<List<Vec3>> done) {
        List<Vec3> path = new ArrayList<>();
        path.add(player.position());
        FallGuard.protect(player, ticks + 40);
        push(player, dir, speed, ticks, 0, path, each, done);
    }

    private static void push(ServerPlayer player, Vec3 dir, double speed, int ticks, int tick, List<Vec3> path, Consumer<Vec3> each,
                             Consumer<List<Vec3>> done) {
        if (player.isRemoved() || !player.isAlive()) {
            return;
        }
        if (tick >= ticks) {
            player.setDeltaMovement(dir.scale(0.25).add(0.0, Math.min(0.0, player.getDeltaMovement().y), 0.0));
            player.hurtMarked = true;
            path.add(player.position());
            done.accept(path);
            return;
        }
        player.setDeltaMovement(dir.scale(speed).add(0.0, tick == 0 ? 0.12 : 0.0, 0.0));
        player.hurtMarked = true;
        player.resetFallDistance();
        TheabyssMod.queueServerWork(1, () -> {
            path.add(player.position());
            each.accept(player.position());
            push(player, dir, speed, ticks, tick + 1, path, each, done);
        });
    }
}
