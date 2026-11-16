package net.yezon.theabyss.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TheabyssModSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, "theabyss");
    public static final DeferredHolder<SoundEvent, SoundEvent> PHANTOM_HURT = REGISTRY.register(
        "phantom_hurt", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "phantom_hurt"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> PHANTOM_AMBIENT = REGISTRY.register(
        "phantom_ambient", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "phantom_ambient"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> PHANTOM_ATTACK = REGISTRY.register(
        "phantom_attack", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "phantom_attack"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENTMUSIC = REGISTRY.register(
        "ambientmusic", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "ambientmusic"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSS_AMBIENCE = REGISTRY.register(
        "abyss_ambience", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "abyss_ambience"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> RAPTOR_HURT = REGISTRY.register(
        "raptor_hurt", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "raptor_hurt"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> RAPTOR_DIE = REGISTRY.register(
        "raptor_die", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "raptor_die"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> RAPTOR_IDLE = REGISTRY.register(
        "raptor_idle", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "raptor_idle"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_GOLEM_IDLE = REGISTRY.register(
        "crystal_golem_idle", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "crystal_golem_idle"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_GOLEM_HURT = REGISTRY.register(
        "crystal_golem_hurt", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "crystal_golem_hurt"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_GOLEM_DIE = REGISTRY.register(
        "crystal_golem_die", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "crystal_golem_die"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> LIZARD_IDLE = REGISTRY.register(
        "lizard_idle", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "lizard_idle"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_HURT = REGISTRY.register(
        "deer_hurt", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "deer_hurt"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_IDLE = REGISTRY.register(
        "deer_idle", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "deer_idle"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_TELEPORT = REGISTRY.register(
        "spell_of_teleport", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_teleport"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_FREEZE = REGISTRY.register(
        "spell_of_freeze", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_freeze"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_JUGGER = REGISTRY.register(
        "spell_of_jugger", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_jugger"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_INVISIBILITY = REGISTRY.register(
        "spell_of_invisibility", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_invisibility"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTBLADE_STORM_CIRCLE = REGISTRY.register(
        "nightblade_storm_circle", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "nightblade_storm_circle"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTBLADE_STORM_STRIKE = REGISTRY.register(
        "nightblade_storm_strike", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "nightblade_storm_strike"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_WHEEL_OPEN = REGISTRY.register(
        "spell_wheel_open", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_wheel_open"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSS_CHEST_OPEN = REGISTRY.register(
        "abyss_chest_open", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "abyss_chest_open"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSS_CHEST_CLOSE = REGISTRY.register(
        "abyss_chest_close", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "abyss_chest_close"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHT_NOTICE = REGISTRY.register(
        "night_notice", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "night_notice"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHT_STRIKE = REGISTRY.register(
        "night_strike", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "night_strike"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> PIXEL_SPEECH = REGISTRY.register(
        "pixel_speech", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "pixel_speech"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_WHEEL_CLOSE = REGISTRY.register(
        "spell_wheel_close", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_wheel_close"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTBLADE_PHASE_1 = REGISTRY.register(
        "nightblade_phase_1", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "nightblade_phase_1"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTBLADE_PHASE_2 = REGISTRY.register(
        "nightblade_phase_2", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "nightblade_phase_2"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTBLADE_PHASE_3 = REGISTRY.register(
        "nightblade_phase_3", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "nightblade_phase_3"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_TIME = REGISTRY.register(
        "spell_of_time", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_time"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_TELEKINETIC = REGISTRY.register(
        "spell_of_telekinetic", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_telekinetic"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_WATER = REGISTRY.register(
        "spell_of_water", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_water"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_FIRE = REGISTRY.register(
        "spell_of_fire", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_fire"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_BLACKSTRIKE_SHOOT = REGISTRY.register(
        "spell_of_blackstrike_shoot", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_blackstrike_shoot")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_BLACKSTRIKE_HIT = REGISTRY.register(
        "spell_of_blackstrike_hit", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_blackstrike_hit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_VOID_CHARGE = REGISTRY.register(
        "spell_of_void_charge", () -> SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_void_charge"), 32.0F));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_VOID_BOOM = REGISTRY.register(
        "spell_of_void_boom", () -> SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_void_boom"), 38.0F));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_VOID_EXPLODE = REGISTRY.register(
        "spell_of_void_explode", () -> SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_void_explode"), 26.0F));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_VOID_DEPLETE = REGISTRY.register(
        "spell_of_void_deplete", () -> SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_void_deplete"), 32.0F));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_BLACKHOLE_SPAWN = REGISTRY.register(
        "spell_of_blackhole_spawn", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_blackhole_spawn")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_HEALING_PULSE = REGISTRY.register(
        "spell_of_healing_pulse", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_healing_pulse")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_NATURE = REGISTRY.register(
        "spell_of_nature", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_nature")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_FIRE_SHOOT = REGISTRY.register(
        "spell_of_fire_shoot", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_fire_shoot")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_CURSE = REGISTRY.register(
        "spell_of_curse", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_curse")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_HEX_MARK = REGISTRY.register(
        "spell_of_hex_mark", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_hex_mark")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_REGEN = REGISTRY.register(
        "spell_of_regen", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_regen"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_GHOST = REGISTRY.register(
        "spell_of_ghost", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_ghost"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_MISC = REGISTRY.register(
        "spell_of_misc", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_misc"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SOMNIUM_INFUSER_BUBBLE = REGISTRY.register(
        "somnium_infuser_bubble", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "somnium_infuser_bubble"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSSAUR_IDLE = REGISTRY.register(
        "abyssaur_idle", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "abyssaur_idle"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSSAUR_DEATH = REGISTRY.register(
        "abyssaur_death", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "abyssaur_death"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSSAUR_HURT = REGISTRY.register(
        "abyssaur_hurt", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "abyssaur_hurt"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> ABYSSAUR_ATTACK = REGISTRY.register(
        "abyssaur_attack", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "abyssaur_attack"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> VERSA_WHALE_IDLE = REGISTRY.register(
        "versa_whale_idle", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "versa_whale_idle"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_BREAK = REGISTRY.register(
        "crystal_break", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "crystal_break"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> ZOMBIE_IDLE = REGISTRY.register(
        "zombie_idle", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "zombie_idle"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> ZOMBIE_DEATH = REGISTRY.register(
        "zombie_death", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "zombie_death"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> ZOMBIE_HURT = REGISTRY.register(
        "zombie_hurt", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "zombie_hurt"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> DASH = REGISTRY.register(
        "dash", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "dash"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_OF_ELEC = REGISTRY.register(
        "spell_of_elec", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "spell_of_elec"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> WOLF_IDLE = REGISTRY.register(
        "wolf_idle", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "wolf_idle"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> WOLF_HURT = REGISTRY.register(
        "wolf_hurt", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "wolf_hurt"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> WOLF_DIE = REGISTRY.register(
        "wolf_die", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "wolf_die"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> BLUE_MOUNTAINS_AMBIENCE = REGISTRY.register(
        "blue_mountains_ambience", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "blue_mountains_ambience"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> BLUE_JUNGLE_AMBIENCE = REGISTRY.register(
        "blue_jungle_ambience", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "blue_jungle_ambience"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> FROST_WORLD_AMBIENCE = REGISTRY.register(
        "frost_world_ambience", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "frost_world_ambience"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> PHANTOM_CRATE_AMBIENCE = REGISTRY.register(
        "phantom_crate_ambience", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "phantom_crate_ambience"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SLIME_FOREST_AMBIENCE = REGISTRY.register(
        "slime_forest_ambience", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "slime_forest_ambience"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SHROOMLANDS_AMBIENCE = REGISTRY.register(
        "shroomlands_ambience", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "shroomlands_ambience"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> RANDOM_FX = REGISTRY.register(
        "random_fx", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "random_fx"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SEEKER_AMBIENT = REGISTRY.register(
        "seeker_ambient", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "seeker_ambient"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHTBLADE_ATTACK = REGISTRY.register(
        "nightblade_attack", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "nightblade_attack"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> WOOD_PLACE = REGISTRY.register(
        "wood_place", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "wood_place"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> WOOD_STEP = REGISTRY.register(
        "wood_step", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "wood_step"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> THEABYSS_DIMENSION_MUSIC = REGISTRY.register(
        "theabyss_dimension_music", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "theabyss_dimension_music"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> BLADE_SLICES = REGISTRY.register(
        "blade_slices", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "blade_slices"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> WARP_ENTERED = REGISTRY.register(
        "warp_entered", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "warp_entered"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> WARP_ENTERING = REGISTRY.register(
        "warp_entering", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "warp_entering"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> BLARU_FOREST = REGISTRY.register(
        "blaru_forest", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "blaru_forest"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> POTION_USED = REGISTRY.register(
        "potion_used", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "potion_used"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> RYCILIAN_MUSHROOM = REGISTRY.register(
        "rycilian_mushroom", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "rycilian_mushroom"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> MUD_STEP = REGISTRY.register(
        "mud_step", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("theabyss", "mud_step"))
    );
}
