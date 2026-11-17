package net.yezon.theabyss.client.renderer;

import java.util.function.BiConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.yezon.theabyss.TheabyssMod;

public final class BlockRenderLayers {
    private static final String[] CUTOUT = {
        "abyss_crystal_ore", "abyssal_energetica", "anima_fire", "arcane_workbench", "ashen_bramble_vines", "ashen_bramble_vines_plant",
        "aurel_crystal_ore", "blaru_door", "blaru_grass", "blaru_leave_carpet", "blaru_leaves", "blaru_starpetals", "blaru_trap_door",
        "blossom_rank", "blue_cave_shroom", "bog_door", "bog_log_fungi", "bulb_rank", "caecus_versa", "caverna_crystal_ore", "coilvine",
        "coilvine_plant", "crimson_crystal_ore", "crookstalk", "crystal_cutter", "dusk_coral", "duskveil_blossoms", "ender_crystal_ore",
        "ender_hanging_lantern", "ender_lantern", "exolius_plant", "flos_vitae", "fortis_ultima", "frost_crystal_ore", "frozen_door",
        "frozen_leaves", "gauntlet_station", "gloom_groove", "glow_shroom", "glowing_blaru_moss", "heal_camp_fire",
        "hoglin_hanging_lantern", "hoglin_lantern", "hollow_crystal_ore", "hookleaf", "ice_rank", "icela_rank", "icora_flower",
        "infected_bush", "infected_short_grass", "loran_flower", "lumen_strands", "lumen_strands_plant", "luna_door", "luna_shroom",
        "luna_trap_door", "lungo_rank", "lush_azalea", "lush_azalea_leaves", "lush_big_dripleaf", "lush_big_dripleaf_stem",
        "lush_cave_vines", "lush_cave_vines_plant", "lush_flowering_azalea", "lush_flowering_azalea_leaves", "lush_glow_lichen",
        "lush_hanging_roots", "lush_small_dripleaf", "lush_spore_blossom", "mortar_and_pestle", "night_altar_activated", "nightgrove_bush",
        "nightgrove_door", "nightgrove_grass", "nightgrove_leave_carpet", "nightgrove_leaves", "nightgrove_melon", "old_exolius_plant",
        "phantom_hanging_lantern", "phantom_lantern", "pointed_abyss_dripstone", "ralom_plant", "raptor_egg", "rena_door", "rena_shroom",
        "rena_trap_door", "research_table", "rub_shroom", "rycilian_mushroom", "sal_door", "sal_log_fungi", "saturnia_versa",
        "shadow_tendrils", "shadow_tendrils_plant", "shield_camp_fire", "shroomland_bush", "shroomland_grass", "slimed_door",
        "slimed_grass", "slimed_log_crystal", "slimed_log_fungi", "slimed_vitae", "somnium_extractor", "somnium_infuser", "soulbud_vine",
        "soulbud_vine_plant", "srala_shroom", "standing_lumen_strands", "standing_lumen_strands_plant", "stone_brick_pillar_ignited",
        "tall_blaru_grass", "tall_dusk_coral", "tall_glow_shroom", "tall_infected_grass", "tall_luna_shroom", "tall_nightgrove_grass",
        "tall_rena_shroom", "tall_rub_shroom", "tall_shroomland_grass", "tesla_rank", "thunder_flower", "toxic_sun_flower", "twisted_bloom",
        "twisted_bloom_plant", "unstable_obsidian", "vigilant_blue_grass", "vigilant_bush", "vigilant_door", "vigilant_grass",
        "vigilant_hanging_fruit", "violet_weeper", "violet_weeper_plant", "warped_crystal_ore", "wyrmcurl", "wyrmcurl_plant",
        "young_exolius_plant"
    };
    private static final String[] CUTOUT_MIPPED = {
        "infected_grass"
    };
    private static final String[] TRANSLUCENT = {
        "chiseled_gravity_stone", "frost_world_portal", "slime_block", "slimed_carpet", "the_abyss_portal"
    };

    private BlockRenderLayers() {
    }

    public static void forEach(BiConsumer<Block, RenderType> consumer) {
        add(consumer, CUTOUT, RenderType.cutout());
        add(consumer, CUTOUT_MIPPED, RenderType.cutoutMipped());
        add(consumer, TRANSLUCENT, RenderType.translucent());
    }

    private static void add(BiConsumer<Block, RenderType> consumer, String[] ids, RenderType type) {
        for (String id : ids) {
            ResourceLocation key = ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, id);
            if (BuiltInRegistries.BLOCK.containsKey(key)) {
                consumer.accept(BuiltInRegistries.BLOCK.get(key), type);
            }
        }
    }
}
