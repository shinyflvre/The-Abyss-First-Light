package net.yezon.theabyss.client.debug;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class DebugClientCommands {
    private DebugClientCommands() {
    }

    @SubscribeEvent
    public static void onRegister(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("ta2")
            .then(Commands.literal("debug")
                .then(Commands.literal("ores")
                    .then(Commands.argument("ore", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(OreHighlighter.suggestions(false), builder))
                        .executes(context -> OreHighlighter.command(false, StringArgumentType.getString(context, "ore")))))
                .then(Commands.literal("crystals")
                    .then(Commands.argument("crystal", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(OreHighlighter.suggestions(true), builder))
                        .executes(context -> OreHighlighter.command(true, StringArgumentType.getString(context, "crystal")))))
                .then(Commands.literal("biomefog").executes(context -> {
                    Minecraft mc = Minecraft.getInstance();
                    mc.schedule(() -> {
                        if (mc.level != null && mc.player != null) {
                            mc.gui.setScreen(new BiomeColorScreen(BiomeColors.biomeAtCamera(mc)));
                        }
                    });
                    return 1;
                }))));
    }

    @SubscribeEvent
    public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        BiomeColors.applySaved(Minecraft.getInstance());
    }
}
