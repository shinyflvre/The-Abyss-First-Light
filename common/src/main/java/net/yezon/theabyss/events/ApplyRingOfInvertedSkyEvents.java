package net.yezon.theabyss.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.ring.InvertedSky;
import net.yezon.theabyss.ring.RingCast;

public class ApplyRingOfInvertedSkyEvents {
    public static void execute(LevelAccessor world, Entity entity, ItemStack itemstack) {
        if (entity == null) {
            return;
        }
        RingCast cast = RingCast.start(world, entity, itemstack, InvertedSky.COST);
        if (cast == null) {
            return;
        }
        ServerLevel level = cast.server();
        if (level != null && entity instanceof LivingEntity caster) {
            InvertedSky.cast(level, caster, cast.vars());
        }
        cast.durability(TheabyssModItems.RING_OF_INVERTED_SKY.get(), 1).cooldown(InvertedSky.COOLDOWN).charge(InvertedSky.COST);
    }
}
