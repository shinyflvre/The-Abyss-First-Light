package net.yezon.theabyss.events;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;

public class BreakVinesEvents {
    public static void execute(LevelAccessor world, double x, double y, double z) {
        if (!world.getBlockState(BlockPos.containing(x + 1.0, y, z)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:mineable/pickaxe")))
            && !world.getBlockState(BlockPos.containing(x + 1.0, y, z)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:mineable/axe")))
            && !world.getBlockState(BlockPos.containing(x + 1.0, y, z)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:leaves")))
            && !world.getBlockState(BlockPos.containing(x + 1.0, y, z)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:glass")))
            && !world.getBlockState(BlockPos.containing(x + 1.0, y, z)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:dirt")))
            && !world.getBlockState(BlockPos.containing(x - 1.0, y, z)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:mineable/pickaxe")))
            && !world.getBlockState(BlockPos.containing(x - 1.0, y, z)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:mineable/axe")))
            && !world.getBlockState(BlockPos.containing(x - 1.0, y, z)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:leaves")))
            && !world.getBlockState(BlockPos.containing(x - 1.0, y, z)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:glass")))
            && !world.getBlockState(BlockPos.containing(x - 1.0, y, z)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:dirt")))
            && !world.getBlockState(BlockPos.containing(x, y, z - 1.0)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:mineable/pickaxe")))
            && !world.getBlockState(BlockPos.containing(x, y, z - 1.0)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:mineable/axe")))
            && !world.getBlockState(BlockPos.containing(x, y, z - 1.0)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:leaves")))
            && !world.getBlockState(BlockPos.containing(x, y, z - 1.0)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:glass")))
            && !world.getBlockState(BlockPos.containing(x, y, z - 1.0)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:dirt")))
            && !world.getBlockState(BlockPos.containing(x, y, z + 1.0)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:mineable/pickaxe")))
            && !world.getBlockState(BlockPos.containing(x, y, z + 1.0)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:mineable/axe")))
            && !world.getBlockState(BlockPos.containing(x, y, z + 1.0)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:leaves")))
            && !world.getBlockState(BlockPos.containing(x, y, z + 1.0)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:glass")))
            && !world.getBlockState(BlockPos.containing(x, y, z + 1.0)).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("minecraft:dirt")))) {
            world.destroyBlock(BlockPos.containing(x, y, z), false);
        }
    }
}
