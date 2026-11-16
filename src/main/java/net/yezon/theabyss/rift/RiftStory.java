package net.yezon.theabyss.rift;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.configuration.ConfigConfiguration;
import net.yezon.theabyss.entity.LizardEntity;
import net.yezon.theabyss.lore.ChronicleData;
import net.yezon.theabyss.network.DialogueMessage;
import net.yezon.theabyss.network.DialogueReplyMessage;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class RiftStory {
    private static final String DATA = "theabyss_rift_story";
    private static final String SPEAKER = "dialog.theabyss.speaker.unknown";
    private static final String INFO = "dialog.theabyss.speaker.info";
    static final String LINE = "dialog.theabyss.rift.";
    private static final String CHOICE = "dialog.theabyss.rift.choice.";
    private static final int MIN_USES = 5;
    private static final int MAX_USES = 10;
    private static final int STAGE_NONE = 0;
    private static final int STAGE_ARRIVED = 1;
    private static final int STAGE_QUESTIONS = 2;
    private static final int STAGE_STORY = 3;
    private static final int STAGE_BLESSING = 4;
    private static final int STAGE_DONE = 5;
    private static final double TALK_RANGE = 5.0;
    private static final double AWAY_RANGE = 9.0;
    private static final double LEAVE_RANGE = 14.0;
    private static final int NUDGE_TICKS = 600;
    private static final int CALL_TIMEOUT = 600;
    private static final int HIT_COOLDOWN = 40;
    private static final long WHISPER_COOLDOWN = 12000L;
    private static final int SIGHT_INTERVAL = 40;
    private static final double SIGHT_RANGE = 16.0;
    private static final ResourceKey<Level> ABYSS = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, "the_abyss"));
    private static final String[] WHISPERS = {"whisper.1", "whisper.2", "whisper.3", "whisper.4", "whisper.5", "whisper.6", "whisper.7", "whisper.8",
        "whisper.9", "whisper.10"};
    private static final Map<String, java.util.function.Predicate<ServerPlayer>> VARIANTS = Map.of(
        "story.4", p -> !deed(p, 6),
        "story.5", p -> !deed(p, 5) || !deed(p, 10),
        "a.where.2", p -> !deed(p, 6),
        "b.believe.2", p -> !deed(p, 8),
        "topic.voice.q2.2", p -> !deed(p, 12),
        "topic.altar.q1.1", p -> !deed(p, 12),
        "topic.altar.q4.2", p -> !deed(p, 12) || !ConfigConfiguration.loresEnabled(),
        "topic.doubt.q2.2", p -> !deed(p, 3) || !ConfigConfiguration.loresEnabled()
    );
    private static final String[] QUESTIONS_A = {"who", "where", "why"};
    private static final String[] QUESTIONS_B = {"believe", "stop", "want"};
    private static final String[] BLESSINGS = {"undying", "strength", "night"};
    private static final AtomicInteger IDS = new AtomicInteger();
    private static final Map<UUID, Active> ACTIVE = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> CALLING = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> NEXT_NUDGE = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> NEXT_HIT = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> ARMED = new ConcurrentHashMap<>();

    private record Active(int id, List<String> actions, boolean throne) {
    }

    private RiftStory() {
    }

    static CompoundTag data(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(DATA)) {
            root.put(DATA, new CompoundTag());
        }
        return root.getCompoundOrEmpty(DATA);
    }

    public static boolean called(ServerPlayer player) {
        return data(player).getBooleanOr("called", false);
    }

    static boolean deed(ServerPlayer player, int page) {
        return (data(player).getIntOr("deeds", 0) & 1 << page) != 0 || ChronicleData.of(player).has(page);
    }

    public static boolean bossDefeated(ServerPlayer player) {
        return ChronicleData.of(player).bossDefeated() || deed(player, 17);
    }

    private static boolean silent(ServerPlayer player) {
        return !ConfigConfiguration.storyEnabled() || bossDefeated(player);
    }

    private static List<String> resolve(ServerPlayer player, List<String> keys) {
        List<String> out = new ArrayList<>(keys.size());
        for (String key : keys) {
            String name = key.startsWith(LINE) ? key.substring(LINE.length()) : key;
            java.util.function.Predicate<ServerPlayer> swap = VARIANTS.get(name);
            out.add(swap != null && swap.test(player) ? key + ".alt" : key);
        }
        return out;
    }

    private static int stage(ServerPlayer player) {
        return data(player).getIntOr("stage", STAGE_NONE);
    }

    private static void setStage(ServerPlayer player, int stage) {
        data(player).putInt("stage", stage);
    }

    public static void onGauntletCast(ServerPlayer player) {
        if (!ConfigConfiguration.storyEnabled()) {
            return;
        }
        CompoundTag data = data(player);
        if (data.getBooleanOr("called", false)) {
            return;
        }
        if (data.getIntOr("need", 0) <= 0) {
            data.putInt("need", MIN_USES + player.getRandom().nextInt(MAX_USES - MIN_USES + 1));
        }
        data.putInt("uses", data.getIntOr("uses", 0) + 1);
    }

    static List<String> lines(String... names) {
        List<String> keys = new ArrayList<>();
        for (String name : names) {
            keys.add(LINE + name);
        }
        return keys;
    }

    public static void voice(ServerPlayer player, String line) {
        if (!busy(player)) {
            say(player, lines(line), false);
        }
    }

    static void say(ServerPlayer player, List<String> lines, boolean hold) {
        DialogueMessage.send(player, new DialogueMessage(IDS.incrementAndGet(), SPEAKER, resolve(player, lines), List.of(), hold));
    }

    private static String choiceKey(String action) {
        String group = action.substring(0, action.indexOf(':'));
        String name = action.substring(action.indexOf(':') + 1);
        return CHOICE + switch (group) {
            case "yes", "no" -> group;
            case "t" -> "topic." + name;
            case "e" -> "enough";
            default -> name;
        };
    }

    static void ask(ServerPlayer player, List<String> lines, List<String> actions, boolean throne) {
        int id = IDS.incrementAndGet();
        List<String> choices = new ArrayList<>();
        for (String action : actions) {
            choices.add(choiceKey(action));
        }
        ACTIVE.put(player.getUUID(), new Active(id, actions, throne));
        DialogueMessage.send(player, new DialogueMessage(id, SPEAKER, resolve(player, lines), choices, true));
    }

    private static void close(ServerPlayer player) {
        ACTIVE.remove(player.getUUID());
        DialogueMessage.send(player, new DialogueMessage(IDS.incrementAndGet(), SPEAKER, List.of(), List.of(), false));
    }

    private static boolean busy(ServerPlayer player) {
        return ACTIVE.containsKey(player.getUUID()) || CALLING.containsKey(player.getUUID());
    }

    static String random(ServerPlayer player, String... names) {
        return names[player.getRandom().nextInt(names.length)];
    }

    private static void startCall(ServerPlayer player) {
        int id = IDS.incrementAndGet();
        CALLING.put(player.getUUID(), player.level().getGameTime());
        ACTIVE.put(player.getUUID(), new Active(id, List.of(), false));
        DialogueMessage.send(player, new DialogueMessage(id, SPEAKER, lines("call.1", "call.2", "call.3", "call.4"), List.of(), false));
    }

    private static void finishCall(ServerPlayer player) {
        CALLING.remove(player.getUUID());
        ACTIVE.remove(player.getUUID());
        data(player).putBoolean("called", true);
        RiftOfSins.summon(player);
    }

    private static void switchInfo(ServerPlayer player) {
        CompoundTag data = data(player);
        if (stage(player) >= STAGE_DONE && !data.getBooleanOr("toldSwitch", false)) {
            data.putBoolean("toldSwitch", true);
            DialogueMessage.send(player, new DialogueMessage(IDS.incrementAndGet(), INFO, List.of(LINE + "info.switch"), List.of(), false));
        }
    }

    public static void onArrive(ServerPlayer player) {
        if (!RiftOfSins.isRift(player.level()) || silent(player)) {
            return;
        }
        data(player).remove("whisperPending");
        NEXT_NUDGE.put(player.getUUID(), player.level().getGameTime() + NUDGE_TICKS);
        if (stage(player) == STAGE_NONE) {
            setStage(player, STAGE_ARRIVED);
            RiftTopics.onArrive(player, false);
            say(player, lines("arrival.1", "arrival.2", "arrival.3"), true);
            return;
        }
        boolean spoke = !busy(player) && RiftTopics.onArrive(player, stage(player) >= STAGE_DONE);
        if (!spoke && !busy(player)) {
            switchInfo(player);
        }
    }

    static void requestWhisper(ServerPlayer player) {
        if (called(player) && !silent(player)) {
            data(player).putBoolean("whisperPending", true);
        }
    }

    private static void tickWhisper(ServerPlayer player) {
        CompoundTag data = data(player);
        if (!data.getBooleanOr("whisperPending", false) || busy(player)) {
            return;
        }
        long now = player.level().getServer().overworld().getGameTime();
        if (now < data.getLongOr("lastWhisper", Long.MIN_VALUE / 2) + WHISPER_COOLDOWN) {
            return;
        }
        data.remove("whisperPending");
        data.putLong("lastWhisper", now);
        say(player, lines(random(player, WHISPERS)), false);
        DialogueMessage.send(player, new DialogueMessage(IDS.incrementAndGet(), INFO, List.of(LINE + "info.enter"), List.of(), false));
    }

    public static void onDeed(ServerPlayer player, int page) {
        data(player).putInt("deeds", data(player).getIntOr("deeds", 0) | 1 << page);
        if (!silent(player)) {
            RiftTopics.onDeed(player, page);
        }
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player || !(event.getSource().getEntity() instanceof ServerPlayer player) || silent(player)) {
            return;
        }
        if (TheabyssMod.MODID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).getNamespace())) {
            RiftTopics.onKill(player);
        }
    }

    private static void watchWorld(ServerPlayer player) {
        if (player.level().dimension() == ABYSS) {
            RiftTopics.onAbyssTick(player);
        }
        if (player.tickCount % SIGHT_INTERVAL == 0 && !data(player).getBooleanOr("sawLizard", false)) {
            for (LizardEntity lizard : player.level().getEntitiesOfClass(LizardEntity.class, player.getBoundingBox().inflate(SIGHT_RANGE))) {
                if (player.hasLineOfSight(lizard)) {
                    data(player).putBoolean("sawLizard", true);
                    RiftTopics.unlockEvent(player, "small");
                    break;
                }
            }
        }
    }

    public static void onVoid(ServerPlayer player) {
        if (!busy(player) && !silent(player)) {
            say(player, lines("void.1"), false);
        }
    }

    public static void onLeave(ServerPlayer player) {
        ACTIVE.remove(player.getUUID());
        if (silent(player)) {
            return;
        }
        say(player, lines(stage(player) >= STAGE_DONE ? "leave.after" : "leave.before"), false);
    }

    public static void onTalk(ServerPlayer player) {
        if (busy(player) || silent(player)) {
            return;
        }
        if (stage(player) >= STAGE_DONE) {
            RiftTopics.talk(player);
        } else {
            ARMED.put(player.getUUID(), false);
            startThrone(player);
        }
    }

    public static void onHit(ServerPlayer player) {
        long now = player.level().getGameTime();
        if (busy(player) || silent(player) || now < NEXT_HIT.getOrDefault(player.getUUID(), Long.MIN_VALUE)) {
            return;
        }
        NEXT_HIT.put(player.getUUID(), now + HIT_COOLDOWN);
        say(player, lines(random(player, "hit.1", "hit.2")), false);
    }

    private static void startThrone(ServerPlayer player) {
        switch (stage(player)) {
            case STAGE_NONE, STAGE_ARRIVED -> {
                setStage(player, STAGE_QUESTIONS);
                ask(player, lines("open.1", "open.2"), remaining(player, "askedA", QUESTIONS_A, "a"), true);
            }
            case STAGE_QUESTIONS -> ask(player, lines("open.1"), remaining(player, "askedA", QUESTIONS_A, "a"), true);
            case STAGE_STORY -> ask(player, lines("story.5"), questionsB(player), true);
            case STAGE_BLESSING -> ask(player, lines("c.return.1"), blessings(player), true);
            default -> {
            }
        }
    }

    private static List<String> remaining(ServerPlayer player, String key, String[] questions, String prefix) {
        int asked = data(player).getIntOr(key, 0);
        List<String> actions = new ArrayList<>();
        for (int i = 0; i < questions.length; i++) {
            if ((asked & 1 << i) == 0) {
                actions.add(prefix + ":" + questions[i]);
            }
        }
        return actions;
    }

    private static List<String> questionsB(ServerPlayer player) {
        List<String> actions = remaining(player, "askedB", QUESTIONS_B, "b");
        if (data(player).getIntOr("askedB", 0) != 0) {
            actions.add("b:understand");
        }
        return actions;
    }

    private static List<String> blessings(ServerPlayer player) {
        List<String> actions = new ArrayList<>();
        for (String blessing : BLESSINGS) {
            actions.add("c:" + blessing);
        }
        actions.add(data(player).getBooleanOr("toldMore", false) ? "c:time" : "c:more");
        return actions;
    }

    private static void mark(ServerPlayer player, String key, String[] questions, String question) {
        for (int i = 0; i < questions.length; i++) {
            if (questions[i].equals(question)) {
                data(player).putInt(key, data(player).getIntOr(key, 0) | 1 << i);
            }
        }
    }

    public static void onReply(ServerPlayer player, int id, int choice) {
        Active active = ACTIVE.get(player.getUUID());
        if (active == null || active.id() != id) {
            return;
        }
        if (choice == DialogueReplyMessage.FINISHED || choice == DialogueReplyMessage.CANCEL) {
            if (CALLING.containsKey(player.getUUID())) {
                finishCall(player);
            } else if (active.actions().isEmpty() || choice == DialogueReplyMessage.CANCEL) {
                ACTIVE.remove(player.getUUID());
                switchInfo(player);
            }
            return;
        }
        if (choice < 0 || choice >= active.actions().size()) {
            return;
        }
        ACTIVE.remove(player.getUUID());
        handle(player, active.actions().get(choice));
    }

    private static void handle(ServerPlayer player, String action) {
        String[] parts = action.split(":");
        String group = parts[0];
        String name = parts[1];
        if (RiftTopics.handle(player, group, name)) {
            return;
        }
        switch (group) {
            case "a" -> {
                mark(player, "askedA", QUESTIONS_A, name);
                List<String> said = lines(answerLines(name));
                List<String> left = remaining(player, "askedA", QUESTIONS_A, "a");
                if (left.isEmpty()) {
                    setStage(player, STAGE_STORY);
                    said.addAll(lines("story.1", "story.2", "story.3", "story.4", "story.5"));
                    ask(player, said, questionsB(player), true);
                } else {
                    ask(player, said, left, true);
                }
            }
            case "b" -> {
                if (name.equals("understand")) {
                    setStage(player, STAGE_BLESSING);
                    ask(player, lines("b.understand.1", "b.understand.2"), blessings(player), true);
                } else {
                    mark(player, "askedB", QUESTIONS_B, name);
                    ask(player, lines(answerLines(name)), questionsB(player), true);
                }
            }
            case "c" -> {
                switch (name) {
                    case "more" -> {
                        data(player).putBoolean("toldMore", true);
                        ask(player, lines("c.more.1", "c.more.2", "c.more.3"), blessings(player), true);
                    }
                    case "time" -> say(player, lines("c.time.1"), true);
                    default -> ask(player, lines("confirm." + name), List.of("yes:" + name, "no:" + name), true);
                }
            }
            case "yes" -> {
                boolean changed = !data(player).getStringOr("blessing", "").isEmpty();
                data(player).putString("blessing", name);
                setStage(player, STAGE_DONE);
                int id = IDS.incrementAndGet();
                ACTIVE.put(player.getUUID(), new Active(id, List.of(), true));
                List<String> said = changed ? lines("chosen.1", "chosen.2") : lines("chosen.1", "chosen.2", "chosen.3", "chosen.4");
                DialogueMessage.send(player, new DialogueMessage(id, SPEAKER, said, List.of(), true));
            }
            case "d" -> {
                if (name.equals("gifts")) {
                    close(player);
                    net.yezon.theabyss.skill.Skills.open(player);
                } else if (name.equals("change")) {
                    List<String> actions = new ArrayList<>();
                    String current = data(player).getStringOr("blessing", "");
                    for (String blessing : BLESSINGS) {
                        if (!blessing.equals(current)) {
                            actions.add("c:" + blessing);
                        }
                    }
                    actions.add("d:nevermind");
                    ask(player, lines("change.1"), actions, true);
                } else {
                    say(player, lines("c.time.1"), true);
                }
            }
            case "no" -> ask(player, lines("b.understand.2"), blessings(player), true);
            default -> {
            }
        }
    }

    private static String[] answerLines(String question) {
        return switch (question) {
            case "who" -> new String[] {"a.who.1", "a.who.2", "a.who.3"};
            case "where" -> new String[] {"a.where.1", "a.where.2"};
            case "why" -> new String[] {"a.why.1", "a.why.2"};
            case "believe" -> new String[] {"b.believe.1", "b.believe.2"};
            case "stop" -> new String[] {"b.stop.1", "b.stop.2"};
            default -> new String[] {"b.want.1"};
        };
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator() || silent(player)) {
            return;
        }
        watchWorld(player);
        UUID id = player.getUUID();
        long now = player.level().getGameTime();
        Long calling = CALLING.get(id);
        if (calling != null) {
            if (now - calling > CALL_TIMEOUT) {
                finishCall(player);
            }
            return;
        }
        if (!RiftOfSins.isRift(player.level())) {
            tickWhisper(player);
            CompoundTag data = data(player);
            if (!data.getBooleanOr("called", false) && data.getIntOr("need", 0) > 0 && data.getIntOr("uses", 0) >= data.getIntOr("need", 0)
                && player.isAlive() && player.onGround() && !busy(player)) {
                startCall(player);
            }
            return;
        }
        Vec3 throne = RiftOfSins.nightbladePos();
        double distance = player.position().distanceTo(throne);
        if (distance > AWAY_RANGE) {
            ARMED.put(id, true);
        }
        Active active = ACTIVE.get(id);
        if (active != null && active.throne() && distance > LEAVE_RANGE) {
            close(player);
            return;
        }
        int stage = stage(player);
        if (busy(player)) {
            return;
        }
        if (distance <= TALK_RANGE && ARMED.getOrDefault(id, true)) {
            if (stage < STAGE_DONE) {
                ARMED.put(id, false);
                startThrone(player);
            } else if (RiftTopics.pending(player)) {
                ARMED.put(id, false);
                RiftTopics.autoStart(player);
            }
        } else if (stage <= STAGE_ARRIVED && distance > AWAY_RANGE && now >= NEXT_NUDGE.computeIfAbsent(id, key -> now + NUDGE_TICKS)) {
            NEXT_NUDGE.put(id, now + NUDGE_TICKS);
            say(player, lines(random(player, "nudge.1", "nudge.2", "nudge.3")), false);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        ACTIVE.remove(id);
        CALLING.remove(id);
        NEXT_NUDGE.remove(id);
        NEXT_HIT.remove(id);
        ARMED.remove(id);
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        Tag story = event.getOriginal().getPersistentData().get(DATA);
        if (story != null) {
            event.getEntity().getPersistentData().put(DATA, story.copy());
        }
    }
}
