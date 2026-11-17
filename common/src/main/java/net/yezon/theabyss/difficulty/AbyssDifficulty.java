package net.yezon.theabyss.difficulty;

import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.EntityJoinLevelEvent;
import net.yezon.theabyss.platform.event.entity.living.FinalizeSpawnEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingIncomingDamageEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.entity.PlayerBodyEntity;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class AbyssDifficulty {
    private static final ResourceLocation HEALTH = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "config_mob_health");
    private static final Set<String> MINI_BOSSES = Set.of("the_roka", "elder", "crystal_golem");

    private AbyssDifficulty() {
    }

    public static boolean applies(Entity entity) {
        if (!(entity instanceof Mob) || !(entity instanceof Enemy) || entity instanceof PlayerBodyEntity) {
            return false;
        }
        return TheabyssMod.MODID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace())
            || TheabyssMod.MODID.equals(entity.level().dimension().location().getNamespace());
    }

    private static boolean miniBoss(Entity entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return TheabyssMod.MODID.equals(id.getNamespace()) && MINI_BOSSES.contains(id.getPath());
    }

    @SubscribeEvent
    public static void onSpawn(FinalizeSpawnEvent event) {
        MobSpawnType reason = event.getSpawnType();
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) {
            return;
        }
        Mob mob = event.getEntity();
        if (!applies(mob)) {
            return;
        }
        int rate = miniBoss(mob) ? ConfigConfiguration.MINI_BOSS_SPAWN_RATE.get() : ConfigConfiguration.MOB_SPAWN_RATE.get();
        if (rate < 100 && mob.getRandom().nextInt(100) >= rate) {
            event.setSpawnCancelled(true);
        }
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob) || !applies(mob)) {
            return;
        }
        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) {
            return;
        }
        boolean full = mob.getHealth() >= mob.getMaxHealth() - 0.01F;
        health.removeModifier(HEALTH);
        int percent = ConfigConfiguration.MOB_HEALTH.get();
        if (percent != 100) {
            health.addTransientModifier(new AttributeModifier(HEALTH, percent / 100.0 - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        if (full || mob.getHealth() > mob.getMaxHealth()) {
            mob.setHealth(mob.getMaxHealth());
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        int percent = ConfigConfiguration.MOB_DAMAGE.get();
        if (percent == 100) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker != null && attacker != event.getEntity() && applies(attacker)) {
            event.setAmount(event.getAmount() * percent / 100.0F);
        }
    }
}
