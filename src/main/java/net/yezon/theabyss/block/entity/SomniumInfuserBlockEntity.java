package net.yezon.theabyss.block.entity;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.yezon.theabyss.init.TheabyssModBlockEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.jei_recipes.InfuserRecipeTypeRecipe;
import net.yezon.theabyss.station.StationEnergy;
import net.yezon.theabyss.station.StationRecipes;
import net.yezon.theabyss.world.inventory.InfuserMenu;
import org.jspecify.annotations.Nullable;

public class SomniumInfuserBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    public static final int RESULT = 0;
    public static final int[] INPUTS = {1, 2, 3, 4};
    public static final int SOMNIUM = 5;
    public static final int ENERGY = 6;
    public static final int BREW_TIME = 200;
    public static final int OK = 0;
    public static final int NO_ENERGY = 2;
    public static final int OUTPUT_FULL = 3;

    private NonNullList<ItemStack> stacks = NonNullList.withSize(7, ItemStack.EMPTY);
    private int progress;
    private int state;

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> SomniumInfuserBlockEntity.this.progress;
                case 1 -> BREW_TIME;
                default -> SomniumInfuserBlockEntity.this.state;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                SomniumInfuserBlockEntity.this.progress = value;
            } else if (index == 2) {
                SomniumInfuserBlockEntity.this.state = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public SomniumInfuserBlockEntity(BlockPos position, BlockState state) {
        super(TheabyssModBlockEntities.SOMNIUM_INFUSER.get(), position, state);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState blockState, SomniumInfuserBlockEntity be) {
        int before = be.progress;
        int beforeState = be.state;
        be.state = OK;
        be.keepOneEnergy(level, pos);
        ItemStack result = be.result(level);
        if (result.isEmpty()) {
            be.progress = 0;
        } else if (!StationEnergy.available(be.stacks.get(ENERGY))) {
            be.state = NO_ENERGY;
        } else if (!be.fits(result)) {
            be.state = OUTPUT_FULL;
        } else if (++be.progress >= BREW_TIME) {
            be.progress = 0;
            for (int i : INPUTS) {
                be.useOne(level, pos, i);
            }
            be.useOne(level, pos, SOMNIUM);
            StationEnergy.pay(level, be, ENERGY);
            ItemStack out = be.stacks.get(RESULT);
            if (out.isEmpty()) {
                be.stacks.set(RESULT, result.copy());
            } else {
                out.grow(result.getCount());
            }
            SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:somnium_infuser_bubble"));
            if (sound != null) {
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, sound, SoundSource.BLOCKS, 0.8F, 1.0F);
            }
        }
        if (be.progress != before || be.state != beforeState) {
            be.setChanged();
        }
    }

    private ItemStack result(ServerLevel level) {
        ItemStack[] inputs = new ItemStack[INPUTS.length];
        for (int i = 0; i < INPUTS.length; i++) {
            inputs[i] = this.stacks.get(INPUTS[i]);
        }
        InfuserRecipeTypeRecipe recipe = StationRecipes.infuser(level, inputs, this.stacks.get(SOMNIUM));
        return recipe == null ? ItemStack.EMPTY : recipe.getResultItem(level.registryAccess());
    }

    private boolean fits(ItemStack result) {
        ItemStack out = this.stacks.get(RESULT);
        return out.isEmpty() || ItemStack.isSameItemSameComponents(out, result) && out.getCount() + result.getCount() <= out.getMaxStackSize();
    }

    private void useOne(ServerLevel level, BlockPos pos, int index) {
        ItemStack stack = this.stacks.get(index);
        if (stack.isEmpty()) {
            return;
        }
        ItemStackTemplate template = stack.getCraftingRemainder();
        ItemStack remainder = template == null ? ItemStack.EMPTY : template.create();
        stack.shrink(1);
        if (stack.isEmpty()) {
            this.stacks.set(index, remainder);
        } else if (!remainder.isEmpty()) {
            net.minecraft.world.Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, remainder);
        }
    }

    private void keepOneEnergy(ServerLevel level, BlockPos pos) {
        ItemStack energy = this.stacks.get(ENERGY);
        if (energy.getCount() > 1) {
            net.minecraft.world.Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, energy.split(energy.getCount() - 1));
            this.setChanged();
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        if (!this.tryLoadLootTable(input)) {
            this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        }
        ContainerHelper.loadAllItems(input, this.stacks);
        this.progress = input.getIntOr("progress", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.trySaveLootTable(output)) {
            ContainerHelper.saveAllItems(output, this.stacks);
        }
        output.putInt("progress", this.progress);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithFullMetadata(registries);
    }

    @Override
    public int getContainerSize() {
        return this.stacks.size();
    }

    @Override
    public boolean isEmpty() {
        return this.stacks.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.theabyss.somnium_infuser");
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        RegistryFriendlyByteBuf extraData = new RegistryFriendlyByteBuf(Unpooled.buffer(), inventory.player.registryAccess());
        extraData.writeBlockPos(this.worldPosition);
        return new InfuserMenu(id, inventory, extraData);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.theabyss.somnium_infuser");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == SOMNIUM) {
            return stack.is(TheabyssModItems.SOMNIUM.get());
        }
        if (index == ENERGY) {
            return StationEnergy.isEnergy(stack) && this.stacks.get(ENERGY).isEmpty();
        }
        return index >= 1 && index <= 4;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? INPUTS : side == Direction.DOWN ? new int[] {RESULT} : new int[] {SOMNIUM, ENERGY};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == RESULT;
    }
}
