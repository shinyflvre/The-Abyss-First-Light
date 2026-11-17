package net.yezon.theabyss.fabric.server;

import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.yezon.theabyss.platform.common.world.ModifiableBiomeInfo;
import net.yezon.theabyss.platform.network.IContainerFactory;
import net.yezon.theabyss.platform.services.ServerHooks;
import org.jspecify.annotations.Nullable;

public final class FabricServerHooks implements ServerHooks {
    private static final Set<MenuType<?>> EXTENDED_TYPES = Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
    private static @Nullable Field climateSettings;

    @Override
    public boolean hasChannel(ServerGamePacketListenerImpl connection, CustomPacketPayload.Type<?> type) {
        return ServerPlayNetworking.canSend(connection, type);
    }

    @Override
    public ItemStack getCraftingRemainder(ItemStack stack) {
        return stack.getRecipeRemainder();
    }

    @Override
    public boolean applyBonemeal(ItemStack stack, Level level, BlockPos pos, @Nullable Player player) {
        return BoneMealItem.growCrop(stack, level, pos);
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createMenuType(IContainerFactory<T> factory, boolean openedWithExtraData) {
        if (!openedWithExtraData) {
            return new MenuType<>((id, inventory) -> factory.create(id, inventory, null), FeatureFlags.DEFAULT_FLAGS);
        }
        MenuType<T> type = new ExtendedScreenHandlerType<>((id, inventory, data) -> factory.create(id, inventory, data.map(bytes -> buffer(inventory, bytes)).orElse(null)),
            ByteBufCodecs.optional(ByteBufCodecs.BYTE_ARRAY));
        EXTENDED_TYPES.add(type);
        return type;
    }

    private static RegistryFriendlyByteBuf buffer(Inventory inventory, byte[] data) {
        return new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), inventory.player.registryAccess());
    }

    public static boolean isExtendedType(MenuType<?> type) {
        return EXTENDED_TYPES.contains(type);
    }

    public static MenuProvider withoutExtraData(MenuProvider provider) {
        return withData(provider, Optional.empty());
    }

    private static MenuProvider withData(MenuProvider provider, Optional<byte[]> data) {
        return new ExtendedScreenHandlerFactory<Optional<byte[]>>() {
            @Override
            public Optional<byte[]> getScreenOpeningData(ServerPlayer opener) {
                return data;
            }

            @Override
            public Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Override
            public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player opener) {
                return provider.createMenu(containerId, inventory, opener);
            }

            @Override
            public boolean shouldCloseCurrentScreen() {
                return provider.shouldCloseCurrentScreen();
            }
        };
    }

    @Override
    public OptionalInt openMenu(ServerPlayer player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        byte[] data;
        try {
            extraDataWriter.accept(buf);
            data = new byte[buf.readableBytes()];
            buf.readBytes(data);
        } finally {
            buf.release();
        }
        return player.openMenu(withData(provider, Optional.of(data)));
    }

    @Override
    public OptionalInt openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        return this.openMenu(player, provider, buf -> buf.writeBlockPos(pos));
    }

    @Override
    public ModifiableBiomeInfo.BiomeInfo biomeInfo(Biome biome) {
        return new ModifiableBiomeInfo.BiomeInfo(climateSettings(biome), biome.getSpecialEffects(), biome.getGenerationSettings(), biome.getMobSettings());
    }

    private static Object climateSettings(Biome biome) {
        try {
            Field field = climateSettings;
            if (field == null) {
                field = climateField();
                field.setAccessible(true);
                climateSettings = field;
            }
            return field.get(biome);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot read the climate settings of " + biome, e);
        }
    }

    private static Field climateField() throws NoSuchFieldException {
        for (Field field : Biome.class.getDeclaredFields()) {
            Class<?> type = field.getType();
            if (!Modifier.isStatic(field.getModifiers()) && type.isRecord() && type.getEnclosingClass() == Biome.class) {
                RecordComponent[] parts = type.getRecordComponents();
                if (parts.length == 4 && parts[2].getType() == Biome.TemperatureModifier.class) {
                    return field;
                }
            }
        }
        throw new NoSuchFieldException("climate settings of " + Biome.class.getName());
    }

    @Override
    public void setModifiedBiomeInfo(Biome biome, ModifiableBiomeInfo.BiomeInfo info) {
    }

    @Override
    public Holder<Attribute> swimSpeedAttribute() {
        return FabricAttributes.SWIM_SPEED;
    }

    @Override
    public Holder<Attribute> creativeFlightAttribute() {
        return FabricAttributes.CREATIVE_FLIGHT;
    }
}
