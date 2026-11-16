package net.yezon.theabyss.rift;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.yezon.theabyss.configuration.ConfigConfiguration;

final class RiftTopics {
    private record Story(String id, int intro, int[] replies, int[] pages, boolean lore, int needsDeed, boolean repeat) {
    }

    private static Story story(String id, int intro, int... replies) {
        return new Story(id, intro, replies, new int[0], false, 0, false);
    }

    private static Story page(String id, int intro, int[] replies, int... pages) {
        return new Story(id, intro, replies, pages, false, 0, false);
    }

    private static Story lorePage(String id, int intro, int[] replies, int... pages) {
        return new Story(id, intro, replies, pages, true, 0, false);
    }

    private static final List<Story> STORIES = List.of(
        lorePage("black", 3, new int[] {3, 4, 3}, 3),
        page("rings", 3, new int[] {3, 3, 3}, 4),
        page("roots", 3, new int[] {4, 4, 3}, 5),
        page("wolf", 3, new int[] {4, 3, 3}, 6),
        page("kneeling", 3, new int[] {3, 3, 3}, 7),
        page("voice", 3, new int[] {3, 3, 3}, 8),
        page("back", 3, new int[] {3, 3, 3}, 9),
        page("hand", 3, new int[] {3, 3, 3}, 10),
        new Story("giants", 3, new int[] {3, 3, 3}, new int[] {11}, false, 6, false),
        page("cold", 3, new int[] {3, 3, 3}, 12),
        lorePage("pillars", 3, new int[] {3, 3, 3}, 13),
        lorePage("plea", 3, new int[] {3, 3, 3}, 15),
        page("altar", 4, new int[] {3, 4, 3, 3}, 14, 16),
        story("count", 2, 3, 3),
        story("small", 2, 3, 2),
        new Story("away", 2, new int[] {2, 2}, new int[0], false, 0, true),
        story("visits10", 1),
        story("visits25", 1),
        story("visits50", 1, 2),
        story("time", 2, 2, 2),
        story("frag1", 2, 2),
        story("frag2", 3, 1),
        story("frag3", 3, 2),
        story("frag4", 3, 2),
        story("frag5", 2, 2),
        story("frag6", 2, 2),
        story("frag7", 2, 2),
        story("frag8", 2, 1),
        story("slip", 2, 2),
        new Story("wounded", 2, new int[] {2}, new int[0], false, 0, true)
    );
    private static final Story DOUBT = story("doubt", 1, 3, 3, 3, 3);
    private static final String[] FRAGMENTS = {"frag1", "frag2", "frag3", "frag4", "frag5", "frag6", "frag7", "frag8"};
    private static final String[] AFTER = {"after.1", "after.2", "after.3", "after.4"};
    private static final String[] TRANSITIONS = {"transition.1", "transition.2", "transition.3", "transition.4", "transition.5", "transition.6"};
    private static final int BACK_DOUBT_QUESTION = 2;
    private static final int HOME_ANSWERS = 3;
    private static final int HOME_INTRO = 2;
    private static final int NAME_INTRO = 1;
    private static final int REPLY = 3;
    private static final int HOME_AFTER = 2;
    private static final int DOUBT_AFTER = 3;
    private static final int NAME_AFTER = 4;
    private static final int LATE_AFTER = 6;
    private static final int MAX_CHOICES = 4;
    private static final int KILLS = 100;
    private static final long ABYSS_TICKS = 10L * 60L * 60L * 20L;
    private static final long AWAY_DAYS = 3L;
    private static final long DAY = 24000L;
    private static final int FRAGMENT_CHANCE = 3;
    private static final int SLIP_CHANCE = 30;
    private static final int WOUNDED_CHANCE = 5;
    private static final int SATURATION_TICKS = 600;
    private static final String WOUNDED = "wounded";
    private static final String QUEUE = "topicQueue";
    private static final String HEARD = "topicHeard";

    private RiftTopics() {
    }

    private static List<String> list(ServerPlayer player, String key) {
        String value = RiftStory.data(player).getStringOr(key, "");
        return value.isEmpty() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(value.split(",")));
    }

    private static void store(ServerPlayer player, String key, List<String> values) {
        RiftStory.data(player).putString(key, String.join(",", values));
    }

    private static Story find(String id) {
        if (DOUBT.id().equals(id)) {
            return DOUBT;
        }
        for (Story story : STORIES) {
            if (story.id().equals(id)) {
                return story;
            }
        }
        return null;
    }

    private static boolean unlock(ServerPlayer player, String id) {
        Story story = find(id);
        if (story == null || story.lore() && !ConfigConfiguration.loresEnabled()) {
            return false;
        }
        List<String> queue = list(player, QUEUE);
        if (queue.contains(id) || !story.repeat() && list(player, HEARD).contains(id)) {
            return false;
        }
        queue.add(id);
        store(player, QUEUE, queue);
        return true;
    }

    static void onDeed(ServerPlayer player, int page) {
        for (Story story : STORIES) {
            for (int p : story.pages()) {
                if (p == page && unlock(player, story.id())) {
                    RiftStory.requestWhisper(player);
                }
            }
        }
    }

    static void unlockEvent(ServerPlayer player, String id) {
        if (unlock(player, id)) {
            RiftStory.requestWhisper(player);
        }
    }

    static void backfill(ServerPlayer player) {
        for (Story story : STORIES) {
            for (int p : story.pages()) {
                if (RiftStory.deed(player, p)) {
                    unlock(player, story.id());
                }
            }
        }
    }

    static void onKill(ServerPlayer player) {
        CompoundTag data = RiftStory.data(player);
        int kills = data.getIntOr("abyssKills", 0) + 1;
        data.putInt("abyssKills", kills);
        if (kills == KILLS) {
            unlockEvent(player, "count");
        }
    }

    static void onAbyssTick(ServerPlayer player) {
        CompoundTag data = RiftStory.data(player);
        long ticks = data.getLongOr("abyssTicks", 0L) + 1L;
        data.putLong("abyssTicks", ticks);
        if (ticks == ABYSS_TICKS) {
            unlockEvent(player, "time");
        }
    }

    static boolean onArrive(ServerPlayer player, boolean done) {
        CompoundTag data = RiftStory.data(player);
        int visits = data.getIntOr("visits", 0) + 1;
        data.putInt("visits", visits);
        long day = player.level().getServer().overworld().getGameTime() / DAY;
        long last = data.getLongOr("lastVisitDay", -1L);
        data.putLong("lastVisitDay", day);
        if (!done) {
            return false;
        }
        if (visits == 10 || visits == 25 || visits == 50) {
            unlock(player, "visits" + visits);
        }
        if (last >= 0L && day - last >= AWAY_DAYS) {
            unlock(player, "away");
        }
        List<String> queue = list(player, QUEUE);
        List<String> heard = list(player, HEARD);
        if (!queue.contains("slip") && !heard.contains("slip") && player.getRandom().nextInt(SLIP_CHANCE) == 0) {
            unlock(player, "slip");
        } else if (queue.stream().noneMatch(id -> id.startsWith("frag")) && player.getRandom().nextInt(FRAGMENT_CHANCE) == 0) {
            List<String> left = new ArrayList<>();
            for (String fragment : FRAGMENTS) {
                if (!heard.contains(fragment)) {
                    left.add(fragment);
                }
            }
            if (!left.isEmpty()) {
                unlock(player, left.get(player.getRandom().nextInt(left.size())));
            }
        }
        if (player.getHealth() < player.getMaxHealth() / 2.0F && player.getRandom().nextInt(WOUNDED_CHANCE) == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, SATURATION_TICKS, 1));
            open(player, find(WOUNDED), new ArrayList<>());
            return true;
        }
        return false;
    }

    private static boolean eligible(ServerPlayer player, Story story) {
        return story != null && (!story.lore() || ConfigConfiguration.loresEnabled()) && (story.needsDeed() == 0 || RiftStory.deed(player, story.needsDeed()));
    }

    private static Story nextPending(ServerPlayer player) {
        Story plea = null;
        Story altar = null;
        for (String id : list(player, QUEUE)) {
            Story story = find(id);
            if (!eligible(player, story)) {
                continue;
            }
            if (id.equals("plea")) {
                plea = story;
            } else if (id.equals("altar")) {
                altar = story;
            } else {
                return story;
            }
        }
        return plea != null ? plea : altar;
    }

    private static int heardCount(ServerPlayer player) {
        return list(player, HEARD).size();
    }

    private static boolean homeDue(ServerPlayer player) {
        return heardCount(player) >= HOME_AFTER && !RiftStory.data(player).getBooleanOr("homeDone", false);
    }

    private static boolean nameDue(ServerPlayer player) {
        return heardCount(player) >= NAME_AFTER && !RiftStory.data(player).getBooleanOr("nameDone", false);
    }

    static boolean pending(ServerPlayer player) {
        backfill(player);
        return nextPending(player) != null || homeDue(player) || nameDue(player);
    }

    static void autoStart(ServerPlayer player) {
        backfill(player);
        continueWith(player, new ArrayList<>(), false);
    }

    static void talk(ServerPlayer player) {
        backfill(player);
        if (nextPending(player) != null || homeDue(player) || nameDue(player)) {
            continueWith(player, new ArrayList<>(), false);
        } else {
            menu(player, RiftStory.lines(RiftStory.random(player, AFTER)));
        }
    }

    private static void continueWith(ServerPlayer player, List<String> said, boolean transition) {
        Story next = nextPending(player);
        boolean more = next != null || homeDue(player) || nameDue(player);
        if (more && transition) {
            said.addAll(RiftStory.lines(RiftStory.random(player, TRANSITIONS)));
        }
        if (next != null) {
            open(player, next, said);
        } else if (homeDue(player)) {
            home(player, said);
        } else if (nameDue(player)) {
            name(player, said);
        } else {
            menu(player, said.isEmpty() ? RiftStory.lines(RiftStory.random(player, AFTER)) : said);
        }
    }

    static void menu(ServerPlayer player, List<String> lines) {
        RiftStory.ask(player, lines, menuActions(player), true);
    }

    private static List<String> menuActions(ServerPlayer player) {
        List<String> actions = new ArrayList<>();
        if (heardCount(player) >= DOUBT_AFTER && !remaining(player, DOUBT).isEmpty()) {
            actions.add("t:" + DOUBT.id());
        }
        actions.add("d:gifts");
        actions.add("d:change");
        actions.add(heardCount(player) >= LATE_AFTER ? "d:leave_we" : "d:leave");
        return actions;
    }

    private static List<String> remaining(ServerPlayer player, Story story) {
        int asked = RiftStory.data(player).getIntOr("asked." + story.id(), 0);
        List<String> actions = new ArrayList<>();
        for (int i = 0; i < story.replies().length; i++) {
            boolean hidden = story.id().equals("back") && i == BACK_DOUBT_QUESTION && !ConfigConfiguration.loresEnabled();
            if ((asked & 1 << i) == 0 && !hidden) {
                actions.add("q:" + story.id() + ".q" + (i + 1));
            }
        }
        return actions;
    }

    private static List<String> withEnough(List<String> questions, Story story) {
        if (questions.size() < MAX_CHOICES) {
            questions.add("e:" + story.id());
        }
        return questions;
    }

    private static void open(ServerPlayer player, Story story, List<String> said) {
        if (story == null) {
            return;
        }
        if (story != DOUBT) {
            List<String> queue = list(player, QUEUE);
            queue.remove(story.id());
            store(player, QUEUE, queue);
            if (story.repeat()) {
                RiftStory.data(player).remove("asked." + story.id());
            } else {
                List<String> heard = list(player, HEARD);
                if (!heard.contains(story.id())) {
                    heard.add(story.id());
                    store(player, HEARD, heard);
                }
            }
        }
        for (int i = 1; i <= story.intro(); i++) {
            said.add(RiftStory.LINE + "topic." + story.id() + ".intro." + i);
        }
        List<String> questions = remaining(player, story);
        if (questions.isEmpty()) {
            continueWith(player, said, true);
        } else {
            RiftStory.ask(player, said, withEnough(questions, story), !story.id().equals(WOUNDED));
        }
    }

    private static void answer(ServerPlayer player, String question) {
        int dot = question.lastIndexOf(".q");
        Story story = find(question.substring(0, dot));
        if (story == null) {
            return;
        }
        int index = Integer.parseInt(question.substring(dot + 2)) - 1;
        CompoundTag data = RiftStory.data(player);
        data.putInt("asked." + story.id(), data.getIntOr("asked." + story.id(), 0) | 1 << index);
        List<String> said = new ArrayList<>();
        for (int i = 1; i <= story.replies()[index]; i++) {
            said.add(RiftStory.LINE + "topic." + story.id() + ".q" + (index + 1) + "." + i);
        }
        List<String> left = remaining(player, story);
        if (story.id().equals(WOUNDED)) {
            RiftStory.say(player, said, true);
        } else if (left.isEmpty()) {
            continueWith(player, said, true);
        } else {
            RiftStory.ask(player, said, withEnough(left, story), true);
        }
    }

    private static void home(ServerPlayer player, List<String> said) {
        for (int i = 1; i <= HOME_INTRO; i++) {
            said.add(RiftStory.LINE + "topic.home.intro." + i);
        }
        List<String> actions = new ArrayList<>();
        for (int i = 1; i <= HOME_ANSWERS; i++) {
            actions.add("h:home.a" + i);
        }
        RiftStory.ask(player, said, actions, true);
    }

    private static void name(ServerPlayer player, List<String> said) {
        for (int i = 1; i <= NAME_INTRO; i++) {
            said.add(RiftStory.LINE + "topic.name.intro." + i);
        }
        RiftStory.ask(player, said, List.of("h:name.a1"), true);
    }

    private static void memory(ServerPlayer player, String answer) {
        String which = answer.startsWith("home") ? "home" : "name";
        RiftStory.data(player).putBoolean(which + "Done", true);
        List<String> said = new ArrayList<>();
        for (int i = 1; i <= REPLY; i++) {
            said.add(RiftStory.LINE + "topic." + which + ".reply." + i);
        }
        continueWith(player, said, true);
    }

    static boolean handle(ServerPlayer player, String group, String name) {
        switch (group) {
            case "t" -> open(player, find(name), new ArrayList<>());
            case "q" -> answer(player, name);
            case "e" -> {
                if (!name.equals(WOUNDED)) {
                    continueWith(player, new ArrayList<>(), true);
                }
            }
            case "h" -> memory(player, name);
            default -> {
                return false;
            }
        }
        return true;
    }
}
