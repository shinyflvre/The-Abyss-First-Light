package net.yezon.theabyss.ring;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.yezon.theabyss.network.TheabyssModVariables;

public final class SomniumUpgrades {
    public static final int MAX = 5;
    private static final int[] NEED = {1, 2, 4, 5, 6};
    public static final double USAGE_EFFECT = 0.7;
    private static final double[] REGEN_PER_SECOND = {0.0, 0.5, 1.5, 2.0, 3.0, 4.0};

    public enum Kind {
        DAMAGE("damage"), COOLDOWN("cooldown"), USAGE("usage"), REGEN("regen");

        public final String id;

        Kind(String id) {
            this.id = id;
        }
    }

    private SomniumUpgrades() {
    }

    public static double costFactor(TheabyssModVariables.PlayerVariables vars) {
        return costFactor(vars.ManaUpgrade);
    }

    public static double costFactor(double manaUpgrade) {
        return 1.0 - USAGE_EFFECT * (1.0 - manaUpgrade);
    }

    public static double regenPerTick(double regenLevel) {
        int l = Math.max(0, Math.min(MAX, (int)Math.round(regenLevel)));
        return REGEN_PER_SECOND[l] / 20.0;
    }

    public static int level(TheabyssModVariables.PlayerVariables vars, Kind kind) {
        double v = switch (kind) {
            case DAMAGE -> vars.ManaDamage;
            case COOLDOWN -> (1.0 - vars.ManaCooldown) * 10.0;
            case USAGE -> (1.0 - vars.ManaUpgrade) * 10.0;
            case REGEN -> vars.ManaRegenUpgrade;
        };
        return Math.max(0, Math.min(MAX, (int)Math.round(v)));
    }

    public static void setLevel(TheabyssModVariables.PlayerVariables vars, Kind kind, int level) {
        int l = Math.max(0, Math.min(MAX, level));
        switch (kind) {
            case DAMAGE -> vars.ManaDamage = l;
            case COOLDOWN -> vars.ManaCooldown = 1.0 - 0.1 * l;
            case USAGE -> vars.ManaUpgrade = 1.0 - 0.1 * l;
            case REGEN -> vars.ManaRegenUpgrade = l;
        }
    }

    public static int potions(TheabyssModVariables.PlayerVariables vars, Kind kind) {
        return switch (kind) {
            case DAMAGE -> vars.DamagePotions;
            case COOLDOWN -> vars.CooldownPotions;
            case USAGE -> vars.UsagePotions;
            case REGEN -> vars.RegenPotions;
        };
    }

    public static void setPotions(TheabyssModVariables.PlayerVariables vars, Kind kind, int potions) {
        switch (kind) {
            case DAMAGE -> vars.DamagePotions = potions;
            case COOLDOWN -> vars.CooldownPotions = potions;
            case USAGE -> vars.UsagePotions = potions;
            case REGEN -> vars.RegenPotions = potions;
        }
    }

    public static int needed(int level) {
        return level >= MAX ? 0 : NEED[level];
    }

    public static float progress(TheabyssModVariables.PlayerVariables vars, Kind kind) {
        int level = level(vars, kind);
        return level >= MAX ? 1.0F : Math.min(1.0F, potions(vars, kind) / (float)needed(level));
    }

    public static void drink(Entity entity, Kind kind) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(player);
        int level = level(vars, kind);
        Component name = Component.translatable("gauntlet.theabyss.stat." + kind.id);
        if (level >= MAX) {
            player.sendOverlayMessage(Component.translatable("event.theabyss.no_effect"));
            return;
        }
        int drunk = potions(vars, kind) + 1;
        boolean up = drunk >= needed(level);
        int newLevel = up ? level + 1 : level;
        int left = up ? 0 : drunk;
        TheabyssModVariables.updatePlayerVariables(player, capability -> {
            setPotions(capability, kind, left);
            setLevel(capability, kind, newLevel);
            capability.syncPlayerVariables(player);
        });
        ServerLevel level0 = player.level();
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("theabyss:potion_used"));
        if (sound != null) {
            level0.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.NEUTRAL, 1.0F, up ? 1.25F : 1.0F);
        }
        if (up) {
            player.sendOverlayMessage(Component.translatable("upgrade.theabyss.level_up", name, newLevel));
        } else {
            player.sendOverlayMessage(Component.translatable("upgrade.theabyss.progress", name, left, needed(level), level + 1));
        }
    }
}
