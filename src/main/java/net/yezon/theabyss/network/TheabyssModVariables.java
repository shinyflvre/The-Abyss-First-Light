package net.yezon.theabyss.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber
public class TheabyssModVariables {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TheabyssMod.MODID);
    public static final Supplier<AttachmentType<PlayerVariables>> PLAYER_VARIABLES = ATTACHMENT_TYPES.register(
        "player_variables",
        () -> AttachmentType.serializable(PlayerVariables::new)
            .copyOnDeath()
            .sync((holder, to) -> holder == to, PlayerVariables.STREAM_CODEC)
            .build()
    );

    public static PlayerVariables getPlayerVariables(Entity entity) {
        if (entity instanceof Player player && !(player instanceof FakePlayer)) {
            return player.getData(PLAYER_VARIABLES);
        }
        return new PlayerVariables();
    }

    public static void updatePlayerVariables(Entity entity, Consumer<PlayerVariables> action) {
        if (entity instanceof Player player && !(player instanceof FakePlayer)) {
            action.accept(player.getData(PLAYER_VARIABLES));
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new SavedDataSyncMessage(SavedDataSyncMessage.MAP, MapVariables.get(player.level()).toTag()));
            PacketDistributor.sendToPlayer(player, new SavedDataSyncMessage(SavedDataSyncMessage.WORLD, WorldVariables.get(player.level()).toTag()));
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new SavedDataSyncMessage(SavedDataSyncMessage.WORLD, WorldVariables.get(player.level()).toTag()));
        }
    }

    public static class PlayerVariables implements ValueIOSerializable {
        public static final StreamCodec<RegistryFriendlyByteBuf, PlayerVariables> STREAM_CODEC = StreamCodec.of(
            (buf, vars) -> {
                TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, buf.registryAccess());
                vars.serialize(output);
                ByteBufCodecs.COMPOUND_TAG.encode(buf, output.buildResult());
            },
            buf -> {
                PlayerVariables vars = new PlayerVariables();
                vars.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, buf.registryAccess(), ByteBufCodecs.COMPOUND_TAG.decode(buf)));
                return vars;
            }
        );

        public double SaveFD = 0.0;
        public double SaveY = 0.0;
        public double SaveZ = 0.0;
        public boolean BookSpawn = false;
        public double SaveX = 0.0;
        public double SaveHP = 0.0;
        public boolean SleepEvent = false;
        public double Mana = 0.0;
        public String WandAbility = "";
        public boolean RadioEvent = false;
        public boolean AbyssCloak = false;
        public boolean ManaBarToggle = false;
        public boolean ManaBlinkAnimation = false;
        public double ManaUpgrade = 1.0;
        public double ManaRegenUpgrade = 0.0;
        public double SomniumWard = 0.0;
        public boolean SleepEvent2 = false;
        public String GetConfigVersion = "";
        public double ArmorPotionLevel = 0.0;
        public boolean FearEffect = true;
        public boolean EnableSomnium = true;
        public boolean Prolog = true;
        public double ManaCooldown = 1.0;
        public double ManaDurability = 1.0;
        public double ManaDamage = 0.0;
        public int DamagePotions = 0;
        public int CooldownPotions = 0;
        public int UsagePotions = 0;
        public int RegenPotions = 0;
        public boolean InfiniteSomnium = false;
        public String Playername = "";
        public double PlayerRep = 0.0;
        public double NightbladeY = 0.0;
        public double NightbladeX = 0.0;
        public double NightbladeZ = 0.0;
        public double HomeY = 0.0;
        public double HomeZ = 0.0;
        public double HomeX = 0.0;
        public boolean RingOfHome = false;
        public double PocketPlayerZ = 0.0;
        public double PocketPlayerY = 0.0;
        public double PocketPlayerX = 0.0;
        public boolean PocketDimension = false;
        public double ClockX = 0.0;
        public double ClockY = 0.0;
        public double ClockZ = 0.0;
        public boolean ClockActive = false;
        public boolean FoundBody = false;
        public boolean DestriyAfterLifeItem = false;
        public double NetherX = 0.0;
        public double NetherY = 0.0;
        public double NetherZ = 0.0;
        public double EndX = 0.0;
        public double EndY = 0.0;
        public double EndZ = 0.0;
        public double AbyssX = 0.0;
        public double AbyssY = 0.0;
        public double AbyssZ = 0.0;
        public double OverworldX = 0.0;
        public double OverworldY = 0.0;
        public double OverworldZ = 0.0;
        public boolean CrimsonCrystalResearch = false;
        public boolean WarpedCrystalResearch = false;
        public boolean EnderCrystalResearch = false;
        public boolean AbyssCrystalResearch = false;
        public boolean FrostCrystalResearch = false;
        public boolean HollowCrystalResearch = false;
        public boolean LPMMode = false;
        public boolean EnableManaBar = false;
        public boolean FirstJoin = false;
        public double HudX = 0.0;
        public double HudY = 0.0;
        public boolean HasAbsorb = false;
        public boolean HasArmor = false;
        public boolean HasExtraHP = false;
        public boolean BarComp = false;
        public boolean EmptySomnium = false;
        public boolean SomniumHandler = false;
        public boolean EternalAbyss = false;
        public boolean EA_Intro = false;
        public boolean FriendMode = false;
        public double Infected = 0.0;
        public double Freeze = 0.0;
        public boolean CavernaCrystalResearch = false;
        public boolean AurelCrystalResearch = false;
        public boolean ConfigBook = false;
        public String GetDimension = "\"\"";
        public ItemStack RECIPES = ItemStack.EMPTY;

        public void syncPlayerVariables(Entity entity) {
            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.syncData(PLAYER_VARIABLES);
            }
        }

        @Override
        public void serialize(ValueOutput output) {
            output.putDouble("SaveFD", this.SaveFD);
            output.putDouble("SaveY", this.SaveY);
            output.putDouble("SaveZ", this.SaveZ);
            output.putBoolean("BookSpawn", this.BookSpawn);
            output.putDouble("SaveX", this.SaveX);
            output.putDouble("SaveHP", this.SaveHP);
            output.putBoolean("SleepEvent", this.SleepEvent);
            output.putDouble("Mana", this.Mana);
            output.putString("WandAbility", this.WandAbility);
            output.putBoolean("RadioEvent", this.RadioEvent);
            output.putBoolean("AbyssCloak", this.AbyssCloak);
            output.putBoolean("ManaBarToggle", this.ManaBarToggle);
            output.putBoolean("ManaBlinkAnimation", this.ManaBlinkAnimation);
            output.putDouble("ManaUpgrade", this.ManaUpgrade);
            output.putDouble("ManaRegenUpgrade", this.ManaRegenUpgrade);
            output.putBoolean("SleepEvent2", this.SleepEvent2);
            output.putString("GetConfigVersion", this.GetConfigVersion);
            output.putDouble("ArmorPotionLevel", this.ArmorPotionLevel);
            output.putBoolean("FearEffect", this.FearEffect);
            output.putBoolean("EnableSomnium", this.EnableSomnium);
            output.putBoolean("Prolog", this.Prolog);
            output.putDouble("ManaCooldown", this.ManaCooldown);
            output.putDouble("ManaDurability", this.ManaDurability);
            output.putDouble("ManaDamage", this.ManaDamage);
            output.putInt("DamagePotions", this.DamagePotions);
            output.putInt("CooldownPotions", this.CooldownPotions);
            output.putInt("UsagePotions", this.UsagePotions);
            output.putInt("RegenPotions", this.RegenPotions);
            output.putDouble("SomniumWard", this.SomniumWard);
            output.putBoolean("InfiniteSomnium", this.InfiniteSomnium);
            output.putString("Playername", this.Playername);
            output.putDouble("PlayerRep", this.PlayerRep);
            output.putDouble("NightbladeY", this.NightbladeY);
            output.putDouble("NightbladeX", this.NightbladeX);
            output.putDouble("NightbladeZ", this.NightbladeZ);
            output.putDouble("HomeY", this.HomeY);
            output.putDouble("HomeZ", this.HomeZ);
            output.putDouble("HomeX", this.HomeX);
            output.putBoolean("RingOfHome", this.RingOfHome);
            output.putDouble("PocketPlayerZ", this.PocketPlayerZ);
            output.putDouble("PocketPlayerY", this.PocketPlayerY);
            output.putDouble("PocketPlayerX", this.PocketPlayerX);
            output.putBoolean("PocketDimension", this.PocketDimension);
            output.putDouble("ClockX", this.ClockX);
            output.putDouble("ClockY", this.ClockY);
            output.putDouble("ClockZ", this.ClockZ);
            output.putBoolean("ClockActive", this.ClockActive);
            output.putBoolean("FoundBody", this.FoundBody);
            output.putBoolean("DestriyAfterLifeItem", this.DestriyAfterLifeItem);
            output.putDouble("NetherX", this.NetherX);
            output.putDouble("NetherY", this.NetherY);
            output.putDouble("NetherZ", this.NetherZ);
            output.putDouble("EndX", this.EndX);
            output.putDouble("EndY", this.EndY);
            output.putDouble("EndZ", this.EndZ);
            output.putDouble("AbyssX", this.AbyssX);
            output.putDouble("AbyssY", this.AbyssY);
            output.putDouble("AbyssZ", this.AbyssZ);
            output.putDouble("OverworldX", this.OverworldX);
            output.putDouble("OverworldY", this.OverworldY);
            output.putDouble("OverworldZ", this.OverworldZ);
            output.putBoolean("CrimsonCrystalResearch", this.CrimsonCrystalResearch);
            output.putBoolean("WarpedCrystalResearch", this.WarpedCrystalResearch);
            output.putBoolean("EnderCrystalResearch", this.EnderCrystalResearch);
            output.putBoolean("AbyssCrystalResearch", this.AbyssCrystalResearch);
            output.putBoolean("FrostCrystalResearch", this.FrostCrystalResearch);
            output.putBoolean("HollowCrystalResearch", this.HollowCrystalResearch);
            output.putBoolean("LPMMode", this.LPMMode);
            output.putBoolean("EnableManaBar", this.EnableManaBar);
            output.putBoolean("FirstJoin", this.FirstJoin);
            output.putDouble("HudX", this.HudX);
            output.putDouble("HudY", this.HudY);
            output.putBoolean("HasAbsorb", this.HasAbsorb);
            output.putBoolean("HasArmor", this.HasArmor);
            output.putBoolean("HasExtraHP", this.HasExtraHP);
            output.putBoolean("BarComp", this.BarComp);
            output.putBoolean("EmptySomnium", this.EmptySomnium);
            output.putBoolean("SomniumHandler", this.SomniumHandler);
            output.putBoolean("EternalAbyss", this.EternalAbyss);
            output.putBoolean("EA_Intro", this.EA_Intro);
            output.putBoolean("FriendMode", this.FriendMode);
            output.putDouble("Infected", this.Infected);
            output.putDouble("Freeze", this.Freeze);
            output.putBoolean("CavernaCrystalResearch", this.CavernaCrystalResearch);
            output.putBoolean("AurelCrystalResearch", this.AurelCrystalResearch);
            output.putBoolean("ConfigBook", this.ConfigBook);
            output.putString("GetDimension", this.GetDimension);
            output.store("RECIPES", ItemStack.OPTIONAL_CODEC, this.RECIPES);
        }

        @Override
        public void deserialize(ValueInput input) {
            this.SaveFD = input.getDoubleOr("SaveFD", 0.0);
            this.SaveY = input.getDoubleOr("SaveY", 0.0);
            this.SaveZ = input.getDoubleOr("SaveZ", 0.0);
            this.BookSpawn = input.getBooleanOr("BookSpawn", false);
            this.SaveX = input.getDoubleOr("SaveX", 0.0);
            this.SaveHP = input.getDoubleOr("SaveHP", 0.0);
            this.SleepEvent = input.getBooleanOr("SleepEvent", false);
            this.Mana = input.getDoubleOr("Mana", 0.0);
            this.WandAbility = input.getStringOr("WandAbility", "");
            this.RadioEvent = input.getBooleanOr("RadioEvent", false);
            this.AbyssCloak = input.getBooleanOr("AbyssCloak", false);
            this.ManaBarToggle = input.getBooleanOr("ManaBarToggle", false);
            this.ManaBlinkAnimation = input.getBooleanOr("ManaBlinkAnimation", false);
            this.ManaUpgrade = input.getDoubleOr("ManaUpgrade", 1.0);
            this.ManaRegenUpgrade = input.getDoubleOr("ManaRegenUpgrade", 0.0);
            this.SleepEvent2 = input.getBooleanOr("SleepEvent2", false);
            this.GetConfigVersion = input.getStringOr("GetConfigVersion", "");
            this.ArmorPotionLevel = input.getDoubleOr("ArmorPotionLevel", 0.0);
            this.FearEffect = input.getBooleanOr("FearEffect", true);
            this.EnableSomnium = input.getBooleanOr("EnableSomnium", true);
            this.Prolog = input.getBooleanOr("Prolog", true);
            this.ManaCooldown = input.getDoubleOr("ManaCooldown", 1.0);
            this.ManaDurability = input.getDoubleOr("ManaDurability", 1.0);
            this.ManaDamage = input.getDoubleOr("ManaDamage", 0.0);
            this.DamagePotions = input.getIntOr("DamagePotions", 0);
            this.CooldownPotions = input.getIntOr("CooldownPotions", 0);
            this.UsagePotions = input.getIntOr("UsagePotions", 0);
            this.RegenPotions = input.getIntOr("RegenPotions", 0);
            this.SomniumWard = input.getDoubleOr("SomniumWard", 0.0);
            this.InfiniteSomnium = input.getBooleanOr("InfiniteSomnium", false);
            this.Playername = input.getStringOr("Playername", "");
            this.PlayerRep = input.getDoubleOr("PlayerRep", 0.0);
            this.NightbladeY = input.getDoubleOr("NightbladeY", 0.0);
            this.NightbladeX = input.getDoubleOr("NightbladeX", 0.0);
            this.NightbladeZ = input.getDoubleOr("NightbladeZ", 0.0);
            this.HomeY = input.getDoubleOr("HomeY", 0.0);
            this.HomeZ = input.getDoubleOr("HomeZ", 0.0);
            this.HomeX = input.getDoubleOr("HomeX", 0.0);
            this.RingOfHome = input.getBooleanOr("RingOfHome", false);
            this.PocketPlayerZ = input.getDoubleOr("PocketPlayerZ", 0.0);
            this.PocketPlayerY = input.getDoubleOr("PocketPlayerY", 0.0);
            this.PocketPlayerX = input.getDoubleOr("PocketPlayerX", 0.0);
            this.PocketDimension = input.getBooleanOr("PocketDimension", false);
            this.ClockX = input.getDoubleOr("ClockX", 0.0);
            this.ClockY = input.getDoubleOr("ClockY", 0.0);
            this.ClockZ = input.getDoubleOr("ClockZ", 0.0);
            this.ClockActive = input.getBooleanOr("ClockActive", false);
            this.FoundBody = input.getBooleanOr("FoundBody", false);
            this.DestriyAfterLifeItem = input.getBooleanOr("DestriyAfterLifeItem", false);
            this.NetherX = input.getDoubleOr("NetherX", 0.0);
            this.NetherY = input.getDoubleOr("NetherY", 0.0);
            this.NetherZ = input.getDoubleOr("NetherZ", 0.0);
            this.EndX = input.getDoubleOr("EndX", 0.0);
            this.EndY = input.getDoubleOr("EndY", 0.0);
            this.EndZ = input.getDoubleOr("EndZ", 0.0);
            this.AbyssX = input.getDoubleOr("AbyssX", 0.0);
            this.AbyssY = input.getDoubleOr("AbyssY", 0.0);
            this.AbyssZ = input.getDoubleOr("AbyssZ", 0.0);
            this.OverworldX = input.getDoubleOr("OverworldX", 0.0);
            this.OverworldY = input.getDoubleOr("OverworldY", 0.0);
            this.OverworldZ = input.getDoubleOr("OverworldZ", 0.0);
            this.CrimsonCrystalResearch = input.getBooleanOr("CrimsonCrystalResearch", false);
            this.WarpedCrystalResearch = input.getBooleanOr("WarpedCrystalResearch", false);
            this.EnderCrystalResearch = input.getBooleanOr("EnderCrystalResearch", false);
            this.AbyssCrystalResearch = input.getBooleanOr("AbyssCrystalResearch", false);
            this.FrostCrystalResearch = input.getBooleanOr("FrostCrystalResearch", false);
            this.HollowCrystalResearch = input.getBooleanOr("HollowCrystalResearch", false);
            this.LPMMode = input.getBooleanOr("LPMMode", false);
            this.EnableManaBar = input.getBooleanOr("EnableManaBar", false);
            this.FirstJoin = input.getBooleanOr("FirstJoin", false);
            this.HudX = input.getDoubleOr("HudX", 0.0);
            this.HudY = input.getDoubleOr("HudY", 0.0);
            this.HasAbsorb = input.getBooleanOr("HasAbsorb", false);
            this.HasArmor = input.getBooleanOr("HasArmor", false);
            this.HasExtraHP = input.getBooleanOr("HasExtraHP", false);
            this.BarComp = input.getBooleanOr("BarComp", false);
            this.EmptySomnium = input.getBooleanOr("EmptySomnium", false);
            this.SomniumHandler = input.getBooleanOr("SomniumHandler", false);
            this.EternalAbyss = input.getBooleanOr("EternalAbyss", false);
            this.EA_Intro = input.getBooleanOr("EA_Intro", false);
            this.FriendMode = input.getBooleanOr("FriendMode", false);
            this.Infected = input.getDoubleOr("Infected", 0.0);
            this.Freeze = input.getDoubleOr("Freeze", 0.0);
            this.CavernaCrystalResearch = input.getBooleanOr("CavernaCrystalResearch", false);
            this.AurelCrystalResearch = input.getBooleanOr("AurelCrystalResearch", false);
            this.ConfigBook = input.getBooleanOr("ConfigBook", false);
            this.GetDimension = input.getStringOr("GetDimension", "\"\"");
            this.RECIPES = input.read("RECIPES", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        }
    }

    public static class MapVariables extends SavedData {
        public static final Codec<MapVariables> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("XTest", 0.0).forGetter(v -> v.XTest),
            Codec.DOUBLE.optionalFieldOf("ZTest", 0.0).forGetter(v -> v.ZTest),
            Codec.BOOL.optionalFieldOf("FullSomnium", false).forGetter(v -> v.FullSomnium)
        ).apply(i, MapVariables::new));
        public static final SavedDataType<MapVariables> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "map_variables"), MapVariables::new, CODEC
        );
        static MapVariables clientSide = new MapVariables();

        public double XTest = 0.0;
        public double ZTest = 0.0;
        public boolean FullSomnium = false;

        public MapVariables() {
        }

        private MapVariables(double xTest, double zTest, boolean fullSomnium) {
            this.XTest = xTest;
            this.ZTest = zTest;
            this.FullSomnium = fullSomnium;
        }

        CompoundTag toTag() {
            return (CompoundTag) CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, this).getOrThrow();
        }

        static MapVariables fromTag(CompoundTag tag) {
            return CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, tag).result().orElseGet(MapVariables::new);
        }

        public void syncData(LevelAccessor world) {
            this.setDirty();
            if (world instanceof Level level && !level.isClientSide()) {
                PacketDistributor.sendToAllPlayers(new SavedDataSyncMessage(SavedDataSyncMessage.MAP, this.toTag()));
            }
        }

        public static MapVariables get(LevelAccessor world) {
            if (world instanceof ServerLevelAccessor serverLevelAcc) {
                return serverLevelAcc.getLevel().getServer().getDataStorage().computeIfAbsent(TYPE);
            }
            return clientSide;
        }
    }

    public static class WorldVariables extends SavedData {
        public static final Codec<WorldVariables> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("MobLevel", 0.0).forGetter(v -> v.MobLevel),
            Codec.DOUBLE.optionalFieldOf("PocketDimensionX", 0.0).forGetter(v -> v.PocketDimensionX),
            Codec.DOUBLE.optionalFieldOf("PocketDimensionZ", 0.0).forGetter(v -> v.PocketDimensionZ)
        ).apply(i, WorldVariables::new));
        public static final SavedDataType<WorldVariables> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "world_variables"), WorldVariables::new, CODEC
        );
        static WorldVariables clientSide = new WorldVariables();

        public double MobLevel = 0.0;
        public double PocketDimensionX = 0.0;
        public double PocketDimensionZ = 0.0;

        public WorldVariables() {
        }

        private WorldVariables(double mobLevel, double pocketDimensionX, double pocketDimensionZ) {
            this.MobLevel = mobLevel;
            this.PocketDimensionX = pocketDimensionX;
            this.PocketDimensionZ = pocketDimensionZ;
        }

        CompoundTag toTag() {
            return (CompoundTag) CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, this).getOrThrow();
        }

        static WorldVariables fromTag(CompoundTag tag) {
            return CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, tag).result().orElseGet(WorldVariables::new);
        }

        public void syncData(LevelAccessor world) {
            this.setDirty();
            if (world instanceof ServerLevel level) {
                PacketDistributor.sendToPlayersInDimension(level, new SavedDataSyncMessage(SavedDataSyncMessage.WORLD, this.toTag()));
            }
        }

        public static WorldVariables get(LevelAccessor world) {
            if (world instanceof ServerLevel level) {
                return level.getDataStorage().computeIfAbsent(TYPE);
            }
            return clientSide;
        }
    }

    public record SavedDataSyncMessage(int dataType, CompoundTag data) implements CustomPacketPayload {
        public static final int MAP = 0;
        public static final int WORLD = 1;
        public static final Type<SavedDataSyncMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "saved_data_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SavedDataSyncMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SavedDataSyncMessage::dataType,
            ByteBufCodecs.COMPOUND_TAG, SavedDataSyncMessage::data,
            SavedDataSyncMessage::new
        );

        @Override
        public Type<SavedDataSyncMessage> type() {
            return TYPE;
        }

        public static void handleData(SavedDataSyncMessage message, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (message.dataType() == MAP) {
                    MapVariables.clientSide = MapVariables.fromTag(message.data());
                } else {
                    WorldVariables.clientSide = WorldVariables.fromTag(message.data());
                }
            });
        }
    }
}
