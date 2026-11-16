package net.yezon.theabyss.block.entity;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.init.TheabyssModBlockEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.world.inventory.ExtractorMenu;

public class SomniumExtractorBlockEntity extends StationMachineBlockEntity {
    public static final int DURATION = 40;

    public SomniumExtractorBlockEntity(BlockPos position, BlockState state) {
        super(TheabyssModBlockEntities.SOMNIUM_EXTRACTOR.get(), position, state);
    }

    @Override
    public int duration() {
        return DURATION;
    }

    @Override
    public boolean isInput(ItemStack stack) {
        return stack.is(TheabyssModItems.LORAN.get());
    }

    @Override
    protected ItemStack result(ServerLevel level, ItemStack input) {
        return this.isInput(input) ? new ItemStack(TheabyssModItems.SOMNIUM.get()) : ItemStack.EMPTY;
    }

    @Override
    protected void onFinished(ServerLevel level) {
        BlockPos p = this.worldPosition;
        level.playSound(null, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.5F, 1.4F);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.theabyss.somnium_extractor");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        RegistryFriendlyByteBuf extraData = new RegistryFriendlyByteBuf(Unpooled.buffer(), inventory.player.registryAccess());
        extraData.writeBlockPos(this.worldPosition);
        return new ExtractorMenu(id, inventory, extraData);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.theabyss.somnium_extractor");
    }
}
