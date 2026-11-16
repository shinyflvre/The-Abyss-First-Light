package net.yezon.theabyss.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.events.ManaboostCommandEvents;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public class ManaboostCommand {
    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher()
            .register(
                Commands.literal("manaboost")
                    .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                    .then(Commands.argument("arguments", StringArgumentType.greedyString()).executes(ManaboostCommand::execute))
                    .executes(ManaboostCommand::execute)
            );
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Entity entity = source.getEntity();
        if (entity == null) {
            entity = FakePlayerFactory.getMinecraft(source.getLevel());
        }

        ManaboostCommandEvents.execute(entity);
        return 0;
    }
}
