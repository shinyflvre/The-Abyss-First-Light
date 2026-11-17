package net.yezon.theabyss.platform.services;

import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.yezon.theabyss.platform.common.world.ModifiableBiomeInfo;
import net.yezon.theabyss.platform.network.IContainerFactory;
import org.jspecify.annotations.Nullable;

public interface ServerHooks {
    static ServerHooks get() {
        ServerHooks hooks = Instance.hooks;
        if (hooks == null) {
            throw new IllegalStateException("The Abyss server hooks were not installed by the loader");
        }
        return hooks;
    }

    static void install(ServerHooks hooks) {
        Instance.hooks = hooks;
    }

    boolean hasChannel(ServerGamePacketListenerImpl connection, CustomPacketPayload.Type<?> type);

    ItemStack getCraftingRemainder(ItemStack stack);

    boolean applyBonemeal(ItemStack stack, Level level, BlockPos pos, @Nullable Player player);

    <T extends AbstractContainerMenu> MenuType<T> createMenuType(IContainerFactory<T> factory, boolean openedWithExtraData);

    OptionalInt openMenu(ServerPlayer player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraDataWriter);

    OptionalInt openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos);

    ModifiableBiomeInfo.BiomeInfo biomeInfo(Biome biome);

    void setModifiedBiomeInfo(Biome biome, ModifiableBiomeInfo.BiomeInfo info);

    Holder<Attribute> swimSpeedAttribute();

    Holder<Attribute> creativeFlightAttribute();

    final class Instance {
        private static volatile @Nullable ServerHooks hooks;

        private Instance() {
        }
    }
}
