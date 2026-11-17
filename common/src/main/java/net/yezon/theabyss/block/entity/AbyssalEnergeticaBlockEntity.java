package net.yezon.theabyss.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.block.AbyssalEnergeticaBlock;
import net.yezon.theabyss.init.TheabyssModBlockEntities;
import net.yezon.theabyss.station.StationEnergy;

public class AbyssalEnergeticaBlockEntity extends BlockEntity {
    private final NonNullList<ItemStack> energy = NonNullList.withSize(1, ItemStack.EMPTY);

    public AbyssalEnergeticaBlockEntity(BlockPos pos, BlockState state) {
        super(TheabyssModBlockEntities.ABYSSAL_ENERGETICA.get(), pos, state);
    }

    public ItemStack energy() {
        return this.energy.get(0);
    }

    public boolean charged() {
        return StationEnergy.isEnergy(this.energy());
    }

    public void setEnergy(ItemStack stack) {
        this.energy.set(0, stack);
        this.setChanged();
        if (this.level != null) {
            BlockState state = this.getBlockState();
            BlockState lit = state.setValue(AbyssalEnergeticaBlock.CHARGED, this.charged());
            this.level.setBlock(this.worldPosition, lit, 3);
            this.level.sendBlockUpdated(this.worldPosition, state, lit, 3);
        }
    }

    public void dropEnergy(BlockPos pos) {
        if (this.level != null && !this.energy().isEmpty()) {
            Containers.dropItemStack(this.level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, this.energy());
            this.energy.set(0, ItemStack.EMPTY);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.energy.set(0, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.energy, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.energy, registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithFullMetadata(registries);
    }
}
