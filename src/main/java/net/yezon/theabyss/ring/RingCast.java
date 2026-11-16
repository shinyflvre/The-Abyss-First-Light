package net.yezon.theabyss.ring;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.yezon.theabyss.network.TheabyssModVariables;
import org.jspecify.annotations.Nullable;

public final class RingCast {
    private final Entity caster;
    private final ItemStack stack;
    private final LevelAccessor world;
    private final Vec3 at;
    private final TheabyssModVariables.PlayerVariables vars;

    private RingCast(LevelAccessor world, Entity caster, ItemStack stack, Vec3 at, TheabyssModVariables.PlayerVariables vars) {
        this.world = world;
        this.caster = caster;
        this.stack = stack;
        this.at = at;
        this.vars = vars;
    }

    public static @Nullable RingCast start(LevelAccessor world, Entity caster, ItemStack stack, double cost) {
        return caster == null ? null : start(world, caster, stack, cost, caster.position());
    }

    public static @Nullable RingCast start(LevelAccessor world, Entity caster, ItemStack stack, double cost, Vec3 at) {
        if (caster == null) {
            return null;
        }
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(caster);
        if (vars.Mana < cost * SomniumUpgrades.costFactor(vars)) {
            if (caster instanceof Player player && !player.level().isClientSide()) {
                player.sendOverlayMessage(Component.translatable("ring.theabyss.low_energy"));
            }
            return null;
        }
        return new RingCast(world, caster, stack, at, vars);
    }

    public Entity caster() {
        return this.caster;
    }

    public Vec3 at() {
        return this.at;
    }

    public TheabyssModVariables.PlayerVariables vars() {
        return this.vars;
    }

    public @Nullable ServerLevel server() {
        return this.world instanceof ServerLevel level ? level : null;
    }

    public RingCast durability(Item ring, int amount) {
        if (this.stack.getItem() == ring && this.world instanceof ServerLevel level) {
            RingWear.wear(this.stack, level, this.caster, amount);
        }
        return this;
    }

    public RingCast cooldown(int ticks) {
        if (this.caster instanceof Player player) {
            player.getCooldowns().addCooldown(this.stack, (int)(ticks * this.vars.ManaCooldown));
        }
        return this;
    }

    public RingCast sound(String id) {
        return this.sound(id, this.at, 1.0F, 1.0F);
    }

    public RingCast sound(String id, Vec3 where, float volume, float pitch) {
        if (this.world instanceof ServerLevel level) {
            SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(id));
            if (sound != null) {
                level.playSound(null, where.x, where.y, where.z, sound, SoundSource.NEUTRAL, volume, pitch);
            }
        }
        return this;
    }

    public void charge(double cost) {
        double left = this.vars.Mana - cost * SomniumUpgrades.costFactor(this.vars);
        TheabyssModVariables.updatePlayerVariables(this.caster, capability -> {
            capability.Mana = left;
            capability.syncPlayerVariables(this.caster);
        });
    }
}
