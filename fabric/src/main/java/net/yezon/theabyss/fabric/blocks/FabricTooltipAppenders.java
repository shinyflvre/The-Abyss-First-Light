package net.yezon.theabyss.fabric.blocks;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.yezon.theabyss.platform.common.tooltip.TooltipAppender;
import net.yezon.theabyss.platform.common.tooltip.TooltipLocation;
import org.jspecify.annotations.Nullable;

public final class FabricTooltipAppenders {
    private static final Map<TooltipLocation, List<TooltipAppender>> APPENDERS = new EnumMap<>(TooltipLocation.class);

    private FabricTooltipAppenders() {
    }

    static synchronized void add(TooltipLocation location, TooltipAppender appender) {
        APPENDERS.computeIfAbsent(location, k -> new ArrayList<>()).add(appender);
    }

    public static void append(TooltipLocation location, ItemStack stack, Item.TooltipContext context, TooltipDisplay display, @Nullable Player player,
                              TooltipFlag flag, Consumer<Component> builder) {
        List<TooltipAppender> appenders = APPENDERS.get(location);
        if (appenders == null) {
            return;
        }
        for (TooltipAppender appender : appenders) {
            appender.append(stack, context, display, player, flag, builder);
        }
    }
}
