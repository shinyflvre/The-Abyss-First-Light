package net.yezon.theabyss.events;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModSounds;
import net.yezon.theabyss.ring.RingCast;
import net.yezon.theabyss.ring.RingVisuals;

public final class ApplyRingOfNatureEvents {
    private static final double COST = 8.0;
    private static final int[][] OFFSETS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, -1}, {-1, 1}};

    private ApplyRingOfNatureEvents() {
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        RingCast cast = RingCast.start(world, entity, itemstack, COST, new Vec3(x, y, z));
        if (cast == null) {
            return;
        }
        cast.durability(TheabyssModItems.RING_OF_NATURE.get(), 1);
        if (world instanceof ServerLevel level) {
            BlockPos centre = BlockPos.containing(x, y, z);
            Player player = entity instanceof Player p ? p : null;
            List<Vec3> grown = new ArrayList<>();
            for (int[] offset : OFFSETS) {
                BlockPos pos = centre.offset(offset[0], 0, offset[1]);
                if (BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, pos, player)
                    || BoneMealItem.growWaterPlant(new ItemStack(Items.BONE_MEAL), level, pos, null)) {
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15, 0.25, 0.25, 0.25, 0.02);
                    grown.add(Vec3.atLowerCornerOf(pos));
                }
            }
            level.playSound(null, centre, TheabyssModSounds.SPELL_OF_NATURE.get(), SoundSource.NEUTRAL, 1.0F, RingVisuals.castPitch(entity.getRandom()));
            RingVisuals.nature(level, new Vec3(x, y, z), grown);
        }
        cast.charge(COST);
    }
}
