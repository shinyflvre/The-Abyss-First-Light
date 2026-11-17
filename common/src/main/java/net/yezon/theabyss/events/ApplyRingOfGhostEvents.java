package net.yezon.theabyss.events;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModMobEffects;
import net.yezon.theabyss.ring.RingCast;

public class ApplyRingOfGhostEvents {
    private static final ResourceKey<Level> POCKET_DIMENSION = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("theabyss:pocket_dimension"));

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        if (entity == null) {
            return;
        }
        if (entity.level().dimension() == POCKET_DIMENSION) {
            if (entity instanceof Player player && !player.level().isClientSide()) {
                player.displayClientMessage(Component.translatable("ring.theabyss.not_here"), true);
            }
            return;
        }
        RingCast cast = RingCast.start(world, entity, itemstack, 95.0, new Vec3(x, y, z));
        if (cast == null) {
            return;
        }
        if (cast.server() != null && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(TheabyssModMobEffects.GHOST.getDelegate(), 1000, 0));
            net.yezon.theabyss.ring.fx.SpellCircle.at(cast.server(), net.yezon.theabyss.ring.fx.SpellCircle.RUNES, living.position(), 0xCFE8FF, 2.0F, 30);
        }
        cast.durability(TheabyssModItems.RING_OF_GHOSTS.get(), 4).cooldown(10000).sound("theabyss:spell_of_ghost").charge(95.0);
    }
}
