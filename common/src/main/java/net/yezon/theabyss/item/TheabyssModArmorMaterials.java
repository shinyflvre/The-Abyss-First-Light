package net.yezon.theabyss.item;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.platform.registries.DeferredHolder;
import net.yezon.theabyss.platform.registries.DeferredRegister;

public final class TheabyssModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> REGISTRY = DeferredRegister.create(Registries.ARMOR_MATERIAL, TheabyssMod.MODID);

    public static final Material ABERYTHE = register(
        "aberythe_armor", 34, defense(4, 6, 7, 4), 15, SoundEvents.ARMOR_EQUIP_DIAMOND, 3.5F, 0.1F, TheabyssModItemTags.REPAIRS_ABERYTHE
    );
    public static final Material BONE = register(
        "bone_armor", 15, defense(4, 4, 4, 4), 10, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.1F, TheabyssModItemTags.REPAIRS_BONE
    );
    public static final Material FUSION = register(
        "fusion_armor", 70, defense(6, 6, 7, 5), 100, SoundEvents.ARMOR_EQUIP_DIAMOND, 3.5F, 0.2F, TheabyssModItemTags.REPAIRS_FUSION
    );
    public static final Material GARNITE = register(
        "garnite_armor", 35, defense(5, 6, 6, 5), 15, SoundEvents.ARMOR_EQUIP_DIAMOND, 3.5F, 0.4F, TheabyssModItemTags.REPAIRS_GARNITE
    );
    public static final Material GLACERYTHE = register(
        "glacerythe_armor", 30, defense(5, 5, 6, 3), 15, SoundEvents.ARMOR_EQUIP_IRON, 4.0F, 0.3F, TheabyssModItemTags.REPAIRS_GLACERYTHE
    );
    public static final Material IGNISITHE = register(
        "ignisithe_armor", 32, defense(4, 5, 6, 5), 15, SoundEvents.ARMOR_EQUIP_DIAMOND, 3.5F, 0.1F, TheabyssModItemTags.REPAIRS_IGNISITHE
    );
    public static final Material INCORYTHE = register(
        "incorythe_armor", 65, defense(5, 7, 8, 5), 45, SoundEvents.ARMOR_EQUIP_NETHERITE, 5.0F, 0.3F, TheabyssModItemTags.REPAIRS_INCORYTHE
    );
    public static final Material PHANTOM = register(
        "phantom_armor", 50, defense(5, 6, 8, 4), 20, SoundEvents.ARMOR_EQUIP_NETHERITE, 4.0F, 0.2F, TheabyssModItemTags.REPAIRS_PHANTOM
    );
    public static final Material UNORITHE = register(
        "unorithe_armor", 100, defense(5, 7, 8, 6), 40, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.4F, TheabyssModItemTags.REPAIRS_UNORITHE
    );

    private TheabyssModArmorMaterials() {
    }

    public static void init() {
    }

    public record Material(Holder<ArmorMaterial> holder, int durability) {
        public Item.Properties properties(Item.Properties properties, ArmorItem.Type type) {
            return properties.durability(type.getDurability(this.durability));
        }
    }

    private static Material register(String name, int durability, Map<ArmorItem.Type, Integer> defense, int enchantmentValue, Holder<SoundEvent> equipSound,
                                      float toughness, float knockbackResistance, TagKey<Item> repairItems) {
        DeferredHolder<ArmorMaterial, ArmorMaterial> holder = REGISTRY.register(name, () -> new ArmorMaterial(
            defense, enchantmentValue, equipSound, () -> Ingredient.of(repairItems),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, name))), toughness, knockbackResistance
        ));
        return new Material(holder.asHolder(), durability);
    }

    private static Map<ArmorItem.Type, Integer> defense(int boots, int leggings, int chestplate, int helmet) {
        Map<ArmorItem.Type, Integer> map = new EnumMap<>(ArmorItem.Type.class);
        map.put(ArmorItem.Type.BOOTS, boots);
        map.put(ArmorItem.Type.LEGGINGS, leggings);
        map.put(ArmorItem.Type.CHESTPLATE, chestplate);
        map.put(ArmorItem.Type.HELMET, helmet);
        return map;
    }
}
