package net.yezon.theabyss.fabric.event;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BooleanSupplier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yezon.theabyss.platform.event.AbyssEventBus;
import net.yezon.theabyss.platform.event.entity.living.LivingChangeTargetEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingDamageEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingDeathEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingDropsEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingEntityUseItemEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingExperienceDropEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingFallEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingHealEvent;
import net.yezon.theabyss.platform.event.entity.living.LivingIncomingDamageEvent;
import net.yezon.theabyss.platform.event.entity.living.MobEffectEvent;
import org.jspecify.annotations.Nullable;

public final class LivingFabricEvents {
    private LivingFabricEvents() {
    }

    public static boolean effectApplicable(LivingEntity entity, MobEffectInstance effect, @Nullable Entity source, BooleanSupplier vanilla) {
        if (!AbyssEventBus.hasListeners(MobEffectEvent.Applicable.class)) {
            return vanilla.getAsBoolean();
        }
        MobEffectEvent.Applicable event = AbyssEventBus.post(new MobEffectEvent.Applicable(effect, source, entity, MobEffectEvent.Applicable.Result.DEFAULT));
        return switch (event.getResult()) {
            case APPLY -> true;
            case DO_NOT_APPLY -> false;
            case DEFAULT -> vanilla.getAsBoolean();
        };
    }

    public static void effectAdded(LivingEntity entity) {
        if (AbyssEventBus.hasListeners(MobEffectEvent.Added.class)) {
            AbyssEventBus.post(new MobEffectEvent.Added(entity));
        }
    }

    public static boolean effectRemove(LivingEntity entity) {
        return AbyssEventBus.hasListeners(MobEffectEvent.Remove.class) && AbyssEventBus.post(new MobEffectEvent.Remove(entity)).isCanceled();
    }

    public static boolean effectExpired(LivingEntity entity) {
        return AbyssEventBus.hasListeners(MobEffectEvent.Expired.class) && AbyssEventBus.post(new MobEffectEvent.Expired(entity)).isCanceled();
    }

    public static ArrayDeque<FabricDamageFrame> frames(LivingEntity entity) {
        return ((FabricDamageState)entity).theabyss$damageFrames();
    }

    public static FabricDamageFrame pushDamage(LivingEntity entity, float damage) {
        FabricDamageFrame frame = new FabricDamageFrame(damage);
        frames(entity).push(frame);
        return frame;
    }

    public static void popDamage(LivingEntity entity, FabricDamageFrame frame) {
        ArrayDeque<FabricDamageFrame> frames = frames(entity);
        if (frames.peek() == frame) {
            frames.pop();
        } else {
            frames.remove(frame);
        }
    }

    public static @Nullable Float incomingDamage(LivingEntity entity, DamageSource source, float damage) {
        if (!AbyssEventBus.hasListeners(LivingIncomingDamageEvent.class)) {
            return damage;
        }
        LivingIncomingDamageEvent event = AbyssEventBus.post(new LivingIncomingDamageEvent(damage, entity, source));
        if (event.isCanceled() || entity.isDeadOrDying()) {
            return null;
        }
        return event.getAmount();
    }

    public static void damageBlocked(LivingEntity entity, float blocked) {
        FabricDamageFrame frame = frames(entity).peek();
        if (frame != null && blocked > 0.0F) {
            frame.blocked = blocked;
        }
    }

    public static float damagePre(LivingEntity entity, DamageSource source, float damage) {
        float result = damage;
        if (AbyssEventBus.hasListeners(LivingDamageEvent.Pre.class)) {
            result = AbyssEventBus.post(new LivingDamageEvent.Pre(entity, damage, source)).getNewDamage();
        }
        FabricDamageFrame frame = frames(entity).peek();
        if (frame != null) {
            frame.inflicted = result;
            frame.reachedEnd = true;
        }
        return result;
    }

    public static void damagePost(LivingEntity entity, DamageSource source) {
        FabricDamageFrame frame = frames(entity).peek();
        if (frame == null || !frame.reachedEnd) {
            return;
        }
        frame.reachedEnd = false;
        if (AbyssEventBus.hasListeners(LivingDamageEvent.Post.class)) {
            AbyssEventBus.post(new LivingDamageEvent.Post(frame.blocked, entity, frame.inflicted, frame.original, source));
        }
    }

    public static boolean death(LivingEntity entity, DamageSource source) {
        return AbyssEventBus.hasListeners(LivingDeathEvent.class) && AbyssEventBus.post(new LivingDeathEvent(entity, source)).isCanceled();
    }

    public static boolean startCapturingDrops(Entity entity) {
        if (!AbyssEventBus.hasListeners(LivingDropsEvent.class)) {
            return false;
        }
        ((FabricDropCaptureState)entity).theabyss$captureDrops(new ArrayList<>());
        return true;
    }

    public static void finishCapturingDrops(LivingEntity entity) {
        Collection<ItemEntity> drops = ((FabricDropCaptureState)entity).theabyss$captureDrops(null);
        if (drops == null) {
            return;
        }
        if (!AbyssEventBus.post(new LivingDropsEvent(drops, entity)).isCanceled()) {
            for (ItemEntity drop : drops) {
                entity.level().addFreshEntity(drop);
            }
        }
    }

    public static boolean captureDrop(Entity dropper, ItemEntity drop) {
        Collection<ItemEntity> drops = ((FabricDropCaptureState)dropper).theabyss$capturedDrops();
        if (drops == null) {
            return false;
        }
        drops.add(drop);
        return true;
    }

    public static boolean fall(LivingEntity entity) {
        return AbyssEventBus.hasListeners(LivingFallEvent.class) && AbyssEventBus.post(new LivingFallEvent(entity)).isCanceled();
    }

    public static @Nullable Float heal(LivingEntity entity, float amount) {
        if (!AbyssEventBus.hasListeners(LivingHealEvent.class)) {
            return null;
        }
        LivingHealEvent event = AbyssEventBus.post(new LivingHealEvent(amount, entity));
        return event.isCanceled() ? 0.0F : event.getAmount();
    }

    public static boolean changeTarget(LivingEntity entity, @Nullable LivingEntity target) {
        return AbyssEventBus.hasListeners(LivingChangeTargetEvent.class) && AbyssEventBus.post(new LivingChangeTargetEvent(entity, target)).isCanceled();
    }

    public static int experience(LivingEntity entity, @Nullable Player attackingPlayer, int experience) {
        if (!AbyssEventBus.hasListeners(LivingExperienceDropEvent.class)) {
            return experience;
        }
        LivingExperienceDropEvent event = AbyssEventBus.post(new LivingExperienceDropEvent(attackingPlayer, experience, entity));
        return event.isCanceled() ? 0 : event.getDroppedExperience();
    }

    public static boolean useItemStart(LivingEntity entity) {
        return AbyssEventBus.hasListeners(LivingEntityUseItemEvent.Start.class)
            && AbyssEventBus.post(new LivingEntityUseItemEvent.Start(entity)).isCanceled();
    }

    public static void useItemFinish(LivingEntity entity, ItemStack used) {
        if (AbyssEventBus.hasListeners(LivingEntityUseItemEvent.Finish.class)) {
            AbyssEventBus.post(new LivingEntityUseItemEvent.Finish(entity, used));
        }
    }

    public static double visibility(LivingEntity entity, double visibility) {
        if (!AbyssEventBus.hasListeners(LivingEvent.LivingVisibilityEvent.class)) {
            return visibility;
        }
        Visibility event = AbyssEventBus.post(new Visibility(entity, visibility));
        return Math.max(0.0, event.modifier);
    }

    private static final class Visibility extends LivingEvent.LivingVisibilityEvent {
        private final LivingEntity entity;
        private double modifier;

        private Visibility(LivingEntity entity, double modifier) {
            this.entity = entity;
            this.modifier = modifier;
        }

        @Override
        public Entity getEntity() {
            return this.entity;
        }

        @Override
        public void modifyVisibility(double factor) {
            this.modifier *= factor;
        }
    }
}
