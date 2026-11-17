package net.yezon.theabyss.enchant;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.common.Tags;
import net.yezon.theabyss.platform.event.level.BlockDropsEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.SpellFxMessage;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class MagmaSmelt {
    public static final TagKey<Block> ABYSS_ORES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "magma_smelt_ores"));

    private MagmaSmelt() {
    }

    @SubscribeEvent
    public static void onDrops(BlockDropsEvent event) {
        ServerLevel level = event.getLevel();
        if (!(event.getState().is(Tags.Blocks.ORES) || event.getState().is(ABYSS_ORES))
            || AbyssEnchantments.level(level, AbyssEnchantments.MAGMA_SMELT, event.getTool()) <= 0) {
            return;
        }
        boolean smelted = false;
        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            SingleRecipeInput input = new SingleRecipeInput(stack.copyWithCount(1));
            Optional<RecipeHolder<SmeltingRecipe>> recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level);
            if (recipe.isEmpty()) {
                continue;
            }
            ItemStack result = recipe.get().value().assemble(input, level.registryAccess());
            if (result.isEmpty()) {
                continue;
            }
            drop.setItem(result.copyWithCount(Math.min(result.getMaxStackSize(), result.getCount() * stack.getCount())));
            smelted = true;
        }
        if (smelted) {
            BlockPos pos = event.getPos();
            Vec3 centre = Vec3.atCenterOf(pos);
            SpellFxMessage.around(level, centre, SpellFxMessage.ENCHANT_SMELT, java.util.List.of(), SpellFxMessage.points(centre), 0);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.35F, 1.4F);
            level.playSound(null, pos, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.6F, 0.8F);
        }
    }
}
