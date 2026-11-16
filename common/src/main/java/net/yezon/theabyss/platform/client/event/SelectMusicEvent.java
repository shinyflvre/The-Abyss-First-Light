package net.yezon.theabyss.platform.client.event;

import net.yezon.theabyss.platform.event.Event;
import net.yezon.theabyss.platform.event.ICancellableEvent;

public class SelectMusicEvent extends Event implements ICancellableEvent {
    private net.minecraft.sounds.Music music;

    public SelectMusicEvent(net.minecraft.sounds.Music music) {
        this.music = music;
    }

    public net.minecraft.sounds.Music getMusic() {
        return this.music;
    }

    public void setMusic(net.minecraft.sounds.Music value) {
        this.music = value;
    }
}
