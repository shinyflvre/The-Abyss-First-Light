package net.yezon.theabyss.block.lush;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.yezon.theabyss.init.TheabyssModItems;

final class LushVines {
    private LushVines() {
    }

    static InteractionResult pick(Entity picker, BlockState state, Level level, BlockPos pos) {
        if (!state.getValue(CaveVines.BERRIES)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            Block.popResource(serverLevel, pos, new ItemStack(TheabyssModItems.LUSH_GLOW_BERRIES.get()));
            float pitch = Mth.randomBetween(serverLevel.getRandom(), 0.8F, 1.2F);
            serverLevel.playSound(null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, pitch);
            BlockState picked = state.setValue(CaveVines.BERRIES, false);
            serverLevel.setBlock(pos, picked, 2);
            serverLevel.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(picker, picked));
        }
        return InteractionResult.SUCCESS;
    }
}
