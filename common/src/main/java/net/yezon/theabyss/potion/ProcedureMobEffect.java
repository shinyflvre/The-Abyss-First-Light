package net.yezon.theabyss.potion;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.EntityJoinLevelEvent;
import net.yezon.theabyss.platform.event.entity.living.MobEffectEvent;
import net.yezon.theabyss.TheabyssMod;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public abstract class ProcedureMobEffect extends MobEffect {
    private static final Map<AttributeMap, WeakReference<LivingEntity>> OWNERS = Collections.synchronizedMap(new WeakHashMap<>());

    protected ProcedureMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    protected void onStarted(LivingEntity entity, int amplifier) {
    }

    protected void onTick(LivingEntity entity, int amplifier) {
    }

    protected void onEnded(LivingEntity entity) {
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        this.onTick(entity, amplifier);
        return true;
    }

    protected int attributeAmplifier(int amplifier) {
        return amplifier;
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(attributes, this.attributeAmplifier(amplifier));
        LivingEntity entity = owner(attributes);
        if (entity != null) {
            this.onStarted(entity, amplifier);
        }
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributes) {
        super.removeAttributeModifiers(attributes);
        LivingEntity entity = owner(attributes);
        if (entity != null) {
            this.onEnded(entity);
        }
    }

    private static @Nullable LivingEntity owner(AttributeMap attributes) {
        WeakReference<LivingEntity> reference = OWNERS.get(attributes);
        return reference != null ? reference.get() : null;
    }

    private static void remember(LivingEntity entity) {
        if (!entity.level().isClientSide()) {
            OWNERS.putIfAbsent(entity.getAttributes(), new WeakReference<>(entity));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living) {
            remember(living);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEffectAdded(MobEffectEvent.Added event) {
        remember(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        remember(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        remember(event.getEntity());
    }
}
