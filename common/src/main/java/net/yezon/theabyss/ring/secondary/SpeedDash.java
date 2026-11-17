package net.yezon.theabyss.ring.secondary;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.SpellFxMessage;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.RingVisuals;
import net.yezon.theabyss.ring.fx.RingFx;

public final class SpeedDash {
    public static final double SPEED = 2.0;
    public static final int TICKS = 4;

    private SpeedDash() {
    }

    static boolean cast(ServerPlayer player, ItemStack ring, TheabyssModVariables.PlayerVariables vars) {
        ServerLevel level = player.serverLevel();
        Vec3 dir = Dash.ahead(player);
        SpellFxMessage.around(level, player.position(), SpellFxMessage.DASH, List.of(player.getId()), List.of((double)RingVisuals.SPEED), TICKS);
        RingFx.burst(level, RingVisuals.zap(RingVisuals.SPEED, 0.3F, 6), player.position().add(0.0, 0.3, 0.0), 8, 0.4, 0.08);
        RingVisuals.sound(level, player.position(), SoundEvents.BREEZE_JUMP, 1.4F, 1.4F);
        var dash = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.parse("theabyss:dash"));
        if (dash != null) {
            RingVisuals.sound(level, player.position(), dash, 1.0F, 1.2F);
        }
        Dash.run(player, dir, SPEED, TICKS, at -> RingFx.burst(level, RingVisuals.zap(RingVisuals.SPEED, 0.22F, 5), at.add(0.0, 0.2, 0.0), 3, 0.3, 0.05),
            path -> {
                List<Double> ghosts = new ArrayList<>();
                ghosts.add((double)RingVisuals.SPEED);
                for (Vec3 p : path) {
                    ghosts.add(p.x);
                    ghosts.add(p.y);
                    ghosts.add(p.z);
                    ghosts.add((double)player.getYRot());
                }
                SpellFxMessage.around(level, player.position(), SpellFxMessage.GHOSTS, List.of(player.getId()), ghosts, 18);
                net.yezon.theabyss.ring.fx.SpellCircle.at(level, net.yezon.theabyss.ring.fx.SpellCircle.SLASH, player.position(), RingVisuals.SPEED, 1.6F, 10);
            });
        return true;
    }
}
