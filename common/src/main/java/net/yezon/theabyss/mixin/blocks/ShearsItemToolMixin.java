package net.yezon.theabyss.mixin.blocks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import net.yezon.theabyss.TheabyssMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShearsItem.class)
public abstract class ShearsItemToolMixin {
    @Unique
    private static final TagKey<Block> THEABYSS$MINOR_SPEED = TagKey.create(Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(TheabyssMod.MODID, "shears_minor_breaking_speed"));

    @ModifyReturnValue(method = "createToolProperties", at = @At("RETURN"))
    private static Tool theabyss$abyssShearRules(Tool original) {
        List<Tool.Rule> rules = new ArrayList<>(original.rules());
        rules.add(Tool.Rule.overrideSpeed(THEABYSS$MINOR_SPEED, 2.0F));
        return new Tool(rules, original.defaultMiningSpeed(), original.damagePerBlock());
    }
}
