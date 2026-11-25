package net.yezon.theabyss.ring;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.network.TheabyssModVariables;
import org.jspecify.annotations.Nullable;

public final class RingDamage {
    public static final Table BLACK_STRIKE = new Table(8, 12, 14, 16, 20, 25);
    public static final Table VOID_RIFT = new Table(12.5, 12.5, 12.5, 15, 17.5, 20);
    public static final Table CRYSTAL_NEEDLES = new Table(2, 2, 2, 2, 3, 4);
    public static final Table CRYSTAL_FANGS = new Table(4, 5, 6, 7, 8, 10);
    public static final Table CRYSTAL_WALL = new Table(4, 5, 6, 8, 10, 12);
    public static final Table FIRESTORM = new Table(0.5, 1.5, 2, 3, 4, 5);
    public static final Table METEOR = new Table(18, 18, 18, 18, 20, 25);
    public static final Table FIRE_STRIKE = new Table(8, 10, 12, 16, 18, 20);
    public static final Table SCATTER_SHOT = new Table(5, 5, 5, 7, 9, 11);
    public static final Table ICE_PRISON = new Table(4.5, 4.5, 4.5, 6, 8, 10);
    public static final Table GHOST_CURSE = new Table(0.5, 1, 2, 3, 4, 5);
    public static final Table GHOST_FIRE = new Table(0.5, 1, 2, 3, 4, 5);
    public static final Table NIGHTBLADE_RIFT = new Table(2, 2.5, 3, 3.5, 4, 5);
    public static final Table HOLLOW_SEEKERS = new Table(5, 6.5, 7, 8, 10, 12);
    public static final Table TELEKINESIS = new Table(8, 10, 12, 15, 16, 20);
    public static final Table PARRY = new Table(8, 10, 13, 15, 18, 20);
    public static final Table THUNDER = new Table(6, 8, 12, 16, 18, 20);
    public static final Table OVERCHARGE = new Table(6, 6, 6, 8, 12, 14);
    public static final Table METEOROID = new Table(4, 5, 6, 7, 8, 9);
    public static final Table METEOROID_BIG = new Table(8, 10, 12, 14, 16, 18);
    public static final Table METEOR_STRIKE = new Table(2.5, 3, 3.5, 4, 5, 6);
    public static final Table SKY_STAR = new Table(3, 4, 5, 6, 7, 8);
    public static final Table AURORA_CAGE = new Table(2, 2.5, 3, 3.5, 4, 5);

    private RingDamage() {
    }

    public record Table(double... levels) {
        public double at(double upgrade) {
            int l = Math.max(0, Math.min(this.levels.length - 1, (int)Math.round(upgrade)));
            return this.levels[l];
        }

        public float at(@Nullable Entity caster) {
            return (float)this.at(upgrade(caster));
        }
    }

    public static double upgrade(@Nullable Entity e) {
        return e instanceof Player player ? TheabyssModVariables.getPlayerVariables(player).ManaDamage : 0.0;
    }
}
