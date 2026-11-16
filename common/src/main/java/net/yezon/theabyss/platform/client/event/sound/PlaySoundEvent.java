package net.yezon.theabyss.platform.client.event.sound;

import net.yezon.theabyss.platform.event.Event;

public class PlaySoundEvent extends Event {
    private final net.minecraft.client.resources.sounds.SoundInstance originalSound;
    private net.minecraft.client.resources.sounds.SoundInstance sound;

    public PlaySoundEvent(net.minecraft.client.resources.sounds.SoundInstance originalSound, net.minecraft.client.resources.sounds.SoundInstance sound) {
        this.originalSound = originalSound;
        this.sound = sound;
    }

    public net.minecraft.client.resources.sounds.SoundInstance getOriginalSound() {
        return this.originalSound;
    }

    public net.minecraft.client.resources.sounds.SoundInstance getSound() {
        return this.sound;
    }

    public void setSound(net.minecraft.client.resources.sounds.SoundInstance value) {
        this.sound = value;
    }
}
