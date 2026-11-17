package net.yezon.theabyss.fabric.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.yezon.theabyss.platform.client.event.RegisterClientCommandsEvent;
import net.yezon.theabyss.platform.event.AbyssEventBus;

public final class FabricClientCommands {
    private FabricClientCommands() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> {
            CommandDispatcher<CommandSourceStack> own = new CommandDispatcher<>();
            AbyssEventBus.post(new RegisterClientCommandsEvent() {
                @Override
                public CommandDispatcher<CommandSourceStack> getDispatcher() {
                    return own;
                }
            });
            for (CommandNode<CommandSourceStack> child : own.getRoot().getChildren()) {
                if (child instanceof LiteralCommandNode<CommandSourceStack> literal) {
                    dispatcher.register((LiteralArgumentBuilder<FabricClientCommandSource>)convert(own, literal));
                }
            }
        });
    }

    private static ArgumentBuilder<FabricClientCommandSource, ?> convert(CommandDispatcher<CommandSourceStack> own, CommandNode<CommandSourceStack> node) {
        ArgumentBuilder<FabricClientCommandSource, ?> builder;
        if (node instanceof ArgumentCommandNode<CommandSourceStack, ?> argument) {
            RequiredArgumentBuilder<FabricClientCommandSource, ?> required = RequiredArgumentBuilder.argument(argument.getName(), argument.getType());
            if (argument.getCustomSuggestions() != null) {
                required.suggests((context, suggestions) -> suggest(own, context, suggestions));
            }
            builder = required;
        } else {
            builder = LiteralArgumentBuilder.literal(node.getName());
        }
        if (node.getCommand() != null) {
            builder.executes(context -> own.execute(context.getInput(), source(context.getSource())));
        }
        for (CommandNode<CommandSourceStack> child : node.getChildren()) {
            builder.then(convert(own, child));
        }
        return builder;
    }

    private static CompletableFuture<Suggestions> suggest(CommandDispatcher<CommandSourceStack> own, CommandContext<FabricClientCommandSource> context,
        SuggestionsBuilder suggestions) {
        ParseResults<CommandSourceStack> parse = own.parse(suggestions.getInput(), source(context.getSource()));
        return own.getCompletionSuggestions(parse);
    }

    private static CommandSourceStack source(FabricClientCommandSource source) {
        Player player = source.getPlayer();
        return new CommandSourceStack(new CommandSource() {
            @Override
            public void sendSystemMessage(Component message) {
                source.sendFeedback(message);
            }

            @Override
            public boolean acceptsSuccess() {
                return true;
            }

            @Override
            public boolean acceptsFailure() {
                return true;
            }

            @Override
            public boolean shouldInformAdmins() {
                return false;
            }
        }, source.getPosition(), source.getRotation(), null, permissionLevel(player), player.getName().getString(), player.getDisplayName(), null, player);
    }

    private static int permissionLevel(Player player) {
        for (int level = 4; level > 0; level--) {
            if (player.hasPermissions(level)) {
                return level;
            }
        }
        return 0;
    }
}
