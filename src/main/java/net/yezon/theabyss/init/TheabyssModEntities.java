package net.yezon.theabyss.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.entity.AbylagerEntity;
import net.yezon.theabyss.entity.AbyssalLionEntity;
import net.yezon.theabyss.entity.AbyssaurEntity;
import net.yezon.theabyss.entity.AncientSeekerEntity;
import net.yezon.theabyss.entity.ArtifactOfPhantomsProjectileEntity;
import net.yezon.theabyss.entity.CrystalGolemEntity;
import net.yezon.theabyss.entity.DeerEntity;
import net.yezon.theabyss.entity.ElderEntity;
import net.yezon.theabyss.entity.EndSpiderEntity;
import net.yezon.theabyss.entity.FrostSpiderEntity;
import net.yezon.theabyss.entity.GlowPugEntity;
import net.yezon.theabyss.entity.GuardEntity;
import net.yezon.theabyss.entity.HauntedSkeletonEntity;
import net.yezon.theabyss.entity.HummingbirdEntity;
import net.yezon.theabyss.entity.HyliaFoxEntity;
import net.yezon.theabyss.entity.IceKnightEntity;
import net.yezon.theabyss.entity.IceSkeletonEntity;
import net.yezon.theabyss.entity.InfcetedCowEntity;
import net.yezon.theabyss.entity.InfectedCreeperEntity;
import net.yezon.theabyss.entity.InfectedDragonflyEntity;
import net.yezon.theabyss.entity.InfectedPhantomEntity;
import net.yezon.theabyss.entity.InfectedSpiderEntity;
import net.yezon.theabyss.entity.InfectedWolfEntity;
import net.yezon.theabyss.entity.InfectedZombieEntity;
import net.yezon.theabyss.entity.JellyFishEntity;
import net.yezon.theabyss.entity.JungleLurkerEntity;
import net.yezon.theabyss.entity.LaroFishEntity;
import net.yezon.theabyss.entity.LizardEntity;
import net.yezon.theabyss.entity.LuroFishEntity;
import net.yezon.theabyss.entity.MagicEntity;
import net.yezon.theabyss.entity.MagicianEntity;
import net.yezon.theabyss.entity.MudZombieEntity;
import net.yezon.theabyss.entity.MutatedEnderpearlProjectileEntity;
import net.yezon.theabyss.entity.NightBladeBossCloneEntity;
import net.yezon.theabyss.entity.NightbladeBossEntity;
import net.yezon.theabyss.entity.RiftNightbladeEntity;
import net.yezon.theabyss.entity.PhantomAttackProjectileEntity;
import net.yezon.theabyss.entity.PlayerBodyEntity;
import net.yezon.theabyss.entity.PoweredWolfEntity;
import net.yezon.theabyss.entity.RaluFishEntity;
import net.yezon.theabyss.entity.RaptorEntity;
import net.yezon.theabyss.entity.RingOfBlackStrikeAttackProjectileEntity;
import net.yezon.theabyss.entity.RingOfFireStrikeAttackProjectileEntity;
import net.yezon.theabyss.entity.ScorpionEntity;
import net.yezon.theabyss.entity.SeekerEntity;
import net.yezon.theabyss.entity.ShatteredZombieEntity;
import net.yezon.theabyss.entity.ShurikenExplosiveProjectileEntity;
import net.yezon.theabyss.entity.ShurikenProjectileEntity;
import net.yezon.theabyss.entity.SlimeSpiderEntity;
import net.yezon.theabyss.entity.SoulGuardEntity;
import net.yezon.theabyss.entity.SummonedHollowSeekerEntity;
import net.yezon.theabyss.entity.SummonedSeekerEntity;
import net.yezon.theabyss.entity.TheRokaEntity;
import net.yezon.theabyss.entity.VigilantHummingbirdEntity;
import net.yezon.theabyss.entity.VersaWhaleEntity;

@EventBusSubscriber
public class TheabyssModEntities {
    public static final DeferredRegister.Entities REGISTRY = DeferredRegister.createEntities(TheabyssMod.MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<TheRokaEntity>> THE_ROKA = REGISTRY.registerEntityType(
        "the_roka",
        TheRokaEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(1.0F, 2.7F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<IceSkeletonEntity>> ICE_SKELETON = REGISTRY.registerEntityType(
        "ice_skeleton",
        IceSkeletonEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.6F, 1.8F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<IceKnightEntity>> ICE_KNIGHT = REGISTRY.registerEntityType(
        "ice_knight",
        IceKnightEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(18)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.8F, 1.8F)
            .ridingOffset(-0.6F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<NightbladeBossEntity>> NIGHTBLADE_BOSS = REGISTRY.registerEntityType(
        "nightblade_boss",
        NightbladeBossEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(74)
            .setUpdateInterval(1)
            .fireImmune()
            .sized(0.8F, 1.8F)
            .ridingOffset(-0.6F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<net.yezon.theabyss.entity.AbyssEchoEntity>> ABYSS_ECHO = REGISTRY.registerEntityType(
        "abyss_echo",
        net.yezon.theabyss.entity.AbyssEchoEntity::new,
        MobCategory.MISC,
        b -> b.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).fireImmune().sized(0.6F, 1.8F).noSave().noSummon()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<net.yezon.theabyss.entity.SilentEchoEntity>> SILENT_ECHO = REGISTRY.registerEntityType(
        "silent_echo",
        net.yezon.theabyss.entity.SilentEchoEntity::new,
        MobCategory.MISC,
        b -> b.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.6F, 1.8F).eyeHeight(1.62F).noSave().noSummon()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<net.yezon.theabyss.entity.FearShadeEntity>> FEAR_SHADE = REGISTRY.registerEntityType(
        "fear_shade",
        net.yezon.theabyss.entity.FearShadeEntity::new,
        MobCategory.MISC,
        b -> b.setTrackingRange(0).fireImmune().sized(0.6F, 1.8F).noSave().noSummon()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<net.yezon.theabyss.entity.NightbladeBladeEntity>> NIGHTBLADE_BLADE = REGISTRY.registerEntityType(
        "nightblade_blade",
        net.yezon.theabyss.entity.NightbladeBladeEntity::new,
        MobCategory.MISC,
        b -> b.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).fireImmune().sized(1.0F, 0.3F).noSave().noSummon()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<net.yezon.theabyss.entity.CrystalSlashEntity>> CRYSTAL_SLASH = REGISTRY.registerEntityType(
        "crystal_slash",
        net.yezon.theabyss.entity.CrystalSlashEntity::new,
        MobCategory.MISC,
        b -> b.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).fireImmune().sized(0.5F, 0.5F).noSave().noSummon()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<net.yezon.theabyss.entity.CrystalNeedleEntity>> CRYSTAL_NEEDLE = REGISTRY.registerEntityType(
        "crystal_needle",
        net.yezon.theabyss.entity.CrystalNeedleEntity::new,
        MobCategory.MISC,
        b -> b.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).fireImmune().sized(0.3F, 0.3F).noSave().noSummon()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<net.yezon.theabyss.entity.CrystalGolemShardEntity>> CRYSTAL_GOLEM_SHARD = REGISTRY.registerEntityType(
        "crystal_golem_shard",
        net.yezon.theabyss.entity.CrystalGolemShardEntity::new,
        MobCategory.MISC,
        b -> b.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).fireImmune().sized(0.8F, 0.8F).noSave().noSummon()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<net.yezon.theabyss.entity.LurkerSpitEntity>> LURKER_SPIT = REGISTRY.registerEntityType(
        "lurker_spit",
        net.yezon.theabyss.entity.LurkerSpitEntity::new,
        MobCategory.MISC,
        b -> b.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).fireImmune().sized(0.4F, 0.4F).noSave().noSummon()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<net.yezon.theabyss.entity.NightbladeSpikeEntity>> NIGHTBLADE_SPIKE = REGISTRY.registerEntityType(
        "nightblade_spike",
        net.yezon.theabyss.entity.NightbladeSpikeEntity::new,
        MobCategory.MISC,
        b -> b.setTrackingRange(48).setUpdateInterval(20).fireImmune().sized(0.8F, 2.0F).noSave().noSummon()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<NightBladeBossCloneEntity>> NIGHT_BLADE_BOSS_CLONE = REGISTRY.registerEntityType(
        "night_blade_boss_clone",
        NightBladeBossCloneEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(20)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.8F, 1.8F)
            .ridingOffset(-0.6F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<DeerEntity>> DEER = REGISTRY.registerEntityType(
        "deer",
        DeerEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(20)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.9F, 1.4F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<LaroFishEntity>> LARO_FISH = REGISTRY.registerEntityType(
        "laro_fish",
        LaroFishEntity::new,
        MobCategory.WATER_AMBIENT,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .sized(0.6F, 0.5F)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<LuroFishEntity>> LURO_FISH = REGISTRY.registerEntityType(
        "luro_fish",
        LuroFishEntity::new,
        MobCategory.WATER_AMBIENT,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .sized(0.6F, 0.5F)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<RaluFishEntity>> RALU_FISH = REGISTRY.registerEntityType(
        "ralu_fish",
        RaluFishEntity::new,
        MobCategory.WATER_AMBIENT,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .sized(0.6F, 0.5F)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<HyliaFoxEntity>> HYLIA_FOX = REGISTRY.registerEntityType(
        "hylia_fox",
        HyliaFoxEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.8F, 0.8F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<CrystalGolemEntity>> CRYSTAL_GOLEM = REGISTRY.registerEntityType(
        "crystal_golem",
        CrystalGolemEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.8F, 3.2F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<AbyssaurEntity>> ABYSSAUR = REGISTRY.registerEntityType(
        "abyssaur",
        AbyssaurEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(2.2F, 2.5F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<MagicianEntity>> MAGICIAN = REGISTRY.registerEntityType(
        "magician",
        MagicianEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.6F, 2.8F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<SoulGuardEntity>> SOUL_GUARD = REGISTRY.registerEntityType(
        "soul_guard",
        SoulGuardEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(18)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.8F, 1.8F)
            .ridingOffset(-0.6F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<InfectedSpiderEntity>> INFECTED_SPIDER = REGISTRY.registerEntityType(
        "infected_spider",
        InfectedSpiderEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(30)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(1.4F, 1.2F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<InfectedCreeperEntity>> INFECTED_CREEPER = REGISTRY.registerEntityType(
        "infected_creeper",
        InfectedCreeperEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(40)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.6F, 1.7F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<GuardEntity>> GUARD = REGISTRY.registerEntityType(
        "guard",
        GuardEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(20)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.8F, 1.8F)
            .ridingOffset(-0.6F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<MudZombieEntity>> MUD_ZOMBIE = REGISTRY.registerEntityType(
        "mud_zombie",
        MudZombieEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(30)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.6F, 1.8F)
            .ridingOffset(-0.6F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<InfectedZombieEntity>> INFECTED_ZOMBIE = REGISTRY.registerEntityType(
        "infected_zombie",
        InfectedZombieEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(30)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.6F, 1.7F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<RaptorEntity>> RAPTOR = REGISTRY.registerEntityType(
        "raptor",
        RaptorEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .sized(1.5F, 1.2F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<EndSpiderEntity>> END_SPIDER = REGISTRY.registerEntityType(
        "end_spider",
        EndSpiderEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(40)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.8F, 1.2F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<LizardEntity>> LIZARD = REGISTRY.registerEntityType(
        "lizard",
        LizardEntity::new,
        MobCategory.CREATURE,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.8F, 0.6F)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<ElderEntity>> ELDER = REGISTRY.registerEntityType(
        "elder",
        ElderEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.8F, 2.5F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<JungleLurkerEntity>> JUNGLE_LURKER = REGISTRY.registerEntityType(
        "jungle_lurker",
        JungleLurkerEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.6F, 1.8F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<SlimeSpiderEntity>> SLIME_SPIDER = REGISTRY.registerEntityType(
        "slime_spider",
        SlimeSpiderEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(35)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(1.4F, 1.2F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<ScorpionEntity>> SCORPION = REGISTRY.registerEntityType(
        "scorpion",
        ScorpionEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(30)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(1.4F, 1.0F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<SeekerEntity>> SEEKER = REGISTRY.registerEntityType(
        "seeker",
        SeekerEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(100)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.8F, 2.0F)
            .ridingOffset(-0.6F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<PlayerBodyEntity>> PLAYER_BODY = REGISTRY.registerEntityType(
        "player_body",
        PlayerBodyEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .sized(0.5F, 0.5F)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<RiftNightbladeEntity>> RIFT_NIGHTBLADE = REGISTRY.registerEntityType(
        "rift_nightblade",
        RiftNightbladeEntity::new,
        MobCategory.MISC,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(96)
            .setUpdateInterval(1)
            .fireImmune()
            .sized(0.8F, 1.8F)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<HauntedSkeletonEntity>> HAUNTED_SKELETON = REGISTRY.registerEntityType(
        "haunted_skeleton",
        HauntedSkeletonEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.6F, 1.9F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<ShatteredZombieEntity>> SHATTERED_ZOMBIE = REGISTRY.registerEntityType(
        "shattered_zombie",
        ShatteredZombieEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(30)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.6F, 1.7F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<SummonedSeekerEntity>> SUMMONED_SEEKER = REGISTRY.registerEntityType(
        "summoned_seeker",
        SummonedSeekerEntity::new,
        MobCategory.CREATURE,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(100)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.8F, 2.0F)
            .ridingOffset(-0.6F)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<InfectedWolfEntity>> INFECTED_WOLF = REGISTRY.registerEntityType(
        "infected_wolf",
        InfectedWolfEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.8F, 1.2F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<InfectedPhantomEntity>> INFECTED_PHANTOM = REGISTRY.registerEntityType(
        "infected_phantom",
        InfectedPhantomEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.8F, 2.0F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<PoweredWolfEntity>> POWERED_WOLF = REGISTRY.registerEntityType(
        "powered_wolf",
        PoweredWolfEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.8F, 1.2F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<VersaWhaleEntity>> VERSA_WHALE = REGISTRY.registerEntityType(
        "versa_whale",
        VersaWhaleEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .sized(1.8F, 2.0F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<FrostSpiderEntity>> FROST_SPIDER = REGISTRY.registerEntityType(
        "frost_spider",
        FrostSpiderEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(35)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(1.4F, 1.2F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<GlowPugEntity>> GLOW_PUG = REGISTRY.registerEntityType(
        "glow_pug",
        GlowPugEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .sized(0.8F, 0.8F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<AbyssalLionEntity>> ABYSSAL_LION = REGISTRY.registerEntityType(
        "abyssal_lion",
        AbyssalLionEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.8F, 1.5F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<InfcetedCowEntity>> INFCETED_COW = REGISTRY.registerEntityType(
        "infceted_cow",
        InfcetedCowEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(30)
            .setUpdateInterval(3)
            .sized(0.8F, 1.4F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<AbylagerEntity>> ABYLAGER = REGISTRY.registerEntityType(
        "abylager",
        AbylagerEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.6F, 1.95F)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<SummonedHollowSeekerEntity>> SUMMONED_HOLLOW_SEEKER = REGISTRY.registerEntityType(
        "summoned_hollow_seeker",
        SummonedHollowSeekerEntity::new,
        MobCategory.CREATURE,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(100)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.8F, 2.0F)
            .ridingOffset(-0.6F)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<JellyFishEntity>> JELLY_FISH = REGISTRY.registerEntityType(
        "jelly_fish",
        JellyFishEntity::new,
        MobCategory.WATER_AMBIENT,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.6F, 0.5F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<InfectedDragonflyEntity>> INFECTED_DRAGONFLY = REGISTRY.registerEntityType(
        "infected_dragonfly",
        InfectedDragonflyEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .sized(0.6F, 0.6F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<HummingbirdEntity>> HUMMINGBIRD = REGISTRY.registerEntityType(
        "hummingbird",
        HummingbirdEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .sized(0.6F, 0.8F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<VigilantHummingbirdEntity>> VIGILANT_HUMMINGBIRD = REGISTRY.registerEntityType(
        "vigilant_hummingbird",
        VigilantHummingbirdEntity::new,
        MobCategory.AMBIENT,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .sized(0.6F, 0.8F)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<AncientSeekerEntity>> ANCIENT_SEEKER = REGISTRY.registerEntityType(
        "ancient_seeker",
        AncientSeekerEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(20)
            .setUpdateInterval(3)
            .fireImmune()
            .sized(0.8F, 2.0F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<MagicEntity>> MAGIC = REGISTRY.registerEntityType(
        "magic",
        MagicEntity::new,
        MobCategory.MONSTER,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(3)
            .sized(0.6F, 0.6F)
            .notInPeaceful()
    );
    public static final DeferredHolder<EntityType<?>, EntityType<PhantomAttackProjectileEntity>> PHANTOM_ATTACK_PROJECTILE = REGISTRY.registerEntityType(
        "projectile_phantom_attack_projectile",
        PhantomAttackProjectileEntity::new,
        MobCategory.MISC,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(1)
            .sized(0.5F, 0.5F)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<ShurikenProjectileEntity>> SHURIKEN_PROJECTILE = REGISTRY.registerEntityType(
        "projectile_shuriken_projectile",
        ShurikenProjectileEntity::new,
        MobCategory.MISC,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(1)
            .sized(0.5F, 0.5F)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<ShurikenExplosiveProjectileEntity>> SHURIKEN_EXPLOSIVE_PROJECTILE = REGISTRY.registerEntityType(
        "projectile_shuriken_explosive_projectile",
        ShurikenExplosiveProjectileEntity::new,
        MobCategory.MISC,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(1)
            .sized(0.5F, 0.5F)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<MutatedEnderpearlProjectileEntity>> MUTATED_ENDERPEARL_PROJECTILE = REGISTRY.registerEntityType(
        "projectile_mutated_enderpearl_projectile",
        MutatedEnderpearlProjectileEntity::new,
        MobCategory.MISC,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(1)
            .sized(0.5F, 0.5F)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<RingOfFireStrikeAttackProjectileEntity>> RING_OF_FIRE_STRIKE_ATTACK_PROJECTILE = REGISTRY.registerEntityType(
        "projectile_ring_of_fire_strike_attack_projectile",
        RingOfFireStrikeAttackProjectileEntity::new,
        MobCategory.MISC,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(1)
            .sized(0.5F, 0.5F)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<RingOfBlackStrikeAttackProjectileEntity>> RING_OF_BLACK_STRIKE_ATTACK_PROJECTILE = REGISTRY.registerEntityType(
        "projectile_ring_of_black_strike_attack_projectile",
        RingOfBlackStrikeAttackProjectileEntity::new,
        MobCategory.MISC,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(1)
            .sized(0.5F, 0.5F)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<ArtifactOfPhantomsProjectileEntity>> ARTIFACT_OF_PHANTOMS_PROJECTILE = REGISTRY.registerEntityType(
        "projectile_artifact_of_phantoms_projectile",
        ArtifactOfPhantomsProjectileEntity::new,
        MobCategory.MISC,
        b -> b
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(64)
            .setUpdateInterval(1)
            .sized(0.5F, 0.5F)
    );

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        net.yezon.theabyss.entity.SpawnFixes.register(event);
        TheRokaEntity.init(event);
        IceSkeletonEntity.init(event);
        IceKnightEntity.init(event);
        NightbladeBossEntity.init(event);
        NightBladeBossCloneEntity.init(event);
        DeerEntity.init(event);
        LaroFishEntity.init(event);
        LuroFishEntity.init(event);
        RaluFishEntity.init(event);
        HyliaFoxEntity.init(event);
        CrystalGolemEntity.init(event);
        AbyssaurEntity.init(event);
        MagicianEntity.init(event);
        SoulGuardEntity.init(event);
        InfectedSpiderEntity.init(event);
        InfectedCreeperEntity.init(event);
        GuardEntity.init(event);
        MudZombieEntity.init(event);
        InfectedZombieEntity.init(event);
        RaptorEntity.init(event);
        EndSpiderEntity.init(event);
        LizardEntity.init(event);
        ElderEntity.init(event);
        JungleLurkerEntity.init(event);
        SlimeSpiderEntity.init(event);
        ScorpionEntity.init(event);
        SeekerEntity.init(event);
        PlayerBodyEntity.init(event);
        HauntedSkeletonEntity.init(event);
        ShatteredZombieEntity.init(event);
        SummonedSeekerEntity.init(event);
        InfectedWolfEntity.init(event);
        InfectedPhantomEntity.init(event);
        PoweredWolfEntity.init(event);
        VersaWhaleEntity.init(event);
        FrostSpiderEntity.init(event);
        GlowPugEntity.init(event);
        AbyssalLionEntity.init(event);
        InfcetedCowEntity.init(event);
        AbylagerEntity.init(event);
        SummonedHollowSeekerEntity.init(event);
        JellyFishEntity.init(event);
        InfectedDragonflyEntity.init(event);
        HummingbirdEntity.init(event);
        VigilantHummingbirdEntity.init(event);
        AncientSeekerEntity.init(event);
        MagicEntity.init(event);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(THE_ROKA.get(), TheRokaEntity.createAttributes().build());
        event.put(ICE_SKELETON.get(), IceSkeletonEntity.createAttributes().build());
        event.put(ICE_KNIGHT.get(), IceKnightEntity.createAttributes().build());
        event.put(NIGHTBLADE_BOSS.get(), NightbladeBossEntity.createAttributes().build());
        event.put(ABYSS_ECHO.get(), net.yezon.theabyss.entity.AbyssEchoEntity.createAttributes().build());
        event.put(SILENT_ECHO.get(), net.yezon.theabyss.entity.SilentEchoEntity.createAttributes().build());
        event.put(FEAR_SHADE.get(), net.yezon.theabyss.entity.FearShadeEntity.createAttributes().build());
        event.put(NIGHT_BLADE_BOSS_CLONE.get(), NightBladeBossCloneEntity.createAttributes().build());
        event.put(DEER.get(), DeerEntity.createAttributes().build());
        event.put(LARO_FISH.get(), LaroFishEntity.createAttributes().build());
        event.put(LURO_FISH.get(), LuroFishEntity.createAttributes().build());
        event.put(RALU_FISH.get(), RaluFishEntity.createAttributes().build());
        event.put(HYLIA_FOX.get(), HyliaFoxEntity.createAttributes().build());
        event.put(CRYSTAL_GOLEM.get(), CrystalGolemEntity.createAttributes().build());
        event.put(ABYSSAUR.get(), AbyssaurEntity.createAttributes().build());
        event.put(MAGICIAN.get(), MagicianEntity.createAttributes().build());
        event.put(SOUL_GUARD.get(), SoulGuardEntity.createAttributes().build());
        event.put(INFECTED_SPIDER.get(), InfectedSpiderEntity.createAttributes().build());
        event.put(INFECTED_CREEPER.get(), InfectedCreeperEntity.createAttributes().build());
        event.put(GUARD.get(), GuardEntity.createAttributes().build());
        event.put(MUD_ZOMBIE.get(), MudZombieEntity.createAttributes().build());
        event.put(INFECTED_ZOMBIE.get(), InfectedZombieEntity.createAttributes().build());
        event.put(RAPTOR.get(), RaptorEntity.createAttributes().build());
        event.put(END_SPIDER.get(), EndSpiderEntity.createAttributes().build());
        event.put(LIZARD.get(), LizardEntity.createAttributes().build());
        event.put(ELDER.get(), ElderEntity.createAttributes().build());
        event.put(JUNGLE_LURKER.get(), JungleLurkerEntity.createAttributes().build());
        event.put(SLIME_SPIDER.get(), SlimeSpiderEntity.createAttributes().build());
        event.put(SCORPION.get(), ScorpionEntity.createAttributes().build());
        event.put(SEEKER.get(), SeekerEntity.createAttributes().build());
        event.put(PLAYER_BODY.get(), PlayerBodyEntity.createAttributes().build());
        event.put(RIFT_NIGHTBLADE.get(), RiftNightbladeEntity.createAttributes().build());
        event.put(HAUNTED_SKELETON.get(), HauntedSkeletonEntity.createAttributes().build());
        event.put(SHATTERED_ZOMBIE.get(), ShatteredZombieEntity.createAttributes().build());
        event.put(SUMMONED_SEEKER.get(), SummonedSeekerEntity.createAttributes().build());
        event.put(INFECTED_WOLF.get(), InfectedWolfEntity.createAttributes().build());
        event.put(INFECTED_PHANTOM.get(), InfectedPhantomEntity.createAttributes().build());
        event.put(POWERED_WOLF.get(), PoweredWolfEntity.createAttributes().build());
        event.put(VERSA_WHALE.get(), VersaWhaleEntity.createAttributes().build());
        event.put(FROST_SPIDER.get(), FrostSpiderEntity.createAttributes().build());
        event.put(GLOW_PUG.get(), GlowPugEntity.createAttributes().build());
        event.put(ABYSSAL_LION.get(), AbyssalLionEntity.createAttributes().build());
        event.put(INFCETED_COW.get(), InfcetedCowEntity.createAttributes().build());
        event.put(ABYLAGER.get(), AbylagerEntity.createAttributes().build());
        event.put(SUMMONED_HOLLOW_SEEKER.get(), SummonedHollowSeekerEntity.createAttributes().build());
        event.put(JELLY_FISH.get(), JellyFishEntity.createAttributes().build());
        event.put(INFECTED_DRAGONFLY.get(), InfectedDragonflyEntity.createAttributes().build());
        event.put(HUMMINGBIRD.get(), HummingbirdEntity.createAttributes().build());
        event.put(VIGILANT_HUMMINGBIRD.get(), HummingbirdEntity.createAttributes().build());
        event.put(ANCIENT_SEEKER.get(), AncientSeekerEntity.createAttributes().build());
        event.put(MAGIC.get(), MagicEntity.createAttributes().build());
    }
}
