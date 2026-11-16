package net.yezon.theabyss.item;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.yezon.theabyss.TheabyssMod;

public final class TheabyssModArmorMaterials {
    public static final ArmorMaterial ABERYTHE = new ArmorMaterial(
        34, defense(4, 6, 7, 4), 15, SoundEvents.ARMOR_EQUIP_DIAMOND, 3.5F, 0.1F, TheabyssModItemTags.REPAIRS_ABERYTHE, asset("aberythe_armor")
    );
    public static final ArmorMaterial BONE = new ArmorMaterial(
        15, defense(4, 4, 4, 4), 10, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.1F, TheabyssModItemTags.REPAIRS_BONE, asset("bone_armor")
    );
    public static final ArmorMaterial FUSION = new ArmorMaterial(
        70, defense(6, 6, 7, 5), 100, SoundEvents.ARMOR_EQUIP_DIAMOND, 3.5F, 0.2F, TheabyssModItemTags.REPAIRS_FUSION, asset("fusion_armor")
    );
    public static final ArmorMaterial GARNITE = new ArmorMaterial(
        35, defense(5, 6, 6, 5), 15, SoundEvents.ARMOR_EQUIP_DIAMOND, 3.5F, 0.4F, TheabyssModItemTags.REPAIRS_GARNITE, asset("garnite_armor")
    );
    public static final ArmorMaterial GLACERYTHE = new ArmorMaterial(
        30, defense(5, 5, 6, 3), 15, SoundEvents.ARMOR_EQUIP_IRON, 4.0F, 0.3F, TheabyssModItemTags.REPAIRS_GLACERYTHE, asset("glacerythe_armor")
    );
    public static final ArmorMaterial IGNISITHE = new ArmorMaterial(
        32, defense(4, 5, 6, 5), 15, SoundEvents.ARMOR_EQUIP_DIAMOND, 3.5F, 0.1F, TheabyssModItemTags.REPAIRS_IGNISITHE, asset("ignisithe_armor")
    );
    public static final ArmorMaterial INCORYTHE = new ArmorMaterial(
        65, defense(5, 7, 8, 5), 45, SoundEvents.ARMOR_EQUIP_NETHERITE, 5.0F, 0.3F, TheabyssModItemTags.REPAIRS_INCORYTHE, asset("incorythe_armor")
    );
    public static final ArmorMaterial PHANTOM = new ArmorMaterial(
        50, defense(5, 6, 8, 4), 20, SoundEvents.ARMOR_EQUIP_NETHERITE, 4.0F, 0.2F, TheabyssModItemTags.REPAIRS_PHANTOM, asset("phantom_armor")
    );
    public static final ArmorMaterial UNORITHE = new ArmorMaterial(
        100, defense(5, 7, 8, 6), 40, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.4F, TheabyssModItemTags.REPAIRS_UNORITHE, asset("unorithe_armor")
    );

    private TheabyssModArmorMaterials() {
    }

    private static Map<ArmorType, Integer> defense(int boots, int leggings, int chestplate, int helmet) {
        Map<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        map.put(ArmorType.BOOTS, boots);
        map.put(ArmorType.LEGGINGS, leggings);
        map.put(ArmorType.CHESTPLATE, chestplate);
        map.put(ArmorType.HELMET, helmet);
        return map;
    }

    private static ResourceKey<EquipmentAsset> asset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(TheabyssMod.MODID, name));
    }
}
