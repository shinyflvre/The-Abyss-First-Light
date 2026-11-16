package net.yezon.theabyss.platform.client.event.sound;

import net.yezon.theabyss.platform.event.Event;

public class PlaySoundSourceEvent extends Event {
    private final com.mojang.blaze3d.audio.Channel channel;
    private final net.minecraft.client.resources.sounds.SoundInstance sound;

    public PlaySoundSourceEvent(com.mojang.blaze3d.audio.Channel channel, net.minecraft.client.resources.sounds.SoundInstance sound) {
        this.channel = channel;
        this.sound = sound;
    }

    public com.mojang.blaze3d.audio.Channel getChannel() {
        return this.channel;
    }

    public net.minecraft.client.resources.sounds.SoundInstance getSound() {
        return this.sound;
    }
}
