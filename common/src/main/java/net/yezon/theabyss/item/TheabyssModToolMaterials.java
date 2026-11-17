package net.yezon.theabyss.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public final class TheabyssModToolMaterials {
    public static final ToolMaterial ABERYTHE = family(1750, 20, TheabyssModItemTags.REPAIRS_ABERYTHE);
    public static final ToolMaterial ABYSS = family(4000, 40, TheabyssModItemTags.REPAIRS_NONE);
    public static final ToolMaterial BONE = family(512, 20, TheabyssModItemTags.REPAIRS_BONE);
    public static final ToolMaterial BRICKED_KNIGHT = family(1664, 10, TheabyssModItemTags.REPAIRS_NONE);
    public static final ToolMaterial FUSION = family(2228, 20, TheabyssModItemTags.REPAIRS_FUSION);
    public static final ToolMaterial GARNITE = family(1800, 20, TheabyssModItemTags.REPAIRS_GARNITE);
    public static final ToolMaterial IGNISITHE = family(1024, 25, TheabyssModItemTags.REPAIRS_IGNISITHE);
    public static final ToolMaterial INCORYTHE = family(3512, 20, TheabyssModItemTags.REPAIRS_INCORYTHE);
    public static final ToolMaterial KNIGHT = family(1664, 5, TheabyssModItemTags.REPAIRS_KNIGHT);
    public static final ToolMaterial NOSAJ = family(10000, 40, TheabyssModItemTags.REPAIRS_NONE);
    public static final ToolMaterial PHANTOM = family(2512, 20, TheabyssModItemTags.REPAIRS_PHANTOM);
    public static final ToolMaterial PHANTOM_SWORD = family(2512, 20, TheabyssModItemTags.REPAIRS_PHANTOM_SWORD);
    public static final ToolMaterial UNORITHE = family(3000, 20, TheabyssModItemTags.REPAIRS_UNORITHE);

    private TheabyssModToolMaterials() {
    }

    private static ToolMaterial family(int durability, int enchantmentValue, TagKey<Item> repairItems) {
        return new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, durability, 0.0F, 0.0F, enchantmentValue, repairItems);
    }

    public static ToolMaterial mining(ToolMaterial family, int miningLevel, float miningSpeed) {
        return new ToolMaterial(
            incorrectBlocksForDrops(miningLevel), family.durability(), miningSpeed, family.attackDamageBonus(), family.enchantmentValue(), family.repairItems()
        );
    }

    public static TagKey<Block> incorrectBlocksForDrops(int miningLevel) {
        return switch (miningLevel) {
            case 0 -> BlockTags.INCORRECT_FOR_WOODEN_TOOL;
            case 1 -> BlockTags.INCORRECT_FOR_STONE_TOOL;
            case 2 -> BlockTags.INCORRECT_FOR_IRON_TOOL;
            case 3 -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
            default -> BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        };
    }

    public static Item.Properties tool(Item.Properties properties, ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline) {
        return properties.attributes(attributes(material, attackDamageBaseline, attackSpeedBaseline));
    }

    public static ItemAttributeModifiers attributes(ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline) {
        return DiggerItem.createAttributes(material, attackDamageBaseline, attackSpeedBaseline);
    }

    public record ToolMaterial(TagKey<Block> incorrectBlocksForDrops, int durability, float speed, float attackDamageBonus, int enchantmentValue,
                               TagKey<Item> repairItems) implements Tier {
        @Override
        public int getUses() {
            return this.durability;
        }

        @Override
        public float getSpeed() {
            return this.speed;
        }

        @Override
        public float getAttackDamageBonus() {
            return this.attackDamageBonus;
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return this.incorrectBlocksForDrops;
        }

        @Override
        public int getEnchantmentValue() {
            return this.enchantmentValue;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(this.repairItems);
        }
    }
}
