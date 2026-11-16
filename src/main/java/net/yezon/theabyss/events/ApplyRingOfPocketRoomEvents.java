package net.yezon.theabyss.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.pocket.PocketBox;
import net.yezon.theabyss.pocket.PocketCells;
import net.yezon.theabyss.util.FallGuard;
import net.yezon.theabyss.ring.SomniumUpgrades;

public class ApplyRingOfPocketRoomEvents {
    public static final int TRAVEL_TICKS = 16;
    private static final String RETURN_KEY = "theabyss_pocket_return";

    public static boolean execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        if (!(entity instanceof ServerPlayer player) || !(world instanceof ServerLevel level)) {
            return false;
        }
        boolean inPocket = level.dimension() == PocketCells.DIMENSION;

        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(entity);
        if (vars.Mana < 50.0 * SomniumUpgrades.costFactor(vars)) {
            player.sendOverlayMessage(Component.translatable("ring.theabyss.low_energy"));
            return false;
        }
        if (itemstack.getItem() != TheabyssModItems.RING_OF_POCKET_BOX.get()) {
            return false;
        }
        player.getCooldowns().addCooldown(itemstack, (int)(500.0 * vars.ManaCooldown));
        net.yezon.theabyss.ring.RingWear.wear(itemstack, level, entity, 1);

        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:spell_of_teleport"));
        if (sound != null) {
            level.playSound((Player)null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
        net.yezon.theabyss.ring.RingVisuals.pocketPortal(level, entity.position());
        net.yezon.theabyss.network.PocketFoldMessage.send(player);
        TheabyssMod.queueServerWork(TRAVEL_TICKS + 2, () -> {
            if (entity.level() instanceof ServerLevel arrival) {
                net.yezon.theabyss.ring.RingVisuals.pocketPortal(arrival, entity.position());
            }
        });

        if (inPocket) {
            ServerLevel home = returnLevel(player);
            Vec3 back = new Vec3(Math.round(vars.SaveX), Math.round(vars.SaveY), Math.round(vars.SaveZ));
            TheabyssMod.queueServerWork(TRAVEL_TICKS, () -> {
                if (!player.isRemoved() && player.level() == level) {
                    FallGuard.protect(player, 60);
                    player.teleport(new TeleportTransition(home, back, Vec3.ZERO, player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING));
                }
            });
            return true;
        }
        player.getPersistentData().putString(RETURN_KEY, level.dimension().identifier().toString());

        double mana = vars.Mana - 50.0 * SomniumUpgrades.costFactor(vars);
        double saveX = Math.round(entity.getX());
        double saveY = Math.round(entity.getY());
        double saveZ = Math.round(entity.getZ());
        Vec3 arrival = PocketCells.arrival(PocketCells.get(level.getServer()).cellOf(player.getUUID()));
        TheabyssModVariables.updatePlayerVariables(entity, capability -> {
            capability.Mana = mana;
            capability.SaveX = saveX;
            capability.SaveY = saveY;
            capability.SaveZ = saveZ;
            capability.PocketDimension = true;
            capability.PocketPlayerX = arrival.x;
            capability.PocketPlayerY = arrival.y;
            capability.PocketPlayerZ = arrival.z;
            capability.syncPlayerVariables(entity);
        });
        TheabyssMod.queueServerWork(TRAVEL_TICKS, () -> {
            if (!player.isRemoved() && player.level() == level) {
                PocketBox.enter(player);
            }
        });
        return true;
    }

    private static ServerLevel returnLevel(ServerPlayer player) {
        String saved = player.getPersistentData().getStringOr(RETURN_KEY, "");
        Identifier id = saved.isEmpty() ? null : Identifier.tryParse(saved);
        if (id != null) {
            ServerLevel level = player.level().getServer().getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, id));
            if (level != null && level.dimension() != PocketCells.DIMENSION) {
                return level;
            }
        }
        return player.level().getServer().getLevel(Level.OVERWORLD);
    }
}
