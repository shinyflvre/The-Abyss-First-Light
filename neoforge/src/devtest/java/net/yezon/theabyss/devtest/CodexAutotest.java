package net.yezon.theabyss.devtest;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.yezon.theabyss.client.codex.CodexScreen;

final class CodexAutotest {
    private CodexAutotest() {
    }

    static void addSteps(PossessionAutotest.StepAdder add, Consumer<String> command, Consumer<String> shot) {
        Minecraft mc = Minecraft.getInstance();
        add.add("codex open", CodexScreen::open, 20);
        add.add("codex shot landing", () -> shot.accept("97_codex_landing"), 10);
        add.add("codex click category", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] codex click category handled: {}", CodexScreen.clickBook(205, 53, false)), 20);
        add.add("codex shot clicked category", () -> shot.accept("97_codex_click_1_category"), 10);
        add.add("codex click entry", () -> TheabyssDevTest.LOGGER.info("[ABYSS-AUTOTEST] codex click entry handled: {}", CodexScreen.clickBook(200, 42, false)), 20);
        add.add("codex shot clicked entry", () -> shot.accept("97_codex_click_2_entry"), 10);
        add.add("codex click next", () -> CodexScreen.clickBook(322, 214, false), 20);
        add.add("codex shot next", () -> shot.accept("97_codex_click_3_next_page"), 10);
        add.add("codex right click back", () -> CodexScreen.clickBook(100, 100, true), 20);
        add.add("codex shot back", () -> shot.accept("97_codex_click_4_back"), 10);
        add.add("codex spells", () -> CodexScreen.openCategory("spells"), 20);
        add.add("codex shot spells", () -> shot.accept("97_codex_category_spells"), 10);
        add.add("codex armors", () -> CodexScreen.openCategory("armors"), 20);
        add.add("codex shot armors", () -> shot.accept("97_codex_category_armors"), 10);
        String[][] views = {{"ring_of_teleport", "0"}, {"ring_of_teleport", "1"}, {"arcane", "0"}, {"arcane", "1"}, {"cooldown_upgr", "1"},
            {"portal", "0"}, {"portal", "3"}, {"ring_of_ender_chest", "0"}, {"ring_of_possession", "1"}, {"incorythe_armor", "0"},
            {"incorythe_armor", "1"}, {"abyss_sword", "0"}, {"clock_of_time", "0"}, {"eye_of_abyss", "1"}};
        for (String[] v : views) {
            add.add("codex " + v[0] + " " + v[1], () -> CodexScreen.openEntry(v[0], Integer.parseInt(v[1])), 20);
            add.add("codex shot " + v[0] + " " + v[1], () -> shot.accept("97_codex_" + v[0] + "_" + v[1]), 10);
        }
        add.add("codex close", () -> mc.setScreen(null), 5);
    }
}
