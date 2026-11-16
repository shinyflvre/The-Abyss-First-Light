package net.yezon.theabyss.ring;

import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.yezon.theabyss.TheabyssMod;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public final class PotionCircle {
    private static final Map<String, Integer> COLOURS = Map.ofEntries(
        Map.entry("anti_fear_essence", 0xD856D3),
        Map.entry("anti_infect_essence", 0x56D8BF),
        Map.entry("bottle_of_somnium", 0x56D8BF),
        Map.entry("enchanted_bottle_of_somnium", 0x56D8BF),
        Map.entry("immortal_substance", 0xD86C51),
        Map.entry("lurker_juice", 0x7AD3D8),
        Map.entry("somnium_cooldown_upgrade", 0xD855A3),
        Map.entry("somnium_damage_upragde", 0xD8994B),
        Map.entry("somnium_regen_upgrade", 0x7C60D8),
        Map.entry("somnium_upgrade", 0x4AADD8),
        Map.entry("somnium_ward", 0x38C5DE),
        Map.entry("draught_of_stillness", 0x7C6CD8),
        Map.entry("shared_heart", 0xD83A55)
    );

    private PotionCircle() {
    }

    @SubscribeEvent
    public static void onDrink(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(event.getItem().getItem());
        if (!TheabyssMod.MODID.equals(id.getNamespace())) {
            return;
        }
        Integer colour = COLOURS.get(id.getPath());
        if (colour != null) {
            RingVisuals.potionCircle(level, entity, colour);
        }
    }
}
