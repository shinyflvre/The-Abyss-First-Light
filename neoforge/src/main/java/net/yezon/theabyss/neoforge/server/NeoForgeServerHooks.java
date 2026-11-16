package net.yezon.theabyss.neoforge.server;

import java.lang.reflect.Field;
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
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.yezon.theabyss.platform.common.world.ModifiableBiomeInfo;
import net.yezon.theabyss.platform.network.IContainerFactory;
import net.yezon.theabyss.platform.services.ServerHooks;
import org.jspecify.annotations.Nullable;

public final class NeoForgeServerHooks implements ServerHooks {
    @Override
    public boolean hasChannel(ServerGamePacketListenerImpl connection, CustomPacketPayload.Type<?> type) {
        return connection.hasChannel(type);
    }

    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(ItemStack stack) {
        return stack.getCraftingRemainder();
    }

    @Override
    public boolean applyBonemeal(ItemStack stack, Level level, BlockPos pos, @Nullable Player player) {
        return BoneMealItem.applyBonemeal(stack, level, pos, player);
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createMenuType(IContainerFactory<T> factory, boolean openedWithExtraData) {
        return IMenuTypeExtension.create(factory::create);
    }

    @Override
    public OptionalInt openMenu(ServerPlayer player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        return player.openMenu(provider, extraDataWriter);
    }

    @Override
    public OptionalInt openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        return player.openMenu(provider, pos);
    }

    @Override
    public ModifiableBiomeInfo.BiomeInfo biomeInfo(Biome biome) {
        net.neoforged.neoforge.common.world.ModifiableBiomeInfo.BiomeInfo info = biome.modifiableBiomeInfo().get();
        return new ModifiableBiomeInfo.BiomeInfo(info.climateSettings(), info.attributes(), info.effects(), info.generationSettings());
    }

    @Override
    public void setModifiedBiomeInfo(Biome biome, ModifiableBiomeInfo.BiomeInfo info) {
        net.neoforged.neoforge.common.world.ModifiableBiomeInfo.BiomeInfo modified = new net.neoforged.neoforge.common.world.ModifiableBiomeInfo.BiomeInfo(
            (Biome.ClimateSettings)info.climateSettings(), info.attributes(), info.effects(), info.generationSettings());
        try {
            Field field = net.neoforged.neoforge.common.world.ModifiableBiomeInfo.class.getDeclaredField("modifiedBiomeInfo");
            field.setAccessible(true);
            field.set(biome.modifiableBiomeInfo(), modified);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot change the biome info of " + biome, e);
        }
    }

    @Override
    public Holder<Attribute> swimSpeedAttribute() {
        return NeoForgeMod.SWIM_SPEED;
    }

    @Override
    public Holder<Attribute> creativeFlightAttribute() {
        return NeoForgeMod.CREATIVE_FLIGHT;
    }
}
