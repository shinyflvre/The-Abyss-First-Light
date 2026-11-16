package net.yezon.theabyss.platform.event.entity.living;

import net.yezon.theabyss.platform.event.Event;

public abstract class LivingDamageEvent extends Event {

    public static class Post extends LivingDamageEvent {
        private final float blockedDamage;
        private final net.minecraft.world.entity.LivingEntity entity;
        private final float inflictedDamage;
        private final float originalDamage;
        private final net.minecraft.world.damagesource.DamageSource source;

        public Post(float blockedDamage, net.minecraft.world.entity.LivingEntity entity, float inflictedDamage, float originalDamage, net.minecraft.world.damagesource.DamageSource source) {
            this.blockedDamage = blockedDamage;
            this.entity = entity;
            this.inflictedDamage = inflictedDamage;
            this.originalDamage = originalDamage;
            this.source = source;
        }

        public float getBlockedDamage() {
            return this.blockedDamage;
        }

        public net.minecraft.world.entity.LivingEntity getEntity() {
            return this.entity;
        }

        public float getInflictedDamage() {
            return this.inflictedDamage;
        }

        public float getOriginalDamage() {
            return this.originalDamage;
        }

        public net.minecraft.world.damagesource.DamageSource getSource() {
            return this.source;
        }
    }

    public static class Pre extends LivingDamageEvent {
        private final net.minecraft.world.entity.LivingEntity entity;
        private float newDamage;
        private final net.minecraft.world.damagesource.DamageSource source;

        public Pre(net.minecraft.world.entity.LivingEntity entity, float newDamage, net.minecraft.world.damagesource.DamageSource source) {
            this.entity = entity;
            this.newDamage = newDamage;
            this.source = source;
        }

        public net.minecraft.world.entity.LivingEntity getEntity() {
            return this.entity;
        }

        public float getNewDamage() {
            return this.newDamage;
        }

        public void setNewDamage(float value) {
            this.newDamage = value;
        }

        public net.minecraft.world.damagesource.DamageSource getSource() {
            return this.source;
        }
    }
}
