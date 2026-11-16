package net.yezon.theabyss.weather;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.tick.LevelTickEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class AbyssStorm {
    public static final ResourceKey<Level> THE_ABYSS = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "the_abyss"));
    private static final int MIN_GAP_TICKS = 600;
    private static final int MAX_GAP_TICKS = 900;
    private static final double MIN_DISTANCE = 24.0;
    private static final double MAX_DISTANCE = 96.0;

    private static final Map<ResourceKey<Level>, Long> NEXT_STRIKE = new HashMap<>();

    private AbyssStorm() {
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(THE_ABYSS)) {
            return;
        }
        long now = level.getGameTime();
        Long next = NEXT_STRIKE.get(level.dimension());
        if (next == null || next - now > MAX_GAP_TICKS) {
            NEXT_STRIKE.put(level.dimension(), now + gap(level.getRandom()));
            return;
        }
        if (now < next) {
            return;
        }
        NEXT_STRIKE.put(level.dimension(), now + gap(level.getRandom()));
        List<ServerPlayer> players = level.getPlayers(player -> !player.isSpectator());
        if (!players.isEmpty()) {
            strikeNear(level, players.get(level.getRandom().nextInt(players.size())));
        }
    }

    private static int gap(RandomSource random) {
        return Mth.nextInt(random, MIN_GAP_TICKS, MAX_GAP_TICKS);
    }

    private static void strikeNear(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();
        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = MIN_DISTANCE + random.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
        BlockPos column = BlockPos.containing(player.getX() + Math.cos(angle) * distance, player.getY(), player.getZ() + Math.sin(angle) * distance);
        if (!level.isLoaded(column)) {
            return;
        }
        BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, column);
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.EVENT);
        if (bolt != null) {
            bolt.snapTo(Vec3.atBottomCenterOf(ground));
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
    }
}
