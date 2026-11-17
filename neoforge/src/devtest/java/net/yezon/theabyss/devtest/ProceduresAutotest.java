package net.yezon.theabyss.devtest;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.network.TheabyssModVariables;

final class ProceduresAutotest {
    private static final Map<String, AtomicInteger> SOUNDS = new ConcurrentHashMap<>();
    private static final net.minecraft.client.sounds.SoundEventListener LISTENER = (sound, events, range) ->
        SOUNDS.computeIfAbsent(sound.getLocation().toString(), k -> new AtomicInteger()).incrementAndGet();
    private static final AtomicInteger UNLUCK_ADDS = new AtomicInteger();
    private static int infectChanges;
    private static double lastInfected = Double.NaN;
    private static int maxQueue;
    private static boolean sampling;
    private static boolean listening;
    private static final AABB ARENA = new AABB(-40, 120, -40, 40, 200, 40);
    private static Vec3 deathAt = Vec3.ZERO;
    private static final double[] saved = new double[8];
    private static final net.minecraft.resources.ResourceKey<Level> ABYSS = net.minecraft.resources.ResourceKey.create(
        net.minecraft.core.registries.Registries.DIMENSION, ResourceLocation.parse("theabyss:the_abyss"));
    private static int deathsBefore;

    private ProceduresAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        add.add("proc listeners", ProceduresAutotest::listen, 1);
        add.add("proc arena", () -> {
            command.accept("gamerule doMobSpawning false");
            command.accept("gamerule doImmediateRespawn true");
            command.accept("gamerule doDaylightCycle false");
            command.accept("gamerule doWeatherCycle false");
            command.accept("execute in theabyss:the_abyss run forceload add -48 -48 48 48");
            command.accept("execute in theabyss:the_abyss run fill -30 149 -30 30 149 30 minecraft:stone");
            command.accept("execute in theabyss:the_abyss run fill -30 150 -30 30 165 30 minecraft:air");
            command.accept("execute in theabyss:the_abyss run fill -6 199 -6 14 199 14 minecraft:stone");
            command.accept("execute in theabyss:the_abyss run fill -6 200 -6 14 205 14 minecraft:air");
            command.accept("execute in minecraft:overworld run forceload add -48 -48 48 48");
            command.accept("execute in minecraft:overworld run fill -30 199 -30 30 199 30 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -30 200 -30 30 215 30 minecraft:air");
            command.accept("execute in theabyss:frost_world run forceload add -16 -16 16 16");
            command.accept("execute in theabyss:frost_world run fill -10 199 -10 10 199 10 minecraft:stone");
            command.accept("execute in theabyss:frost_world run fill -10 200 -10 10 210 10 minecraft:air");
        }, 120);

        totems(add, command);
        infection(add, command);
        afterLife(add, command);
        dashAndSlice(add, command);
        dreamShifter(add, command);
        home(add, command);
        phantomSoul(add, command);
        rings(add, command);
        clock(add, command);
        add.add("proc end", () -> fact("end", "reached"), 1);
    }

    private static void totems(PossessionAutotest.StepAdder add, Consumer<String> command) {
        String[][] cases = {
            {"abyss_hand", "weapon.mainhand", "theabyss:totem_of_abyss"},
            {"abyss_inventory", "inventory.10", "theabyss:totem_of_abyss"},
            {"time_hand", "weapon.mainhand", "theabyss:totem_of_time"},
            {"thunder_offhand", "weapon.offhand", "theabyss:totem_of_thunder"},
        };
        for (String[] c : cases) {
            String key = "totem." + c[0];
            reset(add, command, "the_abyss", key);
            add.add(key + " setup", () -> {
                command.accept("item replace entity @s " + c[1] + " with " + c[2]);
                command.accept("execute in theabyss:the_abyss run summon minecraft:zombie 3.5 150 0.5 {NoAI:1b,PersistenceRequired:1b,Tags:[\"proc\"]}");
                command.accept("execute in theabyss:the_abyss run summon minecraft:item 0.5 150 3.5 {Item:{id:\"minecraft:stone\",count:1},PickupDelay:32767,Tags:[\"proc\"]}");
            }, 20);
            add.add(key + " lethal", () -> {
                sounds();
                server(sp -> sp.hurt(sp.damageSources().generic(), 1000.0F));
            }, 6);
            add.add(key + " result", () -> {
                Minecraft mc = Minecraft.getInstance();
                fact(key + ".client_activation_item", id(activationItem(mc)));
                fact(key + ".sound_totem_use", sound("minecraft:item.totem.use"));
                server(sp -> {
                    fact(key + ".alive", sp.isAlive());
                    fact(key + ".health", r1(sp.getHealth()));
                    fact(key + ".totems_left", count(sp, c[2]));
                    Zombie zombie = first(sp, Zombie.class);
                    fact(key + ".zombie_health", zombie == null ? "gone" : r1(zombie.getHealth()));
                    fact(key + ".zombie_frozen", zombie != null && zombie.hasEffect(effect("theabyss:time_stop_potion_irator")));
                    fact(key + ".caster_time_effect", sp.hasEffect(effect("theabyss:time_stop_potion_effect")));
                    fact(key + ".lightning", sp.serverLevel().getEntitiesOfClass(LightningBolt.class, ARENA).size());
                    fact(key + ".item_entity_survived", !sp.serverLevel().getEntitiesOfClass(ItemEntity.class, ARENA).isEmpty());
                });
            }, 10);
        }

        reset(add, command, "the_abyss", "totem.multi");
        add.add("totem.multi setup", () -> {
            command.accept("item replace entity @s inventory.1 with theabyss:totem_of_abyss");
            command.accept("item replace entity @s inventory.2 with theabyss:totem_of_time");
            command.accept("item replace entity @s inventory.3 with theabyss:totem_of_thunder");
        }, 10);
        add.add("totem.multi lethal", () -> server(sp -> sp.hurt(sp.damageSources().generic(), 1000.0F)), 10);
        add.add("totem.multi result", () -> server(sp -> {
            fact("totem.multi.alive", sp.isAlive());
            fact("totem.multi.left", count(sp, "theabyss:totem_of_abyss") + "/" + count(sp, "theabyss:totem_of_time") + "/" + count(sp, "theabyss:totem_of_thunder"));
        }), 5);

        reset(add, command, "the_abyss", "totem.kill");
        add.add("totem.kill setup", () -> {
            command.accept("item replace entity @s weapon.mainhand with theabyss:totem_of_abyss");
            server(sp -> deathsBefore = deaths(sp));
        }, 10);
        add.add("totem.kill run", () -> command.accept("kill @s"), 10);
        add.add("totem.kill result", () -> server(sp -> {
            fact("totem.kill.totem_left", count(sp, "theabyss:totem_of_abyss"));
            fact("totem.kill.player_died", deaths(sp) > deathsBefore);
        }), 40);
    }

    private static void infection(PossessionAutotest.StepAdder add, Consumer<String> command) {
        reset(add, command, "the_abyss", "infect.abyss");
        add.add("infect.abyss set", () -> server(sp -> setVar(sp, "Infected", 0.5)), 10);
        add.add("infect.abyss sample", () -> {
            infectChanges = 0;
            sampling = true;
            server(sp -> saved[0] = TheabyssModVariables.getPlayerVariables(sp).Infected);
        }, 100);
        add.add("infect.abyss result", () -> {
            sampling = false;
            fact("infect.abyss.client_value_changes_100t", infectChanges);
            fact("infect.abyss.client_value", r4(TheabyssModVariables.getPlayerVariables(Minecraft.getInstance().player).Infected));
            server(sp -> fact("infect.abyss.server_delta_100t", r5(TheabyssModVariables.getPlayerVariables(sp).Infected - saved[0])));
        }, 5);
        add.add("infect.damage set", () -> server(sp -> {
            sp.setHealth(20.0F);
            setVar(sp, "Infected", 0.97);
        }), 40);
        add.add("infect.damage result", () -> server(sp -> fact("infect.damage.health_after_40t", r1(sp.getHealth()))), 5);
        add.add("infect.creative", () -> {
            command.accept("gamemode creative @s");
            server(sp -> setVar(sp, "Infected", 0.5));
        }, 40);
        add.add("infect.creative result", () -> server(sp -> fact("infect.creative.value", r4(TheabyssModVariables.getPlayerVariables(sp).Infected))), 5);

        reset(add, command, "overworld", "infect.overworld");
        add.add("infect.overworld set", () -> server(sp -> setVar(sp, "Infected", 0.5)), 1);
        add.add("infect.overworld sample", () -> {
            infectChanges = 0;
            sampling = true;
        }, 40);
        add.add("infect.overworld result", () -> {
            sampling = false;
            fact("infect.overworld.client_value_changes_40t", infectChanges);
            fact("infect.overworld.client_value", r3(TheabyssModVariables.getPlayerVariables(Minecraft.getInstance().player).Infected));
            server(sp -> fact("infect.overworld.server_value", r3(TheabyssModVariables.getPlayerVariables(sp).Infected)));
        }, 140);
        add.add("infect.overworld floor", () -> server(sp -> fact("infect.overworld.after_180t", r4(TheabyssModVariables.getPlayerVariables(sp).Infected))), 5);

        reset(add, command, "the_abyss", "infect.anti");
        add.add("infect.anti set", () -> server(sp -> {
            setVar(sp, "Infected", 0.5);
            sp.addEffect(new MobEffectInstance(effect("theabyss:anti_infection"), 24000, 0, false, false));
        }), 40);
        add.add("infect.anti result", () -> {
            fact("infect.anti.client_value", r3(TheabyssModVariables.getPlayerVariables(Minecraft.getInstance().player).Infected));
            server(sp -> fact("infect.anti.server_value", r3(TheabyssModVariables.getPlayerVariables(sp).Infected)));
        }, 5);

        reset(add, command, "frost_world", "freeze.frost");
        add.add("freeze.frost set", () -> server(sp -> {
            setVar(sp, "Freeze", 0.5);
            saved[1] = 0.5;
        }), 100);
        add.add("freeze.frost result", () -> {
            fact("freeze.frost.client_value", r4(TheabyssModVariables.getPlayerVariables(Minecraft.getInstance().player).Freeze));
            server(sp -> fact("freeze.frost.server_delta_100t", r5(TheabyssModVariables.getPlayerVariables(sp).Freeze - saved[1])));
        }, 5);
        reset(add, command, "the_abyss", "freeze.abyss");
        add.add("freeze.abyss set", () -> server(sp -> setVar(sp, "Freeze", 0.5)), 40);
        add.add("freeze.abyss result", () -> server(sp -> fact("freeze.abyss.value_after_40t", r3(TheabyssModVariables.getPlayerVariables(sp).Freeze))), 5);
        add.add("freeze.abyss clear", () -> server(sp -> {
            setVar(sp, "Freeze", 0.0);
            setVar(sp, "Infected", 0.0);
        }), 5);
    }

    private static void afterLife(PossessionAutotest.StepAdder add, Consumer<String> command) {
        reset(add, command, "the_abyss", "afterlife.found");
        add.add("afterlife.found setup", () -> {
            command.accept("item replace entity @s inventory.5 with theabyss:artifact_of_after_life");
            maxQueue = 0;
        }, 10);
        add.add("afterlife.found die", () -> server(sp -> {
            deathAt = sp.position();
            sampling = true;
            sp.hurt(sp.damageSources().generic(), 1000.0F);
        }), 10);
        add.add("afterlife.found state", () -> server(sp -> {
            MobEffectInstance life = sp.getEffect(effect("theabyss:after_life"));
            fact("afterlife.found.alive", sp.isAlive());
            fact("afterlife.found.gamemode", sp.gameMode.getGameModeForPlayer().getName());
            fact("afterlife.found.effect", life == null ? "none" : life.getAmplifier() + "/" + (life.getDuration() > 760 && life.getDuration() <= 800));
            fact("afterlife.found.health", r1(sp.getHealth()));
            double dx = sp.getX() - deathAt.x;
            double dz = sp.getZ() - deathAt.z;
            fact("afterlife.found.moved_away_in_range", dx >= 0.0 && dx <= 128.0 && dz >= 0.0 && dz <= 128.0 && (dx > 0.0 || dz > 0.0));
            fact("afterlife.found.same_y", Math.abs(sp.getY() - deathAt.y) < 0.01);
            fact("afterlife.found.bodies_at_death", bodies(sp.serverLevel(), deathAt, 2.0));
            fact("afterlife.found.artifacts_left", count(sp, "theabyss:artifact_of_after_life"));
        }), 20);
        add.add("afterlife.found message", () -> server(sp -> sp.teleportTo(deathAt.x + 20.0, deathAt.y + 1.0, deathAt.z)), 10);
        add.add("afterlife.found return", () -> server(sp -> sp.teleportTo(deathAt.x + 1.0, deathAt.y, deathAt.z)), 5);
        add.add("afterlife.found returned", () -> server(sp -> {
            MobEffectInstance tantrum = sp.getEffect(effect("theabyss:tantrum"));
            fact("afterlife.found.after_life_removed", !sp.hasEffect(effect("theabyss:after_life")));
            fact("afterlife.found.tantrum", tantrum == null ? "none" : tantrum.getAmplifier() + "/" + (tantrum.getDuration() > 180));
            fact("afterlife.found.gamemode_right_after", sp.gameMode.getGameModeForPlayer().getName());
        }), 30);
        add.add("afterlife.found revived", () -> server(sp -> {
            fact("afterlife.found.gamemode_revived", sp.gameMode.getGameModeForPlayer().getName());
            fact("afterlife.found.health_revived", r1(sp.getHealth()));
            fact("afterlife.found.bodies_left", bodies(sp.serverLevel(), deathAt, 4.0));
        }), 5);

        reset(add, command, "the_abyss", "afterlife.timeout");
        add.add("afterlife.timeout setup", () -> {
            command.accept("item replace entity @s inventory.5 with theabyss:artifact_of_after_life");
            command.accept("gamemode adventure @s");
            server(sp -> deathsBefore = deaths(sp));
        }, 10);
        add.add("afterlife.timeout die", () -> server(sp -> {
            deathAt = sp.position();
            sp.hurt(sp.damageSources().generic(), 1000.0F);
        }), 10);
        add.add("afterlife.timeout expire", () -> server(sp -> sp.removeEffect(effect("theabyss:after_life"))), 2);
        add.add("afterlife.timeout mode", () -> server(sp -> fact("afterlife.timeout.gamemode_after_end", sp.gameMode.getGameModeForPlayer().getName())), 40);
        add.add("afterlife.timeout result", () -> server(sp -> {
            fact("afterlife.timeout.died", deaths(sp) > deathsBefore);
            fact("afterlife.timeout.gamemode_now", sp.gameMode.getGameModeForPlayer().getName());
        }), 5);
        add.add("afterlife.despawn wait", () -> {
        }, 760);
        add.add("afterlife.despawn result", () -> {
            sampling = false;
            fact("afterlife.max_scheduled_tasks", maxQueue);
            server(sp -> fact("afterlife.despawn.bodies_after_800t", bodies(sp.serverLevel().getServer().getLevel(ABYSS), deathAt, 300.0)));
        }, 5);
    }

    private static void dashAndSlice(PossessionAutotest.StepAdder add, Consumer<String> command) {
        reset(add, command, "the_abyss", "dash.ground");
        add.add("dash.ground setup", () -> {
            command.accept("item replace entity @s weapon.mainhand with theabyss:abyss_sword");
            command.accept("tp @s 0.5 150 0.5 0 0");
            command.accept("execute in theabyss:the_abyss run summon minecraft:zombie 0.5 150 1.7 {NoAI:1b,PersistenceRequired:1b,Tags:[\"proc\"]}");
            command.accept("execute in theabyss:the_abyss run summon minecraft:item 0.5 150 2.2 {Item:{id:\"minecraft:stone\",count:1},PickupDelay:32767,Tags:[\"proc\"]}");
        }, 20);
        add.add("dash.ground use", () -> {
            sounds();
            Minecraft mc = Minecraft.getInstance();
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            Vec3 v = mc.player.getDeltaMovement();
            fact("dash.ground.client_velocity", r1(v.x) + "," + r1(v.y) + "," + r1(v.z));
        }, 3);
        add.add("dash.ground effects", () -> server(sp -> {
            fact("dash.ground.speed", effectInfo(sp, "minecraft:speed"));
            fact("dash.ground.dash", effectInfo(sp, "theabyss:dash"));
            fact("dash.ground.flying", effectInfo(sp, "theabyss:flying"));
            fact("dash.ground.cooldown", sp.getCooldowns().isOnCooldown(sp.getMainHandItem().getItem()));
        }), 25);
        add.add("dash.ground result", () -> {
            fact("dash.ground.sound_dash", sound("theabyss:dash"));
            fact("dash.ground.sound_attack", sound("theabyss:nightblade_attack"));
            server(sp -> {
                Zombie zombie = first(sp, Zombie.class);
                fact("dash.ground.zombie_health", zombie == null ? "gone" : r1(zombie.getHealth()));
                fact("dash.ground.zombie_burning", zombie != null && zombie.getRemainingFireTicks() > 0);
                fact("dash.ground.item_entity_survived", !sp.serverLevel().getEntitiesOfClass(ItemEntity.class, ARENA).isEmpty());
            });
        }, 5);

        reset(add, command, "the_abyss", "dash.air");
        add.add("dash.air setup", () -> {
            command.accept("item replace entity @s weapon.mainhand with theabyss:abyss_sword");
            command.accept("tp @s 0.5 158 0.5 0 0");
            server(sp -> setVar(sp, "Mana", 200.0));
        }, 3);
        add.add("dash.air use", () -> {
            Minecraft mc = Minecraft.getInstance();
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        }, 3);
        add.add("dash.air effects", () -> server(sp -> {
            fact("dash.air.speed", effectInfo(sp, "minecraft:speed"));
            fact("dash.air.dash", effectInfo(sp, "theabyss:dash"));
            fact("dash.air.flying", effectInfo(sp, "theabyss:flying"));
        }), 10);
        add.add("dash.air mana", () -> server(sp -> fact("dash.air.mana_spent_13t", r1(200.0 - TheabyssModVariables.getPlayerVariables(sp).Mana))), 60);

        reset(add, command, "the_abyss", "slice");
        add.add("slice setup", () -> {
            command.accept("item replace entity @s weapon.mainhand with theabyss:abyss_sword");
            command.accept("tp @s 0.5 150 0.5 0 0");
            command.accept("execute in theabyss:the_abyss run summon minecraft:zombie 0.5 150 2.0 {NoAI:1b,PersistenceRequired:1b,Tags:[\"proc\"],attributes:[{id:\"minecraft:generic.max_health\",base:2000.0}],Health:2000.0f}");
            saved[3] = 0;
            saved[4] = 0;
            saved[5] = 0;
        }, 20);
        add.add("slice face", () -> command.accept("tp @s 0.5 150 0.5 0 10"), 5);
        for (int i = 0; i < 24; i++) {
            add.add("slice hit " + i, () -> {
                if (saved[4] >= 3) {
                    return;
                }
                sounds();
                Minecraft mc = Minecraft.getInstance();
                Entity target = mc.level.getEntitiesOfClass(Zombie.class, mc.player.getBoundingBox().inflate(5.0)).stream().findFirst().orElse(null);
                if (target != null) {
                    mc.gameMode.attack(mc.player, target);
                    saved[5]++;
                }
            }, 3);
            add.add("slice hit dmg " + i, () -> server(sp -> {
                Zombie zombie = first(sp, Zombie.class);
                saved[7] = zombie == null ? 0 : zombie.getHealth();
            }), 26);
            add.add("slice combo " + i, () -> {
                if (saved[4] >= 3) {
                    return;
                }
                int slices = sound("theabyss:blade_slices");
                server(sp -> {
                    Zombie zombie = first(sp, Zombie.class);
                    double extra = zombie == null ? 0 : saved[7] - zombie.getHealth();
                    if (slices > 0) {
                        saved[4]++;
                        fact("slice.combo" + (int)saved[4] + ".slice_sounds", slices);
                        fact("slice.combo" + (int)saved[4] + ".extra_damage", r1(extra));
                    }
                });
            }, 1);
        }

        add.add("slice report", () -> {
            fact("slice.combos_seen_max3", (int)saved[4]);
            TheabyssDevTest.LOGGER.info("[PROC-INFO] slice attacks sent {}", (int)saved[5]);
        }, 1);
    }

    private static void dreamShifter(PossessionAutotest.StepAdder add, Consumer<String> command) {
        reset(add, command, "overworld", "shifter");
        add.add("shifter setup", () -> {
            command.accept("item replace entity @s weapon.mainhand with theabyss:dream_shifter");
            command.accept("execute in minecraft:overworld run tp @s 3.3 200 4.6 0 0");
        }, 20);
        add.add("shifter use", () -> {
            sounds();
            Minecraft mc = Minecraft.getInstance();
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        }, 5);
        add.add("shifter waiting", () -> server(sp -> {
            fact("shifter.to_abyss.cooldown_at_start", sp.getCooldowns().isOnCooldown(sp.getMainHandItem().getItem()));
            fact("shifter.to_abyss.dimension_at_5t", sp.serverLevel().dimension().location().toString());
        }), 40);
        add.add("shifter arrived", () -> {
            fact("shifter.to_abyss.sound_entering", sound("theabyss:warp_entering"));
            fact("shifter.to_abyss.sound_entered", sound("theabyss:warp_entered"));
            server(sp -> {
                fact("shifter.to_abyss.dimension", sp.serverLevel().dimension().location().toString());
                fact("shifter.to_abyss.position", r1(sp.getX()) + "," + r1(sp.getY()) + "," + r1(sp.getZ()));
                fact("shifter.to_abyss.cooldown", sp.getCooldowns().isOnCooldown(sp.getMainHandItem().getItem()));
            });
        }, 600);
        add.add("shifter use back", () -> {
            Minecraft mc = Minecraft.getInstance();
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        }, 45);
        add.add("shifter back", () -> server(sp -> {
            fact("shifter.back.dimension", sp.serverLevel().dimension().location().toString());
            fact("shifter.back.position", r1(sp.getX()) + "," + r1(sp.getY()) + "," + r1(sp.getZ()));
        }), 5);
    }

    private static void home(PossessionAutotest.StepAdder add, Consumer<String> command) {
        reset(add, command, "overworld", "home");
        add.add("home bed", () -> {
            command.accept("execute in minecraft:overworld run setblock 5 200 5 minecraft:red_bed[facing=south,part=head]");
            command.accept("execute in minecraft:overworld run setblock 5 200 4 minecraft:red_bed[facing=south,part=foot]");
            command.accept("execute in minecraft:overworld run tp @s 5.5 200.6 5.5");
        }, 10);
        add.add("home sleep", () -> server(sp -> {
            setVar(sp, "HomeX", 0.0);
            sp.startSleeping(new BlockPos(5, 200, 5));
        }), 10);
        add.add("home wake", () -> server(sp -> {
            saved[0] = sp.getX();
            saved[1] = sp.getY();
            saved[2] = sp.getZ();
            sp.stopSleepInBed(true, true);
        }), 10);
        add.add("home result", () -> {
            TheabyssModVariables.PlayerVariables client = TheabyssModVariables.getPlayerVariables(Minecraft.getInstance().player);
            fact("home.client", client.RingOfHome + " " + r1(client.HomeX) + "," + r1(client.HomeY) + "," + r1(client.HomeZ));
            server(sp -> {
                TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(sp);
                fact("home.server", vars.RingOfHome + " " + r1(vars.HomeX) + "," + r1(vars.HomeY) + "," + r1(vars.HomeZ));
            });
        }, 5);
    }

    private static void phantomSoul(PossessionAutotest.StepAdder add, Consumer<String> command) {
        reset(add, command, "the_abyss", "phantom");
        add.add("phantom give", () -> {
            UNLUCK_ADDS.set(0);
            command.accept("item replace entity @s inventory.4 with theabyss:phantom_soul_item");
        }, 40);
        add.add("phantom result", () -> {
            fact("phantom.client_unluck", Minecraft.getInstance().player.hasEffect(effect("minecraft:unluck")));
            server(sp -> {
                fact("phantom.server_unluck", sp.hasEffect(effect("minecraft:unluck")));
                fact("phantom.unluck_amplifier", sp.getEffect(effect("minecraft:unluck")) == null ? -1 : sp.getEffect(effect("minecraft:unluck")).getAmplifier());
                fact("phantom.adv_ore", done(sp, "theabyss:ore_adv"));
                fact("phantom.adv_phantom_ingot", done(sp, "theabyss:phantom_ingot_adv"));
                fact("phantom.unluck_effect_adds_40t", UNLUCK_ADDS.get());
            });
        }, 5);
        add.add("phantom remove", () -> command.accept("clear @s theabyss:phantom_soul_item"), 40);
        add.add("phantom removed", () -> server(sp -> fact("phantom.unluck_after_removal_40t", sp.hasEffect(effect("minecraft:unluck")))), 5);
    }

    private static void rings(PossessionAutotest.StepAdder add, Consumer<String> command) {
        String[][] shots = {
            {"blackstrike", "theabyss:spell_of_telekinetic"},
            {"firestrike", "theabyss:spell_of_fire"},
        };
        for (String[] ring : shots) {
            String key = "ring." + ring[0];
            reset(add, command, "the_abyss", key);
            add.add(key + " setup", () -> {
                command.accept("item replace entity @s weapon.mainhand with theabyss:ring_of_" + ring[0]);
                command.accept("tp @s 0.5 150 0.5 0 0");
                command.accept("execute in theabyss:the_abyss run summon minecraft:zombie 0.5 150 8.5 {NoAI:1b,PersistenceRequired:1b,Tags:[\"proc\"]}");
                server(sp -> setVar(sp, "Mana", 200.0));
            }, 20);
            add.add(key + " cast", () -> {
                sounds();
                Minecraft mc = Minecraft.getInstance();
                mc.player.setXRot(4.5F);
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            }, 2);
            add.add(key + " projectile", () -> server(sp -> {
                fact(key + ".mana_spent", r1(200.0 - TheabyssModVariables.getPlayerVariables(sp).Mana));
                fact(key + ".cooldown", sp.getCooldowns().isOnCooldown(sp.getMainHandItem().getItem()));
                fact(key + ".projectiles", sp.serverLevel().getEntitiesOfClass(AbstractArrow.class, ARENA).size());
                fact(key + ".ring_damage", sp.getMainHandItem().getDamageValue());
            }), 30);
            add.add(key + " result", () -> {
                fact(key + ".sound_cast", sound(ring[1]));
                server(sp -> {
                    Zombie zombie = first(sp, Zombie.class);
                    fact(key + ".zombie_hit", zombie == null ? "gone" : r1(zombie.getMaxHealth() - zombie.getHealth()));
                    fact(key + ".zombie_effects", zombie == null ? "gone"
                        : effectInfo(zombie, "minecraft:slowness") + " " + effectInfo(zombie, "minecraft:nausea") + " " + effectInfo(zombie, "minecraft:weakness")
                            + " fire=" + (zombie.getRemainingFireTicks() > 0));
                    fact(key + ".client_mana", r1(TheabyssModVariables.getPlayerVariables(Minecraft.getInstance().player).Mana));
                });
            }, 5);
        }

        reset(add, command, "the_abyss", "ring.fangs");
        add.add("ring.fangs setup", () -> {
            command.accept("item replace entity @s weapon.mainhand with theabyss:ring_of_fangs");
            command.accept("execute in theabyss:the_abyss run summon minecraft:zombie 8.5 150 0.5 {NoAI:1b,PersistenceRequired:1b,Tags:[\"proc\"]}");
            command.accept("execute in theabyss:the_abyss run summon minecraft:item -8.5 150 0.5 {Item:{id:\"minecraft:stone\",count:1},PickupDelay:32767,Tags:[\"proc\"]}");
            server(sp -> setVar(sp, "Mana", 200.0));
        }, 20);
        add.add("ring.fangs cast", () -> {
            sounds();
            Minecraft mc = Minecraft.getInstance();
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            maxQueue = 0;
            server(sp -> saved[0] = 0);
        }, 1);
        for (int t = 0; t < 45; t++) {
            add.add("ring.fangs count " + t, () -> server(sp -> saved[0] = Math.max(saved[0], spikes(sp))), 1);
        }
        add.add("ring.fangs result", () -> server(sp -> {
            fact("ring.fangs.mana_spent", r1(200.0 - TheabyssModVariables.getPlayerVariables(sp).Mana));
            fact("ring.fangs.cooldown", sp.getCooldowns().isOnCooldown(sp.getMainHandItem().getItem()));
            fact("ring.fangs.ring_damage", sp.getMainHandItem().getDamageValue());
            fact("ring.fangs.spike_entities_created", spikeTotal);
            fact("ring.fangs.spike_near_item", spikeNearItem);
            Zombie zombie = first(sp, Zombie.class);
            fact("ring.fangs.zombie_hit", zombie == null ? "gone" : r1(zombie.getMaxHealth() - zombie.getHealth()));
        }), 5);

        reset(add, command, "the_abyss", "ring.flight");
        add.add("ring.flight hold", () -> {
            command.accept("tp @s 0.5 154 0.5");
            command.accept("item replace entity @s weapon.mainhand with theabyss:ring_of_flight");
            server(sp -> setVar(sp, "Mana", 500.0));
        }, 10);
        add.add("ring.flight sample", () -> server(sp -> saved[0] = TheabyssModVariables.getPlayerVariables(sp).Mana), 20);
        add.add("ring.flight result", () -> {
            fact("ring.flight.client_flying", Minecraft.getInstance().player.getAbilities().flying);
            server(sp -> {
                fact("ring.flight.server_flying", sp.getAbilities().flying);
                fact("ring.flight.mana_per_tick_mainhand", r2((saved[0] - TheabyssModVariables.getPlayerVariables(sp).Mana) / 20.0));
            });
        }, 1);
        add.add("ring.flight offhand", () -> {
            command.accept("item replace entity @s weapon.mainhand with minecraft:air");
            command.accept("item replace entity @s weapon.offhand with theabyss:ring_of_flight");
        }, 10);
        add.add("ring.flight offhand sample", () -> server(sp -> saved[0] = TheabyssModVariables.getPlayerVariables(sp).Mana), 20);
        add.add("ring.flight offhand result", () -> server(sp -> fact("ring.flight.mana_per_tick_offhand", r2((saved[0] - TheabyssModVariables.getPlayerVariables(sp).Mana) / 20.0))), 1);
        add.add("ring.flight stow", () -> {
            command.accept("item replace entity @s weapon.offhand with minecraft:air");
            command.accept("item replace entity @s inventory.8 with theabyss:ring_of_flight");
        }, 10);
        add.add("ring.flight stowed", () -> server(sp -> fact("ring.flight.flying_when_stowed_survival", sp.getAbilities().flying)), 1);
        add.add("ring.flight creative", () -> {
            command.accept("gamemode creative @s");
            command.accept("tp @s 0.5 154 0.5");
            server(sp -> {
                sp.getAbilities().flying = true;
                sp.onUpdateAbilities();
            });
        }, 10);
        add.add("ring.flight creative result", () -> server(sp -> fact("ring.flight.creative_flight_kept_with_ring_in_inventory", sp.getAbilities().flying)), 1);

        reset(add, command, "the_abyss", "ring.nature");
        add.add("ring.nature farm", () -> {
            command.accept("execute in theabyss:the_abyss run fill -2 149 -2 2 149 2 minecraft:farmland");
            command.accept("execute in theabyss:the_abyss run fill -2 150 -2 2 150 2 minecraft:wheat[age=0]");
            command.accept("item replace entity @s weapon.mainhand with theabyss:ring_of_nature");
            command.accept("tp @s 0.5 150 -3.5 0 40");
            server(sp -> setVar(sp, "Mana", 200.0));
        }, 20);
        add.add("ring.nature cast", () -> {
            sounds();
            Minecraft mc = Minecraft.getInstance();
            BlockPos crop = new BlockPos(0, 150, 0);
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(crop), net.minecraft.core.Direction.NORTH, crop, false));
        }, 10);
        add.add("ring.nature result", () -> {
            fact("ring.nature.sound_cast", sound("theabyss:spell_of_invisibility"));
            server(sp -> {
                int grown = 0;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        var state = sp.serverLevel().getBlockState(new BlockPos(dx, 150, dz));
                        if (state.getBlock() instanceof CropBlock crop && crop.getAge(state) > 0) {
                            grown++;
                        }
                    }
                }
                fact("ring.nature.grown_around", grown);
                fact("ring.nature.mana_spent", r1(200.0 - TheabyssModVariables.getPlayerVariables(sp).Mana));
                fact("ring.nature.ring_damage", sp.getMainHandItem().getDamageValue());
                fact("ring.nature.cooldown", sp.getCooldowns().isOnCooldown(sp.getMainHandItem().getItem()));
            });
        }, 5);
        add.add("ring.nature cleanup", () -> command.accept("execute in theabyss:the_abyss run fill -2 149 -2 2 150 2 minecraft:stone"), 5);

        reset(add, command, "the_abyss", "ring.slide");
        add.add("ring.slide ground", () -> {
            command.accept("item replace entity @s weapon.mainhand with theabyss:ring_of_slide");
            server(sp -> setVar(sp, "Mana", 200.0));
        }, 10);
        add.add("ring.slide ground use", () -> {
            Minecraft mc = Minecraft.getInstance();
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        }, 5);
        add.add("ring.slide ground result", () -> server(sp -> {
            fact("ring.slide.ground.flying", sp.hasEffect(effect("theabyss:flying")));
            fact("ring.slide.ground.mana_spent", r1(200.0 - TheabyssModVariables.getPlayerVariables(sp).Mana));
        }), 2);
        add.add("ring.slide air", () -> command.accept("tp @s 0.5 165 0.5 0 60"), 3);
        add.add("ring.slide air use", () -> {
            sounds();
            Minecraft mc = Minecraft.getInstance();
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        }, 3);
        add.add("ring.slide air state", () -> server(sp -> {
            fact("ring.slide.air.flying", sp.hasEffect(effect("theabyss:flying")));
            fact("ring.slide.air.cooldown", sp.getCooldowns().isOnCooldown(sp.getMainHandItem().getItem()));
            fact("ring.slide.air.ring_damage", sp.getMainHandItem().getDamageValue());
            saved[0] = TheabyssModVariables.getPlayerVariables(sp).Mana;
        }), 10);
        add.add("ring.slide air drain", () -> {
            fact("ring.slide.air.sound_cast", sound("theabyss:spell_of_misc"));
            server(sp -> {
                fact("ring.slide.air.mana_spent_cast", r1(200.0 - saved[0]));
                fact("ring.slide.air.drain_10t", r1(saved[0] - TheabyssModVariables.getPlayerVariables(sp).Mana));
            });
        }, 200);
        add.add("ring.slide landed", () -> server(sp -> {
            fact("ring.slide.landed.on_ground", sp.onGround());
            fact("ring.slide.landed.flying_removed", !sp.hasEffect(effect("theabyss:flying")));
            fact("ring.slide.landed.fall_damage_taken", sp.getHealth() < 20.0F);
        }), 5);
    }

    private static void clock(PossessionAutotest.StepAdder add, Consumer<String> command) {
        reset(add, command, "the_abyss", "clock");
        add.add("clock hold", () -> {
            command.accept("item replace entity @s weapon.mainhand with theabyss:clock_of_time");
            command.accept("tp @s 2.3 150 -3.6");
            server(sp -> setVar(sp, "Mana", 200.0));
        }, 10);
        add.add("clock save", () -> {
            sounds();
            Minecraft mc = Minecraft.getInstance();
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        }, 10);
        add.add("clock saved", () -> {
            fact("clock.save.client_active", TheabyssModVariables.getPlayerVariables(Minecraft.getInstance().player).ClockActive);
            server(sp -> {
                TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(sp);
                fact("clock.save.active", vars.ClockActive);
                fact("clock.save.position", r1(vars.ClockX) + "," + r1(vars.ClockY) + "," + r1(vars.ClockZ));
                fact("clock.save.mana_spent", r1(200.0 - vars.Mana));
                fact("clock.save.damage", sp.getMainHandItem().getDamageValue());
                sp.setHealth(9.0F);
                sp.getFoodData().setFoodLevel(5);
            });
            command.accept("tp @s 12.5 150 9.5");
        }, 10);
        add.add("clock recall", () -> {
            sounds();
            Minecraft mc = Minecraft.getInstance();
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        }, 60);
        add.add("clock recalled", () -> {
            fact("clock.recall.sound", sound("theabyss:spell_of_time"));
            server(sp -> {
                TheabyssModVariables.PlayerVariables vars = TheabyssModVariables.getPlayerVariables(sp);
                fact("clock.recall.position", r1(sp.getX()) + "," + r1(sp.getY()) + "," + r1(sp.getZ()));
                fact("clock.recall.health", r1(sp.getHealth()));
                fact("clock.recall.food", sp.getFoodData().getFoodLevel());
                fact("clock.recall.active", vars.ClockActive);
                fact("clock.recall.mana_spent", r1(200.0 - vars.Mana));
                fact("clock.recall.damage", sp.getMainHandItem().getDamageValue());
            });
        }, 5);
        add.add("clock dim save", () -> {
            Minecraft mc = Minecraft.getInstance();
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        }, 10);
        add.add("clock dim travel", () -> command.accept("execute in minecraft:overworld run tp @s 0.5 200 0.5"), 40);
        add.add("clock dim recall", () -> {
            Minecraft mc = Minecraft.getInstance();
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        }, 90);
        add.add("clock dim result", () -> server(sp -> {
            fact("clock.cross_dimension.dimension", sp.serverLevel().dimension().location().toString());
            fact("clock.cross_dimension.position", r1(sp.getX()) + "," + r1(sp.getY()) + "," + r1(sp.getZ()));
        }), 5);
    }

    private static void reset(PossessionAutotest.StepAdder add, Consumer<String> command, String dimension, String key) {
        String dim = dimension.equals("overworld") ? "minecraft:overworld" : "theabyss:" + dimension;
        int y = dimension.equals("the_abyss") ? 150 : 200;
        add.add(key + " reset", () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && (mc.player.isDeadOrDying() || mc.screen instanceof net.minecraft.client.gui.screens.DeathScreen)) {
                mc.player.respawn();
                mc.setScreen(null);
            }
            command.accept("kill @e[type=!minecraft:player,distance=..80]");
            command.accept("kill @e[type=minecraft:item,distance=..80]");
            command.accept("kill @e[type=minecraft:experience_orb,distance=..80]");
            command.accept("clear @s");
            command.accept("effect clear @s");
            command.accept("gamemode survival @s");
            command.accept("execute in " + dim + " run tp @s 0.5 " + y + " 0.5 0 0");
        }, 30);
        add.add(key + " state", () -> server(sp -> {
            sp.setHealth(sp.getMaxHealth());
            sp.getFoodData().setFoodLevel(20);
            sp.getFoodData().setSaturation(5.0F);
            sp.setRemainingFireTicks(0);
            sp.getAbilities().flying = false;
            sp.onUpdateAbilities();
            TheabyssModVariables.updatePlayerVariables(sp, v -> {
                v.Mana = 200.0;
                v.ManaUpgrade = 1.0;
                v.ManaCooldown = 1.0;
                v.ManaDamage = 0.0;
                v.Infected = 0.0;
                v.Freeze = 0.0;
                v.ClockActive = false;
                v.FearEffect = false;
                v.syncPlayerVariables(sp);
            });
        }), 10);
    }

    private static int spikeTotal;
    private static int spikeNearItem;
    private static final java.util.Set<java.util.UUID> SEEN_SPIKES = ConcurrentHashMap.newKeySet();

    private static int spikes(ServerPlayer sp) {
        List<Entity> list = sp.serverLevel().getEntities((Entity)null, ARENA, e -> BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString().equals("theabyss:nightblade_spike"));
        for (Entity e : list) {
            if (SEEN_SPIKES.add(e.getUUID())) {
                spikeTotal++;
                if (e.position().distanceTo(new Vec3(-8.5, 150, 0.5)) < 0.8) {
                    spikeNearItem++;
                }
            }
        }
        return list.size();
    }

    private static void listen() {
        if (listening) {
            return;
        }
        listening = true;
        Minecraft.getInstance().getSoundManager().addListener(LISTENER);
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return;
            }
            if (sampling) {
                double value = TheabyssModVariables.getPlayerVariables(mc.player).Infected;
                if (!Double.isNaN(lastInfected) && value != lastInfected) {
                    infectChanges++;
                }
                lastInfected = value;
                maxQueue = Math.max(maxQueue, queueSize());
            }
        });
        NeoForge.EVENT_BUS.addListener((MobEffectEvent.Added e) -> {
            if (e.getEntity() instanceof ServerPlayer && e.getEffectInstance().getEffect().value() == effect("minecraft:unluck").value()) {
                UNLUCK_ADDS.incrementAndGet();
            }
        });
    }

    private static int queueSize() {
        try {
            Field f = TheabyssMod.class.getDeclaredField("workQueue");
            f.setAccessible(true);
            return ((Collection<?>)f.get(null)).size();
        } catch (ReflectiveOperationException e) {
            return -1;
        }
    }

    private static void sounds() {
        SOUNDS.clear();
    }

    private static int sound(String id) {
        AtomicInteger n = SOUNDS.get(id);
        return n == null ? 0 : n.get();
    }

    private static void setVar(ServerPlayer sp, String field, double value) {
        TheabyssModVariables.updatePlayerVariables(sp, v -> {
            try {
                TheabyssModVariables.PlayerVariables.class.getField(field).setDouble(v, value);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
            v.syncPlayerVariables(sp);
        });
    }

    private static Holder<MobEffect> effect(String id) {
        return BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(id)).orElseThrow();
    }

    private static String effectInfo(LivingEntity entity, String id) {
        MobEffectInstance instance = entity.getEffect(effect(id));
        return instance == null ? "none" : instance.getAmplifier() + "@" + instance.getDuration();
    }

    private static int bodies(Level level, Vec3 at, double radius) {
        return level.getEntities((Entity)null, new AABB(at, at).inflate(radius),
            e -> BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString().equals("theabyss:player_body")).size();
    }

    private static <T extends Entity> T first(ServerPlayer sp, Class<T> type) {
        return sp.serverLevel().getEntitiesOfClass(type, ARENA).stream().findFirst().orElse(null);
    }

    private static int count(ServerPlayer sp, String id) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
        int n = 0;
        for (int i = 0; i < sp.getInventory().getContainerSize(); i++) {
            ItemStack stack = sp.getInventory().getItem(i);
            if (stack.is(item)) {
                n += stack.getCount();
            }
        }
        return n;
    }

    private static int deaths(ServerPlayer sp) {
        return sp.getStats().getValue(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.DEATHS));
    }

    private static boolean done(ServerPlayer sp, String id) {
        AdvancementHolder holder = sp.serverLevel().getServer().getAdvancements().get(ResourceLocation.parse(id));
        return holder != null && sp.getAdvancements().getOrStartProgress(holder).isDone();
    }

    private static String id(ItemStack stack) {
        return stack == null ? "none" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static ItemStack activationItem(Minecraft mc) {
        try {
            Field ticks = net.minecraft.client.renderer.GameRenderer.class.getDeclaredField("itemActivationTicks");
            Field item = net.minecraft.client.renderer.GameRenderer.class.getDeclaredField("itemActivationItem");
            ticks.setAccessible(true);
            item.setAccessible(true);
            return ticks.getInt(mc.gameRenderer) > 0 && item.get(mc.gameRenderer) instanceof ItemStack stack ? stack : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static void fact(String key, Object value) {
        TheabyssDevTest.LOGGER.info("[PROC] {}={}", key, value);
    }

    private static String r1(double v) {
        return String.format(Locale.ROOT, "%.1f", v);
    }

    private static String r2(double v) {
        return String.format(Locale.ROOT, "%.2f", v);
    }

    private static String r3(double v) {
        return String.format(Locale.ROOT, "%.3f", v);
    }

    private static String r4(double v) {
        return String.format(Locale.ROOT, "%.4f", v);
    }

    private static String r5(double v) {
        return String.format(Locale.ROOT, "%.5f", v);
    }

    private static void server(Consumer<ServerPlayer> action) {
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) {
            return;
        }
        java.util.UUID uuid = mc.player.getUUID();
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(uuid);
            if (sp != null) {
                try {
                    action.accept(sp);
                } catch (Throwable t) {
                    TheabyssDevTest.LOGGER.error("[PROC] server step failed", t);
                }
            }
        });
    }
}
