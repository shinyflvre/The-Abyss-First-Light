package net.yezon.theabyss.item;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.yezon.theabyss.platform.event.EventPriority;
import net.yezon.theabyss.platform.event.SubscribeEvent;
import net.yezon.theabyss.platform.event.EventBusSubscriber;
import net.yezon.theabyss.platform.event.entity.living.LivingIncomingDamageEvent;
import net.yezon.theabyss.TheabyssMod;
import net.yezon.theabyss.util.AbyssTooltip;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TheabyssMod.MODID)
public class AbyssBowItem extends BowItem {
    public static final int DURABILITY = 576;
    private final String id;
    private final float bonusDamage;
    private final float range;

    public AbyssBowItem(Item.Properties properties, String id, float bonusDamage, float range) {
        super(properties);
        this.id = id;
        this.bonusDamage = bonusDamage;
        this.range = range;
    }

    @Override
    protected void shoot(ServerLevel level, LivingEntity shooter, InteractionHand hand, ItemStack weapon, List<ItemStack> projectiles, float power,
                         float uncertainty, boolean isCrit, @Nullable LivingEntity targetOverride) {
        super.shoot(level, shooter, hand, weapon, projectiles, power * this.range, uncertainty, isCrit, targetOverride);
    }

    @Override
    public int getDefaultProjectileRange() {
        return Math.round(DEFAULT_RANGE * this.range);
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> tooltipLines, TooltipFlag flag) {
        Consumer<Component> tooltip = tooltipLines::add;
        AbyssTooltip.ability(tooltip, "tooltip.theabyss." + this.id + ".ability", "tooltip.theabyss." + this.id + ".text");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onIncoming(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof AbstractArrow arrow)) {
            return;
        }
        ItemStack weapon = arrow.getWeaponItem();
        if (weapon == null || !(weapon.getItem() instanceof AbyssBowItem bow)) {
            return;
        }
        event.setAmount(event.getAmount() / bow.range + bow.bonusDamage);
    }
}
