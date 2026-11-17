package net.yezon.theabyss.fabric.event;

import com.mojang.brigadier.CommandDispatcher;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.event.BlockEntityTypeAddBlocksEvent;
import net.yezon.theabyss.platform.event.OnDatapackSyncEvent;
import net.yezon.theabyss.platform.event.RegisterCommandsEvent;
import net.yezon.theabyss.platform.event.TagsUpdatedEvent;
import net.yezon.theabyss.platform.event.VanillaGameEvent;
import net.yezon.theabyss.platform.event.server.ServerStoppedEvent;
import net.yezon.theabyss.platform.event.server.ServerStoppingEvent;
import net.yezon.theabyss.platform.event.tick.EntityTickEvent;
import net.yezon.theabyss.platform.event.tick.LevelTickEvent;
import net.yezon.theabyss.platform.event.tick.PlayerTickEvent;
import net.yezon.theabyss.platform.event.tick.ServerTickEvent;
import net.yezon.theabyss.platform.registries.RegisterEvent;
import org.jspecify.annotations.Nullable;

public final class ServerFabricEvents {
    private ServerFabricEvents() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> registerCommands(dispatcher));
        postRegisterEvent();
        postBlockEntityTypeAddBlocks();
    }

    public static void registerClient() {
        ServerFabricEventsClient.register();
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (AbyssEventBus.hasListeners(RegisterCommandsEvent.class)) {
            AbyssEventBus.post(new RegisterCommandsEvent() {
                @Override
                public CommandDispatcher<CommandSourceStack> getDispatcher() {
                    return dispatcher;
                }
            });
        }
    }

    private static void postRegisterEvent() {
        if (AbyssEventBus.hasListeners(RegisterEvent.class)) {
            AbyssEventBus.post(new RegisterEvent() {
                @Override
                public <T> void register(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation id, Supplier<T> value) {
                    registerValue(registryKey, id, value);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void registerValue(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation id, Supplier<T> value) {
        Registry<T> registry = (Registry<T>)BuiltInRegistries.REGISTRY.get(registryKey.location());
        if (registry == null) {
            throw new IllegalStateException("Unknown registry " + registryKey.location() + " for " + id);
        }
        Registry.register(registry, id, value.get());
    }

    private static void postBlockEntityTypeAddBlocks() {
        if (AbyssEventBus.hasListeners(BlockEntityTypeAddBlocksEvent.class)) {
            AbyssEventBus.post(new BlockEntityTypeAddBlocksEvent() {
                @Override
                public void modify(BlockEntityType<?> type, Block... blocks) {
                    for (Block block : blocks) {
                        type.addSupportedBlock(block);
                    }
                }
            });
        }
    }

    public static void serverTickPre() {
        if (AbyssEventBus.hasListeners(ServerTickEvent.Pre.class)) {
            AbyssEventBus.post(new ServerTickEvent.Pre());
        }
    }

    public static void serverTickPost(MinecraftServer server) {
        if (AbyssEventBus.hasListeners(ServerTickEvent.Post.class)) {
            AbyssEventBus.post(new ServerTickEvent.Post(server));
        }
    }

    public static void levelTickPost(Level level) {
        if (AbyssEventBus.hasListeners(LevelTickEvent.Post.class)) {
            AbyssEventBus.post(new LevelTickEvent.Post(level));
        }
    }

    public static void playerTickPost(Player player) {
        if (AbyssEventBus.hasListeners(PlayerTickEvent.Post.class)) {
            AbyssEventBus.post(new PlayerTickEvent.Post(player));
        }
    }

    public static boolean entityTickPre(Entity entity) {
        return AbyssEventBus.hasListeners(EntityTickEvent.Pre.class) && AbyssEventBus.post(new EntityTickEvent.Pre(entity)).isCanceled();
    }

    public static void entityTickPost(Entity entity) {
        if (AbyssEventBus.hasListeners(EntityTickEvent.Post.class)) {
            AbyssEventBus.post(new EntityTickEvent.Post(entity));
        }
    }

    public static void serverStopping(MinecraftServer server) {
        if (AbyssEventBus.hasListeners(ServerStoppingEvent.class)) {
            AbyssEventBus.post(new ServerStoppingEvent(server));
        }
    }

    public static void serverStopped() {
        if (AbyssEventBus.hasListeners(ServerStoppedEvent.class)) {
            AbyssEventBus.post(new ServerStoppedEvent());
        }
    }

    public static boolean vanillaGameEvent(@Nullable Entity cause) {
        return AbyssEventBus.hasListeners(VanillaGameEvent.class) && AbyssEventBus.post(new VanillaGameEvent(cause)).isCanceled();
    }

    public static void tagsUpdated() {
        if (AbyssEventBus.hasListeners(TagsUpdatedEvent.class)) {
            AbyssEventBus.post(new TagsUpdatedEvent());
        }
    }

    public static void datapackSync(PlayerList playerList, @Nullable ServerPlayer player) {
        if (!AbyssEventBus.hasListeners(OnDatapackSyncEvent.class)) {
            return;
        }
        AbyssEventBus.post(new OnDatapackSyncEvent() {
            @Override
            public PlayerList getPlayerList() {
                return playerList;
            }

            @Override
            public @Nullable ServerPlayer getPlayer() {
                return player;
            }
        });
    }
}
