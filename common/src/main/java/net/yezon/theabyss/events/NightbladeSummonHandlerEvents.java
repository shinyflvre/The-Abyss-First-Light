package net.yezon.theabyss.events;

import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModBlocks;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;

@EventBusSubscriber
public class NightbladeSummonHandlerEvents {
    private static final double[][] PILLARS = {
        {6, 5, 0}, {-6, 5, 0}, {0, 5, 6}, {0, 5, -6}, {3, 4, -3}, {3, 4, 3}, {-3, 4, 3}, {-3, 4, -3}
    };

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() == event.getEntity().getUsedItemHand()) {
            execute(event, event.getLevel(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getEntity());
        }
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        execute(null, world, x, y, z, entity);
    }

    private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity != null) {
            if (mainHand(entity).getItem() == TheabyssModItems.EYE_OF_ABYSS.get()
                && TheabyssModBlocks.NIGHT_ALTAR.get() == world.getBlockState(BlockPos.containing(x, y, z)).getBlock()) {
                if (allPillarsIgnited(world, x, y, z)) {
                    mainHand(entity).shrink(1);
                    world.setBlock(BlockPos.containing(x, y, z), TheabyssModBlocks.NIGHT_ALTAR_ACTIVATED.get().defaultBlockState(), 3);
                    playSound(world, x, y, z, "entity.ender_eye.death");

                    for (int i = 0; i < PILLARS.length; i++) {
                        double[] pillar = PILLARS[i];
                        TheabyssMod.queueServerWork(20 * (i + 1), () -> {
                            world.setBlock(
                                BlockPos.containing(x + pillar[0], y + pillar[1], z + pillar[2]), TheabyssModBlocks.STONE_BRICK_PILLAR.get().defaultBlockState(), 3
                            );
                            visualLightning(world, x, y, z);
                        });
                    }

                    TheabyssMod.queueServerWork(180, () -> {
                        world.setBlock(BlockPos.containing(x, y, z), TheabyssModBlocks.NIGHT_ALTAR.get().defaultBlockState(), 3);
                        visualLightning(world, x, y, z);
                        if (world instanceof ServerLevel _level) {
                            Entity entityToSpawn = TheabyssModEntities.NIGHTBLADE_BOSS.get().spawn(_level, BlockPos.containing(x, y, z), EntitySpawnReason.MOB_SUMMONED);
                            if (entityToSpawn != null) {
                                entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                            }
                        }
                    });

                    if (entity instanceof LivingEntity _entity) {
                        _entity.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
                    }
                } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
                    _player.sendOverlayMessage(Component.translatable("message.theabyss.nightblade_summon.fails"));
                }
            }

            if (mainHand(entity).getItem() == TheabyssModItems.SOUL_HEART.get()
                && TheabyssModBlocks.STONE_BRICK_PILLAR.get() == world.getBlockState(BlockPos.containing(x, y, z)).getBlock()) {
                mainHand(entity).shrink(1);
                world.setBlock(BlockPos.containing(x, y, z), TheabyssModBlocks.STONE_BRICK_PILLAR_IGNITED.get().defaultBlockState(), 3);
                if (entity instanceof net.minecraft.world.entity.player.Player chronicler) {
                    net.yezon.theabyss.lore.Chronicles.note(chronicler, net.yezon.theabyss.lore.Chronicles.SOUL_HEART);
                }
                playSound(world, x, y, z, "block.fire.extinguish");
                if (entity instanceof LivingEntity _entity) {
                    _entity.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
                }
            }
        }
    }

    private static ItemStack mainHand(Entity entity) {
        return entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY;
    }

    private static boolean allPillarsIgnited(LevelAccessor world, double x, double y, double z) {
        for (double[] pillar : PILLARS) {
            if (world.getBlockState(BlockPos.containing(x + pillar[0], y + pillar[1], z + pillar[2])).getBlock()
                != TheabyssModBlocks.STONE_BRICK_PILLAR_IGNITED.get()) {
                return false;
            }
        }
        return true;
    }

    private static void visualLightning(LevelAccessor world, double x, double y, double z) {
        if (world instanceof ServerLevel _level) {
            LightningBolt entityToSpawn = EntityTypes.LIGHTNING_BOLT.create(_level, EntitySpawnReason.TRIGGERED);
            if (entityToSpawn != null) {
                entityToSpawn.snapTo(Vec3.atBottomCenterOf(BlockPos.containing(x, y, z)));
                entityToSpawn.setVisualOnly(true);
                _level.addFreshEntity(entityToSpawn);
            }
        }
    }

    private static void playSound(LevelAccessor world, double x, double y, double z, String sound) {
        if (world instanceof Level _level) {
            if (!_level.isClientSide()) {
                _level.playSound(
                    (Player)null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(sound)), SoundSource.NEUTRAL, 1.0F, 1.0F
                );
            } else {
                _level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(sound)), SoundSource.NEUTRAL, 1.0F, 1.0F, false);
            }
        }
    }
}
