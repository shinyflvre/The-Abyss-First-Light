package net.yezon.theabyss.platform.event.entity.living;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public abstract class MobEffectEvent extends Event {

    public static class Added extends MobEffectEvent {
        private final net.minecraft.world.entity.LivingEntity entity;

        public Added(net.minecraft.world.entity.LivingEntity entity) {
            this.entity = entity;
        }

        public net.minecraft.world.entity.LivingEntity getEntity() {
            return this.entity;
        }
    }

    public static class Applicable extends MobEffectEvent {
        private final net.minecraft.world.effect.MobEffectInstance effectInstance;
        private final net.minecraft.world.entity.Entity effectSource;
        private final net.minecraft.world.entity.LivingEntity entity;
        private net.yezon.theabyss.platform.event.entity.living.MobEffectEvent.Applicable.Result result;

        public Applicable(net.minecraft.world.effect.MobEffectInstance effectInstance, net.minecraft.world.entity.Entity effectSource, net.minecraft.world.entity.LivingEntity entity, net.yezon.theabyss.platform.event.entity.living.MobEffectEvent.Applicable.Result result) {
            this.effectInstance = effectInstance;
            this.effectSource = effectSource;
            this.entity = entity;
            this.result = result;
        }

        public net.minecraft.world.effect.MobEffectInstance getEffectInstance() {
            return this.effectInstance;
        }

        public net.minecraft.world.entity.Entity getEffectSource() {
            return this.effectSource;
        }

        public net.minecraft.world.entity.LivingEntity getEntity() {
            return this.entity;
        }

        public net.yezon.theabyss.platform.event.entity.living.MobEffectEvent.Applicable.Result getResult() {
            return this.result;
        }

        public void setResult(net.yezon.theabyss.platform.event.entity.living.MobEffectEvent.Applicable.Result value) {
            this.result = value;
        }

        public enum Result {
            APPLY,
            DEFAULT,
            DO_NOT_APPLY
        }
    }

    public static class Expired extends MobEffectEvent implements ICancellableEvent {
        private final net.minecraft.world.entity.LivingEntity entity;

        public Expired(net.minecraft.world.entity.LivingEntity entity) {
            this.entity = entity;
        }

        public net.minecraft.world.entity.LivingEntity getEntity() {
            return this.entity;
        }
    }

    public static class Remove extends MobEffectEvent implements ICancellableEvent {
        private final net.minecraft.world.entity.LivingEntity entity;

        public Remove(net.minecraft.world.entity.LivingEntity entity) {
            this.entity = entity;
        }

        public net.minecraft.world.entity.LivingEntity getEntity() {
            return this.entity;
        }
    }
}
