package net.yezon.theabyss.fabric.blocks;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.yezon.theabyss.block.hooks.FluidOverlayBlock;
import net.yezon.theabyss.block.hooks.LevelLoadListener;

public final class FabricBlockItemsClient {
    private FabricBlockItemsClient() {
    }

    public static void init() {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof FluidOverlayBlock) {
                FluidRenderHandlerRegistry.INSTANCE.setBlockTransparency(block, true);
            }
        }
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof LevelLoadListener listener) {
                listener.onLoadedIntoLevel();
            }
        });
    }
}
