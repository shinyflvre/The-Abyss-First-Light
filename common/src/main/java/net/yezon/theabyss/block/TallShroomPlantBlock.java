package net.yezon.theabyss.block;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class TallShroomPlantBlock extends DoublePlantBlock implements HoverTextBlock {
    public TallShroomPlantBlock(BlockBehaviour.Properties properties) {
        super(
            properties
                .mapColor(MapColor.PLANT)
                .sound(SoundType.GRASS)
                .instabreak()
                .postProcess((bs, br, bp) -> bp)
                .emissiveRendering(bs -> true)
                .lightLevel(s -> 4)
                .noCollision()
                .offsetType(OffsetType.XZ)
                .pushReaction(PushReaction.POPPED)
        );
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.theabyss.plant_blinds"));
    }
}
