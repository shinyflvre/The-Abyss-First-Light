package net.yezon.theabyss.block.entity;

import io.netty.buffer.Unpooled;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.yezon.theabyss.init.TheabyssModBlockEntities;
import net.yezon.theabyss.jei_recipes.CrystalCutterRecipeTypeRecipe;
import net.yezon.theabyss.network.TheabyssModVariables;
import net.yezon.theabyss.station.CrystalResearch;
import net.yezon.theabyss.station.StationRecipes;
import net.yezon.theabyss.world.inventory.CutterMenu;

public class CrystalCutterBlockEntity extends StationMachineBlockEntity {
    public static final int DURATION = 40;
    public static final float CHANCE = 0.3F;
    private final Set<String> researched = new HashSet<>();

    public CrystalCutterBlockEntity(BlockPos position, BlockState state) {
        super(TheabyssModBlockEntities.CRYSTAL_CUTTER.get(), position, state);
    }

    public void learnFrom(Player player) {
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        boolean changed = false;
        for (CrystalResearch.Crystal c : CrystalResearch.ALL) {
            if (c.known().apply(vars)) {
                changed |= this.researched.add(c.id());
            }
        }
        if (changed) {
            this.setChanged();
        }
    }

    @Override
    public int duration() {
        return DURATION;
    }

    @Override
    public boolean isInput(ItemStack stack) {
        return CrystalResearch.of(stack) != null;
    }

    @Override
    protected ItemStack result(ServerLevel level, ItemStack input) {
        CrystalCutterRecipeTypeRecipe recipe = StationRecipes.cutter(level, input);
        if (recipe == null) {
            return ItemStack.EMPTY;
        }
        CrystalResearch.Crystal crystal = CrystalResearch.of(input);
        if (crystal != null && !this.researched.contains(crystal.id())) {
            this.state = NEEDS_RESEARCH;
            return ItemStack.EMPTY;
        }
        return recipe.getResultItem(level.registryAccess());
    }

    @Override
    protected ItemStack finish(ServerLevel level, ItemStack result) {
        return level.getRandom().nextFloat() < CHANCE ? result.copy() : ItemStack.EMPTY;
    }

    @Override
    protected void onFinished(ServerLevel level) {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:crystal_break"));
        if (sound != null) {
            BlockPos p = this.worldPosition;
            level.playSound(null, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, sound, SoundSource.BLOCKS, 0.8F, 1.0F);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.researched.clear();
        for (String id : input.getStringOr("researched", "").split(",")) {
            if (!id.isEmpty()) {
                this.researched.add(id);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("researched", String.join(",", this.researched));
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.theabyss.crystal_cutter");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        RegistryFriendlyByteBuf extraData = new RegistryFriendlyByteBuf(Unpooled.buffer(), inventory.player.registryAccess());
        extraData.writeBlockPos(this.worldPosition);
        return new CutterMenu(id, inventory, extraData);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.theabyss.crystal_cutter");
    }
}
