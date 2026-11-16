package net.yezon.theabyss.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.potion.AfterLifeMobEffect;
import net.yezon.theabyss.potion.AntiFearPotionMobEffect;
import net.yezon.theabyss.potion.AntiInfectionMobEffect;
import net.yezon.theabyss.potion.CurseMobEffect;
import net.yezon.theabyss.potion.CursedAuraMobEffect;
import net.yezon.theabyss.potion.DashMobEffect;
import net.yezon.theabyss.potion.ElectricityMobEffect;
import net.yezon.theabyss.potion.FearMobEffect;
import net.yezon.theabyss.potion.FirestormMobEffect;
import net.yezon.theabyss.potion.FlyingMobEffect;
import net.yezon.theabyss.potion.FrostPoisonMobEffect;
import net.yezon.theabyss.potion.GhostMobEffect;
import net.yezon.theabyss.potion.ImmortalMobEffect;
import net.yezon.theabyss.potion.NodePotionMobEffect;
import net.yezon.theabyss.potion.TantrumMobEffect;
import net.yezon.theabyss.potion.TimeStopPotionEffectMobEffect;
import net.yezon.theabyss.potion.TimeStopPotionIratorMobEffect;
import net.yezon.theabyss.potion.WeakElectricityMobEffect;

public class TheabyssModMobEffects {
    public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(Registries.MOB_EFFECT, TheabyssMod.MODID);
    public static final DeferredHolder<MobEffect, MobEffect> FEAR = REGISTRY.register("fear", FearMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> NODE_POTION = REGISTRY.register("node_potion", NodePotionMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> TIME_STOP_POTION_EFFECT = REGISTRY.register("time_stop_potion_effect", TimeStopPotionEffectMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> WEIGHTLESS = REGISTRY.register("weightless", net.yezon.theabyss.potion.WeightlessMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> TIME_STOP_POTION_IRATOR = REGISTRY.register("time_stop_potion_irator", TimeStopPotionIratorMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> CURSE = REGISTRY.register("curse", CurseMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> CURSED_AURA = REGISTRY.register("cursed_aura", CursedAuraMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> ANTI_FEAR_POTION = REGISTRY.register("anti_fear_potion", AntiFearPotionMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> TANTRUM = REGISTRY.register("tantrum", TantrumMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> GHOST = REGISTRY.register("ghost", GhostMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> FLYING = REGISTRY.register("flying", FlyingMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> FIRESTORM = REGISTRY.register("firestorm", FirestormMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> AFTER_LIFE = REGISTRY.register("after_life", AfterLifeMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> IMMORTAL = REGISTRY.register("immortal", ImmortalMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> FROST_POISON = REGISTRY.register("frost_poison", FrostPoisonMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> FROZEN = REGISTRY.register("frozen", net.yezon.theabyss.potion.FrozenMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> DASH = REGISTRY.register("dash", DashMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> ELECTRICITY = REGISTRY.register("electricity", ElectricityMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> WEAK_ELECTRICITY = REGISTRY.register("weak_electricity", WeakElectricityMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> ANTI_INFECTION = REGISTRY.register("anti_infection", AntiInfectionMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> SOMNIUM_WARD = REGISTRY.register("somnium_ward",
        () -> new net.yezon.theabyss.potion.DraughtMobEffect(0x3BE8E9, net.yezon.theabyss.alchemy.Draughts::endWard));
    public static final DeferredHolder<MobEffect, MobEffect> STILLNESS = REGISTRY.register("stillness",
        () -> new net.yezon.theabyss.potion.DraughtMobEffect(0x7C6CD8));
    public static final DeferredHolder<MobEffect, MobEffect> SHARED_HEART = REGISTRY.register("shared_heart",
        () -> new net.yezon.theabyss.potion.DraughtMobEffect(0xC2304A));
}
