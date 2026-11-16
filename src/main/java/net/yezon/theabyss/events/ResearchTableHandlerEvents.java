package net.yezon.theabyss.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.station.CrystalResearch;
import net.yezon.theabyss.world.inventory.ResearchMenu;

public class ResearchTableHandlerEvents {
    public static final int LEVELS = 5;

    public static void execute(Entity entity) {
        if (!(entity instanceof ServerPlayer player) || !(player.containerMenu instanceof ResearchMenu menu)) {
            return;
        }
        ItemStack stack = menu.slots.get(0).getItem();
        CrystalResearch.Crystal crystal = CrystalResearch.of(stack);
        if (crystal == null) {
            player.sendOverlayMessage(Component.translatable("note.theabyss.crystal_not_ore"));
            return;
        }
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        if (crystal.known().apply(vars)) {
            player.sendSystemMessage(Component.translatable("note.theabyss.crystal_researched"));
            return;
        }
        if (player.experienceLevel < LEVELS && !player.isCreative()) {
            player.sendSystemMessage(Component.translatable("note.theabyss.crystal_no_xp"));
            return;
        }
        TheabyssModVariables.updatePlayerVariables(player, capability -> {
            crystal.set().accept(capability, true);
            capability.syncPlayerVariables(player);
        });
        if (!player.isCreative()) {
            player.giveExperienceLevels(-LEVELS);
            player.giveExperiencePoints(20);
        }
        player.sendSystemMessage(Component.translatable("note.theabyss.crystal_research"));
        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.2F);
    }
}
