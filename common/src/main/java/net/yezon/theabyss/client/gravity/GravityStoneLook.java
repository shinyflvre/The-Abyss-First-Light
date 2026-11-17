package net.yezon.theabyss.client.gravity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.client.event.ClientTickEvent;
import net.yezon.theabyss.platform.client.event.ModelEvent;
import net.yezon.theabyss.platform.client.model.DynamicBlockStateModel;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.block.ChiseledGravityStoneBlock;
import net.yezon.theabyss.gravity.GravityIndex;
import net.yezon.theabyss.init.TheabyssModBlocks;
import net.yezon.theabyss.init.TheabyssModItems;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class GravityStoneLook {
    private static volatile boolean revealed;
    private static volatile boolean installed;

    private GravityStoneLook() {
    }

    public static boolean installed() {
        return installed;
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        installed = false;
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        Disguise brick = disguise(models, TheabyssModBlocks.STONE_BRICK.get());
        Disguise cracked = disguise(models, TheabyssModBlocks.STONE_BRICK_BROKEN.get());
        if (brick == null || cracked == null) {
            return;
        }
        Disguise[] disguises = {brick, cracked};
        List<BlockState> states = TheabyssModBlocks.CHISELED_GRAVITY_STONE.get().getStateDefinition().getPossibleStates();
        Map<ModelResourceLocation, Model> wrapped = new HashMap<>();
        for (BlockState state : states) {
            BlockState bare = bare(state);
            BakedModel base = models.get(BlockModelShaper.stateToModelLocation(bare));
            if (base == null) {
                return;
            }
            wrapped.put(BlockModelShaper.stateToModelLocation(state), new Model(base, bare, disguises));
        }
        wrapped.forEach((location, model) -> models.put(location, DynamicBlockStateModel.wrap(model)));
        installed = true;
    }

    private static BlockState bare(BlockState state) {
        BlockState bare = state;
        for (Direction side : Direction.values()) {
            bare = bare.setValue(ChiseledGravityStoneBlock.rim(side), false);
        }
        return bare;
    }

    private static @Nullable Disguise disguise(Map<ModelResourceLocation, BakedModel> models, Block block) {
        BlockState state = block.defaultBlockState();
        BakedModel model = models.get(BlockModelShaper.stateToModelLocation(state));
        return model != null ? new Disguise(state, model) : null;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        boolean holding = player != null && player.isHolding(TheabyssModItems.CHISELED_GRAVITY_STONE.get());
        if (holding == revealed) {
            return;
        }
        revealed = holding;
        ClientLevel level = mc.level;
        if (level != null) {
            GravityIndex.sections(level, key -> mc.levelRenderer.setSectionDirty(SectionPos.x(key), SectionPos.y(key), SectionPos.z(key)));
        }
    }

    private record Disguise(BlockState state, BakedModel model) {
    }

    private static final class Model implements DynamicBlockStateModel {
        private final Delegate base;
        private final Delegate[] disguises;

        private Model(BakedModel base, BlockState baseState, Disguise[] disguises) {
            this.base = new Delegate(base, baseState);
            this.disguises = new Delegate[disguises.length];
            for (int i = 0; i < disguises.length; i++) {
                this.disguises[i] = new Delegate(disguises[i].model(), disguises[i].state());
            }
        }

        private Delegate pick(BlockPos pos) {
            long seed = Mth.getSeed(pos.getX(), pos.getY(), pos.getZ());
            int hash = Mth.murmurHash3Mixer((int)(seed ^ (seed >>> 32)));
            return this.disguises[Math.floorMod(hash, this.disguises.length)];
        }

        @Override
        public Delegate select(BlockAndTintGetter level, BlockPos pos, BlockState state) {
            return revealed ? this.base : this.pick(pos);
        }

        @Override
        public Delegate fallback() {
            return revealed ? this.base : this.disguises[0];
        }
    }
}
