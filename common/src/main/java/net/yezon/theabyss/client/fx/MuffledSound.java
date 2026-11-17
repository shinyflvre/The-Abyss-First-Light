package net.yezon.theabyss.client.fx;

import java.util.concurrent.CompletableFuture;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

final class MuffledSound implements SoundInstance {
    private final SoundInstance inner;
    private final float volume;
    private final float pitch;

    MuffledSound(SoundInstance inner, float volume, float pitch) {
        this.inner = inner;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public ResourceLocation getLocation() {
        return this.inner.getLocation();
    }

    @Override
    public WeighedSoundEvents resolve(SoundManager manager) {
        return this.inner.resolve(manager);
    }

    @Override
    public Sound getSound() {
        return this.inner.getSound();
    }

    @Override
    public SoundSource getSource() {
        return this.inner.getSource();
    }

    @Override
    public boolean isLooping() {
        return this.inner.isLooping();
    }

    @Override
    public boolean isRelative() {
        return this.inner.isRelative();
    }

    @Override
    public int getDelay() {
        return this.inner.getDelay();
    }

    @Override
    public float getVolume() {
        return this.inner.getVolume() * this.volume;
    }

    @Override
    public float getPitch() {
        return this.inner.getPitch() * this.pitch;
    }

    @Override
    public double getX() {
        return this.inner.getX();
    }

    @Override
    public double getY() {
        return this.inner.getY();
    }

    @Override
    public double getZ() {
        return this.inner.getZ();
    }

    @Override
    public Attenuation getAttenuation() {
        return this.inner.getAttenuation();
    }

    @Override
    public boolean canStartSilent() {
        return this.inner.canStartSilent();
    }

    @Override
    public boolean canPlaySound() {
        return this.inner.canPlaySound();
    }

    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary library, Sound sound, boolean looping) {
        return net.yezon.theabyss.platform.services.ClientPlatformHooks.get().soundStream(this.inner, library, sound, looping);
    }

    public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary library, ResourceLocation id, boolean looping) {
        return net.yezon.theabyss.platform.services.ClientPlatformHooks.get().audioStream(this.inner, library, id, looping);
    }
}
