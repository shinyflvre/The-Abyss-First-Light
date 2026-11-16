package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yezon.theabyss.client.codex.CodexScreen;
import net.yezon.theabyss.init.TheabyssModEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.lore.ChronicleData;
import net.yezon.theabyss.lore.Chronicles;
import net.yezon.theabyss.network.TheabyssModVariables;

final class ChronicleAutotest {
    private static final ResourceKey<Level> ABYSS = ResourceKey.create(Registries.DIMENSION, Identifier.parse("theabyss:the_abyss"));
    private static final List<String> failures = new ArrayList<>();
    private static int checks;

    private ChronicleAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("chr reset", () -> {
            command.accept("gamemode creative @s");
            command.accept("execute in minecraft:overworld run forceload add -16 -16 16 16");
            command.accept("execute in minecraft:overworld run fill -6 199 -6 6 199 6 minecraft:stone");
            command.accept("execute in minecraft:overworld run fill -6 200 -6 6 206 6 minecraft:air");
            command.accept("execute in minecraft:overworld run tp @s 0 200 0");
            command.accept("difficulty normal");
            onServer(sp -> sp.setData(ChronicleData.TYPE, ChronicleData.NONE));
        }, 40);
        add.add("chr voices", () -> {
            int found = 0;
            for (int n = 1; n <= ChronicleData.COUNT; n++) {
                var event = mc.getSoundManager().getSoundEvent(net.yezon.theabyss.client.lore.ChronicleVoice.sound(n));
                if (event != null && event.getSound(net.minecraft.util.RandomSource.create()) != net.minecraft.client.sounds.SoundManager.EMPTY_SOUND) {
                    found++;
                }
            }
            check("all 20 voice recordings are loaded (" + found + ")", found == ChronicleData.COUNT);
        }, 2);
        add.add("chr codex locked", () -> CodexScreen.openCategory("chronicles"), 20);
        add.add("chr shot locked", () -> shot.accept("115_chronicles_0_locked"), 4);
        add.add("chr close", () -> mc.gui.setScreen(null), 4);
        add.add("chr enter abyss", () -> command.accept("execute in theabyss:the_abyss run tp @s 0 120 0"), 80);
        add.add("chr check crossing", () -> page("crossing on entering the Abyss", Chronicles.CROSSING), 2);
        add.add("chr subtitle wait", () -> {
        }, 60);
        add.add("chr shot subtitle", () -> shot.accept("115_chronicles_5_subtitle"), 120);
        add.add("chr shot subtitle 2", () -> shot.accept("115_chronicles_6_subtitle_later"), 2);
        add.add("chr kill wolf", () -> kill(TheabyssModEntities.INFECTED_WOLF.get()), 10);
        add.add("chr check wolf", () -> {
            page("familiar eyes on an infected kill", Chronicles.FAMILIAR_EYES);
            page("black ink on the first infected wolf", Chronicles.WOLF);
        }, 2);
        add.add("chr kill phantom", () -> kill(TheabyssModEntities.INFECTED_PHANTOM.get()), 10);
        add.add("chr check phantom", () -> page("kneeling ones on a phantom kill", Chronicles.KNEELING), 2);
        add.add("chr kill abyssaur", () -> kill(TheabyssModEntities.ABYSSAUR.get()), 40);
        add.add("chr check abyssaur", () -> page("old giants on an abyssaur kill", Chronicles.GIANTS), 2);
        add.add("chr somnium full", () -> onServer(sp -> TheabyssModVariables.updatePlayerVariables(sp, v -> {
            v.Mana = 50.0;
            v.syncPlayerVariables(sp);
        })), 20);
        add.add("chr somnium empty", () -> onServer(sp -> TheabyssModVariables.updatePlayerVariables(sp, v -> {
            v.Mana = 0.0;
            v.syncPlayerVariables(sp);
        })), 20);
        add.add("chr check voice", () -> page("the voice when Somnium runs empty", Chronicles.VOICE), 2);
        add.add("chr drink item", () -> command.accept("item replace entity @s weapon.mainhand with theabyss:somnium_regen_upgrade"), 10);
        add.add("chr drink", () -> mc.options.keyUse.setDown(true), 50);
        add.add("chr drink release", () -> mc.options.keyUse.setDown(false), 4);
        add.add("chr check hand", () -> page("the hand after drinking an upgrade", Chronicles.HAND), 2);
        add.add("chr eye", () -> onServer(sp -> sp.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TheabyssModItems.EYE_OF_ABYSS.get()))), 20);
        add.add("chr check eye", () -> page("last entry when holding the eye", Chronicles.LAST_ENTRY), 2);
        add.add("chr die", () -> onServer(sp -> {
            sp.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            sp.hurtServer((ServerLevel)sp.level(), sp.damageSources().fellOutOfWorld(), 1000.0F);
        }), 30);
        add.add("chr respawn", () -> {
            if (mc.player != null) {
                mc.player.respawn();
            }
            mc.gui.setScreen(null);
        }, 60);
        add.add("chr check death", () -> page("black ink after dying in the Abyss", Chronicles.DEATH), 2);
        add.add("chr to overworld", () -> command.accept("execute in minecraft:overworld run tp @s 0 200 0"), 40);
        add.add("chr to abyss again", () -> command.accept("execute in theabyss:the_abyss run tp @s 0 120 0"), 60);
        add.add("chr back again", () -> command.accept("execute in minecraft:overworld run tp @s 0 200 0"), 40);
        add.add("chr check return", () -> page("black ink on the first return", Chronicles.FIRST_RETURN), 2);
        add.add("chr frost", () -> command.accept("execute in theabyss:frost_world run tp @s 0 120 0"), 80);
        add.add("chr check frost", () -> page("ice on entering the frost world", Chronicles.ICE), 2);
        add.add("chr hooks", () -> onServer(sp -> {
            Chronicles.note(sp, Chronicles.SOMNIUM);
            Chronicles.note(sp, Chronicles.ROOTS);
            Chronicles.note(sp, Chronicles.PILLARS);
            Chronicles.note(sp, Chronicles.ALTAR);
            Chronicles.note(sp, Chronicles.SOUL_HEART);
        }), 10);
        add.add("chr boss", () -> command.accept("execute in theabyss:the_abyss run tp @s 0 120 0"), 60);
        add.add("chr kill boss", () -> kill(TheabyssModEntities.NIGHTBLADE_BOSS.get()), 200);
        add.add("chr check boss", () -> {
            page("fallen when the nightblade dies", Chronicles.FALLEN);
            onServer(sp -> check("boss defeated flag", ChronicleData.of(sp).bossDefeated()));
        }, 2);
        add.add("chr water", () -> {
            command.accept("execute in minecraft:overworld run setblock 2 200 2 minecraft:water");
            command.accept("execute in minecraft:overworld run tp @s 2.5 200 2.5");
        }, 60);
        add.add("chr check after", () -> {
            page("after in the water", Chronicles.AFTER);
            page("journal on the return after the fight", Chronicles.JOURNAL);
            page("the end once all others are written", Chronicles.END);
        }, 2);
        add.add("chr codex list", () -> CodexScreen.openCategory("chronicles"), 20);
        add.add("chr shot list", () -> shot.accept("115_chronicles_1_list"), 4);
        add.add("chr codex journal", () -> CodexScreen.openEntry("chronicle_01", 0), 20);
        add.add("chr shot journal", () -> shot.accept("115_chronicles_2_journal"), 4);
        add.add("chr codex black", () -> CodexScreen.openEntry("chronicle_06", 0), 20);
        add.add("chr shot black", () -> shot.accept("115_chronicles_3_black_ink"), 4);
        add.add("chr codex end", () -> CodexScreen.openEntry("chronicle_20", 0), 20);
        add.add("chr shot end", () -> shot.accept("115_chronicles_4_end"), 4);
        add.add("chr close 2", () -> mc.gui.setScreen(null), 4);
        add.add("chr result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT chronicles {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
    }

    private static void kill(EntityType<? extends LivingEntity> type) {
        onServer(sp -> {
            ServerLevel level = (ServerLevel)sp.level();
            LivingEntity e = type.create(level, EntitySpawnReason.COMMAND);
            if (e == null) {
                check("spawn " + type, false);
                return;
            }
            BlockPos at = sp.blockPosition().offset(3, 0, 0);
            e.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0F, 0.0F);
            level.addFreshEntity(e);
            e.hurtServer(level, sp.damageSources().playerAttack(sp), 100000.0F);
            if (e.isAlive()) {
                e.kill(level);
            }
        });
    }

    private static void page(String name, int page) {
        onServer(sp -> check(name + " (page " + page + ")", ChronicleData.of(sp).has(page)));
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] chronicles check {}: {}", name, ok ? "ok" : "FAILED");
    }

    private static void onServer(Consumer<ServerPlayer> action) {
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) {
            return;
        }
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp != null) {
                action.accept(sp);
            }
        });
    }
}
