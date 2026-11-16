package net.yezon.theabyss.platform.common.util;

import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;

public class DeferredSoundType extends SoundType {
    private final Supplier<SoundEvent> breaking;
    private final Supplier<SoundEvent> stepping;
    private final Supplier<SoundEvent> placing;
    private final Supplier<SoundEvent> hitting;
    private final Supplier<SoundEvent> falling;

    public DeferredSoundType(float volume, float pitch, Supplier<SoundEvent> breaking, Supplier<SoundEvent> stepping, Supplier<SoundEvent> placing,
                             Supplier<SoundEvent> hitting, Supplier<SoundEvent> falling) {
        super(volume, pitch, null, null, null, null, null);
        this.breaking = breaking;
        this.stepping = stepping;
        this.placing = placing;
        this.hitting = hitting;
        this.falling = falling;
    }

    @Override
    public SoundEvent getBreakSound() {
        return this.breaking.get();
    }

    @Override
    public SoundEvent getStepSound() {
        return this.stepping.get();
    }

    @Override
    public SoundEvent getPlaceSound() {
        return this.placing.get();
    }

    @Override
    public SoundEvent getHitSound() {
        return this.hitting.get();
    }

    @Override
    public SoundEvent getFallSound() {
        return this.falling.get();
    }
}
