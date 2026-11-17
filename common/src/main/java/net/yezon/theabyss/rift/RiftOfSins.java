package net.yezon.theabyss.rift;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.yezon.theabyss.compat.TriState;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.living.LivingIncomingDamageEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerInteractEvent;
import net.yezon.theabyss.platform.event.level.BlockEvent;
import net.yezon.theabyss.platform.event.level.ExplosionEvent;
import net.yezon.theabyss.platform.event.level.block.BreakBlockEvent;
import net.yezon.theabyss.platform.event.tick.PlayerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.entity.RiftNightbladeEntity;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModSounds;
import net.yezon.theabyss.network.RiftShiftMessage;
import net.yezon.theabyss.util.FallGuard;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class RiftOfSins {
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "riftofsins"));
    private static final ResourceLocation STRUCTURE = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "rift_of_sins");
    private static final BlockPos ORIGIN = new BlockPos(0, 100, 0);
    private static final Vec3 SPAWN = new Vec3(ORIGIN.getX() + 28.5, ORIGIN.getY() + 1.0, ORIGIN.getZ() + 7.5);
    private static final float SPAWN_YAW = 90.0F;
    private static final Vec3 THRONE = new Vec3(ORIGIN.getX() + 2.85, ORIGIN.getY() + 10.5, ORIGIN.getZ() + 7.5);
    private static final float THRONE_YAW = -90.0F;
    private static final Vec3 NIGHTBLADE = new Vec3(ORIGIN.getX() + 3.5, ORIGIN.getY() + 10.0, ORIGIN.getZ() + 7.5);
    private static final float NIGHTBLADE_YAW = -90.0F;
    private static final double VOID_Y = ORIGIN.getY() - 40.0;
    private static final int SHIFT_DELAY = 25;
    private static final int ARRIVAL_SOUND_DELAY = 10;
    private static final int COOLDOWN = 40;
    private static final int FALL_GUARD = 40;
    private static final int ARRIVAL_DIALOGUE_DELAY = 40;
    private static final String RETURN_DIMENSION = "theabyss_rift_dimension";
    private static final String RETURN_X = "theabyss_rift_x";
    private static final String RETURN_Y = "theabyss_rift_y";
    private static final String RETURN_Z = "theabyss_rift_z";
    private static final String RETURN_YAW = "theabyss_rift_yaw";
    private static final String RETURN_PITCH = "theabyss_rift_pitch";
    private static final String[] RETURN_KEYS = {RETURN_DIMENSION, RETURN_X, RETURN_Y, RETURN_Z, RETURN_YAW, RETURN_PITCH};
    private static final Set<UUID> SHIFTING = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> NEXT_USE = new ConcurrentHashMap<>();
    private static final Map<UUID, float[]> FOOD = new ConcurrentHashMap<>();

    private RiftOfSins() {
    }

    public static boolean isRift(Level level) {
        return level.dimension() == DIMENSION;
    }

    public static Vec3 nightbladePos() {
        return NIGHTBLADE;
    }

    public static void toggle(ServerPlayer player) {
        UUID id = player.getUUID();
        long now = player.level().getGameTime();
        if (player.isSpectator() || !ConfigConfiguration.storyEnabled() || !RiftStory.called(player) || SHIFTING.contains(id)
            || now < NEXT_USE.getOrDefault(id, Long.MIN_VALUE)) {
            return;
        }
        boolean leaving = isRift(player.level());
        if (leaving) {
            RiftStory.onLeave(player);
        }
        shift(player, leaving);
    }

    public static void summon(ServerPlayer player) {
        if (player.isSpectator() || !ConfigConfiguration.storyEnabled() || isRift(player.level()) || SHIFTING.contains(player.getUUID())) {
            return;
        }
        shift(player, false);
    }

    private static void shift(ServerPlayer player, boolean leaving) {
        UUID id = player.getUUID();
        MinecraftServer server = player.level().getServer();
        if (server.getLevel(DIMENSION) == null) {
            return;
        }
        if (!leaving) {
            remember(player);
        }
        SHIFTING.add(id);
        RiftShiftMessage.send(player);
        player.level().playSound(null, player.blockPosition(), TheabyssModSounds.WARP_ENTERING.get(), SoundSource.PLAYERS, 1.0F, 0.55F);
        player.level().playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.4F, 0.5F);
        TheabyssMod.queueServerWork(SHIFT_DELAY, () -> {
            SHIFTING.remove(id);
            NEXT_USE.put(id, player.level().getGameTime() + COOLDOWN);
            if (player.isRemoved() || !player.isAlive()) {
                return;
            }
            if (leaving) {
                leave(player);
            } else {
                enter(player);
            }
            TheabyssMod.queueServerWork(ARRIVAL_SOUND_DELAY, () -> {
                if (!player.isRemoved()) {
                    player.level().playSound(null, player.blockPosition(), TheabyssModSounds.WARP_ENTERED.get(), SoundSource.PLAYERS, 1.6F, 0.5F);
                }
            });
        });
    }

    private static void remember(ServerPlayer player) {
        CompoundTag data = net.yezon.theabyss.platform.PersistentData.get(player);
        data.putString(RETURN_DIMENSION, player.level().dimension().location().toString());
        data.putDouble(RETURN_X, player.getX());
        data.putDouble(RETURN_Y, player.getY());
        data.putDouble(RETURN_Z, player.getZ());
        data.putFloat(RETURN_YAW, player.getYRot());
        data.putFloat(RETURN_PITCH, player.getXRot());
    }

    private static void enter(ServerPlayer player) {
        ServerLevel rift = player.level().getServer().getLevel(DIMENSION);
        if (rift == null) {
            return;
        }
        ensureStructure(rift);
        arriveInRift(player, rift);
        TheabyssMod.queueServerWork(ARRIVAL_DIALOGUE_DELAY, () -> {
            if (!player.isRemoved()) {
                RiftStory.onArrive(player);
            }
        });
    }

    private static void leave(ServerPlayer player) {
        CompoundTag data = net.yezon.theabyss.platform.PersistentData.get(player);
        MinecraftServer server = player.level().getServer();
        ResourceLocation saved = ResourceLocation.tryParse(net.yezon.theabyss.compat.NbtCompat.getStringOr(data, RETURN_DIMENSION, ""));
        ServerLevel destination = saved == null ? null : server.getLevel(ResourceKey.create(Registries.DIMENSION, saved));
        if (destination != null && destination.dimension() != DIMENSION) {
            Vec3 pos = new Vec3(net.yezon.theabyss.compat.NbtCompat.getDoubleOr(data, RETURN_X, 0.0), net.yezon.theabyss.compat.NbtCompat.getDoubleOr(data, RETURN_Y, 0.0), net.yezon.theabyss.compat.NbtCompat.getDoubleOr(data, RETURN_Z, 0.0));
            arrive(player, destination, pos, net.yezon.theabyss.compat.NbtCompat.getFloatOr(data, RETURN_YAW, player.getYRot()), net.yezon.theabyss.compat.NbtCompat.getFloatOr(data, RETURN_PITCH, 0.0F));
        } else {
            ServerLevel level = server.overworld();
            BlockPos pos = level.getSharedSpawnPos();
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
            arrive(player, level, new Vec3(pos.getX() + 0.5, Math.max(y, pos.getY()), pos.getZ() + 0.5), level.getSharedSpawnAngle(), 0.0F);
        }
        for (String key : RETURN_KEYS) {
            data.remove(key);
        }
    }

    private static void arriveInRift(ServerPlayer player, ServerLevel rift) {
        if (RiftStory.bossDefeated(player)) {
            arrive(player, rift, THRONE, THRONE_YAW, 0.0F);
        } else {
            arrive(player, rift, SPAWN, SPAWN_YAW, 0.0F);
        }
    }

    private static void arrive(ServerPlayer player, ServerLevel level, Vec3 pos, float yaw, float pitch) {
        player.stopRiding();
        net.yezon.theabyss.util.AbyssTeleport.level(player, level, pos.x, pos.y, pos.z, yaw, pitch);
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        FallGuard.protect(player, FALL_GUARD);
    }

    private static void ensureStructure(ServerLevel rift) {
        Placed placed = net.yezon.theabyss.util.AbyssSavedData.level(rift, "rift_of_sins", Placed.FACTORY, Placed.DATA_NAME);
        if (placed.done) {
            return;
        }
        StructureTemplate template = rift.getServer().getStructureManager().getOrCreate(STRUCTURE);
        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(false);
        if (template.placeInWorld(rift, ORIGIN, ORIGIN, settings, rift.getRandom(), Block.UPDATE_CLIENTS)) {
            placed.done = true;
            placed.setDirty();
        }
    }

    private static void ensureNightblade(ServerLevel rift) {
        Placed placed = net.yezon.theabyss.util.AbyssSavedData.level(rift, "rift_of_sins", Placed.FACTORY, Placed.DATA_NAME);
        BlockPos at = BlockPos.containing(NIGHTBLADE);
        if (placed.nightblade || !placed.done || !rift.areEntitiesLoaded(new ChunkPos(at).toLong())) {
            return;
        }
        if (rift.getEntitiesOfClass(RiftNightbladeEntity.class, new AABB(at).inflate(16.0)).isEmpty()) {
            RiftNightbladeEntity nightblade = TheabyssModEntities.RIFT_NIGHTBLADE.get().create(rift);
            if (nightblade == null) {
                return;
            }
            nightblade.moveTo(NIGHTBLADE.x, NIGHTBLADE.y, NIGHTBLADE.z, NIGHTBLADE_YAW, 0.0F);
            nightblade.setYHeadRot(NIGHTBLADE_YAW);
            nightblade.setYBodyRot(NIGHTBLADE_YAW);
            rift.addFreshEntity(nightblade);
        }
        placed.nightblade = true;
        placed.setDirty();
    }

    private static void keepFood(ServerPlayer player) {
        FoodData food = player.getFoodData();
        float[] kept = FOOD.get(player.getUUID());
        if (kept == null) {
            FOOD.put(player.getUUID(), new float[] {food.getFoodLevel(), food.getSaturationLevel()});
            return;
        }
        if (food.getFoodLevel() < kept[0]) {
            food.setFoodLevel((int)kept[0]);
        }
        if (food.getSaturationLevel() < kept[1]) {
            food.setSaturation(kept[1]);
        }
        kept[0] = food.getFoodLevel();
        kept[1] = food.getSaturationLevel();
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!isRift(player.level())) {
            FOOD.remove(player.getUUID());
            return;
        }
        ServerLevel rift = player.serverLevel();
        if (!ConfigConfiguration.storyEnabled()) {
            if (!SHIFTING.contains(player.getUUID())) {
                leave(player);
            }
            return;
        }
        keepFood(player);
        if (player.tickCount % 20 == 0) {
            ensureNightblade(rift);
        }
        if (player.isSpectator() || player.getY() >= VOID_Y) {
            return;
        }
        ensureStructure(rift);
        arriveInRift(player, rift);
        rift.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.6F);
        RiftStory.onVoid(player);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && isRift(player.level()) && !event.getSource().is(DamageTypes.GENERIC_KILL)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBreak(BreakBlockEvent event) {
        if (event.getLevel() instanceof Level level && isRift(level)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (isRift(event.getLevel())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level && isRift(level)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Item item = event.getItemStack().getItem();
        if (isRift(event.getLevel()) && (item instanceof BlockItem || item instanceof BucketItem)) {
            event.setUseItem(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (isRift(event.getLevel())) {
            event.getAffectedBlocks().clear();
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag from = net.yezon.theabyss.platform.PersistentData.get(event.getOriginal());
        CompoundTag to = net.yezon.theabyss.platform.PersistentData.get(event.getEntity());
        for (String key : RETURN_KEYS) {
            Tag tag = from.get(key);
            if (tag != null) {
                to.put(key, tag.copy());
            }
        }
    }

    private static final class Placed extends SavedData {
        static final Codec<Placed> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("placed", false).forGetter(p -> p.done),
            Codec.BOOL.optionalFieldOf("nightblade", false).forGetter(p -> p.nightblade)
        ).apply(i, Placed::new));
        static final String DATA_NAME = "theabyss_rift_of_sins";
        static final SavedData.Factory<Placed> FACTORY = new SavedData.Factory<>(Placed::new,
            (tag, registries) -> CODEC.parse(NbtOps.INSTANCE, tag).result().orElseGet(Placed::new), null);

        boolean done;
        boolean nightblade;

        Placed() {
        }

        Placed(boolean done, boolean nightblade) {
            this.done = done;
            this.nightblade = nightblade;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            CODEC.encodeStart(NbtOps.INSTANCE, this).result().ifPresent(encoded -> tag.merge((CompoundTag)encoded));
            return tag;
        }
    }
}
