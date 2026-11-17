package net.yezon.theabyss.devtest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;

final class ThunderAutotest {
    private record Heard(String event, String file, float volume, float pitch, SoundInstance.Attenuation attenuation, String dimension) {
    }

    private static final List<Heard> heard = new ArrayList<>();
    private static final List<String> failures = new ArrayList<>();
    private static int checks;
    private static final SoundEventListener LISTENER = (sound, events, range) -> {
        String id = sound.getLocation().toString();
        if (id.contains("lightning") || id.contains("thunder")) {
            Minecraft mc = Minecraft.getInstance();
            heard.add(new Heard(id, sound.getSound() != null ? sound.getSound().getLocation().toString() : "?", sound.getVolume(), sound.getPitch(),
                sound.getAttenuation(), mc.level != null ? mc.level.dimension().location().toString() : "?"));
        }
    };

    private ThunderAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command) {
        add.add("th abyss", () -> {
            command.accept("gamemode creative @s");
            command.accept("execute in theabyss:the_abyss run forceload add -8 -8 8 8");
            command.accept("execute in theabyss:the_abyss run fill -3 119 -3 3 119 3 minecraft:stone");
            command.accept("execute in theabyss:the_abyss run fill -3 120 -3 3 124 3 minecraft:air");
            command.accept("execute in theabyss:the_abyss run tp @s 0.5 120 0.5 0 0");
            Minecraft.getInstance().getSoundManager().addListener(LISTENER);
        }, 80);
        for (int i = 0; i < 10; i++) {
            int n = i;
            add.add("th strike abyss " + n, () -> strike(n), 12);
        }
        add.add("th check abyss", () -> {
            List<Heard> abyss = heard.stream().filter(h -> h.dimension().equals("theabyss:the_abyss")).toList();
            abyss.forEach(h -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] thunder heard {}", h));
            List<Heard> thunder = abyss.stream().filter(h -> h.event().equals("theabyss:abyss_thunder")).toList();
            List<Heard> impact = abyss.stream().filter(h -> h.event().equals("theabyss:abyss_thunder_impact")).toList();
            check("10 strikes -> own thunder for each, storm may add more (" + thunder.size() + ")", thunder.size() >= 10);
            check("no vanilla lightning sound in the Abyss", abyss.stream().noneMatch(h -> h.event().startsWith("minecraft:")));
            check("thunder volume 0.5", thunder.stream().allMatch(h -> Math.abs(h.volume() - 0.5F) < 1.0E-4F));
            check("thunder pitch within 0.8 .. 1.2", thunder.stream().allMatch(h -> h.pitch() >= 0.8F && h.pitch() <= 1.2F));
            check("thunder pitch varies", thunder.stream().map(Heard::pitch).distinct().count() > 5);
            check("thunder heard everywhere (no attenuation)", thunder.stream().allMatch(h -> h.attenuation() == SoundInstance.Attenuation.NONE));
            Set<String> files = new HashSet<>();
            thunder.forEach(h -> files.add(h.file()));
            check("both new recordings used " + files, files.contains("theabyss:weather/thunder_01") && files.contains("theabyss:weather/thunder_02"));
            check("impact at half volume (" + impact.size() + ")", impact.size() >= 10 && impact.stream().allMatch(h -> Math.abs(h.volume() - 0.5F) < 1.0E-4F));
            command.accept("execute in minecraft:overworld run tp @s 0.5 200 0.5 0 0");
        }, 120);
        add.add("th strike overworld", () -> strike(0), 20);
        add.add("th check overworld", () -> {
            List<Heard> over = heard.stream().filter(h -> h.dimension().equals("minecraft:overworld")).toList();
            over.forEach(h -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] thunder heard {}", h));
            check("Overworld keeps the vanilla thunder", over.stream().anyMatch(h -> h.event().equals("minecraft:entity.lightning_bolt.thunder")));
            Minecraft.getInstance().getSoundManager().removeListener(LISTENER);
        }, 4);
        add.add("th result", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] RESULT thunder {} ({} checks{})", failures.isEmpty() ? "PASS" : "FAIL",
            checks, failures.isEmpty() ? "" : ", failed: " + failures), 2);
    }

    private static void strike(int n) {
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) {
            return;
        }
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp == null) {
                return;
            }
            double angle = n * 0.9;
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sp.serverLevel());
            if (bolt != null) {
                bolt.moveTo(new Vec3(sp.getX() + Math.cos(angle) * 40.0, sp.getY(), sp.getZ() + Math.sin(angle) * 40.0));
                bolt.setVisualOnly(true);
                sp.serverLevel().addFreshEntity(bolt);
            }
        });
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(name);
        }
        TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] thunder check {}: {}", name, ok ? "ok" : "FAILED");
    }
}
