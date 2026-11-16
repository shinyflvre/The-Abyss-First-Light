package net.yezon.theabyss.init;

import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.client.event.RegisterParticleProvidersEvent;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.client.particle.EndSwordPTParticle;
import net.yezon.theabyss.client.particle.FluidDripParticle;
import net.yezon.theabyss.client.particle.FluidEmberParticle;
import net.yezon.theabyss.client.particle.FluidSmokeParticle;
import net.yezon.theabyss.client.particle.FluidSplashParticle;
import net.yezon.theabyss.client.particle.FireFXParticle;
import net.yezon.theabyss.client.particle.KnightSwordPTParticle;
import net.yezon.theabyss.client.particle.NodeParticleParticle;
import net.yezon.theabyss.client.particle.PhantomFXParticle;
import net.yezon.theabyss.client.particle.RenderBrightCyanParticle;
import net.yezon.theabyss.client.particle.RenderCyanParticle;
import net.yezon.theabyss.client.particle.RenderLightCyanParticle;
import net.yezon.theabyss.client.particle.ShockFXParticle;
import net.yezon.theabyss.client.particle.SpursFXParticle;

@EventBusSubscriber(value = Dist.CLIENT)
public class TheabyssModParticles {
    @SubscribeEvent
    public static void registerParticleGroups(net.yezon.theabyss.platform.client.event.RegisterParticleGroupsEvent event) {
        event.register(net.yezon.theabyss.client.particle.SpriteFxParticleGroup.TYPE, net.yezon.theabyss.client.particle.SpriteFxParticleGroup::new);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(TheabyssModParticleTypes.END_SWORD_PT.get(), EndSwordPTParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.NODE_PARTICLE.get(), NodeParticleParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.KNIGHT_SWORD_PT.get(), KnightSwordPTParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.NIGHTBLADE_TRAIL.get(), net.yezon.theabyss.client.particle.NightbladeTrailParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.GLOW.get(), net.yezon.theabyss.client.particle.GlowParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.SP_FLAME_ORB.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 16, 1, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_METEOR_EMBER.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 24, 2, false));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_VOID_CORE.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 16, 2, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_RIFT_WISP.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 16, 2, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_CURSE_SEAL.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 24, 2, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_HEX_FLAME.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 16, 1, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_FROST_FLAKE.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 24, 2, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_ICE_SHARD.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 12, 2, false));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_WATER_GLOB.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 16, 2, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_THUNDER_ORB.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 12, 1, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_HEAL_MOTE.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 16, 2, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_CLOCK_MARK.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 48, 1, false));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_STEP_SMOKE.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 16, 2, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_SWAP_SPARK.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 8, 2, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_SOUL_PEARL.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 24, 2, false));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_NATURE_BLOOM.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 24, 2, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_SPEED_ZAP.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 8, 1, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_NIGHT_CRESCENT.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 12, 1, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_RECALL_RUNE.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 48, 1, false));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_FX_PIXEL_PUFF.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 12, 2, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_FX_SQUARE_MOTE.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 6, 2, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_FX_CROSS_MOTE.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 4, 2, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_FX_RECT_SPARK.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 4, 1, true));
        event.registerSpriteSet(TheabyssModParticleTypes.SP_FX_RING_BUBBLE.get(), sprites -> net.yezon.theabyss.client.particle.SpriteFxParticle.looping(sprites, 6, 2, true));
        event.registerSpriteSet(TheabyssModParticleTypes.FX_FLAME.get(), net.yezon.theabyss.client.particle.SpriteFxParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.FX_WISP.get(), net.yezon.theabyss.client.particle.SpriteFxParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.FX_SPARKLE.get(), net.yezon.theabyss.client.particle.SpriteFxParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.FX_ZAP.get(), net.yezon.theabyss.client.particle.SpriteFxParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.FX_ORB.get(), net.yezon.theabyss.client.particle.SpriteFxParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.FX_SMOKE.get(), net.yezon.theabyss.client.particle.SpriteFxParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.FX_RING.get(), net.yezon.theabyss.client.particle.SpriteFxParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.FX_DOT.get(), net.yezon.theabyss.client.particle.SpriteFxParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.AMBIENT_SMOKE.get(), net.yezon.theabyss.client.particle.AmbientSmokeParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.GROUND_FOG.get(), net.yezon.theabyss.client.particle.GroundFogParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.BLIZZARD_FLAKE.get(), net.yezon.theabyss.client.particle.BlizzardFlakeParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.GRAVITY_MOTE.get(), net.yezon.theabyss.client.particle.GravityMoteParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.CRYSTAL_DROP.get(), net.yezon.theabyss.client.particle.CrystalDropParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.CRYSTAL_GROUND.get(), net.yezon.theabyss.client.particle.CrystalGroundParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.RENDER_CYAN.get(), RenderCyanParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.RENDER_LIGHT_CYAN.get(), RenderLightCyanParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.RENDER_BRIGHT_CYAN.get(), RenderBrightCyanParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.FIRE_FX.get(), FireFXParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.PHANTOM_FX.get(), PhantomFXParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.SHOCK_FX.get(), ShockFXParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.SPURS_FX.get(), SpursFXParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.ARENO_EMBER.get(), sprites -> FluidEmberParticle.provider(sprites, TheabyssModParticleTypes.ARENO_SMOKE));
        event.registerSpriteSet(TheabyssModParticleTypes.ARENO_SPLASH.get(), FluidSplashParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.ARENO_SMOKE.get(), sprites -> FluidSmokeParticle.provider(sprites, 0x2A1C40, 0x55407A));
        event.registerSpriteSet(TheabyssModParticleTypes.DRIPPING_ARENO.get(), sprites -> FluidDripParticle.provider(sprites, FluidDripParticle.Stage.HANG, TheabyssModFluids.ARENO, TheabyssModParticleTypes.FALLING_ARENO, 0xFFF0FF, 0xB45AE6));
        event.registerSpriteSet(TheabyssModParticleTypes.FALLING_ARENO.get(), sprites -> FluidDripParticle.provider(sprites, FluidDripParticle.Stage.FALL, TheabyssModFluids.ARENO, TheabyssModParticleTypes.LANDING_ARENO, 0xFFF0FF, 0xB45AE6));
        event.registerSpriteSet(TheabyssModParticleTypes.LANDING_ARENO.get(), sprites -> FluidDripParticle.provider(sprites, FluidDripParticle.Stage.LAND, TheabyssModFluids.ARENO, null, 0xFFF0FF, 0xB45AE6));
        event.registerSpriteSet(TheabyssModParticleTypes.INFUSED_LAVA_EMBER.get(), sprites -> FluidEmberParticle.provider(sprites, TheabyssModParticleTypes.INFUSED_LAVA_SMOKE));
        event.registerSpriteSet(TheabyssModParticleTypes.INFUSED_LAVA_SPLASH.get(), FluidSplashParticle::provider);
        event.registerSpriteSet(TheabyssModParticleTypes.INFUSED_LAVA_SMOKE.get(), sprites -> FluidSmokeParticle.provider(sprites, 0x0F2E31, 0x25585B));
        event.registerSpriteSet(TheabyssModParticleTypes.DRIPPING_INFUSED_LAVA.get(), sprites -> FluidDripParticle.provider(sprites, FluidDripParticle.Stage.HANG, TheabyssModFluids.INFUSED_LAVA, TheabyssModParticleTypes.FALLING_INFUSED_LAVA, 0xECFFFD, 0x00D6C6));
        event.registerSpriteSet(TheabyssModParticleTypes.FALLING_INFUSED_LAVA.get(), sprites -> FluidDripParticle.provider(sprites, FluidDripParticle.Stage.FALL, TheabyssModFluids.INFUSED_LAVA, TheabyssModParticleTypes.LANDING_INFUSED_LAVA, 0xECFFFD, 0x00D6C6));
        event.registerSpriteSet(TheabyssModParticleTypes.LANDING_INFUSED_LAVA.get(), sprites -> FluidDripParticle.provider(sprites, FluidDripParticle.Stage.LAND, TheabyssModFluids.INFUSED_LAVA, null, 0xECFFFD, 0x00D6C6));
    }
}
