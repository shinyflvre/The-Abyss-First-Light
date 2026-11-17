package net.yezon.theabyss.client.renderer;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.compat.ARGB;
import net.yezon.theabyss.init.TheabyssModBlockEntities;
import net.yezon.theabyss.init.TheabyssModItems;
import net.yezon.theabyss.init.TheabyssModWoodSets;
import net.yezon.theabyss.platform.Dist;
import net.yezon.theabyss.platform.client.event.EntityRenderersEvent;
import net.yezon.theabyss.platform.client.event.RegisterColorHandlersEvent;
import net.yezon.theabyss.platform.client.event.RegisterItemPropertiesEvent;
import net.yezon.theabyss.platform.client.event.RegisterShadersEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.SubscribeEvent;

@EventBusSubscriber(modid = TheabyssMod.MODID, value = Dist.CLIENT)
public final class AbyssRenderSetup {
    private static final ResourceLocation PULL = ResourceLocation.withDefaultNamespace("pull");
    private static final ResourceLocation PULLING = ResourceLocation.withDefaultNamespace("pulling");
    private static final int INFECTED_PLANT_TINT = 0xFF6A7697;

    private AbyssRenderSetup() {
    }

    @SubscribeEvent
    public static void registerItemProperties(RegisterItemPropertiesEvent event) {
        ClampedItemPropertyFunction pull = (stack, level, entity, seed) -> entity == null || entity.getUseItem() != stack ? 0.0F
            : (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0F;
        ClampedItemPropertyFunction pulling = (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F;
        for (Item bow : new Item[] {TheabyssModItems.CRYSTAL_BOW.get(), TheabyssModItems.NIGHTGROVE_LONGBOW.get()}) {
            event.register(bow, PULL, pull);
            event.register(bow, PULLING, pulling);
        }
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof SpawnEggItem egg && TheabyssMod.MODID.equals(BuiltInRegistries.ITEM.getKey(item).getNamespace())) {
                event.register((stack, tintIndex) -> ARGB.opaque(egg.getColor(tintIndex)), egg);
            }
        }
        event.register((stack, tintIndex) -> tintIndex == 0 ? INFECTED_PLANT_TINT : -1, TheabyssModItems.INFECTED_BUSH.get(),
            TheabyssModItems.INFECTED_SHORT_GRASS.get(), TheabyssModItems.TALL_INFECTED_GRASS.get());
        event.register((stack, tintIndex) -> tintIndex == 0 ? ARGB.opaque(GrassColor.get(0.5, 1.0)) : -1, TheabyssModItems.INFECTED_GRASS.get());
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        AbyssRenderTypes.registerShaders(event);
        net.yezon.theabyss.client.fx.PostFx.registerShaders(event);
    }

    @SubscribeEvent
    public static void registerShelfRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TheabyssModBlockEntities.SHELF.get(), AbyssShelfRenderer::new);
    }

    @SubscribeEvent
    public static void registerSignMaterials(EntityRenderersEvent.RegisterRenderers event) {
        for (TheabyssModWoodSets.WoodSet set : TheabyssModWoodSets.ALL) {
            WoodType type = set.woodType();
            ResourceLocation id = ResourceLocation.parse(type.name());
            Sheets.SIGN_MATERIALS.putIfAbsent(type, new Material(Sheets.SIGN_SHEET, id.withPrefix("entity/signs/")));
            Sheets.HANGING_SIGN_MATERIALS.putIfAbsent(type, new Material(Sheets.SIGN_SHEET, id.withPrefix("entity/signs/hanging/")));
        }
    }
}
