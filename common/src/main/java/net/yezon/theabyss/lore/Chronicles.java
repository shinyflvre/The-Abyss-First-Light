package net.yezon.theabyss.lore;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.living.LivingDeathEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingEntityUseItemEvent;
import net.yezon.theabyss.platform.event.entity.player.PlayerEvent;
import net.yezon.theabyss.platform.event.tick.PlayerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.network.ChronicleMessage;
import net.yezon.theabyss.network.TheabyssModVariables;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class Chronicles {
    public static final int CROSSING = 1;
    public static final int FAMILIAR_EYES = 2;
    public static final int FIRST_RETURN = 3;
    public static final int SOMNIUM = 4;
    public static final int ROOTS = 5;
    public static final int WOLF = 6;
    public static final int KNEELING = 7;
    public static final int VOICE = 8;
    public static final int DEATH = 9;
    public static final int HAND = 10;
    public static final int GIANTS = 11;
    public static final int ICE = 12;
    public static final int PILLARS = 13;
    public static final int ALTAR = 14;
    public static final int SOUL_HEART = 15;
    public static final int LAST_ENTRY = 16;
    public static final int FALLEN = 17;
    public static final int AFTER = 18;
    public static final int JOURNAL = 19;
    public static final int END = 20;
    private static final int[] BLACK_INK = {FIRST_RETURN, WOLF, DEATH, PILLARS, SOUL_HEART, FALLEN};

    private static final ResourceKey<Level> ABYSS = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "the_abyss"));
    private static final ResourceKey<Level> FROST = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "frost_world"));
    private static final ResourceKey<Biome> LUSH = ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "lush_glow_cave"));
    private static final ResourceKey<Structure> ALTAR_STRUCTURE = ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "altar"));
    private static final Map<UUID, Double> SOMNIUM_BEFORE = new ConcurrentHashMap<>();

    private Chronicles() {
    }

    public static boolean blackInk(int page) {
        for (int p : BLACK_INK) {
            if (p == page) {
                return true;
            }
        }
        return false;
    }

    public static void note(Player player, int page) {
        if (!(player instanceof ServerPlayer sp) || page < 1 || page > ChronicleData.COUNT) {
            return;
        }
        net.yezon.theabyss.skill.Skills.storyProgress(sp, page);
        ChronicleData data = ChronicleData.of(sp);
        if (!net.yezon.theabyss.configuration.ConfigConfiguration.loresEnabled() || data.has(page)) {
            net.yezon.theabyss.rift.RiftStory.onDeed(sp, page);
            return;
        }
        data = data.with(page);
        net.yezon.theabyss.platform.attachment.Attachments.setData(sp, ChronicleData.TYPE, data);
        ChronicleMessage.send(sp, page);
        net.yezon.theabyss.rift.RiftStory.onDeed(sp, page);
        String key = blackInk(page) ? "chronicle.theabyss.noted_black" : "chronicle.theabyss.noted";
        sp.sendOverlayMessage(Component.translatable(key, Component.translatable(String.format("chronicle.theabyss.page_%02d", page))));
        if (page != END && !data.has(END) && data.count() >= ChronicleData.COUNT - 1) {
            note(sp, END);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.level().dimension() == ABYSS) {
            note(player, CROSSING);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ResourceKey<Level> to = event.getTo();
        if (to == ABYSS) {
            note(player, CROSSING);
        } else if (to == FROST) {
            note(player, ICE);
        } else if (to == Level.OVERWORLD && event.getFrom() == ABYSS) {
            note(player, FIRST_RETURN);
            if (ChronicleData.of(player).bossDefeated()) {
                note(player, JOURNAL);
            }
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        Entity dead = event.getEntity();
        if (dead.level().isClientSide()) {
            return;
        }
        if (dead instanceof ServerPlayer player) {
            if (player.level().dimension() == ABYSS) {
                net.yezon.theabyss.platform.attachment.Attachments.setData(player, ChronicleData.TYPE, ChronicleData.of(player).died(true));
            }
            return;
        }
        EntityType<?> type = dead.getType();
        Entity killer = event.getSource().getEntity();
        if (type == TheabyssModEntities.NIGHTBLADE_BOSS.get()) {
            for (ServerPlayer player : dead.level().getEntitiesOfClass(ServerPlayer.class, new AABB(dead.blockPosition()).inflate(64.0))) {
                net.yezon.theabyss.platform.attachment.Attachments.setData(player, ChronicleData.TYPE, ChronicleData.of(player).boss(true));
                note(player, FALLEN);
            }
            return;
        }
        if (type == TheabyssModEntities.ABYSSAUR.get()) {
            for (ServerPlayer player : dead.level().getEntitiesOfClass(ServerPlayer.class, new AABB(dead.blockPosition()).inflate(32.0))) {
                note(player, GIANTS);
            }
            return;
        }
        if (!(killer instanceof ServerPlayer player)) {
            return;
        }
        if (type == TheabyssModEntities.INFECTED_ZOMBIE.get() || type == TheabyssModEntities.INFECTED_WOLF.get()
            || type == TheabyssModEntities.INFCETED_COW.get() || type == TheabyssModEntities.INFECTED_SPIDER.get()
            || type == TheabyssModEntities.INFECTED_CREEPER.get()) {
            note(player, FAMILIAR_EYES);
        }
        if (type == TheabyssModEntities.INFECTED_WOLF.get()) {
            note(player, WOLF);
        }
        if (type == TheabyssModEntities.INFECTED_PHANTOM.get()) {
            note(player, KNEELING);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && ChronicleData.of(player).diedInAbyss()) {
            net.yezon.theabyss.platform.attachment.Attachments.setData(player, ChronicleData.TYPE, ChronicleData.of(player).died(false));
            note(player, DEATH);
        }
    }

    @SubscribeEvent
    public static void onDrink(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack item = event.getItem();
        if (item.is(TheabyssModItems.SOMNIUM_UPGRADE.get()) || item.is(TheabyssModItems.SOMNIUM_COOLDOWN_UPGRADE.get())
            || item.is(TheabyssModItems.SOMNIUM_REGEN_UPGRADE.get()) || item.is(TheabyssModItems.SOMNIUM_DAMAGE_UPRAGDE.get())) {
            note(player, HAND);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SOMNIUM_BEFORE.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 10 != 0) {
            return;
        }
        ChronicleData data = ChronicleData.of(player);
        double somnium = TheabyssModVariables.getPlayerVariables(player).Mana;
        Double before = SOMNIUM_BEFORE.put(player.getUUID(), somnium);
        if (before != null && before >= 1.0 && somnium < 1.0) {
            note(player, VOICE);
        }
        if (!data.has(LAST_ENTRY) && (player.getMainHandItem().is(TheabyssModItems.EYE_OF_ABYSS.get())
            || player.getOffhandItem().is(TheabyssModItems.EYE_OF_ABYSS.get()))) {
            note(player, LAST_ENTRY);
        }
        if (data.bossDefeated() && !data.has(AFTER) && player.isInWater()) {
            note(player, AFTER);
        }
        if (!(player.level() instanceof ServerLevel level) || level.dimension() != ABYSS) {
            return;
        }
        BlockPos pos = player.blockPosition();
        if (!data.has(ROOTS) && level.getBiome(pos).is(LUSH)) {
            note(player, ROOTS);
        }
        if (!data.has(ALTAR) && player.tickCount % 40 == 0) {
            Structure altar = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getValue(ALTAR_STRUCTURE);
            if (altar != null && level.structureManager().getStructureAt(pos, altar).isValid()) {
                note(player, ALTAR);
            }
        }
    }
}
