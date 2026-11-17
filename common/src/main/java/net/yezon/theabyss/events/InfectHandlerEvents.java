package net.yezon.theabyss.events;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.tick.PlayerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.TheabyssModVariables;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class InfectHandlerEvents {
    private static final ResourceKey<Level> THE_ABYSS = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("theabyss:the_abyss"));
    private static final ResourceKey<Level> FROST_WORLD = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("theabyss:frost_world"));
    private static final double INFECT_RATE = 7.5E-5;
    private static final double FREEZE_RATE = 5.0E-5;
    private static final double RECOVERY = 0.005;
    private static final double EMPTY = 1.0E-4;
    private static final double THRESHOLD = 0.95;
    private static final double SYNC_STEP = 0.01;
    private static final int SYNC_INTERVAL = 20;
    private static final Map<ServerPlayer, double[]> SYNCED = new WeakHashMap<>();
    private static final double BOOST_FACTOR = 4.0;
    private static final Map<ServerPlayer, Long> BOOSTED_UNTIL = new WeakHashMap<>();

    private InfectHandlerEvents() {
    }

    public static void boost(ServerPlayer player, int ticks) {
        BOOSTED_UNTIL.put(player, player.level().getGameTime() + ticks);
    }

    private static double infectRate(ServerPlayer player) {
        Long until = BOOSTED_UNTIL.get(player);
        if (until == null) {
            return INFECT_RATE;
        }
        if (player.level().getGameTime() > until) {
            BOOSTED_UNTIL.remove(player);
            return INFECT_RATE;
        }
        return INFECT_RATE * BOOST_FACTOR;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        if (!player.isCreative() && !player.isSpectator()) {
            ResourceKey<Level> dimension = player.level().dimension();
            if (dimension == THE_ABYSS) {
                if (vars.Infected < 1.0) {
                    vars.Infected += infectRate(player);
                }
            } else if (vars.Infected > EMPTY) {
                vars.Infected -= RECOVERY;
            }
            if (vars.Infected > THRESHOLD) {
                net.yezon.theabyss.infection.Overgrowth.tick(player);
            } else {
                net.yezon.theabyss.infection.Overgrowth.clear(player);
            }
            if (dimension == FROST_WORLD) {
                if (vars.Freeze < 1.0) {
                    vars.Freeze += FREEZE_RATE;
                }
            } else if (vars.Freeze > EMPTY) {
                vars.Freeze -= RECOVERY;
            }
            if (vars.Freeze > THRESHOLD && player.getRandom().nextFloat() < 0.05F) {
                player.hurt(namedGenericDamage(player, "death.attack.freeze"), 1.0F);
            }
        } else {
            net.yezon.theabyss.infection.Overgrowth.clear(player);
        }
        sync(player, vars);
    }

    private static void sync(ServerPlayer player, TheabyssModVariables.PlayerVariables vars) {
        double[] last = SYNCED.computeIfAbsent(player, p -> new double[]{Double.NaN, Double.NaN});
        boolean changed = vars.Infected != last[0] || vars.Freeze != last[1];
        if (!changed) {
            return;
        }
        boolean bigStep = !(Math.abs(vars.Infected - last[0]) < SYNC_STEP) || !(Math.abs(vars.Freeze - last[1]) < SYNC_STEP);
        boolean crossed = vars.Infected > THRESHOLD != last[0] > THRESHOLD || vars.Freeze > THRESHOLD != last[1] > THRESHOLD;
        if (bigStep || crossed || player.tickCount % SYNC_INTERVAL == 0) {
            last[0] = vars.Infected;
            last[1] = vars.Freeze;
            vars.syncPlayerVariables(player);
        }
    }

    private static DamageSource namedGenericDamage(LivingEntity entity, String translateKey) {
        return new DamageSource(entity.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.GENERIC)) {
            @Override
            public Component getLocalizedDeathMessage(LivingEntity dying) {
                if (this.getEntity() == null && this.getDirectEntity() == null) {
                    return dying.getKillCredit() != null
                        ? Component.translatable(translateKey + ".player", dying.getDisplayName(), dying.getKillCredit().getDisplayName())
                        : Component.translatable(translateKey, dying.getDisplayName());
                }
                Component attacker = this.getEntity() == null ? this.getDirectEntity().getDisplayName() : this.getEntity().getDisplayName();
                ItemStack weapon = this.getEntity() instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY;
                return !weapon.isEmpty() && weapon.has(DataComponents.CUSTOM_NAME)
                    ? Component.translatable(translateKey + ".item", dying.getDisplayName(), attacker, weapon.getDisplayName())
                    : Component.translatable(translateKey, dying.getDisplayName(), attacker);
            }
        };
    }
}
