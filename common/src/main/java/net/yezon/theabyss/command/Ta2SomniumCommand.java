package net.yezon.theabyss.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.RegisterCommandsEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.ring.SomniumUpgrades;
import net.yezon.theabyss.skill.Skills;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class Ta2SomniumCommand {
    private Ta2SomniumCommand() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> level = Commands.literal("level")
            .then(Commands.literal("reset").executes(Ta2SomniumCommand::reset));
        for (SomniumUpgrades.Kind kind : SomniumUpgrades.Kind.values()) {
            level.then(Commands.literal(kind.id)
                .then(Commands.argument("level", IntegerArgumentType.integer(0, SomniumUpgrades.MAX))
                    .executes(context -> setLevel(context, kind))));
        }
        event.getDispatcher().register(Commands.literal("ta2")
            .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("somnium")
                .then(level)
                .then(Commands.literal("set")
                    .then(Commands.literal("infinite").executes(Ta2SomniumCommand::infinite))
                    .then(Commands.argument("percent", IntegerArgumentType.integer(0, 100)).executes(Ta2SomniumCommand::set))))
            .then(Commands.literal("debug")
                .then(Commands.literal("skill")
                    .then(Commands.literal("reset").executes(Ta2SomniumCommand::skillReset))
                    .then(Commands.literal("unlock").then(Commands.literal("all").executes(Ta2SomniumCommand::skillUnlockAll))))));
    }

    private static int skillReset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Skills.debugReset(player);
        context.getSource().sendSuccess(() -> Component.translatable("command.theabyss.skill.reset"), false);
        return 1;
    }

    private static int skillUnlockAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Skills.unlockAll(player);
        context.getSource().sendSuccess(() -> Component.translatable("command.theabyss.skill.unlock"), false);
        return 1;
    }

    private static int setLevel(CommandContext<CommandSourceStack> context, SomniumUpgrades.Kind kind) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int value = IntegerArgumentType.getInteger(context, "level");
        TheabyssModVariables.updatePlayerVariables(player, vars -> {
            SomniumUpgrades.setLevel(vars, kind, value);
            SomniumUpgrades.setPotions(vars, kind, 0);
            vars.syncPlayerVariables(player);
        });
        context.getSource().sendSuccess(() -> Component.translatable("command.theabyss.somnium.level", Component.translatable("gauntlet.theabyss.stat." + kind.id), value), false);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TheabyssModVariables.updatePlayerVariables(player, vars -> {
            for (SomniumUpgrades.Kind kind : SomniumUpgrades.Kind.values()) {
                SomniumUpgrades.setLevel(vars, kind, 0);
                SomniumUpgrades.setPotions(vars, kind, 0);
            }
            vars.syncPlayerVariables(player);
        });
        context.getSource().sendSuccess(() -> Component.translatable("command.theabyss.somnium.reset"), false);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int percent = IntegerArgumentType.getInteger(context, "percent");
        TheabyssModVariables.updatePlayerVariables(player, vars -> {
            vars.InfiniteSomnium = false;
            vars.Mana = percent;
            vars.syncPlayerVariables(player);
        });
        context.getSource().sendSuccess(() -> Component.translatable("command.theabyss.somnium.percent", percent), false);
        return 1;
    }

    private static int infinite(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TheabyssModVariables.updatePlayerVariables(player, vars -> {
            vars.InfiniteSomnium = true;
            vars.Mana = 100.0;
            vars.syncPlayerVariables(player);
        });
        context.getSource().sendSuccess(() -> Component.translatable("command.theabyss.somnium.infinite"), false);
        return 1;
    }
}
