package net.yezon.theabyss.client.gravity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
        Map<BlockState, BlockStateModel> models = event.getBakingResult().blockStateModels();
        Disguise brick = disguise(models, TheabyssModBlocks.STONE_BRICK.get());
        Disguise cracked = disguise(models, TheabyssModBlocks.STONE_BRICK_BROKEN.get());
        if (brick == null || cracked == null) {
            return;
        }
        Disguise[] disguises = {brick, cracked};
        List<BlockState> states = TheabyssModBlocks.CHISELED_GRAVITY_STONE.get().getStateDefinition().getPossibleStates();
        Map<BlockState, BlockStateModel> bases = new HashMap<>();
        for (BlockState state : states) {
            BlockStateModel base = models.get(bare(state));
            if (base == null) {
                return;
            }
            bases.put(state, base);
        }
        bases.forEach((state, base) -> models.put(state, DynamicBlockStateModel.wrap(new Model(base, disguises))));
        installed = true;
    }

    private static BlockState bare(BlockState state) {
        BlockState bare = state;
        for (Direction side : Direction.values()) {
            bare = bare.setValue(ChiseledGravityStoneBlock.rim(side), false);
        }
        return bare;
    }

    private static @Nullable Disguise disguise(Map<BlockState, BlockStateModel> models, Block block) {
        BlockState state = block.defaultBlockState();
        BlockStateModel model = models.get(state);
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
            GravityIndex.sections(level, key -> mc.levelExtractor.setSectionDirty(SectionPos.x(key), SectionPos.y(key), SectionPos.z(key)));
        }
    }

    private record Disguise(BlockState state, BlockStateModel model) {
    }

    private static final class Model implements DynamicBlockStateModel {
        private final BlockStateModel base;
        private final Disguise[] disguises;
        private final @BakedQuad.MaterialFlags int flags;

        private Model(BlockStateModel base, Disguise[] disguises) {
            this.base = base;
            this.disguises = disguises;
            int flags = base.materialFlags();
            for (Disguise disguise : disguises) {
                flags |= disguise.model().materialFlags();
            }
            this.flags = flags;
        }

        private Disguise pick(BlockPos pos) {
            long seed = Mth.getSeed(pos.getX(), pos.getY(), pos.getZ());
            int hash = Mth.murmurHash3Mixer((int)(seed ^ (seed >>> 32)));
            return this.disguises[Math.floorMod(hash, this.disguises.length)];
        }

        @Override
        public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
            if (revealed) {
                DynamicBlockStateModel.collectPartsOf(this.base, level, pos, state, random, parts);
                return;
            }
            Disguise disguise = this.pick(pos);
            DynamicBlockStateModel.collectPartsOf(disguise.model(), level, pos, disguise.state(), random, parts);
        }

        @Override
        @Deprecated
        public Material.Baked particleMaterial() {
            return revealed ? this.base.particleMaterial() : this.disguises[0].model().particleMaterial();
        }

        @Override
        public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
            if (revealed) {
                return DynamicBlockStateModel.particleMaterialOf(this.base, level, pos, state);
            }
            Disguise disguise = this.pick(pos);
            return DynamicBlockStateModel.particleMaterialOf(disguise.model(), level, pos, disguise.state());
        }

        @Override
        @Deprecated
        public @BakedQuad.MaterialFlags int materialFlags() {
            return this.flags;
        }

        @Override
        public @BakedQuad.MaterialFlags int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
            return this.flags;
        }
    }
}
