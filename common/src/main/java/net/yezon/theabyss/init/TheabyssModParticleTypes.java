package net.yezon.theabyss.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;

public class TheabyssModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, "theabyss");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> END_SWORD_PT = REGISTRY.register("end_sword_pt", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> NODE_PARTICLE = REGISTRY.register("node_particle", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> KNIGHT_SWORD_PT = REGISTRY.register("knight_sword_pt", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> NIGHTBLADE_TRAIL = REGISTRY.register("nightblade_trail", () -> simple(true));
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.GlowParticleOptions.Type> GLOW = REGISTRY.register("glow", net.yezon.theabyss.ring.fx.GlowParticleOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_FLAME_ORB = REGISTRY.register("sp_flame_orb", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_METEOR_EMBER = REGISTRY.register("sp_meteor_ember", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_VOID_CORE = REGISTRY.register("sp_void_core", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_RIFT_WISP = REGISTRY.register("sp_rift_wisp", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_CURSE_SEAL = REGISTRY.register("sp_curse_seal", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_HEX_FLAME = REGISTRY.register("sp_hex_flame", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_FROST_FLAKE = REGISTRY.register("sp_frost_flake", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_ICE_SHARD = REGISTRY.register("sp_ice_shard", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_WATER_GLOB = REGISTRY.register("sp_water_glob", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_THUNDER_ORB = REGISTRY.register("sp_thunder_orb", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_HEAL_MOTE = REGISTRY.register("sp_heal_mote", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_CLOCK_MARK = REGISTRY.register("sp_clock_mark", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_STEP_SMOKE = REGISTRY.register("sp_step_smoke", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_SWAP_SPARK = REGISTRY.register("sp_swap_spark", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_SOUL_PEARL = REGISTRY.register("sp_soul_pearl", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_NATURE_BLOOM = REGISTRY.register("sp_nature_bloom", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_SPEED_ZAP = REGISTRY.register("sp_speed_zap", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_NIGHT_CRESCENT = REGISTRY.register("sp_night_crescent", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_RECALL_RUNE = REGISTRY.register("sp_recall_rune", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_FX_PIXEL_PUFF = REGISTRY.register("sp_fx_pixel_puff", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_FX_SQUARE_MOTE = REGISTRY.register("sp_fx_square_mote", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_FX_CROSS_MOTE = REGISTRY.register("sp_fx_cross_mote", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_FX_RECT_SPARK = REGISTRY.register("sp_fx_rect_spark", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> SP_FX_RING_BUBBLE = REGISTRY.register("sp_fx_ring_bubble", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> FX_FLAME = REGISTRY.register("fx_flame", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> FX_WISP = REGISTRY.register("fx_wisp", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> FX_SPARKLE = REGISTRY.register("fx_sparkle", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> FX_ZAP = REGISTRY.register("fx_zap", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> FX_ORB = REGISTRY.register("fx_orb", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> FX_SMOKE = REGISTRY.register("fx_smoke", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> FX_RING = REGISTRY.register("fx_ring", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.ring.fx.FxOptions.Type> FX_DOT = REGISTRY.register("fx_dot", net.yezon.theabyss.ring.fx.FxOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> AMBIENT_SMOKE = REGISTRY.register("ambient_smoke", () -> simple(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GROUND_FOG = REGISTRY.register("ground_fog", () -> simple(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLIZZARD_FLAKE = REGISTRY.register("blizzard_flake", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CRYSTAL_DROP = REGISTRY.register("crystal_drop", () -> simple(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CRYSTAL_GROUND = REGISTRY.register("crystal_ground", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RENDER_CYAN = REGISTRY.register("render_cyan", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RENDER_LIGHT_CYAN = REGISTRY.register("render_light_cyan", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RENDER_BRIGHT_CYAN = REGISTRY.register("render_bright_cyan", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIRE_FX = REGISTRY.register("fire_fx", () -> simple(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PHANTOM_FX = REGISTRY.register("phantom_fx", () -> simple(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SHOCK_FX = REGISTRY.register("shock_fx", () -> simple(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPURS_FX = REGISTRY.register("spurs_fx", () -> simple(true));
    public static final DeferredHolder<ParticleType<?>, net.yezon.theabyss.gravity.GravityMoteOptions.Type> GRAVITY_MOTE = REGISTRY.register("gravity_mote", net.yezon.theabyss.gravity.GravityMoteOptions.Type::new);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ARENO_EMBER = REGISTRY.register("areno_ember", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ARENO_SPLASH = REGISTRY.register("areno_splash", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ARENO_SMOKE = REGISTRY.register("areno_smoke", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DRIPPING_ARENO = REGISTRY.register("dripping_areno", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FALLING_ARENO = REGISTRY.register("falling_areno", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LANDING_ARENO = REGISTRY.register("landing_areno", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> INFUSED_LAVA_EMBER = REGISTRY.register("infused_lava_ember", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> INFUSED_LAVA_SPLASH = REGISTRY.register("infused_lava_splash", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> INFUSED_LAVA_SMOKE = REGISTRY.register("infused_lava_smoke", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DRIPPING_INFUSED_LAVA = REGISTRY.register("dripping_infused_lava", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FALLING_INFUSED_LAVA = REGISTRY.register("falling_infused_lava", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LANDING_INFUSED_LAVA = REGISTRY.register("landing_infused_lava", () -> simple(false));
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOption>> TINTED_LEAVES = REGISTRY.register("tinted_leaves", () -> colored(false));

    private static SimpleParticleType simple(boolean alwaysShow) {
        return new SimpleParticleType(alwaysShow) {
        };
    }

    private static ParticleType<ColorParticleOption> colored(boolean alwaysShow) {
        return new ParticleType<ColorParticleOption>(alwaysShow) {
            private final MapCodec<ColorParticleOption> codec = ColorParticleOption.codec(this);
            private final StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec = ColorParticleOption.streamCodec(this);

            @Override
            public MapCodec<ColorParticleOption> codec() {
                return this.codec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                return this.streamCodec;
            }
        };
    }
}
