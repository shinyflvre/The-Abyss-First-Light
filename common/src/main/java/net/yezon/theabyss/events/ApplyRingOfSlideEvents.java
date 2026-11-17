package net.yezon.theabyss.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.ring.RingCast;
import net.yezon.theabyss.ring.RingVisuals;

public final class ApplyRingOfSlideEvents {
    private static final double COST = 10.0;
    private static final int COOLDOWN = 1000;
    private static final int GLIDE_TICKS = 999999;

    private ApplyRingOfSlideEvents() {
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        if (entity == null) {
            return;
        }
        if (entity.onGround()) {
            if (entity instanceof Player player && !player.level().isClientSide()) {
                player.displayClientMessage(Component.translatable("ring.theabyss.slide.not_in_air"), true);
            }
            return;
        }
        RingCast cast = RingCast.start(world, entity, itemstack, COST, new Vec3(x, y, z));
        if (cast == null) {
            return;
        }
        cast.durability(TheabyssModItems.RING_OF_SLIDE.get(), 1);
        if (entity instanceof LivingEntity living && !living.level().isClientSide()) {
            living.addEffect(new MobEffectInstance(TheabyssModMobEffects.FLYING.getDelegate(), GLIDE_TICKS, 0, true, false, true));
        }
        cast.cooldown(COOLDOWN).sound("theabyss:spell_of_misc");
        if (world instanceof ServerLevel level) {
            RingVisuals.slideStart(level, entity);
        }
        cast.charge(COST);
    }
}
