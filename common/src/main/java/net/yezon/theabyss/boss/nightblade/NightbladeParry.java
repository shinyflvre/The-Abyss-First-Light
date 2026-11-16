package net.yezon.theabyss.boss.nightblade;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class NightbladeParry {
    public static final int WINDOW = 11;
    public static final int MINI_BOSS_WINDOW = 15;
    static final int LOCKOUT = 16;
    private static final Map<Player, Long> ATTEMPTS = new WeakHashMap<>();

    private NightbladeParry() {
    }

    public static void playParryEffect(net.minecraft.server.level.ServerLevel level, net.minecraft.world.phys.Vec3 at) {
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 30, 0.4, 0.4, 0.4, 0.4);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME, at.x, at.y, at.z, 12, 0.3, 0.3, 0.3, 0.08);
        level.playSound(null, at.x, at.y, at.z, net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(), net.minecraft.sounds.SoundSource.PLAYERS, 0.9F, 1.5F);
        level.playSound(null, at.x, at.y, at.z, net.minecraft.sounds.SoundEvents.ANVIL_LAND, net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 1.8F);
    }

    public static void attempt(ServerPlayer player) {
        if (player.getMainHandItem().isEmpty() && !net.yezon.theabyss.ring.ComeHere.pulling(player) || player.isSpectator()) {
            return;
        }
        long now = player.level().getGameTime();
        Long last = ATTEMPTS.get(player);
        if (last != null && now - last >= 0 && now - last < LOCKOUT) {
            return;
        }
        ATTEMPTS.put(player, now);
    }

    static boolean consume(Player player) {
        return consume(player, WINDOW);
    }

    public static boolean consume(Player player, int window) {
        Long last = ATTEMPTS.get(player);
        if (last == null) {
            return false;
        }
        long age = player.level().getGameTime() - last;
        if (age < 0 || age > window) {
            return false;
        }
        ATTEMPTS.remove(player);
        return true;
    }
}
